package fi.dy.masa.enderutilities.inventory.wrapper;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

public class PlayerMainInvWrapperNoSync extends RangedWrapper
{
    private final InventoryPlayer inventoryPlayer;

    public PlayerMainInvWrapperNoSync(InventoryPlayer inv)
    {
        super(new InvWrapper(inv), 0, inv.mainInventory.length);
        inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        ItemStack stackRemaining = super.insertItem(slot, stack, simulate);

        if (stackRemaining == null || stackRemaining.stackSize != stack.stackSize)
        {
            // the stack in the slot changed, animate it
            ItemStack stackSlot = getStackInSlot(slot);

            if(stackSlot != null)
            {
                if (this.getInventoryPlayer().player.getEntityWorld().isRemote)
                {
                    stackSlot.animationsToGo = 5;
                }
            }
        }

        return stackRemaining;
    }

    public InventoryPlayer getInventoryPlayer()
    {
        return inventoryPlayer;
    }
}
