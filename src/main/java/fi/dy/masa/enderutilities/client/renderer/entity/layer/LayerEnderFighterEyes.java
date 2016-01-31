package fi.dy.masa.enderutilities.client.renderer.entity.layer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SideOnly(Side.CLIENT)
public class LayerEnderFighterEyes implements LayerRenderer<EntityEndermanFighter>
{
    private static final ResourceLocation TEXTURE_EYES_NORMAL = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.eyes.normal"));
    private static final ResourceLocation TEXTURE_EYES_RAGING = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.eyes.raging"));
    private final RenderEndermanFighter renderEnderFighter;

    public LayerEnderFighterEyes(RenderEndermanFighter ref)
    {
        this.renderEnderFighter = ref;
    }

    public void doRenderLayer(EntityEndermanFighter entityEnderFighter, float p_177201_2, float p_177201_3, float partialTicks, float p_177201_5_, float p_177201_6_, float p_177201_7_, float scale)
    {
        if (entityEnderFighter.isRaging() == true)
        {
            this.renderEnderFighter.bindTexture(TEXTURE_EYES_RAGING);
        }
        else
        {
            this.renderEnderFighter.bindTexture(TEXTURE_EYES_NORMAL);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.blendFunc(1, 1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(entityEnderFighter.isInvisible() == false);

        char c0 = 61680;
        int i = c0 % 65536;
        int j = c0 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)i / 1.0F, (float)j / 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderEnderFighter.getMainModel().render(entityEnderFighter, p_177201_2, p_177201_3, p_177201_5_, p_177201_6_, p_177201_7_, scale);
        this.renderEnderFighter.func_177105_a(entityEnderFighter, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
