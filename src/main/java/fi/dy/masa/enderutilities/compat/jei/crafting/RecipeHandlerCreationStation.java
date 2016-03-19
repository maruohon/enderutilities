package fi.dy.masa.enderutilities.compat.jei.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import fi.dy.masa.enderutilities.inventory.ContainerCreationStation;
import fi.dy.masa.enderutilities.util.SlotRange;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

public class RecipeHandlerCreationStation implements IRecipeTransferInfo
{
    @Override
    public Class<? extends Container> getContainerClass()
    {
        return ContainerCreationStation.class;
    }

    @Override
    public String getRecipeCategoryUid()
    {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public List<Slot> getRecipeSlots(Container container)
    {
        if ((container instanceof ContainerCreationStation) == false)
        {
            return new ArrayList<Slot>();
        }

        List<Slot> slots = new ArrayList<Slot>();
        ContainerCreationStation containerCS = (ContainerCreationStation)container;
        SlotRange slotRange = containerCS.getCraftingGridSlotRange(containerCS.getLastInteractedCraftingGridId());

        for (int slotNum = slotRange.first; slotNum < slotRange.lastExc; slotNum++)
        {
            slots.add(container.getSlot(slotNum));
        }

        return slots;
    }

    @Override
    public List<Slot> getInventorySlots(Container container)
    {
        if ((container instanceof ContainerCreationStation) == false)
        {
            return new ArrayList<Slot>();
        }

        List<Slot> slots = new ArrayList<Slot>();
        ContainerCreationStation containerCS = (ContainerCreationStation)container;
        SlotRange playerInventorySlots = containerCS.getPlayerMainInventorySlotRange();
        SlotRange customInventorySlots = containerCS.getCustomInventorySlotRange();

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
