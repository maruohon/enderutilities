package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockInfo;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemBuildersWand extends ItemLocationBoundModular
{
    public static final int MAX_BLOCKS = 6;
    public static final String WRAPPER_TAG_NAME = "BuildersWand";
    public static final String TAG_NAME_CONFIG_PRE = "Config_";
    public static final String TAG_NAME_MODE = "Mode";
    public static final String TAG_NAME_BLOCK = "Block_";
    public static final String TAG_NAME_BLOCK_SEL = "SelBlock";
    public static final String TAG_NAME_BLOCKPOS = "BlockPos_";

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

        if (player.isSneaking() == true)
        {
            if (world.isRemote == false)
            {
                this.setSelectedBlockType(stack, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
            }

            return true;
        }

        return this.useWand(stack, world, player, x, y, z, side);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String itemName = StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
        if (stack.getTagCompound() == null)
        {
            return itemName;
        }

        String preGreen = EnumChatFormatting.GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        if (itemName.length() >= 14)
        {
            itemName = EUStringUtils.getInitialsWithDots(itemName);
        }
        itemName = itemName + " M: " + preGreen + Mode.getMode(stack).getDisplayName() + rst;

        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null)
        {
        }

        BlockInfo blockInfo = this.getSelectedBlockType(stack);
        if (blockInfo != null)
        {
            ItemStack blockStack = new ItemStack(Block.getBlockFromName(blockInfo.blockName), 1, blockInfo.meta);
            if (blockStack != null && blockStack.getItem() != null)
            {
                itemName = itemName + " B: " + preGreen + blockStack.getDisplayName() + rst;
            }
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

        String preBlue = EnumChatFormatting.BLUE.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + preBlue + Mode.getMode(stack).getDisplayName() + rst);

        BlockInfo blockInfo = this.getSelectedBlockType(stack);
        if (blockInfo != null)
        {
            ItemStack blockStack = new ItemStack(Block.getBlockFromName(blockInfo.blockName), 1, blockInfo.meta);
            if (blockStack != null && blockStack.getItem() != null)
            {
                String str = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedblock");
                list.add(str + ": " + preBlue + blockStack.getDisplayName() + rst);
            }
        }

        super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
    }

    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        //super.addTooltips(stack, list, verbose);
    }

    public boolean useWand(ItemStack stack, World world, EntityPlayer player, int x, int y, int z, int side)
    {
        switch(Mode.getMode(stack))
        {
            case EXTEND_TARGETED:
            case EXTEND_SAME:
            case EXTEND_FIXED:
            case COLUMN:
            case LINE:
            case PLANE:
            case WALLS:
            case CUBE:
            default:
        }

        return false;
    }

    public void setSelectedBlockType(ItemStack stack, Block block, int meta)
    {
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, true);
        NBTTagCompound tag = new NBTTagCompound();
        int sel = NBTUtils.getByte(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCK_SEL);

        tag.setString("Block", Block.blockRegistry.getNameForObject(block));
        tag.setByte("Meta", (byte)meta);

        wrapperTag.setTag(TAG_NAME_BLOCK + sel, tag);
    }

    public BlockInfo getSelectedBlockType(ItemStack stack)
    {
        NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, false);
        int sel = NBTUtils.getByte(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCK_SEL);

        if (wrapperTag != null && wrapperTag.hasKey(TAG_NAME_BLOCK + sel, Constants.NBT.TAG_COMPOUND) == true)
        {
            NBTTagCompound tag = wrapperTag.getCompoundTag(TAG_NAME_BLOCK + sel);
            return new BlockInfo(tag.getString("Block"), tag.getByte("Meta"));
        }

        return null;
    }

    public void changeSelectedBlockType(ItemStack stack, boolean reverse)
    {
        NBTUtils.cycleByteValue(stack, WRAPPER_TAG_NAME, TAG_NAME_BLOCK_SEL, MAX_BLOCKS, reverse);
    }

    public void changeAreaDimensions(EntityPlayer player, ItemStack stack, boolean reverse)
    {
        Area area = new Area(stack);
        ForgeDirection dir = EntityUtils.getLookingDirection(player);
        int amount = reverse ? -1 : 1;
        int maxRadius = 64;

        switch(dir)
        {
            case UP:
                area.rPosY = MathHelper.clamp_int(area.rPosY + amount, 0, maxRadius);
                break;
            case DOWN:
                area.rNegY = MathHelper.clamp_int(area.rNegY + amount, 0, maxRadius);
                break;
            case WEST:
                area.rNegX = MathHelper.clamp_int(area.rNegX + amount, 0, maxRadius);
                break;
            case EAST:
                area.rPosX = MathHelper.clamp_int(area.rPosX + amount, 0, maxRadius);
                break;
            case NORTH:
                area.rNegZ = MathHelper.clamp_int(area.rNegZ + amount, 0, maxRadius);
                break;
            case SOUTH:
                area.rPosZ = MathHelper.clamp_int(area.rPosZ + amount, 0, maxRadius);
                break;
            default:
        }

        area.writeToNBT(stack);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Alt + (Shift + ) Toggle key: Change the dimensions of the current mode
        if (ReferenceKeys.keypressContainsControl(key) == false &&
            ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.changeAreaDimensions(player, stack, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
        // Shift + Toggle Mode: Change the selected block type
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedBlockType(stack, ReferenceKeys.keypressActionIsReversed(key));
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
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 60;
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
            int maxUse = stack.getMaxItemUseDuration();
            inUse = maxUse - useRemaining;
            index = MathHelper.clamp_int(inUse / 8, 0, 4);
            /*if (inUse >= 18) { index += 3; }
            else if (inUse >= 13) { index += 2; }
            else if (inUse > 0) { index += 1; }*/
        }

        return this.getItemIconForUseDuration(index);
    }

    public class Area
    {
        public int rPosX;
        public int rPosY;
        public int rPosZ;
        public int rNegX;
        public int rNegY;
        public int rNegZ;

        public Area(int rPosX, int rPosY, int rPosZ, int rNegX, int rNegY, int rNegZ)
        {
            this.init(rPosX, rPosY, rPosZ, rNegX, rNegY, rNegZ);
        }

        public Area(ItemStack stack)
        {
            int mode = Mode.getModeOrdinal(stack);
            NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, false);
            if (wrapperTag != null && wrapperTag.hasKey(TAG_NAME_CONFIG_PRE + mode, Constants.NBT.TAG_COMPOUND) == true)
            {
                NBTTagCompound tag = wrapperTag.getCompoundTag(TAG_NAME_CONFIG_PRE + mode);
                this.init(tag.getByteArray("Area"));
            }
            else
            {
                this.init(2, 2, 2, 2, 2, 2);
            }
        }

        public void init(int rPosX, int rPosY, int rPosZ, int rNegX, int rNegY, int rNegZ)
        {
            this.rPosX = rPosX;
            this.rPosY = rPosY;
            this.rPosZ = rPosZ;
            this.rNegX = rNegX;
            this.rNegY = rNegY;
            this.rNegZ = rNegZ;
        }

        public void init(byte[] rArr)
        {
            if (rArr != null && rArr.length == 6)
            {
                this.init(rArr[0], rArr[1], rArr[2], rArr[3], rArr[4], rArr[5]);
            }
        }

        public void writeToNBT(ItemStack stack)
        {
            int mode = Mode.getModeOrdinal(stack);
            byte[] rArr = new byte[] { (byte)this.rPosX, (byte)this.rPosY, (byte)this.rPosZ, (byte)this.rNegX, (byte)this.rNegY, (byte)this.rNegZ };

            NBTTagCompound wrapperTag = NBTUtils.getCompoundTag(stack, WRAPPER_TAG_NAME, false);
            if (wrapperTag.hasKey(TAG_NAME_CONFIG_PRE + mode, Constants.NBT.TAG_COMPOUND) == false)
            {
                wrapperTag.setTag(TAG_NAME_CONFIG_PRE + mode, new NBTTagCompound());
            }

            NBTTagCompound tag = wrapperTag.getCompoundTag(TAG_NAME_CONFIG_PRE + mode);
            tag.setByteArray("Area", rArr);
        }
    }

    public Mode getMode(ItemStack stack)
    {
        return Mode.getMode(stack);
    }

    public static enum Mode
    {
        EXTEND_TARGETED ("enderutilities.tooltip.item.extend.targeted"),
        EXTEND_SAME ("enderutilities.tooltip.item.extend.samematerial"),
        EXTEND_FIXED ("enderutilities.tooltip.item.extend.fixedmaterial"),
        COLUMN ("enderutilities.tooltip.item.build.column"),
        LINE ("enderutilities.tooltip.item.build.line"),
        PLANE ("enderutilities.tooltip.item.build.plane"),
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
