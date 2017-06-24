package fi.dy.masa.enderutilities.gui.client.button;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiButtonHoverText extends GuiButtonIcon
{
    protected ArrayList<String> hoverStrings;

    public GuiButtonHoverText(int id, int x, int y, int w, int h, int u, int v,
            ResourceLocation texture, int hoverOffsetU, int hoverOffsetV, String ... hoverStrings)
    {
        super(id, x, y, w, h, u, v, texture, hoverOffsetU, hoverOffsetV);
        this.hoverStrings = new ArrayList<String>();

        for (String text : hoverStrings)
        {
            this.hoverStrings.add(I18n.format(text));
        }
    }

    public List<String> getHoverStrings()
    {
        return this.hoverStrings;
    }
}
