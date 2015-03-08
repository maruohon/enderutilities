package fi.dy.masa.enderutilities.client.resources;

import java.util.Arrays;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;

public class EnderUtilitiesTextureRegistry
{
    public static void registerItemTextures(TextureMap textureMap)
    {
        EnderUtilitiesItems.enderArrow.registerTextures(textureMap);
        EnderUtilitiesItems.enderBag.registerTextures(textureMap);
        EnderUtilitiesItems.enderBow.registerTextures(textureMap);
        EnderUtilitiesItems.enderBucket.registerTextures(textureMap);
        EnderUtilitiesItems.enderLasso.registerTextures(textureMap);
        EnderUtilitiesItems.enderPearlReusable.registerTextures(textureMap);
        EnderUtilitiesItems.enderPorter.registerTextures(textureMap);
        EnderUtilitiesItems.enderCapacitor.registerTextures(textureMap);
        EnderUtilitiesItems.enderPart.registerTextures(textureMap);
        ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerTextures(textureMap);
        ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerTextures(textureMap);
        EnderUtilitiesItems.linkCrystal.registerTextures(textureMap);
        EnderUtilitiesItems.mobHarness.registerTextures(textureMap);
    }

    /**
     * Taken from DenseOres, by RWTema, in accordance to http://creativecommons.org/licenses/by/4.0/deed.en_GB
     */
    public static BakedQuad copyQuad(BakedQuad quad)
    {
        return new BakedQuad(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.getTintIndex(), quad.getFace());
    }

    /**
     * Copy a quad with a different texture overlayed on it.
     * Taken from DenseOres, by RWTema, in accordance to http://creativecommons.org/licenses/by/4.0/deed.en_GB
     */
    public static BakedQuad changeTextureForItem(BakedQuad quad, TextureAtlasSprite tex)
    {
        quad = copyQuad(quad);

        // 4 vertexes on each quad
        for (int i = 0; i < 4; ++i)
        {
            int j = 7 * i;
            // get the x,y,z coordinates
            float x = Float.intBitsToFloat(quad.getVertexData()[j    ]);
            float y = Float.intBitsToFloat(quad.getVertexData()[j + 1]);
            float z = Float.intBitsToFloat(quad.getVertexData()[j + 2]);
            float u = 0.0F;
            float v = 0.0F;

            // move x,y,z in boundary if they are outside
            if (x < 0 || x > 1) x = (x + 1) % 1;
            if (y < 0 || y > 1) y = (y + 1) % 1;
            if (z < 0 || z > 1) z = (z + 1) % 1;


            // calculate the UVs based on the x,y,z and the 'face' of the quad
            switch (quad.getFace().ordinal())
            {
                case 0: // Down
                    u = x * 16.0F;
                    v = (1.0F - z) * 16.0F;
                    break;
                case 1: // Up
                    u = x * 16.0F;
                    v = z * 16.0F;
                    break;
                case 2: // North
                    u = (1.0F - x) * 16.0F;
                    v = (1.0F - y) * 16.0F;
                    break;
                case 3: // South
                    u = x * 16.0F;
                    v = (1.0F - y) * 16.0F;
                    break;
                case 4: // West
                    u = z * 16.0F;
                    v = (1.0F - y) * 16.0F;
                    break;
                case 5: // East
                    u = (1.0F - z) * 16.0F;
                    v = (1.0F - y) * 16.0F;
            }

            // set the new texture uvs
            quad.getVertexData()[j + 4    ] = Float.floatToRawIntBits(tex.getInterpolatedU((double) u));
            quad.getVertexData()[j + 4 + 1] = Float.floatToRawIntBits(tex.getInterpolatedV((double) v));
        }

        return quad;
    }
}
