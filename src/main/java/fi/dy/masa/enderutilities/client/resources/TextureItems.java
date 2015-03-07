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
}
