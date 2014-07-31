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
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderFurnace;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.container.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.reference.tileentity.ReferenceTileEntity;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class TileEntityEnderFurnace extends TileEntityEU
{
	// The values that define how fuels burn and items smelt
	public static final int COOKTIME_INC_NOFUEL = 5; // No fuel mode: 60 seconds per item
	public static final int COOKTIME_INC_SLOW = 100; // Slow/eco mode: 3 seconds per item
	public static final int COOKTIME_INC_FAST = 300; // Fast mode: 1 second per item (3x as fast)
	public static final int COOKTIME_DEFAULT = 6000; // Base cooktime per item: 3 seconds on slow

	public static final int BURNTIME_USAGE_SLOW = 100; // Slow/eco mode base usage
	public static final int BURNTIME_USAGE_FAST = 600; // Fast mode: use fuel 6x faster over time

	public static final int OUTPUT_BUFFER_SIZE = 1000; // How many items can we store in the output buffer?

	public static final int OUTPUT_INTERVAL = 20; // Only try outputting items every 1 seconds, to try to reduce server load

	protected static final int[] SLOTS_TOP = new int[] {0};
	protected static final int[] SLOTS_BOTTOM = new int[] {2, 1};
	protected static final int[] SLOTS_SIDES = new int[] {0, 1};

	@SideOnly(Side.CLIENT)
	public boolean isActive;
	@SideOnly(Side.CLIENT)
	public boolean usingFuel;

	public byte rotation;
	public byte operatingMode;
	public byte outputMode;

	public int burnTimeRemaining;	// Remaining burn time from the currently burning fuel
	public int burnTimeFresh;		// The time the currently burning fuel will burn in total
	public int cookTime;			// The time the currently cooking item has been cooking for
	public int cookTimeFresh;		// The total time the currently cooking item will take to finish

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
		this.rotation			= (byte)(flags & 0x07);
		this.operatingMode		= (byte)((flags & 0x80) >> 7);
		this.outputMode			= (byte)((flags & 0x40) >> 6);
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
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		byte flags = (byte)(this.rotation & 0x07);
		flags |= ((this.operatingMode & 0x01) << 7);
		flags |= ((this.outputMode & 0x01) << 6);
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
	}

	@Override
	public Packet getDescriptionPacket()
	{
		if (this.worldObj != null)
		{
			NBTTagCompound nbt = new NBTTagCompound();

			byte flags = (byte)(this.rotation & 0x07);
			// 0x10: is cooking something, 0x20: is burning fuel, 0x40: fast mode active, 0x80: output to ender chest enabled
			if (canSmelt() == true) { flags |= 0x10; }
			if (isBurning() == true) { flags |= 0x20; }
			if (this.operatingMode == 1) { flags |= 0x40; }
			if (this.outputMode == 1) { flags |= 0x80; }
			nbt.setByte("f", flags);

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
		this.rotation = (byte)(flags & 0x07);
		this.isActive = (flags & 0x10) == 0x10;
		this.usingFuel = (flags & 0x20) == 0x20;
		this.operatingMode = (byte)((flags & 0x40) >> 6);
		this.outputMode = (byte)((flags & 0x80) >> 7);
		if (nbt.hasKey("o") == true)
		{
			this.ownerName = nbt.getString("o");
		}
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
		boolean isBurningLast = this.isBurning();
		boolean dirty = false;
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
		else if (this.canSmelt() == true)
		{
			this.burnTimeRemaining = this.consumeFuelItem();
			dirty = true;
		}

		// Valid items to smelt, room in output
		if (this.canSmelt() == true)
		{
			// Items just added to be smelted, sync the status to clients.
			if (this.cookTimeFresh == 0)
			{
				needsSync = true;
			}

			this.cookTimeFresh = COOKTIME_DEFAULT; // TODO: per-item cook times?
			this.cookTime += cookTimeIncrement;

			// One item done smelting
			if (this.cookTime >= this.cookTimeFresh)
			{
				this.smeltItem();

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

				EntityPlayer player = EntityUtils.findPlayerFromUUID(this.ownerUUID);
				// Player is online
				if (player != null)
				{
					ItemStack stack;
					InventoryEnderChest e = player.getInventoryEnderChest();

					for(int i = 0; i < e.getSizeInventory(); ++i)
					{
						stack = e.getStackInSlot(i);
						int size = 0;
						if (stack != null)
						{
							size = stack.stackSize;
						}

						if (stack == null || (stack.getItem() != null && stack.getItem().equals(this.itemStacks[2].getItem()) == true && e.getInventoryStackLimit() - size > 0))
						{
							int moved = Math.min(this.itemStacks[2].stackSize, e.getInventoryStackLimit() - size);
							stack = this.itemStacks[2].copy();
							stack.stackSize = size + moved;

							e.setInventorySlotContents(i, stack);
							this.itemStacks[2].stackSize -= moved;

							if (this.itemStacks[2].stackSize <= 0)
							{
								this.itemStacks[2] = null;
								break;
							}
							dirty = true;
						}
					}
				}
			}
		}

		if (dirty == true)
		{
			this.markDirty();
		}

		// Check if we need to sync some stuff to the clients
		if (needsSync == true || isBurningLast != this.isBurning())
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	public boolean hasFuelAvailable()
	{
		if (this.itemStacks[1] == null)
		{
			return false;
		}

		return getItemBurnTime(this.itemStacks[1]) > 0;
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
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[0]);
			if (itemstack == null)
			{
				return false;
			}
			if (this.itemStacks[2] == null)
			{
				return true;
			}
			if (this.itemStacks[2].isItemEqual(itemstack) == false)
			{
				return false;
			}
			// FIXME add the output buffer logic
			int result = itemStacks[2].stackSize + itemstack.stackSize;
			return result <= getInventoryStackLimit() && result <= this.itemStacks[2].getMaxStackSize();
		}
	}

	public int consumeFuelItem()
	{
		if (this.itemStacks[1] == null)
		{
			return 0;
		}

		int burnTime = getItemBurnTime(this.itemStacks[1]);
		if (burnTime == 0)
		{
			return 0;
		}

		if (--this.itemStacks[1].stackSize <= 0)
		{
			this.itemStacks[1] = this.itemStacks[1].getItem().getContainerItem(this.itemStacks[1]);
		}
		this.burnTimeRemaining += burnTime;
		this.burnTimeFresh = burnTime;

		return burnTime;
	}

	// Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
	public void smeltItem()
	{
		if (this.canSmelt() == true)
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[0]);

			if (this.itemStacks[2] == null)
			{
				this.itemStacks[2] = itemstack.copy();
			}
			else if (this.itemStacks[2].getItem() == itemstack.getItem())
			{
				// FIXME add output buffer logic
				this.itemStacks[2].stackSize += itemstack.stackSize;
			}

			--this.itemStacks[0].stackSize;

			if (this.itemStacks[0].stackSize <= 0)
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
		else
		{
			Item item = stack.getItem();

			if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.air)
			{
				Block block = Block.getBlockFromItem(item);
				if (block == Blocks.wooden_slab) { return COOKTIME_DEFAULT * 3 / 4; }
				if (block.getMaterial() == Material.wood) { return COOKTIME_DEFAULT * 3 / 2; }
				if (block == Blocks.coal_block) { return COOKTIME_DEFAULT * 80; }
			}

			if (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT;
			if (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT;
			if (item instanceof ItemHoe && ((ItemHoe)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT;
			if (item == Items.stick) return COOKTIME_DEFAULT / 2;
			if (item == Items.coal) return COOKTIME_DEFAULT * 8;
			if (item == Items.lava_bucket) return COOKTIME_DEFAULT * 100;
			if (item == Item.getItemFromBlock(Blocks.sapling)) return COOKTIME_DEFAULT / 2;
			if (item == Items.blaze_rod) return COOKTIME_DEFAULT * 12;

			// Ender Furnace custom fuels
			if (item == Items.ender_pearl) { return COOKTIME_DEFAULT * 4; }
			if (item == Items.ender_eye) { return COOKTIME_DEFAULT * 8; }

			return GameRegistry.getFuelValue(stack) * COOKTIME_DEFAULT / 200;
		}
	}

	/* Check if the given item works as a fuel source in this furnace */
	public static boolean isItemFuel(ItemStack stack)
	{
		return getItemBurnTime(stack) > 0;
	}

	public void openInventory() {}

	public void closeInventory() {}

	/* Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. */
	@Override
	public boolean isItemValidForSlot(int slotNum, ItemStack itemStack)
	{
		// Don't allow inserting to output slot
		if (slotNum == 2)
		{
			return false;
		}
		// Only accept fuels into fuel slot
		if (slotNum == 1)
		{
			return isItemFuel(itemStack);
		}
		return true;
	}

	/* Returns an array containing the indices of the slots that can be accessed by automation on the given side of this block. */
	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if (side == 0)
		{
			return SLOTS_BOTTOM;
		}
		if (side == 1)
		{
			return SLOTS_TOP;
		}

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
		// Only allow pulling out items that are not fuel from the fuel slot
		if (slot == 1)
		{
			return isItemFuel(stack) == false;
		}

		// Allow pulling out output items from any side
		if (slot == 2)
		{
			return true;
		}

		// Don't allow pulling out items from the input slot
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
		// FIXME debug
		//System.out.printf("mode: %d output: %d side: %s\n", this.operatingMode, this.outputMode, this.worldObj.isRemote);
	}
}
