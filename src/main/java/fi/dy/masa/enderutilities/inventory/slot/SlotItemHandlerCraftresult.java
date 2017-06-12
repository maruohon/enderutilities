package fi.dy.masa.enderutilities.inventory.slot;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

public class SlotItemHandlerCraftresult extends SlotItemHandlerGeneric
{
    private final EntityPlayer player;
    private final InventoryCrafting craftMatrix;
    private int amountCrafted;

    public SlotItemHandlerCraftresult(EntityPlayer player, InventoryCrafting craftMatrix, IItemHandler craftResult,
            int index, int xPosition, int yPosition)
    {
        super(craftResult, index, xPosition, yPosition);
        this.player = player;
        this.craftMatrix = craftMatrix;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        if (this.getHasStack())
        {
            this.amountCrafted += Math.min(amount, this.getStack().getCount());
        }

        return super.decrStackSize(amount);
    }

    @Override
    protected void onCrafting(ItemStack stack, int amount)
    {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    @Override
    protected void onCrafting(ItemStack stack)
    {
        if (this.amountCrafted > 0)
        {
            stack.onCrafting(this.player.getEntityWorld(), this.player, this.amountCrafted);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, stack, this.craftMatrix);
        }

        this.amountCrafted = 0;

        FIXME 1.12 update
        InventoryCraftResult inventorycraftresult = (InventoryCraftResult) this.inventory;
        IRecipe irecipe = inventorycraftresult.func_193055_i();

        if (irecipe != null && !irecipe.func_192399_d())
        {
            this.player.func_192021_a(Lists.newArrayList(irecipe));
            inventorycraftresult.func_193056_a((IRecipe)null);
        }
    }

    public ItemStack onTake(EntityPlayer player, ItemStack stack)
    {
        this.onCrafting(stack);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
        NonNullList<ItemStack> remainingItems = CraftingManager.getInstance().getRemainingItems(this.craftMatrix, player.getEntityWorld());
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < remainingItems.size(); i++)
        {
            ItemStack stackInSlot = this.craftMatrix.getStackInSlot(i);
            ItemStack remainingItemsInSlot = remainingItems.get(i);

            if (stackInSlot.isEmpty() == false)
            {
                this.craftMatrix.decrStackSize(i, 1);
                stackInSlot = this.craftMatrix.getStackInSlot(i);
            }

            if (remainingItemsInSlot.isEmpty() == false)
            {
                if (stackInSlot.isEmpty())
                {
                    this.craftMatrix.setInventorySlotContents(i, remainingItemsInSlot);
                }
                else if (ItemStack.areItemsEqual(stackInSlot, remainingItemsInSlot) &&
                         ItemStack.areItemStackTagsEqual(stackInSlot, remainingItemsInSlot))
                {
                    remainingItemsInSlot.grow(stackInSlot.getCount());
                    this.craftMatrix.setInventorySlotContents(i, remainingItemsInSlot);
                }
                else if (this.player.inventory.addItemStackToInventory(remainingItemsInSlot) == false)
                {
                    this.player.dropItem(remainingItemsInSlot, false);
                }
            }
        }

        return stack;
    }
}
