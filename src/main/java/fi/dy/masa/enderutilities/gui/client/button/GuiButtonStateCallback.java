package fi.dy.masa.enderutilities.gui.client.button;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;

@SideOnly(Side.CLIENT)
public class GuiButtonStateCallback extends GuiButtonHoverText
{
    protected static final ButtonState STATE_INVALID = ButtonState.create(0, 0, "INVALID");
    protected final IButtonStateCallback callback;
    protected final ButtonState[] states;

    public GuiButtonStateCallback(int id, int x, int y, int w, int h, int hoverOffsetU, int hoverOffsetV,
            ResourceLocation texture, IButtonStateCallback callback, ButtonState... states)
    {
        super(id, x, y, w, h, 0, 0, texture, hoverOffsetU, hoverOffsetV);

        this.callback = callback;
        this.states = states;
    }

    @Override
    protected int getU()
    {
        return this.getState(this.callback.getButtonStateIndex(this.id)).getU();
    }

    @Override
    protected int getV()
    {
        return this.getState(this.callback.getButtonStateIndex(this.id)).getV();
    }

    @Override
    protected boolean isEnabled()
    {
        return this.callback.isButtonEnabled(this.id);
    }

    @Override
    public List<String> getHoverStrings()
    {
        return this.getState(this.callback.getButtonStateIndex(this.id)).getHoverStrings();
    }

    protected ButtonState getState(int index)
    {
        return index >= 0 && index < this.states.length ? this.states[index] : STATE_INVALID;
    }

    public static class ButtonState
    {
        private final int u;
        private final int v;
        private final List<String> hoverText;

        private ButtonState(int u, int v, boolean translate, String... hoverStrings)
        {
            this.u = u;
            this.v = v;
            this.hoverText = new ArrayList<String>();

            for (String key : hoverStrings)
            {
                if (translate)
                {
                    ItemEnderUtilities.addTranslatedTooltip(key, this.hoverText, false);
                }
                else
                {
                    this.hoverText.add(key);
                }
            }
        }

        public int getU()
        {
            return this.u;
        }

        public int getV()
        {
            return this.v;
        }

        public List<String> getHoverStrings()
        {
            return this.hoverText;
        }

        public static ButtonState create(int u, int v, String... hoverStrings)
        {
            return new ButtonState(u, v, false, hoverStrings);
        }

        public static ButtonState createTranslate(int u, int v, String... hoverStrings)
        {
            return new ButtonState(u, v, true, hoverStrings);
        }
    }
}
