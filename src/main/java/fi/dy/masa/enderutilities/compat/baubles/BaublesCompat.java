package fi.dy.masa.enderutilities.compat.baubles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import fi.dy.masa.enderutilities.inventory.container.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.container.ContainerInventorySwapper.BaublesInvProviderBase;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerBaubles;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerBaubles.BaublesItemValidatorBase;

public class BaublesCompat
{
    @CapabilityInject(IBaublesItemHandler.class)
    public static <T> void initBaubles(Capability<T> capability)
    {
        SlotItemHandlerBaubles.setBaublesItemValidator(new BaublesItemValidator());
        ContainerInventorySwapper.setBaublesInvProvider(new BaublesInvProvider());
    }

    public static class BaublesItemValidator extends BaublesItemValidatorBase
    {
        @Override
        public boolean isItemValidForSlot(SlotItemHandlerBaubles slot, ItemStack stack)
        {
            return slot.getItemHandler() instanceof IBaublesItemHandler &&
                    ((IBaublesItemHandler) slot.getItemHandler()).isItemValidForSlot(slot.getSlotIndex(), stack, slot.getContainer().player);
        }
    }

    public static class BaublesInvProvider extends BaublesInvProviderBase
    {
        @Override
        public IItemHandler getBaublesInventory(EntityPlayer player)
        {
            return BaublesApi.getBaublesHandler(player);
        }
    }
}
