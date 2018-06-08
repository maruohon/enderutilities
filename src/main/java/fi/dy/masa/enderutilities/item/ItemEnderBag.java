package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.BlackLists;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBag extends ItemLocationBoundModular implements IChunkLoadingItem, IKeyBound
{
    public static final int ENDER_CHARGE_COST = 200;

    public ItemEnderBag(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getTagCompound() == null)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        NBTTagCompound bagNbt = stack.getTagCompound();
        bagNbt.removeTag("IsOpen");

        TargetData targetData = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (targetData == null || targetData.blockName == null)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // Access is allowed for everyone to a vanilla Ender Chest
        if (targetData.blockName.equals("minecraft:ender_chest"))
        {
            if (UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, false) == false)
            {
                return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
            }

            if (world.isRemote == false)
            {
                player.displayGUIChest(player.getInventoryEnderChest());
            }

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        /* Disable everything except the Ender Chest mode for now, because PlayerOpenContainerEvent is gone
        // For other targets, access is only allowed if the mode is set to public, or if the player is the owner
        if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        if (world.isRemote)
        {
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        // Target block is not whitelisted, so it is known to not work unless within the client's loaded range
        if (isTargetBlockWhitelisted(targetData.blockName, targetData.blockMeta) == false && targetOutsideOfPlayerRange(stack, player))
        {
            player.addChatMessage(new TextComponentTranslation(I18n.format("enderutilities.chat.message.enderbag.outofrange")));
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // The target block has changed since binding the bag, remove the bind (not for vanilla Ender Chests)
        if (targetData.isTargetBlockUnchanged() == false)
        {
            TargetData.removeTargetTagFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            bagNbt.removeTag("ChunkLoadingRequired");
            bagNbt.removeTag("IsOpen");

            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.bound.block.changed"));

            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // Check that we have sufficient charge left to use the bag.
        if (UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, true) == false)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        // Only open the GUI if the chunk loading succeeds. 60 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, targetData.dimension,
                targetData.pos.getX() >> 4, targetData.pos.getZ() >> 4, 15))
        {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server == null)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }

            World targetWorld = server.worldServerForDimension(targetData.dimension);
            if (targetWorld == null)
            {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }

            // Actually use the charge. This _shouldn't_ be able to fail due to the above simulation...
            if (UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, false) == false)
            {
                // Remove the chunk loading delay FIXME this doesn't take into account possible overlapping chunk loads...
                //ChunkLoading.getInstance().refreshChunkTimeout(targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 0, false);
                return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
            }

            bagNbt.setBoolean("ChunkLoadingRequired", true);
            bagNbt.setBoolean("IsOpen", true);

            float hx = (float)targetData.dPosX - targetData.pos.getX();
            float hy = (float)targetData.dPosY - targetData.pos.getY();
            float hz = (float)targetData.dPosZ - targetData.pos.getZ();

            IBlockState state = targetWorld.getBlockState(targetData.pos);
            Block block = state.getBlock();
            // Access is allowed in onPlayerOpenContainer(PlayerOpenContainerEvent event) in PlayerEventHandler
            block.onBlockActivated(targetWorld, targetData.pos, state, player, EnumHand.MAIN_HAND, stack, targetData.facing, hx, hy, hz);

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }*/

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);

        if (player.isSneaking() && te != null && te.getClass() == TileEntityEnderChest.class)
        {
            return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
        }

        return EnumActionResult.PASS;
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 5;
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
            return 4;
        }

        return 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.isEmpty() || (moduleStack.getItem() instanceof IModule) == false)
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
        TargetData targetData = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (targetData == null || targetData.blockName == null)
        {
            return false;
        }

        // Player's location doesn't matter with Ender Chests
        if (targetData.blockName.equals("minecraft:ender_chest") || BlackLists.isBlockAllowedForEnderBag(targetData.blockName))
        {
            return false;
        }

        return true;
    }

    public static boolean targetOutsideOfPlayerRange(ItemStack stack, EntityPlayer player)
    {
        TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (target == null)
        {
            return true;
        }

        // We allow a max range of 64 blocks, to hopefully be on the safer side
        //return target.dimension != player.dimension || player.getDistanceSq(target.posX, target.posY, target.posZ) >= 4096.0d;

        WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(target.dimension);

        if ((player instanceof EntityPlayerMP) == false || world == null ||
             world.getPlayerChunkMap().isPlayerWatchingChunk((EntityPlayerMP) player, target.pos.getX() >> 4, target.pos.getZ() >> 4) == false)
        {
            return true;
        }

        return false;
    }

    @Override
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean verbose)
    {
        TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (target != null)
        {
            if ("minecraft:ender_chest".equals(target.blockName))
            {
                ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);
                String targetName = (targetStack.isEmpty() == false ? targetStack.getDisplayName() : "");

                String textPre = TextFormatting.DARK_GREEN.toString();
                String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
                list.add(I18n.format("enderutilities.tooltip.item.target") + ": " + textPre + targetName + rst);

                // Ender Capacitor charge, if one has been installed
                ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);

                if (capacitorStack.isEmpty() == false && capacitorStack.getItem() instanceof ItemEnderCapacitor)
                {
                    ((ItemEnderCapacitor) capacitorStack.getItem()).addTooltipLines(capacitorStack, player, list, verbose);
                }

                return;
            }
        }

        super.addTooltipLines(stack, player, list, verbose);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return false;
    }

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

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        TargetData target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        OwnerData playerData = OwnerData.getOwnerDataFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        String locked = (playerData != null && playerData.getIsPublic() == false) ? "locked=true" : "locked=false";
        String mode = (target != null && "minecraft:ender_chest".equals(target.blockName)) ? ",mode=ender" : ",mode=normal";
        String isOpen = (stack.getTagCompound() != null && stack.getTagCompound().getBoolean("IsOpen")) ? "_open" : "_closed";

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, locked + mode + isOpen);
    }
}
