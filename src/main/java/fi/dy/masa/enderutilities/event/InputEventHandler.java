package fi.dy.masa.enderutilities.event;

import org.lwjgl.input.Keyboard;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.Keybindings;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import gnu.trove.map.hash.TIntIntHashMap;

@SideOnly(Side.CLIENT)
public class InputEventHandler
{
    public static final TIntIntHashMap KEY_CODE_MAPPINGS = new TIntIntHashMap(16);
    private final Minecraft mc;
    /** Has the active mouse scroll modifier mask, if any */
    private static int scrollingMask = 0;
    /** Has the currently active/pressed mask of supported modifier keys */
    private static int modifierMask = 0;

    public InputEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
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
        return EntityUtils.isHoldingItemOfType(player, IKeyBound.class);
    }

    public static boolean hasKeyBoundUnselectedItem(EntityPlayer player)
    {
        return InventoryUtils.getFirstItemOfType(player, IKeyBoundUnselected.class) != null;
    }

    @SubscribeEvent
    public void onKeyInputEvent(InputEvent.KeyInputEvent event)
    {
        EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
        int eventKey = Keyboard.getEventKey();
        boolean keyState = Keyboard.getEventKeyState();

        // One of our supported modifier keys was pressed or released
        if (KEY_CODE_MAPPINGS.containsKey(eventKey) == true)
        {
            int mask = KEY_CODE_MAPPINGS.get(eventKey);

            // Key was pressed
            if (keyState == true)
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
        if (FMLClientHandler.instance().getClient().inGameHasFocus == true)
        {
            if (eventKey == Keybindings.keyToggleMode.getKeyCode() && keyState == true)
            {
                if (isHoldingKeyboundItem(player) == true || hasKeyBoundUnselectedItem(player) == true)
                {
                    int keyCode = ReferenceKeys.KEYBIND_ID_TOGGLE_MODE | modifierMask;
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyCode));
                }
            }
            // Track the event of opening and closing the player's inventory.
            // This is intended to have the Handy Bag either open or not open ie. do the same thing for the duration
            // that the inventory is open at once. This is intended to get rid of the previous unintended behavior where
            // if you sneak + open the inventory to open just the regular player inventory, if you then look at recipes in JEI
            // or something similar where the GuiScreen changes, then the bag would suddenly open instead of the player inventory
            // when closing the recipe screen and returning to the inventory.

            // Based on a quick test, the inventory key fires as state == true when opening the inventory (before the gui opens)
            // and as state == false when closing the inventory (after the gui has closed).
            else if (eventKey == this.mc.gameSettings.keyBindInventory.getKeyCode())
            {
                boolean shouldOpen = keyState == true && player.isSneaking() == Configs.handyBagOpenRequiresSneak;
                GuiEventHandler.instance().setHandyBagShouldOpen(shouldOpen);
            }
            else if (eventKey == Keyboard.KEY_ESCAPE)
            {
                GuiEventHandler.instance().setHandyBagShouldOpen(false);
            }

            // Jump or sneak above an Ender Elevator - activate it
            if (keyState == true && (eventKey == this.mc.gameSettings.keyBindJump.getKeyCode() ||
                eventKey == this.mc.gameSettings.keyBindSneak.getKeyCode()))
            {
                // EntityPlayerSP adds 0.5 to all the coordinates for some reason...
                BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
                IBlockState state = player.worldObj.getBlockState(pos.down());

                if (state.getBlock() == EnderUtilitiesBlocks.blockElevator)
                {
                    int key = eventKey == this.mc.gameSettings.keyBindJump.getKeyCode() ? ReferenceKeys.KEYCODE_JUMP : ReferenceKeys.KEYCODE_SNEAK;
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event)
    {
        int dWheel = event.getDwheel();
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
