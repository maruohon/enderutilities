package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.client.effects.Sounds;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntity;

public class ItemEnderPorter extends ItemLocationBoundModular
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

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

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
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
            if (UtilItemModular.useEnderCharge(stack, player, cost, false) == false)
            {
                return stack;
            }

            player.setItemInUse(stack, this.getMaxItemUseDuration(stack));

            if (world.isRemote == false)
            {
                Sounds.playSoundEffectServer(world, player.posX, player.posY, player.posZ, "portal.travel", 0.06f, 1.2f);
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
            if (UtilItemModular.useEnderCharge(stack, player, cost, true) == false)
            {
                return;
            }

            TeleportEntity.teleportEntityUsingModularItem(player, stack, true, true);
        }
    }

    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 5;
    }

    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 4;
        }

        return 0;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
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
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".stage.1");
        this.iconArray = new IIcon[14];

        for (int i = 0; i < 7; ++i)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + ".stage." + (i + 1));
        }

        for (int i = 0; i < 7; ++i)
        {
            this.iconArray[7 + i] = iconRegister.registerIcon(this.getIconString() + ".advanced.stage." + (i + 1));
        }
    }

    /**
     * Used to cycle through icons based on their used duration, i.e. for the bow
     */
    @SideOnly(Side.CLIENT)
    public IIcon getItemIconForUseDuration(int index)
    {
        if (index >= this.iconArray.length)
        {
            index = this.iconArray.length - 1;
        }
        if (index < 0)
        {
            index = 0;
        }

        return this.iconArray[index];
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
        return this.getIcon(stack, renderPass, null, null, 0);
    }

    /**
     * Player, Render pass, and item usage sensitive version of getIconIndex.
     *
     * @param stack The item stack to get the icon for. (Usually this, and usingItem will be the same if usingItem is not null)
     * @param renderPass The pass to get the icon for, 0 is default.
     * @param player The player holding the item
     * @param usingItem The item the player is actively using. Can be null if not using anything.
     * @param useRemaining The ticks remaining for the active item.
     * @return The icon index
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
        int index = 0;

        if (player != null && player.getItemInUse() != null && stack != null)
        {
            int inUse = stack.getMaxItemUseDuration() - useRemaining;
            int useTime = USE_TIME;

            // Use a shorter delay in creative mode
            if (player.capabilities.isCreativeMode == true)
            {
                useTime >>= 2;
            }

            index += (7 * inUse / useTime); // 7 stages/icons

            if (index > 6)
            {
                index = 6;
            }
        }

        // damage 1: 'Ender Porter (Advanced)', offset the icon range
        if (stack.getItemDamage() == 1)
        {
            index += 7;
        }

        return this.getItemIconForUseDuration(index);
    }
}
