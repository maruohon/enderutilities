package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.util.ResourceLocation;

public class GuiButtonCallback extends GuiButtonHoverText
{
    protected IButtonCallback callback;
    protected int callbackId;

    public GuiButtonCallback(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture,
            int callbackId, IButtonCallback callback, String ... hoverStrings)
    {
        this(id, x, y, w, h, u, v, texture, w, 0, callbackId, callback, hoverStrings);
    }

    public GuiButtonCallback(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture,
            int hoverOffsetU, int hoverOffsetV, int callbackId, IButtonCallback callback, String ... hoverStrings)
    {
        super(id, x, y, w, h, u, v, texture, hoverOffsetU, hoverOffsetV, hoverStrings);
        this.callbackId = callbackId;
        this.callback = callback;
    }

    public void setCallback(IButtonCallback callback)
    {
        this.callback = callback;
    }

    @Override
    protected int getU()
    {
        return this.callback.getButtonU(this.callbackId);
    }

    @Override
    protected int getV()
    {
        return this.callback.getButtonV(this.callbackId);
    }
}
