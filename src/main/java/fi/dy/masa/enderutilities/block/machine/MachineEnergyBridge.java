package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class MachineEnergyBridge extends Machine
{
    public MachineEnergyBridge(EnumMachine machine, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(machine, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, EntityLivingBase livingBase, ItemStack stack)
    {
        super.onBlockPlacedBy(world, pos, livingBase, stack);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).tryAssembleMultiBlock(world, pos.getX(), pos.getY(), pos.getZ());
            }
        }
    }

    @Override
    public boolean breakBlock(World world, BlockPos pos, IBlockState iBlockState)
    {
        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).disassembleMultiblock(world, pos.getX(), pos.getY(), pos.getZ(), world.getBlockState(pos).getBlock().getMetaFromState(iBlockState));
            }
        }

        return super.breakBlock(world, pos, iBlockState);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer player, EnumFacing face, float offsetX, float offsetY, float offsetZ)
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos, IBlockState iBlockState)
    {
        return 15;
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        // These are for the rendering in ItemBlock form in inventories etc.

        if (side == 0 || side == 1)
        {
            return icons[0]; // top
        }
        if (side == 3)
        {
            return icons[2]; // front
        }

        return icons[6]; // side (left)
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(TileEntityEnderUtilities te, int side, int meta)
    {
        int offsetActive = 0;

        if (te == null)
        {
            return icons[0];
        }

        if (te instanceof TileEntityEnergyBridge && ((TileEntityEnergyBridge)te).isActive == true)
        {
            offsetActive += 1;
        }

        if (side == 0 || side == 1)
        {
            return icons[0 + offsetActive]; // top & bottom
        }

        int rot = te.getRotation();
        if (side == rot)
        {
            return icons[2 + offsetActive]; // front
        }

        if (side == ForgeDirection.getOrientation(rot).getOpposite().ordinal())
        {
            return icons[4 + offsetActive]; // back
        }

        return icons[6 + offsetActive]; // sides
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void registerIcons(IIconRegister iconRegister)
    {
        this.icons = new IIcon[8];

        // It might be better to just use explicitly separate textures for all sides, so that texture pack artists could change them all...
        if ("energybridge.transmitter".equals(this.blockName))
        {
            this.icons[0] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.off"); // Bottom & Top off
            this.icons[1] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.active"); // Bottom & Top active
            this.icons[2] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.off"); // Front off
            this.icons[3] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.active"); // Front active
            this.icons[4] = this.icons[2]; // Back off
            this.icons[5] = this.icons[3]; // Back active
            this.icons[6] = this.icons[2]; // "Left" & "Right" off
            this.icons[7] = this.icons[3]; // "Left" & "Right" active
        }
        else if ("energybridge.receiver".equals(this.blockName))
        {
            this.icons[0] = iconRegister.registerIcon(ReferenceTextures.getTileName("energybridge.transmitter.top.off")); // Bottom & Top off
            this.icons[1] = iconRegister.registerIcon(ReferenceTextures.getTileName("energybridge.transmitter.top.active")); // Bottom & Top active
            this.icons[2] = this.icons[0]; // Front off
            this.icons[3] = this.icons[1]; // Front active
            this.icons[4] = this.icons[0]; // Back off
            this.icons[5] = this.icons[1]; // Back active
            this.icons[6] = this.icons[0]; // "Left" & "Right" off
            this.icons[7] = this.icons[1]; // "Left" & "Right" active
        }
        else if ("energybridge.resonator".equals(this.blockName))
        {
            this.icons[0] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.off"); // Bottom & Top off
            this.icons[1] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.active"); // Bottom & Top active
            this.icons[2] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.off"); // Front off
            this.icons[3] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.active"); // Front active
            this.icons[4] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".back"); // Back off
            this.icons[5] = this.icons[4]; // Back active
            this.icons[6] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.off"); // "Left" & "Right" off
            this.icons[7] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.active"); // "Left" & "Right" active
        }
    }*/
}
