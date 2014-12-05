package fi.dy.masa.enderutilities.util;


public class TooltipHelper
{
    public static String getDimensionName(int dim, String name, boolean useFallback)
    {
        if (name == null || name.length() == 0)
        {
            if (dim == 0)
            {
                return "Overworld";
            }
            else if (dim == -1)
            {
                return "Nether";
            }
            else if (dim == 1)
            {
                return "The End";
            }
            else if (useFallback == true)
            {
                return "DIM: " + dim;
            }
            else
            {
                return "";
            }
        }

        return name;
    }
}
