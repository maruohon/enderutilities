package fi.dy.masa.enderutilities.inventory;

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
            this.amountCrafted += Math.min(amount, this.getStack().stackSize);
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
            stack.onCrafting(this.player.worldObj, this.player, this.amountCrafted);
        }

        this.amountCrafted = 0;

        if (stack.getItem() == Item.getItemFromBlock(Blocks.crafting_table))
        {
            this.player.addStat(AchievementList.buildWorkBench);
        }

        if (stack.getItem() instanceof ItemPickaxe)
        {
            this.player.addStat(AchievementList.buildPickaxe);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.furnace))
        {
            this.player.addStat(AchievementList.buildFurnace);
        }

        if (stack.getItem() instanceof ItemHoe)
        {
            this.player.addStat(AchievementList.buildHoe);
        }

        if (stack.getItem() == Items.bread)
        {
            this.player.addStat(AchievementList.makeBread);
        }

        if (stack.getItem() == Items.cake)
        {
            this.player.addStat(AchievementList.bakeCake);
        }

        if (stack.getItem() instanceof ItemPickaxe && ((ItemPickaxe)stack.getItem()).getToolMaterial() != Item.ToolMaterial.WOOD)
        {
            this.player.addStat(AchievementList.buildBetterPickaxe);
        }

        if (stack.getItem() instanceof ItemSword)
        {
            this.player.addStat(AchievementList.buildSword);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.enchanting_table))
        {
            this.player.addStat(AchievementList.enchantments);
        }

        if (stack.getItem() == Item.getItemFromBlock(Blocks.bookshelf))
        {
            this.player.addStat(AchievementList.bookcase);
        }
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(playerIn, stack, craftMatrix);
        this.onCrafting(stack);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(playerIn);
        ItemStack[] remainingItems = CraftingManager.getInstance().getRemainingItems(this.craftMatrix, playerIn.worldObj);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < remainingItems.length; ++i)
        {
            ItemStack stackInSlot = this.craftMatrix.getStackInSlot(i);
            ItemStack remainingItemsInSlot = remainingItems[i];

            if (stackInSlot != null)
            {
                this.craftMatrix.decrStackSize(i, 1);
                stackInSlot = this.craftMatrix.getStackInSlot(i);
            }

            if (remainingItemsInSlot != null)
            {
                if (stackInSlot == null)
                {
                    this.craftMatrix.setInventorySlotContents(i, remainingItemsInSlot);
                }
                else if (ItemStack.areItemsEqual(stackInSlot, remainingItemsInSlot) && ItemStack.areItemStackTagsEqual(stackInSlot, remainingItemsInSlot))
                {
                    remainingItemsInSlot.stackSize += stackInSlot.stackSize;
                    this.craftMatrix.setInventorySlotContents(i, remainingItemsInSlot);
                }
                else if (this.player.inventory.addItemStackToInventory(remainingItemsInSlot) == false)
                {
                    this.player.dropPlayerItemWithRandomChoice(remainingItemsInSlot, false);
                }
            }
        }
    }
}
