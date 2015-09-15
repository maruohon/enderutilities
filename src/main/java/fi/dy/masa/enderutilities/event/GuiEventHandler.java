package fi.dy.masa.enderutilities.event;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class GuiEventHandler
{
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiOpenEvent(GuiOpenEvent event)
    {
        // Reset the scrolling modifier when the player opens a GUI.
        // Otherwise the key up event will get eaten and our scrolling mode will get stuck on
        // until the player sneaks again.
        // FIXME Apparently there are key input events for GUI screens in 1.8,
        // so this probably can be removed then.
        InputEventHandler.resetModifiers();

        // Opening the player's Inventory GUI
        if (event.gui != null && event.gui.getClass() == GuiInventory.class)
        {
            EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
            if (player.inventory.hasItem(EnderUtilitiesItems.handyBag) == true)
            {
                if (((ItemHandyBag)EnderUtilitiesItems.handyBag).getSlotContainingEnabledBag(player) != -1)
                {
                    if (event.isCancelable() == true)
                    {
                        event.setCanceled(true);
                    }
                    // TODO: send a packet to the server to open the Handy Bag GUI
                }
            }
        }
    }
}
