package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.entity.EntityChair;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class RenderChair extends Render<EntityChair>
{
    private static final ResourceLocation RESOURCE = new ResourceLocation(ReferenceTextures.getEntityTextureName(ReferenceNames.NAME_ENTITY_CHAIR));

    public RenderChair(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityChair entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        // NO-OP
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityChair entity)
    {
        return RESOURCE;
    }
}
