package fi.dy.masa.enderutilities.reference;

import org.lwjgl.input.Keyboard;

public class ReferenceKeys
{
    public static final String KEYBIND_CATEGORY_ENDERUTILITIES = "category." + Reference.MOD_ID;

    public static final String KEYBIND_NAME_TOGGLE_MODE = Reference.MOD_ID + ".key.togglemode";

    public static final int DEFAULT_KEYBIND_TOGGLE_MODE = Keyboard.KEY_G;

    // These are used to identify the pressed key on the server side. They have nothing to do with actual key codes.
    public static final int KEYBIND_ID_TOGGLE_MODE = 0x0001;

    public static final int KEYBIND_MODIFIER_SHIFT      = 0x00010000;
    public static final int KEYBIND_MODIFIER_CONTROL    = 0x00020000;
    public static final int KEYBIND_MODIFIER_ALT        = 0x00040000;
    public static final int KEYBIND_MODIFIER_REVERSE    = 0x00080000;

    public static int getBaseKey(int key)
    {
        return (key & 0xFFFF);
    }

    public static boolean keypressContainsShift(int key)
    {
        return (key & KEYBIND_MODIFIER_SHIFT) != 0;
    }

    public static boolean keypressContainsControl(int key)
    {
        return (key & KEYBIND_MODIFIER_CONTROL) != 0;
    }

    public static boolean keypressContainsAlt(int key)
    {
        return (key & KEYBIND_MODIFIER_ALT) != 0;
    }

    public static boolean keypressActionIsReversed(int key)
    {
        return (key & KEYBIND_MODIFIER_REVERSE) != 0;
    }
}
