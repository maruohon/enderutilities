package fi.dy.masa.enderutilities.item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.event.tasks.IPlayerTask;
import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.event.tasks.TaskBuildersWand;
import fi.dy.masa.enderutilities.event.tasks.TaskMoveArea;
import fi.dy.masa.enderutilities.event.tasks.TaskReplaceBlocks;
import fi.dy.masa.enderutilities.event.tasks.TaskReplaceBlocks3D;
import fi.dy.masa.enderutilities.event.tasks.TaskStackArea;
import fi.dy.masa.enderutilities.event.tasks.TaskTemplatePlaceBlocks;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.IStringInput;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.*;
import fi.dy.masa.enderutilities.util.EntityUtils.LeftRight;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities.ReplaceMode;
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
    public static final String TAG_NAME_TEMPLATES = "Templates";
    public static final int BLOCK_TYPE_TARGETED = -1;
    public static final int BLOCK_TYPE_ADJACENT = -2;
    public static final boolean POS_START = true;
    public static final boolean POS_END = false;

    protected Map<UUID, Long> lastLeftClick = new HashMap<UUID, Long>();

    public ItemBuildersWand(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        BlockPosEU pos = this.getPosition(stack, POS_START);

        if (pos == null)
        {
            return super.onItemRightClick(world, player, hand);
        }

        Mode mode = Mode.getMode(stack);

        if (world.isRemote == false)
        {
            if (this.cancelRunningTask(player))
            {
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
            else if (mode.hasUseDelay() == false)
            {
                EnumActionResult result = this.useWand(stack, world, player, pos);
                return new ActionResult<ItemStack>(result, stack);
            }
        }

        if (mode.hasUseDelay() && this.getPosition(stack, POS_END) != null)
        {
            player.setActiveHand(hand);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
    }

    private boolean cancelRunningTask(EntityPlayer player)
    {
        boolean removed = false;
        @SuppressWarnings("unchecked")
        Class<? extends IPlayerTask>[] classes = new Class[] {
                TaskBuildersWand.class,
                //TaskMoveArea.class,
                TaskReplaceBlocks.class,
                TaskStackArea.class,
                TaskTemplatePlaceBlocks.class };

        PlayerTaskScheduler scheduler = PlayerTaskScheduler.getInstance();

        for (Class<? extends IPlayerTask> clazz : classes)
        {
            if (scheduler.hasTask(player, clazz))
            {
                PlayerTaskScheduler.getInstance().removeTask(player, clazz);
                removed = true;
            }
        }

        return removed;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);

        if (te != null && (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) || te.getClass() == TileEntityEnderChest.class))
        {
            return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
        }

        Mode mode = Mode.getMode(stack);

        if (mode.hasTwoPlacableCorners())
        {
            if (world.isRemote == false)
            {
                if (mode == Mode.REPLACE_3D && player.isSneaking() && WandOption.BIND_MODE.isEnabled(stack, mode))
                {
                    this.setSelectedFixedBlockType(stack, player, world, pos, true);
                }
                else
                {
                    BlockPosEU posEU = new BlockPosEU(player.isSneaking() ? pos : pos.offset(side), world.provider.getDimension(), side);
                    this.setPosition(posEU, POS_END, stack, player);
                }
            }

            return EnumActionResult.SUCCESS;
        }

        // Don't allow targeting the top face of blocks while sneaking
        // This should make sneak building a platform a lot less annoying
        if (world.isRemote == false && (player.isSneaking() == false || side != EnumFacing.UP || mode == Mode.REPLACE))
        {
            // Don't offset the position in Replace mode
            if (mode != Mode.REPLACE)
            {
                pos = pos.offset(side);
            }

            return this.useWand(stack, world, player, new BlockPosEU(pos, world.provider.getDimension(), side));
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        Mode mode = Mode.getMode(stack);

        if (mode == Mode.REPLACE_3D && player.isSneaking() && WandOption.BIND_MODE.isEnabled(stack, mode))
        {
            if (world.isRemote == false)
            {
                this.setSelectedFixedBlockType(stack, player, world, pos, true);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;
        }
        else if (mode.hasTwoPlacableCorners())
        {
            if (world.isRemote == false)
            {
                BlockPosEU posEU = new BlockPosEU(player.isSneaking() ? pos : pos.offset(side), world.provider.getDimension(), side);
                this.setPosition(posEU, POS_END, stack, player);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.SUCCESS;
        }

        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    public void onLeftClickBlock(EntityPlayer player, World world, ItemStack stack, BlockPos pos, int dimension, EnumFacing side)
    {
        if (world.isRemote)
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
            if (player.isSneaking() && (mode.isAreaMode() == false ||
                    (mode == Mode.REPLACE_3D && WandOption.BIND_MODE.isEnabled(stack, mode))))
            {
                this.setSelectedFixedBlockType(stack, player, world, pos, false);
            }
            else if (mode == Mode.REPLACE)
            {
                if (WandOption.REPLACE_MODE_IS_AREA.isEnabled(stack, mode))
                {
                    this.setPosition(new BlockPosEU(pos, world.provider.getDimension(), side), POS_START, stack, player);
                }
            }
            else
            {
                BlockPosEU posEU = new BlockPosEU(player.isSneaking() ? pos : pos.offset(side), world.provider.getDimension(), side);
                this.setPosition(posEU, POS_START, stack, player);
            }
        }

        this.lastLeftClick.put(player.getUniqueID(), world.getTotalWorldTime());
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase livingBase, int itemInUseCount)
    {
        if (world.isRemote || (livingBase instanceof EntityPlayer) == false)
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
                    player.getEntityWorld().playSound(null, player.getPosition(),
                            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.MASTER, 0.4f, 0.7f);
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
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        if (itemName.length() >= 14)
        {
            itemName = EUStringUtils.getInitialsWithDots(itemName);
        }

        itemName = itemName + " - " + preGreen + mode.getDisplayName() + rst;

        return itemName;
    }

    @Override
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean verbose)
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
        String strYes = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
        String strNo = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;
        String str;

        Mode mode = Mode.getMode(stack);
        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + pre + mode.getDisplayName() + rst);

        int sel = this.getSelectionIndex(stack);

        if (mode.isAreaMode() && mode != Mode.REPLACE_3D)
        {
            if (mode == Mode.COPY || mode == Mode.PASTE)
            {
                str = I18n.format("enderutilities.tooltip.item.selectedtemplate");
            }
            else
            {
                str = I18n.format("enderutilities.tooltip.item.area");
            }

            list.add(str + ": " + preGreen + (sel + 1) + rst);
        }
        else if (sel >= 0)
        {
            BlockInfo blockInfo = this.getSelectedFixedBlockType(stack, false);

            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(blockInfo.block, 1, blockInfo.itemMeta);

                if (blockStack.isEmpty() == false)
                {
                    str = I18n.format("enderutilities.tooltip.item.build.target");
                    list.add(str + ": " + pre + blockStack.getDisplayName() + rst);
                }
            }

            if (mode == Mode.REPLACE_3D)
            {
                blockInfo = this.getSelectedFixedBlockType(stack, true);

                if (blockInfo != null)
                {
                    ItemStack blockStack = new ItemStack(blockInfo.block, 1, blockInfo.itemMeta);

                    if (blockStack.isEmpty() == false)
                    {
                        str = I18n.format("enderutilities.tooltip.item.build.replacement");
                        list.add(str + ": " + pre + blockStack.getDisplayName() + rst);
                    }
                }
            }
        }
        else
        {
            str = I18n.format("enderutilities.tooltip.item.selectedblock");

            if (sel == BLOCK_TYPE_TARGETED)
            {
                list.add(str + ": " + pre + I18n.format("enderutilities.tooltip.item.blocktype.targeted") + rst);
            }
            else if (sel == BLOCK_TYPE_ADJACENT)
            {
                list.add(str + ": " + pre + I18n.format("enderutilities.tooltip.item.blocktype.adjacent") + rst);
            }
        }

        if (mode.isAreaMode())
        {
            str = I18n.format("enderutilities.tooltip.item.rotation") + ": ";
            EnumFacing facing = this.getAreaFacing(stack, mode);
            String str2 = facing != null ? preGreen + facing.toString().toLowerCase() : preRed + "N/A";
            list.add(str + str2 + rst);

            str2 = I18n.format("enderutilities.tooltip.item.mirror") + ": ";

            if (this.isMirrored(stack))
            {
                list.add(str2 + preGreen + (this.getMirror(stack) == Mirror.FRONT_BACK ? "x" : "z") + rst);
            }
            else
            {
                list.add(str2 + strNo);
            }
        }
        else
        {
            str = I18n.format("enderutilities.tooltip.item.area.flipped");

            if (this.isAreaFlipped(stack))
            {
                list.add(str + ": " + strYes + rst);

                str = I18n.format("enderutilities.tooltip.item.flipaxis");
                String preBlue = TextFormatting.BLUE.toString();
                list.add(str + ": " + preBlue + this.getAreaFlipAxis(stack, EnumFacing.UP) + rst);
            }
            else
            {
                list.add(str + ": " + strNo + rst);
            }

            str = I18n.format("enderutilities.tooltip.item.move");
            list.add(str + ": " + (WandOption.MOVE_POSITION.isEnabled(stack, mode) ? strYes : strNo) + rst);
        }

        if (mode == Mode.EXTEND_CONTINUOUS)
        {
            str = I18n.format("enderutilities.tooltip.item.builderswand.allowdiagonals");
            list.add(str + ": " + (WandOption.ALLOW_DIAGONALS.isEnabled(stack, mode) ? strYes : strNo) + rst);
        }

        if (mode.isAreaMode() == false)
        {
            str = I18n.format("enderutilities.tooltip.item.builderswand.renderghostblocks");
            list.add(str + ": " + (WandOption.RENDER_GHOST.isEnabled(stack, mode) ? strYes : strNo) + rst);
        }

        super.addTooltipLines(stack, player, list, verbose);
    }

    private NBTTagCompound getModeTag(ItemStack stack, Mode mode)
    {
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        return NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode.getName(), true);
    }

    public BlockPosEU getPosition(ItemStack stack, boolean isStart)
    {
        return this.getPosition(stack, Mode.getMode(stack), isStart);
    }

    public BlockPosEU getPosition(ItemStack stack, Mode mode, boolean isStart)
    {
        if (mode.isAreaMode())
        {
            if (isStart)
            {
                return this.getPerTemplateAreaCorner(stack, mode, isStart);
            }
            else
            {
                return this.getTransformedEndPosition(stack, mode);
            }
        }

        if (mode == Mode.REPLACE && WandOption.REPLACE_MODE_IS_AREA.isEnabled(stack, mode) == false)
        {
            return null;
        }

        return BlockPosEU.readFromTag(this.getModeTag(stack, mode).getCompoundTag(isStart ? "Pos1" : "Pos2"));
    }

    private BlockPosEU getTransformedEndPosition(ItemStack stack, Mode mode)
    {
        return this.getTransformedEndPosition(stack, mode, this.getPerTemplateAreaCorner(stack, mode, true));
    }

    public BlockPosEU getTransformedEndPosition(ItemStack stack, Mode mode, BlockPosEU posStart)
    {
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
        else if (mode == Mode.MOVE_DST)
        {
            BlockPosEU posStartSrc = this.getPerTemplateAreaCorner(stack, Mode.MOVE_SRC, true);
            BlockPosEU posEnd = this.getPerTemplateAreaCorner(stack, Mode.MOVE_SRC, false);

            if (posStartSrc == null || posEnd == null)
            {
                return null;
            }

            posEndRelative = posEnd.subtract(posStartSrc);
            origFacing = PositionUtils.getFacingFromPositions(posStartSrc, posEnd);
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

        mirror = this.getMirror(stack, mode);
        rotation = PositionUtils.getRotation(origFacing, adjustedFacing);
        posEndRelative = PositionUtils.getTransformedBlockPos(posEndRelative, mirror, rotation);

        return posStart.add(posEndRelative);
    }

    private NBTTagCompound getCornerPositionTag(ItemStack stack, Mode mode, boolean isStart)
    {
        int sel = mode == Mode.REPLACE_3D ? 0 : this.getSelectionIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CORNERS + "_" + sel, true);
        tag = NBTUtils.getCompoundTag(tag, (isStart ? "Pos1" : "Pos2"), true);
        return tag;
    }

    private void removeCornerPositionTag(ItemStack stack, Mode mode, boolean isStart)
    {
        int sel = this.getSelectionIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag = NBTUtils.getCompoundTag(tag, TAG_NAME_CORNERS + "_" + sel, true);
        tag.removeTag(isStart ? "Pos1" : "Pos2");
    }

    public NBTTagCompound getAreaTag(ItemStack stack)
    {
        NBTTagCompound tag = this.getModeTag(stack, Mode.getMode(stack));
        return NBTUtils.getCompoundTag(tag, "Area_" + this.getSelectionIndex(stack), true);
    }

    private BlockPosEU getPerTemplateAreaCorner(ItemStack stack, Mode mode, boolean isStart)
    {
        return BlockPosEU.readFromTag(this.getCornerPositionTag(stack, mode, isStart));
    }

    private void setPerTemplateAreaCorner(BlockPosEU pos, Mode mode, boolean isStart, ItemStack stack, EntityPlayer player)
    {
        BlockPosEU oldPos = this.getPerTemplateAreaCorner(stack, mode, isStart);

        if (oldPos != null && oldPos.equals(pos))
        {
            this.removeCornerPositionTag(stack, mode, isStart);
            return;
        }

        if (PositionUtils.isPositionValid(pos))
        {
            if (isStart)
            {
                BlockPosEU endPos = this.getTransformedEndPosition(stack, mode, pos);

                if (endPos != null && PositionUtils.isPositionValid(endPos) == false)
                {
                    player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.positionoutsideworld"), true);
                    return;
                }
            }

            pos.writeToTag(this.getCornerPositionTag(stack, mode, isStart));
        }
        else
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.positionoutsideworld"), true);
        }
    }

    public void setPosition(BlockPosEU pos, boolean isStart, ItemStack stack, EntityPlayer player)
    {
        Mode mode = Mode.getMode(stack);

        if (PositionUtils.isPositionValid(pos) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.positionoutsideworld"), true);
            return;
        }

        if ((isStart == false && (mode == Mode.PASTE || mode == Mode.MOVE_DST || mode == Mode.REPLACE)) ||
            (mode == Mode.REPLACE && WandOption.REPLACE_MODE_IS_AREA.isEnabled(stack, mode) == false))
        {
            return;
        }

        if (mode.isAreaMode())
        {
            this.setPerTemplateAreaCorner(pos, mode, isStart, stack, player);
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

        String tagName = isStart ? "Pos1" : "Pos2";

        if (tag.hasKey(tagName, Constants.NBT.TAG_COMPOUND))
        {
            BlockPosEU oldPos = BlockPosEU.readFromTag(tag.getCompoundTag(tagName));

            if (oldPos != null && oldPos.equals(pos))
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
        if (world.provider.getDimension() != posTarget.getDimension())
        {
            return EnumActionResult.FAIL;
        }

        if (player.capabilities.isCreativeMode == false && UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, true) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.notenoughendercharge"), true);
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
        else if (mode == Mode.MOVE_DST)
        {
            return this.moveArea(stack, world, player, posStart, posEnd);
        }
        else if (mode == Mode.MOVE_SRC)
        {
            return EnumActionResult.PASS;
        }
        else if (mode == Mode.REPLACE)
        {
            return this.replaceBlocks(stack, world, player, posStart != null ? posStart : posTarget);
        }
        else if (mode == Mode.REPLACE_3D)
        {
            return this.replaceBlocks3D(stack, world, player);
        }
        else if (mode == Mode.STACK)
        {
            return this.stackArea(stack, world, player, posStart, posEnd);
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

            if (pos != null && mode != Mode.WALLS && mode != Mode.CUBE &&
                mode != Mode.REPLACE && WandOption.MOVE_POSITION.isEnabled(stack, mode))
            {
                this.setPosition(pos.offset(pos.getFacing()), POS_START, stack, player);
            }
        }
        else
        {
            UUID wandUUID = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
            TaskBuildersWand task = new TaskBuildersWand(world, wandUUID, positions, Configs.buildersWandBlocksPerTick);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    public static boolean hasEnoughCharge(ItemStack stack, EntityPlayer player)
    {
        if (player.capabilities.isCreativeMode)
        {
            return true;
        }

        return UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST, true);
    }

    public boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player, BlockPosStateDist posStateDist)
    {
        BlockInfo blockInfo;

        if (this.getSelectionIndex(wandStack) == BLOCK_TYPE_ADJACENT)
        {
            blockInfo = this.getBlockInfoForAdjacentBlock(world, posStateDist.toBlockPos(), posStateDist.getFacing());
        }
        else
        {
            blockInfo = posStateDist.blockInfo;
        }

        if (blockInfo == null || blockInfo.block == Blocks.AIR)
        {
            return false;
        }

        return this.placeBlockToPosition(wandStack, world, player, posStateDist.toBlockPos(), posStateDist.getFacing(), blockInfo.blockStateActual, 3, true, true);
    }

    public boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player,
            BlockPos pos, EnumFacing side, IBlockState newState, int setBlockStateFlags, boolean requireItems, boolean dropItems)
    {
        if (newState.getMaterial() == Material.AIR || world.getBlockState(pos) == newState ||
            (player.capabilities.isCreativeMode == false && world.isBlockModifiable(player, pos) == false))
        {
            return false;
        }

        boolean replace = WandOption.REPLACE_EXISTING.isEnabled(wandStack);
        boolean isAir = world.isAirBlock(pos);

        if (player.capabilities.isCreativeMode)
        {
            if (replace || isAir)
            {
                BlockUtils.setBlockStateWithPlaceSound(world, pos, newState, setBlockStateFlags);
                return true;
            }

            return false;
        }

        if (hasEnoughCharge(wandStack, player) == false || (isAir == false &&
            (replace == false || BlockUtils.canChangeBlock(world, pos, player, false, Configs.buildersWandMaxBlockHardness) == false)))
        {
            return false;
        }

        ItemStack targetStack = ItemStack.EMPTY;
        Block blockNew = newState.getBlock();

        if (requireItems)
        {
            // Simulate getting the item to build with
            targetStack = getAndConsumeBuildItem(wandStack, world, pos, newState, player, true);
        }

        if (requireItems == false || targetStack.isEmpty() == false)
        {
            // Break the existing block
            if (replace && isAir == false && player instanceof EntityPlayerMP)
            {
                if (dropItems)
                {
                    BlockUtils.breakBlockAsPlayer(world, pos, (EntityPlayerMP) player, wandStack);

                    // Couldn't break the existing block
                    if (world.isAirBlock(pos) == false)
                    {
                        return false;
                    }
                }
                else
                {
                    BlockUtils.setBlockToAirWithoutSpillingContents(world, pos, 2);
                }
            }

            // Check if we can place the block. If we don't require items, then we are moving an area
            // and in that case we always want to place the block.
            if (requireItems == false || BlockUtils.checkCanPlaceBlockAt(world, pos, side, blockNew))
            {
                if (requireItems)
                {
                    // Actually consume the build item
                    getAndConsumeBuildItem(wandStack, world, pos, newState, player, false);
                }

                BlockUtils.setBlockStateWithPlaceSound(world, pos, newState, setBlockStateFlags);

                if (player.capabilities.isCreativeMode == false)
                {
                    UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, false);
                }

                return true;
            }
        }

        return false;
    }

    public boolean replaceBlock(World world, EntityPlayer player, ItemStack stack, BlockPosStateDist posIn)
    {
        BlockPos pos = posIn.toBlockPos();

        if (this.canReplaceBlock(world, player, stack, posIn))
        {
            BlockUtils.getDropAndSetToAir(world, player, pos, posIn.getFacing(), false);

            ItemBuildersWand wand = (ItemBuildersWand) stack.getItem();
            return wand.placeBlockToPosition(stack, world, player, posIn);
        }

        return false;
    }

    private boolean canReplaceBlock(World world, EntityPlayer player, ItemStack stack, BlockPosStateDist posIn)
    {
        return player.capabilities.isCreativeMode || (hasEnoughCharge(stack, player) &&
                BlockUtils.canChangeBlock(world, posIn.toBlockPos(), player, true, Configs.buildersWandMaxBlockHardness) &&
                getAndConsumeBuildItem(stack, world, posIn.toBlockPos(), posIn.blockInfo.blockState, player, true).isEmpty() == false);
    }

    public static ItemStack getAndConsumeBuildItem(ItemStack wandStack, World world, BlockPos pos, IBlockState newState, EntityPlayer player, boolean simulate)
    {
        ItemStack templateStack = BlockUtils.getPickBlockItemStack(world, pos, newState, player, EnumFacing.UP);

        if (templateStack.isEmpty())
        {
            templateStack = BlockUtils.getSilkTouchDrop(newState);
        }

        if (templateStack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        IItemHandler inv = UtilItemModular.getPlayerOrBoundInventoryWithItems(wandStack, templateStack, player);
        return inv != null ? InventoryUtils.extractMatchingItems(inv, templateStack, 1, simulate) : ItemStack.EMPTY;
    }

    private void setSelectedFixedBlockType(ItemStack stack, EntityPlayer player, World world, BlockPos pos, boolean secondary)
    {
        int sel = this.getSelectionIndex(stack, secondary);

        if (sel < 0)
        {
            return;
        }

        String tagName = secondary ? TAG_NAME_BLOCKS + "2" : TAG_NAME_BLOCKS;
        NBTTagCompound blocksTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, tagName, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(blocksTag, TAG_NAME_BLOCK_PRE + sel, true);

        this.setSelectedFixedBlockType(tag, player, world, pos);
    }

    private void setSelectedFixedBlockType(NBTTagCompound tag, EntityPlayer player, World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        tag.setString("BlockName", ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());
        tag.setByte("BlockMeta", (byte)state.getBlock().getMetaFromState(state));

        ItemStack stackTmp = state.getBlock().getPickBlock(state, EntityUtils.getRayTraceFromPlayer(world, player, false), world, pos, player);
        int itemMeta = stackTmp.isEmpty() == false ? stackTmp.getMetadata() : 0;

        tag.setShort("ItemMeta", (short)itemMeta);
    }

    public BlockInfo getSelectedFixedBlockType(ItemStack stack)
    {
        return this.getSelectedFixedBlockType(stack, false);
    }

    public BlockInfo getSelectedFixedBlockType(ItemStack stack, boolean secondary)
    {
        int sel = this.getSelectionIndex(stack, secondary);

        if (sel < 0)
        {
            return null;
        }

        String tagName = secondary ? TAG_NAME_BLOCKS + "2" : TAG_NAME_BLOCKS;
        NBTTagCompound blocksTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, tagName, false);
        NBTTagCompound tag = NBTUtils.getCompoundTag(blocksTag, TAG_NAME_BLOCK_PRE + sel, false);

        if (tag != null && tag.hasKey("BlockName", Constants.NBT.TAG_STRING))
        {
            return new BlockInfo(new ResourceLocation(tag.getString("BlockName")), tag.getByte("BlockMeta"), tag.getShort("ItemMeta"));
        }

        return null;
    }

    public int getSelectionIndex(ItemStack stack)
    {
        return this.getSelectionIndex(stack, false);
    }

    public int getSelectionIndex(ItemStack stack, boolean secondary)
    {
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + Mode.getMode(stack).getName(), true);

        return tag.getByte(secondary ? TAG_NAME_BLOCK_SEL + "2" : TAG_NAME_BLOCK_SEL);
    }

    private void changeSelectionIndex(ItemStack stack, boolean reverse)
    {
        Mode mode = Mode.getMode(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);

        int min = mode.isAreaMode() || mode == Mode.REPLACE ? 0 : -2;
        NBTUtils.cycleByteValue(tag, TAG_NAME_BLOCK_SEL, min, MAX_BLOCKS - 1, reverse);
    }

    private void changeSecondarySelectionIndex(ItemStack stack, boolean reverse)
    {
        NBTUtils.cycleByteValue(this.getModeTag(stack, Mode.getMode(stack)), TAG_NAME_BLOCK_SEL + "2", 0, MAX_BLOCKS - 1, reverse);
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
        int sel = this.getSelectionIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);
        tag.setByte("Mirror_" + sel, (byte)mirror.ordinal());
        tag.setBoolean("IsMirrored_" + sel, mirror != Mirror.NONE);
    }

    public boolean isMirrored(ItemStack stack)
    {
        int sel = this.getSelectionIndex(stack);
        return this.getModeTag(stack, Mode.getMode(stack)).getBoolean("IsMirrored_" + sel);
    }

    public Mirror getMirror(ItemStack stack)
    {
        return getMirror(stack, Mode.getMode(stack));
    }

    public Mirror getMirror(ItemStack stack, Mode mode)
    {
        int sel = this.getSelectionIndex(stack);
        NBTTagCompound tag = this.getModeTag(stack, mode);

        if (tag.getBoolean("IsMirrored_" + sel) && tag.hasKey("Mirror_" + sel, Constants.NBT.TAG_BYTE))
        {
            return Mirror.values()[tag.getByte("Mirror_" + sel) % Mirror.values().length];
        }

        return Mirror.NONE;
    }

    public boolean isAreaFlipped(ItemStack stack)
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

        if (tag.hasKey("FlipAxis", Constants.NBT.TAG_BYTE))
        {
            return EnumFacing.byIndex(tag.getByte("FlipAxis"));
        }

        return defaultFlipAxis;
    }

    private EnumFacing getAxisRight(ItemStack stack, BlockPosEU pos)
    {
        EnumFacing face = pos.getFacing();
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

        if (this.isAreaFlipped(stack))
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, face);
            axisRight = BlockPosEU.getRotation(axisRight, flipAxis);
        }

        return axisRight;
    }

    private EnumFacing getAxisUp(ItemStack stack, BlockPosEU pos)
    {
        EnumFacing face = pos.getFacing();
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

        if (this.isAreaFlipped(stack))
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, face);
            axisUp = BlockPosEU.getRotation(axisUp, flipAxis);
        }

        return axisUp;
    }

    private void changeAreaDimensions(EntityPlayer player, ItemStack stack, boolean reverse)
    {
        Mode mode = Mode.getMode(stack);
        BlockPosEU pos = this.getPosition(stack, POS_START);

        if (pos == null || mode == Mode.PASTE || mode == Mode.MOVE_DST)
        {
            return;
        }

        int amount = reverse ? 1 : -1;
        NBTTagCompound modeTag = this.getModeTag(stack, mode);

        // Stack mode uses a 3D area
        if (mode == Mode.STACK)
        {
            Area3D area = Area3D.getAreaFromNBT(this.getAreaTag(stack));
            EnumFacing lookDir = EntityUtils.getClosestLookingDirection(player);

            if (Math.abs(player.rotationPitch) > 40.0f)
            {
                lookDir = EntityUtils.getVerticalLookingDirection(player);
            }

            area.adjustArea(lookDir, amount);
            area.writeToNBT(this.getAreaTag(stack));

            return;
        }

        // Other modes use a 2D area that can be flipped around an axis
        Area area = new Area(modeTag);

        // Only one dimension is used for the column mode
        if (mode == Mode.COLUMN)
        {
            area.adjustFromPlanarizedFacing(EnumFacing.EAST, amount, EnumFacing.UP, EnumFacing.EAST);
            area.writeToNBT(modeTag);
            return;
        }

        EnumFacing faceAxis = pos.getFacing();
        EnumFacing axisRight = this.getAxisRight(stack, pos);
        EnumFacing axisUp = this.getAxisUp(stack, pos);

        boolean isFlipped = this.isAreaFlipped(stack);
        EnumFacing flipAxis = this.getAreaFlipAxis(stack, faceAxis);
        EnumFacing faceAxisFlipped = isFlipped ? BlockPosEU.getRotation(faceAxis, flipAxis) : faceAxis;

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
        area.writeToNBT(modeTag);
    }

    private void moveEndPosition(ItemStack stack, EntityPlayer player, boolean reverse)
    {
        EnumFacing direction = EntityUtils.getClosestLookingDirection(player);
        Mode mode = Mode.getMode(stack);
        BlockPosEU posStart = mode == Mode.CUBE || mode == Mode.WALLS ?
                this.getPosition(stack, mode, POS_START) : this.getPerTemplateAreaCorner(stack, mode, POS_START);
        BlockPosEU posEnd = this.getPosition(stack, mode, POS_END);

        if (posEnd == null)
        {
            posEnd = posStart;
        }

        if (posStart != null && posEnd != null)
        {
            int v = reverse ? 1 : -1;
            posEnd = posEnd.add(direction.getXOffset() * v, direction.getYOffset() * v, direction.getZOffset() * v);

            if (mode == Mode.WALLS || mode == Mode.CUBE)
            {
                this.setPosition(posEnd, false, stack, player);
            }
            else
            {
                this.setPerTemplateAreaCorner(posEnd, mode, false, stack, player);
                this.setAreaFacing(stack, mode, PositionUtils.getFacingFromPositions(posStart, posEnd));
                this.setMirror(stack, mode, Mirror.NONE);
            }
        }
    }

    private List<BlockPosStateDist> getPositionsOnPlane(ItemStack stack, Mode mode, World world, BlockPosEU posStart,
            EnumFacing axisRight, EnumFacing axisUp)
    {
        BlockPosEU pos = posStart;
        Area area = new Area(this.getModeTag(stack, mode));
        BlockPos pos1 = posStart.toBlockPos().offset(axisRight, -area.rNegH).offset(axisUp, -area.rNegV);
        BlockPos pos2 = posStart.toBlockPos().offset(axisRight,  area.rPosH).offset(axisUp,  area.rPosV);
        BlockPos posMin = PositionUtils.getMinCorner(pos1, pos2);
        BlockPos posMax = PositionUtils.getMaxCorner(pos1, pos2);
        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        List<BlockPosEU> branches = new ArrayList<BlockPosEU>();
        Set<BlockPosEU> visited = new HashSet<BlockPosEU>();
        boolean continueThrough = mode != Mode.EXTEND_CONTINUOUS && WandOption.CONTINUE_THROUGH.isEnabled(stack, mode);
        boolean diagonals = mode == Mode.EXTEND_CONTINUOUS && WandOption.ALLOW_DIAGONALS.isEnabled(stack, mode);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, posStart.offset(posStart.getFacing(), -1).toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = this.getSelectionIndex(stack);

        int counter = 0;
        int branchIndex = 0;
        BlockPosEU nextPos = null;

        while (counter <= 16641) // 129 * 129 area
        {
            nextPos = this.checkPositionOnPlane(mode, world, pos, posMin, posMax, visited, branches, positions,
                    continueThrough, diagonals, blockType, biTarget, biBound);
            counter++;

            if (nextPos == null)
            {
                if (branchIndex < branches.size())
                {
                    pos = branches.get(branchIndex);
                    branchIndex++;
                }
                else
                {
                    break;
                }
            }
            else
            {
                pos = nextPos;
            }
        }
        //System.out.printf("counter: %d\n", counter);
        return positions;
    }

    private BlockPosEU checkPositionOnPlane(Mode mode, World world, BlockPosEU posIn, BlockPos posMin, BlockPos posMax,
            Set<BlockPosEU> visited, List<BlockPosEU> branches, List<BlockPosStateDist> positions,
            boolean continueThrough, boolean diagonals, int blockType, BlockInfo biTarget, BlockInfo biBound)
    {
        BlockPos posTmp = posIn.toBlockPos();
        BlockPosEU continueTo = null;
        int sides = 0;

        if (visited.contains(posIn) || PositionUtils.isPositionInsideArea(posIn, posMin, posMax) == false)
        {
            return null;
        }

        if (world.getBlockState(posTmp).getBlock().isReplaceable(world, posTmp))
        {
            if (mode == Mode.EXTEND_AREA || mode == Mode.EXTEND_CONTINUOUS)
            {
                BlockPos posTgt = posTmp.offset(posIn.getFacing(), -1);

                IBlockState state = world.getBlockState(posTgt);
                Block block = state.getBlock();
                int meta = block.getMetaFromState(state);

                // The block on the back face must not be air and also it must not be fluid.
                // If the block type to work with is BLOCK_TYPE_TARGETED, then the block adjacent
                // to his position must match the targeted block.
                if (block.isAir(state, world, posTgt) == false && state.getMaterial().isLiquid() == false &&
                       (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && biBound != null) ||
                       (biTarget != null && biTarget.block == block && biTarget.blockMeta == meta)))
                {
                    positions.add(new BlockPosStateDist(posIn,
                                    this.getBlockInfoForBlockType(world, posTmp, posIn.getFacing(), blockType, biTarget, biBound)));
                }
                // Extend Continuous can't continue past air blocks on the back face
                else if (mode == Mode.EXTEND_CONTINUOUS)
                {
                    return null;
                }
            }
            else
            {
                positions.add(new BlockPosStateDist(posIn,
                            this.getBlockInfoForBlockType(world, posTmp, posIn.getFacing(), blockType, biTarget, biBound)));
            }
        }
        else if (continueThrough == false)
        {
            return null;
        }

        visited.add(posIn);

        for (BlockPosEU pos : PositionUtils.getAdjacentPositions(posIn, posIn.getFacing(), diagonals))
        {
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
                        continueTo = pos;
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

    private List<BlockPosStateDist> getPositionsOnCircle(ItemStack stack, World world, BlockPosEU posCenter, EnumFacing axisRight, EnumFacing axisUp)
    {
        int radius = new Area(this.getModeTag(stack, Mode.CIRCLE)).rPosH;
        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        List<BlockPos> branches = new ArrayList<BlockPos>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        boolean continueThrough = WandOption.CONTINUE_THROUGH.isEnabled(stack, Mode.CIRCLE);
        boolean diagonals = WandOption.ALLOW_DIAGONALS.isEnabled(stack, Mode.CIRCLE);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, posCenter.offset(posCenter.getFacing(), -1).toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = this.getSelectionIndex(stack);

        // Radius = 0 means only the targeted block position
        if (radius < 1)
        {
            positions.add(new BlockPosStateDist(posCenter, this.getBlockInfoForAdjacentBlock(world, posCenter.toBlockPos(), posCenter.getFacing())));
            return positions;
        }

        int counter = 0;
        int branchIndex = 0;
        BlockPos nextPos = null;
        BlockPos pos = null;//this.getCircleStartPosition(world, posCenter, radius, axisRight);

        while (counter <= 16641) // 129 * 129 area
        {
            nextPos = this.checkCirclePositionIgnoringSide(world, pos, visited, branches, positions, continueThrough, diagonals, blockType, biTarget, biBound);
            counter++;

            if (nextPos == null)
            {
                if (branchIndex < branches.size())
                {
                    pos = branches.get(branchIndex);
                    branchIndex++;
                }
                else
                {
                    break;
                }
            }
            else
            {
                pos = nextPos;
            }
        }
        System.out.printf("counter: %d\n", counter);
        return positions;
    }

    private BlockPos checkCirclePositionIgnoringSide(World world, BlockPos pos, Set<BlockPos> visited, List<BlockPos> branches,
            List<BlockPosStateDist> positions, boolean continueThrough, boolean diagonals, int blockType, BlockInfo biTarget, BlockInfo biBound)
    {
        return null;
    }

    private List<BlockPosStateDist> getReplacePositions(ItemStack stack, World world,
            BlockPosEU posStart, EnumFacing axisRight, EnumFacing axisUp)
    {
        Area area = new Area(this.getModeTag(stack, Mode.REPLACE));
        BlockPos pos1 = posStart.toBlockPos().offset(axisRight, -area.rNegH).offset(axisUp, -area.rNegV);
        BlockPos pos2 = posStart.toBlockPos().offset(axisRight,  area.rPosH).offset(axisUp,  area.rPosV);
        BlockPos posMin = PositionUtils.getMinCorner(pos1, pos2);
        BlockPos posMax = PositionUtils.getMaxCorner(pos1, pos2);
        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        List<BlockPos> branches = new ArrayList<BlockPos>();
        Set<BlockPos> visited = new HashSet<BlockPos>();
        boolean diagonals = WandOption.ALLOW_DIAGONALS.isEnabled(stack, Mode.REPLACE);
        BlockPos pos = posStart.toBlockPos();
        BlockInfo biTarget = BlockInfo.getBlockInfo(world, pos);
        BlockInfo biBound = this.getSelectedFixedBlockType(stack);

        if (biTarget == null || biBound == null)
        {
            return positions;
        }

        int counter = 0;
        int branchIndex = 0;
        BlockPos nextPos = null;

        while (counter <= 16641) // 129 * 129 area
        {
            nextPos = this.checkReplacePositionIgnoringSide(world, pos, posStart.getFacing(), posMin, posMax,
                    visited, branches, positions, diagonals, biTarget, biBound);
            counter++;

            if (nextPos == null)
            {
                if (branchIndex < branches.size())
                {
                    pos = branches.get(branchIndex);
                    branchIndex++;
                }
                else
                {
                    break;
                }
            }
            else
            {
                pos = nextPos;
            }
        }
        //System.out.printf("counter: %d\n", counter);
        return positions;
    }

    private BlockPos checkReplacePositionIgnoringSide(World world, BlockPos posIn, EnumFacing side, BlockPos posMin, BlockPos posMax,
            Set<BlockPos> visited, List<BlockPos> branches, List<BlockPosStateDist> positions, boolean diagonals, BlockInfo biTarget, BlockInfo biBound)
    {
        BlockPos pos = posIn;
        BlockPos continueTo = null;
        int sides = 0;

        if (visited.contains(pos) || PositionUtils.isPositionInsideArea(pos, posMin, posMax) == false)
        {
            return null;
        }

        IBlockState state = world.getBlockState(pos);

        // The block must be identical to the original targeted block
        if (state == biTarget.blockState)
        {
            BlockPos posAdj = pos.offset(side);
            IBlockState stateAdj = world.getBlockState(posAdj);
            Block blockAdj = stateAdj.getBlock();

            // The block must have a non-full block on the front face to be valid for replacing
            if (blockAdj.isAir(stateAdj, world, posAdj) || stateAdj.isSideSolid(world, posAdj, side.getOpposite()) == false)
            {
                positions.add(new BlockPosStateDist(posIn, world.provider.getDimension(), side, biBound));
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }

        visited.add(pos);

        for (BlockPos posTmp : PositionUtils.getAdjacentPositions(pos, side, diagonals))
        {
            if (visited.contains(posTmp) || PositionUtils.isPositionInsideArea(posTmp, posMin, posMax) == false)
            {
                continue;
            }

            if (world.getBlockState(posTmp) == biTarget.blockState)
            {
                if (visited.contains(posTmp) == false)
                {
                    if (sides == 0)
                    {
                        continueTo = posTmp;
                    }
                    else if (branches.contains(posTmp) == false)
                    {
                        branches.add(posTmp);
                    }
                }

                sides++;
            }
        }

        return continueTo;
    }

    private BlockInfo getBlockInfoForAdjacentBlock(World world, BlockPos pos, EnumFacing side)
    {
        return BlockInfo.getBlockInfo(world, pos.offset(side, -1));
    }

    private BlockInfo getBlockInfoForTargeted(ItemStack stack, World world, BlockPos pos)
    {
        int blockType = this.getSelectionIndex(stack);

        if (blockType == BLOCK_TYPE_TARGETED || blockType == BLOCK_TYPE_ADJACENT)
        {
            return BlockInfo.getBlockInfo(world, pos);
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
     * Get the actual block positions and block types for all other modes except Walls/Cube/Copy/Paste/Delete etc. area modes.
     */
    public void getBlockPositions(ItemStack stack, World world, EntityPlayer player, List<BlockPosStateDist> positions, BlockPosEU center)
    {
        BlockPosEU flippedCenter = center;
        EnumFacing side = center.getFacing();
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

        if (this.isAreaFlipped(stack))
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, side);
            axisRight = BlockPosEU.getRotation(axisRight, flipAxis);
            axisUp = BlockPosEU.getRotation(axisUp, flipAxis);

            if (flipAxis.getAxis() != center.getFacing().getAxis())
            {
                flippedCenter = new BlockPosEU(center.toBlockPos(), center.getDimension(), BlockPosEU.getRotation(center.getFacing(), flipAxis));
            }
            //System.out.printf("flipAxis: %s axisRight: %s axisUp: %s\n", flipAxis, axisRight, axisUp);
        }

        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, center.offset(side, -1).toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = this.getSelectionIndex(stack);
        Mode mode = Mode.getMode(stack);
        Area area = new Area(this.getModeTag(stack, mode));
        int dim = world.provider.getDimension();
        boolean continueThrough = WandOption.CONTINUE_THROUGH.isEnabled(stack, mode);
        BlockPos pos;

        switch (mode)
        {
            case COLUMN:
                for (int i = 0; i <= area.rPosH; i++)
                {
                    BlockPosEU posTmp = center.offset(side, i);
                    pos = posTmp.toBlockPos();

                    if (world.getBlockState(pos).getBlock().isReplaceable(world, pos))
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

                    if (world.getBlockState(posTmp).getBlock().isReplaceable(world, posTmp))
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, side,
                                        this.getBlockInfoForBlockType(world, posTmp, side, blockType, biTarget, biBound)));
                    }
                    else if (continueThrough == false)
                    {
                        break;
                    }
                }

                for (int i = -1; -i <= area.rNegH; i--)
                {
                    BlockPos posTmp = center.offset(axisRight, i).toBlockPos();

                    if (world.getBlockState(posTmp).getBlock().isReplaceable(world, posTmp))
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

            case CIRCLE:
                positions.addAll(this.getPositionsOnCircle(stack, world, flippedCenter, axisRight, axisUp));
                break;

            case PLANE:
            case EXTEND_AREA:
            case EXTEND_CONTINUOUS:
                positions.addAll(this.getPositionsOnPlane(stack, mode, world, flippedCenter, axisRight, axisUp));
                break;

            case REPLACE:
                if (WandOption.REPLACE_MODE_IS_AREA.isEnabled(stack, Mode.REPLACE))
                {
                    positions.addAll(this.getReplacePositions(stack, world, center, axisRight, axisUp));
                }
                else
                {
                    positions.add(new BlockPosStateDist(center, biBound));
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

        int startX = Math.min(pos1.getX(), pos2.getX());
        int startY = Math.min(pos1.getY(), pos2.getY());
        int startZ = Math.min(pos1.getZ(), pos2.getZ());

        int endX = Math.max(pos1.getX(), pos2.getX());
        int endY = Math.max(pos1.getY(), pos2.getY());
        int endZ = Math.max(pos1.getZ(), pos2.getZ());

        if (endX - startX > 128 || endY - startY > 128 || endZ - startZ > 128)
        {
            return;
        }

        BlockPosEU targeted = pos1.offset(pos1.getFacing(), -1);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted.toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = this.getSelectionIndex(stack);
        int dim = world.provider.getDimension();

        for (int x = startX; x <= endX; x++)
        {
            for (int y = startY; y <= endY; y++)
            {
                positions.add(new BlockPosStateDist(x, y, startZ, dim, targeted.getFacing(),
                                this.getBlockInfoForBlockType(world, new BlockPos(x, y, startZ), targeted.getFacing(), blockType, biTarget, biBound)));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int y = startY; y <= endY; y++)
            {
                positions.add(new BlockPosStateDist(x, y, endZ, dim, targeted.getFacing(),
                        this.getBlockInfoForBlockType(world, new BlockPos(x, y, endZ), targeted.getFacing(), blockType, biTarget, biBound)));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                positions.add(new BlockPosStateDist(x, startY, z, dim, targeted.getFacing(),
                                this.getBlockInfoForBlockType(world, new BlockPos(x, startY, z), targeted.getFacing(), blockType, biTarget, biBound)));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                positions.add(new BlockPosStateDist(x, endY, z, dim, targeted.getFacing(),
                                this.getBlockInfoForBlockType(world, new BlockPos(x, endY, z), targeted.getFacing(), blockType, biTarget, biBound)));
            }
        }

        for (int z = startZ + 1; z <= endZ - 1; z++)
        {
            for (int y = startY + 1; y <= endY - 1; y++)
            {
                positions.add(new BlockPosStateDist(startX, y, z, dim, targeted.getFacing(),
                                this.getBlockInfoForBlockType(world, new BlockPos(startX, y, z), targeted.getFacing(), blockType, biTarget, biBound)));
            }
        }

        for (int z = startZ + 1; z <= endZ - 1; z++)
        {
            for (int y = startY + 1; y <= endY - 1; y++)
            {
                positions.add(new BlockPosStateDist(endX, y, z, dim, targeted.getFacing(),
                                this.getBlockInfoForBlockType(world, new BlockPos(endX, y, z), targeted.getFacing(), blockType, biTarget, biBound)));
            }
        }
    }

    private void getBlockPositionsCube(ItemStack stack, World world, List<BlockPosStateDist> positions, BlockPosEU pos1, BlockPosEU pos2)
    {
        if (pos1 == null || pos2 == null)
        {
            return;
        }

        int startX = Math.min(pos1.getX(), pos2.getX());
        int startY = Math.min(pos1.getY(), pos2.getY());
        int startZ = Math.min(pos1.getZ(), pos2.getZ());

        int endX = Math.max(pos1.getX(), pos2.getX());
        int endY = Math.max(pos1.getY(), pos2.getY());
        int endZ = Math.max(pos1.getZ(), pos2.getZ());

        if (endX - startX > 128 || endY - startY > 128 || endZ - startZ > 128)
        {
            return;
        }

        BlockPosEU targeted = pos1.offset(pos1.getFacing(), -1);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted.toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
        int blockType = this.getSelectionIndex(stack);
        int dim = world.provider.getDimension();

        for (int y = startY; y <= endY; y++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                for (int x = startX; x <= endX; x++)
                {
                    positions.add(new BlockPosStateDist(x, y, z, dim, pos1.getFacing(),
                                    this.getBlockInfoForBlockType(world, new BlockPos(x, y, z), targeted.getFacing(), blockType, biTarget, biBound)));
                }
            }
        }
    }

    private EnumActionResult copyAreaToTemplate(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn, BlockPosEU posEndIn)
    {
        if (player.capabilities.isCreativeMode == false && Configs.buildersWandEnableCopyMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabledinsurvivalmode"), true);
            return EnumActionResult.FAIL;
        }

        if (posStartIn == null || posEndIn == null)
        {
            return EnumActionResult.FAIL;
        }

        BlockPos posStart = posStartIn.toBlockPos();
        BlockPos endOffset = posEndIn.toBlockPos().subtract(posStart);
        //System.out.printf("posStart: %s posEnd: %s endOffset: %s\n", posStart, posEndIn.toBlockPos(), endOffset);

        if (this.isAreaWithinSizeLimit(endOffset, stack, player) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge", this.getMaxAreaDimension(stack, player)), true);
            return EnumActionResult.FAIL;
        }

        int dim = world.provider.getDimension();

        if (posEndIn.getDimension() != dim || posStartIn.isWithinDistance(player, 160) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoofar"), true);
            return EnumActionResult.FAIL;
        }

        ResourceLocation templateLocation = this.getTemplateResource(stack, player);

        boolean success = this.saveAreaToTemplate(world, posStart, endOffset, templateLocation,
                this.getTemplateName(stack, Mode.COPY), player.getName(), WandOption.CHISELS_AND_BITS_CROSSWORLD.isEnabled(stack));

        if (success)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areasavedtotemplate", (this.getSelectionIndex(stack) + 1)), true);
        }
        else
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.failedtosaveareatotemplate"), true);
        }

        return EnumActionResult.SUCCESS;
    }

    /**
     * Saves an area of the world into a template.
     * The positions must not be null!
     */
    private boolean saveAreaToTemplate(World world, BlockPos posStart, BlockPos endOffset,
            ResourceLocation templateLocation, String templateName, String author, boolean cbCrossWorld)
    {
        TemplateManagerEU templateManager = this.getTemplateManager();
        TemplateEnderUtilities template = templateManager.getTemplate(templateLocation);
        template.takeBlocksFromWorld(world, posStart, endOffset, true, cbCrossWorld);
        template.setAuthor(author);

        boolean success = templateManager.writeTemplate(templateLocation);

        TemplateMetadata templateMeta = templateManager.getTemplateMetadata(templateLocation);
        EnumFacing facing = PositionUtils.getFacingFromPositions(posStart, posStart.add(endOffset));
        templateMeta.setValues(endOffset, facing, templateName, author);
        templateManager.writeTemplateMetadata(templateLocation);

        return success;
    }

    private EnumActionResult pasteAreaIntoWorld(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn)
    {
        if (posStartIn == null)
        {
            return EnumActionResult.FAIL;
        }

        if (player.capabilities.isCreativeMode == false && Configs.buildersWandEnablePasteMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabledinsurvivalmode"), true);
            return EnumActionResult.FAIL;
        }

        if (posStartIn.isWithinDistance(player, 160) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoofar"), true);
            return EnumActionResult.FAIL;
        }

        TemplateMetadata templateMeta = this.getTemplateMetadata(stack, player);

        if (this.isAreaWithinSizeLimit(templateMeta.getRelativeEndPosition(), stack, player) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge", this.getMaxAreaDimension(stack, player)), true);
            return EnumActionResult.FAIL;
        }

        PlacementSettings placement = this.getPasteModePlacement(stack, player);
        TemplateEnderUtilities template = this.getTemplate(world, player, stack, placement);

        if (player.capabilities.isCreativeMode)
        {
            template.setReplaceMode(WandOption.REPLACE_EXISTING.isEnabled(stack, Mode.PASTE) ? ReplaceMode.EVERYTHING : ReplaceMode.NOTHING);
            template.addBlocksToWorld(world, posStartIn.toBlockPos());
        }
        else
        {
            template.setReplaceMode(WandOption.REPLACE_EXISTING.isEnabled(stack, Mode.PASTE) ? ReplaceMode.WITH_NON_AIR : ReplaceMode.NOTHING);

            UUID wandUUID = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
            TaskTemplatePlaceBlocks task = new TaskTemplatePlaceBlocks(template, posStartIn.toBlockPos(), world.provider.getDimension(),
                    player.getUniqueID(), wandUUID, Configs.buildersWandBlocksPerTick);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult deleteArea(ItemStack stack, World world, EntityPlayer player, BlockPosEU posStartIn, BlockPosEU posEndIn)
    {
        if (posStartIn == null || posEndIn == null)
        {
            return EnumActionResult.PASS;
        }

        if (player.capabilities.isCreativeMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.creativeonly"), true);
            return EnumActionResult.FAIL;
        }

        this.deleteArea(stack, world, player, posStartIn.toBlockPos(), posEndIn.toBlockPos(),
                WandOption.AFFECT_ENTITIES.isEnabled(stack, Mode.DELETE));

        return EnumActionResult.SUCCESS;
    }

    private void deleteArea(ItemStack stack, World world, EntityPlayer player, BlockPos posStart, BlockPos posEnd, boolean removeEntities)
    {
        if (posStart == null || posEnd == null)
        {
            return;
        }

        if (player.getDistanceSq(posStart) > 160 * 160)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoofar"), true);
            return;
        }

        if (this.isAreaWithinSizeLimit(posStart.subtract(posEnd), stack, player) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolarge"), true);
            return;
        }

        // Set all blocks to air
        for (BlockPos.MutableBlockPos posMutable : BlockPos.getAllInBoxMutable(posStart, posEnd))
        {
            if (world.isAirBlock(posMutable) == false)
            {
                BlockUtils.setBlockToAirWithoutSpillingContents(world, posMutable, 2);
            }
        }

        // Remove pending block updates from within the area
        BlockPos posMin = PositionUtils.getMinCorner(posStart, posEnd);
        BlockPos posMax = PositionUtils.getMaxCorner(posStart, posEnd).add(1, 1, 1);
        StructureBoundingBox sbb = StructureBoundingBox.createProper(posMin.getX(), posMin.getY(), posMin.getZ(), posMax.getX(), posMax.getY(), posMax.getZ());
        world.getPendingBlockUpdates(sbb, true); // The boolean parameter indicates whether the entries will be removed

        // Remove all entities within the area
        int count = 0;

        if (removeEntities)
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
                if ((entity instanceof EntityPlayer) == false || entity instanceof FakePlayer)
                {
                    entity.setDead();
                    count++;
                }
            }

            if (count > 0)
            {
                player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.killedentitieswithcount", count), true);
            }
        }
    }

    private EnumActionResult moveArea(ItemStack stack, World world, EntityPlayer player, BlockPosEU posDst1EU, BlockPosEU posDst2EU)
    {
        if (player.capabilities.isCreativeMode == false && Configs.buildersWandEnableMoveMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabledinsurvivalmode"), true);
            return EnumActionResult.FAIL;
        }

        BlockPosEU posSrc1EU = this.getPosition(stack, Mode.MOVE_SRC, true);
        BlockPosEU posSrc2EU = this.getPosition(stack, Mode.MOVE_SRC, false);

        if (posSrc1EU == null || posSrc2EU == null || posDst1EU == null || posDst2EU == null)
        {
            return EnumActionResult.FAIL;
        }

        BlockPos posDst1 = posDst1EU.toBlockPos();
        BlockPos posDst2 = posDst2EU.toBlockPos();

        if (posSrc1EU.isWithinDistance(player, 160) == false ||
            posSrc2EU.isWithinDistance(player, 160) == false ||
            posDst1EU.isWithinDistance(player, 160) == false ||
            posDst2EU.isWithinDistance(player, 160) == false ||
            this.isAreaWithinSizeLimit(posDst2.subtract(posDst1), stack, player) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolargeortoofar"), true);
            return EnumActionResult.FAIL;
        }

        BlockPos posSrc1 = posSrc1EU.toBlockPos();
        BlockPos posSrc2 = posSrc2EU.toBlockPos();
        EnumFacing origFacing = PositionUtils.getFacingFromPositions(posSrc1, posSrc2);
        EnumFacing areaFacing = this.getAreaFacing(stack, Mode.MOVE_DST);

        if (areaFacing == null)
        {
            areaFacing = origFacing;
        }

        Rotation rotation = PositionUtils.getRotation(origFacing, areaFacing);
        Mirror mirror = this.getMirror(stack, Mode.MOVE_DST);

        // Don't do anything if the destination is exactly the same as the source
        if (posSrc1.equals(posDst1) && rotation == Rotation.NONE && mirror == Mirror.NONE)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.builderswand.areasarethesame"), true);
            return EnumActionResult.FAIL;
        }

        // Create "backup templates" of the source and destination areas, just in case...
        int id = this.getSelectionIndex(stack);
        UUID uuid = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
        String name = player.getName();

        ResourceLocation rl = new ResourceLocation(Reference.MOD_ID, "move_src_" + name + "_" + uuid.toString() + "_" + id);
        boolean success = this.saveAreaToTemplate(world, posSrc1, posSrc2.subtract(posSrc1), rl, "Move source", name, false);

        if (success == false)
        {
            return EnumActionResult.FAIL;
        }

        rl = new ResourceLocation(Reference.MOD_ID, "move_dst_" + name + "_" + uuid.toString() + "_" + id);
        success = this.saveAreaToTemplate(world, posDst1, posDst2.subtract(posDst1), rl, "Move destination", name, false);

        if (success == false)
        {
            return EnumActionResult.FAIL;
        }

        if (player.capabilities.isCreativeMode)
        {
            this.moveAreaImmediate(stack, world, player, posSrc1, posSrc2, posDst1, mirror, rotation);
        }
        else
        {
            UUID wandUUID = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
            TaskMoveArea task = new TaskMoveArea(world.provider.getDimension(), posSrc1, posSrc2, posDst1,
                    rotation, mirror, wandUUID, Configs.buildersWandBlocksPerTick);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    private void moveAreaImmediate(ItemStack stack, World world, EntityPlayer player, BlockPos posSrc1, BlockPos posSrc2, BlockPos posDst1,
            Mirror mirror, Rotation rotation)
    {
        PlacementSettings placement = new PlacementSettings();
        placement.setMirror(mirror);
        placement.setRotation(rotation);
        placement.setIgnoreEntities(false);
        placement.setReplacedBlock(Blocks.BARRIER); // meh

        ReplaceMode replace = WandOption.REPLACE_EXISTING.isEnabled(stack, Mode.MOVE_DST) ? ReplaceMode.EVERYTHING : ReplaceMode.NOTHING;
        TemplateEnderUtilities template = new TemplateEnderUtilities(placement, replace);
        template.takeBlocksFromWorld(world, posSrc1, posSrc2.subtract(posSrc1), true, false);
        this.deleteArea(stack, world, player, posSrc1, posSrc2, true);
        template.addBlocksToWorld(world, posDst1);
    }

    private EnumActionResult replaceBlocks(ItemStack stack, World world, EntityPlayer player, BlockPosEU center)
    {
        if (player.capabilities.isCreativeMode == false && Configs.buildersWandEnableReplaceMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabledinsurvivalmode"), true);
            return EnumActionResult.FAIL;
        }

        BlockInfo biBound = getSelectedFixedBlockType(stack);

        if (biBound == null || biBound.blockState == world.getBlockState(center.toBlockPos()))
        {
            return EnumActionResult.FAIL;
        }

        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        this.getBlockPositions(stack, world, player, positions, center);

        UUID wandUUID = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
        TaskReplaceBlocks task = new TaskReplaceBlocks(world, wandUUID, positions, Configs.buildersWandReplaceBlocksPerTick);
        PlayerTaskScheduler.getInstance().addTask(player, task, 1);

        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult replaceBlocks3D(ItemStack stack, World world, EntityPlayer player)
    {
        if (player.capabilities.isCreativeMode == false && Configs.buildersWandEnableReplace3DMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabledinsurvivalmode"), true);
            return EnumActionResult.FAIL;
        }

        BlockPosEU pos1 = this.getPosition(stack, true);
        BlockPosEU pos2 = this.getPosition(stack, false);

        if (pos1 == null || pos2 == null)
        {
            return EnumActionResult.FAIL;
        }

        BlockInfo blockInfoTarget = getSelectedFixedBlockType(stack, true);
        BlockInfo blockInfoReplacement = getSelectedFixedBlockType(stack, false);

        if (blockInfoTarget != null && blockInfoReplacement != null)
        {
            UUID wandUUID = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
            TaskReplaceBlocks3D task = new TaskReplaceBlocks3D(world, wandUUID, pos1, pos2, blockInfoTarget.blockState, blockInfoReplacement, 5);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }

    private boolean isStackedAreaWithinLimits(BlockPos pos1, BlockPos pos2, BlockPos endPosRelative, Area3D area, EntityPlayer player)
    {
        int sx = Math.abs(endPosRelative.getX()) + 1;
        int sy = Math.abs(endPosRelative.getY()) + 1;
        int sz = Math.abs(endPosRelative.getZ()) + 1;
        BlockPos posMin = PositionUtils.getMinCorner(pos1, pos2).add(-area.getXNeg() * sx, -area.getYNeg() * sy, -area.getZNeg() * sz);
        BlockPos posMax = PositionUtils.getMaxCorner(pos1, pos2).add( area.getXPos() * sx,  area.getYPos() * sy,  area.getZPos() * sz);
        int max = 160;

        if (player.getDistanceSq(posMin) > (max * max) || player.getDistanceSq(posMax) > (max * max))
        {
            return false;
        }

        return true;
    }

    private EnumActionResult stackArea(ItemStack stack, World world, EntityPlayer player, BlockPosEU pos1EU, BlockPosEU pos2EU)
    {
        if (player.capabilities.isCreativeMode == false && Configs.buildersWandEnableStackMode == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.featuredisabledinsurvivalmode"), true);
            return EnumActionResult.FAIL;
        }

        if (pos1EU == null || pos2EU == null)
        {
            return EnumActionResult.FAIL;
        }

        int dim = world.provider.getDimension();
        BlockPos pos1 = pos1EU.toBlockPos();
        BlockPos pos2 = pos2EU.toBlockPos();
        BlockPos endPosRelative = pos2.subtract(pos1);
        Area3D area = Area3D.getAreaFromNBT(this.getAreaTag(stack));

        if (pos1EU.getDimension() != dim || pos2EU.getDimension() != dim ||
            this.isStackedAreaWithinLimits(pos1, pos2, endPosRelative, area, player) == false)
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.areatoolargeortoofar"), true);
            return EnumActionResult.FAIL;
        }

        boolean takeEntities = player.capabilities.isCreativeMode && WandOption.AFFECT_ENTITIES.isEnabled(stack);
        PlacementSettings placement = new PlacementSettings();
        placement.setIgnoreEntities(takeEntities == false);
        ReplaceMode replaceMode = WandOption.REPLACE_EXISTING.isEnabled(stack, Mode.STACK) ? ReplaceMode.WITH_NON_AIR : ReplaceMode.NOTHING;
        TemplateEnderUtilities template = new TemplateEnderUtilities(placement, replaceMode);
        template.takeBlocksFromWorld(world, pos1, pos2.subtract(pos1), takeEntities, false);

        if (player.capabilities.isCreativeMode)
        {
            this.stackAreaImmediate(world, pos1, endPosRelative, area, template);
        }
        else
        {
            UUID wandUUID = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
            TaskStackArea task = new TaskStackArea(world, wandUUID, pos1, endPosRelative, template, area, Configs.buildersWandBlocksPerTick);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    private void stackAreaImmediate(World world, BlockPos posOrig, BlockPos endPosRelative, Area3D area, TemplateEnderUtilities template)
    {
        int sx = Math.abs(endPosRelative.getX()) + 1;
        int sy = Math.abs(endPosRelative.getY()) + 1;
        int sz = Math.abs(endPosRelative.getZ()) + 1;

        for (int y = -area.getYNeg(); y <= area.getYPos(); y++)
        {
            for (int x = -area.getXNeg(); x <= area.getXPos(); x++)
            {
                for (int z = -area.getZNeg(); z <= area.getZPos(); z++)
                {
                    // Don't try to stack over the original area at offset 0, 0, 0
                    if (x != 0 || y != 0 || z != 0)
                    {
                        template.addBlocksToWorld(world, posOrig.add(x * sx, y * sy, z * sz));
                    }
                }
            }
        }
    }

    private void placeHelperBlock(EntityPlayer player)
    {
        BlockPos pos = PositionUtils.getPositionInfrontOfEntity(player);
        //player.worldObj.setBlockState(pos, Blocks.RED_MUSHROOM_BLOCK.getDefaultState(), 3);
        player.getEntityWorld().setBlockState(pos, Blocks.LEAVES.getDefaultState()
                .withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE)
                .withProperty(BlockLeaves.CHECK_DECAY, false)
                .withProperty(BlockLeaves.DECAYABLE, true), 3);
    }

    private int getMaxAreaDimension(ItemStack stack, EntityPlayer player)
    {
        return player.capabilities.isCreativeMode ? 256 : 64;
    }

    private boolean isAreaWithinSizeLimit(BlockPos size, ItemStack stack, EntityPlayer player)
    {
        int limit = this.getMaxAreaDimension(stack, player);
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
        int id = this.getSelectionIndex(stack);
        UUID uuid = NBTUtils.getUUIDFromItemStack(stack, WRAPPER_TAG_NAME, true);
        String name = NBTUtils.getOrCreateString(stack, WRAPPER_TAG_NAME, "player", player.getName());
        return new ResourceLocation(Reference.MOD_ID, name + "_" + uuid.toString() + "_" + id);
    }

    @Nullable
    private TemplateManagerEU getTemplateManager()
    {
        File saveDir = DimensionManager.getCurrentSaveRootDirectory();

        if (saveDir != null)
        {
            return new TemplateManagerEU(new File(new File(saveDir, Reference.MOD_ID), this.name), FMLCommonHandler.instance().getDataFixer());
        }

        return null;
    }

    @Nullable
    private NBTTagCompound getSelectedTemplateTag(ItemStack stack, Mode mode, boolean create)
    {
        int sel = this.getSelectionIndex(stack);
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

            this.setTemplateNameOnItem(stack, Mode.COPY, meta.getTemplateName());
        }
    }

    public EnumFacing getTemplateFacing(ItemStack stack)
    {
        NBTTagCompound tag = this.getSelectedTemplateTag(stack, Mode.PASTE, true);
        return EnumFacing.byIndex(tag.getByte("TemplateFacing"));
    }

    public EnumFacing getAreaFacing(ItemStack stack, Mode mode)
    {
        NBTTagCompound tag = this.getSelectedTemplateTag(stack, mode, true);

        if (tag.hasKey("Rotation", Constants.NBT.TAG_BYTE))
        {
            return EnumFacing.byIndex(tag.getByte("Rotation"));
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

    private PlacementSettings getPasteModePlacement(ItemStack stack, EntityPlayer player)
    {
        EnumFacing facing = this.getTemplateFacing(stack);
        EnumFacing areaFacing = this.getAreaFacing(stack, Mode.PASTE);

        if (areaFacing == null)
        {
            areaFacing = facing;
        }

        Rotation rotation = PositionUtils.getRotation(facing, areaFacing);
        PlacementSettings placement = new PlacementSettings();
        boolean ignoreEntities = player.capabilities.isCreativeMode == false ||
                WandOption.AFFECT_ENTITIES.isEnabled(stack, Mode.PASTE) == false;
        placement.setMirror(this.getMirror(stack));
        placement.setRotation(rotation);
        placement.setIgnoreEntities(ignoreEntities);

        return placement;
    }

    @Override
    public boolean doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (key == HotKeys.KEYCODE_CUSTOM_1)
        {
            this.placeHelperBlock(player);
            return true;
        }

        Mode mode = Mode.getMode(stack);

        // Alt + Shift + Scroll: Change the secondary selected block type or stack grid size
        if (EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            if (mode == Mode.REPLACE_3D)
            {
                this.changeSecondarySelectionIndex(stack, EnumKey.keypressActionIsReversed(key));
                return true;
            }
            else if (mode == Mode.STACK)
            {
                this.changeAreaDimensions(player, stack, EnumKey.keypressActionIsReversed(key));
                return true;
            }
        }
        // Alt + Scroll: Change the selected block type
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_ALT))
        {
            this.changeSelectionIndex(stack, EnumKey.keypressActionIsReversed(key));

            if (mode == Mode.PASTE)
            {
                this.updateTemplateMetadata(stack, player);
            }
            return true;
        }
        // Shift + Scroll: Change the dimensions of the current mode
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT))
        {
            if (mode != Mode.PASTE && mode != Mode.MOVE_DST)
            {
                if (mode.hasTwoPlacableCorners())
                {
                    this.moveEndPosition(stack, player, EnumKey.keypressActionIsReversed(key));
                }
                else
                {
                    this.changeAreaDimensions(player, stack, EnumKey.keypressActionIsReversed(key));
                }
                return true;
            }
        }
        // Shift + Toggle key: Toggle the mirroring in the appropriate modes
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            if (mode == Mode.REPLACE_3D)
            {
                WandOption.BIND_MODE.toggle(stack, mode);
                return true;
            }
            else if (mode.isAreaMode())
            {
                this.toggleMirror(stack, mode, player);
                return true;
            }
            else if (mode != Mode.WALLS || mode != Mode.CUBE)
            {
                WandOption.MOVE_POSITION.toggle(stack, mode);
                return true;
            }
        }
        // Ctrl + Toggle key: Cycle the mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            Mode.cycleMode(stack, EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key), player);

            if (Mode.getMode(stack) == Mode.PASTE)
            {
                this.updateTemplateMetadata(stack, player);
            }
            return true;
        }
        // Ctrl + Alt + Shift + Scroll: Change the selected link crystal
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT_CTRL_ALT))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, EnumKey.keypressActionIsReversed(key));
            return true;
        }
        // Ctrl + Alt + Shift + Toggle
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_CTRL_ALT))
        {
            if (mode == Mode.COPY)
            {
                WandOption.CHISELS_AND_BITS_CROSSWORLD.toggle(stack, mode);
                return true;
            }
        }
        // Ctrl + Alt + Toggle key: Toggle some mode-specific features
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL_ALT))
        {
            if (mode == Mode.PASTE || mode == Mode.MOVE_DST || mode == Mode.STACK)
            {
                WandOption.REPLACE_EXISTING.toggle(stack, mode);
                return true;
            }
            else if (mode == Mode.DELETE)
            {
                WandOption.AFFECT_ENTITIES.toggle(stack, mode);
                return true;
            }
            else if (mode == Mode.EXTEND_CONTINUOUS || mode == Mode.REPLACE)
            {
                WandOption.ALLOW_DIAGONALS.toggle(stack, mode);
                return true;
            }
            else if (mode == Mode.COLUMN || mode == Mode.LINE || mode == Mode.PLANE || mode == Mode.EXTEND_AREA)
            {
                WandOption.CONTINUE_THROUGH.toggle(stack, mode);
                return true;
            }
        }
        // Alt + Shift + Toggle key: Toggle ghost blocks
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            if (mode == Mode.PASTE || mode == Mode.STACK)
            {
                WandOption.AFFECT_ENTITIES.toggle(stack, mode);
                return true;
            }
            else if (mode.isAreaMode() == false)
            {
                WandOption.RENDER_GHOST.toggle(stack, mode);
                return true;
            }
        }
        // Just Toggle key: Toggle the area flipped property
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            if (mode == Mode.REPLACE)
            {
                WandOption.REPLACE_MODE_IS_AREA.toggle(stack, mode);
                return true;
            }
            else if (mode.isAreaMode() && mode != Mode.COPY)
            {
                this.setAreaFacing(stack, mode, player);
                return true;
            }
            else if (mode.isAreaMode() == false)
            {
                this.toggleAreaFlipped(stack, player);
                return true;
            }
        }
        // Alt + Toggle: FIXME Temporary key for toggling replace on build modes
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_ALT))
        {
            if (mode == Mode.COLUMN || mode == Mode.LINE || mode == Mode.PLANE || mode == Mode.EXTEND_AREA)
            {
                WandOption.REPLACE_EXISTING.toggle(stack, mode);
                return true;
            }
        }

        return false;
    }

    @Override
    public void handleString(EntityPlayer player, ItemStack stack, String text)
    {
        if (stack.isEmpty() == false)
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
        if (moduleStack.isEmpty() || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Only allow the "Miscellaneous" type Memory Cards
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) && imodule.getModuleTier(moduleStack) != ItemLinkCrystal.TYPE_BLOCK)
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
                this.rPosV = MathHelper.clamp(this.rPosV + amount, 0, this.maxRadius);
            }
            else if (facing == upAxis.getOpposite())
            {
                this.rNegV = MathHelper.clamp(this.rNegV + amount, 0, this.maxRadius);
            }
            else if (facing == rightAxis)
            {
                this.rPosH = MathHelper.clamp(this.rPosH + amount, 0, this.maxRadius);
            }
            else if (facing == rightAxis.getOpposite())
            {
                this.rNegH = MathHelper.clamp(this.rNegH + amount, 0, this.maxRadius);
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

    public static class Area3D
    {
        private final MutableBlockPos pos;
        private final MutableBlockPos neg;
        private int maxSize = 64;

        public Area3D()
        {
            this(0, 0, 0, 0, 0, 0);
        }

        public Area3D(int xp, int yp, int zp, int xn, int yn, int zn)
        {
            this.pos = new MutableBlockPos(xp, yp, zp);
            this.neg = new MutableBlockPos(xn, yn, zn);
        }

        private Area3D(NBTTagCompound tag)
        {
            this.pos = new MutableBlockPos(0, 0, 0);
            this.neg = new MutableBlockPos(0, 0, 0);

            this.readFromNBT(tag);
        }

        public static Area3D getAreaFromNBT(NBTTagCompound tag)
        {
            return new Area3D(tag);
        }

        public Area3D setMaxSize(int max)
        {
            this.maxSize = max;
            return this;
        }

        public int getXPos()
        {
            return this.pos.getX();
        }

        public int getYPos()
        {
            return this.pos.getY();
        }

        public int getZPos()
        {
            return this.pos.getZ();
        }

        public int getXNeg()
        {
            return this.neg.getX();
        }

        public int getYNeg()
        {
            return this.neg.getY();
        }

        public int getZNeg()
        {
            return this.neg.getZ();
        }

        public Area3D adjustArea(EnumFacing facing, int amount)
        {
            if (facing.getAxisDirection() == AxisDirection.POSITIVE)
            {
                this.pos.move(facing, amount);
                this.clampBounds(this.pos);
            }
            else
            {
                this.neg.move(facing, -amount);
                this.clampBounds(this.neg);
            }

            return this;
        }

        private int getPacked(BlockPos pos)
        {
            return (pos.getX() & 0xFF) << 16 | (pos.getY() & 0xFF) << 8 | (pos.getZ() & 0xFF);
        }

        private MutableBlockPos clampBounds(MutableBlockPos bounds)
        {
            int x = MathHelper.clamp(bounds.getX(), 0, this.maxSize);
            int y = MathHelper.clamp(bounds.getY(), 0, this.maxSize);
            int z = MathHelper.clamp(bounds.getZ(), 0, this.maxSize);

            return bounds.setPos(x, y, z);
        }

        private void setFromPacked(MutableBlockPos bounds, int packed)
        {
            int x = MathHelper.clamp((packed >> 16) & 0xFF, 0, this.maxSize);
            int y = MathHelper.clamp((packed >>  8) & 0xFF, 0, this.maxSize);
            int z = MathHelper.clamp(packed         & 0xFF, 0, this.maxSize);

            bounds.setPos(x, y, z);
        }

        public void readFromNBT(NBTTagCompound tag)
        {
            int packed = tag.getInteger("DimPos");
            this.setFromPacked(this.pos, packed);

            packed = tag.getInteger("DimNeg");
            this.setFromPacked(this.neg, packed);
        }

        public void writeToNBT(NBTTagCompound tag)
        {
            tag.setInteger("DimPos", this.getPacked(this.pos));
            tag.setInteger("DimNeg", this.getPacked(this.neg));
        }
    }

    public static enum WandOption
    {
        AFFECT_ENTITIES,
        ALLOW_DIAGONALS,
        AREA_FLIPPED,
        BIND_MODE,
        CHISELS_AND_BITS_CROSSWORLD,
        CONTINUE_THROUGH,
        MIRRORED,
        MOVE_POSITION,
        RENDER_GHOST,
        REPLACE_EXISTING,
        REPLACE_MODE_IS_AREA;

        public boolean isEnabled(ItemStack stack)
        {
            return this.isEnabled(stack, Mode.getMode(stack));
        }

        public boolean isEnabled(ItemStack stack, Mode mode)
        {
            if (stack.getItem() instanceof ItemBuildersWand)
            {
                NBTTagCompound tag = ((ItemBuildersWand) stack.getItem()).getModeTag(stack, mode);
                return (tag.getInteger("Modes") & (1 << this.ordinal())) != 0;
            }

            return false;
        }

        public void toggle(ItemStack stack)
        {
            this.toggle(stack, Mode.getMode(stack));
        }

        public void toggle(ItemStack stack, Mode mode)
        {
            if (stack.getItem() instanceof ItemBuildersWand)
            {
                NBTTagCompound tag = ((ItemBuildersWand) stack.getItem()).getModeTag(stack, mode);
                tag.setInteger("Modes", (tag.getInteger("Modes") ^ (1 << this.ordinal())));
            }
        }
    }

    public static enum Mode
    {
        EXTEND_CONTINUOUS   ("extcont", "enderutilities.tooltip.item.build.extend.continuous"),
        EXTEND_AREA         ("extarea", "enderutilities.tooltip.item.build.extend.area"),
        COLUMN              ("column",  "enderutilities.tooltip.item.build.column"),
        LINE                ("line",    "enderutilities.tooltip.item.build.line"),
        PLANE               ("plane",   "enderutilities.tooltip.item.build.plane"),
        CIRCLE              ("circle",  "enderutilities.tooltip.item.build.circle"),
        WALLS               ("walls",   "enderutilities.tooltip.item.build.walls", false, true, true),
        CUBE                ("cube",    "enderutilities.tooltip.item.build.cube", false, true, true),
        REPLACE             ("replace", "enderutilities.tooltip.item.build.replace"),
        REPLACE_3D          ("replace3d", "enderutilities.tooltip.item.build.replace.3d", true, true, true),
        MOVE_SRC            ("movesrc", "enderutilities.tooltip.item.build.move.source", true, true, false),
        MOVE_DST            ("movedst", "enderutilities.tooltip.item.build.move.destination", true, false, true),
        COPY                ("copy",    "enderutilities.tooltip.item.build.copy", true, true, true),
        PASTE               ("paste",   "enderutilities.tooltip.item.build.paste", true, false, true),
        STACK               ("stack",   "enderutilities.tooltip.item.build.stack", true, true, true),
        // Creative-only modes are at the end.
        DELETE              ("delete",  "enderutilities.tooltip.item.build.delete", true, true, true, true);

        private final String name;
        private final String unlocName;
        private final boolean isAreaMode;
        private final boolean hasTwoCorners;
        private final boolean hasUseDelay;
        private boolean creativeOnly;
        private static int numCreativeOnly;

        static
        {
            for (Mode mode : values())
            {
                if (mode.creativeOnly)
                {
                    numCreativeOnly++;
                }
            }
        }

        private Mode (String name, String unlocName)
        {
            this(name, unlocName, false, false, false);
        }

        private Mode (String name, String unlocName, boolean isAreaMode, boolean twoCorners, boolean useDelay)
        {
            this(name, unlocName, isAreaMode, twoCorners, useDelay, false);
        }

        private Mode (String name, String unlocName, boolean isAreaMode, boolean twoCorners, boolean useDelay, boolean creativeOnly)
        {
            this.name = name;
            this.unlocName = unlocName;
            this.isAreaMode = isAreaMode;
            this.hasTwoCorners = twoCorners;
            this.hasUseDelay = useDelay;
            this.creativeOnly = creativeOnly;
        }

        public String getName()
        {
            return this.name;
        }

        public String getDisplayName()
        {
            return I18n.format(this.unlocName);
        }

        public boolean isAreaMode()
        {
            return this.isAreaMode;
        }

        public boolean hasTwoPlacableCorners()
        {
            return this.hasTwoCorners;
        }

        public boolean hasUseDelay()
        {
            return this.hasUseDelay;
        }

        public static Mode getMode(ItemStack stack)
        {
            return values()[getModeOrdinal(stack)];
        }

        public static void cycleMode(ItemStack stack, boolean reverse, EntityPlayer player)
        {
            int max = player.capabilities.isCreativeMode ? values().length - 1 : values().length - 1 - numCreativeOnly;
            NBTUtils.cycleByteValue(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE, max, reverse);
        }

        public static int getModeOrdinal(ItemStack stack)
        {
            int id = NBTUtils.getByte(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE);
            return (id >= 0 && id < values().length) ? id : 0;
        }

        public static int getModeCount(EntityPlayer player)
        {
            return player.capabilities.isCreativeMode ? values().length : values().length - numCreativeOnly;
        }
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "usetime"), new IItemPropertyGetter()
        {
            @Override
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                if (entityIn == null)
                {
                    return 0.0F;
                }
                else
                {
                    stack = entityIn.getActiveItemStack();

                    if (stack.isEmpty() == false)
                    {
                        return (float)(stack.getMaxItemUseDuration() - entityIn.getItemInUseCount()) / 50.0F;
                    }

                    return 0f;
                }
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "inuse"), new IItemPropertyGetter()
        {
            @Override
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }
}
