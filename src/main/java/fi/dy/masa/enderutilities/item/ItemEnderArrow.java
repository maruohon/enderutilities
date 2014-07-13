package fi.dy.masa.enderutilities.item;

import net.minecraft.item.Item;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemEnderArrow extends Item
{
	public ItemEnderArrow()
	{
		super();
		this.setMaxStackSize(64);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_ARROW);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
		this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
	}
}
