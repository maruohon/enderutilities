package fi.dy.masa.enderutilities.client.renderer.item;

import net.minecraft.client.renderer.Tessellator;

public class ItemRenderer
{
	/**
	 * Renders an item as a 2D texture with thickness
	 */
	public static void renderItemLayerIn2D(Tessellator tessellator, int width, int height, float thickness,
			float minX, float minY, float maxX, float maxY,
			float minU, float minV, float maxU, float maxV,
			double layerOffsetX, double layerOffsetY, double layerOffsetZ)
	{
		double x1, x2, y1, y2, z1, z2;
		//double layerOffset = (double)layerNum * 0.0000025d;
		x1 = minX + layerOffsetX;
		x2 = maxX - layerOffsetX;
		y1 = minY + layerOffsetY;
		y2 = maxY - layerOffsetY;
		z1 = 0.0d - layerOffsetZ;
		z2 = layerOffsetZ - thickness;

		// Render front side
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0f, 0.0f, 1.0f);
		tessellator.addVertexWithUV(x1, y1, z1, (double)minU, (double)minV);
		tessellator.addVertexWithUV(x2, y1, z1, (double)maxU, (double)minV);
		tessellator.addVertexWithUV(x2, y2, z1, (double)maxU, (double)maxV);
		tessellator.addVertexWithUV(x1, y2, z1, (double)minU, (double)maxV);
		tessellator.draw();

		// Render back side
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0f, 0.0f, -1.0f);
		tessellator.addVertexWithUV(x1, y2, z2, (double)minU, (double)maxV);
		tessellator.addVertexWithUV(x2, y2, z2, (double)maxU, (double)maxV);
		tessellator.addVertexWithUV(x2, y1, z2, (double)maxU, (double)minV);
		tessellator.addVertexWithUV(x1, y1, z2, (double)minU, (double)minV);
		tessellator.draw();

		float halfPixelWidth = 0.5f * (minU - maxU) / (float)width;
		float halfPixelHeight = 0.5f * (minV - maxV) / (float)height;
		int i;
		double x, y, u, v;

		// Render left side
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0f, 0.0f, 0.0f);
		for (i = 0; i < width; ++i)
		{
			x = ((float)i / (float)width); // relative position
			u = minU + (maxU - minU) * x - halfPixelWidth;
			x = x1 + ((float)i * (maxX - minX - (2 * layerOffsetX)) / (float)width); // actual position
			tessellator.addVertexWithUV(x, y1, z2, u, (double)minV);
			tessellator.addVertexWithUV(x, y1, z1, u, (double)minV);
			tessellator.addVertexWithUV(x, y2, z1, u, (double)maxV);
			tessellator.addVertexWithUV(x, y2, z2, u, (double)maxV);
		}
		tessellator.draw();

		// Render right side
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0f, 0.0f, 0.0f);
		for (i = 0; i < width; ++i)
		{
			x = ((float)i / (float)width); // relative position
			u = minU + (maxU - minU) * x - halfPixelWidth;
			x = x1 + (((float)i + 1.0f) * (maxX - minX - (2 * layerOffsetX)) / (float)width); // actual position
			tessellator.addVertexWithUV(x, y2, z2, u, (double)maxV);
			tessellator.addVertexWithUV(x, y2, z1, u, (double)maxV);
			tessellator.addVertexWithUV(x, y1, z1, u, (double)minV);
			tessellator.addVertexWithUV(x, y1, z2, u, (double)minV);
		}
		tessellator.draw();

		// Render bottom side
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0f, 1.0f, 0.0f);
		for (i = 0; i < height; ++i)
		{
			y = ((float)i / (float)height); // relative position
			v = minV + (maxV - minV) * y - halfPixelHeight;
			y = y1 + (((float)i + 1.0f) * (maxY - minY - (2 * layerOffsetY)) / (float)height); // actual position
			tessellator.addVertexWithUV(x1, y, z1, (double)minU, v);
			tessellator.addVertexWithUV(x2, y, z1, (double)maxU, v);
			tessellator.addVertexWithUV(x2, y, z2, (double)maxU, v);
			tessellator.addVertexWithUV(x1, y, z2, (double)minU, v);
		}
		tessellator.draw();

		// Render top side
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0f, -1.0f, 0.0f);
		for (i = 0; i < height; ++i)
		{
			y = ((float)i / (float)height); // relative position
			v = minV + (maxV - minV) * y - halfPixelHeight;
			y = y1 + ((float)i * (maxY - minY - (2 * layerOffsetY)) / (float)height); // actual position
			tessellator.addVertexWithUV(x2, y, z1, (double)maxU, v);
			tessellator.addVertexWithUV(x1, y, z1, (double)minU, v);
			tessellator.addVertexWithUV(x1, y, z2, (double)minU, v);
			tessellator.addVertexWithUV(x2, y, z2, (double)maxU, v);
		}
		tessellator.draw();

	}
}
