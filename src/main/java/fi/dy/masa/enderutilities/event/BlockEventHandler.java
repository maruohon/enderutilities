package fi.dy.masa.enderutilities.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;

public class BlockEventHandler
{
    @SubscribeEvent
    public void onHarvestDropsEvent(HarvestDropsEvent event)
    {
        if (event.harvester == null)
        {
            return;
        }

        ItemStack stack = event.harvester.getCurrentEquippedItem();
        if (stack != null && stack.getItem() instanceof ItemEnderTool)
        {
            ((ItemEnderTool) stack.getItem()).handleHarvestDropsEvent(stack, event);
        }
    }
}
