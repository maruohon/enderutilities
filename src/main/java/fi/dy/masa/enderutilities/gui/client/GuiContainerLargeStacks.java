package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;

import fi.dy.masa.enderutilities.client.renderer.item.RenderItemLargeStacks;
import fi.dy.masa.enderutilities.inventory.ContainerEnderUtilities;

public class GuiContainerLargeStacks extends GuiEnderUtilities
{
    protected final RenderItemLargeStacks renderItemLargeStacks;
    protected final List<IInventory> scaledStackSizeTextTargetInventories;

    public GuiContainerLargeStacks(ContainerEnderUtilities container, int xSize, int ySize, String textureName)
    {
        super(container, xSize, ySize, textureName);
        this.scaledStackSizeTextTargetInventories = new ArrayList<IInventory>();
        this.renderItemLargeStacks = new RenderItemLargeStacks(this.itemRender, this.container, this.scaledStackSizeTextTargetInventories);
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.itemRender = this.renderItemLargeStacks;
    }
}
