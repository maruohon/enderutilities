package fi.dy.masa.enderutilities.event;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageOpenGui;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;

@SideOnly(Side.CLIENT)
public class GuiEventHandler
{
    private static GuiEventHandler instance;
    private boolean handyBagShouldOpen;

    public GuiEventHandler()
    {
        instance = this;
    }

    public static GuiEventHandler instance()
    {
        return instance;
    }

    public void setHandyBagShouldOpen(boolean shouldOpen)
    {
        this.handyBagShouldOpen = shouldOpen;
    }

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
        if (event.getGui() != null && event.getGui().getClass() == GuiInventory.class)
        {
            EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();

            if (this.handyBagShouldOpen && player != null && ItemHandyBag.getOpenableBag(player).isEmpty() == false)
            {
                if (event.isCancelable())
                {
                    event.setCanceled(true);
                }

                PacketHandler.INSTANCE.sendToServer(new MessageOpenGui(player.dimension, ReferenceGuiIds.GUI_ID_HANDY_BAG));
            }
        }
    }
}
