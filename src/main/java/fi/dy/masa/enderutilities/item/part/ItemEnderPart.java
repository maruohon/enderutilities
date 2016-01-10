package fi.dy.masa.enderutilities.item.part;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderPart extends ItemModule
{
    public static final int MEMORY_CARD_TYPE_MISC = 0;
    public static final int MEMORY_CARD_TYPE_ITEMS_6B  = 6;
    public static final int MEMORY_CARD_TYPE_ITEMS_8B  = 8;
    public static final int MEMORY_CARD_TYPE_ITEMS_10B = 10;
    public static final int MEMORY_CARD_TYPE_ITEMS_12B = 12;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
    @SideOnly(Side.CLIENT)
    private IIcon[] slotBackgrounds;

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
        int damage = stack.getItemDamage();

        switch(damage)
        {
            case 0: // Damage 0: Ender Alloy (Basic)
            case 1: // Damage 1: Ender Alloy (Enhanced)
            case 2: // Damage 2: Ender Alloy (Advanced)
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + damage;

            case 10: // Damage 10: Inactive Ender Core (Basic)
            case 11: // Damage 11: Inactive Ender Core (Enhanced)
            case 12: // Damage 12: Inactive Ender Core (Advanced)
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + (damage - 10) + ".inactive";

            case 15: // Damage 15: Ender Core (Basic)
            case 16: // Damage 16: Ender Core (Enhanced)
            case 17: // Damage 17: Ender Core (Advanced)
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + (damage - 15) + ".active";

            case 20: // Damage 20: Ender Stick
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK;

            case 21: // Damage 21: Ender Rope
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE;

            case 40: // Damage 40: Ender Relic
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERRELIC;

            case 45: // Damage 45: Mob Persistence
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE;

            case 50: // Damage 50: Memory Card (misc)
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_MISC;

            case 51: // Damage 51: Memory Card (items) 6 B
            case 52: // Damage 52: Memory Card (items) 8 B
            case 53: // Damage 53: Memory Card (items) 10 B
            case 54: // Damage 54: Memory Card (items) 12 B
                return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS + "." + (damage - 51);
        }

        return super.getUnlocalizedName();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return false;
        }

        // Ender Relic
        if (stack != null && stack.getItemDamage() == 40)
        {
            if (EntityUtils.spawnEnderCrystal(world, x, y, z) == true)
            {
                --stack.stackSize;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        // Jailer module
        if (stack != null && this.getModuleType(stack).equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            if (livingBase instanceof EntityLiving && EntityUtils.applyMobPersistence((EntityLiving)livingBase) == true)
            {
                --stack.stackSize;

                return true;
            }
        }

        return false;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        String preWh = EnumChatFormatting.WHITE.toString();
        String preRed = EnumChatFormatting.RED.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        String strOwner = StatCollector.translateToLocal("enderutilities.tooltip.item.owner");

        // Set to private and not the owner
        NBTHelperPlayer ownerData = NBTHelperPlayer.getPlayerDataFromItem(stack);
        if (ownerData != null && ownerData.canAccess(player) == false)
        {
            list.add(String.format("%s: %s%s%s - %s%s%s", strOwner, preWh, ownerData.playerName, rst, preRed, StatCollector.translateToLocal("enderutilities.tooltip.item.private"), rst));
            return;
        }

        int damage = stack.getItemDamage();
        NBTTagCompound nbt = stack.getTagCompound();
        if (damage >= 50 && damage <= 54 && (nbt == null || nbt.hasNoTags() == true))
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.nodata"));
            return;
        }

        if (damage == 50) // Memory Card (misc)
        {
            ArrayList<String> listDataTypes = new ArrayList<String>();
            Iterator<String> iter = nbt.func_150296_c().iterator();
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
                String str1 = StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.datatypecount.1");
                String str2 = StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.datatypecount.2");
                list.add(String.format("%s %d %s", str1, listDataTypes.size(), str2));
                list.addAll(listDataTypes);
            }
            else
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.nodata"));
            }
        }
        else if (damage >= 51 && damage <= 54) // Memory Card (items)
        {
            ArrayList<String> lines = new ArrayList<String>();
            int itemCount = UtilItemModular.getFormattedItemListFromContainerItem(stack, lines, 20);
            if (lines.size() > 0)
            {
                NBTTagList tagList = NBTUtils.getStoredItemsList(stack, false);
                int stackCount = tagList != null ? tagList.tagCount() : 0;
                String str1 = StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.items.stackcount.1");
                String str2 = StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.items.stackcount.2");
                String str3 = StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.items.stackcount.3");
                list.add(String.format("%s %d %s %d %s", str1, stackCount, str2, itemCount, str3));
                list.addAll(lines);
            }
            else if (damage != 50)
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.memorycard.nodata"));
            }
        }

        // Print the owner data after the contents if the player can access/see the contents
        if (ownerData != null)
        {
            String mode = ownerData.isPublic ? StatCollector.translateToLocal("enderutilities.tooltip.item.public") : StatCollector.translateToLocal("enderutilities.tooltip.item.private");
            String modeColor = ownerData.isPublic ? EnumChatFormatting.GREEN.toString() : preRed;
            list.add(String.format("%s: %s%s%s - %s%s%s", strOwner, preWh, ownerData.playerName, rst, modeColor, mode, rst));
        }
    }

    public void activateEnderCore(ItemStack stack)
    {
        // Inactive Ender Cores
        if (stack != null && stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            // "Activate" the Ender Core (ie. change the item)
            stack.setItemDamage(stack.getItemDamage() + 5);
        }
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        // Inactive Ender Cores
        if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            return ModuleType.TYPE_ENDERCORE_INACTIVE;
        }

        // Active Ender Cores
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return ModuleType.TYPE_ENDERCORE_ACTIVE;
        }

        // Mob Persistence
        if (stack.getItemDamage() == 45)
        {
            return ModuleType.TYPE_MOBPERSISTENCE;
        }

        // Memory Card
        if (stack.getItemDamage() >= 50 && stack.getItemDamage() <= 54)
        {
            return ModuleType.TYPE_MEMORY_CARD;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        // Inactive Ender Cores
        if (this.getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE_INACTIVE))
        {
            return stack.getItemDamage() - 10;
        }

        // Active Ender Cores
        if (this.getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE_ACTIVE))
        {
            return stack.getItemDamage() - 15;
        }

        // Mob Persistence
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 0;
        }

        // Memory Card (misc)
        if (stack.getItemDamage() == 50)
        {
            return MEMORY_CARD_TYPE_MISC;
        }

        // Memory Card (items)
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD))
        {
            int tier = stack.getItemDamage() - 51;
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

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        if (Configs.disableItemCraftingPart.getBoolean(false) == false)
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
            list.add(new ItemStack(this, 1, 40)); // Ender Relic
            list.add(new ItemStack(this, 1, 45)); // Mob Persistence
            list.add(new ItemStack(this, 1, 50)); // Memory Card (misc)
            list.add(new ItemStack(this, 1, 51)); // Memory Card (items) 6 B
            list.add(new ItemStack(this, 1, 52)); // Memory Card (items) 8 B
            list.add(new ItemStack(this, 1, 53)); // Memory Card (items) 10 B
            list.add(new ItemStack(this, 1, 54)); // Memory Card (items) 12 B
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + ".0");
        this.iconArray = new IIcon[18];

        int i = 0, j;

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + j);
        }

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + j + ".inactive");
        }

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + j + ".active");
        }

        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERRELIC);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_MISC);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS + ".6b");
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS + ".8b");
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS + ".10b");
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD_ITEMS + ".12b");

        // The background icon for empty slots for this item type
        this.slotBackgrounds = new IIcon[3];
        this.slotBackgrounds[0] = iconRegister.registerIcon(ReferenceTextures.getSlotBackgroundName(ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE));
        this.slotBackgrounds[1] = iconRegister.registerIcon(ReferenceTextures.getSlotBackgroundName(ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE));
        this.slotBackgrounds[2] = iconRegister.registerIcon(ReferenceTextures.getSlotBackgroundName(ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage)
    {
        // Ender Alloy
        if (damage >= 0 && damage <= 2) { return this.iconArray[damage]; }

        // Inactive Ender Core
        if (damage >= 10 && damage <= 12) { return this.iconArray[damage - 7]; }

        // Ender Core (active)
        if (damage >= 15 && damage <= 17) { return this.iconArray[damage - 9]; }

        if (damage == 20) { return this.iconArray[9]; } // Ender Stick
        if (damage == 21) { return this.iconArray[10]; } // Ender Rope
        if (damage == 40) { return this.iconArray[11]; } // Ender Relic
        if (damage == 45) { return this.iconArray[12]; } // Mob Persistence
        if (damage >= 50 && damage <= 54) { return this.iconArray[damage - 37]; } // Memory Card

        return this.itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getGuiSlotBackgroundIconIndex(ModuleType moduleType)
    {
        // Ender Cores
        if (ModuleType.TYPE_ENDERCORE_ACTIVE.equals(moduleType) || ModuleType.TYPE_ENDERCORE_ACTIVE.equals(moduleType)) { return this.slotBackgrounds[0]; }

        // Jailer module
        if (ModuleType.TYPE_MOBPERSISTENCE.equals(moduleType)) { return this.slotBackgrounds[1]; }

        // Memory Cards
        if (ModuleType.TYPE_MEMORY_CARD.equals(moduleType)) { return this.slotBackgrounds[2]; }

        return null;
    }
}
