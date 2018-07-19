package fi.dy.masa.enderutilities.item.part;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class ItemLinkCrystal extends ItemLocationBound implements IModule
{
    public static final int TYPE_LOCATION = 0;
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_PORTAL = 2;

    public ItemLinkCrystal(String name)
    {
        super(name);

        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        // Damage 0: Link Crystal (In-World)
        // Damage 1: Link Crystal (Inventory)
        // Damage 2: Link Crystal (Portal)
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 2)
        {
            return super.getTranslationKey() + "_" + stack.getMetadata();
        }

        return super.getTranslationKey();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getMetadata() == TYPE_PORTAL)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.getMetadata() == TYPE_PORTAL && world.getBlockState(pos).getBlock() != EnderUtilitiesBlocks.PORTAL)
        {
            return EnumActionResult.PASS;
        }

        return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public boolean doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Alt + Shift + Toggle mode: Store the player's current location, including rotation
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            if (stack.getMetadata() == TYPE_PORTAL)
            {
                return false;
            }
            else
            {
                this.setTarget(stack, player, true);
                return true;
            }
        }

        return super.doKeyBindingAction(player, stack, key);
    }

    @Override
    public String getTargetDisplayName(ItemStack stack)
    {
        // Location type Link Crystal
        if (this.getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION)
        {
            TargetData target = TargetData.getTargetFromItem(stack);
            return target != null ? target.getDimensionName(true) : null;
        }

        return super.getTargetDisplayName(stack);
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 2)
        {
            return ModuleType.TYPE_LINKCRYSTAL;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 2)
        {
            return stack.getMetadata();
        }

        return -1;
    }

    @Override
    public void getSubItemsCustom(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0)); // Location
        list.add(new ItemStack(this, 1, 1)); // Block
        list.add(new ItemStack(this, 1, 2)); // Portal
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "type=location"),
                new ModelResourceLocation(rl, "type=block"),
                new ModelResourceLocation(rl, "type=portal")
        };
    }
}
