package fi.dy.masa.enderutilities.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class ItemEnderArrow extends ItemEnderUtilities
{
    public ItemEnderArrow(String name)
    {
        super(name);

        this.setMaxStackSize(64);
        this.setMaxDamage(0);
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
