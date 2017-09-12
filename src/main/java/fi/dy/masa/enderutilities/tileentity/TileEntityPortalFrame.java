package fi.dy.masa.enderutilities.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntityPortalFrame extends TileEntityEnderUtilities
{
    public TileEntityPortalFrame()
    {
        super(ReferenceNames.NAME_TILE_FRAME);
    }

    @Override
    protected NBTTagCompound writeToNBTCustom(NBTTagCompound nbt)
    {
        // Override without calling super, because the Frame
        // doesn't use/need rotation, owner data etc.
        if (this.camoState != null)
        {
            nbt.setInteger("Camo", Block.getStateId(this.camoState));

            if (this.camoData != null)
            {
                nbt.setTag("CamoData", this.camoData);
            }
        }

        return nbt;
    }

    @Override
    protected boolean hasCamouflageAbility()
    {
        return true;
    }

    @Override
    public boolean hasGui()
    {
        return false;
    }
}
