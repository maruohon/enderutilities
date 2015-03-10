package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemEnderPart extends ItemModule
{
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

        // Damage 45: Mob Persistence
        if (stack.getItemDamage() == 45)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE;
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

            list.add(new ItemStack(this, 1, 45)); // Mob Persistence
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return false;
        }

        // Ender Relic
        if (stack != null && stack.getItemDamage() == 40)
        {
            if (EntityUtils.spawnEnderCrystal(world, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d) == true)
            {
                --stack.stackSize;
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the item can be used on the given entity, e.g. shears on sheep.
     */
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        if (stack != null && stack.getItemDamage() == 45)
        {
            if (livingBase instanceof EntityLiving && EntityUtils.applyMobPersistence((EntityLiving)livingBase) == true)
            {
                --stack.stackSize;
            }
        }

        return false;
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

        // Mob Persistence
        if (stack.getItemDamage() == 45)
        {
            return ModuleType.TYPE_MOBPERSISTENCE;
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

        // Mob Persistence
        if (stack.getItemDamage() == 45)
        {
            return 0;
        }

        return -1; // Invalid item (= non-module)
    }


    @SideOnly(Side.CLIENT)
    @Override
    public String getBaseModelName(String variant)
    {
        if (variant.equals(this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK))
        {
            return ReferenceNames.NAME_ITEM_ENDERTOOL;
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerVariants()
    {
        String[] allVariants = new String[13];
        int i = 0, j = 0;

        for (i = 0; i < 3; ++i, ++j)
        {
            allVariants[j] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + i;
        }

        for (i = 0; i < 3; ++i, ++j)
        {
            allVariants[j] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + i + ".inactive";
        }

        for (i = 0; i < 3; ++i, ++j)
        {
            allVariants[j] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + i + ".active";
        }

        allVariants[j++] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK;
        allVariants[j++] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE;
        allVariants[j++] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERRELIC;
        allVariants[j++] = this.name + "." + ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE;

        this.addVariants(allVariants);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IFlexibleBakedModel getItemModel(ItemStack stack)
    {
        int index = 0;
        int damage = stack.getItemDamage();

        // Ender Alloy
        if (damage >= 0 && damage <= 2) { index = damage; }

        // Inactive Ender Core
        if (damage >= 10 && damage <= 12) { index = damage - 7; }

        // Ender Core (active)
        if (damage >= 15 && damage <= 17) { index = damage - 9; }

        // Ender Stick
        if (damage == 20) { index = 9; }

        // Ender Rope
        if (damage == 21) { index = 10; }

        // Ender Rope
        if (damage == 40) { index = 11; }

        // Mob Persistence
        if (damage == 45) { index = 12; }

        return this.models[index];
    }
}
