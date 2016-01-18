package fi.dy.masa.enderutilities.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.event.tasks.TaskBuildersWand;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
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
    public static final String TAG_NAME_CORNER1 ="Corner1";
    public static final String TAG_NAME_CORNER2 ="Corner2";
    public static final int BLOCK_TYPE_TARGETED = -1;
    public static final int BLOCK_TYPE_ADJACENT = -2;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemBuildersWand()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_BUILDERS_WAND);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        BlockPosEU pos = this.getPosition(stack, true);
        if (pos != null)
        {
            Mode mode = Mode.getMode(stack);

            //System.out.println("onItemRightClick - " + (world.isRemote ? "client" : "server"));
            if (world.isRemote == false)
            {
                if (PlayerTaskScheduler.getInstance().hasTask(player, TaskBuildersWand.class) == true)
                {
                    PlayerTaskScheduler.getInstance().removeTask(player, TaskBuildersWand.class);
                    return stack;
                }
                else if (mode != Mode.CUBE && mode != Mode.WALLS)
                {
                    this.useWand(stack, world, player, pos);
                    return stack;
                }
            }

            if (mode == Mode.CUBE || mode == Mode.WALLS)
            {
                if (this.getPosition(stack, false) != null)
                {
                    player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
                    player.worldObj.playSoundAtEntity(player, "mob.endermen.portal", 0.4f, 0.7f);
                }
            }

            return stack;
        }

        return super.onItemRightClick(stack, world, player);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IInventory)
        {
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.CUBE || mode == Mode.WALLS)
        {
            if (world.isRemote == false)
            {
                this.setPosition(stack, new BlockPosEU(x, y, z, player.dimension, side), player.isSneaking() == false);
            }

            return true;
        }

        if (world.isRemote == false)
        {
            this.useWand(stack, world, player, new BlockPosEU(x, y, z, player.dimension, side));
        }

        return true;
    }

    public void onLeftClickBlock(EntityPlayer player, World world, ItemStack stack, int x, int y, int z, int dimension, int side)
    {
        // Left click without sneaking: Set the "anchor" position
        if (player.isSneaking() == false)
        {
            this.setPosition(stack, new BlockPosEU(x, y, z, player.dimension, side), true);
        }
        // Sneak + left click: Set the selected block type
        else
        {
            this.setSelectedFixedBlockType(stack, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int itemInUseCount)
    {
        if (this.getMaxItemUseDuration(stack) - itemInUseCount >= 20)
        {
            BlockPosEU pos = this.getPosition(stack, true);
            if (pos != null)
            {
                this.useWand(stack, world, player, pos);
            }
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String itemName = StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
        if (stack.getTagCompound() == null)
        {
            return itemName;
        }

        String preBT = EnumChatFormatting.AQUA.toString();
        String preGreen = EnumChatFormatting.GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        if (itemName.length() >= 14)
        {
            itemName = EUStringUtils.getInitialsWithDots(itemName);
        }
        itemName = itemName + " M: " + preGreen + Mode.getMode(stack).getDisplayName() + rst;

        int sel = getSelectedBlockType(stack);
        if (sel >= 0)
        {
            BlockInfo blockInfo = getSelectedFixedBlockType(stack);
            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(Block.getBlockFromName(blockInfo.blockName), 1, blockInfo.meta);
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
                str = StatCollector.translateToLocal("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                str = StatCollector.translateToLocal("enderutilities.tooltip.item.blocktype.adjacent");
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
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        String pre = EnumChatFormatting.DARK_GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        Mode mode = Mode.getMode(stack);
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + pre + mode.getDisplayName() + rst);

        int sel = getSelectedBlockType(stack);
        if (sel >= 0)
        {
            BlockInfo blockInfo = getSelectedFixedBlockType(stack);
            if (blockInfo != null)
            {
                ItemStack blockStack = new ItemStack(Block.getBlockFromName(blockInfo.blockName), 1, blockInfo.meta);
                if (blockStack != null && blockStack.getItem() != null)
                {
                    String str = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedblock");
                    list.add(str + ": " + pre + blockStack.getDisplayName() + rst);
                }
            }
        }
        else
        {
            String str = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedblock");
            String str2;
            if (sel == BLOCK_TYPE_TARGETED)
            {
                str2 = StatCollector.translateToLocal("enderutilities.tooltip.item.blocktype.targeted");
            }
            else
            {
                str2 = StatCollector.translateToLocal("enderutilities.tooltip.item.blocktype.adjacent");
            }

            list.add(str + ": " + pre + str2 + rst);
        }

        String str = StatCollector.translateToLocal("enderutilities.tooltip.item.area.flipped");
        String str2;
        if (this.getAreaFlipped(stack) == true)
        {
            str2 = EnumChatFormatting.GREEN + StatCollector.translateToLocal("enderutilities.tooltip.item.yes");
        }
        else
        {
            str2 = EnumChatFormatting.RED + StatCollector.translateToLocal("enderutilities.tooltip.item.no");
        }
        list.add(str + ": " + str2 + rst);

        if (mode == Mode.EXTEND_CONTINUOUS)
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.builderswand.allowdiagonals");
            if (NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS) == true)
            {
                str2 = EnumChatFormatting.GREEN + StatCollector.translateToLocal("enderutilities.tooltip.item.yes");
            }
            else
            {
                str2 = EnumChatFormatting.RED + StatCollector.translateToLocal("enderutilities.tooltip.item.no");
            }
            list.add(str + ": " + str2 + rst);
        }

        str = StatCollector.translateToLocal("enderutilities.tooltip.item.builderswand.renderghostblocks");
        if (NBTUtils.getBoolean(stack, ItemBuildersWand.WRAPPER_TAG_NAME, ItemBuildersWand.TAG_NAME_GHOST_BLOCKS) == true)
        {
            str2 = EnumChatFormatting.GREEN + StatCollector.translateToLocal("enderutilities.tooltip.item.yes");
        }
        else
        {
            str2 = EnumChatFormatting.RED + StatCollector.translateToLocal("enderutilities.tooltip.item.no");
        }
        list.add(str + ": " + str2 + rst);

        super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
    }

    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        //super.addTooltips(stack, list, verbose);
    }

    public BlockPosEU getPosition(ItemStack stack, boolean isStart)
    {
        String tagName = (isStart == true) ? TAG_NAME_CORNER1 : TAG_NAME_CORNER2;

        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, tagName, false);
        if (tag != null)
        {
            return BlockPosEU.readFromNBT(tag);
        }

        return null;
    }

    public void setPosition(ItemStack stack, BlockPosEU pos, boolean isStart)
    {
        String tagName = (isStart == true) ? TAG_NAME_CORNER1 : TAG_NAME_CORNER2;

        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, tagName, true);
        if (pos == null)
        {
            BlockPosEU.removeFromNBT(tag);
            return;
        }

        BlockPosEU storedPos = BlockPosEU.readFromNBT(tag);
        if (storedPos != null && storedPos.equals(pos) == true)
        {
            BlockPosEU.removeFromNBT(tag);
        }
        else
        {
            pos.writeToNBT(tag);
        }
    }

    public boolean useWand(ItemStack stack, World world, EntityPlayer player, BlockPosEU targetPos)
    {
        if (player.dimension != targetPos.dimension)
        {
            return false;
        }

        List<BlockPosStateDist> positions = new ArrayList<BlockPosStateDist>();

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.CUBE)
        {
            this.getBlockPositionsCube(stack, world, player, positions, this.getBlockInfo(stack, targetPos, world));
        }
        else if (mode == Mode.WALLS)
        {
            this.getBlockPositionsWalls(stack, world, player, positions, this.getBlockInfo(stack, targetPos, world));
        }
        else
        {
            this.getBlockPositions(stack, targetPos, world, player, positions);
        }

        if (positions.size() <= 48)
        {
            for (int i = 0; i < positions.size(); i++)
            {
                placeBlockToPosition(stack, world, player, positions.get(i));
            }

            // Offset the start position by one after a build operation completes, but not for Walls and Cube modes
            BlockPosEU pos = this.getPosition(stack, true);
            if (pos != null && mode != Mode.WALLS && mode != Mode.CUBE)
            {
                this.setPosition(stack, pos.offset(ForgeDirection.getOrientation(targetPos.face), 1), true);
            }
        }
        else
        {
            // TODO add a config option for the block-per-tick value
            TaskBuildersWand task = new TaskBuildersWand(world, player.getUniqueID(), positions, 8);
            PlayerTaskScheduler.getInstance().addTask(player, task, 1);
        }

        return false;
    }

    public static boolean placeBlockToPosition(ItemStack wandStack, World world, EntityPlayer player, BlockPosStateDist pos)
    {
        if (world.isAirBlock(pos.posX, pos.posY, pos.posZ) == true)
        {
            BlockInfo blockInfo = null;
            int type = getSelectedBlockType(wandStack);
            if (type >= 0)
            {
                blockInfo = getSelectedFixedBlockType(wandStack);

                if (blockInfo == null)
                {
                    return false;
                }
            }
            else if (type == BLOCK_TYPE_TARGETED)
            {
                blockInfo = new BlockInfo(world.getBlock(pos.posX, pos.posY, pos.posZ),
                                          world.getBlockMetadata(pos.posX, pos.posY, pos.posZ));
            }

            Block block;
            int meta = 0;
            // Pre-determined block type to build
            if (blockInfo != null && blockInfo.block != null)
            {
                block = blockInfo.block;
                meta = blockInfo.meta;
            }
            else
            {
                ForgeDirection face = ForgeDirection.getOrientation(pos.face);

                block = world.getBlock(pos.posX - face.offsetX, pos.posY - face.offsetY, pos.posZ - face.offsetZ);

                if (block.isAir(world, pos.posX - face.offsetX, pos.posY - face.offsetY, pos.posZ - face.offsetZ) == false &&
                    block.getMaterial().isLiquid() == false)
                {
                    meta = world.getBlockMetadata(pos.posX - face.offsetX, pos.posY - face.offsetY, pos.posZ - face.offsetZ);
                }
                else
                {
                    return false;
                }
            }

            if (player.capabilities.isCreativeMode == true)
            {
                ItemStack targetStack = new ItemStack(block, 1, block.damageDropped(meta));
                if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
                {
                    // Check if we can place the block
                    if (BlockUtils.checkCanPlaceBlockAt(world, pos.posX, pos.posY, pos.posZ, pos.face, player, targetStack) == true)
                    {
                        if (ForgeHooks.onPlaceItemIntoWorld(targetStack, player, world, pos.posX, pos.posY, pos.posZ, pos.face, 0.5f, 0.5f, 0.5f) == true)
                        {
                            return true;
                        }
                    }
                }
            }
            else
            {
                ItemStack templateStack = new ItemStack(block, 1, block.damageDropped(meta));
                IInventory inv = getInventoryWithItems(wandStack, templateStack, player);
                ItemStack targetStack = getItemToBuildWith(inv, templateStack, 1);

                if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
                {
                    // Check if we can place the block
                    if (BlockUtils.checkCanPlaceBlockAt(world, pos.posX, pos.posY, pos.posZ, pos.face, player, targetStack) == false ||
                        ForgeHooks.onPlaceItemIntoWorld(targetStack, player, world, pos.posX, pos.posY, pos.posZ, pos.face, 0.5f, 0.5f, 0.5f) == false)
                    {
                        if (InventoryUtils.tryInsertItemStackToInventory(inv, targetStack, 1) == false)
                        {
                            EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, targetStack);
                            world.spawnEntityInWorld(item);
                        }
                    }
                    else
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static IInventory getInventoryWithItems(ItemStack wandStack, ItemStack templateStack, EntityPlayer player)
    {
        IInventory inv = player.inventory;
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

    public static ItemStack getItemToBuildWith(IInventory inv, ItemStack templateStack, int amount)
    {
        if (inv != null)
        {
            int slot = InventoryUtils.getSlotOfFirstMatchingItemStack(inv, templateStack);
            if (slot != -1)
            {
                return inv.decrStackSize(slot, amount);
            }
        }

        return null;
    }

    public void setSelectedFixedBlockType(ItemStack stack, Block block, int meta)
    {
        int sel = getSelectedBlockType(stack);
        if (sel < 0)
        {
            return;
        }

        NBTTagCompound blocksTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCKS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(blocksTag, TAG_NAME_BLOCK_PRE + sel, true);

        tag.setString("Block", Block.blockRegistry.getNameForObject(block));
        tag.setByte("Meta", (byte)meta);
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

        if (tag != null && tag.hasKey("Block", Constants.NBT.TAG_STRING) == true)
        {
            return new BlockInfo(tag.getString("Block"), tag.getByte("Meta"));
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

    public void toggleAreaFlipped(ItemStack stack)
    {
        int mode = Mode.getModeOrdinal(stack);
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, TAG_NAME_CONFIGS, true);
        NBTTagCompound tag = NBTUtils.getCompoundTag(wrapperTag, TAG_NAME_CONFIG_PRE + mode, true);

        tag.setBoolean("Flip", ! tag.getBoolean("Flip"));
    }

    public void changeAreaDimensions(EntityPlayer player, ItemStack stack, boolean reverse)
    {
        BlockPosEU pos = this.getPosition(stack, true);
        if (pos == null)
        {
            return;
        }

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.WALLS || mode == Mode.CUBE)
        {
            return;
        }

        int amount = reverse ? 1 : -1;
        int maxRadius = 64;
        Area area = new Area(stack);

        // Only one dimension is used for the column mode
        if (mode == Mode.COLUMN)
        {
            area.rPosH = MathHelper.clamp_int(area.rPosH + amount, 0, maxRadius);
            area.writeToNBT(stack);
            return;
        }

        ForgeDirection faceAxis = ForgeDirection.getOrientation(pos.face);
        ForgeDirection lookDir = EntityUtils.getClosestLookingDirection(player);

        if (faceAxis == ForgeDirection.UP || faceAxis == ForgeDirection.DOWN)
        {
            lookDir = EntityUtils.getHorizontalLookingDirection(player);

            switch(lookDir)
            {
                case SOUTH:
                    area.rPosV = MathHelper.clamp_int(area.rPosV + amount, 0, maxRadius);
                    break;
                case NORTH:
                    area.rNegV = MathHelper.clamp_int(area.rNegV + amount, 0, maxRadius);
                    break;
                case EAST:
                    // Why are these reversed?
                    if (faceAxis == ForgeDirection.DOWN)
                        area.rPosH = MathHelper.clamp_int(area.rPosH + amount, 0, maxRadius);
                    else
                        area.rNegH = MathHelper.clamp_int(area.rNegH + amount, 0, maxRadius);
                    break;
                case WEST:
                    if (faceAxis == ForgeDirection.DOWN)
                        area.rNegH = MathHelper.clamp_int(area.rNegH + amount, 0, maxRadius);
                    else
                        area.rPosH = MathHelper.clamp_int(area.rPosH + amount, 0, maxRadius);
                    break;
                default:
            }
        }
        else
        {
            switch(lookDir)
            {
                case UP:
                    area.rPosV = MathHelper.clamp_int(area.rPosV + amount, 0, maxRadius);
                    break;
                case DOWN:
                    area.rNegV = MathHelper.clamp_int(area.rNegV + amount, 0, maxRadius);
                    break;
                default:
                    LeftRight look = EntityUtils.getLookLeftRight(player, faceAxis);
                    //System.out.println("yaw: " + player.rotationYaw + " look: " + look);
                    if (look == LeftRight.RIGHT)
                    {
                        area.rPosH = MathHelper.clamp_int(area.rPosH + amount, 0, maxRadius);
                    }
                    else
                    {
                        area.rNegH = MathHelper.clamp_int(area.rNegH + amount, 0, maxRadius);
                    }
            }
        }

        area.writeToNBT(stack);
    }

    public void addAdjacent(World world, BlockPosEU center, Area area, int posV, int posH, List<BlockPosStateDist> positions,
             int blockType, boolean diagonals, BlockInfo blockInfo, ForgeDirection face, ForgeDirection axisRight, ForgeDirection axisUp)
    {
        if (posH < -area.rNegH || posH > area.rPosH || posV < -area.rNegV || posV > area.rPosV)
        {
            return;
        }

        //System.out.printf("addAdjacent(): posV: %d posH: %d blockInfo: %s\n", posV, posH, blockInfo != null ? blockInfo.blockName : "null");
        int x = center.posX + posH * axisRight.offsetX + posV * axisUp.offsetX;
        int y = center.posY + posH * axisRight.offsetY + posV * axisUp.offsetY;
        int z = center.posZ + posH * axisRight.offsetZ + posV * axisUp.offsetZ;

        // The location itself must be air
        if (world.isAirBlock(x, y, z) == false)
        {
            return;
        }

        int xb = x - face.offsetX;
        int yb = y - face.offsetY;
        int zb = z - face.offsetZ;

        Block block = world.getBlock(xb, yb, zb);
        int meta = world.getBlockMetadata(xb, yb, zb);

        // The block on the back face must not be air or fluid ...
        if (block.isAir(world, xb, yb, zb) == true || block.getMaterial().isLiquid() == true)
        {
            return;
        }

        // The block on the back face must not be air and also it must not be fluid.
        // It must also be a matching block with our current build block, or we must have no fixed block requirement.
        // sel >= 0 means that we want to build with a fixed/bound block type,
        // as does BLOCK_TYPE_TARGETED, so in those cases we don't require a specific block type on the back.

        //if (blockType >= 0 || blockType == BLOCK_TYPE_TARGETED || blockInfo == null || (blockInfo.block == block && blockInfo.meta == meta))
        if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && blockInfo != null) ||
           (blockInfo != null && blockInfo.block == block && blockInfo.meta == meta))
        {
            BlockPosStateDist pos = new BlockPosStateDist(x, y, z, 0, center.face, blockType == BLOCK_TYPE_ADJACENT ? new BlockInfo(block, meta) : blockInfo);
            if (positions.contains(pos) == false)
            {
                positions.add(pos);

                // Adjacent blocks
                this.addAdjacent(world, center, area, posV - 1, posH + 0, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                this.addAdjacent(world, center, area, posV + 0, posH - 1, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                this.addAdjacent(world, center, area, posV + 0, posH + 1, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                this.addAdjacent(world, center, area, posV + 1, posH + 0, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);

                // Diagonals/corners
                if (diagonals == true)
                {
                    this.addAdjacent(world, center, area, posV - 1, posH - 1, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                    this.addAdjacent(world, center, area, posV - 1, posH + 1, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                    this.addAdjacent(world, center, area, posV + 1, posH - 1, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                    this.addAdjacent(world, center, area, posV + 1, posH + 1, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                }
            }
        }
    }

    public BlockInfo getAdjacentBlock(World world, int x, int y, int z, ForgeDirection faceAxis)
    {
        return new BlockInfo(world.getBlock(x - faceAxis.offsetX, y - faceAxis.offsetY, z - faceAxis.offsetZ),
                             world.getBlockMetadata(x - faceAxis.offsetX, y - faceAxis.offsetY, z - faceAxis.offsetZ));
    }

    public BlockInfo getBlockInfo(ItemStack stack, BlockPosEU targeted, World world)
    {
        BlockInfo blockInfo = null;
        int blockType = getSelectedBlockType(stack);

        if (blockType == BLOCK_TYPE_TARGETED || blockType == BLOCK_TYPE_ADJACENT)
        {
            blockInfo = new BlockInfo(world.getBlock(targeted.posX, targeted.posY, targeted.posZ),
                                      world.getBlockMetadata(targeted.posX, targeted.posY, targeted.posZ));
        }
        // If using a fixed block type, then we require a valid block
        else if (blockType >= 0)
        {
            blockInfo = getSelectedFixedBlockType(stack);
        }

        return blockInfo;
    }

    /**
     * Get the actual block positions and block types for all other modes except Walls and Cube.
     */
    public void getBlockPositions(ItemStack stack, BlockPosEU targeted, World world, EntityPlayer player, List<BlockPosStateDist> positions)
    {
        BlockInfo blockInfo = this.getBlockInfo(stack, targeted, world);
        int blockType = getSelectedBlockType(stack);

        ForgeDirection face = ForgeDirection.getOrientation(targeted.face);
        ForgeDirection axisRight = face.getRotation(ForgeDirection.DOWN);
        ForgeDirection axisUp = face.getRotation(axisRight);

        if (face == ForgeDirection.UP)
        {
            axisRight = face.getRotation(ForgeDirection.SOUTH);
            axisUp = face.getRotation(axisRight);
        }
        else if (face == ForgeDirection.DOWN)
        {
            axisRight = face.getRotation(ForgeDirection.SOUTH);
            axisUp = face.getRotation(axisRight);
        }

        if (this.getAreaFlipped(stack) == true)
        {
            axisRight = axisRight.getRotation(face);
            axisUp = axisUp.getRotation(face);
        }

        // Move the position forward by one from the targeted block
        BlockPosEU center = targeted.offset(face, 1);
        BlockPosStateDist posTmp;
        Area area = new Area(stack);

        // NOTE: The block position are added so that the targeted position is first in the list,
        // and it will then be rendered in a different color

        Mode mode = Mode.getMode(stack);
        switch(mode)
        {
            case COLUMN:
                for (int i = 0; i <= area.rPosH; i++)
                {
                    int x = center.posX + i * face.offsetX;
                    int y = center.posY + i * face.offsetY;
                    int z = center.posZ + i * face.offsetZ;

                    if (world.isAirBlock(x, y, z) == true)
                    {
                        positions.add(new BlockPosStateDist(x, y, z, 0, targeted.face, blockInfo));
                    }
                    else
                    {
                        break;
                    }
                }
                break;

            case LINE:
                for (int i = 0; i <= area.rPosH; i++)
                {
                    int x = center.posX + i * axisRight.offsetX;
                    int y = center.posY + i * axisRight.offsetY;
                    int z = center.posZ + i * axisRight.offsetZ;

                    if (world.isAirBlock(x, y, z) == true)
                    {
                        //System.out.printf("x: %d y: %d z: %d\n", x, y, z);
                        posTmp = new BlockPosStateDist(x, y, z, 0, targeted.face,
                                blockType == BLOCK_TYPE_ADJACENT ? this.getAdjacentBlock(world, x, y, z, face) : blockInfo);
                        positions.add(posTmp);
                    }
                    else
                    {
                        break;
                    }
                }

                for (int i = -1; i >= -area.rNegH; i--)
                {
                    int x = center.posX + i * axisRight.offsetX;
                    int y = center.posY + i * axisRight.offsetY;
                    int z = center.posZ + i * axisRight.offsetZ;

                    if (world.isAirBlock(x, y, z) == true)
                    {
                        posTmp = new BlockPosStateDist(x, y, z, 0, targeted.face,
                                blockType == BLOCK_TYPE_ADJACENT ? this.getAdjacentBlock(world, x, y, z, face) : blockInfo);
                        positions.add(posTmp);
                    }
                    else
                    {
                        break;
                    }
                }
                break;

            case PLANE:
                // Add the center position first, it will be rendered in different color
                posTmp = new BlockPosStateDist(center.posX, center.posY, center.posZ, 0, targeted.face,
                        blockType == BLOCK_TYPE_ADJACENT ? this.getAdjacentBlock(world, center.posX, center.posY, center.posZ, face) : blockInfo);
                positions.add(posTmp);

                for (int v = -area.rNegV; v <= area.rPosV; v++)
                {
                    for (int h = -area.rNegH; h <= area.rPosH; h++)
                    {
                        int x = center.posX + h * axisRight.offsetX + v * axisUp.offsetX;
                        int y = center.posY + h * axisRight.offsetY + v * axisUp.offsetY;
                        int z = center.posZ + h * axisRight.offsetZ + v * axisUp.offsetZ;

                        if (world.isAirBlock(x, y, z) == true && (h != 0 || v != 0))
                        {
                            posTmp = new BlockPosStateDist(x, y, z, 0, targeted.face,
                                    blockType == BLOCK_TYPE_ADJACENT ? this.getAdjacentBlock(world, x, y, z, face) : blockInfo);
                            positions.add(posTmp);
                        }
                    }
                }
                break;

            case EXTEND_CONTINUOUS:
                boolean diagonals = NBTUtils.getBoolean(stack, WRAPPER_TAG_NAME, TAG_NAME_ALLOW_DIAGONALS);
                this.addAdjacent(world, center, area, 0, 0, positions, blockType, diagonals, blockInfo, face, axisRight, axisUp);
                break;

            case EXTEND_AREA:
                // Add the center position first, it will be rendered in different color
                posTmp = new BlockPosStateDist(center.posX, center.posY, center.posZ, 0, targeted.face,
                        blockType == BLOCK_TYPE_ADJACENT ? this.getAdjacentBlock(world, center.posX, center.posY, center.posZ, face) : blockInfo);
                positions.add(posTmp);

                for (int v = -area.rNegV; v <= area.rPosV; v++)
                {
                    for (int h = -area.rNegH; h <= area.rPosH; h++)
                    {
                        int x = center.posX + h * axisRight.offsetX + v * axisUp.offsetX;
                        int y = center.posY + h * axisRight.offsetY + v * axisUp.offsetY;
                        int z = center.posZ + h * axisRight.offsetZ + v * axisUp.offsetZ;

                        // The target position must be air
                        if (world.isAirBlock(x, y, z) == true && (h != 0 || v != 0))
                        {
                            int xb = x - face.offsetX;
                            int yb = y - face.offsetY;
                            int zb = z - face.offsetZ;

                            Block block = world.getBlock(xb, yb, zb);
                            int meta = world.getBlockMetadata(xb, yb, zb);

                            // The block on the back face must not be air and also it must not be fluid.
                            // It must also be a matching block with our current build block, or we must have no fixed block requirement.
                            // sel >= 0 means that we want to build with a fixed/bound block type,
                            // as does BLOCK_TYPE_TARGETED, so in those cases we don't require a specific block type on the back.
                            if (block.isAir(world, xb, yb, zb) == false && block.getMaterial().isLiquid() == false)
                            {
                                if (blockType == BLOCK_TYPE_ADJACENT || (blockType >= 0 && blockInfo != null) ||
                                   (blockInfo != null && blockInfo.block == block && blockInfo.meta == meta))
                                //if (blockType >= 0 || blockType == BLOCK_TYPE_TARGETED || blockInfo == null ||
                                //   (blockInfo.block == block && blockInfo.meta == meta))
                                {
                                    posTmp = new BlockPosStateDist(x, y, z, 0, targeted.face,
                                            blockType == BLOCK_TYPE_ADJACENT ? this.getAdjacentBlock(world, x, y, z, face) : blockInfo);
                                    positions.add(posTmp);
                                }
                            }
                        }
                    }
                }
                break;

            default:
        }
    }

    public void getBlockPositionsWalls(ItemStack stack, World world, EntityPlayer player, List<BlockPosStateDist> positions, BlockInfo blockInfo)
    {
        BlockPosEU pos1 = this.getPosition(stack, true);
        BlockPosEU pos2 = this.getPosition(stack, false);
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

        for (int x = startX; x <= endX; x++)
        {
            for (int y = startY; y <= endY; y++)
            {
                positions.add(new BlockPosStateDist(x, y, startZ, player.dimension, 0, blockInfo));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int y = startY; y <= endY; y++)
            {
                positions.add(new BlockPosStateDist(x, y, endZ, player.dimension, 0, blockInfo));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                positions.add(new BlockPosStateDist(x, startY, z, player.dimension, 0, blockInfo));
            }
        }

        for (int x = startX; x <= endX; x++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                positions.add(new BlockPosStateDist(x, endY, z, player.dimension, 0, blockInfo));
            }
        }

        for (int z = startZ + 1; z <= endZ - 1; z++)
        {
            for (int y = startY + 1; y <= endY - 1; y++)
            {
                positions.add(new BlockPosStateDist(startX, y, z, player.dimension, 0, blockInfo));
            }
        }

        for (int z = startZ + 1; z <= endZ - 1; z++)
        {
            for (int y = startY + 1; y <= endY - 1; y++)
            {
                positions.add(new BlockPosStateDist(endX, y, z, player.dimension, 0, blockInfo));
            }
        }
    }

    public void getBlockPositionsCube(ItemStack stack, World world, EntityPlayer player, List<BlockPosStateDist> positions, BlockInfo blockInfo)
    {
        BlockPosEU pos1 = this.getPosition(stack, true);
        BlockPosEU pos2 = this.getPosition(stack, false);
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

        for (int y = startY; y <= endY; y++)
        {
            for (int z = startZ; z <= endZ; z++)
            {
                for (int x = startX; x <= endX; x++)
                {
                    positions.add(new BlockPosStateDist(x, y, z, player.dimension, 0, blockInfo));
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
            this.toggleAreaFlipped(stack);
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
        return EnumAction.block;
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
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".0");
        this.iconArray = new IIcon[5];
        for (int i = 0; i < 5; i++)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + i);
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getItemIconForUseDuration(int index)
    {
        if (index < this.iconArray.length)
        {
            return this.iconArray[index];
        }

        return this.itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        return this.getIcon(stack, renderPass, null, null, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
        int index = 0;

        if (player != null && player.getItemInUse() != null)
        {
            int inUse = 0;
            int maxUse = this.getMaxItemUseDuration(stack);
            inUse = maxUse - useRemaining;
            index = MathHelper.clamp_int(inUse / 4, 0, 4);
        }

        return this.getItemIconForUseDuration(index);
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
            return StatCollector.translateToLocal(this.unlocName);
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
}
