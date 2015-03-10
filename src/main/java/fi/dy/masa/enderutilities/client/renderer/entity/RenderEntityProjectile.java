package fi.dy.masa.enderutilities.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.entity.IItemData;

@SideOnly(Side.CLIENT)
public class RenderEntityProjectile extends RenderSnowball
{
    //private final RenderItem renderItem;
    //private Item item;

    public RenderEntityProjectile(RenderManager renderManager, Item item, RenderItem renderItem)
    {
        super(renderManager, item, renderItem);
        //this.item = item;
        //this.renderItem = renderItem;
    }

    /*@Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        this.bindTexture(TextureMap.locationBlocksTexture);
        //Tessellator tessellator = Tessellator.getInstance();
        //WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        this.renderItem.renderItemModel(this.getItemStack(entity));
        //this.drawQuad(tessellator, iicon);

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }*/

    @Override
    public ItemStack func_177082_d(Entity entity)
    {
        int damage = (entity instanceof IItemData ? ((IItemData)entity).getItemDamage(entity) : 0);
        return new ItemStack(this.field_177084_a, 1, damage);
    }

    /*@Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_)
    {
        return TextureMap.locationBlocksTexture;
    }*/

    /*private void drawQuad(Tessellator tessellator, WorldRenderer worldRenderer)
    {
        GlStateManager.rotate(180.0f - this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);

        //GlStateManager.normal(0.0f, 1.0f, 0.0f);
        worldRenderer.startDrawingQuads();
        worldRenderer.addVertexWithUV(-0.5d, -0.25d, 0.0d, (double)minU, (double)maxV);
        worldRenderer.addVertexWithUV( 0.5d, -0.25d, 0.0d, (double)maxU, (double)maxV);
        worldRenderer.addVertexWithUV( 0.5d,  0.75d, 0.0d, (double)maxU, (double)minV);
        worldRenderer.addVertexWithUV(-0.5d,  0.75d, 0.0d, (double)minU, (double)minV);
        tessellator.draw();
    }*/
}
