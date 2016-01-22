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
import fi.dy.masa.enderutilities.gui.client.GuiTileEntityInventory;
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
        super(ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER, 3);
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

        // Melt Ender Pearls or Eyes of Ender into... emm... Ender Goo?
        if (this.itemStacks[0] != null)
        {
            Item item = this.itemStacks[0].getItem();
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

                    if (--this.itemStacks[0].stackSize <= 0)
                    {
                        this.itemStacks[0] = null;
                    }
                }

                dirty = true;
            }
        }
        else
        {
            this.meltingProgress = 0;
        }

        ItemStack chargeableStack = this.itemStacks[1];
        // Charge IChargeable items with the Ender Goo
        if (chargeableStack != null)
        {
            Item item = chargeableStack.getItem();
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
                    chargeableStack = UtilItemModular.getSelectedModuleStack(chargeableStack, ModuleType.TYPE_ENDERCAPACITOR);
                    if (chargeableStack != null)
                    {
                        item = chargeableStack.getItem();
                        if ((item instanceof IChargeable) == true)
                        {
                            iChargeable = (IChargeable) item;
                            isModular = true;
                        }
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
                            UtilItemModular.setSelectedModuleStack(this.itemStacks[1], ModuleType.TYPE_ENDERCAPACITOR, chargeableStack);
                        }
                    }
                }

                // A fully charged item is in the input slot, move it to the output slot, if possible
                if (iChargeable != null && iChargeable.getCharge(chargeableStack) >= iChargeable.getCapacity(chargeableStack))
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
            return (item instanceof IChargeable || (item instanceof IModular && ((IModular)item).getInstalledModuleCount(stack, ModuleType.TYPE_ENDERCAPACITOR) > 0));
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
    public ContainerEnderInfuser getContainer(InventoryPlayer inventoryPlayer)
    {
        return new ContainerEnderInfuser(inventoryPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiTileEntityInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiEnderInfuser(this.getContainer(inventoryPlayer), this);
    }
}
