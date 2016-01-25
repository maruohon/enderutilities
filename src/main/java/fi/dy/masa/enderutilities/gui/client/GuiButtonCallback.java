package fi.dy.masa.enderutilities.gui.client;

import net.minecraft.util.ResourceLocation;

public class GuiButtonCallback extends GuiButtonHoverText
{
    protected IButtonCallback callback;
    protected int callbackId;

    public GuiButtonCallback(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture,
            String[] hoverStrings, int callbackId, IButtonCallback callback)
    {
        this(id, x, y, w, h, u, v, texture, w, 0, hoverStrings, callbackId, callback);
    }

    public GuiButtonCallback(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture,
            int hoverOffsetU, int hoverOffsetV, String[] hoverStrings, int callbackId, IButtonCallback callback)
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
