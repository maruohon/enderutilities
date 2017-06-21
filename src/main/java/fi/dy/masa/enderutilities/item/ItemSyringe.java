package fi.dy.masa.enderutilities.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemSyringe extends ItemEnderUtilities
{
    public static final String TAG_PASSIFIED = Reference.MOD_ID + ":passified";

    public ItemSyringe(String name)
    {
        super(name);

        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName();

        switch (stack.getMetadata())
        {
            case 0:
                return name + "_empty";
            case 1:
                return name + "_paralyzer";
            case 2:
                return name + "_stimulant";
            case 3:
                return name + "_passifier";
            default:
        }

        return name + "_empty";
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        int meta = stack.getMetadata();

        if (meta == 0 || (target instanceof EntityLiving) == false)
        {
            return super.itemInteractionForEntity(stack, playerIn, target, hand);
        }

        EntityLiving living = (EntityLiving) target;

        // 1: Paralyzer - set NoAI
        // 2: Stimulant - disable NoAI
        if (meta == 1 || meta == 2)
        {
            boolean noAI = meta == 1;

            if (living.isAIDisabled() != noAI)
            {
                if (playerIn.getEntityWorld().isRemote == false)
                {
                    living.setNoAI(noAI);

                    if (playerIn.capabilities.isCreativeMode == false)
                    {
                        stack.setItemDamage(0);
                    }

                    playerIn.getEntityWorld().playSound(null, playerIn.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.MASTER, 0.9f, 3.0f);
                }

                return true;
            }
        }

        // 3: Passifier - remove target AI tasks and set a flag which causes the removal to happen on (re-)spawn
        if (meta == 3)
        {
            if (playerIn.getEntityWorld().isRemote == false && passifyEntity(living))
            {
                if (playerIn.capabilities.isCreativeMode == false)
                {
                    stack.setItemDamage(0);
                }

                playerIn.getEntityWorld().playSound(null, playerIn.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.MASTER, 0.9f, 3.0f);
            }

            return true;
        }

        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    public static boolean passifyEntity(EntityLiving living)
    {
        living.addTag(TAG_PASSIFIED);
        return EntityUtils.addDummyAIBlockerTask(living, living.targetTasks, -10, 0xFF);
    }

    public static void removePassifiedState(EntityLiving living)
    {
        living.getTags().remove(TAG_PASSIFIED);
        EntityUtils.removeDummyAIBlockerTask(living.targetTasks);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItemsCustom(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Empty syringe
        list.add(new ItemStack(this, 1, 1)); // Syringe with paralyzer
        list.add(new ItemStack(this, 1, 2)); // Syringe with stimulant
        list.add(new ItemStack(this, 1, 3)); // Syringe with passifier
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ModelResourceLocation[] {
                new ModelResourceLocation(rl, "type=empty"),
                new ModelResourceLocation(rl, "type=paralyzer"),
                new ModelResourceLocation(rl, "type=stimulant"),
                new ModelResourceLocation(rl, "type=passifier")
        };
    }
}
