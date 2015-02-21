package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesSided
{
    public static final int SLOT_TOOL = 0;
    public static final int SLOT_MODULES_START = 1;
    public static final int SLOT_MODULE_STORAGE_START = 11;
    public static final int NUM_MODULE_SLOTS = 10;
    public static final int NUM_STORAGE_SLOTS = 9;
    private static final int[] SLOTS = new int[0];

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
        this.itemStacks = new ItemStack[20];
    }

    public void writeModulesToItem()
    {
        if (this.itemStacks[SLOT_TOOL] != null && this.itemStacks[SLOT_TOOL].getItem() instanceof IModular)
        {
            NBTTagList nbtTagList = new NBTTagList();
            int invSlots = this.getSizeInventory();
            // Write all the modules into a TAG_List
            for (int slotNum = SLOT_MODULES_START; slotNum < invSlots && slotNum < (SLOT_MODULES_START + NUM_MODULE_SLOTS); ++slotNum)
            {
                if (this.itemStacks[slotNum] != null)
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setByte("Slot", (byte)slotNum);
                    this.itemStacks[slotNum].writeToNBT(tag);
                    nbtTagList.appendTag(tag);
                }
            }

            // Write the module list to the tool
            NBTTagCompound nbt = this.itemStacks[SLOT_TOOL].getTagCompound();
            if (nbt == null)
            {
                nbt = new NBTTagCompound();
            }

            if (nbtTagList.tagCount() > 0)
            {
                nbt.setTag("Items", nbtTagList);
            }
            else
            {
                nbt.removeTag("Items");

                // Remove all module selection tags when no modules are installed
                for (ModuleType mt : ModuleType.values())
                {
                    nbt.removeTag("Selected_" + mt.getName());
                }
            }

            // Strip empty compound tags
            if (nbt.hasNoTags() == true)
            {
                nbt = null;
            }

            this.itemStacks[SLOT_TOOL].setTagCompound(nbt);

            if (this.worldObj.isRemote == false && NBTHelperTarget.compatibilityTransferTargetData(this.itemStacks[SLOT_TOOL]) == true)
            {
                this.clearModuleSlots();
                this.readModulesFromItem();
            }
        }
    }

    public void clearModuleSlots()
    {
        // Clear all the module slots from the work station
        int invSlots = this.getSizeInventory();
        for (int slotNum = SLOT_MODULES_START; slotNum < invSlots && slotNum < (SLOT_MODULES_START + NUM_MODULE_SLOTS); ++slotNum)
        {
            this.itemStacks[slotNum] = null;
        }
    }

    public void clearModulesFromItem()
    {
        NBTTagCompound nbt = this.itemStacks[SLOT_TOOL].getTagCompound();
        if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
        {
            return;
        }

        nbt.removeTag("Items");
    }

    public void readModulesFromItem()
    {
        if (this.itemStacks[SLOT_TOOL] != null && this.itemStacks[SLOT_TOOL].getItem() instanceof IModular)
        {
            NBTTagCompound nbt = this.itemStacks[SLOT_TOOL].getTagCompound();
            if (nbt == null || nbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
            {
                return;
            }

            NBTTagList nbtTagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            int num = nbtTagList.tagCount();
            // Read all the module ItemStacks from the tool, and write them to the workstation's module ItemStacks
            for (int i = 0; i < num; ++i)
            {
                NBTTagCompound tag = nbtTagList.getCompoundTagAt(i);
                byte slotNum = tag.getByte("Slot");

                if (slotNum >= 0 && slotNum < this.itemStacks.length && slotNum >= SLOT_MODULES_START && slotNum < (SLOT_MODULES_START + NUM_MODULE_SLOTS))
                {
                    this.itemStacks[slotNum] = ItemStack.loadItemStackFromNBT(tag);
                }
            }
        }
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack)
    {
        //if (this.worldObj.isRemote == false) System.out.println("setInventorySlotContents(" + slotNum + ", " + itemStack + ")" + " " + (this.worldObj.isRemote ? "client" : "server"));

        // Changing the item in the tool slot, write the current modules to the tool first
        if (slotNum == SLOT_TOOL && this.itemStacks[SLOT_TOOL] != null)
        {
            this.writeModulesToItem();
            this.clearModuleSlots();
        }

        super.setInventorySlotContents(slotNum, itemStack);

        // After changing the item in the tool slot, read the modules from the new tool
        if (slotNum == SLOT_TOOL && this.itemStacks[SLOT_TOOL] != null)
        {
            // First clear the module slots
            this.clearModuleSlots();
            this.readModulesFromItem();
        }
    }

    /*@Override
    public ItemStack getStackInSlot(int slotNum)
    {
        // Write the current modules to the tool every time the tool slot is accessed and has a tool
        if (this.worldObj.isRemote == false && slotNum == SLOT_TOOL && this.itemStacks[SLOT_TOOL] != null)
        {
            //System.out.println("getStackInSlot(0); not null, server");
            this.writeModulesToItem();
        }

        return super.getStackInSlot(slotNum);
    }*/

    @Override
    public ItemStack decrStackSize(int slotNum, int maxAmount)
    {
        //System.out.println("decrStackSize(int slotNum, int maxAmount): (" + slotNum + ", " + maxAmount + ")");
        if (this.itemStacks[slotNum] != null)
        {
            ItemStack stack;

            if (this.itemStacks[slotNum].stackSize >= maxAmount)
            {
                ItemStack newStack = this.getStackInSlot(slotNum).copy();
                stack = newStack.splitStack(maxAmount);
                this.setInventorySlotContents(slotNum, newStack);

                if (this.itemStacks[slotNum].stackSize <= 0)
                {
                    this.setInventorySlotContents(slotNum, null);
                }
            }
            else
            {
                stack = this.getStackInSlot(slotNum);
                this.setInventorySlotContents(slotNum, null);
            }

            return stack;
        }

        return null;
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (slotNum == SLOT_TOOL)
        {
            return stack != null && stack.getItem() instanceof IModular;
        }
        else if (slotNum >= SLOT_MODULE_STORAGE_START)
        {
            return stack != null && stack.getItem() instanceof IModule && UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false;
        }
        else if (slotNum > SLOT_TOOL && slotNum < SLOT_MODULE_STORAGE_START)
        {
            return false; // TODO This case is still in the specialized SlotUpgradeModule class
        }

        return false;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing face)
    {
        return SLOTS;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStack, EnumFacing face)
    {
        return false;
    }

    @Override
    public boolean canExtractItem(int slotNum, ItemStack itemStack, EnumFacing face)
    {
        return false;
    }

    @Override
    public ContainerToolWorkstation getContainer(InventoryPlayer inventory)
    {
        return new ContainerToolWorkstation(this, inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiToolWorkstation(getContainer(inventoryPlayer), this);
    }
}
