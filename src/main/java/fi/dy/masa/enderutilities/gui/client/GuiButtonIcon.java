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
			this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			int state = this.getHoverState(this.field_146123_n);
/*
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
*/
			this.drawTexturedModalRect(this.xPosition, this.yPosition, this.u + state * this.width, this.v, this.width, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}

	public void setU(int u)
	{
		this.u = u;
	}

	public void setV(int v)
	{
		this.v = v;
	}
}
