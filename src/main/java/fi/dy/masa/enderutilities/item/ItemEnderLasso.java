package fi.dy.masa.enderutilities.item;

import fi.dy.masa.enderutilities.item.base.ItemLocationBound;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class ItemEnderLasso extends ItemLocationBound
{
	public ItemEnderLasso()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_LASSO);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}
}
