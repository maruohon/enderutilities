package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderFurnace;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.reference.ReferenceTileEntity;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class TileEntityEnderFurnace extends TileEntityEU
{
	// The values that define how fuels burn and items smelt
	public static final int COOKTIME_INC_NOFUEL = 1; // No fuel mode: 60 seconds per item
	public static final int COOKTIME_INC_SLOW = 20; // Slow/eco mode: 3 seconds per item
	public static final int COOKTIME_INC_FAST = 60; // Fast mode: 1 second per item (3x as fast)
	public static final int COOKTIME_DEFAULT = 1200; // Base cooktime per item: 3 seconds on slow

	public static final int BURNTIME_USAGE_SLOW = 20; // Slow/eco mode base usage
	public static final int BURNTIME_USAGE_FAST = 120; // Fast mode: use fuel 6x faster over time

	public static final int OUTPUT_BUFFER_SIZE = 1024; // How many items can we store in the output buffer?

	public static final int OUTPUT_INTERVAL = 20; // Only try outputting items to an Ender Chest once every 1 seconds, to try to reduce server load

	protected static final int[] SLOTS_SIDES = new int[] {0, 1, 2};

	@SideOnly(Side.CLIENT)
	public boolean isActive;
	@SideOnly(Side.CLIENT)
	public boolean usingFuel;

	public byte operatingMode;
	public byte outputMode;
	private int outputBufferAmount;
	private ItemStack outputBufferStack;

	public int burnTimeRemaining;	// Remaining burn time from the currently burning fuel
	public int burnTimeFresh;		// The time the currently burning fuel will burn in total
	public int cookTime;			// The time the currently cooking item has been cooking for
	public int cookTimeFresh;		// The total time the currently cooking item will take to finish

	private boolean isBurningLast;
	private boolean isCookingLast;

	private int timer;

	public TileEntityEnderFurnace()
	{
		super(ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
		this.itemStacks = new ItemStack[3];
		this.operatingMode = 0;
		this.outputMode = 0;
		this.burnTimeRemaining = 0;
		this.cookTime = 0;
		this.ownerName = null;
		this.ownerUUID = null;
		this.timer = 0;
		this.outputBufferAmount = 0;
	}

	/* Returns the name of the inventory */
	@Override
	public String getInventoryName()
	{
		return this.hasCustomInventoryName() ? this.customInventoryName : ReferenceTileEntity.NAME_CONTAINER_ENDER_FURNACE;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		byte flags				= nbt.getByte("Flags"); // Flags
		this.operatingMode		= (byte)(flags & 0x01);
		this.outputMode			= (byte)((flags >> 1) & 0x01);
		this.burnTimeRemaining	= nbt.getInteger("BurnTimeRemaining"); // BurnTimeRemaining
		this.burnTimeFresh		= nbt.getInteger("BurnTimeFresh"); // BurnTimeFresh
		this.cookTime			= nbt.getInteger("CookTime"); // CookTime
		this.cookTimeFresh		= nbt.getInteger("CookTimeFresh"); // CookTimeFresh

		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		this.itemStacks = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			byte slotNum = nbttagcompound.getByte("Slot");

			if (slotNum >= 0 && slotNum < this.itemStacks.length)
			{
				this.itemStacks[slotNum] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}

		if (nbt.hasKey("OutputBufferAmount", Constants.NBT.TAG_INT) == true && nbt.hasKey("OutputBufferStack", Constants.NBT.TAG_COMPOUND) == true)
		{
			this.outputBufferAmount = nbt.getInteger("OutputBufferAmount");
			this.outputBufferStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("OutputBufferStack"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		byte flags = 0;
		flags |= (this.operatingMode & 0x01);
		flags |= ((this.outputMode & 0x01) << 1);
		nbt.setByte("Flags", flags);
		nbt.setShort("BurnTimeRemaining", (short)this.burnTimeRemaining);
		nbt.setShort("BurnTimeFresh", (short)this.burnTimeFresh);
		nbt.setShort("CookTime", (short)this.cookTime);
		nbt.setShort("CookTimeFresh", (short)this.cookTimeFresh);

		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.itemStacks.length; ++i)
		{
			if (this.itemStacks[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte)i);
				this.itemStacks[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}

		nbt.setTag("Items", nbttaglist);

		if (this.outputBufferStack != null)
		{
			nbt.setTag("OutputBufferStack", this.outputBufferStack.writeToNBT(new NBTTagCompound()));
			nbt.setInteger("OutputBufferAmount", this.outputBufferAmount);
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.worldObj != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();

			byte flags = (byte)(this.getRotation() & 0x07);
			// 0x10: is cooking something, 0x20: is burning fuel, 0x40: fast mode active, 0x80: output to ender chest enabled
			if (canSmelt() == true) { flags |= 0x10; }
			if (isBurning() == true) { flags |= 0x20; }
			if (this.operatingMode == 1) { flags |= 0x40; }
			if (this.outputMode == 1) { flags |= 0x80; }
			nbt.setByte("f", flags);
			nbt.setInteger("b", this.outputBufferAmount);

			if (this.ownerName != null)
			{
				nbt.setString("o", this.ownerName);
			}

			return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
		}

		return null;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		NBTTagCompound nbt = packet.func_148857_g();
		byte flags = nbt.getByte("f");
		this.setRotation((byte)(flags & 0x07));
		this.isActive = (flags & 0x10) == 0x10;
		this.usingFuel = (flags & 0x20) == 0x20;
		this.operatingMode = (byte)((flags & 0x40) >> 6);
		this.outputMode = (byte)((flags & 0x80) >> 7);
		this.outputBufferAmount = nbt.getInteger("b");
		if (nbt.hasKey("o", Constants.NBT.TAG_STRING) == true)
		{
			this.ownerName = nbt.getString("o");
		}
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	// Returns an integer between 0 and the passed value representing how close the current item is to being completely cooked
	@SideOnly(Side.CLIENT)
	public int getCookProgressScaled(int i)
	{
		return this.cookTime * i / this.cookTimeFresh;
	}

	// Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
	// item, where 0 means that the item is exhausted and the passed value means that the item is fresh
	@SideOnly(Side.CLIENT)
	public int getBurnTimeRemainingScaled(int i)
	{
		if (this.burnTimeFresh == 0)
		{
			return 0;
		}

		return this.burnTimeRemaining * i / this.burnTimeFresh;
	}

	public boolean isBurning()
	{
		// This returns if the furnace is actually burning fuel at the moment
		return this.burnTimeRemaining > 0;
	}

	public void updateEntity()
	{
		if (this.worldObj.isRemote == true)
		{
			return;
		}

		boolean needsSync = false;
		boolean dirty = false;

		if (this.fillOutputSlotFromBuffer() == true)
		{
			needsSync = true;
		}

		int cookTimeIncrement = COOKTIME_INC_SLOW;
		if (this.burnTimeRemaining == 0 && this.hasFuelAvailable() == false)
		{
			cookTimeIncrement = COOKTIME_INC_NOFUEL;
		}
		else if (this.operatingMode == 1) // Fast mode
		{
			cookTimeIncrement = COOKTIME_INC_FAST;
		}

		// The furnace is currently burning fuel
		if (this.burnTimeRemaining > 0)
		{
			int btUse = BURNTIME_USAGE_SLOW;
			if (this.operatingMode == 1) // Fast mode
			{
				btUse = BURNTIME_USAGE_FAST;
			}

			// Not enough fuel burn time remaining for the elapsed tick
			if (btUse > this.burnTimeRemaining)
			{
				if (this.hasFuelAvailable() == true && this.canSmelt() == true)
				{
					this.burnTimeRemaining += consumeFuelItem();
				}
				// Running out of fuel, scale the cook progress according to the elapsed burn time
				else
				{
					cookTimeIncrement = (this.burnTimeRemaining * cookTimeIncrement) / btUse;
					btUse = this.burnTimeRemaining;
				}
			}

			this.burnTimeRemaining -= btUse;
			dirty = true;
		}
		else if (this.canSmelt() == true && this.hasFuelAvailable() == true)
		{
			this.burnTimeRemaining += this.consumeFuelItem();
			dirty = true;
		}

		// Valid items to smelt, room in output
		if (this.canSmelt() == true)
		{
			this.cookTimeFresh = COOKTIME_DEFAULT; // TODO: per-item cook times?
			this.cookTime += cookTimeIncrement;

			// One item done smelting
			if (this.cookTime >= this.cookTimeFresh)
			{
				this.smeltItem();
				needsSync = true;

				// We can smelt the next item and we "overcooked" the last one, carry over the extra progress
				if (this.canSmelt() == true && this.cookTime > this.cookTimeFresh)
				{
					this.cookTime -= this.cookTimeFresh;
				}
				else // No more items to smelt or didn't overcook
				{
					this.cookTime = 0;
				}
			}
			// If the current fuel ran out and we still have items to cook, consume the next fuel item
			if (this.burnTimeRemaining == 0 && this.hasFuelAvailable() == true && this.canSmelt() == true)
			{
				this.burnTimeRemaining += consumeFuelItem();
			}
			dirty = true;
		}
		else
		{
			if (this.cookTime != 0)
			{
				dirty = true;
			}

			this.cookTime = 0;
			this.cookTimeFresh = 0;
		}

		// Output to Ender Chest enabled
		if (this.outputMode == 1 && this.itemStacks[2] != null && this.itemStacks[2].stackSize > 0)
		{
			if (++this.timer >= OUTPUT_INTERVAL)
			{
				this.timer = 0;

				if (this.moveItemsToEnderChest() == true)
				{
					dirty = true;
					needsSync = true;
				}
			}
		}

		if (dirty == true)
		{
			this.markDirty();
		}

		// Check if we need to sync some stuff to the clients
		if (needsSync == true || this.isBurningLast != this.isBurning() || this.isCookingLast != this.canSmelt())
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}

		this.isBurningLast = this.isBurning();
		this.isCookingLast = this.canSmelt();
	}

	private boolean fillOutputSlotFromBuffer()
	{
		if (this.outputBufferAmount == 0 || this.outputBufferStack == null)
		{
			return false;
		}

		if (this.itemStacks[2] == null)
		{
			this.itemStacks[2] = this.outputBufferStack.copy();
			this.itemStacks[2].stackSize = 0;
		}

		int size = this.itemStacks[2].stackSize;
		int max = Math.min(this.getInventoryStackLimit(), this.itemStacks[2].getMaxStackSize());

		if (size == max)
		{
			return false;
		}

		max = Math.min(max - size, this.outputBufferAmount);
		this.itemStacks[2].stackSize += max;
		this.outputBufferAmount -= max;
		if (this.outputBufferAmount <= 0)
		{
			this.outputBufferStack = null;
		}

		return true;
	}

	private boolean moveItemsToEnderChest()
	{
		EntityPlayer player = EntityUtils.findPlayerFromUUID(this.ownerUUID);
		boolean movedSomething = false;

		// Player is online
		if (player != null)
		{
			ItemStack enderChestStack;
			InventoryEnderChest invEnderChest = player.getInventoryEnderChest();

			int size = 0;
			for(int i = 0; i < invEnderChest.getSizeInventory(); ++i)
			{
				size = 0;
				enderChestStack = invEnderChest.getStackInSlot(i);

				if (enderChestStack != null)
				{
					size = enderChestStack.stackSize;
				}

				// Check that the target stack either is empty, or has the same item, same damage and same NBT
				if (enderChestStack == null ||
						((invEnderChest.getInventoryStackLimit() - size) > 0 &&
						this.itemStacks[2].isItemEqual(enderChestStack) &&
						ItemStack.areItemStackTagsEqual(this.itemStacks[2], enderChestStack)))
				{
					if (enderChestStack == null)
					{
						enderChestStack = this.itemStacks[2].copy();
					}

					int moved = Math.min(this.itemStacks[2].stackSize, invEnderChest.getInventoryStackLimit() - size);
					enderChestStack.stackSize = size + moved;

					invEnderChest.setInventorySlotContents(i, enderChestStack);
					this.itemStacks[2].stackSize -= moved;

					if (this.itemStacks[2].stackSize <= 0)
					{
						this.itemStacks[2] = null;
						break;
					}

					movedSomething = true;
				}
			}
		}

		return movedSomething;
	}

	public int getOutputBufferAmount()
	{
		return this.outputBufferAmount;
	}

	public ItemStack getOutputBufferStack()
	{
		return this.outputBufferStack;
	}

	public boolean hasFuelAvailable()
	{
		if (this.itemStacks[1] == null)
		{
			return false;
		}

		return (getItemBurnTime(this.itemStacks[1]) > 0 || itemContainsFluidFuel(this.itemStacks[1]));
	}

	public int consumeFuelItem()
	{
		if (this.itemStacks[1] == null)
		{
			return 0;
		}

		int burnTime = getItemBurnTime(this.itemStacks[1]);

		// Regular solid fuels
		if (burnTime > 0)
		{
			if (--this.itemStacks[1].stackSize <= 0)
			{
				this.itemStacks[1] = this.itemStacks[1].getItem().getContainerItem(this.itemStacks[1]);
			}
			this.burnTimeFresh = burnTime;
		}
		// IFluidContainerItem items with lava
		else if (itemContainsFluidFuel(this.itemStacks[1]) == true)
		{
			burnTime = consumeFluidFuelDosage(this.itemStacks[1]);
			this.burnTimeFresh = burnTime;
		}

		return burnTime;
	}

	// Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
	public boolean canSmelt()
	{
		if (this.itemStacks[0] == null)
		{
			return false;
		}
		else
		{
			ItemStack resultStack = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[0]);
			if (resultStack == null)
			{
				return false;
			}
			if (this.itemStacks[2] == null && this.outputBufferAmount == 0)
			{
				return true;
			}
			if (this.itemStacks[2] != null &&
				(this.itemStacks[2].isItemEqual(resultStack) == false || ItemStack.areItemStackTagsEqual(this.itemStacks[2], resultStack) == false))
			{
				return false;
			}
			if (this.outputBufferStack != null &&
				(this.outputBufferStack.isItemEqual(resultStack) == false || ItemStack.areItemStackTagsEqual(this.outputBufferStack, resultStack) == false))
			{
				return false;
			}

			int amount = 0;
			int stackLimit = Math.min(this.getInventoryStackLimit(), resultStack.getMaxStackSize());
			if (this.itemStacks[2] != null)
			{
				amount = this.itemStacks[2].stackSize;
			}
			amount = amount + this.outputBufferAmount + resultStack.stackSize;

			return amount <= (OUTPUT_BUFFER_SIZE + stackLimit);
		}
	}

	// Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
	public void smeltItem()
	{
		if (this.canSmelt() == true)
		{
			ItemStack resultStack = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[0]);
			int stackLimit = Math.min(this.getInventoryStackLimit(), resultStack.getMaxStackSize());

			if (this.itemStacks[2] == null)
			{
				this.itemStacks[2] = resultStack.copy();
				this.itemStacks[2].stackSize = 0;
			}

			int resultAmount = resultStack.stackSize;

			if ((this.itemStacks[2].stackSize + resultAmount) <= stackLimit)
			{
				this.itemStacks[2].stackSize += resultAmount;
			}
			else
			{
				int max = stackLimit - this.itemStacks[2].stackSize;
				this.itemStacks[2].stackSize += max;
				this.outputBufferAmount += (resultAmount - max);

				if (this.outputBufferStack == null)
				{
					this.outputBufferStack = resultStack.copy();
					this.outputBufferStack.stackSize = 1;
				}
			}

			if (--this.itemStacks[0].stackSize <= 0)
			{
				this.itemStacks[0] = null;
			}
		}
	}

	// Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't fuel
	public static int getItemBurnTime(ItemStack stack)
	{
		if (stack == null)
		{
			return 0;
		}

		Item item = stack.getItem();

		if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air)
		{
			Block block = Block.getBlockFromItem(item);
			if (block.getMaterial() == Material.wood) { return COOKTIME_DEFAULT * 225 / 100; }
			if (block == Blocks.coal_block) { return COOKTIME_DEFAULT * 120; }
			if (block == Blocks.wooden_slab) { return COOKTIME_DEFAULT * 45 / 40; }
		}
		else
		{
			if (item == Items.coal) return COOKTIME_DEFAULT * 12;
			if (item == Items.blaze_rod) return COOKTIME_DEFAULT * 18;

			// Ender Furnace custom fuels
			if (item == Items.blaze_powder) return COOKTIME_DEFAULT * 9;
			if (item == Items.ender_pearl) { return COOKTIME_DEFAULT * 8; }
			if (item == Items.ender_eye) { return COOKTIME_DEFAULT * 17; }

			if (item == Items.lava_bucket) return COOKTIME_DEFAULT * 150;
			if (item == Items.stick) return COOKTIME_DEFAULT * 3 / 4;
			if (item == Item.getItemFromBlock(Blocks.sapling)) return COOKTIME_DEFAULT * 3 / 4;
			if (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT * 15 / 10;
			if (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT * 15 / 10;
			if (item instanceof ItemHoe && ((ItemHoe)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT * 15 / 10;

		}

		return GameRegistry.getFuelValue(stack) * COOKTIME_DEFAULT * 3 / 400;
	}

	public static int consumeFluidFuelDosage(ItemStack stack)
	{
		if (itemContainsFluidFuel(stack) == false)
		{
			return 0;
		}

		int burnTime = 0;

		// All the null checks happened already in itemContainsFluidFuel()
		FluidStack fluidStack = ((IFluidContainerItem)stack.getItem()).getFluid(stack);

		if (fluidStack.getFluid().getName().equals("lava") == true)
		{
			// Consume max 250 mB per use.
			int amount = Math.min(250, fluidStack.amount);
			// 1.5 times vanilla lava fuel value (150 items per bucket => 37.5 items per 250 mB)
			burnTime = amount * 15 * COOKTIME_DEFAULT / 100;
			((IFluidContainerItem)stack.getItem()).drain(stack, amount, true);
		}

		return burnTime;
	}

	/* Check if the given item works as a fuel source in this furnace */
	public static boolean isItemFuel(ItemStack stack)
	{
		return itemContainsFluidFuel(stack) || getItemBurnTime(stack) > 0;
	}

	public static boolean itemContainsFluidFuel(ItemStack stack)
	{
		if (stack == null || stack.getItem() == null || stack.getItem() instanceof IFluidContainerItem == false)
		{
			return false;
		}

		FluidStack fluidStack = ((IFluidContainerItem)stack.getItem()).getFluid(stack);
		if (fluidStack == null || fluidStack.amount <= 0)
		{
			return false;
		}

		Fluid fluid = fluidStack.getFluid();
		if (fluid == null)
		{
			return false;
		}

		if (fluid.getName().equals("lava") == true)
		{
			return true;
		}

		return false;
	}

	/* Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. */
	@Override
	public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
	{
		// Don't allow inserting anything to the output slot
		if (slotNum == 2)
		{
			return false;
		}

		// Only accept fuels into the fuel slot
		if (slotNum == 1)
		{
			return isItemFuel(itemStack);
		}

		return FurnaceRecipes.smelting().getSmeltingResult(itemStack) != null;
	}

	/* Returns an array containing the indices of the slots that can be accessed by automation on the given side of this block. */
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		// v0.3.4+: Allow access to all slots from all sides
		return SLOTS_SIDES;
	}

	/* Returns true if automation can insert the given item in the given slot from the given side. Args: slot, itemstack, side */
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return this.isItemValidForSlot(slot, stack);
	}

	// Returns true if automation can extract the given item in the given slot from the given side. Args: slot, itemstack, side
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		// Only allow pulling out items that are not fuel from the fuel slot (like empty buckets)
		if (slot == 1)
		{
			return isItemFuel(stack) == false;
		}

		// Allow pulling out output items from any side, but only when not outputting to Ender Chest
		if (slot == 2 && (this.outputMode & 0x01) == 0)
		{
			return true;
		}

		// Don't allow pulling out items from the input slot, or from the output slot when outputting to an Ender Chest
		return false;
	}

	@Override
	public ContainerEnderFurnace getContainer(InventoryPlayer inventory)
	{
		return new ContainerEnderFurnace(this, inventory);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiEnderFurnace(getContainer(inventoryPlayer), this);
	}

	@Override
	public void performGuiAction(int element, short action)
	{
		// 0: Operating mode (slow/eco vs. fast)
		if (element == 0)
		{
			if (++this.operatingMode > 1)
			{
				this.operatingMode = 0;
			}
			this.markDirty();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		// 1: Output mode (output to Ender Chest OFF/ON)
		else if (element == 1)
		{
			if (++this.outputMode > 1)
			{
				this.outputMode = 0;
			}
			this.markDirty();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}
}
