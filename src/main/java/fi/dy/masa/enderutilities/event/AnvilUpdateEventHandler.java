package fi.dy.masa.enderutilities.event;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool.ToolType;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class AnvilUpdateEventHandler
{
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // Handle Ender Tool repairing
        if (left.getItem() == EnderUtilitiesItems.enderTool || left.getItem() == EnderUtilitiesItems.enderSword)
        {
            // Advanced Ender Alloy
            if (right.getItem() == EnderUtilitiesItems.enderPart && right.getMetadata() == 2)
            {
                this.fullyRepairItem(event, 1, 15);
            }
            else if (right.getItem() == Items.ENCHANTED_BOOK)
            {
                this.enhantItem(event, (ItemEnchantedBook)right.getItem());
            }
            else
            {
                // Cancel vanilla behaviour, otherwise it would allow repairing tools with another tool (and lose the modules)
                event.setCanceled(true);
            }
        }
        else if (left.getItem() == EnderUtilitiesItems.enderBow)
        {
            // Enhanced Ender Alloy
            if (right.getItem() == EnderUtilitiesItems.enderPart && right.getMetadata() == 1)
            {
                this.fullyRepairItem(event, 1, 15);
            }
            else if (right.getItem() != Items.ENCHANTED_BOOK)
            {
                // Cancel vanilla behaviour, otherwise it would allow repairing the bow with another bow (and lose the modules)
                event.setCanceled(true);
            }
        }
    }

    private void fullyRepairItem(AnvilUpdateEvent event, int materialCost, int xpCost)
    {
        ItemStack left = event.getLeft();
        ItemStack repaired = left.copy();

        if (repaired.getItem() == EnderUtilitiesItems.enderTool)
        {
            if (repaired.getItemDamage() == 0)
            {
                event.setCanceled(true);
                return;
            }

            ((ItemEnderTool)repaired.getItem()).repairTool(repaired, -1);
        }
        else if (repaired.getItem() == EnderUtilitiesItems.enderSword)
        {
            if (repaired.getItemDamage() == 0)
            {
                event.setCanceled(true);
                return;
            }

            int repairAmount = Math.min(repaired.getMaxDamage(), repaired.getItemDamage());
            repaired.setItemDamage(repaired.getItemDamage() - repairAmount);
        }

        event.setMaterialCost(materialCost);
        event.setCost(xpCost);
        event.setOutput(repaired);
        this.updateItemName(event, repaired);
    }

    private void enhantItem(AnvilUpdateEvent event, ItemEnchantedBook book)
    {
        ItemStack toolStack = event.getLeft().copy();
        ItemStack bookStack = event.getRight().copy();
        NBTTagList bookEnchantmentList = book.getEnchantments(bookStack);

        if (bookEnchantmentList.tagCount() <= 0 || toolStack.getItem().isBookEnchantable(toolStack, bookStack) == false)
        {
            return;
        }

        Map<Enchantment, Integer> oldEnchantments = EnchantmentHelper.getEnchantments(toolStack);
        Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(bookStack);
        Iterator<Map.Entry<Enchantment, Integer>> iterBookEnchantments = bookEnchantments.entrySet().iterator();
        int cost = oldEnchantments.size() * 2;
        boolean levelIncreased = false;

        while (iterBookEnchantments.hasNext() == true)
        {
            Map.Entry<Enchantment, Integer> enchantmentEntry = iterBookEnchantments.next();
            Enchantment enchBook = enchantmentEntry.getKey();

            if (enchBook != null)
            {
                int oldEnchLvl = oldEnchantments.containsKey(enchBook) ? oldEnchantments.get(enchBook).intValue() : 0;
                int bookEnchLvl = enchantmentEntry.getValue();
                int newLvl = bookEnchLvl == oldEnchLvl ? oldEnchLvl + 1 : Math.max(oldEnchLvl, bookEnchLvl);
                newLvl = Math.min(newLvl, enchBook.getMaxLevel());

                if (this.canApplyEnchantment(enchBook, toolStack) == false)
                {
                    event.setCanceled(true);
                    return;
                }

                if (newLvl > oldEnchLvl)
                {
                    levelIncreased = true;
                }

                Iterator<Map.Entry<Enchantment, Integer>> iterOldEnchantments = oldEnchantments.entrySet().iterator();

                // Check that the new enchantment doesn't conflict with any of the existing enchantments
                while (iterOldEnchantments.hasNext() == true)
                {
                    Enchantment enchOld = iterOldEnchantments.next().getKey();

                    if (enchOld.equals(enchBook) == false && (enchBook.canApplyTogether(enchOld) && enchOld.canApplyTogether(enchBook)) == false)
                    {
                        event.setCanceled(true);
                        return;
                    }
                }

                oldEnchantments.put(enchBook, newLvl);
                cost += newLvl * 2;
            }
        }

        // Check that at least some new enchantments would be added, or some of the existing levels increased
        if (levelIncreased == false)
        {
            event.setCanceled(true);
            return;
        }

        EnchantmentHelper.setEnchantments(oldEnchantments, toolStack);
        event.setOutput(toolStack);
        event.setCost(cost);
        this.updateItemName(event, toolStack);
        event.setCost(Math.min(event.getCost(), 39));
    }

    private boolean canApplyEnchantment(Enchantment ench, ItemStack stack)
    {
        if (ench.type  == EnumEnchantmentType.BREAKABLE || ench.type  == EnumEnchantmentType.ALL)
        {
            return true;
        }

        if (stack.getItem() == EnderUtilitiesItems.enderSword)
        {
            return ench.type == EnumEnchantmentType.WEAPON;
        }

        if (stack.getItem() == EnderUtilitiesItems.enderTool)
        {
            ToolType type = ItemEnderTool.ToolType.fromStack(stack);
            if (type == ToolType.HOE || type == ToolType.INVALID)
            {
                return false;
            }

            if (type == ToolType.AXE && ench.type == EnumEnchantmentType.WEAPON)
            {
                return true;
            }

            return ench.type == EnumEnchantmentType.DIGGER;
        }

        return false;
    }

    private void updateItemName(AnvilUpdateEvent event, ItemStack outputStack)
    {
        String name = event.getName();

        if (StringUtils.isBlank(name) == false)
        {
            outputStack.setStackDisplayName(name);
            event.setCost(event.getCost() + 1);
        }
        else if (StringUtils.isBlank(name) == true && outputStack.hasDisplayName() == true)
        {
            // Remove the custom name
            outputStack.clearCustomName();
            event.setCost(event.getCost() + 1);
        }
    }
}
