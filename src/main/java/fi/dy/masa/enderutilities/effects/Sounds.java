package fi.dy.masa.enderutilities.effects;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import fi.dy.masa.enderutilities.reference.Reference;

public class Sounds
{
    public static final SoundEvent JAILER                   = getRegisteredSound("jailer");
    public static final SoundEvent MOLECULAR_EXCITER        = getRegisteredSound("molecular_exciter");

    private static SoundEvent getRegisteredSound(String name)
    {
        return SoundEvent.REGISTRY.getObject(new ResourceLocation(Reference.MOD_ID, name));
    }
}
