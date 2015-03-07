package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;

public class ItemLinkCrystal extends ItemLocationBound implements IModule
{
    public static final int TYPE_LOCATION = 0;
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_PORTAL = 2;

    public ItemLinkCrystal()
    {
        super();
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // Damage 0: Link Crystal (In-World)
        // Damage 1: Link Crystal (Inventory)
        // Damage 2: Link Crystal (Portal)
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return super.getUnlocalizedName() + "." + stack.getItemDamage();
        }

        return super.getUnlocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        if (Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            // FIXME Disabled the Portal type Link Crystal until it is actually used
            for (int i = 0; i <= 1; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }


    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return ModuleType.TYPE_LINKCRYSTAL;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return stack.getItemDamage();
        }

        return -1;
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        this.textures = new TextureAtlasSprite[3];
        this.registerTexture(0, this.name + ".location", textureMap);
        this.registerTexture(1, this.name + ".block",    textureMap);
        this.registerTexture(2, this.name + ".portal",   textureMap);
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getItemTexture(ItemStack stack)
    {
        int index = stack.getItemDamage();

        return this.textures[index < this.textures.length ? index : 0];
    }
}
