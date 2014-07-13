package fi.dy.masa.enderutilities.client.renderer.item;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemRendererEnderBucket implements IItemRenderer
{
	public ItemRendererEnderBucket()
	{
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		if (item.getTagCompound() != null)
		{
			NBTTagCompound nbt = item.getTagCompound();
			if (nbt.hasKey("fluid") && nbt.hasKey("amount") && nbt.getShort("amount") >= 1000 && nbt.getString("fluid").length() > 0)
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
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		if (item == null)
		{
			return;
		}
		IIcon iicon = null;
		short amount = 0;

		final NBTTagCompound nbt = item.getTagCompound();
		if (nbt != null)
		{
			final String fluidName = nbt.getString("fluid");
			amount = nbt.getShort("amount");
			if (nbt.hasKey("fluid") && nbt.hasKey("amount") && amount >= 1000 && fluidName.length() > 0)
			{
				Block block = Block.getBlockFromName(fluidName);
				if (block != null)
				{
					// We won't find fluid for the flowing variants...
					if (block == Blocks.flowing_water) { block = Blocks.water; }
					else if (block == Blocks.flowing_lava) { block = Blocks.lava; }

					final Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
					if (fluid != null)
					{
						iicon = fluid.getStillIcon();
					}
				}
			}
		}

		GL11.glPushMatrix();

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);

		Tessellator t = Tessellator.instance;

		if (iicon != null)
		{
			FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

			// Center part of fluid
			this.renderQuad(type, t, iicon, 0.375f, 0.1875f, 0.25f, 0.625f, -0.000025d, -0.000025d, 0.00005d);
			// Left square
			this.renderQuad(type, t, iicon, 0.25f, 0.25f, 0.125f, 0.125f, -0.000025d, -0.000025d, 0.00005d);
			// Right square
			this.renderQuad(type, t, iicon, 0.625f, 0.25f, 0.125f, 0.125f, -0.000025d, -0.000025d, 0.00005d);
		}

		FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationItemsTexture);

		GL11.glEnable(GL11.GL_BLEND);

		iicon = ((ItemEnderBucket)item.getItem()).getIconPart(0); // 0: Bucket main part
		this.renderQuad(type, t, iicon, 0.0f, 0.0f, 1.0f, 1.0f, 0.0d, 0.0d, 0.0d);

		iicon = ((ItemEnderBucket)item.getItem()).getIconPart(1); // 1: Bucket window background (empty part of gauge)
		float scale = 1.0f - (((float)amount) / (float)ReferenceItem.ENDER_BUCKET_MAX_AMOUNT);
		this.renderQuad(type, t, iicon, 0.375f, 0.5625f, 0.25f, scale * 0.25f, 0.0d, 0.0d, 0.000025d);

		iicon = ((ItemEnderBucket)item.getItem()).getIconPart(2); // 2: Bucket top part inside
		this.renderQuad(type, t, iicon, 0.25f, 0.1875f, 0.5f, scale * 0.25f, 0.0d, 0.0d, 0.000025d);

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
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
				GL11.glScalef(0.8f, 0.8f, 0.8f);
			case EQUIPPED:
				GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
				GL11.glTranslatef(-1.0f, -1.0f, 0.0F);
				break;
			case ENTITY:
				GL11.glTranslatef(-0.5f, -0.25f, 0.0f);
				GL11.glRotatef(180.0f, 0.0f, 0.0f, 1.0f);
				GL11.glTranslatef(-1.0f, -1.0f, 0.0F);
				//GL11.glScalef(relWidth, relHeight, 1.0f);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_LIGHTING);
				break;
			case INVENTORY:
				layerShrinkZ = -layerShrinkZ; // FIXME wtf, why is this needed? what axis or thing is flipped?
				GL11.glScalef(16.0f, 16.0f, 1.0f);
				// For debugging: scale up and reposition for a closer look:
				//GL11.glScalef(4.0f, 4.0f, 1.0f);
				//GL11.glTranslatef(-2.0f, -2.0f, 0.0f);
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
