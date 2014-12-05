package fi.dy.masa.enderutilities.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class EnderUtilitiesGUIHandler implements IGuiHandler
{

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (player == null || world == null)
        {
            return null;
        }

        if (ID == 0)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null )
            {
                if (te instanceof TileEntityEnderUtilitiesInventory)
                {
                    return ((TileEntityEnderUtilitiesInventory)te).getContainer(player.inventory);
                }
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (player == null || world == null)
        {
            return null;
        }

        if (ID == 0)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te != null )
            {
                if (te instanceof TileEntityEnderUtilitiesInventory)
                {
                    return ((TileEntityEnderUtilitiesInventory)te).getGui(player.inventory);
                }
            }
        }
        return null;
    }

}
