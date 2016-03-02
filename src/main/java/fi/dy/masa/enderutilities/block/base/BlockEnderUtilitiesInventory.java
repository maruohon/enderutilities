package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class BlockEnderUtilitiesInventory extends BlockEnderUtilitiesTileEntity
{
    public BlockEnderUtilitiesInventory(String name, float hardness, int harvestLevel, Material material)
    {
        super(name, hardness, harvestLevel, material);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
        {
            TileEntityEnderUtilitiesInventory teeui = (TileEntityEnderUtilitiesInventory)te;

            for (int i = 0; i < teeui.getSizeInventory(); ++i)
            {
                EntityUtils.dropItemStacksInWorld(world, pos, teeui.getStackInSlot(i), -1, false);
            }
        }

        world.removeTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        PlayerInteractEvent e = new PlayerInteractEvent(playerIn, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, pos, side, worldIn, new Vec3(hitX, hitY, hitZ));
        if (MinecraftForge.EVENT_BUS.post(e) || e.getResult() == Result.DENY || e.useBlock == Result.DENY)
        {
            return false;
        }

        if (worldIn.isRemote == false)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityEnderUtilities == false)
            {
                return false;
            }

            if (this.isTileEntityValid(te) == true)
            {
                playerIn.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }

        return true;
    }

    @Override
    public boolean hasComparatorInputOverride()
    {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World worldIn, BlockPos pos)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if ((te instanceof IInventory) == false)
        {
            return 0;
        }

        // FIXME this won't work for custom stack sizes
        return Container.calcRedstoneFromInventory((IInventory)te);
    }
}
