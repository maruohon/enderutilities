package fi.dy.masa.enderutilities.item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.event.tasks.TaskBuildersWand;
import fi.dy.masa.enderutilities.event.tasks.TaskStructureBuild;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.*;
import fi.dy.masa.enderutilities.util.EntityUtils.LeftRight;
import fi.dy.masa.enderutilities.util.TemplateManagerEU.FileInfo;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemBuildersWand extends ItemLocationBoundModular implements IStringInput
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
            if (world.isRemote == false)
            {
                this.setPosition(stack, new BlockPosEU(player.isSneaking() ? pos : pos.offset(side), player.dimension, side), POS_END);
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
            Mode mode = Mode.getMode(stack);
            // Sneak + left click: Set the selected block type (in the appropriate modes)
            if (player.isSneaking() == true && mode != Mode.COPY && mode != Mode.PASTE && mode != Mode.DELETE)
            {
                this.setSelectedFixedBlockType(stack, player, world, pos);
            }
            else
            {
                this.setPosition(stack, new BlockPosEU(player.isSneaking() ? pos : pos.offset(side), player.dimension, side), POS_START);
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
                if (this.useWand(stack, world, player, pos) == EnumActionResult.SUCCESS)
                {
                    player.worldObj.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.4f, 0.7f);
                }
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
        itemName = itemName + " " + preGreen + mode.getDisplayName() + rst;

        int sel = this.getSelectedBlockTypeIndex(stack);

        if (mode == Mode.COPY || mode == Mode.PASTE)
        {
            if (mode == Mode.PASTE)
            {
                //EnumFacing facing = this.getTemplateFacing(stack);
                //itemName = itemName + " rot: " + preGreen + this.getAreaFlipAxis(stack, facing) + rst;

                if (this.getReplaceExisting(stack) == true)
                {
                    itemName += " rep: " + preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    itemName += " rep: " + preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
                }
            }

            itemName = itemName + " - " + I18n.format("enderutilities.tooltip.item.template") +
                    ": " + preGreen + (sel + 1) + rst;

            return itemName;
        }

        if (mode == Mode.DELETE)
        {
            return itemName;
        }

        if (mode != Mode.CUBE && mode != Mode.WALLS)
        {
            if (this.getAreaFlipped(stack) == true)
            {
                String strFlip = this.getAreaFlipAxis(stack, EnumFacing.NORTH).toString();
                itemName = itemName + " flip: " + preGreen + strFlip + rst;
            }
            else
            {
                itemName = itemName + " flip: " + preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
            }
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
                str = I18n.format("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                str = I18n.format("enderutilities.tooltip.item.blocktype.adjacent");
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
            list.add(I18n.format("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        String pre = TextFormatting.DARK_GREEN.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        Mode mode = Mode.getMode(stack);
        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + pre + mode.getDisplayName() + rst);

        int sel = this.getSelectedBlockTypeIndex(stack);
        if (mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE)
        {
            String str = I18n.format("enderutilities.tooltip.item.selectedtemplate");
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
                    String str = I18n.format("enderutilities.tooltip.item.selectedblock");
                    list.add(str + ": " + pre + blockStack.getDisplayName() + rst);
                }
            }
        }
        else
        {
            String str = I18n.format("enderutilities.tooltip.item.selectedblock");
            String str2;
            if (sel == BLOCK_TYPE_TARGETED)
            {
                str2 = I18n.format("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                str2 = I18n.format("enderutilities.tooltip.item.blocktype.adjacent");
            }

            list.add(str + ": " + pre + str2 + rst);
        }

        String str;
        String str2;
        if (mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE)
        {
            str = I18n.format("enderutilities.tooltip.item.rotation") + ": ";
            EnumFacing facing = this.getAreaFacing(stack, mode);
            str2 = facing != null ? preGreen + facing.toString().toLowerCase() : preRed + "N/A";
            list.add(str + str2 + rst);

            str2 = I18n.format("enderutilities.tooltip.item.mirror") + ": ";
            if (this.isMirrored(stack))
            {
                String m = this.getMirror(stack) == Mirror.FRONT_BACK ? "x" : "z";
                list.add(str2 + preGreen + m + rst);
            }
            else
            {
                list.add(str2 + preRed + I18n.format("enderutilities.tooltip.item.no") + rst);
            }
        }
        else
        {
            str = I18n.format("enderutilities.tooltip.item.area.flipped");

            if (this.getAreaFlipped(stack) == true)
            {
                str2 = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
                list.add(str + ": " + str2 + rst);

                str = I18n.format("enderutilities.tooltip.item.flipaxis");
                String preBlue = TextFormatting.BLUE.toString();
                list.add(str + ": " + preBlue + this.getAreaFlipAxis(stack, EnumFacing.UP) + rst);
            }
            else
            {
                str2 = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
                list.add(str + ": " + str2 + rst);
            }

            str = I18n.format("enderutilities.tooltip.item.move");

            if (this.getMovePosition(stack, mode) == true)
            {
                str2 = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
                list.add(str + ": " + str2 + rst);
            }
            else
            {
                str2 = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
                list.add(str + ": " + str2 + rst);
            }
        }

        if (mode == Mode.EXTEND_CONTINUOUS)
        {
            str = I18n.format("enderutilities.tooltip.item.builderswand.allowdiagonals");

            if (NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS) == true)
            {
                str2 = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
            }
            else
            {
                str2 = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
            }

            list.add(str + ": " + str2 + rst);
        }

        if (mode != Mode.COPY && mode != Mode.PASTE && mode != Mode.DELETE)
        {
            str = I18n.format("enderutilities.tooltip.item.builderswand.renderghostblocks");

            if (NBTUtils.getBoolean(stack, ItemBuildersWand.WRAPPER_TAG_NAME, ItemBuildersWand.TAG_NAME_GHOST_BLOCKS) == true)
            {
                str2 = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
            }
            else
            {
                str2 = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
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

    private NBTTagCompound getModeTag(ItemStack stack, Mode mode)
    {
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        return NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode.ordinal(), true);
    }

    public BlockPosEU getPosition(ItemStack stack, boolean isStart)
    {
        return this.getPosition(stack, Mode.getMode(stack), isStart);
    }

    private BlockPosEU getPosition(ItemStack stack, Mode mode, boolean isStart)
    {
        if (mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE)
        {
            if (isStart == true)
            {
                return this.getPerTemplateAreaCorner(stack, mode, isStart);
            }
            else
            {
                return this.getTransformedEndPosition(stack, mode);
            }
        }

        return BlockPosEU.readFromTag(this.getModeTag(stack, mode).getCompoundTag(isStart == true ? "Pos1" : "Pos2"));
    }

    private BlockPosEU getTransformedEndPosition(ItemStack stack, Mode mode)
    {
        BlockPosEU posStart = this.getPerTemplateAreaCorner(stack, mode, true);
        if (posStart == null)
        {
            return null;
        }

        BlockPosEU posEndRelative;
        Mirror mirror = Mirror.NONE;
        Rotation rotation = Rotation.NONE;
        EnumFacing origFacing;
        EnumFacing adjustedFacing = this.getAreaFacing(stack, mode);

        if (mode == Mode.PASTE)
        {
            NBTTagCompound tag = this.getSelectedTemplateTag(stack, mode, false);
            if (tag == null)
            {
                return null;
            }

            posEndRelative = new BlockPosEU(tag.getInteger("endOffsetX"), tag.getInteger("endOffsetY"), tag.getInteger("endOffsetZ"));
            origFacing = this.getTemplateFacing(stack);
        }
        else
        {
            BlockPosEU posEnd = this.getPerTemplateAreaCorner(stack, mode, false);
            if (posEnd == null)
            {
                return null;
            }

            posEndRelative = posEnd.subtract(posStart);
            origFacing = PositionUtils.getFacingFromPositions(posStart, posEnd);
        }

        if (adjustedFacing == null)
        {
            adjustedFacing = origFacing;
        }

        mirror = this.getMirror(stack);
        rotation = PositionUtils.getRotation(origFacing, adjustedFacing);
        posEndRelative = PositionUtils.getTransformedBlockPos(posEndRelative, mirror, rotation);

        return posStart.add(posEndRelative);
    }

    private NBTTagCompound getCornerPositionTag(ItemStack stack, Mode mode, boolean isStart)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CORNERS + "_" + sel, true);
        tag = NBTUtils.getCompoundTag(tag, (isStart == true ? "Pos1" : "Pos2"), true);
        return tag;
    }

    private BlockPosEU getPerTemplateAreaCorner(ItemStack stack, Mode mode, boolean isStart)
    {
        return BlockPosEU.readFromTag(this.getCornerPositionTag(stack, mode, isStart));
    }

    private void setPerTemplateAreaCorner(ItemStack stack, Mode mode, boolean isStart, BlockPosEU pos)
    {
        pos.writeToTag(this.getCornerPositionTag(stack, mode, isStart));
    }

    public void setPosition(ItemStack stack, BlockPosEU pos, boolean isStart)
    {
        Mode mode = Mode.getMode(stack);

        if (isStart == false && mode == Mode.PASTE)
        {
            return;
        }

        if (mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE)
        {
            this.setPerTemplateAreaCorner(stack, mode, isStart, pos);
            this.setMirror(stack, mode, Mirror.NONE);

            // Update the base rotation to the current area when changing corners
            BlockPosEU posStart = this.getPerTemplateAreaCorner(stack, mode, true);
            BlockPosEU posEnd = this.getPerTemplateAreaCorner(stack, mode, false);
            if (posStart != null && posEnd != null)
            {
                this.setAreaFacing(stack, mode, PositionUtils.getFacingFromPositions(posStart, posEnd));
            }

            return;
        }

        NBTTagCompound tag = this.getModeTag(stack, mode);

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
    }

    private EnumActionResult useWand(ItemStack stack, World world, EntityPlayer player, BlockPosEU posTarget)
    {
        if (player.dimension != posTarget.dimension)
        {
            return EnumActionResult.FAIL;
        }

        if (player.capabilities.isCreativeMode == false && UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, true) == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.notenoughendercharge"));
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
            return this.copyAreaToTemplate(stack, world, player, posStart, posEnd);
        }
        else if (mode == Mode.PASTE)
        {
            return this.pasteAreaIntoWorld(stack, world, player, posStart);
        }
        else if (mode == Mode.DELETE)
        {
            return this.deleteArea(stack, world, player, posStart, posEnd);
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
                this.placeBlockToPosition(stack, world, player, positions.get(i));
            }

            // Offset the start position by one after a build operation completes, but not for Walls and Cube modes
            BlockPosEU pos = this.getPosition(stack, POS_START);
            if (pos != null && mode != Mode.WALLS && mode != Mode.CUBE && this.getMovePosition(stack, mode))
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

    public boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player, BlockPosStateDist posStateDist)
    {
        if (world.isAirBlock(posStateDist.toBlockPos()) == false ||
            (player.capabilities.isCreativeMode == false && UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, true) == false))
        {
            return false;
        }

        BlockInfo blockInfo;

        if (this.getSelectedBlockTypeIndex(wandStack) == BLOCK_TYPE_ADJACENT)
        {
            blockInfo = this.getBlockInfoForAdjacentBlock(world, posStateDist.toBlockPos(), posStateDist.side);
        }
        else
        {
            blockInfo = posStateDist.blockInfo;
        }

        if (blockInfo == null || blockInfo.block == Blocks.AIR)
        {
            return false;
        }

        return this.placeBlockToPosition(wandStack, world, player, posStateDist.toBlockPos(), posStateDist.side, blockInfo.blockState, 3);
    }

    public boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player,
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
            IItemHandler inv = this.getInventoryWithItems(wandStack, templateStack, player);
            ItemStack targetStack = this.getItemToBuildWith(inv, templateStack, 1);

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

    private IItemHandler getInventoryWithItems(ItemStack wandStack, ItemStack templateStack, EntityPlayer player)
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

    private ItemStack getItemToBuildWith(IItemHandler inv, ItemStack templateStack, int amount)
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

    private void setSelectedFixedBlockType(ItemStack stack, EntityPlayer player, World world, BlockPos pos)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
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

    public BlockInfo getSelectedFixedBlockType(ItemStack stack)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
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

    public int getSelectedBlockTypeIndex(ItemStack stack)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode, true);

        return tag.getByte(TAG_NAME_BLOCK_SEL);
    }

    private void changeSelectedBlockType(ItemStack stack, boolean reverse)
    {
        Mode mode = Mode.getMode(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);

        int min = mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE ? 0 : -2;
        NBTUtils.cycleByteValue(tag, TAG_NAME_BLOCK_SEL, min, MAX_BLOCKS - 1, reverse);
    }

    private void toggleMirror(ItemStack stack, Mode mode, EntityPlayer player)
    {
        Mirror mirror = player.getHorizontalFacing().getAxis() == EnumFacing.Axis.Z ? Mirror.LEFT_RIGHT : Mirror.FRONT_BACK;

        // Same mirror setting as the one stored, toggle mirror off
        if (mirror == this.getMirror(stack) && this.isMirrored(stack))
        {
            mirror = Mirror.NONE;
        }

        this.setMirror(stack, mode, mirror);
    }

    private void setMirror(ItemStack stack, Mode mode, Mirror mirror)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag.setByte("Mirror_" + sel, (byte)mirror.ordinal());
        tag.setBoolean("IsMirrored_" + sel, mirror != Mirror.NONE);
    }

    public boolean isMirrored(ItemStack stack)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
        return this.getModeTag(stack, Mode.getMode(stack)).getBoolean("IsMirrored_" + sel);
    }

    public Mirror getMirror(ItemStack stack)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, Mode.getMode(stack));

        if (tag.getBoolean("IsMirrored_" + sel) && tag.hasKey("Mirror_" + sel, Constants.NBT.TAG_BYTE) == true)
        {
            return Mirror.values()[tag.getByte("Mirror_" + sel) % Mirror.values().length];
        }

        return Mirror.NONE;
    }

    public boolean getAreaFlipped(ItemStack stack)
    {
        return this.getModeTag(stack, Mode.getMode(stack)).getBoolean("Flip");
    }

    private void toggleAreaFlipped(ItemStack stack, EntityPlayer player)
    {
        NBTTagCompound tag = this.getModeTag(stack, Mode.getMode(stack));
        EnumFacing facing = EntityUtils.getClosestLookingDirection(player);
        tag.setByte("FlipAxis", (byte)facing.getIndex());
        tag.setBoolean("Flip", ! tag.getBoolean("Flip"));
    }

    public EnumFacing getAreaFlipAxis(ItemStack stack, EnumFacing defaultFlipAxis)
    {
        NBTTagCompound tag = this.getModeTag(stack, Mode.getMode(stack));

        if (tag.hasKey("FlipAxis", Constants.NBT.TAG_BYTE) == true)
        {
            return EnumFacing.getFront(tag.getByte("FlipAxis"));
        }

        return defaultFlipAxis;
    }

    private EnumFacing getAxisRight(ItemStack stack, BlockPosEU pos)
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

    private EnumFacing getAxisUp(ItemStack stack, BlockPosEU pos)
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

    private void changeAreaDimensions(EntityPlayer player, ItemStack stack, boolean reverse)
    {
        Mode mode = Mode.getMode(stack);

        if (mode == Mode.COPY || mode == Mode.DELETE)
        {
            this.moveEndPosition(stack, EntityUtils.getClosestLookingDirection(player), reverse);
            return;
        }

        BlockPosEU pos = this.getPosition(stack, POS_START);
        if (pos == null || mode == Mode.WALLS || mode == Mode.CUBE)
        {
            return;
        }

        int amount = reverse == true ? 1 : -1;
        Area area = new Area(this.getModeTag(stack, mode));

        // Only one dimension is used for the column mode
        if (mode == Mode.COLUMN)
        {
            area.adjustFromPlanarizedFacing(EnumFacing.EAST, amount, EnumFacing.UP, EnumFacing.EAST);
            area.writeToNBT(this.getModeTag(stack, mode));
            return;
        }

        EnumFacing faceAxis = pos.side;
        EnumFacing axisRight = this.getAxisRight(stack, pos);
        EnumFacing axisUp = this.getAxisUp(stack, pos);

        boolean isFlipped = this.getAreaFlipped(stack);
        EnumFacing flipAxis = this.getAreaFlipAxis(stack, faceAxis);
        EnumFacing faceAxisFlipped = isFlipped == true ? BlockPosEU.getRotation(faceAxis, flipAxis) : faceAxis;

        EnumFacing lookDir;

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

        //System.out.printf("face: %s flippedFace: %s flipAxis: %s look: %s up: %s right: %s\n", faceAxis, (isFlipped ? faceAxisFlipped : "none"), flipAxis, lookDir, axisUp, axisRight);
        area.adjustFromPlanarizedFacing(lookDir, amount, axisUp, axisRight);
        area.writeToNBT(this.getModeTag(stack, mode));
    }

    private void moveEndPosition(ItemStack stack, EnumFacing direction, boolean reverse)
    {
        Mode mode = Mode.getMode(stack);
        BlockPosEU posStart = this.getPerTemplateAreaCorner(stack, mode, POS_START);
        BlockPosEU posEnd = this.getPosition(stack, mode, POS_END);
        if (posEnd == null)
        {
            posEnd = posStart;
        }

        if (posStart != null && posEnd != null)
        {
            int v = reverse ? 1 : -1;
            posEnd = posEnd.add(direction.getFrontOffsetX() * v, direction.getFrontOffsetY() * v, direction.getFrontOffsetZ() * v);
            this.setPerTemplateAreaCorner(stack, mode, false, posEnd);
            this.setAreaFacing(stack, mode, PositionUtils.getFacingFromPositions(posStart, posEnd));
            this.setMirror(stack, mode, Mirror.NONE);
        }
    }

    private void addAdjacent(EntityPlayer player, World world, BlockPosEU center, Area area, int posV, int posH, List<BlockPosStateDist> positions,
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

        // The block on the back face must not be air or fluid ...
        if (block.isAir(state, world, blockPos) == true || state.getMaterial().isLiquid() == true)
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
                blockInfo = new BlockInfo(world, blockPos);
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

    private List<BlockPosStateDist> getPositionsOnPlane(ItemStack stack, Mode mode, World world, BlockPosEU posStart, EnumFacing axisRight, EnumFacing axisUp)
    {
        BlockPosEU pos = posStart;
        Area area = new Area(this.getModeTag(stack, mode));
        BlockPos pos1 = posStart.toBlockPos().offset(axisRight, -area.rNegH).offset(axisUp, -area.rNegV);
        BlockPos pos2 = posStart.toBlockPos().offset(axisRight,  area.rPosH).offset(axisUp,  area.rPosV);
        BlockPos posMin = PositionUtils.getMinCorner(pos1, pos2);
        BlockPos posMax = PositionUtils.getMaxCorner(pos1, pos2);
        EnumFacing side = posStart.side;
        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        List<BlockPosEU> branches = new ArrayList<BlockPosEU>();
        Set<BlockPosEU> visited = new HashSet<BlockPosEU>();
        boolean continueThrough = this.getContinueThrough(stack, mode);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, posStart.offset(side, -1).toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = this.getSelectedBlockTypeIndex(stack);

        int counter = 0;
        int branchIndex = 0;
        EnumFacing nextSide = null;
        EnumFacing ignoreSide = null;

        while (counter <= 16641) // 129 * 129 area
        {
            nextSide = this.checkPositionIgnoringSide(mode, world, pos, posMin, posMax, visited, branches, positions,
                    ignoreSide, continueThrough, blockType, biTarget, biBound);
            counter++;

            if (nextSide == null)
            {
                if (branchIndex < branches.size())
                {
                    pos = branches.get(branchIndex);
                    ignoreSide = null;
                    branchIndex++;
                }
                else
                {
                    break;
                }
            }
            else
            {
                pos = pos.offset(nextSide);
                ignoreSide = nextSide.getOpposite();
            }
        }
        //System.out.printf("counter: %d\n", counter);
        return positions;
    }



    private EnumFacing checkPositionIgnoringSide(Mode mode, World world, BlockPosEU posIn, BlockPos posMin, BlockPos posMax,
            Set<BlockPosEU> visited, List<BlockPosEU> branches, List<BlockPosStateDist> positions,
            EnumFacing ignoreSide, boolean continueThrough, int blockType, BlockInfo biTarget, BlockInfo biBound)
    {
        BlockPosEU pos = posIn;
        BlockPos posTmp = pos.toBlockPos();
        EnumFacing continueTo = null;
        int sides = 0;

        if (visited.contains(posIn) || PositionUtils.isPositionInsideArea(pos, posMin, posMax) == false)
        {
            return null;
        }

        if (world.isAirBlock(posTmp))
        {
            if (mode == Mode.EXTEND_AREA)
            {
                BlockPos posTgt = posTmp.offset(posIn.side, -1);

                IBlockState state = world.getBlockState(posTgt);
                Block block = state.getBlock();
                int meta = block.getMetaFromState(state);

                // The block on the back face must not be air and also it must not be fluid.
                // If the block type to work with is BLOCK_TYPE_TARGETED, then the block adjacent
                // to his position must match the targeted block.
                if (block.isAir(state, world, posTgt) == false && state.getMaterial().isLiquid() == false)
                {
                    if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && biBound != null) ||
                       (biTarget != null && biTarget.block == block && biTarget.blockMeta == meta))
                    {
                        positions.add(new BlockPosStateDist(posIn,
                                        this.getBlockInfoForBlockType(world, posTmp, posIn.side, blockType, biTarget, biBound)));
                    }
                }
            }
            else
            {
                positions.add(new BlockPosStateDist(posIn,
                            this.getBlockInfoForBlockType(world, posTmp, posIn.side, blockType, biTarget, biBound)));
            }
        }
        else if (continueThrough == false)
        {
            return null;
        }

        visited.add(posIn);

        for (EnumFacing side : PositionUtils.getSidesForAxis(posIn.side.getAxis()))
        {
            if (side == ignoreSide)
            {
                continue;
            }

            pos = posIn.offset(side);

            if (visited.contains(pos) || PositionUtils.isPositionInsideArea(pos, posMin, posMax) == false)
            {
                continue;
            }

            if (world.isAirBlock(pos.toBlockPos()) || continueThrough)
            {
                if (visited.contains(pos) == false)
                {
                    if (sides == 0)
                    {
                        continueTo = side;
                    }
                    else if (branches.contains(pos) == false)
                    {
                        branches.add(pos);
                    }
                }

                sides++;
            }
        }

        return continueTo;
    }

    private BlockInfo getBlockInfoForAdjacentBlock(World world, BlockPos pos, EnumFacing side)
    {
        return new BlockInfo(world, pos.offset(side, -1));
    }

    private BlockInfo getBlockInfoForTargeted(ItemStack stack, World world, BlockPos pos)
    {
        int blockType = this.getSelectedBlockTypeIndex(stack);
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

    private BlockInfo getBlockInfoForBlockType(World world, BlockPos pos, EnumFacing side, int blockType, BlockInfo biTarget, BlockInfo biBound)
    {
        if (blockType == BLOCK_TYPE_TARGETED)
        {
            return biTarget;
        }

        if (blockType == BLOCK_TYPE_ADJACENT)
        {
            return this.getBlockInfoForAdjacentBlock(world, pos, side);
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
        int blockType = this.getSelectedBlockTypeIndex(stack);
        Mode mode = Mode.getMode(stack);
        Area area = new Area(this.getModeTag(stack, mode));
        int dim = world.provider.getDimension();
        boolean continueThrough = this.getContinueThrough(stack, mode);

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
                    else if (continueThrough == false)
                    {
                        break;
                    }
                }
                break;

            case LINE:
                for (int i = 0; i <= area.rPosH; i++)
                {
                    BlockPos posTmp = center.offset(axisRight, i).toBlockPos();
                    if (world.isAirBlock(posTmp) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, side,
                                        this.getBlockInfoForBlockType(world, posTmp, side, blockType, biTarget, biBound)));
                    }
                    else if (continueThrough == false)
                    {
                        break;
                    }
                }

                for (int i = -1; i >= area.rNegH; i--)
                {
                    BlockPos posTmp = center.offset(axisRight, i).toBlockPos();
                    if (world.isAirBlock(posTmp) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, side,
                                        this.getBlockInfoForBlockType(world, posTmp, side, blockType, biTarget, biBound)));
                    }
                    else if (continueThrough == false)
                    {
                        break;
                    }
                }
                break;

            case PLANE:
            case EXTEND_AREA:
                positions.addAll(this.getPositionsOnPlane(stack, mode, world, center, axisRight, axisUp));
                break;

            case EXTEND_CONTINUOUS:
                boolean diagonals = NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
                this.addAdjacent(player, world, center, area, 0, 0, positions, blockType, diagonals, biTarget, axisRight, axisUp);
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
        int blockType = this.getSelectedBlockTypeIndex(stack);
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

    private void getBlockPositionsCube(ItemStack stack, World world, List<BlockPosStateDist> positions, BlockPosEU pos1, BlockPosEU pos2)
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
        int blockType = this.getSelectedBlockTypeIndex(stack);
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

    private EnumActionResult copyAreaToTemplate(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn, BlockPosEU posEndIn)
    {
        if (Configs.buildersWandEnableCopyPaste == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabled"));
            return EnumActionResult.FAIL;
        }

        if (posStartIn == null || posEndIn == null)
        {
            return EnumActionResult.FAIL;
        }

        BlockPos posStart = posStartIn.toBlockPos();
        BlockPos endOffset = posEndIn.toBlockPos().subtract(posStart);
        //System.out.printf("posStart: %s posEnd: %s endOffset: %s\n", posStart, posEndIn.toBlockPos(), endOffset);

        if (this.isAreaWithinSizeLimit(endOffset, player) == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge", this.getMaxAreaDimension(player)));
            return EnumActionResult.FAIL;
        }

        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack, player);

        TemplateEnderUtilities template = templateManager.getTemplate(rl);
        template.takeBlocksFromWorld(world, posStart, endOffset, true);
        template.setAuthor(player.getName());
        templateManager.writeTemplate(rl);

        TemplateMetadata templateMeta = templateManager.getTemplateMetadata(rl);
        EnumFacing facing = PositionUtils.getFacingFromPositions(posStart, posStart.add(endOffset));
        templateMeta.setValues(endOffset, facing, this.getTemplateName(stack, Mode.COPY), player.getName());
        templateManager.writeTemplateMetadata(rl);

        player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areasavedtotemplate", (this.getSelectedBlockTypeIndex(stack) + 1)));

        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult pasteAreaIntoWorld(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn)
    {
        if (Configs.buildersWandEnableCopyPaste == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabled"));
            return EnumActionResult.FAIL;
        }

        if (posStartIn == null || posStartIn.dimension != player.dimension ||
            player.getDistanceSq(posStartIn.toBlockPos()) > 16384)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areatoofar"));
            return EnumActionResult.FAIL;
        }

        TemplateMetadata templateMeta = this.getTemplateMetadata(stack, player);

        if (this.isAreaWithinSizeLimit(templateMeta.getRelativeEndPosition(), player) == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge", this.getMaxAreaDimension(player)));
            return EnumActionResult.FAIL;
        }

        PlacementSettings placement = this.getPasteModePlacement(stack, player);
        TemplateEnderUtilities template = this.getTemplate(world, player, stack, placement);
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

        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult deleteArea(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn, BlockPosEU posEndIn)
    {
        if (player.capabilities.isCreativeMode == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.creativeonly"));
            return EnumActionResult.FAIL;
        }

        if (posStartIn == null || posEndIn == null || posStartIn.dimension != player.dimension || posEndIn.dimension != player.dimension)
        {
            return EnumActionResult.FAIL;
        }

        BlockPos posStart = posStartIn.toBlockPos();
        BlockPos posEnd = posEndIn.toBlockPos();

        if (player.getDistanceSq(posStart) >= 16384 || player.getDistanceSq(posEnd) >= 16384 ||
            this.isAreaWithinSizeLimit(posStart.subtract(posEnd), player) == false)
        {
            player.addChatMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolargeortoofar"));
            return EnumActionResult.FAIL;
        }

        for (BlockPos.MutableBlockPos posMutable : BlockPos.getAllInBoxMutable(posStart, posEnd))
        {
            if (world.isAirBlock(posMutable) == false)
            {
                TileEntity te = world.getTileEntity(posMutable);
                if (te instanceof IInventory)
                {
                    ((IInventory) te).clear();
                }

                world.setBlockToAir(posMutable);
            }
        }

        if (this.getRemoveEntities(stack))
        {
            int x1 = Math.min(posStart.getX(), posEnd.getX());
            int y1 = Math.min(posStart.getY(), posEnd.getY());
            int z1 = Math.min(posStart.getZ(), posEnd.getZ());
            int x2 = Math.max(posStart.getX(), posEnd.getX());
            int y2 = Math.max(posStart.getY(), posEnd.getY());
            int z2 = Math.max(posStart.getZ(), posEnd.getZ());

            AxisAlignedBB bb = new AxisAlignedBB(x1, y1, z1, x2 + 1, y2 + 1, z2 + 1);
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, bb);

            for (Entity entity : entities)
            {
                if ((entity instanceof EntityPlayer) == false)
                {
                    entity.setDead();
                }
            }
        }

        return EnumActionResult.SUCCESS;
    }

    private void placeHelperBlock(EntityPlayer player)
    {
        BlockPos pos = PositionUtils.getPositionInfrontOfEntity(player);
        //player.worldObj.setBlockState(pos, Blocks.RED_MUSHROOM_BLOCK.getDefaultState(), 3);
        player.worldObj.setBlockState(pos, Blocks.LEAVES.getDefaultState()
                .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE)
                .withProperty(BlockLeaves.CHECK_DECAY, false)
                .withProperty(BlockLeaves.DECAYABLE, true), 3);
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

    private TemplateEnderUtilities getTemplate(World world, EntityPlayer player, ItemStack stack, PlacementSettings placement)
    {
        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack, player);
        TemplateEnderUtilities template = templateManager.getTemplate(rl);
        template.setPlacementSettings(placement);

        return template;
    }

    private TemplateMetadata getTemplateMetadata(ItemStack stack, EntityPlayer player)
    {
        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack, player);
        TemplateMetadata templateMeta = templateManager.getTemplateMetadata(rl);

        return templateMeta;
    }

    public String getTemplateName(ItemStack stack, Mode mode)
    {
        NBTTagCompound nbt = this.getSelectedTemplateTag(stack, mode, false);
        if (nbt != null && nbt.hasKey("TemplateName"))
        {
            return nbt.getString("TemplateName");
        }

        return "N/A";
    }

    public void setTemplateName(ItemStack stack, EntityPlayer player, String name)
    {
        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack, player);
        TemplateMetadata meta = templateManager.getTemplateMetadata(rl);
        meta.setTemplateName(name);
        templateManager.writeTemplateMetadata(rl);

        this.setTemplateNameOnItem(stack, Mode.COPY, name);
        this.updateTemplateMetadata(stack, player);
    }

    public void setTemplateNameOnItem(ItemStack stack, Mode mode, String name)
    {
        NBTTagCompound nbt = this.getSelectedTemplateTag(stack, mode, true);
        nbt.setString("TemplateName", name);
    }

    private ResourceLocation getTemplateResource(ItemStack stack, EntityPlayer player)
    {
        int id = this.getSelectedBlockTypeIndex(stack);
        UUID uuid = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
        String name = NBTUtils.getOrCreateString(stack, WRAPPER_TAG_NAME, "player", player.getName());
        return new ResourceLocation(Reference.MOD_ID, name + "_" + uuid.toString() + "_" + id);
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

    private NBTTagCompound getSelectedTemplateTag(ItemStack stack, Mode mode, boolean create)
    {
        int sel = this.getSelectedBlockTypeIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_TEMPLATES + "_" + sel, create);

        return tag;
    }

    private void updateTemplateMetadata(ItemStack stack, EntityPlayer player)
    {
        TemplateManagerEU templateManager = this.getTemplateManager();
        ResourceLocation rl = this.getTemplateResource(stack, player);
        FileInfo info = templateManager.getTemplateInfo(rl);
        NBTTagCompound tag = this.getSelectedTemplateTag(stack, Mode.PASTE, true);

        if (tag.getLong("Timestamp") != info.timestamp || tag.getLong("FileSize") != info.fileSize)
        {
            TemplateMetadata meta = templateManager.getTemplateMetadata(rl);
            BlockPos size = meta.getRelativeEndPosition();
            tag.setLong("TimeStamp", info.timestamp);
            tag.setLong("FileSize", info.fileSize);
            tag.setInteger("endOffsetX", size.getX());
            tag.setInteger("endOffsetY", size.getY());
            tag.setInteger("endOffsetZ", size.getZ());
            tag.setByte("TemplateFacing", (byte)meta.getFacing().getIndex());
            tag.setString("TemplateName", meta.getTemplateName());
        }
    }

    public EnumFacing getTemplateFacing(ItemStack stack)
    {
        NBTTagCompound tag = this.getSelectedTemplateTag(stack, Mode.PASTE, true);
        return EnumFacing.getFront(tag.getByte("TemplateFacing"));
    }

    public EnumFacing getAreaFacing(ItemStack stack, Mode mode)
    {
        NBTTagCompound tag = this.getSelectedTemplateTag(stack, mode, true);

        if (tag.hasKey("Rotation", Constants.NBT.TAG_BYTE))
        {
            return EnumFacing.getFront(tag.getByte("Rotation"));
        }
        else
        {
            BlockPosEU posStart = this.getPerTemplateAreaCorner(stack, mode, true);
            BlockPosEU posEnd = this.getPerTemplateAreaCorner(stack, mode, false);
            if (posStart != null && posEnd != null)
            {
                return PositionUtils.getFacingFromPositions(posStart, posEnd);
            }
        }

        return null;
    }

    private void setAreaFacing(ItemStack stack, Mode mode, EntityPlayer player)
    {
        this.setAreaFacing(stack, mode, player.getHorizontalFacing());
    }

    private void setAreaFacing(ItemStack stack, Mode mode, EnumFacing facing)
    {
        NBTTagCompound tag = this.getSelectedTemplateTag(stack, mode, true);
        tag.setByte("Rotation", (byte)facing.getIndex());
    }

    private void toggleMovePosition(ItemStack stack, Mode mode)
    {
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag.setBoolean("Move", ! tag.getBoolean("Move"));
    }

    public boolean getMovePosition(ItemStack stack, Mode mode)
    {
        return this.getModeTag(stack, mode).getBoolean("Move");
    }

    private void toggleReplaceExisting(ItemStack stack)
    {
        NBTTagCompound tag = this.getModeTag(stack, Mode.PASTE);
        tag.setBoolean("Replace", ! tag.getBoolean("Replace"));
    }

    public boolean getReplaceExisting(ItemStack stack)
    {
        return this.getModeTag(stack, Mode.PASTE).getBoolean("Replace");
    }

    private void toggleRemoveEntities(ItemStack stack)
    {
        NBTTagCompound tag = this.getModeTag(stack, Mode.DELETE);
        tag.setBoolean("RemoveEntities", ! tag.getBoolean("RemoveEntities"));
    }

    public boolean getRemoveEntities(ItemStack stack)
    {
        return this.getModeTag(stack, Mode.DELETE).getBoolean("RemoveEntities");
    }

    private void toggleContinueThrough(ItemStack stack, Mode mode)
    {
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag.setBoolean("GoThrough", ! tag.getBoolean("GoThrough"));
    }

    public boolean getContinueThrough(ItemStack stack, Mode mode)
    {
        return this.getModeTag(stack, mode).getBoolean("GoThrough");
    }

    private PlacementSettings getPasteModePlacement(ItemStack stack, EntityPlayer player)
    {
        EnumFacing facing = this.getTemplateFacing(stack);
        EnumFacing areaFacing = this.getAreaFacing(stack, Mode.PASTE);
        if (areaFacing == null)
        {
            areaFacing = facing;
        }

        Rotation rotation = PositionUtils.getRotation(facing, areaFacing);
        boolean ignoreEntities = player == null || player.capabilities.isCreativeMode == false;
        return new PlacementSettings(this.getMirror(stack), rotation, ignoreEntities, Blocks.BARRIER, null);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (key == HotKeys.KEYCODE_CUSTOM_1)
        {
            this.placeHelperBlock(player);
            return;
        }

        Mode mode = Mode.getMode(stack);

        // Alt + Toggle key: Change the selected block type
        if (EnumKey.SCROLL.matches(key, HotKeys.MOD_ALT))
        {
            this.changeSelectedBlockType(stack, EnumKey.keypressActionIsReversed(key));

            if (mode == Mode.PASTE)
            {
                this.updateTemplateMetadata(stack, player);
            }
        }
        // Shift + Scroll: Change the dimensions of the current mode
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT))
        {
            if (mode != Mode.PASTE)
            {
                this.changeAreaDimensions(player, stack, EnumKey.keypressActionIsReversed(key));
            }
        }
        // Shift + Toggle key: Toggle the mirroring in the appropriate modes
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            if (mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE)
            {
                this.toggleMirror(stack, mode, player);
            }
            else if (mode != Mode.WALLS || mode != Mode.CUBE)
            {
                this.toggleMovePosition(stack, mode);
            }
        }
        // Ctrl + Toggle key: Cycle the mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            Mode.cycleMode(stack, EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));

            if (Mode.getMode(stack) == Mode.PASTE)
            {
                this.updateTemplateMetadata(stack, player);
            }
        }
        // Ctrl + Alt + Shift + Toggle key: Change the selected link crystal
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_CTRL_ALT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT_CTRL_ALT))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, EnumKey.keypressActionIsReversed(key));
        }
        // Ctrl + Alt + Toggle key: Toggle "allow diagonals" in Extend Continuous mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL_ALT))
        {
            if (mode == Mode.PASTE)
            {
                this.toggleReplaceExisting(stack);
            }
            else if (mode == Mode.DELETE)
            {
                this.toggleRemoveEntities(stack);
            }
            else if (mode == Mode.EXTEND_CONTINUOUS)
            {
                NBTUtils.toggleBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
            }
            else if (mode == Mode.COLUMN || mode == Mode.LINE || mode == Mode.PLANE || mode == Mode.EXTEND_AREA)
            {
                this.toggleContinueThrough(stack, mode);
            }
        }
        // Alt + Shift + Toggle key: Toggle ghost blocks
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            NBTUtils.toggleBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_GHOST_BLOCKS);
        }
        // Just Toggle key: Toggle the area flipped property
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            if (mode == Mode.COPY || mode == Mode.PASTE || mode == Mode.DELETE)
            {
                this.setAreaFacing(stack, mode, player);
            }
            else
            {
                this.toggleAreaFlipped(stack, player);
            }
        }
    }

    @Override
    public void handleString(EntityPlayer player, ItemStack stack, String text)
    {
        if (stack != null)
        {
            this.setTemplateName(stack, player, text);
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

        public Area(NBTTagCompound tag)
        {
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

        public void writeToNBT(NBTTagCompound tag)
        {
            tag.setInteger(TAG_NAME_DIMENSIONS, this.getPacked());
        }
    }

    public static enum Mode
    {
        EXTEND_CONTINUOUS ("enderutilities.tooltip.item.build.extend.continuous"),
        EXTEND_AREA ("enderutilities.tooltip.item.build.extend.area"),
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
            return I18n.format(this.unlocName);
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
