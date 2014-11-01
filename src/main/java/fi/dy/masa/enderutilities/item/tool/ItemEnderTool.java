package fi.dy.masa.enderutilities.item.tool;

import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceMaterial;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderTool extends ItemTool implements IKeyBound, IModular
{
	public float efficiencyOnProperMaterial;
	public float damageVsEntity;
	private final Item.ToolMaterial material;

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	@SideOnly(Side.CLIENT)
	// Note: Sword "has to be" last, because it has one extra texture compared to the others
	String[] tools = new String[] {	ReferenceBlocksItems.NAME_ITEM_ENDER_PICKAXE,
									ReferenceBlocksItems.NAME_ITEM_ENDER_AXE,
									ReferenceBlocksItems.NAME_ITEM_ENDER_SHOVEL,
									ReferenceBlocksItems.NAME_ITEM_ENDER_HOE};
	@SideOnly(Side.CLIENT)
	String[] parts = new String[] {"rod", "head.1", "head.2", "core.1", "core.2", "core.3",
									"capacitor.1", "capacitor.2", "capacitor.3", "linkcrystal"};

	public ItemEnderTool()
	{
		// The Set is not actually used for anything!
		super(2.0f, ReferenceMaterial.Tool.ENDER_ALLOY_ADVANCED, Sets.newHashSet(new Block[]{Blocks.torch}));
		this.material = ReferenceMaterial.Tool.ENDER_ALLOY_ADVANCED;
		this.setMaxStackSize(1);
		this.setMaxDamage(this.material.getMaxUses());
		this.setNoRepair();
		this.efficiencyOnProperMaterial = this.material.getEfficiencyOnProperMaterial();
		this.damageVsEntity = 2.0f + this.material.getDamageVsEntity();
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
		this.setUnlocalizedName(ReferenceBlocksItems.NAME_ITEM_ENDERTOOL);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		int toolType = this.getToolType(stack);
		if (toolType == 0) { return super.getUnlocalizedName() + "." + ReferenceBlocksItems.NAME_ITEM_ENDER_PICKAXE; }
		if (toolType == 1) { return super.getUnlocalizedName() + "." + ReferenceBlocksItems.NAME_ITEM_ENDER_AXE; }
		if (toolType == 2) { return super.getUnlocalizedName() + "." + ReferenceBlocksItems.NAME_ITEM_ENDER_SHOVEL; }
		if (toolType == 3) { return super.getUnlocalizedName() + "." + ReferenceBlocksItems.NAME_ITEM_ENDER_HOE; }

		return super.getUnlocalizedName();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List list)
	{
		ItemStack stack;
		if (EUConfigs.disableItemEnderTool.getBoolean(false) == false)
		{
			for (int i = 0; i <= 3; i++)
			{
				stack = new ItemStack(this, 1, 0);
				this.setToolType(stack, i);
				list.add(stack);
			}
		}
	}

	@Override
	public boolean isItemTool(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean isRepairable()
	{
		return false;
	}

	public int getToolType(ItemStack stack)
	{
		if (stack == null) { return 0; }

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null)
		{
			return nbt.getByte("ToolType");
		}

		return 0;
	}

	public boolean setToolType(ItemStack stack, int type)
	{
		if (stack == null) { return false; }

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		nbt.setByte("ToolType", (byte)type);
		stack.setTagCompound(nbt);

		return true;
	}

	public String getToolClass(ItemStack stack)
	{
		if (stack != null)
		{
			if (this.getToolType(stack) == 0) { return "pickaxe"; }
			if (this.getToolType(stack) == 1) { return "axe"; }
			if (this.getToolType(stack) == 2) { return "shovel"; }
			if (this.getToolType(stack) == 3) { return "hoe"; }
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
		return this.func_150913_i().getMaxUses();
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
	{
		if (stack == null)
		{
			return false;
		}

		stack.damageItem(2, living1);

		return false;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase living)
	{
		//System.out.println("onBlockDestroyed()");
		stack.damageItem(1, living);

		return false;
	}

	@Override
	public boolean func_150897_b(Block block)
	{
		return false;
	}

	@Override
	public float func_150893_a(ItemStack stack, Block block)
	{
		if (this.canHarvestBlock(block, stack) == true)
		{
			return this.efficiencyOnProperMaterial;
		}

		return 1.0f;
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
		if (this.getToolType(stack) == 0) // Ender Pickaxe
		{
			if (block.getMaterial() == net.minecraft.block.material.Material.rock ||
				block.getMaterial() == net.minecraft.block.material.Material.glass ||
				block.getMaterial() == net.minecraft.block.material.Material.ice ||
				block.getMaterial() == net.minecraft.block.material.Material.packedIce ||
				block.getMaterial() == net.minecraft.block.material.Material.iron ||
				block.getMaterial() == net.minecraft.block.material.Material.anvil)
			{
				//System.out.println("canHarvestBlock(): true; Pickaxe");
				return true;
			}
		}
		else if (this.getToolType(stack) == 1) // Ender Axe
		{
			if (block.getMaterial() == net.minecraft.block.material.Material.wood ||
				block.getMaterial() == net.minecraft.block.material.Material.plants ||
				block.getMaterial() == net.minecraft.block.material.Material.vine)
			{
				//System.out.println("canHarvestBlock(): true; Axe");
				return true;
			}
		}
		else if (this.getToolType(stack) == 2) // Ender Shovel
		{
			if (block.getMaterial() == net.minecraft.block.material.Material.ground ||
				block.getMaterial() == net.minecraft.block.material.Material.grass ||
				block.getMaterial() == net.minecraft.block.material.Material.sand ||
				block.getMaterial() == net.minecraft.block.material.Material.snow ||
				block.getMaterial() == net.minecraft.block.material.Material.craftedSnow ||
				block.getMaterial() == net.minecraft.block.material.Material.clay)
			{
				//System.out.println("canHarvestBlock(): true; Shovel");
				return true;
			}
		}

		//System.out.println("canHarvestBlock(): false");
		//return func_150897_b(block);
		return false;
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
		// TODO Add a mode and NBT tag for "fast mode", which uses double durability but allows instant mining @ Efficiency V
		// 34 is the minimum to allow instant mining with just Efficiency V (= no beacon/haste) on cobble
		// 1474 on obsidian. So maybe around 160 might be ok? I don't want insta-mining on obsidian, but all other types of "rock".
		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) >= 5)
		{
			this.efficiencyOnProperMaterial = 160.0f;
		}
		else
		{
			this.efficiencyOnProperMaterial = 5.0f;
		}

		if (ForgeHooks.isToolEffective(stack, block, meta))
		{
			//System.out.println("getDigSpeed(); isToolEffective() true: " + this.efficiencyOnProperMaterial);
			return this.efficiencyOnProperMaterial;
		}
		if (this.canHarvestBlock(block, stack))
		{
			//System.out.println("getDigSpeed(); canHarvestBlock() true: " + this.efficiencyOnProperMaterial);
			return this.efficiencyOnProperMaterial;
		}
		//System.out.println("getDigSpeed(); not effective: " + super.getDigSpeed(stack, block, meta));
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
			return -1;
		}
		if (toolClass.equals(this.getToolClass(stack)) == true)
		{
			return this.func_150913_i().getHarvestLevel();
		}

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
		return super.getItemEnchantability(stack);
	}

	/**
	 * ItemStack sensitive version of getItemAttributeModifiers
	 */
	@Override
	public Multimap getAttributeModifiers(ItemStack stack)
	{
		Multimap<String, AttributeModifier> multimap = HashMultimap.create();
		double dmg = (double)this.damageVsEntity;

		int toolType = this.getToolType(stack);
		if (toolType == 0) { dmg += 2.0d; }	// Pickaxe
		else if (toolType == 1) { dmg += 3.0d; }	// Axe
		else if (toolType == 2) { dmg += 1.0d; }	// Shovel

		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Tool modifier", dmg, 0));
		return multimap;
	}

    @SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceBlocksItems.NAME_ITEM_ENDER_PICKAXE + ".rod");
		this.iconArray = new IIcon[40];
		String prefix = this.getIconString() + ".";

		for (int i = 0; i < 10; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				this.iconArray[(i * 4) + j] = iconRegister.registerIcon(prefix + this.tools[j] + "." + this.parts[i]);
			}
		}
	}

	/**
	 * Render Pass sensitive version of hasEffect()
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack, int pass)
	{
		return false;
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
				i += (getToolMode(stack) << 2) + 4;
				break;
			case 2: // 2: Core
				// TODO: Select the right part based on NBT
				break;
			case 3: // 3: Capacitor
				// TODO: Select the right part based on NBT
				break;
			case 4: // 4: Link Crystal
				break;
			default:
		}

		if (i < 0 || i >= this.iconArray.length)
		{
			return this.itemIcon;
		}

		return this.iconArray[i];
	}

    @SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		//list.add(StatCollector.translateToLocal("gui.tooltip.durability") + ": " + (this.getMaxDamage(stack) - this.getDamage(stack) + " / " + this.getMaxDamage(stack)));
	}

	public static byte getToolMode(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) { nbt = new NBTTagCompound(); }

		return nbt.getByte("ToolMode");
	}

	public void toggleToolMode(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) { nbt = new NBTTagCompound(); }

		byte mode = getToolMode(stack);
		// Two modes: Normal (= insert to player inventory) and Send (= send to bound inventory)
		if (++mode > 1) { mode = 0; }

		nbt.setByte("ToolMode", mode);
		stack.setTagCompound(nbt);
	}

	@Override
	public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
	{
		if (stack == null) { return; }

		if (key == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
		{
			this.toggleToolMode(stack);
		}
	}

	/* Return whether the given module type has been installed. */
	@Override
	public boolean hasModule(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.hasModule(stack, moduleType);
	}

	/* Returns the number of installed modules of the given type. */
	@Override
	public int getModuleCount(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.getModuleCount(stack, moduleType);
	}

	/* Returns the maximum number of modules that can be installed on this item. */
	@Override
	public int getMaxModules(ItemStack stack)
	{
		return 3;
	}

	/* Returns the maximum number of modules of the given type that can be installed on this item. */
	@Override
	public int getMaxModules(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		if (moduleType.equals(UtilItemModular.ModuleType.TYPE_ENDERCORE_ACTIVE))
		{
			return 1;
		}

		if (moduleType.equals(UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR))
		{
			return 1;
		}

		if (moduleType.equals(UtilItemModular.ModuleType.TYPE_LINKCRYSTAL))
		{
			return 1;
		}

		return 0;
	}

	/* Returns a bitmask of the installed module types. Used for quicker checking of what is installed. */
	@Override
	public int getInstalledModulesMask(ItemStack stack)
	{
		return UtilItemModular.getInstalledModulesMask(stack);
	}

	/* Returns the (max, if multiple) tier of the installed module. */
	@Override
	public int getModuleTier(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.getModuleTier(stack, moduleType);
	}

	/* Returns the ItemStack of the (selected, if multiple) given module type. */
	@Override
	public ItemStack getSelectedModuleStack(ItemStack stack, UtilItemModular.ModuleType moduleType)
	{
		return UtilItemModular.getSelectedModuleStack(stack, moduleType);
	}

	/* Returns a list of all the installed modules. */
	@Override
	public List<NBTTagCompound> getAllModules(ItemStack stack)
	{
		return UtilItemModular.getAllModules(stack);
	}

	/* Sets the modules to the ones provided in the list. */
	@Override
	public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
	{
		return UtilItemModular.setAllModules(stack, modules);
	}

	/* Sets the module indicated by the position to the one provided in the compound tag. */
	@Override
	public ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
	{
		return UtilItemModular.setModule(stack, index, nbt);
	}
}
