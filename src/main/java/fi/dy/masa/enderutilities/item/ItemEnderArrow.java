package fi.dy.masa.enderutilities.item;

import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemEnderArrow extends ItemEnderUtilities
{
    public ItemEnderArrow()
    {
        super();
        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_ARROW);
    }
}
