package fi.dy.masa.enderutilities.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemRuler extends ItemModular
{
    public static final boolean DISTANCE_MODE_DIMENSIONS = false;
    public static final boolean DISTANCE_MODE_DIFFERENCE = true;
    public static final boolean POS_START = true;
    public static final boolean POS_END = false;

    // These tags are in the item itself
    public static final String TAG_WRAPPER = "Ruler";
    public static final String TAG_DISTANCE_MODE = "DistanceMode";
    public static final String TAG_RENDER_WHEN_UNSELECTED = "RenderUnselected";
    public static final String TAG_RENDER_ALL = "RenderAll";

    // These tags are on the memory cards
    public static final String TAG_RENDER_WITH_ALL = "RenderWithAll";
    public static final String TAG_LOCATIONS = "Locations";
    public static final String TAG_SELECTED_LOCATION = "SelLocation";

    protected Map<UUID, Long> lastLeftClick;

    public ItemRuler()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_RULER);
        this.lastLeftClick = new HashMap<UUID, Long>();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return false;
        }

        // Hack to work around the fact that when the NBT changes, the left click event will fire again the next tick,
        // so it would easily result in the state toggling multiple times per left click
        Long last = this.lastLeftClick.get(player.getUniqueID());
        if (last == null || (world.getTotalWorldTime() - last) >= 6)
        {
            // When not sneaking, adjust the position to be the adjacent block and not the targeted block itself
            this.setOrRemovePosition(stack, new BlockPosEU(x, y, z, player.dimension, side), POS_START, player.isSneaking() == false);
        }

        this.lastLeftClick.put(player.getUniqueID(), world.getTotalWorldTime());

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        return stack;
    }

    public void onLeftClickBlock(EntityPlayer player, World world, ItemStack stack, int x, int y, int z, int dimension, int side)
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
            // When not sneaking, adjust the position to be the adjacent block and not the targeted block itself
            this.setOrRemovePosition(stack, new BlockPosEU(x, y, z, player.dimension, side), POS_END, player.isSneaking() == false);
        }

        this.lastLeftClick.put(player.getUniqueID(), world.getTotalWorldTime());
    }

    @Override
    public String getItemStackDisplayName(ItemStack rulerStack)
    {
        StringBuilder displayName = new StringBuilder(64);
        displayName.append(super.getItemStackDisplayName(rulerStack));

        if (rulerStack.getTagCompound() == null)
        {
            return displayName.toString();
        }

        //String pre = EnumChatFormatting.AQUA.toString();
        String preGreen = EnumChatFormatting.GREEN.toString();
        String preRed = EnumChatFormatting.RED.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        if (displayName.toString().length() >= 14)
        {
            displayName = new StringBuilder(64).append(EUStringUtils.getInitialsWithDots(displayName.toString()));
        }

        /*displayName.append(" - A: ");
        if (this.getRenderAllLocations(rulerStack) == true)
        {
            displayName.append(preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst);
        }
        else
        {
            displayName.append(preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst);
        }*/

        int count = this.getLocationCount(rulerStack);
        if (count > 0)
        {
            int sel = this.getLocationSelection(rulerStack);
            displayName.append(" - P: ").append(preGreen + (sel + 1)).append("/").append(count).append(rst);

            displayName.append(" - R: ");

            if (this.getAlwaysRenderLocation(rulerStack, sel) == true)
            {
                displayName.append(preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst);
            }
            else
            {
                displayName.append(preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst);
            }
        }

        return displayName.toString();
    }

    @Override
    public void addInformationSelective(ItemStack rulerStack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (rulerStack.getTagCompound() == null)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        String preDGreen = EnumChatFormatting.DARK_GREEN.toString();
        String preGreen = EnumChatFormatting.GREEN.toString();
        String preRed = EnumChatFormatting.RED.toString();
        String preBlue = EnumChatFormatting.BLUE.toString();
        String preWhite = EnumChatFormatting.WHITE.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        String str;
        str = StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": ";
        if (this.getDistanceMode(rulerStack) == DISTANCE_MODE_DIMENSIONS)
        {
            str = str + preDGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.dimensions") + rst;
        }
        else
        {
            str = str + preDGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.difference") + rst;
        }
        list.add(str);

        int sel = this.getLocationSelection(rulerStack);
        str = StatCollector.translateToLocal("enderutilities.tooltip.item.rendercurrentwithall");
        if (this.getAlwaysRenderLocation(rulerStack, sel) == true)
        {
            list.add(str + ": " + preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst);
        }
        else
        {
            list.add(str + ": " + preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst);
        }

        str = StatCollector.translateToLocal("enderutilities.tooltip.item.renderall") + ": ";
        if (this.getRenderAllLocations(rulerStack) == true)
        {
            str = str + preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = str + preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst;
        }
        list.add(str);

        str = StatCollector.translateToLocal("enderutilities.tooltip.item.renderwhenunselected") + ": ";
        if (this.getRenderWhenUnselected(rulerStack) == true)
        {
            str = str + preGreen + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = str + preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst;
        }
        list.add(str);

        int count = this.getLocationCount(rulerStack);
        str = StatCollector.translateToLocal("enderutilities.tooltip.item.selected") + ": ";
        list.add(str + preBlue + (sel + 1) + rst + " / " + preBlue + count + rst);

        int installed = this.getInstalledModuleCount(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (installed > 0)
        {
            int slotNum = UtilItemModular.getClampedModuleSelection(rulerStack, ModuleType.TYPE_MEMORY_CARD);
            int max = this.getMaxModules(rulerStack, ModuleType.TYPE_MEMORY_CARD);
            String preWhiteIta = preWhite + EnumChatFormatting.ITALIC.toString();
            String strShort = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short");

            ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
            if (moduleStack != null && moduleStack.getItem() == EnderUtilitiesItems.enderPart)
            {
                String dName = (moduleStack.hasDisplayName() ? preWhiteIta + moduleStack.getDisplayName() + rst + " " : "");
                list.add(String.format("%s %s(%s%d%s / %s%d%s)", strShort, dName, preBlue, slotNum + 1, rst, preBlue, max, rst));
            }
            else
            {
                String strNo = StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.notinstalled");
                list.add(String.format("%s %s (%s%d%s / %s%d%s)", strShort, strNo, preBlue, slotNum + 1, rst, preBlue, max, rst));
            }
        }
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD))
        {
            return this.getMaxModules(containerStack);
        }

        return 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack != null && (moduleStack.getItem() instanceof IModule))
        {
            IModule module = (IModule)moduleStack.getItem();

            if (ModuleType.TYPE_MEMORY_CARD.equals(module.getModuleType(moduleStack)) && module.getModuleTier(moduleStack) == ItemEnderPart.MEMORY_CARD_TYPE_MISC)
            {
                return this.getMaxModules(containerStack);
            }
        }

        return 0;
    }

    public boolean getDistanceMode(ItemStack rulerStack)
    {
        return NBTUtils.getBoolean(rulerStack, TAG_WRAPPER, TAG_DISTANCE_MODE);
    }

    public boolean getRenderWhenUnselected(ItemStack rulerStack)
    {
        return NBTUtils.getBoolean(rulerStack, TAG_WRAPPER, TAG_RENDER_WHEN_UNSELECTED);
    }

    public boolean getRenderAllLocations(ItemStack rulerStack)
    {
        return NBTUtils.getBoolean(rulerStack, TAG_WRAPPER, TAG_RENDER_ALL);
    }

    public void toggleDistanceMode(ItemStack rulerStack)
    {
        NBTUtils.toggleBoolean(rulerStack, TAG_WRAPPER, TAG_DISTANCE_MODE);
    }

    public void toggleRenderWhenUnselected(ItemStack rulerStack)
    {
        NBTUtils.toggleBoolean(rulerStack, TAG_WRAPPER, TAG_RENDER_WHEN_UNSELECTED);
    }

    public void toggleRenderAllLocations(ItemStack rulerStack)
    {
        NBTUtils.toggleBoolean(rulerStack, TAG_WRAPPER, TAG_RENDER_ALL);
    }

    public int getLocationSelection(ItemStack rulerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            return NBTUtils.getByte(moduleStack, TAG_WRAPPER, TAG_SELECTED_LOCATION);
        }

        return 0;
    }

    public int getLocationCount(ItemStack rulerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, false);
            return tagList != null ? tagList.tagCount() : 0;
        }

        return 0;
    }

    public void cycleLocationSelection(ItemStack rulerStack, boolean reverse)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            int max = Math.min(this.getLocationCount(rulerStack), 7);
            NBTUtils.cycleByteValue(moduleStack, TAG_WRAPPER, TAG_SELECTED_LOCATION, max, reverse);
        }
    }

    public boolean getAlwaysRenderLocation(ItemStack rulerStack, int index)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            if (index < 0)
            {
                index = this.getLocationSelection(rulerStack);
            }

            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, false);
            if (tagList != null)
            {
                NBTTagCompound tag = tagList.getCompoundTagAt(index);
                return tag.getBoolean(TAG_RENDER_WITH_ALL);
            }
        }

        return false;
    }

    public BlockPosEU getPosition(ItemStack rulerStack, int index, boolean isPos1)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, false);
            if (tagList == null)
            {
                return null;
            }

            if (index < 0)
            {
                index = this.getLocationSelection(rulerStack);
            }

            String tagName = isPos1 == true ? "Pos1" : "Pos2";
            NBTTagCompound tag = tagList.getCompoundTagAt(index);

            return BlockPosEU.readFromTag(tag.getCompoundTag(tagName));
        }

        return null;
    }

    /**
     * Writes the given block position to the selected module, to the selected position.
     * If the given position is null, or is equal to the stored position, then the position is removed.
     */
    public void setOrRemovePosition(ItemStack rulerStack, BlockPosEU pos, boolean isPos1, boolean adjustPosition)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            if (adjustPosition == true)
            {
                pos = pos.offset(ForgeDirection.getOrientation(pos.face), 1);
            }

            int selected = this.getLocationSelection(rulerStack);
            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, true);
            if (selected >= tagList.tagCount())
            {
                tagList.appendTag(new NBTTagCompound());
            }

            NBTTagCompound tag = tagList.getCompoundTagAt(selected);
            String tagName = isPos1 == true ? "Pos1" : "Pos2";
            BlockPosEU oldPos = BlockPosEU.readFromTag(tag.getCompoundTag(tagName));

            if (pos == null || pos.equals(oldPos) == true)
            {
                tag.removeTag(tagName);
            }
            else
            {
                tag.setTag(tagName, pos.writeToTag(new NBTTagCompound()));
            }

            if (selected >= tagList.tagCount())
            {
                tagList = NBTUtils.insertToTagList(tagList, tag, selected);
            }
            else
            {
                tagList.func_150304_a(selected, tag);
            }

            NBTUtils.setTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, tagList);

            this.setSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD, moduleStack);
        }
    }

    public void toggleAlwaysRenderSelectedLocation(ItemStack rulerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            int selected = this.getLocationSelection(rulerStack);
            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, true);
            NBTTagCompound tag = tagList.getCompoundTagAt(selected);

            NBTUtils.toggleBoolean(tag, TAG_RENDER_WITH_ALL);

            if (selected >= tagList.tagCount())
            {
                tagList = NBTUtils.insertToTagList(tagList, tag, selected);
            }
            else
            {
                tagList.func_150304_a(selected, tag);
            }

            NBTUtils.setTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, tagList);

            this.setSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD, moduleStack);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack rulerStack, int key)
    {
        if (rulerStack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Shift + Toggle key: Cycle location selection
        if (ReferenceKeys.keypressContainsControl(key) == false &&
            ReferenceKeys.keypressContainsShift(key) == true &&
            ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.cycleLocationSelection(rulerStack, ReferenceKeys.keypressActionIsReversed(key));
        }
        // Alt + Toggle key: Toggle "Render when unselected"
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == false &&
                 ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.toggleRenderWhenUnselected(rulerStack);
        }
        // Ctrl + Alt + Toggle key: Toggle distance display mode
        else if (ReferenceKeys.keypressContainsControl(key) == true &&
                 ReferenceKeys.keypressContainsShift(key) == false &&
                 ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.toggleDistanceMode(rulerStack);
        }
        // Alt + Shift + Toggle key: Toggle "Render all locations"
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == true)
        {
            this.toggleRenderAllLocations(rulerStack);
        }
        // Just Toggle key: Toggle the "Render with all" option on the selected location
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == false &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.toggleAlwaysRenderSelectedLocation(rulerStack);
        }
        // Ctrl + Toggle key: Change selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(rulerStack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
    }
}
