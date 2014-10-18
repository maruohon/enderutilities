package fi.dy.masa.enderutilities.item;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.reference.ReferenceItem;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;

public class ItemEnderLasso extends ItemLocationBoundModular
{
	public ItemEnderLasso()
	{
		super();
		this.setMaxStackSize(1);
		this.setUnlocalizedName(ReferenceItem.NAME_ITEM_ENDER_LASSO);
		this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
	}

	/* Returns the maximum number of modules that can be installed on this item. */
	@Override
	public int getMaxModules(ItemStack stack)
	{
		return 4;
	}
}
