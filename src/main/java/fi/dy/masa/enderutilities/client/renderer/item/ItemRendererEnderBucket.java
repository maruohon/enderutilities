package fi.dy.masa.enderutilities.client.renderer.item;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import fi.dy.masa.enderutilities.item.ItemEnderBucket;

public class ItemRendererEnderBucket implements IItemRenderer
{
    public Minecraft mc;

    public ItemRendererEnderBucket()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type)
    {
        if (stack != null && stack.getItem() instanceof ItemEnderBucket)
        {
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper)
    {
        return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data)
    {
        if (stack == null || stack.getItem() == null || (stack.getItem() instanceof ItemEnderBucket) == false)
        {
            return;
        }

        ItemEnderBucket itemBucket = (ItemEnderBucket)stack.getItem();
        IIcon iicon = null;
        int amount = 0;

        FluidStack fluidStack = itemBucket.getFluidCached(stack);
        Fluid fluid = null;

        if (fluidStack != null)
        {
            amount = fluidStack.amount;
            fluid = fluidStack.getFluid();
            iicon = fluid.getStillIcon();
        }

        GL11.glPushMatrix();

        // Render the bucket upside down if the fluid is a gas
        if (fluid != null && fluid.isGaseous() == true)
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

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);

        switch(type)
        {
            case INVENTORY:
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                break;
            default:
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glDisable(GL11.GL_BLEND);
        }

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        if (iicon != null)
        {
            this.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);

            // Center part of fluid
            this.renderQuad(type, t, iicon, 0.375f, 0.1875f, 0.25f, 0.625f, -0.000025d, -0.000025d, 0.0001d);
            // Left square
            this.renderQuad(type, t, iicon, 0.25f, 0.25f, 0.125f, 0.125f, -0.000025d, -0.000025d, 0.0001d);
            // Right square
            this.renderQuad(type, t, iicon, 0.625f, 0.25f, 0.125f, 0.125f, -0.000025d, -0.000025d, 0.0001d);
        }

        this.mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);

        int offset = 0;
        int mainPartIndex = itemBucket.getBucketMode(stack);
        if (itemBucket.getBucketLinkMode(stack) == ItemEnderBucket.LINK_MODE_ENABLED)
        {
            offset += 6;
        }
        if (mainPartIndex < 0 || mainPartIndex > 3)
        {
            mainPartIndex = 0;
        }

        iicon = itemBucket.getIconPart(offset + mainPartIndex); // 0: Bucket main part
        this.renderQuad(type, t, iicon, 0.0f, 0.0f, 1.0f, 1.0f, 0.0d, 0.0d, 0.0d);

        iicon = itemBucket.getIconPart(offset + 5); // 1: Bucket window background (empty part of gauge)
        int capacity = itemBucket.getCapacityCached(stack, null);
        if (capacity == 0)
        {
            capacity = 1;
        }
        amount = MathHelper.clamp_int(amount, 0, capacity);
        //(float)EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT))
        float scale = 1.0f - (((float)amount) / ((float)capacity));
        //this.renderQuad(type, t, iicon, 0.375f, 0.5625f, 0.25f, scale * 0.25f, 0.0d, 0.0d, 0.00005d);
        this.renderQuad(type, t, iicon, 0.375f, 0.5625f, 0.25f, scale * 0.25f, 0.0d, 0.0d, 0.0d);

        iicon = itemBucket.getIconPart(offset + 4); // 2: Bucket top part inside
        //this.renderQuad(type, t, iicon, 0.25f, 0.1875f, 0.5f, scale * 0.25f, 0.0d, 0.0d, 0.00005d);
        this.renderQuad(type, t, iicon, 0.25f, 0.1875f, 0.5f, scale * 0.25f, 0.0d, 0.0d, 0.0d);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);

        switch(type)
        {
            case INVENTORY:
                break;
            default:
                GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        GL11.glPopAttrib();
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
