package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

public class PlayerInvWrapperNoSync extends CombinedInvWrapper
{
    public PlayerInvWrapperNoSync(InventoryPlayer inv)
    {
        super(new PlayerMainInvWrapperNoSync(inv), new PlayerArmorInvWrapperLimited(inv), new PlayerOffhandInvWrapper(inv));
    }
}
