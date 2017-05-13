package fi.dy.masa.enderutilities.event;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.base.IAnvilRepairable;

public class AnvilUpdateEventHandler
{
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof IAnvilRepairable)
        {
            IAnvilRepairable repairable = (IAnvilRepairable) left.getItem();

            // Advanced Ender Alloy
            if (repairable.isRepairItem(left, right))
            {
                this.fullyRepairItem(event, repairable, 1, 15);
            }
            else if (right.getItem() == Items.ENCHANTED_BOOK)
            {
                this.enchantItem(event, repairable, (ItemEnchantedBook) right.getItem());
            }
            else
            {
                // Cancel vanilla behaviour, otherwise it would allow repairing tools with another tool (and lose the modules)
                event.setCanceled(true);
            }
        }
    }

    private void fullyRepairItem(AnvilUpdateEvent event, IAnvilRepairable repairable, int materialCost, int xpCost)
    {
        ItemStack repaired = event.getLeft().copy();

        if (repairable.repairItem(repaired, -1))
        {
            event.setMaterialCost(materialCost);
            event.setCost(xpCost);
            event.setOutput(repaired);

            this.updateItemName(event, repaired);
        }
        else
        {
            event.setCanceled(true);
        }
    }

    private void enchantItem(AnvilUpdateEvent event, IAnvilRepairable repairable, ItemEnchantedBook book)
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

        while (iterBookEnchantments.hasNext())
        {
            Map.Entry<Enchantment, Integer> enchantmentEntry = iterBookEnchantments.next();
            Enchantment enchBook = enchantmentEntry.getKey();

            if (enchBook != null)
            {
                int oldEnchLvl = oldEnchantments.containsKey(enchBook) ? oldEnchantments.get(enchBook).intValue() : 0;
                int bookEnchLvl = enchantmentEntry.getValue();
                int newLvl = bookEnchLvl == oldEnchLvl ? oldEnchLvl + 1 : Math.max(oldEnchLvl, bookEnchLvl);
                newLvl = Math.min(newLvl, enchBook.getMaxLevel());

                if (repairable.canApplyEnchantment(toolStack, enchBook) == false)
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
                while (iterOldEnchantments.hasNext())
                {
                    Enchantment enchOld = iterOldEnchantments.next().getKey();

                    // func_191560_c - canApplyTogether
                    if (enchOld.equals(enchBook) == false && enchOld.func_191560_c(enchBook) == false)
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
