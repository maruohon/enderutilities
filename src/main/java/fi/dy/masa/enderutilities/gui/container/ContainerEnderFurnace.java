package fi.dy.masa.enderutilities.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class ContainerEnderFurnace extends ContainerEnderUtilitiesInventory
{
	private TileEntityEnderFurnace teef;
	public int burnTimeRemaining;
	public int burnTimeFresh;
	public int cookTime;
	public int cookTimeFresh;

	public ContainerEnderFurnace(TileEntityEnderFurnace te, InventoryPlayer inventory)
	{
		super(te, inventory);
		this.teef = te;
	}

	protected void addSlots()
	{
		this.addSlotToContainer(new Slot(this.te, 0, 34, 17));
		this.addSlotToContainer(new Slot(this.te, 1, 34, 53));
		this.addSlotToContainer(new SlotFurnace(this.inventoryPlayer.player, this.te, 2, 88, 35));
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		ICrafting icrafting;
		for (int i = 0; i < this.crafters.size(); ++i)
		{
			icrafting = (ICrafting)this.crafters.get(i);

			if (this.teef.burnTimeRemaining != this.burnTimeRemaining)
			{
				icrafting.sendProgressBarUpdate(this, 0, this.teef.burnTimeRemaining);
			}

			if (this.teef.burnTimeFresh != this.burnTimeFresh)
			{
				icrafting.sendProgressBarUpdate(this, 1, this.teef.burnTimeFresh);
			}

			if (this.teef.cookTime != this.cookTime)
			{
				icrafting.sendProgressBarUpdate(this, 2, this.teef.cookTime);
			}

			if (this.teef.cookTimeFresh != this.cookTimeFresh)
			{
				icrafting.sendProgressBarUpdate(this, 3, this.teef.cookTimeFresh);
			}

			this.burnTimeRemaining = this.teef.burnTimeRemaining;
			this.burnTimeFresh = this.teef.burnTimeFresh;
			this.cookTime = this.teef.cookTime;
			this.cookTimeFresh = this.teef.cookTimeFresh;
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting icrafting)
	{
		super.addCraftingToCrafters(icrafting);
		icrafting.sendProgressBarUpdate(this, 0, this.teef.burnTimeRemaining);
		icrafting.sendProgressBarUpdate(this, 1, this.teef.burnTimeFresh);
		icrafting.sendProgressBarUpdate(this, 2, this.teef.cookTime);
		icrafting.sendProgressBarUpdate(this, 3, this.teef.cookTimeFresh);
	}

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int var, int val)
	{
		switch(var)
		{
			case 0:
				this.teef.burnTimeRemaining = val;
				break;
			case 1:
				this.teef.burnTimeFresh = val;
				break;
			case 2:
				this.teef.cookTime = val;
				break;
			case 3:
				this.teef.cookTimeFresh = val;
				break;
			default:
		}
	}
}
