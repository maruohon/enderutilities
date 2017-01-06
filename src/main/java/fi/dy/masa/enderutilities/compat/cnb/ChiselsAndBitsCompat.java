package fi.dy.masa.enderutilities.compat.cnb;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities;
import fi.dy.masa.enderutilities.util.TemplateEnderUtilities.ChiselsAndBitsHandler;
import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiseledBlockTileEntity;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;

@ChiselsAndBitsAddon
public class ChiselsAndBitsCompat extends ChiselsAndBitsHandler implements IChiselsAndBitsAddon
{
    public ChiselsAndBitsCompat()
    {
    }

    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI api)
    {
        TemplateEnderUtilities.chiselsAndBitsHandler = this;
        EnderUtilities.logger.info("Chisels and Bits compat loaded");
    }

    @Override
    public boolean isChiselsAndBitsTile(TileEntity te)
    {
        return te instanceof IChiseledBlockTileEntity;
    }

    @Override
    public NBTTagCompound writeChiselsAndBitsTileToNBT(NBTTagCompound tag, TileEntity te)
    {
        if (this.isChiselsAndBitsTile(te))
        {
            return ((IChiseledBlockTileEntity) te).writeTileEntityToTag(tag, true);
        }

        return super.writeChiselsAndBitsTileToNBT(tag, te);
    }
}
