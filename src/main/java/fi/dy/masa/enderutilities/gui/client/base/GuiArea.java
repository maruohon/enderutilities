package fi.dy.masa.enderutilities.gui.client.base;

public class GuiArea
{
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public GuiArea(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public int getWidth()
    {
        return this.width;
    }

    public int getHeight()
    {
        return this.height;
    }

    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return isMouseOverRegion(mouseX, mouseY, this.x, this.y, this.width, this.height);
    }

    public static boolean isMouseOverRegion(int mouseX, int mouseY, int x, int y, int width, int height)
    {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
