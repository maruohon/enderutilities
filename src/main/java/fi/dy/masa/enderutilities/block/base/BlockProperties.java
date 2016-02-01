package fi.dy.masa.enderutilities.block.base;

import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;

public class BlockProperties
{
    public static final PropertyDirection FACING   = PropertyDirection.create("facing");
    public static final PropertyDirection FACING_H = PropertyDirection.create("facing_h", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyDirection FACING_V = PropertyDirection.create("facing_v", EnumFacing.Plane.VERTICAL);
}
