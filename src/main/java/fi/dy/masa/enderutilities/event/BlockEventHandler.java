package fi.dy.masa.enderutilities.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;

public class BlockEventHandler
{
    @SubscribeEvent
    public void onHarvestDropsEvent(HarvestDropsEvent event)
    {
        if (event.getHarvester() == null)
        {
            return;
        }

        ItemStack stack = event.getHarvester().getHeldItemMainhand();
        if (stack != null && stack.getItem() instanceof ItemEnderTool)
        {
            ((ItemEnderTool) stack.getItem()).handleHarvestDropsEvent(stack, event);
        }
    }
}
