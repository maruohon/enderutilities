package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.InventoryUtils;

public class ItemHandyBag extends ItemInventoryModular implements IKeyBound
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

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
    }

    public static boolean bagIsEnabled(ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        if (stack.getTagCompound() == null || stack.getTagCompound().hasKey("HandyBag", Constants.NBT.TAG_COMPOUND) == false
                || stack.getTagCompound().getCompoundTag("HandyBag").getBoolean("DisableOpen") == false)
        {
            return true;
        }

        return false;
    }

    public static int getSlotContainingEnabledBag(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            if (bagIsEnabled(player.inventory.getStackInSlot(slot)) == true)
            {
                return slot;
            }
        }

        return -1;
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return containerStack.getItemDamage() == 1 ? 59 : 27;
    }

    @Override
    public int getInventoryStackLimit(ItemStack containerStack)
    {
        return containerStack.getItemDamage() == 1 ? 64 : 16; // FIXME testing, change to 1000 : 64 or whatever
    }

    public static void performGuiAction(ItemStack stack, EntityPlayer player, int element, int action)
    {
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
    }

    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
    {
        return moduleType.equals(ModuleType.TYPE_MEMORY_CARD) ? 4 : 0;
    }

    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule && ((IModule)moduleStack.getItem()).getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD))
        {
            return 4;
        }

        return 0;
    }
}
