package fi.dy.masa.enderutilities.client.renderer.entity;

import java.util.Random;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.client.renderer.entity.layer.LayerEnderFighterEyes;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class RenderEndermanFighter extends RenderLiving<EntityEndermanFighter>
{
    private static final ResourceLocation TEXTURE_ENDERMAN = new ResourceLocation(ReferenceTextures.getEntityTextureName("endermanfighter.body"));

    private ModelEnderman endermanModel;
    private Random rnd = new Random();

    public RenderEndermanFighter(RenderManager renderManager)
    {
        super(renderManager, new ModelEnderman(0.0f), 0.5F);
        this.endermanModel = (ModelEnderman)super.mainModel;
        this.addLayer(new LayerEnderFighterEyes(this));
    }

    public void doRender(EntityEndermanFighter entityEnderFighter, double x, double y, double z, float entityYaw, float partialTicks)
    {
        this.endermanModel.isAttacking = entityEnderFighter.isScreaming();

        if (entityEnderFighter.isScreaming())
        {
            double d3 = 0.02D;
            x += this.rnd.nextGaussian() * d3;
            z += this.rnd.nextGaussian() * d3;
        }

        super.doRender(entityEnderFighter, x, y, z, entityYaw, partialTicks);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEndermanFighter entity)
    {
        return TEXTURE_ENDERMAN;
    }
}
