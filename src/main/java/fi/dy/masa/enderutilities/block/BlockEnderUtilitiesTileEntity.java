package fi.dy.masa.enderutilities.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.machine.Machine;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

public class BlockEnderUtilitiesTileEntity extends BlockEnderUtilities implements ITileEntityProvider
{
    public static final byte YAW_TO_DIRECTION[] = {2, 5, 3, 4};
    public int blockIndex;

    public BlockEnderUtilitiesTileEntity(int index, String name, float hardness)
    {
        this(index, name, hardness, Material.rock);
    }

    public BlockEnderUtilitiesTileEntity(int index, String name, float hardness, Material material)
    {
        super(index, name, hardness, material);
        this.blockIndex = index;
        Machine.setBlockHardness(this, this.blockIndex);
        Machine.setBlockHarvestLevels(this, this.blockIndex);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            return machine.createNewTileEntity();
        }

        return null;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te == null || (te instanceof TileEntityEnderUtilities) == false)
        {
            return;
        }

        int yaw = MathHelper.floor_double((double)(livingBase.rotationYaw * 4.0f / 360.0f) + 0.5d) & 3;

        TileEntityEnderUtilities teeu = (TileEntityEnderUtilities)te;

        NBTTagCompound nbt = stack.getTagCompound();

        // If the ItemStack has a tag containing saved TE data, restore it to the just placed block/TE
        if (nbt != null && nbt.hasKey("TileEntityData", Constants.NBT.TAG_COMPOUND) == true)
        {
            teeu.readFromNBTCustom(nbt.getCompoundTag("TileEntityData"));
        }
        else
        {
            /*
            if (livingBase.rotationPitch > 45.0f)
            {
                rot = (rot << 4) | 1;
            }
            else if (livingBase.rotationPitch < -45.0f)
            {
                rot = rot << 4;
            }
            else
            {
            */
                // {DOWN, UP, NORTH, SOUTH, WEST, EAST}
                /*switch (yaw)
                {
                    case 0: yaw = 2; break;
                    case 1: yaw = 5; break;
                    case 2: yaw = 3; break;
                    case 3: yaw = 4; break;
                    default:
                }*/
            //}

            if (livingBase instanceof EntityPlayer)
            {
                teeu.setOwner((EntityPlayer)livingBase);
            }

            if (teeu instanceof TileEntityEnderUtilitiesInventory && stack.hasDisplayName())
            {
                ((TileEntityEnderUtilitiesInventory)teeu).setInventoryName(stack.getDisplayName());
            }
        }

        // Update the rotation
        if (yaw < YAW_TO_DIRECTION.length)
        {
            teeu.setRotation(YAW_TO_DIRECTION[yaw]);
        }

        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z));
        if (machine != null)
        {
            machine.onBlockPlacedBy(world, x, y, z, livingBase, stack);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z)
    {
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z));
        if (machine != null)
        {
            machine.onBlockAdded(world, x, y, z);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
    {
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z));
        if (machine != null)
        {
            return machine.onBlockActivated(world, x, y, z, player, side, offsetX, offsetY, offsetZ);
        }

        return false;
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
    {
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z));
        if (machine != null)
        {
            machine.onBlockPreDestroy(world, x, y, z, oldMeta);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta)
    {
        // This is for handling custom storage stuff like buffers, which are not regular
        // ItemStacks and thus not handled by the breakBlock() in BlockEnderUtilitiesInventory
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            machine.breakBlock(world, x, y, z, block, meta);
        }

        super.breakBlock(world, x, y, z, block, meta);   // world.removeTileEntity(x, y, z);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z)
    {
        Block block = world.getBlock(x, y, z);
        if (block != this)
        {
            return block.getLightValue(world, x, y, z);
        }

        int meta = world.getBlockMetadata(x, y, z);
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            return machine.getLightValue(world, x, y, z, block, meta);
        }

        return super.getLightValue(world, x, y, z);
    }

    public int getBlockIndex()
    {
        return this.blockIndex;
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        Machine.getSubBlocks(this.blockIndex, this, item, tab, list);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random rand)
    {
        Machine machine = Machine.getMachine(this.blockIndex, world.getBlockMetadata(x, y, z));
        if (machine != null)
        {
            machine.randomDisplayTick(world, x, y, z, rand);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta)
    {
        Machine machine = Machine.getMachine(this.blockIndex, meta);
        if (machine != null)
        {
            return machine.getIcon(side, meta);
        }

        return this.blockIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side)
    {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        TileEntity te = blockAccess.getTileEntity(x, y, z);
        if (te != null && te instanceof TileEntityEnderUtilities)
        {
            Machine machine = Machine.getMachine(this.blockIndex, meta);
            if (machine != null)
            {
                return machine.getIcon((TileEntityEnderUtilities)te, side, meta);
            }
        }

        return this.getIcon(side, meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        Machine.registerIcons(this.blockIndex, iconRegister);
    }
}
