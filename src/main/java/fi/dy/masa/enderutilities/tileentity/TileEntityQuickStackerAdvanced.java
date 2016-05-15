package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiQuickStackerAdvanced;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStackerAdvanced;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class TileEntityQuickStackerAdvanced extends TileEntityEnderUtilitiesInventory
{
    public static final int NUM_LINK_CRYSTALS = 8;
    public static final int NUM_PRESETS = 4;
    public static final int GUI_ACTION_SELECT_MODULE = 0;
    public static final int GUI_ACTION_CHANGE_PRESET = 1;
    public static final int GUI_ACTION_TOGGLE_SETTINGS_1 = 2;
    public static final int GUI_ACTION_TOGGLE_SETTINGS_2 = 3;
    public static final int GUI_ACTION_TOGGLE_TARGET     = 4;

    private final IItemHandler inventoryFilters;

    public TileEntityQuickStackerAdvanced()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, NUM_LINK_CRYSTALS, 1, false, "Items", this);
        this.inventoryFilters = new ItemStackHandlerTileEntity(1, 36, 1, false, "FilterItems", this);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return new ItemHandlerWrapperContainer(this.getBaseItemHandler(), new ItemHandlerWrapperQuickStackerAdvanced(this.getBaseItemHandler()));
    }

    public IItemHandler getFilterInventory()
    {
        return this.inventoryFilters;
    }

    private class ItemHandlerWrapperQuickStackerAdvanced extends ItemHandlerWrapperSelective
    {
        public ItemHandlerWrapperQuickStackerAdvanced(IItemHandler baseHandler)
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

            // Only allow Ender Pearls and Eyes of Ender to the material slot
            return stack.getItem() == EnderUtilitiesItems.linkCrystal && ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_BLOCK;
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
    }

    @Override
    public ContainerQuickStackerAdvanced getContainer(EntityPlayer player)
    {
        return new ContainerQuickStackerAdvanced(player, this);
    }

    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiQuickStackerAdvanced(this.getContainer(player), this);
    }
}
