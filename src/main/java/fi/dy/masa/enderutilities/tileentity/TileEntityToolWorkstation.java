package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
        this.itemStacks = new ItemStack[10];
    }

    @Override
    public void setInventorySlotContents(int slotNum, ItemStack itemStack)
    {
        super.setInventorySlotContents(slotNum, itemStack);

        // FIXME Compatibility transfer of old target data from items before the change to modular. Remove at some point.
        if (this.worldObj.isRemote == false && slotNum == SLOT_TOOL && this.itemStacks[SLOT_TOOL] != null)
        {
            NBTHelperTarget.compatibilityTransferTargetData(this.itemStacks[SLOT_TOOL]);
            UtilItemModular.compatibilityAdjustInstalledModulePositions(this.itemStacks[SLOT_TOOL]);
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotNum, ItemStack stack)
    {
        if (stack == null)
        {
            return true;
        }

        if (slotNum == SLOT_TOOL)
        {
            return stack == null || stack.getItem() instanceof IModular;
        }

        return (stack.getItem() instanceof IModule) && (UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false);
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public boolean canInsertItem(int slotNum, ItemStack itemStack, int side)
    {
        return false;
    }

    @Override
    public ContainerToolWorkstation getContainer(InventoryPlayer inventoryPlayer)
    {
        return new ContainerToolWorkstation(inventoryPlayer, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilitiesInventory getGui(InventoryPlayer inventoryPlayer)
    {
        return new GuiToolWorkstation(getContainer(inventoryPlayer), this);
    }
}
