package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ItemHandyBag extends ItemModular implements IKeyBound
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemHandyBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_HANDY_BAG);
    }

    public boolean bagIsEnabled(ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        if (stack.getTagCompound() == null || (stack.getTagCompound().hasKey(this.name, Constants.NBT.TAG_COMPOUND) == true
                && stack.getTagCompound().getCompoundTag(this.name).getBoolean("DisableOpen") == false))
        {
            return true;
        }

        return false;
    }

    public int getSlotContainingEnabledBag(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, this);
        for (int slot : slots)
        {
            if (this.bagIsEnabled(player.inventory.getStackInSlot(slot)) == true)
            {
                return slot;
            }
        }

        return -1;
    }

    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 3;
    }

    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
    {
        return moduleType.equals(ModuleType.TYPE_MEMORY_CARD) ? 3 : 0;
    }

    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule && ((IModule)moduleStack.getItem()).getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD))
        {
            return 3;
        }

        return 0;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
    }
}
