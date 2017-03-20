package fi.dy.masa.enderutilities.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class ItemEnderArrow extends ItemEnderUtilities
{
    public ItemEnderArrow()
    {
        super();
        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_ARROW);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (entity instanceof EntityLivingBase)
        {
            TeleportEntity.teleportEntityRandomly((EntityLivingBase)entity, 10.0d);
            return true;
        }

        return false;
    }
}
