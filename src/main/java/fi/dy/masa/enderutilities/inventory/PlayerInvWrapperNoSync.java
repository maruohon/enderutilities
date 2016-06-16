package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper;

public class PlayerInvWrapperNoSync extends CombinedInvWrapper
{
    public PlayerInvWrapperNoSync(InventoryPlayer inv)
    {
        super(new PlayerMainInvWrapperNoSync(inv), new PlayerArmorInvWrapper(inv), new PlayerOffhandInvWrapper(inv));
    }
}
