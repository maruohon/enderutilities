package fi.dy.masa.enderutilities.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemInventorySwapper extends ItemInventoryModular implements IKeyBound
{
    public ItemInventorySwapper()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_INVENTORY_SWAPPER);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.openGui(EnderUtilities.instance, ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return stack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
    }

    @Override
    public int getSizeModuleInventory(ItemStack containerStack)
    {
        return this.getMaxModules(containerStack, ModuleType.TYPE_MEMORY_CARD);
    }

    @Override
    public int getSizeInventory(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getInventoryStackLimit(ItemStack containerStack)
    {
        return 1;
    }

    public static void handleKeyPressUnselected(EntityPlayer player, int key)
    {
        ItemInventorySwapper item = (ItemInventorySwapper)EnderUtilitiesItems.inventorySwapper;
        int slotNum = InventoryUtils.getSlotOfFirstMatchingItem(player.inventory, item);
        if (slotNum >= 0)
        {
            //ItemStack stack = player.inventory.getStackInSlot(slotNum);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Alt + Toggle mode: Toggle the private/public mode
        if (ReferenceKeys.keypressContainsAlt(key) == true
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsControl(key) == false)
        {
            UtilItemModular.changePrivacyModeOnSelectedModule(stack, player, ModuleType.TYPE_MEMORY_CARD);
        }
        // Just Toggle mode: Fire the swapping action
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            handleKeyPressUnselected(player, key);
        }
        // Ctrl (+ Shift) + Toggle mode: Change the selected Memory Card
        else if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 4;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        return moduleType.equals(ModuleType.TYPE_MEMORY_CARD) ? this.getMaxModules(containerStack) : 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.getItem() instanceof IModule)
        {
            IModule imodule = (IModule)moduleStack.getItem();

            if (imodule.getModuleType(moduleStack).equals(ModuleType.TYPE_MEMORY_CARD) &&
                imodule.getModuleTier(moduleStack) == ItemEnderPart.MEMORY_CARD_TYPE_ITEMS_6B)
            {
                return this.getMaxModules(containerStack);
            }
        }

        return 0;
    }
}
