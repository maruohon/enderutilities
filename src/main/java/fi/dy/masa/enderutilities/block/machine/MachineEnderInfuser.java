package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class MachineEnderInfuser extends Machine
{
    public MachineEnderInfuser(EnumMachine machineType, String name, Class<? extends TileEntityEnderUtilities> TEClass, String tool, int harvestLevel, float hardness)
    {
        super(machineType, name, TEClass, tool, harvestLevel, hardness);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerTextures(TextureMap textureMap)
    {
        this.texture_names = new String[3];
        this.texture_names[0] = "enderinfuser.front";
        this.texture_names[1] = "machine.top.0";
        this.texture_names[2] = "machine.side.0";

        super.registerTextures(textureMap);
    }
}
