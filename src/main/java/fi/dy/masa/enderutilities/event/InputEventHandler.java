package fi.dy.masa.enderutilities.event;

import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiHandyBag;
import fi.dy.masa.enderutilities.gui.client.GuiScreenBuilderWandTemplate;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.Keybindings;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

@SideOnly(Side.CLIENT)
public class InputEventHandler
{
    private static final TIntIntHashMap KEY_CODE_MAPPINGS = new TIntIntHashMap(16);
    private static final TIntObjectHashMap<Long> KEY_PRESS_TIMES = new TIntObjectHashMap<Long>(16);
    private final Minecraft mc;
    /** Has the active mouse scroll modifier mask, if any */
    private static int scrollingMask = 0;
    /** Has the currently active/pressed mask of supported modifier keys */
    private static int modifierMask = 0;
    public static int doubleTapLimit = 500;

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
        if (KEY_CODE_MAPPINGS.containsKey(eventKey))
        {
            int mask = KEY_CODE_MAPPINGS.get(eventKey);

            // Key was pressed
            if (keyState)
            {
                modifierMask |= mask;

                // Only add scrolling mode mask if the currently selected item is one of our IKeyBound items
                if (isHoldingKeyboundItem(player))
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
        if (FMLClientHandler.instance().getClient().inGameHasFocus)
        {
            if (eventKey == Keybindings.keyToggleMode.getKeyCode() && keyState)
            {
                if (this.buildersWandClientSideHandling())
                {
                    return;
                }

                if (isHoldingKeyboundItem(player) || hasKeyBoundUnselectedItem(player))
                {
                    int keyCode = HotKeys.KEYBIND_ID_TOGGLE_MODE | modifierMask;
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(keyCode));
                }
            }
            // Track the event of opening and closing the player's inventory.
            // This is intended to have the Handy Bag either open or not open ie. do the same thing for the duration
            // that the inventory is open at once. This is intended to get rid of the previous unintended behavior where
            // if you sneak + open the inventory to open just the regular player inventory, if you then look at recipes in JEI
            // or something similar where the GuiScreen changes, then the bag would suddenly open instead of the player inventory
            // when closing the recipe screen and returning to the inventory.

            // Based on a quick test, the inventory key fires as state when opening the inventory (before the gui opens)
            // and as state == false when closing the inventory (after the gui has closed).
            else if (eventKey == this.mc.gameSettings.keyBindInventory.getKeyCode())
            {
                boolean shouldOpen = keyState && player.isSneaking() == Configs.handyBagOpenRequiresSneak;
                GuiEventHandler.instance().setHandyBagShouldOpen(shouldOpen);
            }
            else if (eventKey == Keyboard.KEY_ESCAPE)
            {
                GuiEventHandler.instance().setHandyBagShouldOpen(false);
            }

            // Jump or sneak above an Ender Elevator - activate it
            if (keyState && (eventKey == this.mc.gameSettings.keyBindJump.getKeyCode() ||
                eventKey == this.mc.gameSettings.keyBindSneak.getKeyCode()))
            {
                BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
                World world = player.getEntityWorld();

                // Check the player's feet position in case they are standing inside a slab or layer elevator
                if (world.getBlockState(pos       ).getBlock() instanceof BlockElevator ||
                    world.getBlockState(pos.down()).getBlock() instanceof BlockElevator)
                {
                    int key = eventKey == this.mc.gameSettings.keyBindJump.getKeyCode() ? HotKeys.KEYCODE_JUMP : HotKeys.KEYCODE_SNEAK;
                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
            }
        }
    }

    @SubscribeEvent
    public void onKeyInputEventGui(GuiScreenEvent.KeyboardInputEvent.Pre event)
    {
        if (event.getGui() instanceof GuiEnderUtilities)
        {
            int eventKey = Keyboard.getEventKey();

            // One of our supported modifier keys was pressed
            if (KEY_CODE_MAPPINGS.containsKey(eventKey) && Keyboard.getEventKeyState() && this.checkForDoubleTap(eventKey))
            {
                if (event.getGui() instanceof GuiHandyBag)
                {
                    PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(0, new BlockPos(0, 0, 0),
                        ReferenceGuiIds.GUI_ID_HANDY_BAG, ItemHandyBag.GUI_ACTION_TOGGLE_SHIFTCLICK_DOUBLETAP, 0));
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
                if (isHoldingKeyboundItem(player))
                {
                    int key = HotKeys.KEYCODE_SCROLL | scrollingMask;

                    // Scrolling up, reverse the direction.
                    if (dWheel > 0)
                    {
                        key |= HotKeys.SCROLL_MODIFIER_REVERSE;
                    }

                    if (event.isCancelable())
                    {
                        event.setCanceled(true);
                    }

                    PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(key));
                }
            }
        }
    }

    private boolean checkForDoubleTap(int key)
    {
        boolean ret = KEY_PRESS_TIMES.containsKey(key) && (System.currentTimeMillis() - KEY_PRESS_TIMES.get(key)) <= doubleTapLimit;

        if (ret == false)
        {
            KEY_PRESS_TIMES.put(key, System.currentTimeMillis());
        }
        else
        {
            KEY_PRESS_TIMES.remove(key);
        }

        return ret;
    }

    private boolean buildersWandClientSideHandling()
    {
        if (GuiScreen.isShiftKeyDown() || GuiScreen.isCtrlKeyDown() || GuiScreen.isAltKeyDown())
        {
            return false;
        }

        ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
        if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand &&
            ItemBuildersWand.Mode.getMode(stack) == Mode.COPY)
        {
            Minecraft.getMinecraft().displayGuiScreen(new GuiScreenBuilderWandTemplate());
            return true;
        }

        return false;
    }

    static
    {
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_LSHIFT,      HotKeys.KEYBIND_MODIFIER_SHIFT);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_RSHIFT,      HotKeys.KEYBIND_MODIFIER_SHIFT);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_LCONTROL,    HotKeys.KEYBIND_MODIFIER_CONTROL);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_RCONTROL,    HotKeys.KEYBIND_MODIFIER_CONTROL);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_LMENU,       HotKeys.KEYBIND_MODIFIER_ALT);
        KEY_CODE_MAPPINGS.put(Keyboard.KEY_RMENU,       HotKeys.KEYBIND_MODIFIER_ALT);
    }
}
