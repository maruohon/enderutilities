package fi.dy.masa.enderutilities.client.renderer.entity;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SideOnly(Side.CLIENT)
public class RenderEndermanFighter extends RenderLiving
{
    private static final ResourceLocation TEXTURE_EYES_NORMAL = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.eyes.normal"));
    private static final ResourceLocation TEXTURE_EYES_RAGING = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.eyes.raging"));
    private static final ResourceLocation TEXTURE_ENDERMAN = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.body"));
    /** The model of the enderman */
    private ModelEnderman endermanModel;
    private Random rnd = new Random();

    public RenderEndermanFighter()
    {
        super(new ModelEnderman(), 0.5F);
        this.endermanModel = (ModelEnderman)super.mainModel;
        this.setRenderPassModel(this.endermanModel);
    }

    public void doRender(EntityEndermanFighter enderman, double x, double y, double z, float p_76986_8_, float p_76986_9_)
    {
        this.endermanModel.isAttacking = enderman.isScreaming();

        if (enderman.isScreaming())
        {
            double d3 = 0.02D;
            x += this.rnd.nextGaussian() * d3;
            z += this.rnd.nextGaussian() * d3;
        }

        super.doRender((EntityLiving)enderman, x, y, z, p_76986_8_, p_76986_9_);
    }

    protected ResourceLocation getEntityTexture(EntityEndermanFighter p_110775_1_)
    {
        return TEXTURE_ENDERMAN;
    }

    protected void renderEquippedItems(EntityEndermanFighter enderman, float p_77029_2_)
    {
        super.renderEquippedItems(enderman, p_77029_2_);
    }

    /**
     * Queries whether should render the specified pass or not.
     */
    protected int shouldRenderPass(EntityEndermanFighter enderman, int pass, float p_77032_3_)
    {
        if (pass != 0)
        {
            return -1;
        }

        if (enderman.isRaging() == true)
        {
            this.bindTexture(TEXTURE_EYES_RAGING);
        }
        else
        {
            this.bindTexture(TEXTURE_EYES_NORMAL);
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (enderman.isInvisible())
        {
            GL11.glDepthMask(false);
        }
        else
        {
            GL11.glDepthMask(true);
        }

        char c0 = 61680;
        int j = c0 % 65536;
        int k = c0 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j, (float)k);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        return 1;
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float p_76986_8_, float p_76986_9_)
    {
        this.doRender((EntityEndermanFighter)entity, x, y, z, p_76986_8_, p_76986_9_);
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int p_77032_2_, float p_77032_3_)
    {
        return this.shouldRenderPass((EntityEndermanFighter)entity, p_77032_2_, p_77032_3_);
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase entity, float p_77029_2_)
    {
        this.renderEquippedItems((EntityEndermanFighter)entity, p_77029_2_);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return this.getEntityTexture((EntityEndermanFighter)entity);
    }
}
