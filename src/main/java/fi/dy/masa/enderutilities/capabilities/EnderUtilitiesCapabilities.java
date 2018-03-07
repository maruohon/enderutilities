package fi.dy.masa.enderutilities.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import fi.dy.masa.enderutilities.reference.Reference;

public class EnderUtilitiesCapabilities
{
    public static final ResourceLocation PORTAL_COOLDOWN_CAP_NAME = new ResourceLocation(Reference.MOD_ID, "entity_portal_cooldown");

    @CapabilityInject(IPortalCooldownCapability.class)
    public static Capability<IPortalCooldownCapability> CAPABILITY_PORTAL_COOLDOWN = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPortalCooldownCapability.class, new DefaultPortalCooldownStorage<>(), () -> new PortalCooldownCapability());
    }

    public static class PortalCooldownCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTBase>
    {
        private final IPortalCooldownCapability cap;
        private static final DefaultPortalCooldownStorage<IPortalCooldownCapability> STORAGE = new DefaultPortalCooldownStorage<>();

        public PortalCooldownCapabilityProvider()
        {
            this.cap = new PortalCooldownCapability();
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing)
        {
            return capability == CAPABILITY_PORTAL_COOLDOWN;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing)
        {
            return capability == CAPABILITY_PORTAL_COOLDOWN ? CAPABILITY_PORTAL_COOLDOWN.cast(this.cap) : null;
        }

        @Override
        public NBTBase serializeNBT()
        {
            return STORAGE.writeNBT(CAPABILITY_PORTAL_COOLDOWN, this.cap, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt)
        {
            STORAGE.readNBT(CAPABILITY_PORTAL_COOLDOWN, this.cap, null, nbt);
        }
    }

    private static class DefaultPortalCooldownStorage<T extends IPortalCooldownCapability> implements Capability.IStorage<T>
    {
        @Override
        public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side)
        {
            if ((instance instanceof IPortalCooldownCapability) == false)
            {
                throw new RuntimeException(instance.getClass().getName() + " does not implement IPortalCooldownCapability");
            }

            NBTTagCompound nbt = new NBTTagCompound();
            IPortalCooldownCapability cap = (IPortalCooldownCapability) instance;
            nbt.setLong("LastInPortal", cap.getLastInPortalTime());

            return nbt;
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt)
        {
            if ((instance instanceof IPortalCooldownCapability) == false)
            {
                throw new RuntimeException(instance.getClass().getName() + " does not implement IPortalCooldownCapability");
            }

            NBTTagCompound tags = (NBTTagCompound) nbt;
            IPortalCooldownCapability cap = capability.cast(instance);
            cap.setLastInPortalTime(tags.getLong("LastInPortal"));
        }
    }
}
