package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;

import fi.dy.masa.enderutilities.client.renderer.item.RenderItemLargeStacks;
import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;

public class GuiContainerLargeStacks extends GuiEnderUtilities
{
    private static RenderItemLargeStacks renderItemLargeStacks;
    protected final List<IInventory> scaledStackSizeTextTargetInventories;

    public GuiContainerLargeStacks(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container, xSize, ySize, textureName);

        this.scaledStackSizeTextTargetInventories = new ArrayList<IInventory>();
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.initCustomRenderItem();
    }

    protected void initCustomRenderItem()
    {
        renderItemLargeStacks.setContainer(this.inventorySlots);
        renderItemLargeStacks.setScaledTextInventories(this.scaledStackSizeTextTargetInventories);
        this.itemRender = renderItemLargeStacks;
    }

    public static void setRenderItem(RenderItemLargeStacks renderItem)
    {
        renderItemLargeStacks = renderItem;
    }

    public static RenderItemLargeStacks getRenderItemLargeStacks()
    {
        return renderItemLargeStacks;
    }
}
