package fi.dy.masa.enderutilities.item.part;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderPart extends ItemModule
{
    public static final int ENDER_CORE_TYPE_INACTIVE_BASIC      = 10;
    public static final int ENDER_CORE_TYPE_INACTIVE_ENHANCED   = 11;
    public static final int ENDER_CORE_TYPE_INACTIVE_ADVANCED   = 12;
    public static final int ENDER_CORE_TYPE_ACTIVE_BASIC        = 0;
    public static final int ENDER_CORE_TYPE_ACTIVE_ENHANCED     = 1;
    public static final int ENDER_CORE_TYPE_ACTIVE_ADVANCED     = 2;

    public static final int MEMORY_CARD_TYPE_MISC       = 0;
    public static final int MEMORY_CARD_TYPE_ITEMS_6B   = 6;
    public static final int MEMORY_CARD_TYPE_ITEMS_8B   = 8;
    public static final int MEMORY_CARD_TYPE_ITEMS_10B  = 10;
    public static final int MEMORY_CARD_TYPE_ITEMS_12B  = 12;

    public ItemEnderPart()
    {
        super();
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERPART);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        int damage = stack.getMetadata();

        switch (damage)
        {
            case 0: // Damage 0: Ender Alloy (Basic)
            case 1: // Damage 1: Ender Alloy (Enhanced)
            case 2: // Damage 2: Ender Alloy (Advanced)
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "_" + damage;

            case 10: // Damage 10: Inactive Ender Core (Basic)
            case 11: // Damage 11: Inactive Ender Core (Enhanced)
            case 12: // Damage 12: Inactive Ender Core (Advanced)
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "_" + (damage - 10) + "_inactive";

            case 15: // Damage 15: Ender Core (Basic)
            case 16: // Damage 16: Ender Core (Enhanced)
            case 17: // Damage 17: Ender Core (Advanced)
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "_" + (damage - 15) + "_active";

            case 20: // Damage 20: Ender Stick
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK;

            case 21: // Damage 21: Ender Rope
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE;

            case 30: // Damage 30: Creative Breaking module
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_CREATIVE_BREAKING;

            case 40: // Damage 40: Ender Relic
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_ENDERRELIC;

            case 45: // Damage 45: Mob Persistence
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_JAILER;

            case 50: // Damage 50: Memory Card (misc)
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_MISC;

            case 51: // Damage 51: Memory Card (items) 6 B
            case 52: // Damage 52: Memory Card (items) 8 B
            case 53: // Damage 53: Memory Card (items) 10 B
            case 54: // Damage 54: Memory Card (items) 12 B
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS + "_" + (damage - 51);

            case 70: // Barrel Label
            case 71: // Barrel Structural Upgrade
            case 72: // Barrel Capacity Upgrade
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_BARREL_UPGRADE + "_" + (damage - 70);

            case 80: // Storage Key
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_STORAGE_KEY;
            case 81: // Creative Storage Key
                return super.getUnlocalizedName() + "_" + ReferenceNames.NAME_ITEM_ENDERPART_STORAGE_KEY + "_creative";
        }

        return super.getUnlocalizedName();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return EnumActionResult.PASS;
        }

        ItemStack stack = player.getHeldItem(hand);

        // Ender Relic
        if (stack.getMetadata() == 40 && EntityUtils.spawnEnderCrystal(world, pos))
        {
            if (player.capabilities.isCreativeMode == false)
            {
                stack.shrink(1);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (worldIn.isRemote == false && this.getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD_ITEMS))
        {
            if (playerIn.isSneaking())
            {
                OwnerData.removeOwnerDataFromItem(stack, playerIn);
            }
            else
            {
                OwnerData.togglePrivacyModeOnItem(stack, playerIn);
            }

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }

        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase, EnumHand hand)
    {
        // Jailer module
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            if (livingBase instanceof EntityLiving && EntityUtils.applyMobPersistence((EntityLiving)livingBase))
            {
                if (player.getEntityWorld().isRemote == false && player.capabilities.isCreativeMode == false)
                {
                    stack.shrink(1);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        String preWh = TextFormatting.WHITE.toString();
        String preRed = TextFormatting.RED.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        String strOwner = I18n.format("enderutilities.tooltip.item.owner");

        // Set to private and not the owner
        OwnerData ownerData = OwnerData.getOwnerDataFromItem(stack);

        if (ownerData != null && ownerData.canAccess(player) == false)
        {
            list.add(String.format("%s: %s%s%s - %s%s%s", strOwner, preWh, ownerData.getOwnerName(), rst,
                    preRed, I18n.format("enderutilities.tooltip.item.private"), rst));
            return;
        }

        int meta = stack.getMetadata();
        NBTTagCompound nbt = stack.getTagCompound();

        if (meta >= 50 && meta <= 54 && (nbt == null || nbt.hasNoTags()))
        {
            list.add(I18n.format("enderutilities.tooltip.item.memorycard.nodata"));
            return;
        }

        if (meta == 50) // Memory Card (misc)
        {
            ArrayList<String> listDataTypes = new ArrayList<String>();
            Iterator<String> iter = nbt.getKeySet().iterator();

            while (iter.hasNext())
            {
                String key = iter.next();

                if (key != null && key.equals("display") == false && key.equals("RepairCost") == false)
                {
                    listDataTypes.add("  " + key);
                }
            }

            if (listDataTypes.size() > 0)
            {
                list.add(I18n.format("enderutilities.tooltip.item.memorycard.datatypecount", listDataTypes.size()));
                list.addAll(listDataTypes);
            }
            else
            {
                list.add(I18n.format("enderutilities.tooltip.item.memorycard.nodata"));
            }
        }
        else if (meta >= 51 && meta <= 54) // Memory Card (items)
        {
            ArrayList<String> lines = new ArrayList<String>();
            int itemCount = UtilItemModular.getFormattedItemListFromContainerItem(stack, lines, 20);

            if (lines.size() > 0)
            {
                NBTTagList tagList = NBTUtils.getStoredItemsList(stack, false);
                int stackCount = tagList != null ? tagList.tagCount() : 0;
                list.add(I18n.format("enderutilities.tooltip.item.memorycard.items.stackcount", stackCount, itemCount));
                list.addAll(lines);
            }
            else
            {
                list.add(I18n.format("enderutilities.tooltip.item.memorycard.noitems"));
            }
        }

        // Print the owner data after the contents if the player can access/see the contents
        if (ownerData != null)
        {
            String mode = ownerData.getIsPublic() ? I18n.format("enderutilities.tooltip.item.public") : I18n.format("enderutilities.tooltip.item.private");
            String modeColor = ownerData.getIsPublic() ? TextFormatting.GREEN.toString() : preRed;
            list.add(String.format("%s: %s%s%s - %s%s%s", strOwner, preWh, ownerData.getOwnerName(), rst, modeColor, mode, rst));
        }
    }

    public void activateEnderCore(ItemStack stack)
    {
        int meta = stack.getMetadata();
        // Inactive Ender Cores

        if (meta >= 10 && meta <= 12)
        {
            // "Activate" the Ender Core (ie. change the item)
            stack.setItemDamage(meta + 5);
        }
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        int meta = stack.getMetadata();

        // Inactive Ender Cores
        // Active Ender Cores
        if ((meta >= 10 && meta <= 12) ||
            (meta >= 15 && meta <= 17))
        {
            return ModuleType.TYPE_ENDERCORE;
        }

        // Creative Breaking module
        if (meta == 30)
        {
            return ModuleType.CREATIVE_BREAKING;
        }

        // Mob Persistence
        if (meta == 45)
        {
            return ModuleType.TYPE_MOBPERSISTENCE;
        }

        // Memory Card (misc)
        if (meta == 50)
        {
            return ModuleType.TYPE_MEMORY_CARD_MISC;
        }

        // Memory Card (items)
        if (meta >= 51 && meta <= 54)
        {
            return ModuleType.TYPE_MEMORY_CARD_ITEMS;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        int meta = stack.getMetadata();

        // Inactive Ender Cores
        if (meta >= 10 && meta <= 12)
        {
            return meta - 10 + ENDER_CORE_TYPE_INACTIVE_BASIC;
        }

        // Active Ender Cores
        if (meta >= 15 && meta <= 17)
        {
            return meta - 15 + ENDER_CORE_TYPE_ACTIVE_BASIC;
        }

        // Creative Breaking module
        if (meta == 30)
        {
            return 0;
        }

        // Mob Persistence
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 0;
        }

        // Memory Card (misc)
        if (meta == 50)
        {
            return MEMORY_CARD_TYPE_MISC;
        }

        // Memory Card (items)
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD_ITEMS))
        {
            int tier = meta - 51;

            switch (tier)
            {
                case 0: return MEMORY_CARD_TYPE_ITEMS_6B;
                case 1: return MEMORY_CARD_TYPE_ITEMS_8B;
                case 2: return MEMORY_CARD_TYPE_ITEMS_10B;
                case 3: return MEMORY_CARD_TYPE_ITEMS_12B;
            }
        }

        return -1; // Invalid item (= non-module)
    }

    public static boolean itemMatches(ItemStack stack, ItemPartType type)
    {
        return stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.ENDER_PART && stack.getMetadata() == type.getMeta();
    }

    public enum ItemPartType
    {
        STORAGE_KEY             (80),
        CREATIVE_STORAGE_KEY    (81);

        private final int meta;

        private ItemPartType(int meta)
        {
            this.meta = meta;
        }

        public int getMeta()
        {
            return this.meta;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        // Ender Alloys
        for (int i = 0; i <= 2; i++)
        {
            list.add(new ItemStack(this, 1, i));
        }

        // Inactive Ender Cores
        for (int i = 10; i <= 12; i++)
        {
            list.add(new ItemStack(this, 1, i));
        }

        // (Active) Ender Cores
        for (int i = 15; i <= 17; i++)
        {
            list.add(new ItemStack(this, 1, i));
        }

        list.add(new ItemStack(this, 1, 20)); // Ender Stick
        list.add(new ItemStack(this, 1, 21)); // Ender Rope
        list.add(new ItemStack(this, 1, 30)); // Creative Breaking module
        list.add(new ItemStack(this, 1, 40)); // Ender Relic
        list.add(new ItemStack(this, 1, 45)); // Mob Persistence
        list.add(new ItemStack(this, 1, 50)); // Memory Card (misc)
        list.add(new ItemStack(this, 1, 51)); // Memory Card (items) 6 B
        list.add(new ItemStack(this, 1, 52)); // Memory Card (items) 8 B
        list.add(new ItemStack(this, 1, 53)); // Memory Card (items) 10 B
        list.add(new ItemStack(this, 1, 54)); // Memory Card (items) 12 B
        list.add(new ItemStack(this, 1, 70)); // Barrel Label
        list.add(new ItemStack(this, 1, 71)); // Barrel Structural Upgrade
        list.add(new ItemStack(this, 1, 72)); // Barrel Capacity Upgrade
        list.add(new ItemStack(this, 1, 80)); // Storage Key
        list.add(new ItemStack(this, 1, 81)); // Creative Storage Key
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "tex=enderalloy_0"),
                new ModelResourceLocation(rl, "tex=enderalloy_1"),
                new ModelResourceLocation(rl, "tex=enderalloy_2"),
                new ModelResourceLocation(rl, "tex=endercore_inactive_0"),
                new ModelResourceLocation(rl, "tex=endercore_inactive_1"),
                new ModelResourceLocation(rl, "tex=endercore_inactive_2"),
                new ModelResourceLocation(rl, "tex=endercore_active_0"),
                new ModelResourceLocation(rl, "tex=endercore_active_1"),
                new ModelResourceLocation(rl, "tex=endercore_active_2"),
                new ModelResourceLocation(rl, "tex=enderstick"),
                new ModelResourceLocation(rl, "tex=enderrope"),
                new ModelResourceLocation(rl, "tex=creative_breaking"),
                new ModelResourceLocation(rl, "tex=enderrelic"),
                new ModelResourceLocation(rl, "tex=jailer"),
                new ModelResourceLocation(rl, "tex=memorycard_misc"),
                new ModelResourceLocation(rl, "tex=memorycard_items_6b"),
                new ModelResourceLocation(rl, "tex=memorycard_items_8b"),
                new ModelResourceLocation(rl, "tex=memorycard_items_10b"),
                new ModelResourceLocation(rl, "tex=memorycard_items_12b"),
                new ModelResourceLocation(rl, "tex=barrel_label"),
                new ModelResourceLocation(rl, "tex=barrel_structure"),
                new ModelResourceLocation(rl, "tex=barrel_capacity"),
                new ModelResourceLocation(rl, "tex=storage_key"),
                new ModelResourceLocation(rl, "tex=storage_key_creative")
        };
    }
}
