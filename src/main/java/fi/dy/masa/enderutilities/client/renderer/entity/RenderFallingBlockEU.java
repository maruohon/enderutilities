package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.EntityFallingBlockEU;

public class RenderFallingBlockEU extends Render<EntityFallingBlockEU>
{
    public RenderFallingBlockEU(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
        this.shadowSize = 0.5F;
    }

    public void doRender(EntityFallingBlockEU entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        IBlockState state = entity.getBlockState();

        if (state != null && state.getRenderType() == EnumBlockRenderType.MODEL)
        {
            World world = entity.getEntityWorld();

            if (state != world.getBlockState(new BlockPos(entity)) && state.getRenderType() != EnumBlockRenderType.INVISIBLE)
            {
                this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                if (this.renderOutlines)
                {
                    GlStateManager.enableColorMaterial();
                    GlStateManager.enableOutlineMode(this.getTeamColor(entity));
                }

                buffer.begin(7, DefaultVertexFormats.BLOCK);
                BlockPos pos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
                GlStateManager.translate((float)(x - pos.getX() - 0.5D), (float)(y - pos.getY()), (float)(z - pos.getZ() - 0.5D));
                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                dispatcher.getBlockModelRenderer().renderModel(
                    world, dispatcher.getModelForState(state), state, pos, buffer, false, MathHelper.getPositionRandom(entity.getOrigin()));
                tessellator.draw();

                if (this.renderOutlines)
                {
                    GlStateManager.disableOutlineMode();
                    GlStateManager.disableColorMaterial();
                }

                GlStateManager.enableLighting();
                GlStateManager.popMatrix();

                super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityFallingBlockEU entity)
    {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
