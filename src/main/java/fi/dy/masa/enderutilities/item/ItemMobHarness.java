package fi.dy.masa.enderutilities.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemMobHarness extends ItemEnderUtilities
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemMobHarness()
    {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(ReferenceBlocksItems.NAME_ITEM_MOB_HARNESS);
        this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
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
            if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
                    && player.rotationPitch > 80.0f)
            {
                this.clearData(stack);
            }
        }

        return stack;
    }

    public boolean handleInteraction(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (player == null || entity == null)
        {
            return false;
        }
        boolean hasTarget = this.hasTarget(stack);

        if (player.isSneaking() == false)
        {
            EntityUtils.unmountRider(entity);
            player.mountEntity(entity);
            return true;
        }

        if (hasTarget == false)
        {
            // Looking up, player ridden by something and harness empty: dismount the rider
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
        // Harness bound to something, mount the entity
        else
        {
            this.mountTarget(stack, player.worldObj, player, entity);
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

        if (mode == (byte)1 || mode == (byte)2)
        {
            if (nbt.hasKey("TargetUUIDMost", Constants.NBT.TAG_LONG) == true &&
                nbt.hasKey("TargetUUIDLeast", Constants.NBT.TAG_LONG) == true &&
                nbt.hasKey("TargetName", Constants.NBT.TAG_STRING) == true)
            {
                return true;
            }
        }

        return false;
    }

    public ItemStack storeTarget(ItemStack stack, Entity entity)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (entity == null) { return stack; }

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        byte mode = (byte)1;

        if (entity instanceof EntityPlayer)
        {
            mode = (byte)2;
            nbt.setString("TargetName", ((EntityPlayer)entity).getCommandSenderName());
        }
        else
        {
            nbt.setString("TargetName", EntityList.getEntityString(entity));
        }

        nbt.setLong("TargetUUIDMost", entity.getUniqueID().getMostSignificantBits());
        nbt.setLong("TargetUUIDLeast", entity.getUniqueID().getLeastSignificantBits());
        nbt.setByte("Mode", mode);

        stack.setTagCompound(nbt);

        return stack;
    }

    public boolean mountTarget(ItemStack stack, World world, EntityPlayer player, Entity entity)
    {
        if (stack == null || player == null || entity == null || stack.getTagCompound() == null)
        {
            return false;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        byte mode = nbt.getByte("Mode");
        double radius = 4.0d;

        if (this.hasTarget(stack) == false)
        {
            return false;
        }

        long most = nbt.getLong("TargetUUIDMost");
        long least = nbt.getLong("TargetUUIDLeast");

        // Mode 1: mount non-player living mobs to eachother or to the player
        if (mode == (byte)1)
        {
            List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player,
                    AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - radius, player.posZ - radius,
                    player.posX + radius, player.posY + radius, player.posZ + radius));

            for (Entity ent : list)
            {
                // Matching entity found
                if (ent.getUniqueID().getMostSignificantBits() == most && ent.getUniqueID().getLeastSignificantBits() == least)
                {
                    // The harness was clicked twice on the same mob, mount that mob on top of the player
                    if (entity.getUniqueID().getMostSignificantBits() == most && entity.getUniqueID().getLeastSignificantBits() == least)
                    {
                        EntityUtils.unmountRider(player);
                        entity.mountEntity(player);
                        this.clearData(stack);
                    }
                    // The harness was clicked on two separate mobs, mount the stored/first one on top of the current one
                    else
                    {
                        EntityUtils.unmountRidden(ent);
                        EntityUtils.unmountRider(entity);
                        ent.mountEntity(entity);
                        this.clearData(stack);
                    }

                    break;
                }
            }
        }
        // Mode 2: mount a player
        else if (mode == (byte)2)
        {
            EntityPlayerMP targetPlayer = EntityUtils.findPlayerFromUUID(new UUID(most, least));
            if (targetPlayer == null)
            {
                return false;
            }

            // The harness was clicked twice on the same player, mount that player on top of the this player
            if (entity == targetPlayer) // && entity.getDistanceToEntity(player) <= radius)
            {
                EntityUtils.unmountRidden(player);
                targetPlayer.mountEntity(player);
                this.clearData(stack);
            }
            // Mount the target player on top of an entity
            else if (entity.getDistanceToEntity(player) <= radius && targetPlayer.getDistanceToEntity(player) <= radius)
            {
                EntityUtils.unmountRidden(entity);
                targetPlayer.mountEntity(entity);
                this.clearData(stack);
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
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt == null || this.hasTarget(stack) == false)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.notlinked"));
            return;
        }

        String pre = "" + EnumChatFormatting.BLUE;
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;
        String target = nbt.getString("TargetName");

        list.add(StatCollector.translateToLocal("gui.tooltip.linked") + ": " + pre + target + rst);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int metadata)
    {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString());
        this.iconArray = new IIcon[2];
        this.iconArray[0] = iconRegister.registerIcon(this.getIconString());
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".active");
    }

    /**
     * Return the correct icon for rendering based on the supplied ItemStack and render pass.
     *
     * Defers to {@link #getIconFromDamageForRenderPass(int, int)}
     * @param stack to render for
     * @param pass the multi-render pass
     * @return the icon
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        if (this.hasTarget(stack) == true)
        {
            return this.iconArray[1];
        }

        return this.iconArray[0];
    }
}
