package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class ItemEnderPart extends ItemModule
{
    public static final int MEMORY_CARD_TYPE_MISC = 0;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
    @SideOnly(Side.CLIENT)
    private IIcon[] slotBackgrounds;

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

        // Damage 50: Memory Card (misc)
        if (stack.getItemDamage() == 50)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD;
        }

        return super.getUnlocalizedName();
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
            if (EntityUtils.spawnEnderCrystal(world, x, y, z) == true)
            {
                --stack.stackSize;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        // Jailer module
        if (stack != null && this.getModuleType(stack).equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            if (livingBase instanceof EntityLiving && EntityUtils.applyMobPersistence((EntityLiving)livingBase) == true)
            {
                --stack.stackSize;

                return true;
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

        // Memory Card
        if (stack.getItemDamage() == 50)
        {
            return ModuleType.TYPE_MEMORY_CARD;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        // Inactive Ender Cores
        if (this.getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE_INACTIVE))
        {
            return stack.getItemDamage() - 10;
        }

        // Active Ender Cores
        if (this.getModuleType(stack).equals(ModuleType.TYPE_ENDERCORE_ACTIVE))
        {
            return stack.getItemDamage() - 15;
        }

        // Mob Persistence
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 0;
        }

        // Memory Card
        if (this.getModuleType(stack).equals(ModuleType.TYPE_MEMORY_CARD))
        {
            return 0;
        }

        return -1; // Invalid item (= non-module)
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
            list.add(new ItemStack(this, 1, 50)); // Memory Card
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + ".0");
        this.iconArray = new IIcon[14];

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
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD + ".misc");

        // The background icon for empty slots for this item type
        this.slotBackgrounds = new IIcon[3];
        this.slotBackgrounds[0] = iconRegister.registerIcon(ReferenceTextures.getSlotBackgroundName(ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE));
        this.slotBackgrounds[1] = iconRegister.registerIcon(ReferenceTextures.getSlotBackgroundName(ReferenceNames.NAME_ITEM_ENDERPART_MOBPERSISTENCE));
        this.slotBackgrounds[2] = iconRegister.registerIcon(ReferenceTextures.getSlotBackgroundName(ReferenceNames.NAME_ITEM_ENDERPART_MEMORY_CARD));
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

        if (damage == 20) { return this.iconArray[9]; } // Ender Stick
        if (damage == 21) { return this.iconArray[10]; } // Ender Rope
        if (damage == 40) { return this.iconArray[11]; } // Ender Relic
        if (damage == 45) { return this.iconArray[12]; } // Mob Persistence
        if (damage == 50) { return this.iconArray[13]; } // Memory Card

        return this.itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getGuiSlotBackgroundIconIndex(ModuleType moduleType)
    {
        // Ender Cores
        if (ModuleType.TYPE_ENDERCORE_ACTIVE.equals(moduleType) || ModuleType.TYPE_ENDERCORE_ACTIVE.equals(moduleType)) { return this.slotBackgrounds[0]; }

        // Jailer module
        if (ModuleType.TYPE_MOBPERSISTENCE.equals(moduleType)) { return this.slotBackgrounds[1]; }

        // Memory Cards
        if (ModuleType.TYPE_MEMORY_CARD.equals(moduleType)) { return this.slotBackgrounds[2]; }

        return null;
    }
}
