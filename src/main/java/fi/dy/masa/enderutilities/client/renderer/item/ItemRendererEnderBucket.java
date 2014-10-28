package fi.dy.masa.enderutilities.client.renderer.item;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class ItemRendererEnderBucket implements IItemRenderer
{
	public ItemRendererEnderBucket()
	{
	}

	@Override
	public boolean handleRenderType(ItemStack itemStack, ItemRenderType type)
	{
		if (itemStack != null && itemStack.getItem() instanceof ItemEnderBucket)
		{
			ItemEnderBucket item = (ItemEnderBucket)itemStack.getItem();
			FluidStack fluidStack = item.getFluid(itemStack);
			if (fluidStack != null && fluidStack.amount > 0)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		//return false;
		return helper == ItemRendererHelper.ENTITY_BOBBING
				|| helper == ItemRendererHelper.ENTITY_ROTATION;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data)
	{
		if (itemStack == null)
		{
			return;
		}
		IIcon iicon = null;
		int amount = 0;

		ItemEnderBucket item = (ItemEnderBucket)itemStack.getItem();
		FluidStack fluidStack = item.getFluid(itemStack);
		if (fluidStack == null || fluidStack.amount == 0 || fluidStack.getFluid() == null)
		{
			return;
		}
		amount = fluidStack.amount;
		iicon = fluidStack.getFluid().getStillIcon();

		GL11.glPushMatrix();

		switch(type)
		{
			case INVENTORY:
				break;
			default:
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);

		// Render the bucket upside down if the fluid is a gas
		if (fluidStack.getFluid().isGaseous() == true)
		{
			switch(type)
			{
				case EQUIPPED_FIRST_PERSON:
					GL11.glTranslatef(0.0f, 0.25f, 0.0F);
					GL11.glRotatef(60.0f, 0.0f, 1.0f, 0.0f);
				case EQUIPPED:
					GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
					GL11.glTranslatef(0.0f, -0.95f, 0.0F);
					break;
				case ENTITY:
					GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
					GL11.glTranslatef(0.0f, -0.5f, 0.0F);
					break;
				case INVENTORY:
					GL11.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
					GL11.glTranslatef(0.0f, -16.0f, 0.0F);
					break;
				default:
			}
		}

		Tessellator t = Tessellator.instance;

		if (iicon != null)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

			// Center part of fluid
			this.renderQuad(type, t, iicon, 0.375f, 0.1875f, 0.25f, 0.625f, -0.000025d, -0.000025d, 0.0001d);
			// Left square
			this.renderQuad(type, t, iicon, 0.25f, 0.25f, 0.125f, 0.125f, -0.000025d, -0.000025d, 0.0001d);
			// Right square
			this.renderQuad(type, t, iicon, 0.625f, 0.25f, 0.125f, 0.125f, -0.000025d, -0.000025d, 0.0001d);
		}

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationItemsTexture);

		GL11.glEnable(GL11.GL_BLEND);

		iicon = ((ItemEnderBucket)itemStack.getItem()).getIconPart(0); // 0: Bucket main part
		this.renderQuad(type, t, iicon, 0.0f, 0.0f, 1.0f, 1.0f, 0.0d, 0.0d, 0.0d);

		iicon = ((ItemEnderBucket)itemStack.getItem()).getIconPart(1); // 1: Bucket window background (empty part of gauge)
		float scale = 1.0f - (((float)amount) / (float)EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));
		this.renderQuad(type, t, iicon, 0.375f, 0.5625f, 0.25f, scale * 0.25f, 0.0d, 0.0d, 0.00005d);

		iicon = ((ItemEnderBucket)itemStack.getItem()).getIconPart(2); // 2: Bucket top part inside
		this.renderQuad(type, t, iicon, 0.25f, 0.1875f, 0.5f, scale * 0.25f, 0.0d, 0.0d, 0.00005d);

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);

		switch(type)
		{
			case INVENTORY:
				break;
			default:
				GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		}

		GL11.glPopMatrix();
	}

	private void renderQuad(ItemRenderType type, Tessellator t, IIcon iicon, float minX, float minY, float relWidth, float relHeight,
			double layerShrinkX, double layerShrinkY, double layerShrinkZ)
	{
		int width = (int)(relWidth * iicon.getIconWidth());
		int height = (int)(relHeight * iicon.getIconHeight());
		float minU = iicon.getMinU();
		float maxU = iicon.getMaxU();
		float minV = iicon.getMinV();
		float maxV = iicon.getMaxV();

		float lenU = maxU - minU;
		float lenV = maxV - minV;
		float startU = minX * lenU + minU;
		float endU = relWidth * lenU + startU;
		float startV = minY * lenV + minV;
		float endV = relHeight * lenV + startV;

		GL11.glPushMatrix();

		switch(type)
		{
			case EQUIPPED_FIRST_PERSON:
				GL11.glTranslatef(0.0f, 0.25f, 0.0F);
				GL11.glRotatef(30.0f, 0.0f, 1.0f, 0.0f);
				GL11.glScalef(0.7f, 0.7f, 0.7f);
				GL11.glTranslatef(0.0f, 0.02f, 0.0F);
			case EQUIPPED:
				GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
				GL11.glTranslatef(-1.0f, -1.0f, 0.0F);
				break;
			case ENTITY:
				GL11.glTranslatef(-0.5f, -0.25f, 0.0f);
				GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
				GL11.glTranslatef(-1.0f, -1.0f, 0.0F);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_LIGHTING);
				break;
			case INVENTORY:
				layerShrinkZ = -layerShrinkZ; // FIXME wtf, why is this needed? what axis or thing is flipped?
				GL11.glScalef(16.0f, 16.0f, 1.0f);
				// For debugging: scale up and reposition for a closer look:
				//GL11.glScalef(4.0f, 4.0f, 1.0f);
				//GL11.glTranslatef(0.0f, -2.0f, 0.0f);
				break;
			default:
		}

		ItemRenderer.renderItemLayerIn2D(t, width, height, 0.0625f,
				minX, minY, minX + relWidth, minY + relHeight,
				startU, startV, endU, endV,
				layerShrinkX, layerShrinkY, layerShrinkZ);

		GL11.glPopMatrix();
	}
}
