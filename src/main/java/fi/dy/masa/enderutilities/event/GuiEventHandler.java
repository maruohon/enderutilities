package fi.dy.masa.enderutilities.event;

import java.io.IOException;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
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

            if (this.handyBagShouldOpen == true && ItemHandyBag.getOpenableBag(player) != null)
            {
                if (event.isCancelable() == true)
                {
                    event.setCanceled(true);
                }

                PacketHandler.INSTANCE.sendToServer(new MessageOpenGui(player.dimension, ReferenceGuiIds.GUI_ID_HANDY_BAG));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void mouseInputEvent(MouseInputEvent.Pre event)
    {
        // Handle the mouse input inside all of the mod's GUIs via the event and then cancel the event,
        // so that some mods like Inventory Sorter don't try to sort the Ender Utilities inventories.
        // Using priority LOW should still allow even older versions of Item Scroller to work,
        // since it uses normal priority.
        if (event.getGui() instanceof GuiEnderUtilities)
        {
            try
            {
                event.getGui().handleMouseInput();
                event.setCanceled(true);
            }
            catch (IOException e)
            {
                EnderUtilities.logger.warn("Exception while executing handleMouseInput() on {}", event.getGui().getClass().getName());
            }
        }
    }
}
