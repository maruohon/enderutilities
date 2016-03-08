package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import fi.dy.masa.enderutilities.gui.client.GuiEnderInfuser;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ContainerEnderInfuser;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityEnderInfuser extends TileEntityEnderUtilitiesInventory implements ITickable
{
    private static final int SLOT_MATERIAL = 0;
    private static final int SLOT_CAP_IN   = 1;
    private static final int SLOT_CAP_OUT  = 2;
    public static final int AMOUNT_PER_ENDERPEARL = 250;
    public static final int AMOUNT_PER_ENDEREYE = 500;
    public static final int ENDER_CHARGE_PER_MILLIBUCKET = 4;
    public static final int MAX_AMOUNT = 4000;
    public int amountStored;
    public int meltingProgress; // 0..100, 100 being 100% done; input item consumed and stored amount increased @ 100
    public boolean isCharging;
    public int chargeableItemCapacity;
    public int chargeableItemStartingCharge;
    public int chargeableItemCurrentCharge;

    public TileEntityEnderInfuser()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);
        this.itemHandler = new ItemStackHandlerTileEntity(3, this);
        this.itemHandlerExternal = new ItemHandlerWrapperEnderInfuser(this.itemHandler);
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        // The stored amount is stored in a Fluid-compatible tag already,
        // just in case I ever decide to change the "Ender Goo" (or Resonant Ender?)
        // to actually be a Fluid, possibly compatible with Resonant Ender.
        if (nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == true)
        {
            this.amountStored = nbt.getCompoundTag("Fluid").getInteger("Amount");
        }
        this.meltingProgress = nbt.getByte("Progress");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Amount", this.amountStored);
        tag.setString("FluidName", "ender"); // For future compatibility
        nbt.setTag("Fluid", tag);
        nbt.setByte("Progress", (byte)this.meltingProgress);
    }

    @Override
    public void update()
    {
        if (this.worldObj.isRemote == true)
        {
            return;
        }

        boolean dirty = false;

        // Melt Ender Pearls or Eyes of Ender into... emm... Ender Goo?
        if (this.itemHandler.getStackInSlot(SLOT_MATERIAL) != null)
        {
            Item item = this.itemHandler.getStackInSlot(0).getItem();
            int amount = 0;

            if (item == Items.ender_pearl)
            {
                amount = AMOUNT_PER_ENDERPEARL;
            }
            else if (item == Items.ender_eye)
            {
                amount = AMOUNT_PER_ENDEREYE;
            }

            if (amount > 0 && (amount + this.amountStored <= MAX_AMOUNT))
            {
                this.meltingProgress += 2;

                if (this.meltingProgress >= 100)
                {
                    this.amountStored += amount;
                    this.meltingProgress = 0;
                    this.itemHandler.extractItem(SLOT_MATERIAL, 1, false);
                }

                dirty = true;
            }
        }
        else
        {
            this.meltingProgress = 0;
        }

        // NOTE: This does break the IItemHandler contract of not modifying the items
        // you get from getStackInSlot(), but since this is internal usage, whatever...
        // Otherwise we would be constantly extracting and inserting it back.
        ItemStack inputStack = this.itemHandler.getStackInSlot(SLOT_CAP_IN);

        // Charge IChargeable items with the Ender Goo
        if (inputStack != null)
        {
            ItemStack chargeableStack = inputStack;
            Item item = inputStack.getItem();

            if (item instanceof IChargeable || item instanceof IModular)
            {
                boolean isModular = false;
                IChargeable iChargeable = null;

                if (item instanceof IChargeable)
                {
                    iChargeable = (IChargeable) item;
                }
                else // if (item instanceof IModular)
                {
                    chargeableStack = UtilItemModular.getSelectedModuleStack(inputStack, ModuleType.TYPE_ENDERCAPACITOR);

                    if (chargeableStack != null && chargeableStack.getItem() instanceof IChargeable)
                    {
                        iChargeable = (IChargeable) chargeableStack.getItem();
                        isModular = true;
                    }
                }

                if (iChargeable != null && this.amountStored > 0)
                {
                    int charge = (this.amountStored >= 10 ? 10 : this.amountStored) * ENDER_CHARGE_PER_MILLIBUCKET;
                    int filled = iChargeable.addCharge(chargeableStack, charge, false);

                    if (filled > 0)
                    {
                        // Just started charging an item, grab the current charge level and capacity for progress bar updating
                        if (this.isCharging == false)
                        {
                            this.chargeableItemCapacity = iChargeable.getCapacity(chargeableStack);
                            this.chargeableItemStartingCharge = iChargeable.getCharge(chargeableStack);
                            this.chargeableItemCurrentCharge = this.chargeableItemStartingCharge;
                            this.isCharging = true;
                        }

                        if (filled < charge)
                        {
                            charge = filled;
                        }

                        charge = iChargeable.addCharge(chargeableStack, charge, true);
                        int used = (int)Math.ceil(charge / ENDER_CHARGE_PER_MILLIBUCKET);
                        this.amountStored -= used;
                        this.chargeableItemCurrentCharge += charge; // = item.getCharge(capacitorStack);
                        dirty = true;

                        if (isModular == true)
                        {
                            UtilItemModular.setSelectedModuleStack(inputStack, ModuleType.TYPE_ENDERCAPACITOR, chargeableStack);
                        }

                        // Put the item back into the slot
                        //this.itemHandler.insertItem(SLOT_CAP_IN, inputStack, false);
                        //this.itemHandler.setStackInSlot(SLOT_CAP_IN, inputStack);
                    }
                }

                // A fully charged item is in the input slot, move it to the output slot, if possible
                if (iChargeable != null && iChargeable.getCharge(chargeableStack) >= iChargeable.getCapacity(chargeableStack))
                {
                    this.isCharging = false;
                    this.chargeableItemCurrentCharge = 0;
                    this.chargeableItemStartingCharge = 0;
                    this.chargeableItemCapacity = 0;

                    // Move the item from the input slot to the output slot
                    if (this.itemHandler.insertItem(SLOT_CAP_OUT, this.itemHandler.extractItem(SLOT_CAP_IN, 1, true), true) == null)
                    {
                        this.itemHandler.insertItem(SLOT_CAP_OUT, this.itemHandler.extractItem(SLOT_CAP_IN, 1, false), false);
                        dirty = true;
                    }
                }
            }
        }
        else
        {
            this.isCharging = false;
            this.chargeableItemCurrentCharge = 0;
            this.chargeableItemStartingCharge = 0;
            this.chargeableItemCapacity = 0;
        }

        if (dirty == true)
        {
            this.markDirty();
        }
    }

    private class ItemHandlerWrapperEnderInfuser implements IItemHandlerModifiable
    {
        private final IItemHandlerModifiable baseHandler;

        public ItemHandlerWrapperEnderInfuser(IItemHandlerModifiable baseHandler)
        {
            this.baseHandler = baseHandler;
        }

        @Override
        public int getSlots()
        {
            return this.baseHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot)
        {
            return this.baseHandler.getStackInSlot(slot);
        }

        private boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return true;
            }

            // Only accept chargeable items to the item input slot
            if (slot == SLOT_CAP_IN)
            {
                Item item = stack.getItem();
                return item instanceof IChargeable || (item instanceof IModular && ((IModular)item).getInstalledModuleCount(stack, ModuleType.TYPE_ENDERCAPACITOR) > 0);
            }

            // Only allow Ender Pearls and Eyes of Ender to the material slot
            return slot == SLOT_MATERIAL && (stack.getItem() == Items.ender_pearl || stack.getItem() == Items.ender_eye) == true;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack)
        {
            if (this.isItemValidForSlot(slot, stack) == true)
            {
                this.baseHandler.setStackInSlot(slot, stack);
            }
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
        {
            if (this.isItemValidForSlot(slot, stack) == true)
            {
                return this.baseHandler.insertItem(slot, stack, simulate);
            }

            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (slot == SLOT_CAP_OUT)
            {
                return this.baseHandler.extractItem(slot, amount, simulate);
            }

            return null;
        }
    }

    @Override
    public ContainerEnderInfuser getContainer(EntityPlayer player)
    {
        return new ContainerEnderInfuser(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiEnderInfuser(this.getContainer(player), this);
    }
}
