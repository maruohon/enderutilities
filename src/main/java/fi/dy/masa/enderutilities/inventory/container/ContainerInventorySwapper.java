package fi.dy.masa.enderutilities.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.inventory.IContainerItem;
import fi.dy.masa.enderutilities.inventory.MergeSlotRange;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemModular;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerArmor;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerBaubles;
import fi.dy.masa.enderutilities.inventory.slot.SlotItemHandlerGeneric;
import fi.dy.masa.enderutilities.inventory.slot.SlotModuleModularItem;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ContainerInventorySwapper extends ContainerCustomSlotClick implements IContainerItem
{
    private static BaublesInvProviderBase baublesProvider = new BaublesInvProviderBase();
    public final InventoryItemModular inventoryItemModular;
    private ItemStack modularStackLast;
    private MergeSlotRange moduleSlots = new MergeSlotRange(0, 0);;
    private MergeSlotRange playerBaublesSlots = new MergeSlotRange(0, 0);;
    private MergeSlotRange swapperBaublesSlots = new MergeSlotRange(0, 0);;
    private final boolean baublesLoaded;
    private final int xOffset;

    public ContainerInventorySwapper(EntityPlayer player, ItemStack containerStack)
    {
        super(player, new InventoryItemModular(containerStack, player, false, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        this.inventoryItemModular = (InventoryItemModular)this.inventory;
        this.inventoryItemModular.setHostInventory(this.playerInv);
        this.baublesLoaded = ModRegistry.isModLoadedBaubles();
        this.xOffset = this.baublesLoaded ? 20 : 0;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(31 + this.xOffset, 167);
        this.addOffhandSlot(8 + this.xOffset, 129);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        super.addPlayerInventorySlots(posX, posY);

        // Player armor slots
        posX = 8 + this.xOffset;
        posY = 57;

        this.playerArmorSlots = new MergeSlotRange(this.inventorySlots.size(), 4);

        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerArmor(this, this.playerInv, i, 39 - i, posX, posY + i * 18));
        }

        if (this.baublesLoaded)
        {
            this.playerBaublesSlots = new MergeSlotRange(this.inventorySlots.size(), 7);

            // Add the Baubles slots as a priority slot range for shift+click merging
            this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 7);
            IItemHandler inv = baublesProvider.getBaublesInventory(this.player);
            posX = 8;

            for (int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotItemHandlerBaubles(this, inv, i, posX, posY + i * 18));
            }
        }
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
        int posX = 31 + this.xOffset;
        int posY = 57;

        // Inventory Swapper's player inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j + 9, posX + j * 18, posY + i * 18));
            }
        }

        // Inventory Swapper's player inventory hotbar
        for (int i = 0; i < 9; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i, posX + i * 18, posY + 58));
        }

        // Add the armor slots inside the Inventory Swapper as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), 4);

        // Inventory Swapper's armor slots
        posY = 37;

        for (int i = 0; i < 4; i++)
        {
            this.addSlotToContainer(new SlotItemHandlerArmor(this, this.inventory, i, 39 - i, posX + i * 18, posY));
        }

        // Inventory Swapper's Off Hand slot
        this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 40, posX + 4 * 18, posY)
        {
            @SideOnly(Side.CLIENT)
            public String getSlotTexture()
            {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        if (this.baublesLoaded)
        {
            posX = 218;
            posY = 57;
            this.swapperBaublesSlots = new MergeSlotRange(this.inventorySlots.size(), 7);

            for (int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 41 + i, posX, posY + i * 18));
            }
        }

        // The Storage Module slots
        posX = 121 + this.xOffset;
        posY = 15;
        int moduleSlots = this.inventoryItemModular.getModuleInventory().getSlots();

        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItemModular.getModuleInventory(), i, posX + i * 18, posY, ModuleType.TYPE_MEMORY_CARD_ITEMS, this));
        }

        // Add Memory Card slots as a priority slot range for shift+click merging
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size() - moduleSlots, moduleSlots);
        this.moduleSlots = new MergeSlotRange(this.inventorySlots.size() - moduleSlots, moduleSlots);

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);
    }

    public MergeSlotRange getModuleSlots()
    {
        return this.moduleSlots;
    }

    public MergeSlotRange getPlayerBaublesSlots()
    {
        return this.playerBaublesSlots;
    }

    public MergeSlotRange getSwapperBaublesSlots()
    {
        return this.swapperBaublesSlots;
    }

    @Override
    public ItemStack getContainerItem()
    {
        return this.inventoryItemModular.getModularItemStack();
    }

    @Override
    public void detectAndSendChanges()
    {
        if (this.player.getEntityWorld().isRemote == false)
        {
            ItemStack modularStack = this.inventoryItemModular.getModularItemStack();

            // The Bag's stack has changed (ie. to/from null, or different instance), re-read the inventory contents.
            if (modularStack != this.modularStackLast)
            {
                this.inventoryItemModular.readFromContainerItemStack();
                this.modularStackLast = modularStack;
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public ItemStack slotClick(int slotNum, int dragType, ClickType clickType, EntityPlayer player)
    {
        ItemStack stack = this.getContainerItem();

        // Middle click
        if (clickType == ClickType.CLONE && dragType == 2 && stack != null &&
            (this.playerMainSlotsIncHotbar.contains(slotNum) ||
             this.playerArmorSlots.contains(slotNum) ||
             this.playerOffhandSlots.contains(slotNum) ||
             this.playerBaublesSlots.contains(slotNum)))
        {
            int invSlotNum = this.getSlot(slotNum) != null ? this.getSlot(slotNum).getSlotIndex() : -1;

            if (this.playerBaublesSlots.contains(slotNum))
            {
                invSlotNum += 41;
            }

            if (invSlotNum == -1)
            {
                return null;
            }

            byte selected = NBTUtils.getByte(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET_SELECTION);
            long mask = NBTUtils.getLong(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET + selected);
            mask ^= (0x1L << invSlotNum);
            NBTUtils.setLong(stack, ItemInventorySwapper.TAG_NAME_CONTAINER, ItemInventorySwapper.TAG_NAME_PRESET + selected, mask);

            return null;
        }

        stack = super.slotClick(slotNum, dragType, clickType, player);
        this.detectAndSendChanges();

        return stack;
    }

    public static void setBaublesInvProvider(BaublesInvProviderBase baublesProviderIn)
    {
        baublesProvider = baublesProviderIn;
    }

    public static BaublesInvProviderBase getBaublesInvProvider()
    {
        return baublesProvider;
    }

    public static class BaublesInvProviderBase
    {
        public IItemHandler getBaublesInventory(EntityPlayer player)
        {
            return null;
        }
    }
}
