package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.event.tasks.TaskBuildersWand;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.BlockInfo;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.BlockPosStateDist;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.EntityUtils.LeftRight;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemBuildersWand extends ItemLocationBoundModular
{
    /** How much Ender Charge does placing each block cost */
    public static final int ENDER_CHARGE_COST = 10;
    /** Max number of stored block types */
    public static final int MAX_BLOCKS = 6;
    public static final String WRAPPER_TAG_NAME = "BuildersWand";
    public static final String TAG_NAME_MODE = "Mode";
    public static final String TAG_NAME_CONFIGS = "Configs";
    public static final String TAG_NAME_CONFIG_PRE = "Mode_";
    public static final String TAG_NAME_DIMENSIONS = "Dim";
    public static final String TAG_NAME_BLOCKS = "Blocks";
    public static final String TAG_NAME_BLOCK_PRE = "Block_";
    public static final String TAG_NAME_BLOCK_SEL = "SelBlock";
    public static final String TAG_NAME_ALLOW_DIAGONALS ="Diag";
    public static final String TAG_NAME_GHOST_BLOCKS ="Ghost";
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
            else if (mode != Mode.CUBE && mode != Mode.WALLS)
            {
                EnumActionResult result = this.useWand(stack, world, player, pos);
                return new ActionResult<ItemStack>(result, stack);
            }
        }

        if (mode == Mode.CUBE || mode == Mode.WALLS)
        {
            if (this.getPosition(stack, POS_END) != null)
            {
                player.setActiveHand(hand);
            }
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
        if (mode == Mode.CUBE || mode == Mode.WALLS)
        {
            if (world.isRemote == false && player.isSneaking() == false)
            {
                this.setPosition(stack, new BlockPosEU(pos, player.dimension, side), POS_END);
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
                this.setPosition(stack, new BlockPosEU(pos, player.dimension, side), POS_START);
            }
            // Sneak + left click: Set the selected block type
            else
            {
                this.setSelectedFixedBlockType(stack, world, pos);
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
                player.worldObj.playSound(player, player.getPosition(), SoundEvents.entity_endermen_teleport, SoundCategory.MASTER, 0.4f, 0.7f);
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
        String itemName = I18n.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
        if (stack.getTagCompound() == null)
        {
            return itemName;
        }

        String preBT = TextFormatting.AQUA.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        if (itemName.length() >= 14)
        {
            itemName = EUStringUtils.getInitialsWithDots(itemName);
        }
        itemName = itemName + " M: " + preGreen + Mode.getMode(stack).getDisplayName() + rst;

        if (this.getAreaFlipped(stack) == true)
        {
            String strFlip = this.getAreaFlipAxis(stack, EnumFacing.NORTH).toString().substring(0, 1);
            itemName = itemName + " F: " + preGreen + strFlip + rst;
        }
        else
        {
            itemName = itemName + " F: " + TextFormatting.RED + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
        }

        int sel = getSelectedBlockType(stack);
        if (sel >= 0)
        {
            BlockInfo blockInfo = getSelectedFixedBlockType(stack);
            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(blockInfo.block, 1, blockInfo.itemMeta);
                if (blockStack != null && blockStack.getItem() != null)
                {
                    itemName = itemName + " B: " + preGreen + blockStack.getDisplayName() + rst;
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

            itemName = itemName + " B: " + preBT + str + rst;
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
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        Mode mode = Mode.getMode(stack);
        list.add(I18n.translateToLocal("enderutilities.tooltip.item.mode") + ": " + pre + mode.getDisplayName() + rst);

        int sel = getSelectedBlockType(stack);
        if (sel >= 0)
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
        if (this.getAreaFlipped(stack) == true)
        {
            str2 = TextFormatting.GREEN + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
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
                str2 = TextFormatting.GREEN + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
            }
            else
            {
                str2 = TextFormatting.RED + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
            }
            list.add(str + ": " + str2 + rst);
        }

        str = I18n.translateToLocal("enderutilities.tooltip.item.builderswand.renderghostblocks");
        if (NBTUtils.getBoolean(stack, ItemBuildersWand.WRAPPER_TAG_NAME, ItemBuildersWand.TAG_NAME_GHOST_BLOCKS) == true)
        {
            str2 = TextFormatting.GREEN + I18n.translateToLocal("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str2 = TextFormatting.RED + I18n.translateToLocal("enderutilities.tooltip.item.no") + rst;
        }
        list.add(str + ": " + str2 + rst);

        super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
    }

    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public BlockPosEU getPosition(ItemStack stack, boolean isStart)
    {
        String tagName = isStart == true ? "Pos1" : "Pos2";
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, tagName, false);
        if (tag != null)
        {
            return BlockPosEU.readFromTag(tag);
        }

        return null;
    }

    public void setPosition(ItemStack stack, BlockPosEU pos, boolean isStart)
    {
        String tagName = isStart == true ? "Pos1" : "Pos2";
        NBTTagCompound nbt = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, true);
        if (nbt != null && nbt.hasKey(tagName, Constants.NBT.TAG_COMPOUND) == true)
        {
            BlockPosEU oldPos = BlockPosEU.readFromTag(nbt.getCompoundTag(tagName));
            if (oldPos != null && oldPos.equals(pos) == true)
            {
                nbt.removeTag(tagName);
            }
            else
            {
                nbt.setTag(tagName, pos.writeToTag(new NBTTagCompound()));
            }
        }
        else
        {
            NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, tagName, true);
            pos.writeToTag(tag);
        }
    }

    public EnumActionResult useWand(ItemStack stack, World world, EntityPlayer player, BlockPosEU targetPos)
    {
        if (player.dimension != targetPos.dimension)
        {
            return EnumActionResult.FAIL;
        }

        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();
        BlockPosEU posStart = this.getPosition(stack, POS_START);
        BlockPosEU posEnd = this.getPosition(stack, POS_END);

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.CUBE)
        {
            posStart = posStart != null ? posStart.offset(posStart.side, 1) : null;
            posEnd = posEnd != null ? posEnd.offset(posEnd.side, 1) : null;
            this.getBlockPositionsCube(stack, targetPos, world, positions, posStart, posEnd);
        }
        else if (mode == Mode.WALLS)
        {
            posStart = posStart != null ? posStart.offset(posStart.side, 1) : null;
            posEnd = posEnd != null ? posEnd.offset(posEnd.side, 1) : null;
            this.getBlockPositionsWalls(stack, targetPos, world, positions, posStart, posEnd);
        }
        else
        {
            if (posStart != null)
            {
                this.getBlockPositions(stack, posStart.toBlockPos(), posStart.side, world, positions);
            }
            else
            {
                this.getBlockPositions(stack, targetPos.toBlockPos(), targetPos.side, world, positions);
            }
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
                this.setPosition(stack, pos.offset(targetPos.side, 1), POS_START);
            }
        }
        else
        {
            TaskBuildersWand task = new TaskBuildersWand(world, player.getUniqueID(), positions, Configs.valueBuildersWandBlocksPerTick);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return EnumActionResult.SUCCESS;
    }

    public static boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player, BlockPosStateDist pos)
    {
        if (world.isAirBlock(pos.toBlockPos()) == false)
        {
            return false;
        }

        if (UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, false) == false)
        {
            return false;
        }

        BlockInfo blockInfo = null;

        if (getSelectedBlockType(wandStack) == BLOCK_TYPE_ADJACENT)
        {
            blockInfo = getBlockInfoForAdjacentBlock(world, pos.toBlockPos(), pos.side);
        }
        else
        {
            blockInfo = pos.blockInfo;
        }

        // FIXME 1.9: check the position and the block state stuff and fix the isLiquid check
        //IBlockState state = world.getBlockState(pos.toBlockPos());
        //if (blockInfo == null || world.isAirBlock(pos.toBlockPos()) == true || blockInfo.block.getMaterial().isLiquid() == true)
        if (blockInfo == null || world.isAirBlock(pos.toBlockPos()) == true)
        {
            return false;
        }

        Block block = blockInfo.block;
        int itemMeta = blockInfo.itemMeta;

        if (player.capabilities.isCreativeMode == true)
        {
            ItemStack targetStack = new ItemStack(block, 1, itemMeta);
            if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
            {
                // Check if we can place the block
                if (BlockUtils.checkCanPlaceBlockAt(world, pos.toBlockPos(), pos.side, player, targetStack) == true)
                {
                    if (ForgeHooks.onPlaceItemIntoWorld(targetStack, player, world, pos.toBlockPos(), pos.side, 0.5f, 0.5f, 0.5f, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS)
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            ItemStack templateStack = new ItemStack(block, 1, itemMeta);
            IItemHandler inv = getInventoryWithItems(wandStack, templateStack, player);
            ItemStack targetStack = getItemToBuildWith(inv, templateStack, 1);

            if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
            {
                // Check if we can place the block
                if (BlockUtils.checkCanPlaceBlockAt(world, pos.toBlockPos(), pos.side, player, targetStack) == false ||
                    ForgeHooks.onPlaceItemIntoWorld(targetStack, player, world, pos.toBlockPos(), pos.side, 0.5f, 0.5f, 0.5f, EnumHand.MAIN_HAND) != EnumActionResult.SUCCESS)
                {
                    targetStack = InventoryUtils.tryInsertItemStackToInventory(inv, targetStack);
                    if (targetStack != null)
                    {
                        EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, targetStack);
                        world.spawnEntityInWorld(item);
                    }
                }
                else
                {
                    UtilItemModular.useEnderCharge(wandStack, ENDER_CHARGE_COST, true);
                    return true;
                }
            }
        }

        return false;
    }

    public static IItemHandler getInventoryWithItems(ItemStack wandStack, ItemStack templateStack, EntityPlayer player)
    {
        IItemHandler inv = new PlayerMainInvWrapper(player.inventory);
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

    public void setSelectedFixedBlockType(ItemStack stack, World world, BlockPos pos)
    {
        int sel = getSelectedBlockType(stack);
        if (sel < 0)
        {
            return;
        }

        NBTTagCompound blocksTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCKS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(blocksTag, TAG_NAME_BLOCK_PRE + sel, true);

        IBlockState state = world.getBlockState(pos);
        tag.setString("BlockName", Block.blockRegistry.getNameForObject(state.getBlock()).toString());
        tag.setByte("BlockMeta", (byte)state.getBlock().getMetaFromState(state));

        @SuppressWarnings("deprecation")
        ItemStack stackTmp = state.getBlock().getItem(world, pos, state);
        int meta = stackTmp != null ? stackTmp.getMetadata() : 0;
        tag.setByte("ItemMeta", (byte)meta);
    }

    public static BlockInfo getSelectedFixedBlockType(ItemStack stack)
    {
        int sel = getSelectedBlockType(stack);
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

    public static int getSelectedBlockType(ItemStack stack)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode, true);

        return tag.getByte(TAG_NAME_BLOCK_SEL);
    }

    public void changeSelectedBlockType(ItemStack stack, boolean reverse)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound configsTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(configsTag, TAG_NAME_CONFIG_PRE + mode, true);

        NBTUtils.cycleByteValue(tag, TAG_NAME_BLOCK_SEL, -2, MAX_BLOCKS - 1, reverse);
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
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + mode, true);

        boolean isFlipped = tag.getBoolean("Flip");
        tag.setBoolean("Flip", ! isFlipped);

        //System.out.println("axis: " + EntityUtils.getClosestLookingDirection(player));
        //if (isFlipped == true)
        {
            EnumFacing facing = EntityUtils.getClosestLookingDirection(player);
            tag.setByte("FlipAxis", (byte)facing.getIndex());
        }
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

            if (Math.abs(player.rotationPitch) > 15.0f && (lookDir == faceAxisFlipped || lookDir == faceAxisFlipped.getOpposite()))
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

    public void addAdjacent(World world, BlockPos center, EnumFacing face, Area area, int posV, int posH, List<BlockPosStateDist> positions,
             int blockType, boolean diagonals, BlockInfo blockInfo, EnumFacing axisRight, EnumFacing axisUp)
    {
        if (posH < -area.rNegH || posH > area.rPosH || posV < -area.rNegV || posV > area.rPosV)
        {
            return;
        }

        //System.out.printf("addAdjacent(): posV: %d posH: %d blockInfo: %s\n", posV, posH, blockInfo != null ? blockInfo.blockName : "null");
        int x = center.getX() + posH * axisRight.getFrontOffsetX() + posV * axisUp.getFrontOffsetX();
        int y = center.getY() + posH * axisRight.getFrontOffsetY() + posV * axisUp.getFrontOffsetY();
        int z = center.getZ() + posH * axisRight.getFrontOffsetZ() + posV * axisUp.getFrontOffsetZ();

        // The location itself must be air
        if (world.isAirBlock(new BlockPos(x, y, z)) == false)
        {
            return;
        }

        int xb = x - face.getFrontOffsetX();
        int yb = y - face.getFrontOffsetY();
        int zb = z - face.getFrontOffsetZ();

        BlockPos blockPos = new BlockPos(xb, yb, zb);
        IBlockState state = world.getBlockState(blockPos);
        Block block = state.getBlock();
        int blockMeta = block.getMetaFromState(state);


        @SuppressWarnings("deprecation")
        ItemStack stackTmp = state.getBlock().getItem(world, blockPos, state);
        int itemMeta = stackTmp != null ? stackTmp.getMetadata() : 0;

        // The block on the back face must not be air or fluid ...
        if (block.isAir(state, world, blockPos) == true || block.getMaterial(state).isLiquid() == true)
        {
            return;
        }

        // The block on the back face must not be air and also it must not be fluid.
        // It must also be a matching block with our current build block, or we must have no fixed block requirement.
        // sel >= 0 means that we want to build with a fixed/bound block type,
        // as does BLOCK_TYPE_TARGETED, so in those cases we don't require a specific block type on the back.

        //if (blockType >= 0 || blockType == BLOCK_TYPE_TARGETED || blockInfo == null || (blockInfo.block == block && blockInfo.meta == meta))
        if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && blockInfo != null) ||
           (blockInfo != null && blockInfo.block == block && blockInfo.blockMeta == blockMeta))
        {
            if (blockType == BLOCK_TYPE_ADJACENT)
            {
                blockInfo = new BlockInfo(block, blockMeta, itemMeta);
            }

            BlockPosStateDist pos = new BlockPosStateDist(new BlockPos(x, y, z), 0, face, blockInfo);

            if (positions.contains(pos) == false)
            {
                positions.add(pos);

                // Adjacent blocks
                this.addAdjacent(world, center, face, area, posV - 1, posH + 0, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                this.addAdjacent(world, center, face, area, posV + 0, posH - 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                this.addAdjacent(world, center, face, area, posV + 0, posH + 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                this.addAdjacent(world, center, face, area, posV + 1, posH + 0, positions, blockType, diagonals, blockInfo, axisRight, axisUp);

                // Diagonals/corners
                if (diagonals == true)
                {
                    this.addAdjacent(world, center, face, area, posV - 1, posH - 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                    this.addAdjacent(world, center, face, area, posV - 1, posH + 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                    this.addAdjacent(world, center, face, area, posV + 1, posH - 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
                    this.addAdjacent(world, center, face, area, posV + 1, posH + 1, positions, blockType, diagonals, blockInfo, axisRight, axisUp);
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
        int blockType = getSelectedBlockType(stack);
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
    public void getBlockPositions(ItemStack stack, BlockPos targeted, EnumFacing face, World world, List<BlockPosStateDist> positions)
    {
        int blockType = getSelectedBlockType(stack);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted);
        BlockInfo biBound = getSelectedFixedBlockType(stack);

        EnumFacing axisRight = BlockPosEU.getRotation(face, EnumFacing.DOWN);
        EnumFacing axisUp = BlockPosEU.getRotation(face, axisRight);

        if (face == EnumFacing.UP)
        {
            axisRight = BlockPosEU.getRotation(face, EnumFacing.SOUTH);
            axisUp = BlockPosEU.getRotation(face, axisRight);
        }
        else if (face == EnumFacing.DOWN)
        {
            axisRight = BlockPosEU.getRotation(face, EnumFacing.SOUTH);
            axisUp = BlockPosEU.getRotation(face, axisRight);
        }

        if (this.getAreaFlipped(stack) == true)
        {
            EnumFacing flipAxis = this.getAreaFlipAxis(stack, face);
            axisRight = BlockPosEU.getRotation(axisRight, flipAxis);
            axisUp = BlockPosEU.getRotation(axisUp, flipAxis);
        }

        // Move the position forward by one from the targeted block
        BlockPos center = targeted.offset(face, 1);
        Area area = new Area(stack);
        int dim = world.provider.getDimension();

        Mode mode = Mode.getMode(stack);
        switch(mode)
        {
            case COLUMN:
                for (int i = 0; i <= area.rPosH; i++)
                {
                    BlockPos posTmp = center.offset(face, i);
                    if (world.isAirBlock(posTmp) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, face, biTarget));
                    }
                    else
                    {
                        break;
                    }
                }
                break;

            case LINE:
                // The line has to start from the center and go out, because it terminates on the first block encountered
                for (int i = 0; i <= area.rPosH; i++)
                {
                    BlockPos posTmp = center.offset(axisRight, i);
                    if (world.isAirBlock(posTmp) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, face,
                                        this.getBlockInfoForBlockType(world, posTmp, face, blockType, biTarget, biBound)));
                    }
                    else
                    {
                        break;
                    }
                }

                for (int i = -1; i >= -area.rNegH; i--)
                {
                    BlockPos posTmp = center.offset(axisRight, i);
                    if (world.isAirBlock(posTmp) == true)
                    {
                        positions.add(new BlockPosStateDist(posTmp, dim, face,
                                        this.getBlockInfoForBlockType(world, posTmp, face, blockType, biTarget, biBound)));
                    }
                    else
                    {
                        break;
                    }
                }
                break;

            case PLANE:
                for (int v = -area.rNegV; v <= area.rPosV; v++)
                {
                    for (int h = -area.rNegH; h <= area.rPosH; h++)
                    {
                        BlockPos posTmp = center.offset(axisRight, h).offset(axisUp, v);
                        if (world.isAirBlock(posTmp) == true)
                        {
                            positions.add(new BlockPosStateDist(posTmp, dim, face,
                                            this.getBlockInfoForBlockType(world, posTmp, face, blockType, biTarget, biBound)));
                        }
                    }
                }
                break;

            case EXTEND_CONTINUOUS:
                boolean diagonals = NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
                this.addAdjacent(world, center, face, area, 0, 0, positions, blockType, diagonals, biTarget, axisRight, axisUp);
                break;

            case EXTEND_AREA:
                for (int v = -area.rNegV; v <= area.rPosV; v++)
                {
                    for (int h = -area.rNegH; h <= area.rPosH; h++)
                    {
                        BlockPos posTmp = center.offset(axisRight, h).offset(axisUp, v);

                        // The target position must be air
                        if (world.isAirBlock(posTmp) == true)
                        {
                            BlockPos posTgt = posTmp.offset(face, -1);

                            IBlockState state = world.getBlockState(posTgt);
                            Block block = state.getBlock();
                            int meta = block.getMetaFromState(state);

                            // The block on the back face must not be air and also it must not be fluid.
                            // It must also be a matching block with our current build block, or we must have no fixed block requirement.
                            // sel >= 0 means that we want to build with a fixed/bound block type,
                            // as does BLOCK_TYPE_TARGETED, so in those cases we don't require a specific block type on the back.
                            if (block.isAir(state, world, posTgt) == false && block.getMaterial(state).isLiquid() == false)
                            {
                                if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && biBound != null) ||
                                   (biTarget != null && biTarget.block == block && biTarget.blockMeta == meta))
                                {
                                    positions.add(new BlockPosStateDist(posTmp, dim, face,
                                                    this.getBlockInfoForBlockType(world, posTmp, face, blockType, biTarget, biBound)));
                                }
                            }
                        }
                    }
                }
                break;

            default:
        }
    }

    public void getBlockPositionsWalls(ItemStack stack, BlockPosEU targeted, World world, List<BlockPosStateDist> positions, BlockPosEU pos1, BlockPosEU pos2)
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

        int blockType = getSelectedBlockType(stack);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted.toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
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

    public void getBlockPositionsCube(ItemStack stack, BlockPosEU targeted, World world, List<BlockPosStateDist> positions, BlockPosEU pos1, BlockPosEU pos2)
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

        int blockType = getSelectedBlockType(stack);
        BlockInfo biTarget = this.getBlockInfoForTargeted(stack, world, targeted.toBlockPos());
        BlockInfo biBound = getSelectedFixedBlockType(stack);
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

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Alt + Toggle key: Change the selected block type
        if (ReferenceKeys.keypressContainsControl(key) == false &&
            ReferenceKeys.keypressContainsShift(key) == false &&
            ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.changeSelectedBlockType(stack, ReferenceKeys.keypressActionIsReversed(key));
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
            NBTUtils.toggleBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
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
                this.init(0x08080808);
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
         * Adjust the area based on the "planarized" ForgeDirection, where<br>
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
        CUBE ("enderutilities.tooltip.item.build.cube");

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

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "stage=0"),
                new ModelResourceLocation(rl, "stage=1"),
                new ModelResourceLocation(rl, "stage=2"),
                new ModelResourceLocation(rl, "stage=3"),
                new ModelResourceLocation(rl, "stage=4")
        };
    }

    // FIXME 1.9
    @SideOnly(Side.CLIENT)
    //@Override
    public ModelResourceLocation getModel(ItemStack stack, EntityPlayer player, int useRemaining)
    {
        int index = 0;

        if (player != null && player.isHandActive() == true)
        {
            index = MathHelper.clamp_int((this.getMaxItemUseDuration(stack) - useRemaining) / 4, 0, 4);
        }

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, "stage=" + index);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return this.getModel(stack, null, 0);
    }
}
