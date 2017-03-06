package fi.dy.masa.enderutilities.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.entity.EntityChair;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemChairWand  extends ItemEnderUtilities implements IKeyBound
{
    public ItemChairWand()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_CHAIR_WAND);
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote == false)
        {
            float yOffset = playerIn.isSneaking() ? -hitY : 0;
            EntityChair chair = new EntityChair(worldIn);
            chair.setWidth(this.getEntityWidth(stack));
            chair.setHeight(this.getEntityHeight(stack));
            chair.setPosition(pos.getX() + hitX, pos.getY() + hitY + yOffset, pos.getZ() + hitZ);
            worldIn.spawnEntity(chair);
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity)
    {
        if (player.getEntityWorld().isRemote == false && entity instanceof EntityChair)
        {
            entity.setDead();
            return true;
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    private float getEntityWidth(ItemStack stack)
    {
        byte value = NBTUtils.getByte(stack, "Chair", "Width");
        return value == 0 ? 0.5f : (float) value / 16f;
    }

    private float getEntityHeight(ItemStack stack)
    {
        byte value = NBTUtils.getByte(stack, "Chair", "Height");
        return value == 0 ? 0.75f : (float) value / 16f;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String itemName = this.getBaseItemDisplayName(stack);
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt != null)
        {
            float width = this.getEntityWidth(stack);
            float height = this.getEntityHeight(stack);
            itemName = String.format("%s - w: %.3f, h: %.3f", itemName, width, height);
        }

        return itemName;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Shift + Scroll: Change entity width
        if (EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT))
        {
            NBTUtils.cycleByteValue(stack, "Chair", "Width", 1, 64, EnumKey.keypressActionIsReversed(key) == false);
        }
        // Alt + Scroll: Change entity height
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_ALT))
        {
            NBTUtils.cycleByteValue(stack, "Chair", "Height", 1, 64, EnumKey.keypressActionIsReversed(key) == false);
        }
    }
}
