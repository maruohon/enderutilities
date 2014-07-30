package fi.dy.masa.enderutilities.tileentity;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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

public class TileEntityEnderFurnace extends TileEntityEU
{
	// How long items take to cook?
	protected static final short COOK_TIME_REGULAR_FUEL = 60; // 3 seconds per item with regular fuel
	protected static final short COOK_TIME_ENDER_FUEL = 30; // 1,5 seconds per item with 'ender' fuel
	protected static final short COOK_TIME_NO_FUEL = 1200; // 60 seconds per item without fuel
	protected static final int[] SLOTS_TOP = new int[] {0};
	protected static final int[] SLOTS_BOTTOM = new int[] {2, 1};
	protected static final int[] SLOTS_SIDES = new int[] {0, 1};

	@SideOnly(Side.CLIENT)
	public boolean isActive;

	public byte rotation;
	public byte operatingMode;
	public byte outputMode;
	public short furnaceBurnTime;
	public short currentItemBurnTime; // Number of ticks a fresh copy of the currently-burning item would keep the furnace burning for
	// The number of ticks that the current item has been cooking for
	public short furnaceCookTime;
	public String ownerName;
	private UUID ownerUUID;

	public TileEntityEnderFurnace()
	{
		super(ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
		this.itemStacks = new ItemStack[3];
		this.operatingMode = 0;
		this.outputMode = 0;
		this.ownerName = null;
		this.ownerUUID = null;
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

		this.furnaceBurnTime = nbt.getShort("BurnTime");
		this.furnaceCookTime = nbt.getShort("CookTime");
		this.currentItemBurnTime = nbt.getShort("CurrentItemBurnTime");

		this.operatingMode = nbt.getByte("Mode");
		this.outputMode = nbt.getByte("OutputMode");

		if (nbt.hasKey("OwnerName", 8) == true)
		{
			this.ownerName = nbt.getString("OwnerName");
		}

		if (nbt.hasKey("OwnerUUIDMost") == true && nbt.hasKey("OwnerUUIDLeast") == true)
		{
			this.ownerUUID = new UUID(nbt.getLong("OwnerUUIDMost"), nbt.getLong("OwnerUUIDLeast"));
		}

		if (nbt.hasKey("CustomName", 8) == true)
		{
			this.customInventoryName = nbt.getString("CustomName");
		}

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

		nbt.setShort("BurnTime", (short)this.furnaceBurnTime);
		nbt.setShort("CookTime", (short)this.furnaceCookTime);
		nbt.setShort("CurrentItemBurnTime", (short)this.currentItemBurnTime);
		nbt.setByte("Mode", this.operatingMode);
		nbt.setByte("OutputMode", this.outputMode);

		if (this.ownerName != null)
		{
			nbt.setString("OwnerName", this.ownerName);
		}

		if (this.ownerUUID != null)
		{
			nbt.setLong("OwnerUUIDMost", this.ownerUUID.getMostSignificantBits());
			nbt.setLong("OwnerUUIDLeast", this.ownerUUID.getLeastSignificantBits());
		}

		if (this.hasCustomInventoryName())
		{
			nbt.setString("CustomName", this.customInventoryName);
		}

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

			nbt.setByte("r", this.rotation);
			nbt.setByte("m", this.operatingMode);
			nbt.setByte("op", this.outputMode);
			//nbt.setBoolean("a", this.canSmelt());
			nbt.setBoolean("a", this.isBurning());
			if (this.ownerName != null)
			{
				nbt.setString("ow", this.ownerName);
			}

			return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, nbt);
		}

		return null;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		NBTTagCompound nbt = packet.func_148857_g();
		this.rotation = nbt.getByte("r");
		this.operatingMode = nbt.getByte("m");
		this.outputMode = nbt.getByte("op");
		this.ownerName = nbt.getString("ow");
		this.isActive = nbt.getBoolean("a");
	}

	public void setOwner(EntityPlayer player)
	{
		if (player != null)
		{
			this.ownerName = player.getCommandSenderName();
			this.ownerUUID = player.getUniqueID();
		}
		else
		{
			this.ownerName = null;
			this.ownerUUID = null;
		}
	}

	// Returns an integer between 0 and the passed value representing how close the current item is to being completely cooked
	@SideOnly(Side.CLIENT)
	public int getCookProgressScaled(int i)
	{
		// FIXME!!
		return this.furnaceCookTime * i / COOK_TIME_REGULAR_FUEL;
	}

	// Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
	// item, where 0 means that the item is exhausted and the passed value means that the item is fresh
	@SideOnly(Side.CLIENT)
	public int getBurnTimeRemainingScaled(int i)
	{
		if (this.currentItemBurnTime == 0)
		{
			// FIXME!!
			this.currentItemBurnTime = COOK_TIME_REGULAR_FUEL;
		}

		return this.furnaceBurnTime * i / this.currentItemBurnTime;
	}

	// Furnace isBurning
	public boolean isBurning()
	{
		return this.furnaceBurnTime > 0;
	}

	public void updateEntity()
	{
		boolean wasBurning = this.furnaceBurnTime > 0;
		boolean flag = this.furnaceBurnTime > 0;
		boolean flag1 = false;

		if (this.furnaceBurnTime > 0)
		{
			--this.furnaceBurnTime;
		}

		if (!this.worldObj.isRemote)
		{
			if (this.furnaceBurnTime == 0 && this.canSmelt())
			{
				this.currentItemBurnTime = this.furnaceBurnTime = (short)getItemBurnTime(this.itemStacks[1]);

				if (this.furnaceBurnTime > 0)
				{
					flag1 = true;

					if (this.itemStacks[1] != null)
					{
						--this.itemStacks[1].stackSize;

						if (this.itemStacks[1].stackSize == 0)
						{
							this.itemStacks[1] = itemStacks[1].getItem().getContainerItem(itemStacks[1]);
						}
					}
				}
			}

			if (this.isBurning() && this.canSmelt())
			{
				++this.furnaceCookTime;

				if (this.furnaceCookTime >= COOK_TIME_REGULAR_FUEL)
				{
					this.furnaceCookTime = 0;
					this.smeltItem();
					flag1 = true;
				}
			}
			else
			{
				this.furnaceCookTime = 0;
			}

			if (flag != this.furnaceBurnTime > 0)
			{
				flag1 = true;
				// FIXME add the custom stuff
				//BlockFurnace.updateFurnaceBlockState(this.furnaceBurnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}
		}

		if (flag1 || true) // FIXME debug
		{
			this.markDirty();
		}

		// Burning status changed
		if (wasBurning != this.furnaceBurnTime > 0)
		{
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	// Returns true if the furnace can smelt an item, i.e. has a source item, destination stack isn't full, etc.
	private boolean canSmelt()
	{
		// FIXME disabled for release 0.1.2
		return false;
/*
		if (this.itemStacks[0] == null)
		{
			return false;
		}
		else
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[0]);
			if (itemstack == null) return false;
			if (this.itemStacks[2] == null) return true;
			if (!this.itemStacks[2].isItemEqual(itemstack)) return false;
			int result = itemStacks[2].stackSize + itemstack.stackSize;
			return result <= getInventoryStackLimit() && result <= this.itemStacks[2].getMaxStackSize();
		}
*/
	}

	// Turn one item from the furnace source stack into the appropriate smelted item in the furnace result stack
	public void smeltItem()
	{
		if (this.canSmelt())
		{
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[0]);

			if (this.itemStacks[2] == null)
			{
				this.itemStacks[2] = itemstack.copy();
			}
			else if (this.itemStacks[2].getItem() == itemstack.getItem())
			{
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
				if (block == Blocks.wooden_slab) { return 150; }
				if (block.getMaterial() == Material.wood) { return 300; }
				if (block == Blocks.coal_block) { return 16000; }
			}

			if (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) return 200;
			if (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) return 200;
			if (item instanceof ItemHoe && ((ItemHoe)item).getToolMaterialName().equals("WOOD")) return 200;
			if (item == Items.stick) return 100;
			if (item == Items.coal) return 1600;
			if (item == Items.lava_bucket) return 20000;
			if (item == Item.getItemFromBlock(Blocks.sapling)) return 100;
			if (item == Items.blaze_rod) return 2400;

			// Ender Furnace custom fuels
			if (item == Items.ender_pearl) { return 800; }
			if (item == Items.ender_eye) { return 1200; }

			return GameRegistry.getFuelValue(stack);
		}
	}

	/* Check if the given item works as a fuel source in this furnace */
	public boolean isItemFuel(ItemStack stack)
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
			return this.isItemFuel(itemStack);
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
			return this.isItemFuel(stack) == false;
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
