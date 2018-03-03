package fi.dy.masa.enderutilities.item.block;

import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.ItemType;
import fi.dy.masa.enderutilities.util.PlacementProperties;
import fi.dy.masa.enderutilities.util.PlacementProperties.PlacementProperty;

public class ItemBlockPlacementProperty extends ItemBlockEnderUtilities implements IKeyBound
{
    private final HashMap<Integer, PlacementProperty> placementProperties = new HashMap<Integer, PlacementProperty>();

    public ItemBlockPlacementProperty(BlockEnderUtilities block)
    {
        super(block);
    }

    public boolean hasPlacementProperty(ItemStack stack)
    {
        return this.placementProperties.containsKey(OreDictionary.WILDCARD_VALUE) ||
               this.placementProperties.containsKey(stack.getMetadata());
    }

    @Nullable
    public PlacementProperty getPlacementProperty(ItemStack stack)
    {
        PlacementProperty pp = this.placementProperties.get(stack.getMetadata());

        if (pp == null)
        {
            pp = this.placementProperties.get(OreDictionary.WILDCARD_VALUE);
        }

        return pp;
    }

    @Nonnull
    public PlacementProperty getOrCreatePlacementProperty(int stackMeta)
    {
        PlacementProperty pp = this.placementProperties.get(stackMeta);

        if (pp == null)
        {
            pp = new PlacementProperty();
            this.placementProperties.put(stackMeta, pp);
        }

        return pp;
    }

    public void addPlacementProperty(int stackMeta, String key, int type, int minValue, int maxValue)
    {
        PlacementProperty pp = this.getOrCreatePlacementProperty(stackMeta);
        pp.addProperty(key, type, minValue, maxValue);
    }

    public void addPlacementPropertyValueNames(int stackMeta, String key, String[] names)
    {
        PlacementProperty pp = this.getOrCreatePlacementProperty(stackMeta);
        pp.addValueNames(key, names);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        ItemBlockPlacementProperty item = (ItemBlockPlacementProperty) stack.getItem();

        if (item.hasPlacementProperty(stack) && player instanceof EntityPlayerMP)
        {
            PlacementProperty pp = this.getPlacementProperty(stack);
            ItemType type = new ItemType(stack, pp.isNBTSensitive());
            int index = PlacementProperties.getInstance().getPropertyIndex(player.getUniqueID(), type);

            if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE) || EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
            {
                index += EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT) ? -1 : 1;
                if (index < 0) { index = Math.max(0, pp.getPropertyCount() - 1); }
                else if (index >= pp.getPropertyCount()) { index = 0; }

                PlacementProperties.getInstance().setPropertyIndex(player.getUniqueID(), type, index);
                PlacementProperties.getInstance().syncCurrentlyHeldItemDataForPlayer((EntityPlayerMP) player, stack);
            }
            else
            {
                Pair<String, Integer> pair = pp.getProperty(index);
                Pair<Integer, Integer> range = pp.getPropertyValueRange(index);
                int minValue = range != null ? range.getLeft() : 0;
                int maxValue = range != null ? range.getRight() : 1;

                if (pair != null && EnumKey.getBaseKey(key) == EnumKey.SCROLL.getKeyCode() &&
                    (EnumKey.keypressContainsShift(key) == false || EnumKey.keypressContainsControl(key) || EnumKey.keypressContainsAlt(key)))
                {
                    int change = EnumKey.keypressActionIsReversed(key) ? -1 : 1;

                    if (EnumKey.keypressContainsShift(key))
                    {
                        change *= 10;
                    }

                    if (EnumKey.keypressContainsControl(key))
                    {
                        change *= EnumKey.keypressContainsAlt(key) ? 100 : 10;
                    }

                    int value = PlacementProperties.getInstance().getPropertyValue(player.getUniqueID(), type, pair.getLeft(), pair.getRight());
                    value += change;

                    if (value < minValue) { value = maxValue; }
                    if (value > maxValue) { value = minValue; }

                    PlacementProperties.getInstance().setPropertyValue(player.getUniqueID(), type, pair.getLeft(), pair.getRight(), value);
                    PlacementProperties.getInstance().syncCurrentlyHeldItemDataForPlayer((EntityPlayerMP) player, stack);
                }
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (this.hasPlacementProperty(stack))
        {
            ItemType type = new ItemType(stack, this.getPlacementProperty(stack).isNBTSensitive());
            stack = stack.copy();

            EnumActionResult result = super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);

            if (result == EnumActionResult.SUCCESS)
            {
                NBTTagCompound tag = PlacementProperties.getInstance().getPropertyTag(player.getUniqueID(), type);

                if (tag != null)
                {
                    Block block = worldIn.getBlockState(pos).getBlock();

                    if (block.isReplaceable(worldIn, pos) == false)
                    {
                        pos = pos.offset(facing);
                        block = worldIn.getBlockState(pos).getBlock();
                    }

                    if (block instanceof BlockEnderUtilities)
                    {
                        ((BlockEnderUtilities) block).setPlacementProperties(worldIn, pos, stack, tag);
                    }
                }
            }

            return result;
        }
        else
        {
            return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
        }
    }

    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        super.addTooltips(stack, list, verbose);

        if (this.hasPlacementProperty(stack))
        {
            ItemEnderUtilities.addTranslatedTooltip(Reference.MOD_ID + ".tooltip.placementproperties.tooltips", list, verbose);
        }
    }
}
