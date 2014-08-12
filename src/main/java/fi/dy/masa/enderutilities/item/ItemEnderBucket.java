package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.ItemFluidContainer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemEnderBucket extends ItemFluidContainer
{
	@SideOnly(Side.CLIENT)
	public IIcon[] iconParts;

	public ItemEnderBucket()
	{
		// the id is actually unused though...
		this(Item.getIdFromItem(GameRegistry.findItem(Reference.MOD_ID, ReferenceItem.NAME_ITEM_ENDER_BUCKET)));
	}

	public ItemEnderBucket(int itemID)
	{
		super(itemID);
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_BUCKET);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()) + ".32");
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
		this.setCapacity(ReferenceItem.ENDER_BUCKET_MAX_AMOUNT);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
	{
		// Do nothing on the client side
		if (world.isRemote == true)
		{
			return itemStack;
		}

		// First, get the stored fluid, if any
		FluidStack storedFluidStack = this.getFluid(itemStack);
		int storedFluidAmount = 0;

		if (storedFluidStack != null)
		{
			storedFluidAmount = storedFluidStack.amount;
		}

		// Next find out what block we are targeting
		// FIXME the boolean flag does what exactly? In vanilla it seems to indicate that the bucket is empty.
		MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

		if (movingobjectposition == null || movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
		{
			return itemStack;
		}

		int x = movingobjectposition.blockX;
		int y = movingobjectposition.blockY;
		int z = movingobjectposition.blockZ;

		Block targetBlock;

		targetBlock = world.getBlock(x, y, z);
		if (targetBlock == null || targetBlock.getMaterial() == null)
		{
			return itemStack;
		}

		// Spawn safe zone checks etc.
		if (world.canMineBlock(player, x, y, x) == false)
		{
			return itemStack;
		}

		// Fluid block
		if (targetBlock.getMaterial().isLiquid() == true)
		{
			Fluid storedFluid = null;
			Fluid targetFluid = null;
			IFluidBlock iFluidBlock = null;
			FluidStack fluidStack = null;

			if (storedFluidStack != null)
			{
				storedFluid = storedFluidStack.getFluid();
			}
			if (targetBlock instanceof IFluidBlock)
			{
				iFluidBlock = (IFluidBlock)targetBlock;
				targetFluid = iFluidBlock.getFluid();
			}
			else
			{
				// We need to convert flowing water and lava to the still variant for logic stuffs
				// We will always convert them to the flowing variant before placing
				if (targetBlock == Blocks.flowing_water) { targetBlock = Blocks.water; }
				else if (targetBlock == Blocks.flowing_lava) { targetBlock = Blocks.lava; }

				//targetFluid = new Fluid(Block.blockRegistry.getNameForObject(targetBlock));
				targetFluid = FluidRegistry.lookupFluidForBlock(targetBlock);
			}

			// Empty || (space && not sneaking && same fluid) => trying to pick up fluid
			if (storedFluidAmount == 0 ||
				((this.capacity - storedFluidAmount) >= FluidContainerRegistry.BUCKET_VOLUME && player.isSneaking() == false && storedFluid == targetFluid))
			{
				if (player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, itemStack) == false)
				{
					return itemStack;
				}

				// Implements IFluidBlock
				if (iFluidBlock != null)
				{
					if (iFluidBlock.canDrain(world, x, y, z) == true)
					{
						fluidStack = iFluidBlock.drain(world, x, y, z, false); // simulate

						// Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
						if (this.fill(itemStack, fluidStack, false) <= (this.capacity - storedFluidAmount))
						{
							fluidStack = iFluidBlock.drain(world, x, y, z, true);
							this.fill(itemStack, fluidStack, true);
						}
					}
					return itemStack;
				}

				// Does not implement IFluidBlock
				if (targetFluid != null)
				{
					//fluidStack = new FluidStack(targetFluid, FluidContainerRegistry.BUCKET_VOLUME);
					fluidStack = FluidRegistry.getFluidStack(targetFluid.getName(), FluidContainerRegistry.BUCKET_VOLUME);
				}

				// Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
				if (this.fill(itemStack, fluidStack, false) >= FluidContainerRegistry.BUCKET_VOLUME)
				{
					if (world.setBlockToAir(x, y, z) == true)
					{
						this.fill(itemStack, fluidStack, true);
					}
				}
				return itemStack;
			}

			// Fluid stored, trying to place fluid
			if (storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				// (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
				if (storedFluid != targetFluid || player.isSneaking() == true)
				{
					if (this.tryPlaceContainedFluid(world, x, y, z, storedFluidStack) == true)
					{
						this.drain(itemStack, FluidContainerRegistry.BUCKET_VOLUME, true);
					}
				}
				return itemStack;
			}
		}
		// Non-fluid block
		else
		{
			TileEntity te = world.getTileEntity(x, y, z);

			// Is this a TileEntity that is also some sort of a fluid storage device?
			if (te != null && te instanceof IFluidHandler)
			{
				IFluidHandler iFluidHandler = (IFluidHandler)te;
				FluidStack fluidStack;
				ForgeDirection fDir = ForgeDirection.getOrientation(movingobjectposition.sideHit);

				// With tanks we pick up fluid when not sneaking
				if (player.isSneaking() == false)
				{
					int space = this.capacity - storedFluidAmount;

					// We can still store more fluid
					if (space > 0)
					{
						if (space > FluidContainerRegistry.BUCKET_VOLUME)
						{
							space = FluidContainerRegistry.BUCKET_VOLUME;
						}

						fluidStack = iFluidHandler.drain(fDir, space, false); // simulate

						// If the bucket is currently empty, or the tank's fluid is the same we currently have
						if (fluidStack != null && (storedFluidAmount == 0 || fluidStack.isFluidEqual(storedFluidStack) == true))
						{
							fluidStack = iFluidHandler.drain(fDir, space, true); // actually drain
							this.fill(itemStack, fluidStack, true);
						}
					}
					return itemStack;
				}
				// Sneaking, try to deposit fluid to the tank
				else
				{
					// Some fluid stored (we allow depositing less than a buckets worth of fluid into _tanks_)
					if (storedFluidAmount > 0)
					{
						// simulate, we try to deposit up to one bucket per use
						fluidStack = this.drain(itemStack, FluidContainerRegistry.BUCKET_VOLUME, false);

						// Check if we can deposit (at least some) the fluid we have stored
						if (iFluidHandler.fill(fDir, fluidStack, false) > 0) // simulate
						{
							int amount = iFluidHandler.fill(fDir, fluidStack, true);
							this.drain(itemStack, amount, true); // actually drain fluid from the bucket (the amount that was deposited into the container)
						}
					}
				}
			}

			// target block is not fluid and not a tank: try to place a fluid block in world against the targeted side
			else if (storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(movingobjectposition.sideHit);
				x += dir.offsetX;
				y += dir.offsetY;
				z += dir.offsetZ;

				if (this.tryPlaceContainedFluid(world, x, y, z, storedFluidStack) == true)
				{
					this.drain(itemStack, FluidContainerRegistry.BUCKET_VOLUME, true);
				}
			}
		}

		return itemStack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4)
	{
/*
		if (EnderUtilities.proxy.isShiftKeyDown() == false)

		{
			list.add("<" + StatCollector.translateToLocal("gui.tooltip.holdshift") + ">");
			return;
		}
*/

		FluidStack fluidStack = this.getFluid(itemStack);
		String fluidName = "<" + StatCollector.translateToLocal("gui.tooltip.empty") + ">";
		int amount = 0;
		String pre = "" + EnumChatFormatting.BLUE;
		String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

		if (fluidStack != null && fluidStack.getFluid() != null)
		{
			amount = fluidStack.amount;
			fluidName = pre + fluidStack.getFluid().getLocalizedName(fluidStack) + rst;
		}

		list.add(StatCollector.translateToLocal("gui.tooltip.fluid") + ": " + fluidName);
		list.add(StatCollector.translateToLocal("gui.tooltip.amount") + String.format(": %d mB", amount));
	}

	/*
	 *  Attempts to place one fluid block in the world, identified by the given FluidStack
	 */
	public boolean tryPlaceContainedFluid(World world, int x, int y, int z, FluidStack fluidStack)
	{
		if (fluidStack == null || fluidStack.getFluid() == null ||
			fluidStack.getFluid().getBlock() == null || fluidStack.getFluid().canBePlacedInWorld() == false)
		{
			return false;
		}

		Block block = fluidStack.getFluid().getBlock();

		// We need to convert water and lava to the flowing variant, otherwise we get non-flowing source blocks
		if (block == Blocks.water) { block = Blocks.flowing_water; }
		else if (block == Blocks.lava) { block = Blocks.flowing_lava; }

		Material material = world.getBlock(x, y, z).getMaterial();

		if (world.isAirBlock(x, y, z) == false && material.isSolid() == true)
		{
			return false;
		}

		if (world.provider.isHellWorld && block == Blocks.flowing_water)
		{
			world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; ++l)
			{
				world.spawnParticle("largesmoke", (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D);
			}
		}
		else
		{
			if (world.isRemote == false && material.isSolid() == false && material.isLiquid() == false)
			{
				// Set a replaceable block to air, and drop the items
				world.func_147480_a(x, y, z, true);
			}

			world.setBlock(x, y, z, block, 0, 3);
		}

		return true;
	}

	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
	{
		int drained = 0;

		NBTTagCompound nbt = container.getTagCompound();
		if (nbt == null || nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == false)
		{
			return null;
		}

		FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));
		if (fluidStack == null)
		{
			return null;
		}

		// Amount that will or would be drained
		drained = Math.min(fluidStack.amount, maxDrain);

		// If not just simulating
		if (doDrain == true)
		{
			// Drained all the fluid
			if (drained >= fluidStack.amount)
			{
				nbt.removeTag("Fluid");
				if (nbt.hasNoTags() == true)
				{
					container.setTagCompound(null);
				}
			}
			else
			{
				NBTTagCompound fluidTag = nbt.getCompoundTag("Fluid");
				fluidTag.setInteger("Amount", fluidTag.getInteger("Amount") - drained);
				nbt.setTag("Fluid", fluidTag);
			}
		}

		fluidStack.amount = drained;

		return fluidStack; // Return the FluidStack that was or would be drained from the item
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString());
		this.iconParts = new IIcon[3];
		this.iconParts[0] = iconRegister.registerIcon(Textures.getTextureName(this.getUnlocalizedName()) + ".32.main");
		this.iconParts[1] = iconRegister.registerIcon(Textures.getTextureName(this.getUnlocalizedName()) + ".32.windowbg");
		this.iconParts[2] = iconRegister.registerIcon(Textures.getTextureName(this.getUnlocalizedName()) + ".32.inside");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconPart(int i)
	{
		if (i >= this.iconParts.length)
		{
			i = 0;
		}
		return this.iconParts[i];
	}

	/**
	 * ItemStack sensitive version of hasContainerItem
	 * @param stack The current item stack
	 * @return True if this item has a 'container'
	 */
	public boolean hasContainerItem(ItemStack itemStack)
	{
		/**
		 * True if this Item has a container item (a.k.a. crafting result)
		 */
		FluidStack fluidStack = this.getFluid(itemStack);

		if (fluidStack != null && fluidStack.amount > 0 && fluidStack.getFluid() != null && fluidStack.getFluid().getName().equals("lava") == true)
		{
			return true;
		}

		return false;
	}

	/**
	 * ItemStack sensitive version of getContainerItem.
	 * Returns a full ItemStack instance of the result.
	 *
	 * @param itemStack The current ItemStack
	 * @return The resulting ItemStack
	 */
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		if (hasContainerItem(itemStack) == false)
		{
			return null;
		}

		// We use 250 mB of lava per "use", mainly for use as a furnace fuel
		FluidStack fluidStack = this.getFluid(itemStack);
		if (fluidStack != null && fluidStack.getFluid() != null && fluidStack.amount > 250 && fluidStack.getFluid().getName().equals("lava") == true)
		{
			fluidStack.amount -= 250;
			NBTTagCompound nbt = itemStack.getTagCompound();
			ItemStack newStack = new ItemStack(EnderUtilitiesItems.enderBucket, 1, 0);
			newStack.setItemDamage(itemStack.getItemDamage());
			nbt.setTag("Fluid", fluidStack.writeToNBT(new NBTTagCompound()));
			newStack.setTagCompound(nbt);
			return newStack;
		}

		return new ItemStack(EnderUtilitiesItems.enderBucket, 1, 0);
	}
}
