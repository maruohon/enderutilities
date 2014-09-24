package fi.dy.masa.enderutilities.item.tool;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.google.common.collect.Multimap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class ItemEnderSword extends ItemSword implements IKeyBound
{
	private float damageVsEntity;
	private final Item.ToolMaterial material;

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	@SideOnly(Side.CLIENT)
	String[] parts = new String[] {"rod", "head.1", "head.2", "head.3", "core.1", "core.2", "core.3",
									"capacitor.1", "capacitor.2", "capacitor.3", "linkcrystal"};

	public ItemEnderSword(Item.ToolMaterial material)
	{
		super(material);
		this.setMaxStackSize(1);
		this.setMaxDamage(material.getMaxUses());
		this.setNoRepair();
		this.material = material;
		this.damageVsEntity = 4.0f + material.getDamageVsEntity();
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_SWORD);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}

	// This is used for determining which weapon is better when mobs pick up items
	public float func_150931_i()
	{
		// TODO no way to check if the item is broken without ItemStack and NBT data
		return this.material.getDamageVsEntity();
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
		return this.material.getMaxUses();
	}

	public float func_150893_a(ItemStack stack, Block block)
	{
		if (block == Blocks.web)
		{
			return 15.0f;
		}

		Material material = block.getMaterial();
		if (material == Material.plants ||
			material == Material.vine ||
			material == Material.coral ||
			material == Material.leaves ||
			material == Material.gourd)
		{
			return 1.5f;
		}

		return 1.0f;
	}

	/**
	 * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
	 * the damage on the stack.
	 */
	public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
	{
		stack.damageItem(1, living2);
		return true;
	}

	public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase livingbase)
	{
		if (block.getBlockHardness(world, x, y, z) != 0.0f)
		{
			stack.damageItem(2, livingbase);
		}

		return true;
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

	/**
	 * Returns True is the item is renderer in full 3D when hold.
	 */
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	/**
	 * returns the action that specifies what animation to play when the items is being used
	 */
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.block;
	}

	/**
	 * How long it takes to use or consume an item
	 */
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	/**
	 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
	 */
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	public boolean func_150897_b(Block block)
	{
		return block == Blocks.web;
	}

	/**
	 * Return the enchantability factor of the item, most of the time is based on material.
	 */
	public int getItemEnchantability()
	{
		return this.material.getEnchantability();
	}

	/**
	 * Return the name for this tool's material.
	 */
	public String getToolMaterialName()
	{
		return this.material.toString();
	}

	/**
	 * Return whether this item is repairable in an anvil.
	 */
	public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
	{
		return false;
	}

	/**
	 * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
	 */
	public Multimap getItemAttributeModifiers()
	{
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers();
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", (double)this.damageVsEntity, 0));
		return multimap;
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

	public void toggleToolMode(ItemStack stack)
	{
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
		{
			nbt = new NBTTagCompound();
		}

		byte mode = this.getToolMode(stack);

		// Ender Sword has 3 modes: Normal (= insert to player inventory), Send (= send to bound inventory) and Summon
		if (++mode > 2)
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

    @SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		//list.add(StatCollector.translateToLocal("gui.tooltip.durability") + ": " + (this.getMaxDamage(stack) - this.getDamage(stack) + " / " + this.getMaxDamage(stack)));
	}

    @SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".rod");
		this.iconArray = new IIcon[11];
		String prefix = this.getIconString() + ".";

		for (int i = 0; i < 11; i++)
		{
			this.iconArray[i] = iconRegister.registerIcon(prefix + this.parts[i]);
		}
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

		int i = 0;

		switch(renderPass)
		{
			// 0: Rod
			case 1: // 1: Head
				i += getToolMode(stack) + 1;
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
}
