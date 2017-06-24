package fi.dy.masa.enderutilities.gui.client.button;

import net.minecraft.util.ResourceLocation;

public class GuiButtonCallback extends GuiButtonHoverText
{
    protected IButtonCallback callback;

    public GuiButtonCallback(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture,
            int hoverOffsetU, int hoverOffsetV, IButtonCallback callback, String ... hoverStrings)
    {
        super(id, x, y, w, h, u, v, texture, hoverOffsetU, hoverOffsetV, hoverStrings);
        this.callback = callback;
    }

    @Override
    protected int getU()
    {
        return this.callback.getButtonU(this.id, this.u);
    }

    @Override
    protected int getV()
    {
        return this.callback.getButtonV(this.id, this.v);
    }

    @Override
    protected boolean isEnabled()
    {
        return this.callback.isButtonEnabled(this.id);
    }
}
