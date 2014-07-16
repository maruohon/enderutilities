package fi.dy.masa.enderutilities.util;

import net.minecraft.util.StatCollector;

public class TooltipHelper
{
	public static String getLocalizedDimensionName(int dim)
	{
		String dimStr;

		if (dim == 0)
		{
			dimStr = StatCollector.translateToLocal("gui.tooltip.dimension.overworld");
		}
		else if (dim == -1)
		{
			dimStr = StatCollector.translateToLocal("gui.tooltip.dimension.nether");
		}
		else if (dim == 1)
		{
			dimStr = StatCollector.translateToLocal("gui.tooltip.dimension.end");
		}
		else
		{
			dimStr = "";
		}

		return dimStr;
	}
}
