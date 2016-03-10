package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesInventory
{
    private IItemHandlerModifiable itemHandlerTW;
    public static final int SLOT_TOOL = 0;
    public static final int SLOT_MODULES_START = 1;

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
        this.itemHandlerBase = new ItemStackHandlerTileEntity(11, this);
        // itemHandlerExternal is left as null, because this block should not expose it's inventory ie. connect to other blocks
        this.itemHandlerTW = new ItemHandlerWrapperToolWorkstation(this.itemHandlerBase);
    }

    public IItemHandler getInventory()
    {
        return this.itemHandlerTW;
    }

    private class ItemHandlerWrapperToolWorkstation extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperToolWorkstation(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        protected boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return true;
            }

            if (slot == SLOT_TOOL)
            {
                return stack.getItem() instanceof IModular;
            }

            return (stack.getItem() instanceof IModule) && (UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            return null;
        }
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
