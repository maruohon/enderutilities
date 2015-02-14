package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;

public class ItemEnderPart extends ItemModule
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemEnderPart()
    {
        super();
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERPART);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // Damage 0: Ender Alloy (Basic)
        // Damage 1: Ender Alloy (Enhanced)
        // Damage 2: Ender Alloy (Advanced)
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + stack.getItemDamage();
        }

        // Damage 10: Inactive Ender Core (Basic)
        // Damage 11: Inactive Ender Core (Enhanced)
        // Damage 12: Inactive Ender Core (Advanced)
        if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + (stack.getItemDamage() - 10) + ".inactive";
        }

        // Damage 15: Ender Core (Basic)
        // Damage 16: Ender Core (Enhanced)
        // Damage 17: Ender Core (Advanced)
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + (stack.getItemDamage() - 15) + ".active";
        }

        // Damage 20: Ender Stick
        if (stack.getItemDamage() == 20)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK;
        }

        // Damage 21: Ender Rope
        if (stack.getItemDamage() == 21)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE;
        }

        // Damage 40: Ender Relic
        if (stack.getItemDamage() == 40)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERRELIC;
        }

        return super.getUnlocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        if (Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            // Ender Alloys
            for (int i = 0; i <= 2; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            // Inactive Ender Cores
            for (int i = 10; i <= 12; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            // (Active) Ender Cores
            for (int i = 15; i <= 17; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            list.add(new ItemStack(this, 1, 20)); // Ender Stick
            list.add(new ItemStack(this, 1, 21)); // Ender Rope

            list.add(new ItemStack(this, 1, 40)); // Ender Relic
        }
    }

    private boolean spawnEnderCrystal(World world, int x, int y, int z)
    {
        // Only allow the activation to happen in The End
        if (world != null && world.provider != null)
        {
            // The item must be right clicked on the Bedrock block on top of the obsidian pillars
            if (world.provider.dimensionId == 1 && world.getBlock(x, y, z) == Blocks.bedrock)
            {
                // Check that there aren't already Ender Crystals nearby
                List<Entity> entities = world.getEntitiesWithinAABB(EntityEnderCrystal.class, AxisAlignedBB.getBoundingBox(x - 2, y - 2, z - 2, x + 2, y + 2, z + 2));
                if (entities.isEmpty() == false)
                {
                    return false;
                }

                // Check that we have a pillar of obsidian below the bedrock block (at least 3x3 wide and 6 tall)
                for (int by = y - 6; by < y; ++by)
                {
                    for (int bx = x - 1; bx <= x + 1; ++bx)
                    {
                        for (int bz = z - 1; bz <= z + 1; ++bz)
                        {
                            if (world.getBlock(bx, by, bz) != Blocks.obsidian)
                            {
                                return false;
                            }
                        }
                    }
                }

                // Everything ok, create an explosion and then spawn a new Ender Crystal
                world.createExplosion(null, x + 0.5f, y + 1, z + 0.5f, 10, true);
                EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
                entityendercrystal.setLocationAndAngles(x + 0.5f, y, z + 0.5f, world.rand.nextFloat() * 360.0f, 0.0f);
                world.spawnEntityInWorld(entityendercrystal);

                return true;
            }
            // Allow spawning decorative Ender Crystals in other dimensions.
            // They won't be valid for Ender Charge, and spawning them doesn't create an explosion or have block requirements.
            else if (world.provider.dimensionId != 1)
            {
                EntityEnderCrystal entityendercrystal = new EntityEnderCrystal(world);
                entityendercrystal.setLocationAndAngles(x + 0.5f, y, z + 0.5f, world.rand.nextFloat() * 360.0f, 0.0f);
                world.spawnEntityInWorld(entityendercrystal);
            }
        }

        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return false;
        }

        // Ender Relic
        if (stack != null && stack.getItemDamage() == 40)
        {
            if (this.spawnEnderCrystal(world, x, y, z) == true)
            {
                if (--stack.stackSize <= 0)
                {
                    player.destroyCurrentEquippedItem();
                }

                return true;
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage)
    {
        // Ender Alloy
        if (damage >= 0 && damage <= 2) { return this.iconArray[damage]; }

        // Inactive Ender Core
        if (damage >= 10 && damage <= 12) { return this.iconArray[damage - 7]; }

        // Ender Core (active)
        if (damage >= 15 && damage <= 17) { return this.iconArray[damage - 9]; }

        // Ender Stick
        if (damage == 20) { return this.iconArray[9]; }

        // Ender Rope
        if (damage == 21) { return this.iconArray[10]; }

        // Ender Rope
        if (damage == 40) { return this.iconArray[11]; }

        return this.itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + ".0");
        this.iconArray = new IIcon[12];

        int i = 0, j;

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + j);
        }

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + j + ".inactive");
        }

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + j + ".active");
        }

        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERRELIC);
    }

    public void activateEnderCore(ItemStack stack)
    {
        // Inactive Ender Cores
        if (stack != null && stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            // "Activate" the Ender Core (ie. change the item)
            stack.setItemDamage(stack.getItemDamage() + 5);
        }
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        // Inactive Ender Cores
        if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            return ModuleType.TYPE_ENDERCORE_INACTIVE;
        }

        // Active Ender Cores
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return ModuleType.TYPE_ENDERCORE_ACTIVE;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        // Inactive Ender Cores
        if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            return stack.getItemDamage() - 10;
        }

        // Active Ender Cores
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return stack.getItemDamage() - 15;
        }

        return -1; // Invalid item (= non-module)
    }
}
