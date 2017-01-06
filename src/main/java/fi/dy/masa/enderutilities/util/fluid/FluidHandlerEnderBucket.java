package fi.dy.masa.enderutilities.util.fluid;

import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import fi.dy.masa.enderutilities.item.ItemEnderBucket;

public class FluidHandlerEnderBucket implements ICapabilityProvider, IFluidHandler
{
    private final ItemEnderBucket bucket;
    private final ItemStack container;

    public FluidHandlerEnderBucket(ItemEnderBucket bucket, ItemStack stack)
    {
        this.bucket = bucket;
        this.container = stack;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing)
    {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing)
    {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
        {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
        }

        return null;
    }

    @Override
    public IFluidTankProperties[] getTankProperties()
    {
        return new FluidTankProperties[] { new FluidTankProperties(this.getFluid(), this.getCapacity()) };
    }

    @Nullable
    public FluidStack getFluid()
    {
        return this.bucket.getFluid(this.container, null);
    }

    protected int getCapacity()
    {
        return this.bucket.getCapacity(this.container, null);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill)
    {
        return this.bucket.fill(this.container, resource, doFill, null);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        if (resource != null && resource.isFluidEqual(this.bucket.getFluid(this.container, null)))
        {
            return this.bucket.drain(this.container, resource.amount, doDrain, null);
        }

        return null;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
        return this.bucket.drain(this.container, maxDrain, doDrain, null);
    }
}
