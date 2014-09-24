package fi.dy.masa.enderutilities.item.tool;

import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class ItemEnderTool extends ItemTool implements IKeyBound
{
	private static final Set<Block> pickaxeBlocksEffectiveAgainst = Sets.newHashSet(new Block[]{
			Blocks.stone, Blocks.cobblestone, Blocks.mossy_cobblestone,
			Blocks.stone_slab, Blocks.double_stone_slab,
			Blocks.sandstone, Blocks.ice,
			Blocks.iron_ore, Blocks.iron_block,
			Blocks.coal_block, Blocks.coal_ore,
			Blocks.gold_block, Blocks.gold_ore,
			Blocks.diamond_ore, Blocks.diamond_block,
			Blocks.netherrack, Blocks.nether_brick,
			Blocks.lapis_ore, Blocks.lapis_block,
			Blocks.redstone_block, Blocks.redstone_ore, Blocks.lit_redstone_ore,
			Blocks.rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.activator_rail
		});
	public float efficiencyOnProperMaterial = 9.0f;

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	@SideOnly(Side.CLIENT)
	// Note: Sword "has to be" last, because it has one extra texture compared to the others
	String[] tools = new String[] {	ReferenceItem.NAME_ITEM_ENDER_PICKAXE,
									ReferenceItem.NAME_ITEM_ENDER_AXE,
									ReferenceItem.NAME_ITEM_ENDER_SHOVEL,
									ReferenceItem.NAME_ITEM_ENDER_HOE,
									ReferenceItem.NAME_ITEM_ENDER_SWORD};
	@SideOnly(Side.CLIENT)
	String[] parts = new String[] {".rod", ".head.1", ".head.2", ".core.1", ".core.2", ".core.3",
									".capacitor.1", ".capacitor.2", ".capacitor.3", ".linkcrystal"};

	public ItemEnderTool()
	{
		super(2.0f, Item.ToolMaterial.EMERALD, pickaxeBlocksEffectiveAgainst);
		this.setMaxStackSize(1);
		this.setMaxDamage(2048);
		this.setNoRepair();
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDERTOOL);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (this.getToolType(stack) == 0) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_PICKAXE; }
		if (this.getToolType(stack) == 1) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_AXE; }
		if (this.getToolType(stack) == 2) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_SHOVEL; }
		if (this.getToolType(stack) == 3) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_HOE; }
		if (this.getToolType(stack) == 4) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_SWORD; }

		return super.getUnlocalizedName();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		if (EUConfigs.disableItemEnderTool.getBoolean(false) == false)
		{
			for (int i = 0; i <= 4; i++)
			{
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public boolean isItemTool(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isRepairable ()
	{
		return false;
	}

	public int getToolType(ItemStack stack)
	{
		if (stack != null)
		{
			return stack.getItemDamage() & 0x0F;
		}
		return -1;
	}

	public String getToolClass(ItemStack stack)
	{
		if (stack != null)
		{
			if (this.getToolType(stack) == 0) { return "pickaxe"; }
			if (this.getToolType(stack) == 1) { return "axe"; }
			if (this.getToolType(stack) == 2) { return "shovel"; }
			if (this.getToolType(stack) == 3) { return "hoe"; }
			if (this.getToolType(stack) == 4) { return "sword"; }
		}
		return null;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		String tc = this.getToolClass(stack);
		return tc != null ? ImmutableSet.of(tc) : super.getToolClasses(stack);
	}

    /**
	 * Return the maxDamage for this ItemStack. Defaults to the maxDamage field in this item, 
	 * but can be overridden here for other sources such as NBT.
	 *
	 * @param stack The itemstack that is damaged
	 * @return the damage value
	 */
	@Override
	public int getMaxDamage(ItemStack stack)
	{
		/**
		 * Returns the maximum damage an item can take.
		 */
		return 2048;
	}

	/**
	 * Return if this itemstack is damaged. Note only called if {@link #isDamageable()} is true.
	 * @param stack the stack
	 * @return if the stack is damaged
	 */
	@Override
	public boolean isDamaged(ItemStack stack)
	{
		if (stack == null || stack.getTagCompound() == null)
		{
			return false;
		}
		if (stack.getTagCompound().getInteger("Damage") > 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Set the damage for this itemstack. Note, this method is responsible for zero checking.
	 * @param stack the stack
	 * @param damage the new damage value
	 */
/*
	@Override
	public void setDamage(ItemStack stack, int damage)
	{
		if (stack == null)
		{
			return;
		}
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}
		nbt.setInteger("Damage", damage);
		stack.setTagCompound(nbt);
	}
*/
    /**
	 * Return the itemDamage represented by this ItemStack. Defaults to the itemDamage field on ItemStack, but can be overridden here for other sources such as NBT.
	 *
	 * @param stack The itemstack that is damaged
	 * @return the damage value
	 */
/*
	@Override
	public int getDamage(ItemStack stack)
	{
		if (stack == null || stack.getTagCompound() == null)
		{
			return 0;
		}
		return stack.getTagCompound().getInteger("Damage");
	}
*/
	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
	{
		System.out.println("hitEntity()");
		if (stack == null)
		{
			return false;
		}
		if (this.getToolType(stack) != 4) // Not an Ender Sword, so some of the tools
		{
			stack.damageItem(2, living2);
		}
		return false;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase living)
	{
		System.out.println("onBlockDestroyed()");
		return false;
	}

	@Override
	public boolean func_150897_b(Block block)
	{
		System.out.println("func_150897_b()");
		int harvestLevel = 3;

		if (block == Blocks.obsidian)
		{
			return harvestLevel >= 3;
		}

		if (block == Blocks.diamond_block || block == Blocks.diamond_ore ||
			block == Blocks.emerald_block || block == Blocks.emerald_ore ||
			block == Blocks.gold_block || block == Blocks.gold_ore ||
			block == Blocks.redstone_ore || block == Blocks.lit_redstone_ore)
		{
			return harvestLevel >= 2;
		}

		if (block == Blocks.iron_block || block == Blocks.iron_ore ||
			block == Blocks.lapis_block || block == Blocks.lapis_ore ||
			block.getMaterial() == net.minecraft.block.material.Material.rock ||
			block.getMaterial() == net.minecraft.block.material.Material.iron ||
			block.getMaterial() == net.minecraft.block.material.Material.anvil)
		{
			return harvestLevel >= 1;
		}

		// Shovel
		if (block == Blocks.snow_layer || block == Blocks.snow)
		{
			return true;
		}

		return harvestLevel >= 1;
	}

	@Override
	public float func_150893_a(ItemStack stack, Block block)
	{
		System.out.println("func_150893_a()");
		if (this.getToolType(stack) == 0) // Ender Pickaxe
		{
			if (block.getMaterial() == net.minecraft.block.material.Material.rock ||
				block.getMaterial() == net.minecraft.block.material.Material.iron ||
				block.getMaterial() == net.minecraft.block.material.Material.anvil)
			{
				return this.efficiencyOnProperMaterial;
			}
		}
		else if (this.getToolType(stack) == 1) // Ender Axe
		{
			if (block.getMaterial() == net.minecraft.block.material.Material.wood ||
				block.getMaterial() == net.minecraft.block.material.Material.plants ||
				block.getMaterial() == net.minecraft.block.material.Material.vine)
			{
				return this.efficiencyOnProperMaterial;
			}
		}

		return super.func_150893_a(stack, block);
	}

    /**
	 * ItemStack sensitive version of {@link #canHarvestBlock(Block)}
	 * @param par1Block The block trying to harvest
	 * @param itemStack The itemstack used to harvest the block
	 * @return true if can harvest the block
	 */
	@Override
	public boolean canHarvestBlock(Block block, ItemStack stack)
	{
		System.out.println("canHarvestBlock()");
		//return func_150897_b(block);
		return true;
	}

	/**
	 * Metadata-sensitive version of getStrVsBlock
	 * @param itemstack The Item Stack
	 * @param block The block the item is trying to break
	 * @param metadata The items current metadata
	 * @return The damage strength
	 */
	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		//if (ForgeHooks.isToolEffective(stack, block, meta) || block == Blocks.obsidian || block == Blocks.redstone_ore || block == Blocks.lit_redstone_ore)
		if (ForgeHooks.isToolEffective(stack, block, meta))
		{
			System.out.println("getDigSpeed(); is effective");
			return this.efficiencyOnProperMaterial;
		}
		System.out.println("getDigSpeed(); not effective");
		return super.getDigSpeed(stack, block, meta);
	}

	/**
	 * Queries the harvest level of this item stack for the specified tool class,
	 * Returns -1 if this tool is not of the specified type
	 * 
	 * @param stack This item stack instance
	 * @param toolClass Tool Class
	 * @return Harvest level, or -1 if not the specified tool type.
	 */
	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		if (stack == null)
		{
			System.out.println("getHarvestLevel(): stack == null");
			return -1;
		}
		if (toolClass.equals(this.getToolClass(stack)) == true)
		{
			System.out.println("getHarvestLevel(): 3");
			return 3; // Diamond tool harvest level for valid blocks
		}
		System.out.println("getHarvestLevel(): -1");
		return -1;
	}

    /**
	 * ItemStack sensitive version of getItemEnchantability
	 * 
	 * @param stack The ItemStack
	 * @return the item echantability value
	 */
	@Override
	public int getItemEnchantability(ItemStack stack)
	{
		return 12;
	}

	/**
	 * ItemStack sensitive version of getItemAttributeModifiers
	 */
	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		System.out.println("getAttributeModifiers()");
		return super.getAttributeModifiers(stack);
	}

    @SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceItem.NAME_ITEM_ENDER_PICKAXE + ".rod");
		this.iconArray = new IIcon[51];
		String prefix = this.getIconString() + ".";

		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 5; j++)
			{
				this.iconArray[(i * 5) + j] = iconRegister.registerIcon(prefix + this.tools[j] + this.parts[i]);
			}
		}

		// Sword has three head textures
		this.iconArray[50] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceItem.NAME_ITEM_ENDER_SWORD + ".head.3");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderPasses(int metadata)
	{
		return 5;
	}

	/**
	 * Return the correct icon for rendering based on the supplied ItemStack and render pass.
	 *
	 * Defers to {@link #getIconFromDamageForRenderPass(int, int)}
	 * @param stack to render for
	 * @param pass the multi-render pass
	 * @return the icon
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass)
	{
		return this.getIcon(stack, renderPass, null, null, 0);
	}

    /**
	 * Player, Render pass, and item usage sensitive version of getIconIndex.
	 *
	 * @param stack The item stack to get the icon for. (Usually this, and usingItem will be the same if usingItem is not null)
	 * @param renderPass The pass to get the icon for, 0 is default.
	 * @param player The player holding the item
	 * @param usingItem The item the player is actively using. Can be null if not using anything.
	 * @param useRemaining The ticks remaining for the active item.
	 * @return The icon index
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
	{
		if (stack == null)
		{
			return this.itemIcon;
		}

		int i = this.getToolType(stack);

		switch(renderPass)
		{
			// 0: Rod
			case 1: // 1: Head
				// TODO: Select the right part based on NBT
				byte mode = this.getToolMode(stack);
				if (mode == 2){ i = 50; } // Ender Sword in the third operation mode
				else if (mode == 1) { i += 10; }
				else { i += 5; }
				break;
			case 2: // 2: Core
				// TODO: Select the right part based on NBT
				//i += 15;
				break;
			case 3: // 3: Capacitor
				// TODO: Select the right part based on NBT
				//i += 30;
				break;
			case 4: // 4: Link Crystal
				//i += 45;
				break;
			default:
		}

		if (i < 0 || i >= this.iconArray.length)
		{
			return this.itemIcon;
		}

		return this.iconArray[i];
	}

	/**
	 * Determines if the durability bar should be rendered for this item.
	 * Defaults to vanilla stack.isDamaged behavior.
	 * But modders can use this for any data they wish.
	 * 
	 * @param stack The current Item Stack
	 * @return True if it should render the 'durability' bar.
	 */
	public boolean showDurabilityBar(ItemStack stack)
	{
		return this.isDamaged(stack);
	}

    /**
	 * Queries the percentage of the 'Durability' bar that should be drawn.
	 * 
	 * @param stack The current ItemStack
	 * @return 1.0 for 100% 0 for 0%
	 */
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		if (stack == null || stack.getTagCompound() == null)
		{
			return 1.0d;
		}
		return ((double)this.getMaxDamage(stack) - (double)stack.getTagCompound().getInteger("Damage")) / (double)this.getMaxDamage(stack);
	}

    @SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		//list.add(StatCollector.translateToLocal("gui.tooltip.durability") + ": " + (this.getMaxDamage(stack) - this.getDamage(stack) + " / " + this.getMaxDamage(stack)));
	}

	private byte getToolMode(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		return nbt.getByte("ToolMode");
	}

	private void toggleToolMode(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		byte mode = this.getToolMode(stack);

		// 4: Ender Sword; it has 3 modes instead of 2 like the other tools
		if (this.getToolType(stack) == 4 && ++mode > 2)
		{
			mode = 0;
		}
		else if (this.getToolType(stack) != 4 && ++mode > 1)
		{
			mode = 0;
		}

		nbt.setByte("ToolMode", mode);
		stack.setTagCompound(nbt);
	}

	@Override
	public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
	{
		if (stack == null)
		{
			return;
		}

		if (key == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
		{
			this.toggleToolMode(stack);
		}
	}
}
