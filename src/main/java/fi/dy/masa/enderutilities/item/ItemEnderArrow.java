package fi.dy.masa.enderutilities.item;

import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class ItemEnderArrow extends ItemEnderUtilities
{
	public ItemEnderArrow()
	{
		super();
		this.setMaxStackSize(64);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_ARROW);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}
}
