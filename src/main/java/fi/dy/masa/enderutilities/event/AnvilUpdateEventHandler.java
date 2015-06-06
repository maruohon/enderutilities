package fi.dy.masa.enderutilities.event;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class AnvilUpdateEventHandler
{
    @SubscribeEvent
    public void onAnvilUpdate(AnvilUpdateEvent event)
    {
        // Handle Ender Tool repairing
        if (event.left.getItem() == EnderUtilitiesItems.enderTool || event.left.getItem() == EnderUtilitiesItems.enderSword)
        {
            // Advanced Ender Alloy
            if (event.right.getItem() == EnderUtilitiesItems.enderPart && event.right.getItemDamage() == 2)
            {
                fullyRepairItem(event, 1, 15);
            }
            else if (event.right.getItem() != Items.enchanted_book)
            {
                // Cancel vanilla behaviour, otherwise it would allow repairing tools with different types of tools (and lose the modules)
                event.setCanceled(true);
            }
        }
        else if (event.left.getItem() == EnderUtilitiesItems.enderBow)
        {
            // Enhanced Ender Alloy
            if (event.right.getItem() == EnderUtilitiesItems.enderPart && event.right.getItemDamage() == 1)
            {
                fullyRepairItem(event, 1, 15);
            }
            else if (event.right.getItem() != Items.enchanted_book)
            {
                // Cancel vanilla behaviour, otherwise it would allow repairing the bow with another bow (and lose the modules)
                event.setCanceled(true);
            }
        }
    }

    private static void fullyRepairItem(AnvilUpdateEvent event, int materialCost, int xpCost)
    {
        ItemStack repaired = event.left.copy();

        if (event.left.getItemDamage() > 0)
        {
            event.materialCost = materialCost;
            event.cost = xpCost;
            int repairAmount = Math.min(event.left.getMaxDamage(), event.left.getItemDamage());
            repaired.setItemDamage(event.left.getItemDamage() - repairAmount);

            if (StringUtils.isBlank(event.name) == false)
            {
                repaired.setStackDisplayName(event.name);
            }
            else if (StringUtils.isBlank(event.name) == true && repaired.hasDisplayName() == true)
            {
                // Remove the custom name
                repaired.func_135074_t();
            }

            event.output = repaired;
        }
    }
}
