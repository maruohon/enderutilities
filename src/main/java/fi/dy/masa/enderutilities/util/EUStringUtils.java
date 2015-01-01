package fi.dy.masa.enderutilities.util;


public class EUStringUtils
{
    public static String formatNumberFloorWithPostfix(int value)
    {
        if (value >= 1000000)
        {
            return String.format("%dM", value / 1000000);
        }

        if (value >= 10000)
        {
            return String.format("%dk", value / 1000);
        }

        if (value > 1000)
        {
            return String.format("%.1fk", ((float)value) / 1000);
        }

        return String.valueOf(value);
    }

    public static String formatNumberWithKSeparators(int value)
    {
        StringBuilder sb = new StringBuilder(16);

        if (value >= 1000000000)
        {
            sb.append(String.valueOf(value / 1000000000) + ",");
        }

        if (value >= 1000000)
        {
            sb.append(String.valueOf((value / 1000000) % 1000) + ",");
        }

        if (value >= 1000)
        {
            sb.append(String.valueOf((value / 1000) % 1000) + ",");
        }

        if (value != 0)
        {
            sb.append(String.format("%03d", value % 1000));
        }
        else
        {
            sb.append(String.valueOf(value));
        }

        return sb.toString();
    }

    public static String getInitialsWithDots(String str)
    {
        StringBuilder sb = new StringBuilder(64);
        String[] split = str.split(" ");

        for (String s : split)
        {
            sb.append(s.substring(0,  1)).append(".");
        }

        return sb.toString();
    }
}
