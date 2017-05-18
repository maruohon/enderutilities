package fi.dy.masa.enderutilities.block.base.property;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PropertyBlockState implements IUnlistedProperty<IBlockState>
{
    private final String name;
    private final Predicate<IBlockState> validator;

    public PropertyBlockState(String name)
    {
        this(name, Predicates.<IBlockState>alwaysTrue());
    }

    public PropertyBlockState(String name, Predicate<IBlockState> validator)
    {
        this.name = name;
        this.validator = validator;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public boolean isValid(IBlockState value)
    {
        return validator.apply(value);
    }

    @Override
    public Class<IBlockState> getType()
    {
        return IBlockState.class;
    }

    @Override
    public String valueToString(IBlockState value)
    {
        return value.toString();
    }
}
