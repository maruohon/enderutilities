package fi.dy.masa.enderutilities.item;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.entity.ai.EntityAIControlledByPlayerUsingHarness;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemMobHarness extends ItemEnderUtilities
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

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
        return super.getItemStackDisplayName(stack) + " " + EnumChatFormatting.GREEN + target + EnumChatFormatting.RESET + EnumChatFormatting.WHITE;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == true)
        {
            return stack;
        }

        if (player.isSneaking() == true)
        {
            MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
            if (movingobjectposition != null && movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY
                && player.rotationPitch > 80.0f)
            {
                this.clearData(stack);
            }
        }

        return stack;
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
            EntityUtils.unmountRider(entity);
            player.mountEntity(entity);
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
            if (player.rotationPitch < -80.0f && player.riddenByEntity != null)
            {
                player.riddenByEntity.mountEntity(null);
            }
            // Empty harness, target is riding something: dismount target
            else if (entity.ridingEntity != null)
            {
                entity.mountEntity(null);
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
        nbt.setString("TargetName", entity.getCommandSenderName());
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
            EntityUtils.unmountRider(player);
            targetEntity.mountEntity(player);
            this.clearData(stack);

            return true;
        }
        // The harness was clicked on two separate entities, mount the stored/first one on top of the currently targeted one
        else
        {
            // Mode 1: mount non-player entities to each other or to the player
            if (mode == 1)
            {
                List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(player.posX - r, player.posY - r, player.posZ - r, player.posX + r, player.posY + r, player.posZ + r));
                storedEntity = EntityUtils.findEntityByUUID(entities, storedUUID);
            }
            // Mode 2: mount a player
            else if (mode == 2)
            {
                storedEntity = world.func_152378_a(storedUUID); // getPlayerEntityByUUID()
            }

            // Matching (stored) entity found
            if (storedEntity != null && storedEntity.dimension == player.dimension)
            {
                EntityUtils.unmountRider(targetEntity);
                storedEntity.mountEntity(targetEntity);
                this.clearData(stack);

                return true;
            }
            else if (storedEntity == null)
            {
                player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.mobharness.targetnotfoundoroutofrange"));
            }
        }

        return false;
    }

    public boolean clearData(ItemStack stack)
    {
        stack.setTagCompound(null);

        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null || this.hasTarget(stack) == false)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.notlinked"));
            return;
        }

        String target = stack.getTagCompound().getString("TargetName");
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.linked") + ": " + EnumChatFormatting.GREEN + target + EnumChatFormatting.RESET + EnumChatFormatting.GRAY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses(int metadata)
    {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString());
        this.iconArray = new IIcon[2];
        this.iconArray[0] = iconRegister.registerIcon(this.getIconString());
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".active");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int pass)
    {
        if (this.hasTarget(stack) == true)
        {
            return this.iconArray[1];
        }

        return this.iconArray[0];
    }
}
