package fi.dy.masa.enderutilities.reference;

import org.lwjgl.input.Keyboard;

public class HotKeys
{
    public static final String KEYBIND_CATEGORY_ENDERUTILITIES = "category." + Reference.MOD_ID;

    public static final String KEYBIND_NAME_TOGGLE_MODE = Reference.MOD_ID + ".key.togglemode";

    public static final int DEFAULT_KEYBIND_TOGGLE_MODE = Keyboard.KEY_G;

    // These are used to identify the pressed key on the server side. They have nothing to do with actual key codes.
    public static final int BASE_KEY_MASK               = 0x0000FFFF;
    public static final int KEYBIND_ID_TOGGLE_MODE      = 0x00000001;
    public static final int KEYCODE_SCROLL              = 0x00001000;

    public static final int MODIFIER_MASK               = 0x000F0000;
    public static final int KEYBIND_MODIFIER_SHIFT      = 0x00010000;
    public static final int KEYBIND_MODIFIER_CONTROL    = 0x00020000;
    public static final int KEYBIND_MODIFIER_ALT        = 0x00040000;
    public static final int SCROLL_MODIFIER_REVERSE     = 0x00100000;

    public static final int KEYCODE_SNEAK               = 0x01000000;
    public static final int KEYCODE_JUMP                = 0x02000000;
    public static final int KEYCODE_CUSTOM_1            = 0x10000000;

    public static final int MOD_NONE                    = 0x00000000;
    public static final int MOD_SHIFT                   = KEYBIND_MODIFIER_SHIFT;
    public static final int MOD_CTRL                    = KEYBIND_MODIFIER_CONTROL;
    public static final int MOD_ALT                     = KEYBIND_MODIFIER_ALT;
    public static final int MOD_SHIFT_CTRL              = KEYBIND_MODIFIER_SHIFT | KEYBIND_MODIFIER_CONTROL;
    public static final int MOD_SHIFT_ALT               = KEYBIND_MODIFIER_SHIFT | KEYBIND_MODIFIER_ALT;
    public static final int MOD_SHIFT_CTRL_ALT          = KEYBIND_MODIFIER_SHIFT | KEYBIND_MODIFIER_CONTROL | KEYBIND_MODIFIER_ALT;
    public static final int MOD_CTRL_ALT                = KEYBIND_MODIFIER_CONTROL | KEYBIND_MODIFIER_ALT;

    public enum EnumKey
    {
        TOGGLE      (KEYBIND_ID_TOGGLE_MODE),
        SCROLL      (KEYCODE_SCROLL),
        SNEAK       (KEYCODE_SNEAK),
        JUMP        (KEYCODE_JUMP),
        CUSTOM_1    (KEYCODE_CUSTOM_1);

        private final int keyCode;

        private EnumKey(int keyCode)
        {
            this.keyCode = keyCode;
        }

        public int getKeyCode()
        {
            return this.keyCode;
        }

        public static int getBaseKey(int value)
        {
            return (value & BASE_KEY_MASK);
        }

        public static int getModifier(int value)
        {
            return (value & MODIFIER_MASK);
        }

        public static boolean matches(int value, EnumKey key, int modifier)
        {
            return getBaseKey(value) == key.keyCode && getModifier(value) == modifier;
        }

        public boolean matches(int value, int modifier)
        {
            return this.matches(value, modifier, 0);
        }

        public boolean matches(int value, int modRequire, int modIgnore)
        {
            return getBaseKey(value) == this.keyCode && (getModifier(value) & ~modIgnore) == modRequire;
        }

        public static boolean keypressContainsShift(int value)
        {
            return (value & KEYBIND_MODIFIER_SHIFT) != 0;
        }

        public static boolean keypressContainsControl(int value)
        {
            return (value & KEYBIND_MODIFIER_CONTROL) != 0;
        }

        public static boolean keypressContainsAlt(int value)
        {
            return (value & KEYBIND_MODIFIER_ALT) != 0;
        }

        public static boolean keypressActionIsReversed(int value)
        {
            return (value & SCROLL_MODIFIER_REVERSE) != 0;
        }
    }
}
