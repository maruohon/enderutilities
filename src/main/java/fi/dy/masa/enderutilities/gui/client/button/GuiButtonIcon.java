package fi.dy.masa.enderutilities.gui.client.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonIcon extends GuiButton
{
    ResourceLocation texture;
    protected int u;
    protected int v;
    protected int hoverOffsetU;
    protected int hoverOffsetV;

    public GuiButtonIcon(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture)
    {
        this(id, x, y, w, h, u, v, texture, w, 0);
    }

    public GuiButtonIcon(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture, int hoverOffsetU, int hoverOffsetV)
    {
        super(id, x, y, w, h, "");
        this.u = u;
        this.v = v;
        this.texture = texture;
        this.hoverOffsetU = hoverOffsetU;
        this.hoverOffsetV = hoverOffsetV;
    }

    protected int getU()
    {
        return this.u;
    }

    protected int getV()
    {
        return this.v;
    }

    protected boolean isEnabled()
    {
        return true;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float p_191745_4_)
    {
        if (this.visible)
        {
            this.enabled = this.isEnabled();
            this.hovered = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height;
            int state = this.getHoverState(this.hovered);

            mc.getTextureManager().bindTexture(this.texture);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x, this.y,
                    this.getU() + state * this.hoverOffsetU,
                    this.getV() + state * this.hoverOffsetV, this.width, this.height);

            this.mouseDragged(mc, mouseX, mouseY);
        }
    }
}
