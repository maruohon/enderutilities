package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SideOnly(Side.CLIENT)
public class RenderEnderArrow extends RenderArrow
{
    public RenderEnderArrow(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityArrow entityArrow)
    {
        return new ResourceLocation(ReferenceTextures.getEntityTextureName(ReferenceNames.NAME_ENTITY_ENDER_ARROW));
    }
}
