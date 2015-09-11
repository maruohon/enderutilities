package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonIcon extends GuiButton
{
    ResourceLocation texture;
    private int u;
    private int v;
    private int hoverOffsetU;
    private int hoverOffsetV;

    public GuiButtonIcon(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture)
    {
        super(id, x, y, w, h, "");
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.hoverOffsetU = this.width;
        this.hoverOffsetV = 0;
    }

    public GuiButtonIcon(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture, int hoverOffsetU, int hoverOffsetV)
    {
        this(id, x, y, w, h, u, v, texture);
        this.hoverOffsetU = hoverOffsetU;
        this.hoverOffsetV = hoverOffsetV;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(this.texture);
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int state = this.getHoverState(this.field_146123_n);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, this.u + state * this.hoverOffsetU, this.v + state * this.hoverOffsetV, this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }
}
