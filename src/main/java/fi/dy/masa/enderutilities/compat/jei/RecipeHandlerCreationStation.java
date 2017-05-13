package fi.dy.masa.enderutilities.compat.jei;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import fi.dy.masa.enderutilities.inventory.container.ContainerCreationStation;
import fi.dy.masa.enderutilities.util.SlotRange;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

public class RecipeHandlerCreationStation<C extends Container> implements IRecipeTransferInfo<ContainerCreationStation>
{
    @Override
    public Class<ContainerCreationStation> getContainerClass()
    {
        return ContainerCreationStation.class;
    }

    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public boolean canHandle(ContainerCreationStation container)
    {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(ContainerCreationStation container)
    {
        List<Slot> slots = new ArrayList<Slot>();
        SlotRange slotRange = container.getCraftingGridSlotRange(container.getLastInteractedCraftingGridId());

        for (int slotNum = slotRange.first; slotNum < slotRange.lastExc; slotNum++)
        {
            slots.add(container.getSlot(slotNum));
        }

        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(ContainerCreationStation container)
    {
        List<Slot> slots = new ArrayList<Slot>();
        SlotRange playerInventorySlots = container.getPlayerMainInventorySlotRange();
        SlotRange customInventorySlots = container.getCustomInventorySlotRange();

        for (int slotNum = customInventorySlots.first; slotNum < customInventorySlots.lastExc; slotNum++)
        {
            slots.add(container.getSlot(slotNum));
        }

        for (int slotNum = playerInventorySlots.first; slotNum < playerInventorySlots.lastExc; slotNum++)
        {
            slots.add(container.getSlot(slotNum));
        }

        return slots;
    }
}
