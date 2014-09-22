package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemEU;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;
import fi.dy.masa.enderutilities.reference.key.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class ItemEnderTool extends ItemEU implements IKeyBound
{
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
		super();
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDERTOOL);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (stack.getItemDamage() == 0) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_PICKAXE; }
		if (stack.getItemDamage() == 1) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_AXE; }
		if (stack.getItemDamage() == 2) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_SHOVEL; }
		if (stack.getItemDamage() == 3) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_HOE; }
		if (stack.getItemDamage() == 4) { return super.getUnlocalizedName() + "." + ReferenceItem.NAME_ITEM_ENDER_SWORD; }

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

		int i = stack.getItemDamage();

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

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		//list.add(StatCollector.translateToLocal("gui.tooltip.craftingingredient"));
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
		if (stack.getItemDamage() == 4 && ++mode > 2)
		{
			mode = 0;
		}
		else if (stack.getItemDamage() != 4 && ++mode > 1)
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
