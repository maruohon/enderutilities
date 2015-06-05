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
    public static int scrollingMode = 0;

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

                    if (EnderUtilities.proxy.isAltKeyDown() == true)
                    {
                        key |= ReferenceKeys.KEYBIND_MODIFIER_ALT;
                    }

                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
                return;
            }

            // Activate or deactivate the scroll-mouse-wheel-to-change-stuff modes

            if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
            {
                if (EnderUtilities.proxy.isShiftKeyDown() == true && isHoldingKeyboundItem(player) == true)
                {
                    scrollingMode |= ReferenceKeys.KEYBIND_MODIFIER_SHIFT;
                }
                else
                {
                    scrollingMode &= ~ReferenceKeys.KEYBIND_MODIFIER_SHIFT;
                }
            }

            if (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL)
            {
                if (EnderUtilities.proxy.isControlKeyDown() == true && isHoldingKeyboundItem(player) == true)
                {
                    scrollingMode |= ReferenceKeys.KEYBIND_MODIFIER_CONTROL;
                }
                else
                {
                    scrollingMode &= ~ReferenceKeys.KEYBIND_MODIFIER_CONTROL;
                }
            }

            if (Keyboard.getEventKey() == Keyboard.KEY_LMENU || Keyboard.getEventKey() == Keyboard.KEY_RMENU)
            {
                if (EnderUtilities.proxy.isAltKeyDown() == true && isHoldingKeyboundItem(player) == true)
                {
                    scrollingMode |= ReferenceKeys.KEYBIND_MODIFIER_ALT;
                }
                else
                {
                    scrollingMode &= ~ReferenceKeys.KEYBIND_MODIFIER_ALT;
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

            // If the player pressed down a modifier key while holding an IKeyBound item
            // (note: this means it specifically WON'T work if the player started pressing a modifier
            // key while holding something else, for example when scrolling through the hotbar!!),
            // then we allow for easily scrolling through the changeable stuff using the mouse wheel.
            if (scrollingMode != 0)
            {
                EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
                if (isHoldingKeyboundItem(player) == true)
                {
                    int key = ReferenceKeys.KEYBIND_ID_TOGGLE_MODE | scrollingMode;

                    // Scrolling up, reverse the direction.
                    if (dWheel > 0)
                    {
                        key |= ReferenceKeys.KEYBIND_MODIFIER_REVERSE;
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
