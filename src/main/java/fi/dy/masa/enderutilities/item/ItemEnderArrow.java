package fi.dy.masa.enderutilities.item;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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

    @SideOnly(Side.CLIENT)
    @Override
    public String getBaseModelName(String variant)
    {
        return ReferenceNames.NAME_ITEM_ENDERTOOL;
    }
}
