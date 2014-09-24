package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
				if (FMLClientHandler.instance().getClientPlayerEntity() != null)
				{
					EntityPlayer player = FMLClientHandler.instance().getClientPlayerEntity();
					if (player.getCurrentEquippedItem() != null)
					{
						ItemStack stack = player.getCurrentEquippedItem();
						if (stack.getItem() instanceof IKeyBound)
						{
							if (player.worldObj.isRemote == true)
							{
								PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(ReferenceKeys.KEYBIND_ID_TOGGLE_MODE));
							}
						}
					}
				}
			}
		}
	}
}
