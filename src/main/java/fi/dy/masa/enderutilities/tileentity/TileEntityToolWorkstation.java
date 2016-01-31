package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesSided
{
    public static final int SLOT_TOOL = 0;
    public static final int SLOT_MODULES_START = 1;

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION, 10);
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
    public boolean canInsertItem(int slotNum, ItemStack itemStackIn, EnumFacing direction)
    {
        return false;
    }

    @Override
    public ContainerToolWorkstation getContainer(EntityPlayer player)
    {
        return new ContainerToolWorkstation(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiToolWorkstation(getContainer(player), this);
    }
}
