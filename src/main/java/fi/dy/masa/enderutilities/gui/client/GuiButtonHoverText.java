package fi.dy.masa.enderutilities.gui.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonHoverText extends GuiButtonIcon
{
    protected ArrayList<String> hoverStrings;

    public GuiButtonHoverText(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture, String[] hoverStrings)
    {
        this(id, x, y, w, h, u, v, texture, w, 0, hoverStrings);
    }

    public GuiButtonHoverText(int id, int x, int y, int w, int h, int u, int v, ResourceLocation texture, int hoverOffsetU, int hoverOffsetV, String[] hoverStrings)
    {
        super(id, x, y, w, h, u, v, texture, hoverOffsetU, hoverOffsetV);
        this.hoverStrings = new ArrayList<String>(2);

        for (String text : hoverStrings)
        {
            this.hoverStrings.add(text);
        }
    }

    public List<String> getHoverStrings()
    {
        return this.hoverStrings;
    }
}
