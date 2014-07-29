package fi.dy.masa.enderutilities.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public class ContainerEnderFurnace extends ContainerEnderUtilitiesInventory
{
	private TileEntityEnderFurnace teef;
	private byte lastOperatingMode;
	private byte lastOutputMode;

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

			if (this.teef.operatingMode != this.lastOperatingMode)
			{
				icrafting.sendProgressBarUpdate(this, 0, this.teef.operatingMode);
			}

			if (this.teef.outputMode != this.lastOutputMode)
			{
				icrafting.sendProgressBarUpdate(this, 1, this.teef.outputMode);
			}

			this.lastOperatingMode = this.teef.operatingMode;
			this.lastOutputMode = this.teef.outputMode;
		}
	}

	@Override
	public void addCraftingToCrafters(ICrafting icrafting)
	{
		super.addCraftingToCrafters(icrafting);
		icrafting.sendProgressBarUpdate(this, 0, this.teef.operatingMode);
		icrafting.sendProgressBarUpdate(this, 1, this.teef.outputMode);
	}

	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int var, int val)
	{
		switch(var)
		{
			case 0:
				this.teef.operatingMode = (byte)val;
				break;
			case 1:
				this.teef.outputMode = (byte)val;
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			default:
		}
	}
}
