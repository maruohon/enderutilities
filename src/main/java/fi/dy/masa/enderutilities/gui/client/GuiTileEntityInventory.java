package fi.dy.masa.enderutilities.gui.client;

import fi.dy.masa.enderutilities.inventory.ContainerTileEntityInventory;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class GuiTileEntityInventory extends GuiEnderUtilities
{
    protected TileEntityEnderUtilitiesInventory te;

    public GuiTileEntityInventory(ContainerTileEntityInventory container, int xSize, int ySize, String textureName, TileEntityEnderUtilitiesInventory te)
    {
        super(container, xSize, ySize, textureName);
        this.te = te;
    }
}
