package fi.dy.masa.enderutilities.reference.key;

import fi.dy.masa.enderutilities.reference.Reference;

public class ReferenceKeys
{
	public static final String KEYBIND_CAREGORY_ENDERUTILITIES = "category." + Reference.MOD_ID;

	public static final String KEYBIND_TOGGLE_MODE = "key." + Reference.MOD_ID + ".togglemode";

	public static final int KEYBIND_DEFAULT_TOGGLE_MODE = -98; // Middle mouse

	// These are used to identify the pressed key on the server side. They have nothing to do with actual key codes.
	public static final int KEYBIND_ID_TOGGLE_MODE = 0;
}
