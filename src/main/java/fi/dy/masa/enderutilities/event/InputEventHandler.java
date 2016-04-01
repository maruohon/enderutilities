package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.Keybindings;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class InputEventHandler
{
    public static final TIntIntHashMap KEY_CODE_MAPPINGS = new TIntIntHashMap(16);
    /** Has the active mouse scroll modifier mask, if any */
    private static int scrollingMask = 0;
    /** Has the currently active/pressed mask of supported modifier keys */
    private static int modifierMask = 0;

    public InputEventHandler()
    {
    }

    /**
     * Reset the modifiers externally. This is to fix the stuck modifier keys
     * if a GUI is opened while the modifiers are active.
     * FIXME Apparently there are key input events for GUI screens in 1.8,
     * so this probably can be removed then.
     */
    public static void resetModifiers()
    {
        scrollingMask = 0;
        modifierMask = 0;
    }

    public static boolean isHoldingKeyboundItem(EntityPlayer player)
    {
        return player != null && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof IKeyBound;
    }

    public static boolean hasKeyBoundUnselectedItem(EntityPlayer player)
    {
        return InventoryUtils.getFirstKeyBoundUnselectedItem(player) != null;
    }

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event)
    {
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        int eventKey = Keyboard.getEventKey();

        // One of our supported modifier keys was pressed or released
        if (KEY_CODE_MAPPINGS.containsKey(eventKey) == true)
        {
            int mask = KEY_CODE_MAPPINGS.get(eventKey);

            // Key was pressed
            if (Keyboard.getEventKeyState() == true)
            {
                modifierMask |= mask;

                // Only add scrolling mode mask if the currently selected item is one of our IKeyBound items
                if (isHoldingKeyboundItem(player) == true)
                {
                    scrollingMask |= mask;
                }
            }
            // Key was released
            else
            {
                modifierMask &= ~mask;
                scrollingMask &= ~mask;
            }
        }
        // In-game (no GUI open)
        else if (FMLClientHandler.instance().getClient().inGameHasFocus == true)
        {
            // or this?: Keybindings.keyToggleMode.isPressed() == true
            if (eventKey == Keybindings.keyToggleMode.getKeyCode() && Keyboard.getEventKeyState() == true)
            {
                if (isHoldingKeyboundItem(player) == true || hasKeyBoundUnselectedItem(player) == true)
                {
                    int keyCode = ReferenceKeys.KEYBIND_ID_TOGGLE_MODE | modifierMask;
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyCode));
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event)
    {
        int dWheel = event.dwheel;
        if (dWheel != 0)
        {
            dWheel /= 120;

            // If the player pressed down a modifier key while holding an IKeyBound item
            // (note: this means it specifically WON'T work if the player started pressing a modifier
            // key while holding something else, for example when scrolling through the hotbar!!),
            // then we allow for easily scrolling through the changeable stuff using the mouse wheel.
            if (scrollingMask != 0)
            {
                EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
                if (isHoldingKeyboundItem(player) == true)
                {
                    int key = ReferenceKeys.KEYBIND_ID_TOGGLE_MODE | scrollingMask;

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

    static
    {
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_LSHIFT,      ReferenceKeys.KEYBIND_MODIFIER_SHIFT);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_RSHIFT,      ReferenceKeys.KEYBIND_MODIFIER_SHIFT);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_LCONTROL,    ReferenceKeys.KEYBIND_MODIFIER_CONTROL);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_RCONTROL,    ReferenceKeys.KEYBIND_MODIFIER_CONTROL);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_LMENU,       ReferenceKeys.KEYBIND_MODIFIER_ALT);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_RMENU,       ReferenceKeys.KEYBIND_MODIFIER_ALT);
    }
}
