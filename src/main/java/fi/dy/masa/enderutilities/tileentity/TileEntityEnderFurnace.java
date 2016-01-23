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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.EnumSkyBlock;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.gui.client.GuiEnderFurnace;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerEnderFurnace;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class TileEntityEnderFurnace extends TileEntityEnderUtilitiesSided
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
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;

    public boolean fastMode;
    public boolean outputToEnderChest;
    private ItemStack outputBufferStack;
    private ItemStack smeltingResultCache;

    private boolean inputDirty;
    //private boolean fuelDirty;

    public int burnTimeRemaining;   // Remaining burn time from the currently burning fuel
    public int burnTimeFresh;       // The time the currently burning fuel will burn in total
    public int cookTime;            // The time the currently cooking item has been cooking for

    public boolean isBurningLast;
    public boolean isCookingLast;

    private int timer;

    public TileEntityEnderFurnace()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE, 3);
        this.smeltingResultCache = null;
        this.inputDirty = true;
        //this.fuelDirty = true;
        this.fastMode = false;
        this.outputToEnderChest = false;
        this.burnTimeRemaining = 0;
        this.cookTime = 0;
        this.timer = 0;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        byte flags              = nbt.getByte("Flags"); // Flags
        this.fastMode           = (flags & 0x01) == 0x01;
        this.outputToEnderChest = (flags & 0x02) == 0x02;
        this.burnTimeRemaining  = nbt.getInteger("BurnTimeRemaining");
        this.burnTimeFresh      = nbt.getInteger("BurnTimeFresh");
        this.cookTime           = nbt.getInteger("CookTime");

        if (nbt.hasKey("OutputBufferAmount", Constants.NBT.TAG_INT) == true && nbt.hasKey("OutputBufferStack", Constants.NBT.TAG_COMPOUND) == true)
        {
            this.outputBufferStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("OutputBufferStack"));
            this.outputBufferStack.stackSize = nbt.getInteger("OutputBufferAmount");
        }

        this.inputDirty = true;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        byte flags = 0;
        if (this.fastMode == true)
        {
            flags |= 0x01;
        }
        if (this.outputToEnderChest == true)
        {
            flags |= 0x02;
        }

        nbt.setByte("Flags", flags);
        nbt.setShort("BurnTimeRemaining", (short)this.burnTimeRemaining);
        nbt.setShort("BurnTimeFresh", (short)this.burnTimeFresh);
        nbt.setShort("CookTime", (short)this.cookTime);

        if (this.outputBufferStack != null)
        {
            nbt.setTag("OutputBufferStack", this.outputBufferStack.writeToNBT(new NBTTagCompound()));
            nbt.setInteger("OutputBufferAmount", this.outputBufferStack.stackSize);
        }
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        byte flags = (byte)(this.getRotation() & 0x07);
        // 0x10: is cooking something, 0x20: is burning fuel, 0x40: fast mode active
        if (canSmelt() == true) { flags |= 0x10; }
        if (isBurning() == true) { flags |= 0x20; }
        if (this.fastMode == true) { flags |= 0x40; }
        nbt.setByte("f", flags);

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.func_148857_g();
        byte flags = nbt.getByte("f");
        this.setRotation((byte)(flags & 0x07));
        this.isCookingLast = (flags & 0x10) == 0x10;
        this.isBurningLast = (flags & 0x20) == 0x20;
        this.fastMode = (flags & 0x40) == 0x40;

        super.onDataPacket(net, packet);

        this.worldObj.updateLightByType(EnumSkyBlock.Block, this.xCoord, this.yCoord, this.zCoord);
    }

    public int getOutputBufferAmount()
    {
        if (this.outputBufferStack == null)
        {
            return 0;
        }

        return this.outputBufferStack.stackSize;
    }

    public ItemStack getOutputBufferStack()
    {
        return this.outputBufferStack;
    }

    public boolean isBurning()
    {
        // This returns if the furnace is actually burning fuel at the moment
        return this.burnTimeRemaining > 0;
    }

    /**
     * Updates the cached smelting result for the current input item, if the input has changed since last caching the result.
     */
    private void updateSmeltingResult()
    {
        if (this.inputDirty == true)
        {
            if (this.itemStacks[SLOT_INPUT] != null)
            {
                this.smeltingResultCache = FurnaceRecipes.smelting().getSmeltingResult(this.itemStacks[SLOT_INPUT]);
            }
            else
            {
                this.smeltingResultCache = null;
            }

            this.inputDirty = false;
        }
    }

    /**
     * Updates the cached fuel value for the current item in the fuel slot, if it has changed since last caching it.
     */
    /*private void updateFuelCache()
    {
        if (this.fuelDirty == true)
        {
            if (this.itemStacks[SLOT_FUEL] != null)
            {
                this.fuelBurnTimeCache = 0;
            }
            else
            {
                this.fuelBurnTimeCache = 0;
            }

            this.fuelDirty = false;
        }
    }*/

    @Override
    public void updateEntity()
    {
        if (this.worldObj.isRemote == true)
        {
            return;
        }

        this.updateSmeltingResult();

        boolean dirty = false;
        boolean canSmelt = this.canSmelt();
        boolean hasFuel = this.hasFuelAvailable();

        int cookTimeIncrement = COOKTIME_INC_SLOW;
        if (this.burnTimeRemaining == 0 && hasFuel == false)
        {
            cookTimeIncrement = COOKTIME_INC_NOFUEL;
        }
        else if (this.fastMode == true)
        {
            cookTimeIncrement = COOKTIME_INC_FAST;
        }

        // The furnace is currently burning fuel
        if (this.burnTimeRemaining > 0)
        {
            int btUse = (this.fastMode == true ? BURNTIME_USAGE_FAST : BURNTIME_USAGE_SLOW);

            // Not enough fuel burn time remaining for the elapsed tick
            if (btUse > this.burnTimeRemaining)
            {
                if (hasFuel == true && canSmelt == true)
                {
                    this.burnTimeRemaining += consumeFuelItem();
                    hasFuel = this.hasFuelAvailable();
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
        // Furnace wasn't burning, but it now has fuel and smeltable items, start burning/smelting
        else if (canSmelt == true && hasFuel == true)
        {
            this.burnTimeRemaining += this.consumeFuelItem();
            hasFuel = this.hasFuelAvailable();
            dirty = true;
        }

        // Valid items to smelt, room in output
        if (canSmelt == true)
        {
            this.cookTime += cookTimeIncrement;

            // One item done smelting
            if (this.cookTime >= COOKTIME_DEFAULT)
            {
                this.smeltItem();
                canSmelt = this.canSmelt();

                // We can smelt the next item and we "overcooked" the last one, carry over the extra progress
                if (canSmelt == true && this.cookTime > COOKTIME_DEFAULT)
                {
                    this.cookTime -= COOKTIME_DEFAULT;
                }
                else // No more items to smelt or didn't overcook
                {
                    this.cookTime = 0;
                }
            }

            // If the current fuel ran out and we still have items to cook, consume the next fuel item
            if (this.burnTimeRemaining == 0 && hasFuel == true && canSmelt == true)
            {
                this.burnTimeRemaining += consumeFuelItem();
            }

            dirty = true;
        }
        // Can't smelt anything at the moment, rewind the cooking progress at half the speed of normal cooking
        else if (this.cookTime > 0)
        {
            this.cookTime -= Math.min(this.cookTime, COOKTIME_INC_SLOW / 2);
            dirty = true;
        }

        // Output to Ender Chest enabled
        if (this.outputToEnderChest == true && this.itemStacks[SLOT_OUTPUT] != null && ++this.timer >= OUTPUT_INTERVAL)
        {
            if (this.moveItemsToEnderChest() == true)
            {
                dirty = true;
            }

            this.timer = 0;
        }

        if (this.fillOutputSlotFromBuffer() == true)
        {
            dirty = true;
        }

        if (dirty == true)
        {
            this.markDirty();
        }

        // Check if we need to sync some stuff to the clients
        if (this.isBurningLast != this.isBurning() || this.isCookingLast != canSmelt)
        {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }

        this.isBurningLast = this.isBurning();
        this.isCookingLast = canSmelt;
    }

    /**
     * Turn one item from the furnace input slot into a smelted item in the furnace output buffer.
     */
    public void smeltItem()
    {
        if (this.canSmelt() == true)
        {
            if (this.outputBufferStack == null)
            {
                this.outputBufferStack = this.smeltingResultCache.copy();
            }
            else
            {
                this.outputBufferStack.stackSize += this.smeltingResultCache.stackSize;
            }

            if (--this.itemStacks[SLOT_INPUT].stackSize <= 0)
            {
                this.itemStacks[SLOT_INPUT] = null;
                this.inputDirty = true;
            }
        }
    }

    /**
     * Consumes one fuel item or one dose of fluid fuel. Sets the burnTimeFresh field to the amount of burn time gained.
     * @return returns the amount of furnace burn time that was gained from the fuel
     */
    public int consumeFuelItem()
    {
        if (this.itemStacks[SLOT_FUEL] == null)
        {
            return 0;
        }

        int burnTime = consumeFluidFuelDosage(this.itemStacks[SLOT_FUEL]);

        // IFluidContainerItem items with lava
        if (burnTime > 0)
        {
            this.burnTimeFresh = burnTime;
            //this.fuelDirty = true;
        }
        // Regular solid fuels
        else
        {
            burnTime = getItemBurnTime(this.itemStacks[SLOT_FUEL]);

            if (burnTime > 0)
            {
                if (--this.itemStacks[SLOT_FUEL].stackSize <= 0)
                {
                    this.itemStacks[SLOT_FUEL] = this.itemStacks[SLOT_FUEL].getItem().getContainerItem(this.itemStacks[SLOT_FUEL]);
                    //this.fuelDirty = true;
                }

                this.burnTimeFresh = burnTime;
            }
        }

        return burnTime;
    }

    /**
     * Transfers items from the output buffer to the output slot.
     * @return true if some items were moved
     */
    private boolean fillOutputSlotFromBuffer()
    {
        if (this.outputBufferStack == null)
        {
            return false;
        }

        if (this.itemStacks[SLOT_OUTPUT] == null)
        {
            this.itemStacks[SLOT_OUTPUT] = this.outputBufferStack.copy();
            this.itemStacks[SLOT_OUTPUT].stackSize = 0;
        }
        else if (InventoryUtils.areItemStacksEqual(this.itemStacks[SLOT_OUTPUT], this.outputBufferStack) == false)
        {
            return false;
        }

        int size = this.itemStacks[SLOT_OUTPUT].stackSize;
        int max = Math.min(this.getInventoryStackLimit(), this.itemStacks[SLOT_OUTPUT].getMaxStackSize());

        if (size >= max)
        {
            return false;
        }

        int amount = Math.min(max - size, this.outputBufferStack.stackSize);
        this.itemStacks[SLOT_OUTPUT].stackSize += amount;
        this.outputBufferStack.stackSize -= amount;

        if (this.outputBufferStack.stackSize <= 0)
        {
            this.outputBufferStack = null;
        }

        return true;
    }

    /**
     * Moves as many items from the output slot to the owner's vanilla Ender Chest as possible.
     * Tries to first fill matching stacks to their max, then puts items into the first empty slot.
     * @return true if some items were moved
     */
    private boolean moveItemsToEnderChest()
    {
        if (this.itemStacks[SLOT_OUTPUT] == null)
        {
            return false;
        }

        EntityPlayer player = EntityUtils.findPlayerByUUID(this.ownerUUID);
        boolean movedSomething = false;

        // Player is online
        if (player != null)
        {
            InventoryEnderChest invEnderChest = player.getInventoryEnderChest();
            ItemStack enderChestStack;

            int invSize = invEnderChest.getSizeInventory();
            int amount = 0;

            // First loop through the Ender Chest: fill matching stacks
            for (int i = 0; i < invSize; ++i)
            {
                enderChestStack = invEnderChest.getStackInSlot(i);

                if (enderChestStack == null)
                {
                    continue;
                }

                if (InventoryUtils.areItemStacksEqual(this.itemStacks[SLOT_OUTPUT], enderChestStack) == true)
                {
                    amount = Math.min(this.itemStacks[SLOT_OUTPUT].stackSize, invEnderChest.getInventoryStackLimit() - enderChestStack.stackSize);
                    amount = Math.min(amount, this.itemStacks[SLOT_OUTPUT].getMaxStackSize());

                    if (amount > 0)
                    {
                        enderChestStack.stackSize += amount;
                        this.itemStacks[SLOT_OUTPUT].stackSize -= amount;
                        movedSomething = true;

                        if (this.itemStacks[SLOT_OUTPUT].stackSize <= 0)
                        {
                            this.itemStacks[SLOT_OUTPUT] = null;
                            return true;
                        }
                    }
                }
            }

            // Second loop (if we still have more items): move items to an empty slot
            for (int i = 0; i < invSize; ++i)
            {
                enderChestStack = invEnderChest.getStackInSlot(i);

                if (enderChestStack != null)
                {
                    continue;
                }

                amount = Math.min(this.itemStacks[SLOT_OUTPUT].stackSize, invEnderChest.getInventoryStackLimit());
                amount = Math.min(amount, this.itemStacks[SLOT_OUTPUT].getMaxStackSize());

                if (amount > 0)
                {
                    ItemStack newStack = this.itemStacks[SLOT_OUTPUT].copy();
                    newStack.stackSize = amount;
                    this.itemStacks[SLOT_OUTPUT].stackSize -= amount;
                    invEnderChest.setInventorySlotContents(i, newStack);
                    movedSomething = true;

                    if (this.itemStacks[SLOT_OUTPUT].stackSize <= 0)
                    {
                        this.itemStacks[SLOT_OUTPUT] = null;
                        return true;
                    }
                }
            }
        }

        return movedSomething;
    }

    /**
     * Checks if there is a valid fuel item in the fuel slot.
     * @return true if the fuel slot has an item that can be used as fuel
     */
    public boolean hasFuelAvailable()
    {
        if (this.itemStacks[SLOT_FUEL] == null)
        {
            return false;
        }

        return (itemContainsFluidFuel(this.itemStacks[SLOT_FUEL]) == true || getItemBurnTime(this.itemStacks[SLOT_FUEL]) > 0);
    }

    /**
     * Returns true if the furnace can smelt an item. Checks the input slot for valid smeltable items and the output buffer
     * for an equal item and free space or empty buffer. Does not check the fuel.
     * @return true if input and output item stacks allow the current item to be smelted
     */
    public boolean canSmelt()
    {
        if (this.itemStacks[SLOT_INPUT] == null || this.smeltingResultCache == null)
        {
            return false;
        }

        int amount = 0;
        if (this.outputBufferStack != null)
        {
            if (InventoryUtils.areItemStacksEqual(this.smeltingResultCache, this.outputBufferStack) == false)
            {
                return false;
            }

            amount = this.outputBufferStack.stackSize;
        }

        if ((OUTPUT_BUFFER_SIZE - amount) < this.smeltingResultCache.stackSize)
        {
            return false;
        }

        return true;
    }

    /**
     * Returns the number of ticks that the supplied fuel item will keep the furnace burning, or 0 if the item isn't fuel
     * @param stack
     * @return
     */
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
            if (block == Blocks.sapling) return COOKTIME_DEFAULT * 3 / 4;
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
            if (item instanceof ItemTool && ((ItemTool)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT * 15 / 10;
            if (item instanceof ItemSword && ((ItemSword)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT * 15 / 10;
            if (item instanceof ItemHoe && ((ItemHoe)item).getToolMaterialName().equals("WOOD")) return COOKTIME_DEFAULT * 15 / 10;

        }

        return GameRegistry.getFuelValue(stack) * COOKTIME_DEFAULT * 3 / 400;
    }

    /**
     * Uses one dose (<= 250 mB) of fluid fuel, returns the amount of cook time that was gained from it.
     * @param stack
     * @return
     */
    public static int consumeFluidFuelDosage(ItemStack stack)
    {
        if (itemContainsFluidFuel(stack) == false)
        {
            return 0;
        }

        // All the null checks happened already in itemContainsFluidFuel()
        FluidStack fluidStack = ((IFluidContainerItem)stack.getItem()).getFluid(stack);

        // Consume max 250 mB per use.
        int amount = Math.min(250, fluidStack.amount);
        ((IFluidContainerItem)stack.getItem()).drain(stack, amount, true);

        // 1.5 times vanilla lava fuel value (150 items per bucket => 37.5 items per 250 mB)
        return (amount * 15 * COOKTIME_DEFAULT / 100);
    }

    /**
     * Check if the given item works as a fuel source in this furnace
     * @param stack
     * @return
     */
    public static boolean isItemFuel(ItemStack stack)
    {
        return itemContainsFluidFuel(stack) || getItemBurnTime(stack) > 0;
    }

    /**
     * Checks if the given ItemStack contains a valid fluid fuel source for the furnace.
     * Valid fuels are currently just lava.
     * @param stack
     * @return
     */
    public static boolean itemContainsFluidFuel(ItemStack stack)
    {
        if (stack == null || (stack.getItem() instanceof IFluidContainerItem) == false)
        {
            return false;
        }

        FluidStack fluidStack = ((IFluidContainerItem)stack.getItem()).getFluid(stack);
        if (fluidStack == null || fluidStack.amount <= 0)
        {
            return false;
        }

        Fluid fluid = fluidStack.getFluid();
        if (fluid != null && fluid.getName() != null && fluid.getName().equals("lava") == true)
        {
            return true;
        }

        return false;
    }

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //System.out.println("decrStackSize(slot: " + slotNum + " maxAmount: " + maxAmount + ")");
        if (slotNum == SLOT_INPUT)
        {
            this.inputDirty = true;
        }
        /*else if (slotNum == SLOT_FUEL)
        {
            this.fuelDirty = true;
        }*/

        return super.decrStackSize(slotNum, maxAmount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotNum)
    {
        //System.out.println("getStackInSlotOnClosing(slot: " + slotNum + ")");
        if (slotNum == SLOT_INPUT)
        {
            this.inputDirty = true;
        }
        /*else if (slotNum == SLOT_FUEL)
        {
            this.fuelDirty = true;
        }*/

        return super.getStackInSlotOnClosing(slotNum);
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack stack)
    {
        //System.out.println("setInventorySlotContents(slot: " + slotNum + ", " + stack + ")");
        if (slotNum == SLOT_INPUT && InventoryUtils.areItemStacksEqual(stack, this.itemStacks[slotNum]) == false)
        {
            this.inputDirty = true;
        }
        /*else if (slotNum == SLOT_FUEL && InventoryUtils.areItemStacksEqual(stack, this.itemStacks[slotNum]) == false)
        {
            this.fuelDirty = true;
        }*/

        super.setInventorySlotContents(slotNum, stack);
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        //System.out.println("isItemvalidForSlot(slot: " + slotNum + ", " + stack + ")");
        // Don't allow inserting anything to the output slot
        if (slotNum == 2)
        {
            return false;
        }

        // Only accept fuels into the fuel slot
        if (slotNum == 1)
        {
            return InventoryUtils.areItemStacksEqual(stack, this.itemStacks[slotNum]) == true || isItemFuel(stack) == true;
        }

        return InventoryUtils.areItemStacksEqual(stack, this.itemStacks[slotNum]) == true || FurnaceRecipes.smelting().getSmeltingResult(stack) != null;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        //System.out.println("getAccessibleSlotsFromSide(side: " + side + ")");
        // Allow access to all slots from all sides
        return SLOTS_SIDES;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side)
    {
        //System.out.println("canExtractItem(slot: " + slot + ", " + stack + " side: " + side + ")");
        // Only allow pulling out items that are not fuel from the fuel slot (like empty buckets)
        if (slot == 1)
        {
            return isItemFuel(stack) == false;
        }

        // Allow pulling out output items from any side, but only when not outputting to Ender Chest
        if (slot == 2 && this.outputToEnderChest == false)
        {
            return true;
        }

        // Don't allow pulling out items from the input slot, or from the output slot when outputting to an Ender Chest
        return false;
    }

    @Override
    public ContainerEnderFurnace getContainer(InventoryPlayer inventoryPlayer)
    {
        return new ContainerEnderFurnace(inventoryPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiEnderFurnace(this.getContainer(inventoryPlayer), this);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        // 0: Operating mode (slow/eco vs. fast)
        if (action == 0)
        {
            this.fastMode = ! this.fastMode;
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
        // 1: Output mode (output to Ender Chest OFF/ON)
        else if (action == 1)
        {
            this.outputToEnderChest = ! this.outputToEnderChest;
        }

        this.markDirty();
    }

    /**
     * Returns an integer between 0 and the passed value representing how close the current item is to being completely cooked
     * @param i
     * @return
     */
    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int i)
    {
        return this.cookTime * i / COOKTIME_DEFAULT;
    }

    /**
     * Returns an integer between 0 and the passed value representing how much burn time is left on the current fuel
     * item, where 0 means that the item is exhausted and the passed value means that the item is fresh
     * @param i
     * @return
     */
    @SideOnly(Side.CLIENT)
    public int getBurnTimeRemainingScaled(int i)
    {
        if (this.burnTimeFresh == 0)
        {
            return 0;
        }

        return this.burnTimeRemaining * i / this.burnTimeFresh;
    }
}
