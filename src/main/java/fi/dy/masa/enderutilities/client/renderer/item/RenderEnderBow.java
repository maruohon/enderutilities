package fi.dy.masa.enderutilities.client.renderer.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEnderBow// implements IItemRenderer
{
    /*Minecraft mc = Minecraft.getMinecraft();

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        EntityLivingBase living = (EntityLivingBase) data[1];
        for (int i = 0; i < item.getItem().getRenderPasses(item.getItemDamage()) + 1; i++)
        {
            this.renderItem(living, item, i, type);
        }
    }

    public void renderItem(EntityLivingBase living, ItemStack stack, int renderPass, ItemRenderType type)
    {
        IIcon iicon = null;

        if (living instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer)living;
            if (player.getItemInUse() != null)
            {
                iicon = stack.getItem().getIcon(stack, renderPass, player, player.getItemInUse(), player.getItemInUseCount());
            }
            else
            {
                iicon = living.getItemIcon(stack, renderPass);
            }
        }
        else
        {
            iicon = living.getItemIcon(stack, renderPass);
        }

        if (iicon == null)
        {
            return;
        }

        GL11.glPushMatrix();
        TextureManager textureManager = this.mc.getTextureManager();
        textureManager.bindTexture(textureManager.getResourceLocation(stack.getItemSpriteNumber()));

        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
        {
            GL11.glTranslatef(0.6f, 0.5f, 0.5f);
        }
        else
        {
            GL11.glRotatef(10.0f, 0.0f, -1.0f, 1.0f);
            GL11.glRotatef(-10.0f, -0.8f, 0.0f, -0.8f);
            GL11.glRotatef(-20.0f, 0.1f, -1.0f, 0.2f);
            GL11.glTranslatef(-0.32f, 0.0f, 0.1f);
            GL11.glRotatef(180.0f, 0f, 0f, 1.0f);
            GL11.glRotatef(45.0f, 1.0f, 0.0f, 0.75f);
            GL11.glTranslatef(-0.6f, -0.25f, 1.0f);
            GL11.glScalef(1.75f, 1.75f, 1.75f);
        }

        Tessellator tessellator = Tessellator.instance;
        float f = iicon.getMinU();
        float f1 = iicon.getMaxU();
        float f2 = iicon.getMinV();
        float f3 = iicon.getMaxV();
        float f4 = 0.0f;
        float f5 = 0.3f;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(-f4, -f5, 0.0f);
        float f6 = 1.5f;
        GL11.glScalef(f6, f6, f6);
        GL11.glRotatef(50.0f, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(335.0f, 0.0f, 0.0f, 1.0f);
        GL11.glTranslatef(-0.9375f, -0.0625f, 0.0f);

        ItemRenderer.renderItemIn2D(tessellator, f1, f2, f, f3, iicon.getIconWidth(), iicon.getIconHeight(), 0.0625f);

        GL11.glPopMatrix();
    }*/
}
