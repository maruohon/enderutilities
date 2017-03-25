package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.gui.client.GuiJSU;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerJSU;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelective;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityJSU extends TileEntityEnderUtilitiesInventory
{
    public static final int INV_SIZE = 270;
    public static final int MAX_STACK_SIZE = 256;
    private ItemHandlerWrapperJSU itemHandlerJSU;

    public TileEntityJSU()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_JSU);

        this.initStorage();
    }

    private int getInvSize()
    {
        return INV_SIZE;
    }

    private void initStorage()
    {
        this.itemHandlerBase        = new ItemStackHandlerTileEntity(0, this.getInvSize(), MAX_STACK_SIZE, true, "Items", this);
        this.itemHandlerJSU         = new ItemHandlerWrapperJSU(this.itemHandlerBase);
        this.itemHandlerExternal    = this.itemHandlerJSU;
    }

    private class ItemHandlerWrapperJSU extends ItemHandlerWrapperSelective
    {
        private final ItemStackHandlerTileEntity itemHandlerBase;

        public ItemHandlerWrapperJSU(ItemStackHandlerTileEntity baseHandler)
        {
            super(baseHandler);

            this.itemHandlerBase = baseHandler;
        }

        @Override
        public int getInventoryStackLimit()
        {
            return this.itemHandlerBase.getInventoryStackLimit();
        }

        @Override
        public int getItemStackLimit(ItemStack stack)
        {
            return this.itemHandlerBase.getInventoryStackLimit();
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            return stack != null && stack.getMaxStackSize() == 1;
        }
    }

    @Override
    public ContainerJSU getContainer(EntityPlayer player)
    {
        return new ContainerJSU(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiJSU(this.getContainer(player), this);
    }
}
