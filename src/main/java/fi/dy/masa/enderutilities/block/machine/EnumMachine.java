package fi.dy.masa.enderutilities.block.machine;

import net.minecraft.util.IStringSerializable;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import gnu.trove.map.hash.TIntObjectHashMap;

public enum EnumMachine implements IStringSerializable
{
    ENDER_FURNACE       (0, 0, ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE),
    TOOL_WORKSTATION    (0, 1, ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION),
    ENDER_INFUSER       (0, 2, ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);

    private static final TIntObjectHashMap<EnumMachine> MACHINE_LOOKUP = new TIntObjectHashMap<EnumMachine>();
    //private static final Map<EnumMachine, Byte> META_LOOKUP = new HashMap<EnumMachine, Byte>();
    private int blockIndex;
    private int meta;
    private String name;

    EnumMachine(int blockIndex, int meta, String name)
    {
        this.blockIndex = blockIndex;
        this.meta = meta;
        this.name = name;
    }

    public int getMetadata()
    {
        return this.meta;
    }

    public int getBlockIndex()
    {
        return this.blockIndex;
    }

    public static EnumMachine getMachineType(int blockIndex, int meta)
    {
        return MACHINE_LOOKUP.get((blockIndex << 4) | (meta & 0xF));
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    static
    {
        for (EnumMachine machine : values())
        {
            MACHINE_LOOKUP.put((machine.blockIndex << 4) | (machine.meta & 0xF), machine);
            //META_LOOKUP.put(machine, Byte.valueOf((byte)(machine.meta & 0xF)));
        }
    }
}
