package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemEnderPearlReusable extends ItemEnderUtilities
{
    public ItemEnderPearlReusable()
    {
        this.setMaxStackSize(4);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_PEARL_REUSABLE);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // damage 1: Elite Ender Pearl
        if (stack.getItemDamage() == 1)
        {
            return super.getUnlocalizedName() + ".elite";
        }

        return super.getUnlocalizedName();
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == true)
        {
            return stack;
        }

        // Damage 1: "Elite version" of the pearl, makes the thrower fly with it. Idea by xisumavoid in episode Hermitcraft III 303 :)

        EntityEnderPearlReusable pearl = new EntityEnderPearlReusable(world, player, stack.getItemDamage() == 1);
        world.spawnEntityInWorld(pearl);

        if (stack.getItemDamage() == 1)
        {
            Entity bottomEntity = EntityUtils.getBottomEntity(player);

            // Dismount the previous pearl if we are already riding one
            if (bottomEntity instanceof EntityEnderPearlReusable && bottomEntity.riddenByEntity != null)
            {
                bottomEntity = bottomEntity.riddenByEntity;
            }

            bottomEntity.mountEntity(pearl);
        }

        --stack.stackSize;
        world.playSoundAtEntity(player, "random.bow", 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f));

        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        if (Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            list.add(new ItemStack(this, 1, 0)); // Regular
            list.add(new ItemStack(this, 1, 1)); // Elite
        }
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "elite=false"),
                new ModelResourceLocation(rl, "elite=true")
        };
    }
}
