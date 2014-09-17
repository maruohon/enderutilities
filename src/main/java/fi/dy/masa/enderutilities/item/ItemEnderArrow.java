package fi.dy.masa.enderutilities.item;

import fi.dy.masa.enderutilities.item.base.ItemEU;
import fi.dy.masa.enderutilities.reference.Textures;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class ItemEnderArrow extends ItemEU
{
	public ItemEnderArrow()
	{
		super();
		this.setMaxStackSize(64);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_ARROW);
		this.setTextureName(Textures.getTextureName(this.getUnlocalizedName()));
	}
}
