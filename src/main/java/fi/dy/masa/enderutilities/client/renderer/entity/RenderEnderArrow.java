package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

@SideOnly(Side.CLIENT)
public class RenderEnderArrow extends RenderArrow
{
    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    protected ResourceLocation getEntityTexture(EntityArrow par1EntityArrow)
    {
        return new ResourceLocation(ReferenceTextures.getEntityTextureName(ReferenceNames.NAME_ENTITY_ENDER_ARROW));
    }
}
