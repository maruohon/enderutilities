package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class MachineEnergyBridge extends Machine
{
    public MachineEnergyBridge(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(index, meta, name, TEClass, tool, harvestLevel, hardness);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase livingBase, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, livingBase, stack);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).tryAssembleMultiBlock(world, x, y, z);
            }
        }
    }

    @Override
    public void onBlockPreDestroy(World world, int x, int y, int z, int oldMeta)
    {
        super.onBlockPreDestroy(world, x, y, z, oldMeta);

        if (world.isRemote == false)
        {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityEnergyBridge)
            {
                ((TileEntityEnergyBridge)te).disassembleMultiblock(world, x, y, z, oldMeta);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
    {
        return false;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z, Block block, int meta)
    {
        return 12;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        // These are for the rendering in ItemBlock form in inventories etc.

        int offsetMeta = 0;
        if (side == 0 || side == 1)
        {
            return this.icons[0 + offsetMeta]; // top
        }
        if (side == 3)
        {
            return this.icons[2 + offsetMeta]; // front
        }

        return this.icons[5 + offsetMeta]; // side (left)
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(TileEntityEnderUtilities te, int side, int meta)
    {
        int offsetMeta = meta * 8;
        int offsetActive = 0;

        if (te == null)
        {
            return this.icons[0 + offsetMeta];
        }

        if (te instanceof TileEntityEnergyBridge && ((TileEntityEnergyBridge)te).isActive == true)
        {
            offsetActive += 1;
        }

        if (side == 0 || side == 1)
        {
            return this.icons[0 + offsetMeta + offsetActive]; // top & bottom
        }

        int rot = te.getRotation();
        if (side == rot)
        {
            return this.icons[2 + offsetMeta + offsetActive]; // front
        }

        if (side == ForgeDirection.getOrientation(rot).getOpposite().ordinal())
        {
            return this.icons[4 + offsetMeta]; // Back side always has the same texture
        }

        return this.icons[6 + offsetMeta + offsetActive]; // sides
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void registerIcons(IIconRegister iconRegister)
    {
        this.icons = new IIcon[24];
        // Transmitter
        this.icons[0] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.off"); // Bottom & Top
        this.icons[1] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.active"); // Bottom & Top
        this.icons[2] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.off");
        this.icons[3] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.active");
        this.icons[4] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".back");
        this.icons[5] = this.icons[4];
        this.icons[6] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.off"); // "Left" & "Right"
        this.icons[7] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.active"); // "Left" & "Right"

        // Receiver
        this.icons[8] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.off"); // Bottom & Top
        this.icons[9] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.active"); // Bottom & Top
        this.icons[10] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.off");
        this.icons[11] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.active");
        this.icons[12] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".back");
        this.icons[13] = this.icons[4];
        this.icons[14] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.off"); // "Left" & "Right"
        this.icons[15] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.active"); // "Left" & "Right"

        // Resonator
        this.icons[16] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.off"); // Bottom & Top
        this.icons[17] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top.active"); // Bottom & Top
        this.icons[18] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.off");
        this.icons[19] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front.active");
        this.icons[20] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".back");
        this.icons[21] = this.icons[4];
        this.icons[22] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.off"); // "Left" & "Right"
        this.icons[23] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".side.active"); // "Left" & "Right"
    }
}
