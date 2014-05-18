package fi.dy.masa.minecraft.mods.enderutilities.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RenderEnderBow implements IItemRenderer
{
	Minecraft mc = Minecraft.getMinecraft();

	/**
	 * Checks if this renderer should handle a specific item's render type
	 * @param item The item we are trying to render
	 * @param type A render type to check if this renderer handles
	 * @return true if this renderer should handle the given render type,
	 * otherwise false
	 */
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
	}

	/**
	 * Checks if certain helper functionality should be executed for this renderer.
	 * See ItemRendererHelper for more info
	 * 
	 * @param type The render type
	 * @param item The ItemStack being rendered
	 * @param helper The type of helper functionality to be ran
	 * @return True to run the helper functionality, false to not.
	 */
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	/**
	 * Called to do the actual rendering, see ItemRenderType for details on when specific 
	 * types are run, and what extra data is passed into the data parameter.
	 * 
	 * @param type The render type
	 * @param item The ItemStack being rendered
	 * @param data Extra Type specific data
	 */
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		EntityLivingBase living = (EntityLivingBase) data[1];
		ItemRenderer renderer = RenderManager.instance.itemRenderer;
		for (int i = 0; i < item.getItem().getRenderPasses(item.getItemDamage()) + 1; i++)
		{
			this.renderItem(living, item, i, type);
		}
	}

	public void renderItem(EntityLivingBase living, ItemStack stack, int renderPass, ItemRenderType type)
	{
		IIcon iicon = null;

		if (living instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)living;
			if (player.getItemInUse() != null)
			{
				iicon = stack.getItem().getIcon(stack, renderPass, player, player.getItemInUse(), player.getItemInUseCount());
			}
			else
			{
				iicon = living.getItemIcon(stack, renderPass);
			}
		}
		else
		{
			iicon = living.getItemIcon(stack, renderPass);
		}

		if (iicon == null)
		{
			return;
		}

		GL11.glPushMatrix();
		TextureManager textureManager = this.mc.getTextureManager();
		textureManager.bindTexture(textureManager.getResourceLocation(stack.getItemSpriteNumber()));
		//ItemRenderer.renderItemIn2D(par0Tessellator, par1, par2, par3, par4, par5, par6, par7);

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
		{
			GL11.glTranslatef(0.6f,  0.5f,  0.5f);
		}
		else
		{
			GL11.glRotatef(180.0f, 0f, 0f, 1.0f);
			GL11.glRotatef(45.0f, 1.0f, 0.0f, 0.75f);
			GL11.glTranslatef(-0.6f, -0.25f, 1.0f);
			GL11.glScalef(1.75f, 1.75f, 1.75f);
		}

		Tessellator tessellator = Tessellator.instance;
		float f = iicon.getMinU();
		float f1 = iicon.getMaxU();
		float f2 = iicon.getMinV();
		float f3 = iicon.getMaxV();
		float f4 = 0.0f;
		float f5 = 0.3f;
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glTranslatef(-f4, -f5, 0.0f);
		float f6 = 1.5f;
		GL11.glScalef(f6, f6, f6);
		GL11.glRotatef(50.0f, 0.0f, 1.0f, 0.0f);
		GL11.glRotatef(335.0f, 0.0f, 0.0f, 1.0f);
		GL11.glTranslatef(-0.9375f, -0.0625f, 0.0f);
		ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625f);

		GL11.glPopMatrix();
	}
}
