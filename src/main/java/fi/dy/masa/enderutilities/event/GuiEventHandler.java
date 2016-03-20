package fi.dy.masa.enderutilities.event;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageOpenGui;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
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
            if (player.isSneaking() == false &&
                (player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.handyBag, 1, 0)) == true) ||
                (player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.handyBag, 1, 1)) == true))
            {
                if (ItemHandyBag.getOpenableBag(player) != null)
                {
                    if (event.isCancelable() == true)
                    {
                        event.setCanceled(true);
                    }

                    PacketHandler.INSTANCE.sendToServer(new MessageOpenGui(player.dimension, player.posX, player.posY, player.posZ, ReferenceGuiIds.GUI_ID_HANDY_BAG));
                }
            }
        }
    }
}
