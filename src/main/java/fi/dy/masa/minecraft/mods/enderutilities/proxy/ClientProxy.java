package fi.dy.masa.minecraft.mods.enderutilities.proxy;

import net.minecraft.client.renderer.entity.RenderSnowball;
import cpw.mods.fml.client.registry.RenderingRegistry;
import fi.dy.masa.minecraft.mods.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, new RenderSnowball(EnderUtilitiesItems.enderPearlReusable));
	}
}
