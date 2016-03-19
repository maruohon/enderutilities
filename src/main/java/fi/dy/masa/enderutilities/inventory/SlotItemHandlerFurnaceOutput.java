package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.items.IItemHandler;

public class SlotItemHandlerFurnaceOutput extends SlotItemHandlerGeneric
{
    private final EntityPlayer player;
    private int amountCrafted;

    public SlotItemHandlerFurnaceOutput(EntityPlayer player, IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.player = player;
    }

    @Override
    public boolean isItemValid(ItemStack stack)
    {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int amount)
    {
        if (this.getHasStack() == true)
        {
            this.amountCrafted += Math.min(amount, this.getStack().stackSize);
        }

        return super.decrStackSize(amount);
    }

    @Override
    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
    {
        this.onCrafting(stack);
        super.onPickupFromSlot(playerIn, stack);
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
        stack.onCrafting(this.player.worldObj, this.player, this.amountCrafted);

        if (this.player.worldObj.isRemote == false)
        {
            int i = this.amountCrafted;
            float f = FurnaceRecipes.instance().getSmeltingExperience(stack);

            if (f == 0.0F)
            {
                i = 0;
            }
            else if (f < 1.0F)
            {
                int j = MathHelper.floor_float((float)i * f);

                if (j < MathHelper.ceiling_float_int((float)i * f) && Math.random() < (double)((float)i * f - (float)j))
                {
                    ++j;
                }

                i = j;
            }

            while (i > 0)
            {
                int k = EntityXPOrb.getXPSplit(i);
                i -= k;
                this.player.worldObj.spawnEntityInWorld(new EntityXPOrb(this.player.worldObj, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, k));
            }
        }

        this.amountCrafted = 0;

        net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerSmeltedEvent(player, stack);

        if (stack.getItem() == Items.iron_ingot)
        {
            this.player.addStat(AchievementList.acquireIron);
        }

        if (stack.getItem() == Items.cooked_fish)
        {
            this.player.addStat(AchievementList.cookFish);
        }
    }
}
