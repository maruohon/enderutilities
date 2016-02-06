package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.client.effects.Effects;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class ItemEnderPorter extends ItemLocationBoundModular
{
    public static final int ENDER_CHARGE_COST_INTER_DIM_TP = 5000;
    public static final int ENDER_CHARGE_COST_CROSS_DIM_TP = 25000;
    private static final int USE_TIME = 40;

    public ItemEnderPorter()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_PORTER);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // damage 1: Ender Porter (Advanced)
        if (stack.getItemDamage() == 1)
        {
            return super.getUnlocalizedName() + ".advanced";
        }

        return super.getUnlocalizedName();
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (player == null || player.worldObj.isRemote == true || player.isSneaking() == false
            || NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return false;
        }

        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        // The basic version can only teleport inside the same dimension
        if (target != null && EntityUtils.doesEntityStackHaveBlacklistedEntities(entity) == false
            && (stack.getItemDamage() == 1 || target.dimension == entity.dimension))
        {
            int cost = (target.dimension == entity.dimension ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);
            if (UtilItemModular.useEnderCharge(stack, cost, false) == false)
            {
                return false;
            }

            // If the target entity is a player, then they have to be sneaking too
            if ((entity instanceof EntityPlayer) == false || ((EntityPlayer)entity).isSneaking() == true)
            {
                UtilItemModular.useEnderCharge(stack, cost, true);
                TeleportEntity.teleportEntityUsingModularItem(entity, stack, true, true);
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        // This needs to also happen on the client, otherwise the in-use will derp up

        if (player == null || NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return stack;
        }

        // Don't activate when sneaking and looking at a block, aka. binding to a new location
        if (player.isSneaking() == true)
        {
            MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);
            if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
            {
                return stack;
            }
        }

        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        // The basic version can only teleport inside the same dimension
        if (target != null && EntityUtils.doesEntityStackHaveBlacklistedEntities(player) == false
            && (stack.getItemDamage() == 1 || target.dimension == player.dimension))
        {
            int cost = (target.dimension == player.dimension ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);
            if (UtilItemModular.useEnderCharge(stack, cost, false) == false)
            {
                return stack;
            }

            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));

            if (world.isRemote == false)
            {
                Effects.playSoundEffectServer(world, player.posX, player.posY, player.posZ, "portal.travel", 0.06f, 1.2f);
            }
        }

        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int inUseCount)
    {
        if (player == null || NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return;
        }

        int useTime = USE_TIME;
        // Use a shorter delay in creative mode
        if (player.capabilities.isCreativeMode == true)
        {
            useTime >>= 2;
        }

        if ((this.getMaxItemUseDuration(stack) - inUseCount) >= useTime)
        {
            NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            if (target == null || (stack.getItemDamage() == 0 && target.dimension != player.dimension))
            {
                return;
            }

            int cost = (target.dimension == player.dimension ? ENDER_CHARGE_COST_INTER_DIM_TP : ENDER_CHARGE_COST_CROSS_DIM_TP);
            if (UtilItemModular.useEnderCharge(stack, cost, true) == false)
            {
                return;
            }

            TeleportEntity.teleportEntityUsingModularItem(player, stack, true, true);
        }
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 10;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 9;
        }

        return 0;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "tex=basic.stage.0"),
                new ModelResourceLocation(rl, "tex=basic.stage.1"),
                new ModelResourceLocation(rl, "tex=basic.stage.2"),
                new ModelResourceLocation(rl, "tex=basic.stage.3"),
                new ModelResourceLocation(rl, "tex=basic.stage.4"),
                new ModelResourceLocation(rl, "tex=basic.stage.5"),
                new ModelResourceLocation(rl, "tex=basic.stage.6"),
                new ModelResourceLocation(rl, "tex=advanced.stage.0"),
                new ModelResourceLocation(rl, "tex=advanced.stage.1"),
                new ModelResourceLocation(rl, "tex=advanced.stage.2"),
                new ModelResourceLocation(rl, "tex=advanced.stage.3"),
                new ModelResourceLocation(rl, "tex=advanced.stage.4"),
                new ModelResourceLocation(rl, "tex=advanced.stage.5"),
                new ModelResourceLocation(rl, "tex=advanced.stage.6")
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining)
    {
        int index = 0;
        String pre = stack.getItemDamage() == 1 ? "tex=advanced.stage." : "tex=basic.stage.";

        if (player != null && player.getItemInUse() != null)
        {
            index = MathHelper.clamp_int((this.getMaxItemUseDuration(stack) - useRemaining) / 4, 0, 6);
        }

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, pre + index);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return this.getModel(stack, null, 0);
    }
}
