package fi.dy.masa.enderutilities.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.entity.ai.EntityAIControlledByPlayerUsingHarness;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemMobHarness extends ItemEnderUtilities
{
    public ItemMobHarness()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_MOB_HARNESS);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        if (stack.getTagCompound() == null || this.hasTarget(stack) == false)
        {
            return super.getItemStackDisplayName(stack);
        }

        String target = stack.getTagCompound().getString("TargetName");
        return super.getItemStackDisplayName(stack) + " " + TextFormatting.GREEN + target + TextFormatting.RESET + TextFormatting.WHITE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote == true)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        if (player.isSneaking() == true)
        {
            RayTraceResult rayTraceResult = this.getMovingObjectPositionFromPlayer(world, player, true);
            if (rayTraceResult != null && rayTraceResult.typeOfHit != RayTraceResult.Type.ENTITY
                && player.rotationPitch > 80.0f)
            {
                this.clearData(stack);
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @SuppressWarnings("unchecked")
    public static boolean addAITask(Entity entity, boolean replaceOld)
    {
        Entity bottom = EntityUtils.getBottomEntity(entity);
        if (bottom instanceof EntityLiving)
        {
            ((EntityLiving)bottom).getNavigator().clearPathEntity();
            // Add a new AI task as the highest priority task after swimming and panic AI tasks
            EntityUtils.addAITaskAfterTasks((EntityLiving)bottom, new EntityAIControlledByPlayerUsingHarness((EntityLiving)bottom, 0.3f), replaceOld, new Class[] {EntityAISwimming.class, EntityAIPanic.class});

            return true;
        }

        return false;
    }

    public boolean handleInteraction(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (player.isSneaking() == false)
        {
            //EntityUtils.unmountFirstRider(entity);
            player.startRiding(entity, true);
            addAITask(entity, true);

            return true;
        }

        // Harness bound to something, mount the entity
        if (this.hasTarget(stack) == true)
        {
            this.mountTarget(stack, player.worldObj, player, entity);
        }
        // Empty harness
        else
        {
            // Empty harness, player looking up and ridden by something: dismount the rider
            if (player.rotationPitch < -80.0f && player.isBeingRidden() == true)
            {
                player.removePassengers();
            }
            // Empty harness, target is riding something: dismount target
            else if (entity.isRiding() == true)
            {
                entity.dismountRidingEntity();
            }
            // Empty harness, target not riding anything, store/link target
            else
            {
                this.storeTarget(stack, entity);
            }
        }

        return true;
    }

    public boolean hasTarget(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt == null || nbt.hasKey("Mode", Constants.NBT.TAG_BYTE) == false)
        {
            return false;
        }

        byte mode = nbt.getByte("Mode");

        if (mode >= 1 && mode <= 2 &&
            nbt.hasKey("TargetUUIDMost", Constants.NBT.TAG_LONG) == true &&
            nbt.hasKey("TargetUUIDLeast", Constants.NBT.TAG_LONG) == true &&
            nbt.hasKey("TargetName", Constants.NBT.TAG_STRING) == true)
        {
            return true;
        }

        return false;
    }

    public ItemStack storeTarget(ItemStack stack, Entity entity)
    {
        NBTTagCompound nbt = NBTUtils.getCompoundTag(stack, null, true);

        byte mode = (byte)(entity instanceof EntityPlayer ? 2 : 1);
        nbt.setString("TargetName", entity.getName());
        nbt.setLong("TargetUUIDMost", entity.getUniqueID().getMostSignificantBits());
        nbt.setLong("TargetUUIDLeast", entity.getUniqueID().getLeastSignificantBits());
        nbt.setByte("Mode", mode);

        return stack;
    }

    public boolean mountTarget(ItemStack stack, World world, EntityPlayer player, Entity targetEntity)
    {
        if (stack == null || stack.getTagCompound() == null || targetEntity == null)
        {
            return false;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        byte mode = nbt.getByte("Mode");
        UUID storedUUID = new UUID(nbt.getLong("TargetUUIDMost"), nbt.getLong("TargetUUIDLeast"));
        Entity storedEntity = null;
        double r = 64.0d;

        // The harness was clicked twice on the same entity, mount that entity on top of the player
        if (storedUUID.equals(targetEntity.getUniqueID()))
        {
            EntityUtils.unmountFirstRider(player);
            targetEntity.startRiding(player);
            this.clearData(stack);

            return true;
        }
        // The harness was clicked on two separate entities, mount the stored/first one on top of the currently targeted one
        else
        {
            // Mode 1: mount non-player entities to each other or to the player
            if (mode == 1)
            {
                List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(player,
                        new AxisAlignedBB(player.posX - r, player.posY - r, player.posZ - r,
                                player.posX + r, player.posY + r, player.posZ + r));
                storedEntity = EntityUtils.findEntityByUUID(entities, storedUUID);
            }
            // Mode 2: mount a player
            else if (mode == 2)
            {
                storedEntity = world.getPlayerEntityByUUID(storedUUID);
            }

            // Matching (stored) entity found
            if (storedEntity != null && storedEntity.dimension == player.dimension)
            {
                //EntityUtils.unmountFirstRider(targetEntity);
                storedEntity.startRiding(targetEntity, true);
                this.clearData(stack);

                return true;
            }
            else if (storedEntity == null && world.isRemote == false)
            {
                player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.mobharness.targetnotfoundoroutofrange"));
            }
        }

        return false;
    }

    public boolean clearData(ItemStack stack)
    {
        stack.setTagCompound(null);

        return true;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null || this.hasTarget(stack) == false)
        {
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.notlinked"));
            return;
        }

        String target = stack.getTagCompound().getString("TargetName");
        list.add(I18n.translateToLocal("enderutilities.tooltip.item.linked") + ": " + TextFormatting.GREEN + target + TextFormatting.RESET + TextFormatting.GRAY);
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation("underutilities:linked"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return stack != null && ItemMobHarness.this.hasTarget(stack) == true ? 1.0F : 0.0F;
            }
        });
    }
}
