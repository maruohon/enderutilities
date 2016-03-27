package fi.dy.masa.enderutilities.event;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool.ToolType;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
            if (right.getItem() == EnderUtilitiesItems.enderPart && right.getItemDamage() == 2)
            {
                this.fullyRepairItem(event, 1, 15);
            }
            else if (right.getItem() == Items.enchanted_book)
            {
                this.enhantItem(event);
            }
            else
            {
                // Cancel vanilla behaviour, otherwise it would allow repairing tools with different types of tools (and lose the modules)
                event.setCanceled(true);
            }
        }
        else if (left.getItem() == EnderUtilitiesItems.enderBow)
        {
            // Enhanced Ender Alloy
            if (right.getItem() == EnderUtilitiesItems.enderPart && right.getItemDamage() == 1)
            {
                this.fullyRepairItem(event, 1, 15);
            }
            else if (right.getItem() != Items.enchanted_book)
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
            ItemEnderTool item = (ItemEnderTool)repaired.getItem();
            if (item.getToolDamage(repaired) == 0)
            {
                event.setCanceled(true);
                return;
            }

            item.repairTool(repaired, -1);
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

    private void enhantItem(AnvilUpdateEvent event)
    {
        // FIXME 1.9
        /*
        ItemStack toolStack = event.left.copy();
        ItemStack bookStack = event.right.copy();
        ItemEnchantedBook book = (ItemEnchantedBook)event.right.getItem();
        NBTTagList bookEnchantmentList = book.getEnchantments(bookStack);

        if (bookEnchantmentList.tagCount() <= 0 || toolStack.getItem().isBookEnchantable(toolStack, bookStack) == false)
        {
            return;
        }

        Map<Integer, Integer> oldEnchantments = EnchantmentHelper.getEnchantments(toolStack);
        Map<Integer, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(bookStack);
        Iterator<Integer> iterBookEnchantments = bookEnchantments.keySet().iterator();
        int cost = 0;

        while (iterBookEnchantments.hasNext() == true)
        {
            int bookEnchId = iterBookEnchantments.next().intValue();
            Enchantment enchBook = Enchantment.getEnchantmentById(bookEnchId);

            if (enchBook != null)
            {
                int oldEnchLvl = oldEnchantments.containsKey(bookEnchId) ? oldEnchantments.get(bookEnchId).intValue() : 0;
                int bookEnchLvl = bookEnchantments.get(bookEnchId);
                int newLvl = bookEnchLvl == oldEnchLvl ? oldEnchLvl + 1 : Math.max(oldEnchLvl, bookEnchLvl);
                newLvl = Math.min(newLvl, enchBook.getMaxLevel());

                if (this.canApplyEnchantment(enchBook, toolStack) == false)
                {
                    event.setCanceled(true);
                    return;
                }

                Iterator<Integer> iterOldEnchantments = oldEnchantments.keySet().iterator();
                boolean canApply = true;

                while (iterOldEnchantments.hasNext() == true)
                {
                    int oldEnchId = iterOldEnchantments.next().intValue();
                    Enchantment enchOld = Enchantment.getEnchantmentById(oldEnchId);

                    if (oldEnchId != bookEnchId && (enchBook.canApplyTogether(enchOld) && enchOld.canApplyTogether(enchBook)) == false)
                    {
                        canApply = false;
                        event.setCanceled(true);
                        return;
                    }
                }

                if (canApply == true)
                {
                    oldEnchantments.put(bookEnchId, newLvl);
                    cost += newLvl * 5;
                }
            }
        }

        EnchantmentHelper.setEnchantments(oldEnchantments, toolStack);
        event.output = toolStack;
        event.cost = cost;
        this.updateItemName(event, toolStack);
        event.cost = Math.min(event.cost, 39);
        */
    }

    private boolean canApplyEnchantment(Enchantment ench, ItemStack stack)
    {
        if (ench.type.equals(EnumEnchantmentType.BREAKABLE) || ench.type.equals(EnumEnchantmentType.ALL))
        {
            return true;
        }

        if (stack.getItem() == EnderUtilitiesItems.enderSword)
        {
            return ench.type.equals(EnumEnchantmentType.WEAPON);
        }

        if (stack.getItem() == EnderUtilitiesItems.enderTool)
        {
            ToolType type = ItemEnderTool.ToolType.fromStack(stack);
            if (type == ToolType.HOE || type == ToolType.INVALID)
            {
                return false;
            }

            return ench.type.equals(EnumEnchantmentType.DIGGER);
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
