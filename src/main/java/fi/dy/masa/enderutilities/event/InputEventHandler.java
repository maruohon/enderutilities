package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.MouseEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.Keybindings;

public class InputEventHandler
{
    public static boolean scrollingActive = false;

    public InputEventHandler()
    {
    }

    public static boolean isHoldingKeyboundItem(EntityPlayer player)
    {
        if (player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof IKeyBound)
        {
            return true;
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onInput(InputEvent.KeyInputEvent event)
    {
        // In-game (no GUI open)
        if (FMLClientHandler.instance().getClient().inGameHasFocus == true)
        {
            EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();

            if (Keybindings.keyToggleMode.isPressed() == true) // or this? Keyboard.getEventKey() == Keybindings.keyToggleMode.getKeyCode()
            {
                if (isHoldingKeyboundItem(player) == true)
                {
                    int key = ReferenceKeys.KEYBIND_ID_TOGGLE_MODE;

                    if (EnderUtilities.proxy.isShiftKeyDown() == true)
                    {
                        key |= ReferenceKeys.KEYBIND_MODIFIER_SHIFT;
                    }

                    if (EnderUtilities.proxy.isControlKeyDown() == true)
                    {
                        key |= ReferenceKeys.KEYBIND_MODIFIER_CONTROL;
                    }

                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
                return;
            }

            // Activate and deactivate the scroll-mouse-to-change-selected-module mode
            if (/*Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT
                || */Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)
            {
                if ((/*Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
                    || */Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && isHoldingKeyboundItem(player) == true)
                {
                    scrollingActive = true;
                }
                else
                {
                    scrollingActive = false;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onInput(MouseEvent event)
    {
        int dWheel = event.dwheel;
        if (dWheel != 0)
        {
            dWheel /= 120;

            // If the player pressed down a shift or control key while holding a IKeyBound item
            // (note: this means it specifically WON'T work if the player started pressing shift or
            // control while holding something else, for example when scrolling through the hotbar!!),
            // then we allow easily scrolling through the installed modules using the mouse wheel.
            if (scrollingActive == true)
            {
                EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
                if (isHoldingKeyboundItem(player) == true)
                {
                    // Currently the "change selected link crystal" functionality is done when holding shift
                    // and pressing the toggle mode hotkey.
                    int key = ReferenceKeys.KEYBIND_ID_TOGGLE_MODE | ReferenceKeys.KEYBIND_MODIFIER_SHIFT;

                    // Scrolling up, reverse the direction. Reverse direction is normally shift + control + toggle mode key
                    if (dWheel > 0)
                    {
                        key |= ReferenceKeys.KEYBIND_MODIFIER_CONTROL;
                    }

                    if (event.isCancelable() == true)
                    {
                        event.setCanceled(true);
                    }

                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
            }
        }
    }
}
