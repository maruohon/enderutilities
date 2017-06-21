package fi.dy.masa.enderutilities.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
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

    protected Map<UUID, Long> lastLeftClick = new HashMap<UUID, Long>();

    public ItemRuler(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        // Hack to work around the fact that when the NBT changes, the left click event will fire again the next tick,
        // so it would easily result in the state toggling multiple times per left click
        Long last = this.lastLeftClick.get(player.getUniqueID());

        if (last == null || (world.getTotalWorldTime() - last) >= 6)
        {
            // When not sneaking, adjust the position to be the adjacent block and not the targeted block itself
            this.setOrRemovePosition(player.getHeldItem(hand), new BlockPosEU(pos, player.getEntityWorld().provider.getDimension(), side),
                    POS_END, player.isSneaking() == false);
        }

        this.lastLeftClick.put(player.getUniqueID(), world.getTotalWorldTime());

        return EnumActionResult.SUCCESS;
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
            // When not sneaking, adjust the position to be the adjacent block and not the targeted block itself
            this.setOrRemovePosition(stack, new BlockPosEU(pos, player.getEntityWorld().provider.getDimension(), side),
                    POS_START, player.isSneaking() == false);
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

        //String pre = TextFormatting.AQUA.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        if (displayName.toString().length() >= 14)
        {
            displayName = new StringBuilder(64).append(EUStringUtils.getInitialsWithDots(displayName.toString()));
        }

        int count = this.getLocationCount(rulerStack);

        if (count > 0)
        {
            int sel = this.getLocationSelection(rulerStack);
            displayName.append(" - sel: ").append(preGreen + (sel + 1)).append("/").append(count).append(rst);

            displayName.append(" - R: ");

            if (this.getAlwaysRenderLocation(rulerStack, sel))
            {
                displayName.append(preGreen + "yes" + rst);
            }
            else
            {
                displayName.append(preRed + "no" + rst);
            }
        }

        return displayName.toString();
    }

    @Override
    public void addTooltipLines(ItemStack rulerStack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (rulerStack.getTagCompound() == null)
        {
            list.add(I18n.format("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        String preDGreen = TextFormatting.DARK_GREEN.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String preRed = TextFormatting.RED.toString();
        String preBlue = TextFormatting.BLUE.toString();
        String preWhite = TextFormatting.WHITE.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        String strYes = preGreen + I18n.format("enderutilities.tooltip.item.yes") + rst;
        String strNo = preRed + I18n.format("enderutilities.tooltip.item.no") + rst;

        String str;

        int selected = this.getLocationSelection(rulerStack);
        BlockPosEU posStart = this.getPosition(rulerStack, selected, POS_START);
        BlockPosEU posEnd = this.getPosition(rulerStack, selected, POS_END);

        if (verbose == false)
        {
            if (posStart != null && posEnd != null)
            {
                list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s ... x: %s%d%s y: %s%d%s z: %s%d%s",
                        preBlue, posStart.getX(), rst, preBlue, posStart.getY(), rst, preBlue, posStart.getZ(), rst,
                        preBlue, posEnd.getX(), rst, preBlue, posEnd.getY(), rst, preBlue, posEnd.getZ(), rst));
            }

            return;
        }

        if (posStart != null)
        {
            str = I18n.format("enderutilities.tooltip.item.start");
            list.add(str + String.format(": x: %s%d%s, y: %s%d%s, z: %s%d%s", preBlue, posStart.getX(), rst,
                    preBlue, posStart.getY(), rst, preBlue, posStart.getZ(), rst));
        }

        if (posEnd != null)
        {
            str = I18n.format("enderutilities.tooltip.item.end");
            list.add(str + String.format(": x: %s%d%s, y: %s%d%s, z: %s%d%s", preBlue, posEnd.getX(), rst,
                    preBlue, posEnd.getY(), rst, preBlue, posEnd.getZ(), rst));
        }

        str = I18n.format("enderutilities.tooltip.item.mode") + ": ";

        if (this.getDistanceMode(rulerStack) == DISTANCE_MODE_DIMENSIONS)
        {
            list.add(str + preDGreen + I18n.format("enderutilities.tooltip.item.ruler.dimensions") + rst);
        }
        else
        {
            list.add(str + preDGreen + I18n.format("enderutilities.tooltip.item.ruler.difference") + rst);
        }

        str = I18n.format("enderutilities.tooltip.item.rendercurrentwithall");
        list.add(str + ": " + (this.getAlwaysRenderLocation(rulerStack, selected) ? strYes : strNo));

        str = I18n.format("enderutilities.tooltip.item.renderall");
        list.add(str + ": " + (this.getRenderAllLocations(rulerStack) ? strYes : strNo));

        str = I18n.format("enderutilities.tooltip.item.renderwhenunselected");
        list.add(str + ": " + (this.getRenderWhenUnselected(rulerStack) ? strYes : strNo));

        int count = this.getLocationCount(rulerStack);
        str = I18n.format("enderutilities.tooltip.item.selected") + ": ";
        list.add(str + preBlue + (selected + 1) + rst + " / " + preBlue + count + rst);

        int installed = this.getInstalledModuleCount(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (installed > 0)
        {
            int slotNum = UtilItemModular.getClampedModuleSelection(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
            int max = this.getMaxModules(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
            String preWhiteIta = preWhite + TextFormatting.ITALIC.toString();
            String strShort = I18n.format("enderutilities.tooltip.item.selectedmemorycard.short");

            ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

            if (moduleStack.isEmpty() == false && moduleStack.getItem() == EnderUtilitiesItems.ENDER_PART)
            {
                String dName = (moduleStack.hasDisplayName() ? preWhiteIta + moduleStack.getDisplayName() + rst + " " : "");
                list.add(String.format("%s %s(%s%d%s / %s%d%s)", strShort, dName, preBlue, slotNum + 1, rst, preBlue, max, rst));
            }
            else
            {
                String strNot = I18n.format("enderutilities.tooltip.item.selectedmemorycard.notinstalled");
                list.add(String.format("%s %s (%s%d%s / %s%d%s)", strShort, strNot, preBlue, slotNum + 1, rst, preBlue, max, rst));
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
        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD_MISC))
        {
            return this.getMaxModules(containerStack);
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

        return this.getMaxModules(containerStack, ((IModule) moduleStack.getItem()).getModuleType(moduleStack));
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
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
        {
            return NBTUtils.getByte(moduleStack, TAG_WRAPPER, TAG_SELECTED_LOCATION);
        }

        return 0;
    }

    public int getLocationCount(ItemStack rulerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
        {
            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, false);
            return tagList != null ? tagList.tagCount() : 0;
        }

        return 0;
    }

    public void cycleLocationSelection(ItemStack rulerStack, boolean reverse)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
        {
            int max = Math.min(this.getLocationCount(rulerStack), 7);
            NBTUtils.cycleByteValue(moduleStack, TAG_WRAPPER, TAG_SELECTED_LOCATION, max, reverse);
        }
    }

    public boolean getAlwaysRenderLocation(ItemStack rulerStack, int index)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
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
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
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

            String tagName = isPos1 ? "Pos1" : "Pos2";
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
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
        {
            if (adjustPosition)
            {
                pos = pos.offset(pos.getFacing());
            }

            int selected = this.getLocationSelection(rulerStack);
            NBTTagList tagList = NBTUtils.getTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, Constants.NBT.TAG_COMPOUND, true);

            if (selected >= tagList.tagCount())
            {
                tagList.appendTag(new NBTTagCompound());
            }

            NBTTagCompound tag = tagList.getCompoundTagAt(selected);
            String tagName = isPos1 ? "Pos1" : "Pos2";
            BlockPosEU oldPos = BlockPosEU.readFromTag(tag.getCompoundTag(tagName));

            if (pos == null || pos.equals(oldPos))
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
                tagList.set(selected, tag);
            }

            NBTUtils.setTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, tagList);

            this.setSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);
        }
    }

    public void toggleAlwaysRenderSelectedLocation(ItemStack rulerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
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
                tagList.set(selected, tag);
            }

            NBTUtils.setTagList(moduleStack, TAG_WRAPPER, TAG_LOCATIONS, tagList);

            this.setSelectedModuleStack(rulerStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Shift + Toggle key: Cycle location selection
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT) ||
            EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT))
        {
            this.cycleLocationSelection(stack, EnumKey.keypressActionIsReversed(key));
        }
        // Alt + Toggle key: Toggle "Render when unselected"
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_ALT))
        {
            this.toggleRenderWhenUnselected(stack);
        }
        // Ctrl + Alt + Toggle key: Toggle distance display mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL_ALT))
        {
            this.toggleDistanceMode(stack);
        }
        // Alt + Shift + Toggle key: Toggle "Render all locations"
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            this.toggleRenderAllLocations(stack);
        }
        // Just Toggle key: Toggle the "Render with all" option on the selected location
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            this.toggleAlwaysRenderSelectedLocation(stack);
        }
        // Ctrl + Toggle key: Change selected Memory Card
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_MISC,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
        }
    }
}
