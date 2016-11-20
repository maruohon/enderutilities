package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemSyringe extends ItemEnderUtilities
{
    public ItemSyringe()
    {
        super();
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_SYRINGE);
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
            default:
        }

        return name + "_empty";
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
    {
        if (stack.getMetadata() == 0)
        {
            return false;
        }

        // Paralyzer - set NoAI
        if (stack.getMetadata() == 1 && (target instanceof EntityLiving) && ((EntityLiving) target).isAIDisabled() == false)
        {
            ((EntityLiving) target).setNoAI(true);

            if (playerIn.capabilities.isCreativeMode == false)
            {
                stack.setItemDamage(0);
            }

            playerIn.getEntityWorld().playSound(null, playerIn.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.MASTER, 0.9f, 3.0f);
            return true;
        }

        // Stimulant - disable NoAI
        if (stack.getMetadata() == 2 && (target instanceof EntityLiving) && ((EntityLiving) target).isAIDisabled())
        {
            ((EntityLiving) target).setNoAI(false);

            if (playerIn.capabilities.isCreativeMode == false)
            {
                stack.setItemDamage(0);
            }

            playerIn.getEntityWorld().playSound(null, playerIn.getPosition(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.MASTER, 0.9f, 3.0f);
            return true;
        }

        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Empty syringe
        list.add(new ItemStack(this, 1, 1)); // Syringe with paralyzer
        list.add(new ItemStack(this, 1, 2)); // Syringe with stimulant
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ModelResourceLocation[] {
                new ModelResourceLocation(rl, "type=empty"),
                new ModelResourceLocation(rl, "type=paralyzer"),
                new ModelResourceLocation(rl, "type=stimulant")
        };
    }
}
