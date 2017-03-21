package fi.dy.masa.enderutilities.gui.client.base;

import javax.annotation.Nullable;
import org.lwjgl.input.Mouse;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class ScrollBar extends GuiArea
{
    private final int id;
    private final int u;
    private final int v;
    @Nullable private final ResourceLocation texture;
    private final GuiEnderUtilities parent;
    private int hoverOffsetU;
    private int hoverOffsetV;
    private int position;
    private boolean dragging;
    //private int dragStartX;
    private int dragStartY;
    private int dragStartPosition;
    private int positionMax = 999;

    public ScrollBar(int id, int x, int y, int u, int v, int width, int height, int positions, GuiEnderUtilities parent)
    {
        this(id, x, y, u, v, width, height, positions, parent, null);
    }

    public ScrollBar(int id, int x, int y, int u, int v, int width, int height, int positions, GuiEnderUtilities parent, ResourceLocation texture)
    {
        super(x, y, width, height);

        this.id = id;
        this.u = u;
        this.v = v;
        this.positionMax = positions - 1;
        this.parent = parent;
        this.texture = texture;
        this.hoverOffsetU = width;
    }

    public ScrollBar setHoverOffsetU(int offsetU)
    {
        this.hoverOffsetU = offsetU;
        return this;
    }

    public ScrollBar setHoverOffsetV(int offsetV)
    {
        this.hoverOffsetV = offsetV;
        return this;
    }

    public int getId()
    {
        return this.id;
    }

    public int getPosition()
    {
        return this.position;
    }

    public int getMaxPosition()
    {
        return this.positionMax;
    }

    public void render(int parentX, int parentY, int mouseX, int mouseY)
    {
        if (this.texture != null)
        {
            this.parent.bindTexture(this.texture);
        }

        int x = parentX + this.getX();
        int y = parentY + this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        int offsetU = 0;
        int offsetV = 0;

        // There are "up" and "down" buttons at the top and bottom of the scroll bar.
        // They are square, ie. their height is the same as this.getWidth().
        // So those will be excluded from the scroll bar rendering, and rendered separately as sort of buttons

        // Top button
        if (isMouseOverRegion(mouseX, mouseY, x, y, w, w)) // square region, so width is correct!
        {
            offsetU = this.hoverOffsetU;
            offsetV = this.hoverOffsetV;
        }

        this.parent.drawTexturedModalRect(x, y, this.u + offsetU, this.v + offsetV, w, w);

        // Bottom button
        if (isMouseOverRegion(mouseX, mouseY, x, y + h - w, w, w)) // square region, so width is correct!
        {
            offsetU = this.hoverOffsetU;
            offsetV = this.hoverOffsetV;
        }
        else
        {
            offsetU = 0;
            offsetV = 0;
        }

        this.parent.drawTexturedModalRect(x, y + h - w, this.u + offsetU, this.v + offsetV + w, w, w);

        // The actual scroll bar
        if (isMouseOverRegion(mouseX, mouseY, x, y + w, w, h - 2 * w))
        {
            offsetU = this.hoverOffsetU;
            offsetV = this.hoverOffsetV;
        }
        else
        {
            offsetU = 0;
            offsetV = 0;
        }

        // 16 is the height of the scroll bar texture
        int offY = (h - 16 - 2 * w) * this.position / this.positionMax;
        this.parent.drawTexturedModalRect(x, y + w + offY, this.u + offsetU, this.v + offsetV + 2 * w, w, h - 2 * w);
    }

    public boolean handleMouseInput(int mouseX, int mouseY)
    {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();
        int dWheel = Mouse.getEventDWheel();

        if (Mouse.isButtonDown(0) == false)
        {
            this.dragging = false;
        }

        if (this.dragging)
        {
            int pos = this.dragStartPosition + (this.positionMax * (mouseY - this.dragStartY) / (h - 16 - 2 * w));
            pos = MathHelper.clamp(pos, 0, this.positionMax);
            this.position = pos;
            this.parent.scrollbarAction(this.id, ScrollbarAction.SET, pos);
            return true;
        }
        else if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0)
        {
            // Click over the up button
            if (isMouseOverRegion(mouseX, mouseY, x, y, w, w))
            {
                return this.move(-1, true);
            }
            // Click over the down button
            else if (isMouseOverRegion(mouseX, mouseY, x, y + h - w, w, w))
            {
                return this.move(1, true);
            }
            // Click over the scroll bar
            else if (isMouseOverRegion(mouseX, mouseY, x, y + w, w, h - 2 * w))
            {
                // 16 is for the scroll bar height
                // This isn't super great at the moment, since it is quite dependent on the moving part of the scroll bar
                // being 16 pixels high...
                int pos = this.positionMax * (mouseY - y - w - 6) / (h - 16 - 2 * w);
                pos = MathHelper.clamp(pos, 0, this.positionMax);
                this.position = pos;
                this.parent.scrollbarAction(this.id, ScrollbarAction.SET, pos);
                this.dragging = true;
                this.dragStartPosition = this.position;
                //this.dragStartX = mouseX;
                this.dragStartY = mouseY;
                return true;
            }
        }
        else if (dWheel != 0)
        {
            return this.move(dWheel < 0 ? 1 : -1, false);
        }

        return false;
    }

    private boolean move(int amount, boolean playSound)
    {
        this.position = MathHelper.clamp(this.position + amount, 0, this.positionMax);
        this.parent.scrollbarAction(this.id, ScrollbarAction.MOVE, -1);

        if (playSound)
        {
            this.playPressSound();
        }

        return true;
    }

    protected void playPressSound()
    {
        this.parent.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public enum ScrollbarAction
    {
        MOVE,
        SET
    }
}
