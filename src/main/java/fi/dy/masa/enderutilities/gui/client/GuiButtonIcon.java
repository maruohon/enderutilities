package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiButtonIcon extends GuiButton
{
    ResourceLocation texture;
    private int u;
    private int v;

    public GuiButtonIcon(int id, int x, int y, int w, int h, ResourceLocation texture, int u, int v)
    {
        super(id, x, y, w, h, "");
        this.texture = texture;
        this.u = u;
        this.v = v;
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
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            int state = this.getHoverState(this.hovered);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, this.u + state * this.width, this.v, this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }
}
