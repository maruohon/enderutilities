package fi.dy.masa.enderutilities.gui.client;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
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
