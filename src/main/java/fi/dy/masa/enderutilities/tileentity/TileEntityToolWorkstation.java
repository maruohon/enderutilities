package fi.dy.masa.enderutilities.tileentity;

import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesInventory
{
    private ItemHandlerWrapperSelective itemHandlerToolWorkstation;
    public static final int INV_SLOT_TOOL = 0;
    public static final int INV_SLOT_MODULES_START = 1;
    public static final int INV_SLOT_RENAME = 10;

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
        this.itemHandlerBase = new ItemStackHandlerTileEntity(11, this);
        // itemHandlerExternal is left as null, because this block should not expose it's inventory ie. connect to other blocks
        this.itemHandlerToolWorkstation = new ItemHandlerWrapperToolWorkstation(this.getBaseItemHandler());
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return this.itemHandlerToolWorkstation;
    }

    public void renameItem(String name)
    {
        ItemStack stack = this.getBaseItemHandler().getStackInSlot(INV_SLOT_RENAME);

        if (stack != null)
        {
            if (StringUtils.isBlank(name))
            {
                stack.clearCustomName();
            }
            else
            {
                stack.setStackDisplayName(name);
            }
        }
    }

    public String getItemName()
    {
        ItemStack stack = this.getBaseItemHandler().getStackInSlot(INV_SLOT_RENAME);

        if (stack != null)
        {
            return stack.getDisplayName();
        }

        return "";
    }

    private class ItemHandlerWrapperToolWorkstation extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapperToolWorkstation(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack == null)
            {
                return true;
            }

            if (slot == INV_SLOT_TOOL)
            {
                return stack.getItem() instanceof IModular;
            }
            else if (slot == INV_SLOT_RENAME)
            {
                return true;
            }

            return (stack.getItem() instanceof IModule) && (UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false);
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
