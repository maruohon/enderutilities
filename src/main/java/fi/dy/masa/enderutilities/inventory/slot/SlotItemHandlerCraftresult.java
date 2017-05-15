package fi.dy.masa.enderutilities.inventory.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.IItemHandler;

public class SlotItemHandlerCraftresult extends SlotItemHandlerGeneric
{
    private final EntityPlayer player;
    private final InventoryCrafting craftMatrix;
    private int amountCrafted;

    public SlotItemHandlerCraftresult(EntityPlayer player, InventoryCrafting craftMatrix, IItemHandler craftResult, int index, int xPosition, int yPosition)
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

        if (stack.getItem() == Item.getItemFromBlock(Blocks.CRAFTING_TABLE))
        {
            this.player.addStat(AchievementList.BUILD_WORK_BENCH);
        }

        if (stack.getItem() instanceof ItemPickaxe)
        {
            this.player.addStat(AchievementList.BUILD_PICKAXE);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.FURNACE))
        {
            this.player.addStat(AchievementList.BUILD_FURNACE);
        }

        if (stack.getItem() instanceof ItemHoe)
        {
            this.player.addStat(AchievementList.BUILD_HOE);
        }

        if (stack.getItem() == Items.BREAD)
        {
            this.player.addStat(AchievementList.MAKE_BREAD);
        }

        if (stack.getItem() == Items.CAKE)
        {
            this.player.addStat(AchievementList.BAKE_CAKE);
        }

        if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe)stack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD)
        {
            this.player.addStat(AchievementList.BUILD_BETTER_PICKAXE);
        }

        if (stack.getItem() instanceof ItemSword)
        {
            this.player.addStat(AchievementList.BUILD_SWORD);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.ENCHANTING_TABLE))
        {
            this.player.addStat(AchievementList.ENCHANTMENTS);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.BOOKSHELF))
        {
            this.player.addStat(AchievementList.BOOKCASE);
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
