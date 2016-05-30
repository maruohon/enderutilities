package fi.dy.masa.enderutilities.item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.event.tasks.TaskBuildersWand;
import fi.dy.masa.enderutilities.event.tasks.TaskStructureBuild;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.*;
import fi.dy.masa.enderutilities.util.EntityUtils.LeftRight;
import fi.dy.masa.enderutilities.util.TemplateManagerEU.FileInfo;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemBuildersWand extends ItemLocationBoundModular
{
    /** How much Ender Charge does placing each block cost */
    public static final int ENDER_CHARGE_COST = 2;
    /** Max number of stored block types */
    public static final int MAX_BLOCKS = 16;
    public static final String WRAPPER_TAG_NAME = "BuildersWand";
    public static final String TAG_NAME_MODE = "Mode";
    public static final String TAG_NAME_CONFIGS = "Configs";
    public static final String TAG_NAME_CONFIG_PRE = "Mode_";
    public static final String TAG_NAME_CORNERS = "Corners";
    public static final String TAG_NAME_DIMENSIONS = "Dim";
    public static final String TAG_NAME_BLOCKS = "Blocks";
    public static final String TAG_NAME_BLOCK_PRE = "Block_";
    public static final String TAG_NAME_BLOCK_SEL = "SelBlock";
    public static final String TAG_NAME_ALLOW_DIAGONALS ="Diag";
    public static final String TAG_NAME_TEMPLATES = "Templates";
    public static final String TAG_NAME_GHOST_BLOCKS = "Ghost";
    public static final int BLOCK_TYPE_TARGETED = -1;
    public static final int BLOCK_TYPE_ADJACENT = -2;
    public static final boolean POS_START = true;
    public static final boolean POS_END = false;

    protected Map<UUID, Long> lastLeftClick = new HashMap<UUID, Long>();

    public ItemBuildersWand()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_BUILDERS_WAND);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        BlockPosEU pos = this.getPosition(stack, POS_START);
        if (pos == null)
        {
            return super.onItemRightClick(stack, world, player, hand);
        }

        Mode mode = Mode.getMode(stack);

        if (world.isRemote == false)
        {
            if (PlayerTaskScheduler.getInstance().hasTask(player, TaskBuildersWand.class) == true)
            {
                PlayerTaskScheduler.getInstance().removeTask(player, TaskBuildersWand.class);
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
            else if (mode != Mode.CUBE && mode != Mode.WALLS && mode != Mode.COPY && mode != Mode.PASTE && mode != Mode.DELETE)
            {
                EnumActionResult result = this.useWand(stack, world, player, pos);
                return new ActionResult<ItemStack>(result, stack);
            }
        }

        if ((mode == Mode.CUBE || mode == Mode.WALLS || mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE) &&
            this.getPosition(stack, POS_END) != null)
        {
            player.setActiveHand(hand);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) == true || te.getClass() == TileEntityEnderChest.class))
        {
            return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
        }

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.CUBE || mode == Mode.WALLS || mode == Mode.COPY || mode == Mode.DELETE)
        {
            if (world.isRemote == false && player.isSneaking() == false)
            {
                this.setPosition(stack, new BlockPosEU(pos.offset(side), player.dimension, side), POS_END);
            }

            return EnumActionResult.SUCCESS;
        }

        // Don't allow targeting the top face of blocks while sneaking
        // This should make sneak building a platform a lot less annoying
        if (world.isRemote == false && (player.isSneaking() == false || side != EnumFacing.UP))
        {
            return this.useWand(stack, world, player, new BlockPosEU(pos, player.dimension, side));
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return slotChanged || oldStack.equals(newStack) == false;
    }

    public void onLeftClickBlock(EntityPlayer player, World world, ItemStack stack, BlockPos pos, int dimension, EnumFacing side)
    {
        if (world.isRemote == true)
        {
            return;
        }

        // Hack to work around the fact that when the NBT changes, the left click event will fire again the next tick,
        // so it would easily result in the state toggling multiple times per left click
        Long last = this.lastLeftClick.get(player.getUniqueID());
        if (last == null || (world.getTotalWorldTime() - last) >= 4)
        {
            if (player.isSneaking() == false)
            {
                this.setPosition(stack, new BlockPosEU(pos.offset(side), player.dimension, side), POS_START);
            }
            // Sneak + left click: Set the selected block type
            else
            {
                this.setSelectedFixedBlockType(stack, player, world, pos);
            }
        }

        this.lastLeftClick.put(player.getUniqueID(), world.getTotalWorldTime());
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase livingBase, int itemInUseCount)
    {
        if (world.isRemote == true || (livingBase instanceof EntityPlayer) == false)
        {
            return;
        }

        if (this.getMaxItemUseDuration(stack) - itemInUseCount >= 20)
        {
            EntityPlayer player = (EntityPlayer) livingBase;
            BlockPosEU pos = this.getPosition(stack, POS_START);
            if (pos != null)
            {
                this.useWand(stack, world, player, pos);
                player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.4f, 0.7f);
            }
        }
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
    {
        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String itemName = this.getBaseItemDisplayName(stack);
        if (stack.getTagCompound() == null)
        {
            return itemName;
        }

        Mode mode = Mode.getMode(stack);
        String preBT = TextFormatting.AQUA.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        if (itemName.length() >= 14)
        {
            itemName = EUStringUtils.getInitialsWithDots(itemName);
        }
        itemName = itemName + " " + preGreen + Mode.getMode(stack).getDisplayName() + rst;

        int sel = getSelectedBlockTypeIndex(stack);

        if (mode == Mode.COPY || mode == Mode.PASTE)
        {
            if (mode == Mode.PASTE)
            {
                EnumFacing facing = this.getTemplateFacing(stack);
                itemName = itemName + " rot: " + preGreen + this.getAreaFlipAxis(stack, facing) + rst;

                if (this.getReplaceExisting(stack) == true)
                {
                    itemName += " rep: " + preGreen + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    itemName += " rep: " + preRed + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
                }
            }

            itemName = itemName + " - " + I18n.translateToLocal("enderutilities.tooltip.item.area") +
                    ": " + preGreen + (sel + 1) + rst;

            return itemName;
        }

        if (this.getAreaFlipped(stack) == true)
        {
            String strFlip = this.getAreaFlipAxis(stack, EnumFacing.NORTH).toString();
            itemName = itemName + " flip: " + preGreen + strFlip + rst;
        }
        else
        {
            itemName = itemName + " flip: " + preRed + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
        }

        if (sel >= 0)
        {
            BlockInfo blockInfo = getSelectedFixedBlockType(stack);
            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(blockInfo.block, 1, blockInfo.itemMeta);
                if (blockStack != null && blockStack.getItem() != null)
                {
                    itemName = itemName + " - " + preGreen + blockStack.getDisplayName() + rst;
                }
            }

            itemName = itemName + " (" + (sel + 1) + "/" + MAX_BLOCKS + ")";
        }
        else
        {
            String str;
            if (sel == BLOCK_TYPE_TARGETED)
            {
                str = I18n.translateToLocal("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                str = I18n.translateToLocal("enderutilities.tooltip.item.blocktype.adjacent");
            }

            itemName = itemName + " - " + preBT + str + rst;
        }

        return itemName;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(I18n.translateToLocal("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        String pre = TextFormatting.DARK_GREEN.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        Mode mode = Mode.getMode(stack);
        list.add(I18n.translateToLocal("enderutilities.tooltip.item.mode") + ": " + pre + mode.getDisplayName() + rst);

        int sel = getSelectedBlockTypeIndex(stack);
        if (mode == Mode.COPY || mode == Mode.PASTE)
        {
            String str = I18n.translateToLocal("enderutilities.tooltip.item.selectedarea");
            list.add(str + ": " + preGreen + (sel + 1) + rst);
        }
        else if (sel >= 0)
        {
            BlockInfo blockInfo = getSelectedFixedBlockType(stack);
            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(blockInfo.block, 1, blockInfo.itemMeta);
                if (blockStack != null && blockStack.getItem() != null)
                {
                    String str = I18n.translateToLocal("enderutilities.tooltip.item.selectedblock");
                    list.add(str + ": " + pre + blockStack.getDisplayName() + rst);
                }
            }
        }
        else
        {
            String str = I18n.translateToLocal("enderutilities.tooltip.item.selectedblock");
            String str2;
            if (sel == BLOCK_TYPE_TARGETED)
            {
                str2 = I18n.translateToLocal("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                str2 = I18n.translateToLocal("enderutilities.tooltip.item.blocktype.adjacent");
            }

            list.add(str + ": " + pre + str2 + rst);
        }

        String str = I18n.translateToLocal("enderutilities.tooltip.item.area.flipped");
        String str2;
        if (mode == Mode.COPY) { }
        else if (mode == Mode.PASTE)
        {
            EnumFacing facing = this.getTemplateFacing(stack);
            str2 = I18n.translateToLocal("enderutilities.tooltip.item.rotation") + ": ";
            list.add(str2 + preGreen + this.getAreaFlipAxis(stack, facing).toString().toLowerCase() + rst);
        }
        else if (this.getAreaFlipped(stack) == true)
        {
            str2 = preGreen + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
            list.add(str + ": " + str2 + rst);

            str = I18n.translateToLocal("enderutilities.tooltip.item.flipaxis");
            String preBlue = TextFormatting.BLUE.toString();
            list.add(str + ": " + preBlue + this.getAreaFlipAxis(stack, EnumFacing.UP) + rst);
        }
        else
        {
            str2 = TextFormatting.RED + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
            list.add(str + ": " + str2 + rst);
        }

        if (mode == Mode.EXTEND_CONTINUOUS)
        {
            str = I18n.translateToLocal("enderutilities.tooltip.item.builderswand.allowdiagonals");

            if (NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS) == true)
            {
                str2 = preGreen + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
            }
            else
            {
                str2 = TextFormatting.RED + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
            }

            list.add(str + ": " + str2 + rst);
        }

        if (mode != Mode.COPY && mode != Mode.PASTE)
        {
            str = I18n.translateToLocal("enderutilities.tooltip.item.builderswand.renderghostblocks");

            if (NBTUtils.getBoolean(stack, ItemBuildersWand.WRAPPER_TAG_NAME, ItemBuildersWand.TAG_NAME_GHOST_BLOCKS) == true)
            {
                str2 = preGreen + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
            }
            else
            {
                str2 = TextFormatting.RED + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
            }

            list.add(str + ": " + str2 + rst);
        }

        super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
    }

    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public BlockPosEU getPosition(ItemStack stack, boolean isStart)
    {
        return this.getPosition(stack, Mode.getMode(stack), isStart);
    }

    public BlockPosEU getPosition(ItemStack stack, Mode mode, boolean isStart)
    {
        if (mode == Mode.COPY)
        {
            return this.getCopyModeAreaCorner(stack, isStart);
        }

        if (mode == Mode.PASTE && isStart == false)
        {
            return this.getPasteModeEndPosition(stack);
        }

        int modeId = mode.ordinal();
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + modeId, true);
        return BlockPosEU.readFromTag(tag.getCompoundTag(isStart == true ? "Pos1" : "Pos2"));
    }

    public void setPosition(ItemStack stack, BlockPosEU pos, boolean isStart)
    {
        Mode mode = Mode.getMode(stack);
        int modeId = mode.ordinal();
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + modeId, true);

        String tagName = isStart == true ? "Pos1" : "Pos2";
        if (tag.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == true)
        {
            BlockPosEU oldPos = BlockPosEU.readFromTag(tag.getCompoundTag(tagName));
            if (oldPos != null && oldPos.equals(pos) == true)
            {
                tag.removeTag(tagName);
            }
            else
            {
                tag.setTag(tagName, pos.writeToTag(new NBTTagCompound()));
            }
        }
        else
        {
            tag.setTag(tagName, pos.writeToTag(new NBTTagCompound()));
        }

        if (mode == Mode.COPY)
        {
            this.setCopyModeAreaCorner(stack, isStart, pos);
        }
    }

    public EnumActionResult useWand(ItemStack stack, World world, EntityPlayer player, BlockPosEU posTarget)
    {
        if (player.dimension != posTarget.dimension)
        {
            return EnumActionResult.FAIL;
        }

        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        BlockPosEU posStart = this.getPosition(stack, POS_START);
        BlockPosEU posEnd = this.getPosition(stack, POS_END);

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.CUBE)
        {
            this.getBlockPositionsCube(stack, world, positions, posStart, posEnd);
        }
        else if (mode == Mode.WALLS)
        {
            this.getBlockPositionsWalls(stack, world, positions, posStart, posEnd);
        }
        else if (mode == Mode.COPY)
        {
            this.copyAreaToTemplate(stack, world, player, posStart, posEnd);
            return EnumActionResult.SUCCESS;
        }
        else if (mode == Mode.PASTE)
        {
            this.pasteAreaIntoWorld(stack, world, player, posStart);
            return EnumActionResult.SUCCESS;
        }
        else if (mode == Mode.DELETE)
        {
            this.deleteArea(stack, world, player, posStart, posEnd);
            return EnumActionResult.SUCCESS;
        }
        else
        {
            this.getBlockPositions(stack, world, player, positions, posStart != null ? posStart : posTarget);
        }

        // Small enough area, build it all in one go without the task
        if (positions.size() <= 60)
        {
            for (int i = 0; i < positions.size(); i++)
            {
                placeBlockToPosition(stack, world, player, positions.get(i));
            }

            // Offset the start position by one after a build operation completes, but not for Walls and Cube modes
            BlockPosEU pos = this.getPosition(stack, POS_START);
            if (pos != null && mode != Mode.WALLS && mode != Mode.CUBE)
            {
                this.setPosition(stack, pos.offset(pos.side, 1), POS_START);
            }
        }
        else
        {
            TaskBuildersWand task = new TaskBuildersWand(world, player.getUniqueID(), positions, Configs.buildersWandBlocksPerTick);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    public static boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player, BlockPosStateDist posStateDist)
    {
        if (world.isAirBlock(posStateDist.toBlockPos()) == false ||
            (player.capabilities.isCreativeMode == false && UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, true) == false))
        {
            return false;
        }

        BlockInfo blockInfo;

        if (getSelectedBlockTypeIndex(wandStack) == BLOCK_TYPE_ADJACENT)
        {
            blockInfo = getBlockInfoForAdjacentBlock(world, posStateDist.toBlockPos(), posStateDist.side);
        }
        else
        {
            blockInfo = posStateDist.blockInfo;
        }

        if (blockInfo == null || blockInfo.block == Blocks.AIR)
        {
            return false;
        }

        return placeBlockToPosition(wandStack, world, player, posStateDist.toBlockPos(),
                posStateDist.side, blockInfo.block.getStateFromMeta(blockInfo.blockMeta));
    }

    public static boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player,
            BlockPos pos, EnumFacing side, IBlockState iBlockState)
    {
        return placeBlockToPosition(wandStack, world, player, pos, side, iBlockState, 3);
    }

    public static boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player,
            BlockPos pos, EnumFacing side, IBlockState iBlockState, int setBlockStateFlag)
    {
        if (world.isAirBlock(pos) == false ||
            (player.capabilities.isCreativeMode == false && UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, true) == false))
        {
            return false;
        }

        Block block = iBlockState.getBlock();

        if (player.capabilities.isCreativeMode == true)
        {
            world.setBlockState(pos, iBlockState, setBlockStateFlag);
            SoundType soundtype = block.getSoundType();
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            return true;
        }
        else
        {
            @SuppressWarnings("deprecation")
            ItemStack templateStack = block.getItem(world, pos, iBlockState);
            IItemHandler inv = getInventoryWithItems(wandStack, templateStack, player);
            ItemStack targetStack = getItemToBuildWith(inv, templateStack, 1);

            if (targetStack != null)
            {
                // Check if we can place the block
                if (BlockUtils.checkCanPlaceBlockAt(world, pos, side, block, targetStack) == true)
                {
                    world.setBlockState(pos, iBlockState, setBlockStateFlag);

                    SoundType soundtype = block.getSoundType();
                    world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                    UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, false);
                    return true;
                }
            }
        }

        return false;
    }

    public static IItemHandler getInventoryWithItems(ItemStack wandStack, ItemStack templateStack, EntityPlayer player)
    {
        IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        int slot = InventoryUtils.getSlotOfFirstMatchingItemStack(inv, templateStack);
        if (slot != -1)
        {
            return inv;
        }

        inv = UtilItemModular.getBoundInventory(wandStack, player, 30);
        if (inv != null)
        {
            slot = InventoryUtils.getSlotOfFirstMatchingItemStack(inv, templateStack);
            if (slot != -1)
            {
                return inv;
            }
        }

        return null;
    }

    public static ItemStack getItemToBuildWith(IItemHandler inv, ItemStack templateStack, int amount)
    {
        if (inv != null)
        {
            int slot = InventoryUtils.getSlotOfFirstMatchingItemStack(inv, templateStack);
            if (slot != -1)
            {
                return inv.extractItem(slot, amount, false);
            }
        }

        return null;
    }

    public void setSelectedFixedBlockType(ItemStack stack, EntityPlayer player, World world, BlockPos pos)
    {
        int sel = getSelectedBlockTypeIndex(stack);
        if (sel < 0)
        {
            return;
        }

        NBTTagCompound blocksTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCKS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(blocksTag, TAG_NAME_BLOCK_PRE + sel, true);

        IBlockState state = world.getBlockState(pos);
        tag.setString("BlockName", ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());
        tag.setByte("BlockMeta", (byte)state.getBlock().getMetaFromState(state));

        ItemStack stackTmp = state.getBlock().getPickBlock(state, EntityUtils.getRayTraceFromPlayer(world, player, false), world, pos, player);
        int itemMeta = stackTmp != null ? stackTmp.getMetadata() : 0;

        tag.setShort("ItemMeta", (short)itemMeta);
    }

    public static BlockInfo getSelectedFixedBlockType(ItemStack stack)
    {
        int sel = getSelectedBlockTypeIndex(stack);
        if (sel < 0)
        {
            return null;
        }

        NBTTagCompound blocksTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCKS, false);
        NBTTagCompound tag = NBTUtils.getCompoundTag(blocksTag, TAG_NAME_BLOCK_PRE + sel, false);

        if (tag != null && tag.hasKey("BlockName", Constants.NBT.TAG_STRING) == true)
        {
            return new BlockInfo(new ResourceLocation(tag.getString("BlockName")), tag.getByte("BlockMeta"), tag.getShort("ItemMeta"));
        }

        return null;
    }

    public static int getSelectedBlockTypeIndex(ItemStack stack)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode, true);

        return tag.getByte(TAG_NAME_BLOCK_SEL);
    }

    public void changeSelectedBlockType(ItemStack stack, boolean reverse)
    {
        Mode mode = Mode.getMode(stack);
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode.ordinal(), true);

        int min = mode == Mode.COPY || mode == Mode.PASTE ? 0 : -2;
        NBTUtils.cycleByteValue(tag, TAG_NAME_BLOCK_SEL, min, MAX_BLOCKS - 1, reverse);
    }

    public boolean getAreaFlipped(ItemStack stack)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + mode, true);

        return tag.getBoolean("Flip");
    }

    public void toggleAreaFlipped(ItemStack stack, EntityPlayer player)
    {
        Mode mode = Mode.getMode(stack);
        int modeId = mode.ordinal();
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + modeId, true);
        EnumFacing facing = EntityUtils.getClosestLookingDirection(player);
        tag.setByte("FlipAxis", (byte)facing.getIndex());
        tag.setBoolean("Flip", ! tag.getBoolean("Flip"));
    }

    public EnumFacing getAreaFlipAxis(ItemStack stack, EnumFacing defaultFlipAxis)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + mode, true);

        if (tag.hasKey("FlipAxis", Constants.NBT.TAG_BYTE) == true)
        {
            return EnumFacing.getFront(tag.getByte("FlipAxis"));
        }

        return defaultFlipAxis;
    }

    public EnumFacing getAxisRight(ItemStack stack, BlockPosEU pos)
    {
        EnumFacing face = pos.side;
        EnumFacing axisRight = BlockPosEU.getRotation(face, EnumFacing.DOWN);

        if (face == EnumFacing.UP)
        {
            axisRight = BlockPosEU.getRotation(face, EnumFacing.SOUTH);
        }
        // FIXME wtf? both are south?
        else if (face == EnumFacing.DOWN)
        {
            axisRight = BlockPosEU.getRotation(face, EnumFacing.SOUTH);
        }

        if (this.getAreaFlipped(stack) == true)
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, face);
            axisRight = BlockPosEU.getRotation(axisRight, flipAxis);
        }

        return axisRight;
    }

    public EnumFacing getAxisUp(ItemStack stack, BlockPosEU pos)
    {
        EnumFacing face = pos.side;
        EnumFacing axisRight = BlockPosEU.getRotation(face, EnumFacing.DOWN);
        EnumFacing axisUp = BlockPosEU.getRotation(face, axisRight);

        if (face == EnumFacing.UP)
        {
            axisRight = BlockPosEU.getRotation(face, EnumFacing.SOUTH);
            axisUp = BlockPosEU.getRotation(face, axisRight);
        }
        // FIXME wtf? both are south?
        else if (face == EnumFacing.DOWN)
        {
            axisRight = BlockPosEU.getRotation(face, EnumFacing.SOUTH);
            axisUp = BlockPosEU.getRotation(face, axisRight);
        }

        if (this.getAreaFlipped(stack) == true)
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, face);
            axisUp = BlockPosEU.getRotation(axisUp, flipAxis);
        }

        return axisUp;
    }

    public void changeAreaDimensions(EntityPlayer player, ItemStack stack, boolean reverse)
    {
        BlockPosEU pos = this.getPosition(stack, POS_START);
        Mode mode = Mode.getMode(stack);
        if (pos == null || mode == Mode.WALLS || mode == Mode.CUBE)
        {
            return;
        }

        int amount = reverse == true ? 1 : -1;
        Area area = new Area(stack);

        // Only one dimension is used for the column mode
        if (mode == Mode.COLUMN)
        {
            area.adjustFromPlanarizedFacing(EnumFacing.EAST, amount, EnumFacing.UP, EnumFacing.EAST);
            area.writeToNBT(stack);
            return;
        }

        EnumFacing faceAxis = pos.side;
        EnumFacing axisRight = this.getAxisRight(stack, pos);
        EnumFacing axisUp = this.getAxisUp(stack, pos);

        boolean isFlipped = this.getAreaFlipped(stack);
        EnumFacing flipAxis = this.getAreaFlipAxis(stack, faceAxis);
        EnumFacing faceAxisFlipped = isFlipped == true ? BlockPosEU.getRotation(faceAxis, flipAxis) : faceAxis;

        EnumFacing lookDir = EnumFacing.NORTH;

        // Horizontal looking direction only
        if (faceAxisFlipped == EnumFacing.UP || faceAxisFlipped == EnumFacing.DOWN)
        {
            lookDir = EntityUtils.getHorizontalLookingDirection(player);
        }
        else
        {
            lookDir = EntityUtils.getClosestLookingDirection(player);

            if (Math.abs(player.rotationPitch) > 20.0f &&
                (lookDir.getAxis() == faceAxisFlipped.getAxis() || lookDir.getAxis() == EnumFacing.Axis.Y))
            {
                lookDir = EntityUtils.getVerticalLookingDirection(player);
            }
            else
            {
                LeftRight leftRight = EntityUtils.getLookLeftRight(player, faceAxisFlipped);
                lookDir = leftRight == LeftRight.RIGHT ?
                    BlockPosEU.getRotation(faceAxisFlipped, EnumFacing.DOWN) :
                    BlockPosEU.getRotation(faceAxisFlipped, EnumFacing.UP);
            }
        }

        /*List<ForgeDirection> rotations = EntityUtils.getTransformationsToMatchPlanes(lookDirUp, lookDirRight, axisUp, axisRight);
        for (ForgeDirection rot : rotations)
        {
            lookDir = lookDir.getRotation(rot);
        }*/

        //System.out.printf("face: %s flippedFace: %s flipAxis: %s look: %s up: %s right: %s\n", faceAxis, (isFlipped ? faceAxisFlipped : "none"), flipAxis, lookDir, axisUp, axisRight);
        area.adjustFromPlanarizedFacing(lookDir, amount, axisUp, axisRight);

        area.writeToNBT(stack);
    }

    public void addAdjacent(EntityPlayer player, World world, BlockPosEU center, Area area, int posV, int posH, List<BlockPosStateDist> positions,
             int blockType, boolean diagonals, BlockInfo blockInfo, EnumFacing axisRight, EnumFacing axisUp)
    {
        if (posH < -area.rNegH || posH > area.rPosH || posV < -area.rNegV || posV > area.rPosV)
        {
            return;
        }

        //System.out.printf("addAdjacent(): posV: %d posH: %d blockInfo: %s\n", posV, posH, blockInfo != null ? blockInfo.blockName : "null");
        int x = center.posX + posH * axisRight.getFrontOffsetX() + posV * axisUp.getFrontOffsetX();
        int y = center.posY + posH * axisRight.getFrontOffsetY() + posV * axisUp.getFrontOffsetY();
        int z = center.posZ + posH * axisRight.getFrontOffsetZ() + posV * axisUp.getFrontOffsetZ();

        // The location itself must be air
        if (world.isAirBlock(new BlockPos(x, y, z)) == false)
        {
            return;
        }

        int xb = x - center.side.getFrontOffsetX();
        int yb = y - center.side.getFrontOffsetY();
        int zb = z - center.side.getFrontOffsetZ();

        BlockPos blockPos = new BlockPos(xb, yb, zb);
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        int blockMeta = block.getMetaFromState(state);

        ItemStack stackTmp = state.getBlock().getPickBlock(state, EntityUtils.getRayTraceFromPlayer(world, player, false), world, blockPos, player);
        int itemMeta = stackTmp != null ? stackTmp.getMetadata() : 0;

        // The block on the back face must not be air or fluid ...
        if (block.isAir(state, world, blockPos) == true || block.getMaterial(state).isLiquid() == true)
        {
            return;
        }

        // The block on the back face must not be air and also it must not be fluid.
        // If the block type to work with is BLOCK_TYPE_TARGETED, then the block adjacent
        // to his position must match the targeted block.

        //if (blockType >= 0 || blockType == BLOCK_TYPE_TARGETED || blockInfo == null || (blockInfo.block == block && blockInfo.meta == meta))
        if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && blockInfo != null) ||
           (blockInfo != null && blockInfo.block == block && blockInfo.blockMeta == blockMeta))
        {
            if (blockType == BLOCK_TYPE_ADJACENT)
            {
                blockInfo = new BlockInfo(block, blockMeta, itemMeta);
            }

            BlockPosStateDist pos = new BlockPosStateDist(new BlockPos(x, y, z), 0, center.side, blockInfo);

            if (positions.contains(pos) == false)
            {
                positions.add(pos);

                // Adjacent blocks
                this.addAdjacent(player, world, center, area, posV - 1, posH + 0, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                this.addAdjacent(player, world, center, area, posV + 0, posH - 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                this.addAdjacent(player, world, center, area, posV + 0, posH + 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                this.addAdjacent(player, world, center, area, posV + 1, posH + 0, positions, blockType, diagonals, blockInfo, axisRight, axisUp);

                // Diagonals/corners
                if (diagonals == true)
                {
                    this.addAdjacent(player, world, center, area, posV - 1, posH - 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                    this.addAdjacent(player, world, center, area, posV - 1, posH + 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                    this.addAdjacent(player, world, center, area, posV + 1, posH - 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                    this.addAdjacent(player, world, center, area, posV + 1, posH + 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                }
            }
        }
    }

    public static BlockInfo getBlockInfoForAdjacentBlock(World world, BlockPos pos, EnumFacing side)
    {
        return new BlockInfo(world, pos.offset(side, -1));
    }

    public BlockInfo getBlockInfoForTargeted(ItemStack stack, World world, BlockPos pos)
    {
        int blockType = getSelectedBlockTypeIndex(stack);
        if (blockType == BLOCK_TYPE_TARGETED || blockType == BLOCK_TYPE_ADJACENT)
        {
            return new BlockInfo(world, pos);
        }
        // Pre-determined bound block type
        else if (blockType >= 0)
        {
            return getSelectedFixedBlockType(stack);
        }

        return null;
    }

    public BlockInfo getBlockInfoForBlockType(World world, BlockPos pos, EnumFacing side, int blockType, BlockInfo biTarget, BlockInfo biBound)
    {
        if (blockType == BLOCK_TYPE_TARGETED)
        {
            return biTarget;
        }

        if (blockType == BLOCK_TYPE_ADJACENT)
        {
            return getBlockInfoForAdjacentBlock(world, pos, side);
        }

        // If using a fixed block type, then we require a valid block
        if (blockType >= 0)
        {
            return biBound;
        }

        return null;
    }

    /**
     * Get the actual block positions and block types for all other modes except Walls and Cube.
     */
    public void getBlockPositions(ItemStack stack, World world, EntityPlayer player, List<BlockPosStateDist> positions, BlockPosEU center)
    {
        EnumFacing side = center.side;
        EnumFacing axisRight = BlockPosEU.getRotation(side, EnumFacing.DOWN);
        EnumFacing axisUp = BlockPosEU.getRotation(side, axisRight);

        if (side == EnumFacing.UP)
        {
            axisRight = BlockPosEU.getRotation(side, EnumFacing.SOUTH);
            axisUp = BlockPosEU.getRotation(side, axisRight);
        }
        else if (side == EnumFacing.DOWN)
        {
            axisRight = BlockPosEU.getRotation(side, EnumFacing.SOUTH);
            axisUp = BlockPosEU.getRotation(side, axisRight);
        }

        if (this.getAreaFlipped(stack) == true)
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, side);
            axisRight = BlockPosEU.getRotation(axisRight, flipAxis);
            axisUp = BlockPosEU.getRotation(axisUp, flipAxis);
            //System.out.printf("flipAxis: %s axisRight: %s axisUp: %s\n", flipAxis, axisRight, axisUp);
        }

        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, center.offset(side, -1).toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = getSelectedBlockTypeIndex(stack);
        Area area = new Area(stack);
        int dim = world.provider.getDimension();

        Mode mode = Mode.getMode(stack);
        switch(mode)
        {
            case COLUMN:
                for (int i = 0; i <= area.rPosH; i++)
                {
                    BlockPosEU posTmp = center.offset(side, i);
                    if (world.isAirBlock(posTmp.toBlockPos()) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, biTarget));
                    }
                }
                break;

            case LINE:
                for (int i = -area.rNegH; i <= area.rPosH; i++)
                {
                    BlockPos posTmp = center.offset(axisRight, i).toBlockPos();
                    if (world.isAirBlock(posTmp) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, side,
                                        this.getBlockInfoForBlockType(world, posTmp, side, blockType, biTarget, biBound)));
                    }
                }
                break;

            case PLANE:
                for (int v = -area.rNegV; v <= area.rPosV; v++)
                {
                    for (int h = -area.rNegH; h <= area.rPosH; h++)
                    {
                        BlockPos posTmp = center.offset(axisRight, h).offset(axisUp, v).toBlockPos();
                        if (world.isAirBlock(posTmp) == true)
                        {
                            positions.add(new BlockPosStateDist(posTmp, dim, side,
                                            this.getBlockInfoForBlockType(world, posTmp, side, blockType, biTarget, biBound)));
                        }
                    }
                }
                break;

            case EXTEND_CONTINUOUS:
                boolean diagonals = NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
                this.addAdjacent(player, world, center, area, 0, 0, positions, blockType, diagonals, biTarget, axisRight, axisUp);
                break;

            case EXTEND_AREA:
                for (int v = -area.rNegV; v <= area.rPosV; v++)
                {
                    for (int h = -area.rNegH; h <= area.rPosH; h++)
                    {
                        BlockPos posTmp = center.offset(axisRight, h).offset(axisUp, v).toBlockPos();

                        // The target position must be air
                        if (world.isAirBlock(posTmp) == true)
                        {
                            BlockPos posTgt = posTmp.offset(side, -1);

                            IBlockState state = world.getBlockState(posTgt);
                            Block block = state.getBlock();
                            int meta = block.getMetaFromState(state);

                            // The block on the back face must not be air and also it must not be fluid.
                            // If the block type to work with is BLOCK_TYPE_TARGETED, then the block adjacent
                            // to his position must match the targeted block.
                            if (block.isAir(state, world, posTgt) == false && block.getMaterial(state).isLiquid() == false)
                            {
                                if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && biBound != null) ||
                                   (biTarget != null && biTarget.block == block && biTarget.blockMeta == meta))
                                {
                                    positions.add(new BlockPosStateDist(posTmp, dim, side,
                                                    this.getBlockInfoForBlockType(world, posTmp, side, blockType, biTarget, biBound)));
                                }
                            }
                        }
                    }
                }
                break;

            default:
        }
    }

    public void getBlockPositionsWalls(ItemStack stack, World world, List<BlockPosStateDist> positions, BlockPosEU pos1, BlockPosEU pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return;
        }

        int startX = Math.min(pos1.posX, pos2.posX);
        int startY = Math.min(pos1.posY, pos2.posY);
        int startZ = Math.min(pos1.posZ, pos2.posZ);

        int endX = Math.max(pos1.posX, pos2.posX);
        int endY = Math.max(pos1.posY, pos2.posY);
        int endZ = Math.max(pos1.posZ, pos2.posZ);

        if (endX - startX > 128 || endY - startY > 128 || endZ - startZ > 128)
        {
            return;
        }

        BlockPosEU targeted = pos1.offset(pos1.side, -1);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted.toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = getSelectedBlockTypeIndex(stack);
        int dim = world.provider.getDimension();

        for (int x = startX; x <= endX; x++)
        {
            for (int y = startY; y <= endY; y++)
            {
                positions.add(new BlockPosStateDist(x, y, startZ, dim, targeted.face,
                                this.getBlockInfoForBlockType(world, new BlockPos(x, y, startZ), targeted.side, blockType, biTarget, biBound)));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int y = startY; y <= endY; y++)
            {
                positions.add(new BlockPosStateDist(x, y, endZ, dim, targeted.face,
                        this.getBlockInfoForBlockType(world, new BlockPos(x, y, endZ), targeted.side, blockType, biTarget, biBound)));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                positions.add(new BlockPosStateDist(x, startY, z, dim, targeted.face,
                                this.getBlockInfoForBlockType(world, new BlockPos(x, startY, z), targeted.side, blockType, biTarget, biBound)));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                positions.add(new BlockPosStateDist(x, endY, z, dim, targeted.face,
                                this.getBlockInfoForBlockType(world, new BlockPos(x, endY, z), targeted.side, blockType, biTarget, biBound)));
            }
        }

        for (int z = startZ + 1; z <= endZ - 1; z++)
        {
            for (int y = startY + 1; y <= endY - 1; y++)
            {
                positions.add(new BlockPosStateDist(startX, y, z, dim, targeted.face,
                                this.getBlockInfoForBlockType(world, new BlockPos(startX, y, z), targeted.side, blockType, biTarget, biBound)));
            }
        }

        for (int z = startZ + 1; z <= endZ - 1; z++)
        {
            for (int y = startY + 1; y <= endY - 1; y++)
            {
                positions.add(new BlockPosStateDist(endX, y, z, dim, targeted.face,
                                this.getBlockInfoForBlockType(world, new BlockPos(endX, y, z), targeted.side, blockType, biTarget, biBound)));
            }
        }
    }

    public void getBlockPositionsCube(ItemStack stack, World world, List<BlockPosStateDist> positions, BlockPosEU pos1, BlockPosEU pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return;
        }

        int startX = Math.min(pos1.posX, pos2.posX);
        int startY = Math.min(pos1.posY, pos2.posY);
        int startZ = Math.min(pos1.posZ, pos2.posZ);

        int endX = Math.max(pos1.posX, pos2.posX);
        int endY = Math.max(pos1.posY, pos2.posY);
        int endZ = Math.max(pos1.posZ, pos2.posZ);

        if (endX - startX > 128 || endY - startY > 128 || endZ - startZ > 128)
        {
            return;
        }

        BlockPosEU targeted = pos1.offset(pos1.side, -1);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted.toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = getSelectedBlockTypeIndex(stack);
        int dim = world.provider.getDimension();

        for (int y = startY; y <= endY; y++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                for (int x = startX; x <= endX; x++)
                {
                    positions.add(new BlockPosStateDist(x, y, z, dim, pos1.face,
                                    this.getBlockInfoForBlockType(world, new BlockPos(x, y, z), targeted.side, blockType, biTarget, biBound)));
                }
            }
        }
    }

    private void copyAreaToTemplate(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn, BlockPosEU posEndIn)
    {
        if (Configs.buildersWandEnableCopyPaste == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabled"));
            return;
        }

        if (posStartIn == null || posEndIn == null)
        {
            return;
        }

        BlockPos posStart = posStartIn.toBlockPos();
        BlockPos endOffset = posEndIn.toBlockPos().subtract(posStart);
        //System.out.printf("posStart: %s posEnd: %s endOffset: %s\n", posStart, posEndIn.toBlockPos(), endOffset);

        if (this.isAreaWithinSizeLimit(endOffset, player) == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge", this.getMaxAreaDimension(player)));
            return;
        }

        MinecraftServer server = world.getMinecraftServer();
        ResourceLocation rl = this.getTemplateResource(stack);
        TemplateManagerEU templateManager = this.getTemplateManager();

        TemplateEnderUtilities template = templateManager.getTemplate(server, rl);
        template.takeBlocksFromWorld(world, posStart, endOffset, true);
        template.setAuthor(player.getName());
        templateManager.writeTemplate(server, rl);

        TemplateMetadata templateMeta = templateManager.getTemplateMetadata(server, rl);
        EnumFacing facing = this.getFacingFromPositions(posStart, posStart.add(endOffset));
        templateMeta.setValues(endOffset, facing, player.getName());
        templateManager.writeTemplateMetadata(server, rl);

        player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areasavedtotemplate", (getSelectedBlockTypeIndex(stack) + 1)));
    }

    private void pasteAreaIntoWorld(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn)
    {
        if (Configs.buildersWandEnableCopyPaste == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabled"));
            return;
        }

        if (posStartIn == null || posStartIn.dimension != player.dimension ||
            player.getDistanceSq(posStartIn.toBlockPos()) > 16384)
        {
            return;
        }

        TemplateMetadata templateMeta = this.getTemplateMetadata(world, stack);

        if (this.isAreaWithinSizeLimit(templateMeta.getRelativeEndPosition(), player) == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge", this.getMaxAreaDimension(player)));
            return;
        }

        PlacementSettings placement = this.getPasteModePlacement(stack, player);
        TemplateEnderUtilities template = this.getTemplate(world, stack, placement);
        //System.out.printf("pasting - posStartIn: %s size: %s rotation: %s\n", posStartIn, template.getTemplateSize(), placement.getRotation());

        if (player.capabilities.isCreativeMode == true)
        {
            template.setReplaceExistingBlocks(this.getReplaceExisting(stack));
            template.addBlocksToWorld(world, posStartIn.toBlockPos());
        }
        else
        {
            TaskStructureBuild task = new TaskStructureBuild(template, posStartIn.toBlockPos(), player.dimension,
                    player.getUniqueID(), Configs.buildersWandBlocksPerTick, false, false);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }
    }

    private void deleteArea(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn, BlockPosEU posEndIn)
    {
        if (player.capabilities.isCreativeMode == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.creativeonly"));
            return;
        }

        if (posStartIn == null || posEndIn == null || posStartIn.dimension != player.dimension || posEndIn.dimension != player.dimension)
        {
            return;
        }

        BlockPos posStart = posStartIn.toBlockPos();
        BlockPos posEnd = posEndIn.toBlockPos();

        if (player.getDistanceSq(posStart) >= 16384 || player.getDistanceSq(posEnd) >= 16384 ||
            this.isAreaWithinSizeLimit(posStart.subtract(posEnd), player) == false)
        {
            return;
        }

        for (BlockPos.MutableBlockPos posMutable : BlockPos.getAllInBoxMutable(posStart, posEnd))
        {
            world.setBlockToAir(posMutable);
        }
    }

    public void placeHelperBlock(EntityPlayer player)
    {
        BlockPos pos = PositionUtils.getPositionInfrontOfEntity(player);
        player.worldObj.setBlockState(pos, Blocks.RED_MUSHROOM_BLOCK.getDefaultState(), 3);
    }

    private int getMaxAreaDimension(EntityPlayer player)
    {
        return player.capabilities.isCreativeMode ? 128 : 64;
    }

    private boolean isAreaWithinSizeLimit(BlockPos size, EntityPlayer player)
    {
        int limit = this.getMaxAreaDimension(player);
        return Math.abs(size.getX()) <= limit && Math.abs(size.getY()) <= limit && Math.abs(size.getZ()) <= limit;
    }

    private TemplateEnderUtilities getTemplate(World world, ItemStack stack, PlacementSettings placement)
    {
        MinecraftServer server = world.getMinecraftServer();
        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack);
        TemplateEnderUtilities template = templateManager.getTemplate(server, rl);
        template.setPlacementSettings(placement);

        return template;
    }

    private TemplateMetadata getTemplateMetadata(World world, ItemStack stack)
    {
        MinecraftServer server = world.getMinecraftServer();
        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack);
        TemplateMetadata templateMeta = templateManager.getTemplateMetadata(server, rl);

        return templateMeta;
    }

    private ResourceLocation getTemplateResource(ItemStack stack)
    {
        int id = getSelectedBlockTypeIndex(stack);
        UUID uuid = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
        return new ResourceLocation(Reference.MOD_ID, uuid.toString() + "_" + id);
    }

    private TemplateManagerEU getTemplateManager()
    {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();
        if (saveDir == null)
        {
            return null;
        }

        File file = new File(new File(saveDir, Reference.MOD_ID), this.name);
        return new TemplateManagerEU(file.getPath());
    }

    private BlockPosEU getPasteModeEndPosition(ItemStack stack)
    {
        BlockPosEU posStartEU = this.getPosition(stack, Mode.PASTE, true);
        if (posStartEU == null)
        {
            return null;
        }

        NBTTagCompound tag = this.getPasteModeSelectedTemplateTag(stack, false);
        if (tag == null)
        {
            return null;
        }

        BlockPosEU endOffset = new BlockPosEU(tag.getInteger("endOffsetX"), tag.getInteger("endOffsetY"), tag.getInteger("endOffsetZ"));
        PlacementSettings placement = this.getPasteModePlacement(stack, null);
        endOffset = PositionUtils.getTransformedBlockPos(endOffset, placement.getMirror(), placement.getRotation());

        return posStartEU.add(endOffset);
    }

    private NBTTagCompound getPasteModeSelectedTemplateTag(ItemStack stack, boolean create)
    {
        int sel = getSelectedBlockTypeIndex(stack);
        int modeId = Mode.PASTE.ordinal();
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CONFIG_PRE + modeId, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_TEMPLATES + "_" + sel, create);

        return tag;
    }

    private void updateTemplateMetadata(ItemStack stack)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        ResourceLocation rl = this.getTemplateResource(stack);
        TemplateManagerEU templateManager = this.getTemplateManager();
        FileInfo info = templateManager.getTemplateInfo(server, rl);
        NBTTagCompound tag = this.getPasteModeSelectedTemplateTag(stack, true);

        if (tag.getLong("Timestamp") != info.timestamp || tag.getLong("FileSize") != info.fileSize)
        {
            TemplateMetadata meta = templateManager.getTemplateMetadata(server, rl);
            BlockPos size = meta.getRelativeEndPosition();
            tag.setLong("TimeStamp", info.timestamp);
            tag.setLong("FileSize", info.fileSize);
            tag.setInteger("endOffsetX", size.getX());
            tag.setInteger("endOffsetY", size.getY());
            tag.setInteger("endOffsetZ", size.getZ());
            tag.setByte("TemplateFacing", (byte)meta.getFacing().getIndex());
        }
    }

    private BlockPosEU getCopyModeAreaCorner(ItemStack stack, boolean isStart)
    {
        int sel = getSelectedBlockTypeIndex(stack);
        int modeId = Mode.COPY.ordinal();
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CONFIG_PRE + modeId, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CORNERS, true);
        return BlockPosEU.readFromTag(tag.getCompoundTag((isStart == true ? "start" : "end") + "_" + sel));
    }

    private void setCopyModeAreaCorner(ItemStack stack, boolean isStart, BlockPosEU pos)
    {
        int sel = getSelectedBlockTypeIndex(stack);
        int modeId = Mode.COPY.ordinal();
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CONFIG_PRE + modeId, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CORNERS, true);
        tag = NBTUtils.getCompoundTag(tag, (isStart == true ? "start" : "end") + "_" + sel, true);
        pos.writeToTag(tag);
    }

    private EnumFacing getTemplateFacing(ItemStack stack)
    {
        int sel = getSelectedBlockTypeIndex(stack);
        int modeId = Mode.PASTE.ordinal();
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CONFIG_PRE + modeId, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_TEMPLATES + "_" + sel, true);
        return EnumFacing.getFront(tag.getByte("TemplateFacing"));
    }

    private void toggleReplaceExisting(ItemStack stack)
    {
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CONFIG_PRE + Mode.PASTE.ordinal(), true);
        tag.setBoolean("Replace", ! tag.getBoolean("Replace"));
    }

    private boolean getReplaceExisting(ItemStack stack)
    {
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CONFIG_PRE + Mode.PASTE.ordinal(), true);
        return tag.getBoolean("Replace");
    }

    private PlacementSettings getPasteModePlacement(ItemStack stack, EntityPlayer player)
    {
        EnumFacing facing = this.getTemplateFacing(stack);
        Rotation rotation = PositionUtils.getRotation(facing, this.getAreaFlipAxis(stack, facing));
        boolean ignoreEntities = player == null || player.capabilities.isCreativeMode == false;
        //System.out.printf("getPasteModePlacement - facingOrig: %s, rot: %s\n", facing, rotation);

        return new PlacementSettings(Mirror.NONE, rotation, ignoreEntities, Blocks.BARRIER, null);
    }

    private EnumFacing getFacingFromPositions(BlockPos pos1, BlockPos pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return null;
        }

        if (pos2.getX() == pos1.getX())
        {
            return pos2.getZ() > pos1.getZ() ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }

        if (pos2.getZ() == pos1.getZ())
        {
            return pos2.getX() > pos1.getX() ? EnumFacing.EAST : EnumFacing.WEST;
        }

        if (pos2.getX() > pos1.getX())
        {
            return pos2.getZ() > pos1.getZ() ? EnumFacing.EAST : EnumFacing.NORTH;
        }

        return pos2.getZ() > pos1.getZ() ? EnumFacing.SOUTH : EnumFacing.WEST;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null)
        {
            return;
        }

        if (key == ReferenceKeys.KEYCODE_CUSTOM_1)
        {
            this.placeHelperBlock(player);
            return;
        }

        if (ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Alt + Toggle key: Change the selected block type
        if (ReferenceKeys.keypressContainsControl(key) == false &&
            ReferenceKeys.keypressContainsShift(key) == false &&
            ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.changeSelectedBlockType(stack, ReferenceKeys.keypressActionIsReversed(key));
            if (Mode.getMode(stack) == Mode.PASTE)
            {
                this.updateTemplateMetadata(stack);
            }
        }
        // Shift + Toggle Mode: Change the dimensions of the current mode
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeAreaDimensions(player, stack, ReferenceKeys.keypressActionIsReversed(key));
        }
        // Ctrl + Toggle key: Cycle the mode
        else if (ReferenceKeys.keypressContainsControl(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            Mode.cycleMode(stack, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
            if (Mode.getMode(stack) == Mode.PASTE)
            {
                this.updateTemplateMetadata(stack);
            }
        }
        // Ctrl + Alt + Shift + Toggle key: Change the selected link crystal
        else if (ReferenceKeys.keypressContainsControl(key) == true &&
                 ReferenceKeys.keypressContainsShift(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressActionIsReversed(key));
        }
        // Ctrl + Alt + Toggle key: Toggle "allow diagonals" in Extend Continuous mode
        else if (ReferenceKeys.keypressContainsControl(key) == true &&
                 ReferenceKeys.keypressContainsShift(key) == false &&
                 ReferenceKeys.keypressContainsAlt(key) == true)
        {
            if (Mode.getMode(stack) == Mode.PASTE)
            {
                this.toggleReplaceExisting(stack);
            }
            else
            {
                NBTUtils.toggleBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
            }
        }
        // Alt + Shift + Toggle key: Toggle ghost blocks
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == true)
        {
            NBTUtils.toggleBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_GHOST_BLOCKS);
        }
        // Just Toggle key: Toggle the area flipped property
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == false &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.toggleAreaFlipped(stack, player);
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 600;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 3;
        }

        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        return 0;
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

        // Only allow the "Miscellaneous" type Memory Cards
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == true && imodule.getModuleTier(moduleStack) != ItemLinkCrystal.TYPE_BLOCK)
        {
            return 0;
        }

        return this.getMaxModules(containerStack, moduleType);
    }

    public Mode getMode(ItemStack stack)
    {
        return Mode.getMode(stack);
    }

    public class Area
    {
        public int rPosH;
        public int rNegH;
        public int rPosV;
        public int rNegV;
        public int maxRadius;

        public Area(int packed)
        {
            this.init(packed);
        }

        public Area(ItemStack stack)
        {
            int mode = Mode.getModeOrdinal(stack);
            NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, false);
            NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + mode, false);
            if (tag != null)
            {
                this.init(tag.getInteger(TAG_NAME_DIMENSIONS));
            }
            else
            {
                this.init(0);
            }
        }

        public void init(int packed)
        {
            this.init(packed & 0xFF, (packed >> 8) & 0xFF, (packed >> 16) & 0xFF, (packed >> 24) & 0xFF);
        }

        public void init(int rPosH, int rNegH, int rPosV, int rNegV)
        {
            this.rPosH = rPosH;
            this.rNegH = rNegH;
            this.rPosV = rPosV;
            this.rNegV = rNegV;
            this.maxRadius = 64;
        }

        /**
         * Adjust the area based on the "planarized" facing, where<br>
         * NORTH = rPosV<br>
         * SOUTH = rNegV<br>
         * EAST  = rPosH<br>
         * WEST  = rNegH<br>
         * @param dir
         * @param amount
         * @return
         */
        public Area adjustFromPlanarizedFacing(EnumFacing facing, int amount, EnumFacing upAxis, EnumFacing rightAxis)
        {
            if (facing == upAxis)
            {
                this.rPosV = MathHelper.clamp_int(this.rPosV + amount, 0, this.maxRadius);
            }
            else if (facing == upAxis.getOpposite())
            {
                this.rNegV = MathHelper.clamp_int(this.rNegV + amount, 0, this.maxRadius);
            }
            else if (facing == rightAxis)
            {
                this.rPosH = MathHelper.clamp_int(this.rPosH + amount, 0, this.maxRadius);
            }
            else if (facing == rightAxis.getOpposite())
            {
                this.rNegH = MathHelper.clamp_int(this.rNegH + amount, 0, this.maxRadius);
            }

            return this;
        }

        public int getPacked()
        {
            return this.rPosH | (this.rNegH << 8) | (this.rPosV << 16) | (this.rNegV << 24);
        }

        public void writeToNBT(ItemStack stack)
        {
            int mode = Mode.getModeOrdinal(stack);
            NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
            NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + mode, true);
            tag.setInteger(TAG_NAME_DIMENSIONS, this.getPacked());
        }
    }

    public static enum Mode
    {
        EXTEND_CONTINUOUS ("enderutilities.tooltip.item.extend.continuous"),
        EXTEND_AREA ("enderutilities.tooltip.item.extend.area"),
        LINE ("enderutilities.tooltip.item.build.line"),
        PLANE ("enderutilities.tooltip.item.build.plane"),
        COLUMN ("enderutilities.tooltip.item.build.column"),
        WALLS ("enderutilities.tooltip.item.build.walls"),
        CUBE ("enderutilities.tooltip.item.build.cube"),
        COPY ("enderutilities.tooltip.item.build.copy"),
        PASTE ("enderutilities.tooltip.item.build.paste"),
        DELETE ("enderutilities.tooltip.item.build.delete");

        private String unlocName;

        Mode (String unlocName)
        {
            this.unlocName = unlocName;
        }

        public String getDisplayName()
        {
            return I18n.translateToLocal(this.unlocName);
        }

        public static Mode getMode(ItemStack stack)
        {
            return values()[getModeOrdinal(stack)];
        }

        public static void cycleMode(ItemStack stack, boolean reverse)
        {
            NBTUtils.cycleByteValue(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE, values().length - 1, reverse);
        }

        public static int getModeOrdinal(ItemStack stack)
        {
            int id = NBTUtils.getByte(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE);
            return (id >= 0 && id < values().length) ? id : 0;
        }
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation("underutilities:usetime"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0.0F;
                }
                else
                {
                    ItemStack itemstack = entityIn.getActiveItemStack();
                    return itemstack != null && itemstack.getItem() == ItemBuildersWand.this ? (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 50.0F : 0.0F;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation("underutilities:inuse"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }
}
