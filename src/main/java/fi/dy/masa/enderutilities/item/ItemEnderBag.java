package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.setup.Registry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBag extends ItemLocationBoundModular implements IChunkLoadingItem, IKeyBound
{
    public static final int ENDER_CHARGE_COST = 200;

    public ItemEnderBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BAG);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == true || stack == null || stack.getTagCompound() == null)
        {
            return stack;
        }

        NBTTagCompound bagNbt = stack.getTagCompound();
        NBTHelperTarget targetData = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (targetData == null || targetData.blockName == null)
        {
            return stack;
        }

        // Access is allowed for everyone to a vanilla Ender Chest
        if (targetData.blockName.equals("minecraft:ender_chest") == true)
        {
            if (UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, true) == false)
            {
                return stack;
            }

            bagNbt.setBoolean("IsOpen", true);
            player.displayGUIChest(player.getInventoryEnderChest());
            return stack;
        }

        // For other targets, access is only allowed if the mode is set to public, or if the player is the owner
        if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return stack;
        }

        // Target block is not whitelisted, so it is known to not work unless within the client's loaded range
        if (isTargetBlockWhitelisted(targetData.blockName, targetData.blockMeta) == false && targetOutsideOfPlayerRange(stack, player) == true)
        {
            player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("enderutilities.chat.message.enderbag.outofrange")));
            return stack;
        }

        // The target block has changed since binding the bag, remove the bind (not for vanilla Ender Chests)
        if (targetData.isTargetBlockUnchanged() == false)
        {
            NBTHelperTarget.removeTargetTagFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            bagNbt.removeTag("ChunkLoadingRequired");
            bagNbt.removeTag("IsOpen");

            player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.bound.block.changed"));

            return stack;
        }

        // Check that we have sufficient charge left to use the bag.
        if (UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, false) == false)
        {
            return stack;
        }

        // Only open the GUI if the chunk loading succeeds. 60 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, targetData.dimension,
                targetData.pos.getX() >> 4, targetData.pos.getZ() >> 4, 60) == true)
        {
            MinecraftServer server = MinecraftServer.getServer();
            if (server == null)
            {
                return stack;
            }

            World targetWorld = server.worldServerForDimension(targetData.dimension);
            if (targetWorld == null)
            {
                return stack;
            }

            // Actually use the charge. This _shouldn't_ be able to fail due to the above simulation...
            if (UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, true) == false)
            {
                // Remove the chunk loading delay FIXME this doesn't take into account possible overlapping chunk loads...
                //ChunkLoading.getInstance().refreshChunkTimeout(targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 0, false);
                return stack;
            }

            bagNbt.setBoolean("ChunkLoadingRequired", true);
            bagNbt.setBoolean("IsOpen", true);

            float hx = (float)targetData.dPosX - targetData.pos.getX();
            float hy = (float)targetData.dPosY - targetData.pos.getY();
            float hz = (float)targetData.dPosZ - targetData.pos.getZ();

            IBlockState state = targetWorld.getBlockState(targetData.pos);
            Block block = state.getBlock();
            // Access is allowed in onPlayerOpenContainer(PlayerOpenContainerEvent event) in PlayerEventHandler
            block.onBlockActivated(targetWorld, targetData.pos, state, player, targetData.facing, hx, hy, hz);
        }

        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if (player.isSneaking() == true && te != null &&
            (te.getClass() == TileEntityEnderChest.class || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == true))
        {
            return super.onItemUse(stack, player, world, pos, side, hitX, hitY, hitZ);
        }

        return false;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Only allow the block/inventory type Link Crystals
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == false || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
        {
            return this.getMaxModules(containerStack, moduleType);
        }

        return 0;
    }

    public static boolean targetNeedsToBeLoadedOnClient(ItemStack stack)
    {
        NBTHelperTarget targetData = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (targetData == null || targetData.blockName == null)
        {
            return false;
        }

        // Player's location doesn't matter with Ender Chests
        if (targetData.blockName.equals("minecraft:ender_chest") == true
            || isTargetBlockWhitelisted(targetData.blockName, targetData.blockMeta) == true)
        {
            return false;
        }

        return true;
    }

    public static boolean targetOutsideOfPlayerRange(ItemStack stack, EntityPlayer player)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (target == null)
        {
            return true;
        }

        // We allow a max range of 64 blocks, to hopefully be on the safer side
        //return target.dimension != player.dimension || player.getDistanceSq(target.posX, target.posY, target.posZ) >= 4096.0d;

        WorldServer world = MinecraftServer.getServer().worldServerForDimension(target.dimension);
        if ((player instanceof EntityPlayerMP) == false ||
             world == null ||
             world.getPlayerManager().isPlayerWatchingChunk((EntityPlayerMP)player, target.pos.getX() >> 4, target.pos.getZ() >> 4) == false)
        {
            return true;
        }

        return false;
    }

    public static boolean isTargetBlockWhitelisted(String name, int meta)
    {
        List<String> list;

        // FIXME add metadata handling
        // Black list
        if (Configs.enderBagListType.equalsIgnoreCase("blacklist") == true)
        {
            list = Registry.getEnderbagBlacklist();
            if (list.contains(name) == true)
            {
                return false;
            }

            return true;
        }
        // White list
        else
        {
            list = Registry.getEnderbagWhitelist();
            if (list.contains(name) == true)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (target != null)
        {
            if ("minecraft:ender_chest".equals(target.blockName))
            {
                ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);
                String targetName = (targetStack != null && targetStack.getItem() != null ? targetStack.getDisplayName() : "");

                String textPre = EnumChatFormatting.DARK_GREEN.toString();
                String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.target") + ": " + textPre + targetName + rst);
                return;
            }
        }

        super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player)
    {
        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean isCurrent)
    {
        super.onUpdate(stack, world, player, slot, isCurrent);

        // Ugly workaround to get the bag closing tag update to sync to the client
        // For some reason it won't sync if set directly in the PlayerOpenContainerEvent
        if (stack != null && stack.getTagCompound() != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt.hasKey("IsOpenDummy") == true)
            {
                nbt.removeTag("IsOpenDummy");
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;
        ModelResourceLocation[] variants = new ModelResourceLocation[8];
        int i = 0;

        for (String strL : new String[] { "false", "true" })
        {
            for (String strM : new String[] { "ender_closed", "ender_open", "normal_closed", "normal_open" })
            {
                variants[i++] = new ModelResourceLocation(rl, String.format("locked=%s,mode=%s", strL, strM));
            }
        }

        return variants;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        NBTHelperPlayer playerData = NBTHelperPlayer.getPlayerDataFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        String locked = (playerData != null && playerData.isPublic == false) ? "locked=true" : "locked=false";
        String mode = (target != null && "minecraft:ender_chest".equals(target.blockName)) ? ",mode=ender" : ",mode=normal";
        String isOpen = (stack.getTagCompound() != null && stack.getTagCompound().getBoolean("IsOpen") == true) ? "_open" : "_closed";

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, locked + mode + isOpen);
    }
}
