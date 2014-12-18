package fi.dy.masa.enderutilities.util;


public class EUStringUtils
{
    public static String formatNumberFloor(int value)
    {
        if (value <= 1000)
        {
            return String.format("%d", value);
        }

        if (value > 1000 && value < 10000)
        {
            return String.format("%.1fk", ((float)value) / 1000);
        }

        if (value >= 10000)
        {
            return String.format("%dk", value / 1000);
        }

        return "" + value; // never reached
    }
}
