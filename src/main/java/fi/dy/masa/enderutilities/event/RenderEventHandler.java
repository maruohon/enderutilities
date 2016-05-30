package fi.dy.masa.enderutilities.event;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class RenderEventHandler
{
    public Minecraft mc;
    public float partialTicksLast;
    protected BuildersWandRenderer buildersWandRenderer;
    protected RulerRenderer rulerRenderer;

    public RenderEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
        this.buildersWandRenderer = new BuildersWandRenderer();
        this.rulerRenderer = new RulerRenderer();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        ItemStack stack = EntityUtils.getHeldItemOfType(this.mc.thePlayer, EnderUtilitiesItems.buildersWand);
        if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand)
        {
            this.buildersWandRenderer.renderSelectedArea(this.mc.theWorld, this.mc.thePlayer, stack, event.getPartialTicks());
        }

        this.rulerRenderer.renderAllPositionPairs(event.getPartialTicks());
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != ElementType.ALL)
        {
            return;
        }

        this.buildersWandRenderer.renderHud();
        this.rulerRenderer.renderHud();
    }
}
