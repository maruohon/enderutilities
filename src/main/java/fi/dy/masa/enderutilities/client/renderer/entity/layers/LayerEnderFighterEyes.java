package fi.dy.masa.enderutilities.client.renderer.entity.layers;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SideOnly(Side.CLIENT)
public class LayerEnderFighterEyes implements LayerRenderer
{
    private static final ResourceLocation TEXTURE_EYES_NORMAL = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.eyes.normal"));
    private static final ResourceLocation TEXTURE_EYES_RAGING = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.eyes.raging"));
    private final RenderEndermanFighter renderEnderFighter;

    public LayerEnderFighterEyes(RenderEndermanFighter ref)
    {
        this.renderEnderFighter = ref;
    }

    public void func_177201_a(EntityEndermanFighter entityEnderFighter, float x, float y, float z, float p_177201_5_, float p_177201_6_, float p_177201_7_, float p_177201_8_)
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

        if (entityEnderFighter.isInvisible())
        {
            GlStateManager.depthMask(false);
        }
        else
        {
            GlStateManager.depthMask(true);
        }

        char c0 = 61680;
        int i = c0 % 65536;
        int j = c0 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)i / 1.0F, (float)j / 1.0F);
        GlStateManager.enableLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderEnderFighter.getMainModel().render(entityEnderFighter, x, y, p_177201_5_, p_177201_6_, p_177201_7_, p_177201_8_);
        this.renderEnderFighter.func_177105_a(entityEnderFighter, z);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_)
    {
        this.func_177201_a((EntityEndermanFighter)entitylivingbaseIn, p_177141_2_, p_177141_3_, p_177141_4_, p_177141_5_, p_177141_6_, p_177141_7_, p_177141_8_);
    }
}
