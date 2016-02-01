package fi.dy.masa.enderutilities.event;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
@Event.HasResult
public class PlayerItemPickupEvent extends PlayerEvent
{
    public final List<ItemStack> drops;

    /**
     * This event is called when a player is about to receive items into their inventory.
     * This is used for example by some items that try to insert items
     * directly into the player's inventory.
     * This event can then be received by other items or other things that want to
     * have a say on what will happen to those items, or if they can even be allowed
     * to be picked up. The drops list can be modified by said event if they process
     * some or all of the drops in some way.
     *
     * The event can be canceled, and no further processing will be done.
     * 
     */
    public PlayerItemPickupEvent(EntityPlayer player, List<ItemStack> items)
    {
        super(player);
        this.drops = items;
    }
}
