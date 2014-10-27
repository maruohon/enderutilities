package fi.dy.masa.enderutilities.block.machine;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.reference.ReferenceTileEntity;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import gnu.trove.map.hash.TIntObjectHashMap;

/*
 * Machine specific data, such as icons.
 * The basic structure of this class has been inspired by MineFactoryReloaded, credits powercrystals, skyboy + others.
 */
public class Machine
{
	public static Machine EnderFurnace = new MachineEnderFurnace(0, 0, ReferenceTileEntity.NAME_TILE_ENDER_FURNACE, TileEntityEnderFurnace.class);
	public static Machine ToolWorkstation = new Machine(0, 1, ReferenceTileEntity.NAME_TILE_TOOL_WORKSTATION, TileEntityToolWorkstation.class);

	protected int blockIndex;
	protected int blockMeta;
	protected int machineIndex;
	protected String blockName;
	protected Class<? extends TileEntityEnderUtilities> tileEntityClass;
	protected IIcon[] icons;
	protected static TIntObjectHashMap<Machine> machines;

	public Machine(int index, int meta, String name, Class<? extends TileEntityEnderUtilities> TEClass)
	{
		this.blockIndex = index;
		this.blockMeta = meta;
		this.machineIndex = (index << 4) | (meta & 0x0F);
		this.blockName = name;
		this.tileEntityClass = TEClass;
		machines.put(this.machineIndex, this);
	}

	public static Machine getMachine(int index, int meta)
	{
		return machines.get((index << 4) | (meta & 0x0F));
	}

	public TileEntityEnderUtilities createNewTileEntity()
	{
		try
		{
			TileEntityEnderUtilities te = this.tileEntityClass.newInstance();
			return te;
		}
		catch (IllegalAccessException e)
		{
			EnderUtilities.logger.fatal("Unable to create instance of TileEntity from %s (IllegalAccessException)", this.tileEntityClass.getName());
			return null;
		}
		catch (InstantiationException e)
		{
			EnderUtilities.logger.fatal("Unable to create instance of TileEntity from %s (InstantiationException)", this.tileEntityClass.getName());
			return null;
		}
	}

	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand)
	{
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side)
    {
		// These are for the rendering in ItemBlock form in inventories etc.

		if (side == 0 || side == 1)
		{
			return this.icons[1]; // top
		}
		if (side == 3)
		{
			return this.icons[2]; // front
		}

		return this.icons[4]; // side (left)
    }

	@SideOnly(Side.CLIENT)
    public IIcon getIcon(TileEntityEnderUtilities te, int side)
    {
    	// FIXME we should get the proper side (left, right, back) textures based on the TE rotation and the side argument
		if (side == 0 || side == 1)
		{
			return this.icons[side];
		}

		if (te != null && side == te.getRotation())
		{
			return this.icons[2]; // front
		}

		return this.icons[2];
    }

	@SideOnly(Side.CLIENT)
	protected void registerIcons(IIconRegister iconRegister)
	{
		this.icons = new IIcon[6];
		this.icons[0] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".bottom");
		this.icons[1] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".top");
		this.icons[2] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".front");
		this.icons[3] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".back");
		this.icons[4] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".left");
		this.icons[5] = iconRegister.registerIcon(ReferenceTextures.getTileName(this.blockName) + ".right");
	}

	@SideOnly(Side.CLIENT)
	public static void registerIcons(int blockIndex, IIconRegister iconRegister)
	{
		for (int i = 0; i < 16; ++i)
		{
			if (machines.containsKey(blockIndex << 4 | i) == true)
			{
				machines.get(blockIndex << 4 | i).registerIcons(iconRegister);
			}
		}
	}
}
