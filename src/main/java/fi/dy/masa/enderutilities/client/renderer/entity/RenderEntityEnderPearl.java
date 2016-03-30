package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.base.IItemData;

public class RenderEntityEnderPearl extends Render<EntityEnderPearlReusable>
{
    private Item item;
    private final RenderItem renderItem;

    public RenderEntityEnderPearl(RenderManager renderManager, Item item)
    {
        super(renderManager);
        this.item = item;
        this.renderItem = Minecraft.getMinecraft().getRenderItem();
    }

    @Override
    public void doRender(EntityEnderPearlReusable entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        this.bindTexture(TextureMap.locationBlocksTexture);

        this.renderItem.renderItem(this.getItemStack(entity), ItemCameraTransforms.TransformType.GROUND);

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ItemStack getItemStack(EntityEnderPearlReusable entity)
    {
        if (entity instanceof IItemData)
        {
            return new ItemStack(this.item, 1, ((IItemData)entity).getItemDamage(entity));
        }

        return new ItemStack(this.item, 1, 0);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityEnderPearlReusable entity)
    {
        return TextureMap.locationBlocksTexture;
    }
}
