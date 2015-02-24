package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderInfuser;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.inventory.ContainerEnderInfuser;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityEnderInfuser extends TileEntityEnderUtilitiesSided
{
    protected static final int[] SLOTS_SIDES = new int[] {0, 1, 2};
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
        this.itemStacks = new ItemStack[3];
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
    public void updateEntity()
    {
        if (this.worldObj.isRemote == true)
        {
            return;
        }

        boolean dirty = false;
        boolean sync = false;

        // Melt Ender Pearls or Eyes of Ender into... emm... Ender Goo?
        if (this.itemStacks[0] != null)
        {
            int amount = 0;
            if (this.itemStacks[0].getItem() == Items.ender_pearl)
            {
                amount = AMOUNT_PER_ENDERPEARL;
            }
            else if (this.itemStacks[0].getItem() == Items.ender_eye)
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

                    if (--this.itemStacks[0].stackSize <= 0)
                    {
                        this.itemStacks[0] = null;
                    }
                }

                dirty = true;
                sync = true;
            }
        }
        else
        {
            this.meltingProgress = 0;
        }

        // Charge IChargeable items with the Ender Goo
        if (this.itemStacks[1] != null && (this.itemStacks[1].getItem() instanceof IChargeable || this.itemStacks[1].getItem() instanceof IModular))
        {
            boolean isModular = false;
            ItemStack capacitorStack = this.itemStacks[1];
            IChargeable item = null;

            if (this.itemStacks[1].getItem() instanceof IChargeable)
            {
                item = (IChargeable) capacitorStack.getItem();
            }
            else if (this.itemStacks[1].getItem() instanceof IModular)
            {
                capacitorStack = UtilItemModular.getSelectedModuleStack(this.itemStacks[1], ModuleType.TYPE_ENDERCAPACITOR);
                if (capacitorStack != null && (capacitorStack.getItem() instanceof IChargeable) == true)
                {
                    item = (IChargeable) capacitorStack.getItem();
                    isModular = true;
                }
            }

            if (item != null && this.amountStored > 0)
            {
                int charge = (this.amountStored >= 10 ? 10 : this.amountStored) * ENDER_CHARGE_PER_MILLIBUCKET;
                int filled = item.addCharge(capacitorStack, charge, false);

                if (filled > 0)
                {
                    // Just started charging an item, grab the current charge level and capacity for progress bar updating
                    if (this.isCharging == false)
                    {
                        this.chargeableItemCapacity = item.getCapacity(capacitorStack);
                        this.chargeableItemStartingCharge = item.getCharge(capacitorStack);
                        this.chargeableItemCurrentCharge = this.chargeableItemStartingCharge;
                        this.isCharging = true;
                    }

                    if (filled < charge)
                    {
                        charge = filled;
                    }

                    charge = item.addCharge(capacitorStack, charge, true);
                    int used = (int)Math.ceil(charge / ENDER_CHARGE_PER_MILLIBUCKET);
                    this.amountStored -= used;
                    this.chargeableItemCurrentCharge += charge; // = item.getCharge(capacitorStack);
                    dirty = true;

                    if (isModular == true)
                    {
                        UtilItemModular.setSelectedModuleStack(this.itemStacks[1], ModuleType.TYPE_ENDERCAPACITOR, capacitorStack);
                    }
                }
            }

            // A fully charged item is in the input slot, move it to the output slot, if possible
            if (item != null && item.getCharge(capacitorStack) >= item.getCapacity(capacitorStack))
            {
                this.isCharging = false;
                this.chargeableItemCurrentCharge = 0;
                this.chargeableItemStartingCharge = 0;
                this.chargeableItemCapacity = 0;

                // Output slot is currently empty, move the item
                if (this.itemStacks[2] == null)
                {
                    this.itemStacks[2] = this.itemStacks[1];
                    this.itemStacks[1] = null;
                    dirty = true;
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

        if (sync == true)
        {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return true;
        }

        Item item = stack.getItem();
        // Only allow Ender Pearls and Eyes of Ender to the melting slot
        if (slotNum == 0)
        {
            return (item == Items.ender_pearl || item == Items.ender_eye);
        }

        // Only accept chargeable items to the item input slot
        if (slotNum == 1)
        {
            return (item instanceof IChargeable || (item instanceof IModular && ((IModular)item).getModuleCount(stack, ModuleType.TYPE_ENDERCAPACITOR) > 0));
        }

        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side)
    {
        // Allow access to all slots from all sides
        return SLOTS_SIDES;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side)
    {
        // Only allow pulling out items from the output slot
        return slot == 2;
    }

    @Override
    public ContainerEnderInfuser getContainer(InventoryPlayer inventory)
    {
        return new ContainerEnderInfuser(this, inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiEnderInfuser(this.getContainer(inventoryPlayer), this);
    }
}
