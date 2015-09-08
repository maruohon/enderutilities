package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemInventoryModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
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
    public String getUnlocalizedName(ItemStack stack)
    {
        return super.getUnlocalizedName() + "." + stack.getItemDamage();
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
    }

    public static boolean bagIsOpenable(ItemStack stack)
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

    /**
     * Returns the slot number of the first open-able Handy Bag in the player's inventory, or -1 if none is found.
     */
    public static int getSlotContainingOpenableBag(EntityPlayer player)
    {
        List<Integer> slots = InventoryUtils.getSlotNumbersOfMatchingItems(player.inventory, EnderUtilitiesItems.handyBag);
        for (int slot : slots)
        {
            if (bagIsOpenable(player.inventory.getStackInSlot(slot)) == true)
            {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns an ItemStack containing an enabled Handy Bag in the player's inventory, or null if none is found.
     */
    public static ItemStack getOpenableBag(EntityPlayer player)
    {
        int slotNum = getSlotContainingOpenableBag(player);
        return slotNum != -1 ? player.inventory.getStackInSlot(slotNum) : null;
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

    public static int getPickupMode(ItemStack stack)
    {
        if (stack.getTagCompound() == null || stack.getTagCompound().hasKey("HandyBag", Constants.NBT.TAG_COMPOUND) == false)
        {
            return 0;
        }

        return stack.getTagCompound().getCompoundTag("HandyBag").getByte("PickupMode");
    }

    public void cyclePickupMode(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        if (nbt.hasKey("HandyBag", Constants.NBT.TAG_COMPOUND) == false)
        {
            NBTTagCompound tag = new NBTTagCompound();
            nbt.setTag("HandyBag", tag);
        }

        nbt = nbt.getCompoundTag("HandyBag");
        byte mode = nbt.getByte("PickupMode");
        nbt.setByte("PickupMode", ++mode > 2 ? 0 : mode);
    }

    public void toggleLockedMode(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        if (nbt.hasKey("HandyBag", Constants.NBT.TAG_COMPOUND) == false)
        {
            NBTTagCompound tag = new NBTTagCompound();
            nbt.setTag("HandyBag", tag);
        }

        nbt = nbt.getCompoundTag("HandyBag");
        nbt.setBoolean("DisableOpen", ! nbt.getBoolean("DisableOpen"));
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Ctrl (+ Shift) + Toggle mode:  Change the selected Memory Card
        if (ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
        // Just Toggle mode: Cycle Pickup Mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == false
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.cyclePickupMode(stack);
        }
        // Shift + Toggle mode: Toggle Locked Mode
        else if (ReferenceKeys.keypressContainsControl(key) == false
            && ReferenceKeys.keypressContainsShift(key) == true
            && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.toggleLockedMode(stack);
        }
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

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        list.add(new ItemStack(this, 1, 0)); // Tier 1
        list.add(new ItemStack(this, 1, 1)); // Tier 2
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses(int metadata)
    {
        return 3;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".0");
        this.iconArray = new IIcon[6];

        this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".0");
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".1");
        this.iconArray[2] = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));

        // Overlay textures
        this.iconArray[3] = iconRegister.registerIcon(this.getIconString() + ".overlay.locked");
        this.iconArray[4] = iconRegister.registerIcon(this.getIconString() + ".overlay.pickup.matching");
        this.iconArray[5] = iconRegister.registerIcon(this.getIconString() + ".overlay.pickup.all");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        switch (renderPass)
        {
            case 0: // Main texture
                int damage = stack.getItemDamage();
                return this.iconArray[damage <= 1 ? damage : 0];
            case 1: // Locked icon
                return bagIsOpenable(stack) ? this.iconArray[2] : this.iconArray[3];
            case 2: // Pickup mode
                int index = getPickupMode(stack);
                if (index == 1 || index == 2)
                {
                    return this.iconArray[index - 1 + 4];
                }
                return this.iconArray[2]; // empty
        }

        return this.itemIcon;
    }
}
