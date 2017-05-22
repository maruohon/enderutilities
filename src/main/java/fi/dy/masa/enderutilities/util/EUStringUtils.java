package fi.dy.masa.enderutilities.util;

import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.item.ItemStack;

public class EUStringUtils
{
    public static final String EMPTY = "";
    public static final String[] SI_PREFIXES = new String[] { "", "k", "M", "G", "T", "P", "E", "Z", "Y" };

    public static String getStackSizeString(ItemStack stack, int maxChars)
    {
        return formatNumber(stack.getCount(), 9999, maxChars);
    }

    public static String formatNumber(long value, long maxUnformatted, int maxChars)
    {
        // Simple case, we can display the entire number
        if (value <= maxUnformatted)
        {
            return String.valueOf(value);
        }

        //long limit = Math.max((long) Math.pow(10, maxChars), 10) / 10; // divide by 10 to leave space for the SI prefix
        double dValue = value;
        int prefixIndex = 0;

        while (dValue >= 1000D)
        {
            dValue /= 1000D;
            prefixIndex++;
        }

        int digits = 1;
        double div = 10D;

        while (dValue >= div && digits < 64) // use a fail-safe
        {
            div *= 10;
            digits++;
        }

        try
        {
            // How many decimals can we fit. The -1 is for the decimal dot.
            int maxDecimals = maxChars - digits - 1 - SI_PREFIXES[prefixIndex].length();
            String valueStr = String.valueOf(dValue);
            int endIndex = maxDecimals > 0 ? Math.min(digits + 1 + maxDecimals, valueStr.length()) : digits;
            valueStr = valueStr.substring(0, endIndex);
            //String fmt = maxDecimals >= 0 ? "%" + (digits + maxDecimals) + "." + maxDecimals + "f%s" : "%.0f%s";

            return valueStr + SI_PREFIXES[prefixIndex];
        }
        catch (Exception e)
        {
            return "OOPS";
        }
    }

    /**
     * Formats the number in value with thousand separator "," for display.
     * @param value
     * @return
     */
    public static String formatNumberWithKSeparators(long value)
    {
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }

    /**
     * Formats the string in str into the initial letters of each word, imploded with "."
     * @param str
     * @return
     */
    public static String getInitialsWithDots(String str)
    {
        StringBuilder sb = new StringBuilder(64);
        String[] split = str.split(" ");

        for (String s : split)
        {
            int i = 0;
            while (i < s.length())
            {
                if (Character.isAlphabetic(s.charAt(i)) == true)
                {
                    sb.append(s.substring(i, i + 1)).append(".");
                    break;
                }
                i++;
            }
        }

        return sb.toString();
    }
}
