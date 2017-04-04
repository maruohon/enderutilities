package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemSyringe extends ItemEnderUtilities
{
    public static final String TAG_PASSIFIED = Reference.MOD_ID + ":passified";

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
            if (playerIn.getEntityWorld().isRemote == false)
            {
                EntityUtils.removeAllAITargetTasks(living);
                living.addTag(TAG_PASSIFIED);

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

    public static boolean removePassifiedState(Entity entity)
    {
        entity.dismountRidingEntity();
        entity.getTags().remove(TAG_PASSIFIED);
        NBTTagCompound tag = new NBTTagCompound();

        if (entity.writeToNBTOptional(tag))
        {
            World world = entity.getEntityWorld();
            Entity entityNew = EntityList.createEntityFromNBT(tag, world);

            if (entityNew != null)
            {
                entity.isDead = true;

                // This removal code is from World#updateEntities()
                // We need to do it here so that the new entity with the same UUID can be spawned
                int cx = entity.chunkCoordX;
                int cz = entity.chunkCoordZ;

                if (entity.addedToChunk)
                {
                    world.getChunkFromChunkCoords(cx, cz).removeEntity(entity);
                }

                world.loadedEntityList.remove(entity);
                world.onEntityRemoved(entity);

                world.spawnEntity(entityNew);

                return true;
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
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
