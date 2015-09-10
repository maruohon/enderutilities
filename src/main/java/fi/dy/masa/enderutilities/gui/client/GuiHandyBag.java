package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class GuiHandyBag extends InventoryEffectRenderer
{
    private ContainerHandyBag container;
    protected ResourceLocation guiTexture;
    private float mouseXFloat;
    private float mouseYFloat;

    public GuiHandyBag(ContainerHandyBag container)
    {
        super(container);
        this.container = container;
        this.guiTexture = ReferenceTextures.getGuiTexture("gui.container.handybag." + container.getBagTier());
        this.xSize = 176;
        this.ySize = 256;
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        super.initGui();
    }

    @Override
    public void updateScreen()
    {
        super.updateScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float gameTicks)
    {
        super.drawScreen(mouseX, mouseY, gameTicks);
        this.drawTooltips(mouseX, mouseY);
        this.mouseXFloat = (float)mouseX;
        this.mouseYFloat = (float)mouseY;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        int startX = 40;
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, startX, 0, this.xSize, this.ySize);

        // TODO Remove this in 1.8 and enable the slot background icon method override instead
        // In Forge 1.7.10 there is a Forge bug that causes Slot background icons to render
        // incorrectly, if there is an item with the glint effect before the Slot in question in the Container.
        this.bindTexture(TextureMap.locationItemsTexture);
        //GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glEnable(GL11.GL_BLEND);

        // Draw the background icon for empty player armor slots
        IInventory inv = this.container.player.inventory;
        int xOff = this.guiLeft + 8;
        int yOff = this.guiTop + 15;
        IIcon icon;
        for (int i = 0; i < 4; i++)
        {
            if (inv.getStackInSlot(39 - i) == null)
            {
                icon = ItemArmor.func_94602_b(i);
                this.drawTexturedModelRectFromIcon(xOff, yOff + i * 18, icon, 16, 16);
            }
        }

        // Draw the background icon over empty storage module slots
        inv = this.container.inventory;
        xOff = this.guiLeft + this.container.getSlot(inv.getSizeInventory() - 4).xDisplayPosition;
        yOff = this.guiTop + this.container.getSlot(inv.getSizeInventory() - 4).yDisplayPosition;
        icon = EnderUtilitiesItems.enderPart.getGuiSlotBackgroundIconIndex(ModuleType.TYPE_MEMORY_CARD);
        for (int i = 0, slotNum = inv.getSizeInventory() - 4; icon != null && i < 4; i++, slotNum++)
        {
            if (inv.getStackInSlot(slotNum) == null)
            {
                this.drawTexturedModelRectFromIcon(xOff + i * 18, yOff, icon, 16, 16);
            }
        }
        //GL11.glDisable(GL11.GL_BLEND);
        //GL11.glDisable(GL11.GL_LIGHTING);
        // TODO end of to-be-removed code in 1.8

        // Draw the player model
        GuiInventory.func_147046_a(this.guiLeft + 51, this.guiTop + 82, 30, this.guiLeft + 51 - this.mouseXFloat, this.guiTop + 25 - this.mouseYFloat, this.mc.thePlayer);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 97, 5, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag.storagemodules", new Object[0]), 97, 59, 0x404040);
        this.fontRendererObj.drawString(I18n.format("enderutilities.container.handybag", new Object[0]), 8, 90, 0x404040);
    }

    protected void drawTooltips(int mouseX, int mouseY)
    {
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
    }
}
