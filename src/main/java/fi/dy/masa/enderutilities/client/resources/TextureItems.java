package fi.dy.masa.enderutilities.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureItems extends TextureAtlasSprite
{
    public TextureItems(String spriteName)
    {
        super(spriteName);
    }

    /*@Override
    public void loadSprite(BufferedImage[] images, AnimationMetadataSection meta)
    {
        System.out.println("loadSprite(); images.len: " + (images != null ? images.length : 0));
        super.loadSprite(images, meta);
    }*/

    /**
     * The result of this function determines is the below 'load' function is called, and the
     * default vanilla loading code is bypassed completely.
     * @param manager
     * @param location
     * @return True to use your own custom load code and bypass vanilla loading.
     */
    @Override
    public boolean hasCustomLoader(net.minecraft.client.resources.IResourceManager manager, net.minecraft.util.ResourceLocation location)
    {
        return false;
    }

    /**
     * Load the specified resource as this sprite's data.
     * Returning false from this function will prevent this icon from being stitched onto the master texture.
     * @param manager Main resource manager
     * @param location File resource location
     * @return False to prevent this Icon from being stitched
     * 
     * NOTE: The above return value description is reversed!!
     */
    @Override
    public boolean load(net.minecraft.client.resources.IResourceManager manager, net.minecraft.util.ResourceLocation location)
    {
        return false;
    }
}
