package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.settings.Keybindings;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;

public class InputEventHandler
{
	public InputEventHandler()
	{
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onInput(InputEvent event)
	{
		// In-game (no GUI open)
		if (FMLClientHandler.instance().getClient().inGameHasFocus == true)
		{
			if (Keybindings.keyToggleMode.isPressed() == true)
			{
				EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
				if (player != null && player.worldObj.isRemote == true &&
					player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() instanceof IKeyBound)
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
			}
		}
	}
}
