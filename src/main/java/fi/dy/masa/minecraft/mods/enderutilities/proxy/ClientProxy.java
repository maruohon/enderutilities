package fi.dy.masa.minecraft.mods.enderutilities.proxy;

import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.RenderingRegistry;
import fi.dy.masa.minecraft.mods.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.minecraft.mods.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.minecraft.mods.enderutilities.render.RenderEnderArrow;
import fi.dy.masa.minecraft.mods.enderutilities.render.RenderEnderBow;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerRenderers()
	{
		// FIXME
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class, new RenderEnderArrow());
		RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, new RenderSnowball(EnderUtilitiesItems.enderPearlReusable));

		MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBow, new RenderEnderBow());
	}
}
