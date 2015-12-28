package fi.dy.masa.enderutilities.inventory;

import java.util.UUID;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

public class ContainerHandyBag extends ContainerLargeStacks implements IContainerModularItem
{
    public final EntityPlayer player;
    public final InventoryItemModular inventoryItemModular;

    public InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public IInventory craftResult = new InventoryCraftResult();

    public ContainerHandyBag(EntityPlayer player, InventoryItemModular inventory)
    {
        super(player.inventory, inventory);
        this.player = player;
        this.inventoryItemModular = inventory;

        this.addCustomInventorySlots();
        this.addPlayerInventorySlots(8, 174);
    }

    @Override
    protected void addPlayerInventorySlots(int posX, int posY)
    {
        if (this.getBagTier() == 1)
        {
            posX += 40;
        }

        super.addPlayerInventorySlots(posX, posY);

        // Player armor slots
        posY = 15;
        for (int i = 0; i < 4; i++)
        {
            final int slotNum = i;
            this.addSlotToContainer(new Slot(this.inventoryPlayer, 39 - i, posX, posY + i * 18)
            {
                public int getSlotStackLimit()
                {
                    return 1;
                }

                public boolean isItemValid(ItemStack stack)
                {
                    if (stack == null) return false;
                    return stack.getItem().isValidArmor(stack, slotNum, ContainerHandyBag.this.player);
                }

                /* TODO: Enable this in 1.8; in 1.7.10, there is a Forge bug that causes
                 * the Slot background icons to render incorrectly if there is an item with the glint effect
                 * before the Slot in question in the Container.
                @SideOnly(Side.CLIENT)
                public IIcon getBackgroundIconIndex()
                {
                    return ItemArmor.func_94602_b(slotNum);
                }
                */
            });
        }

        // Player crafting slots
        posX += 90;
        posY = 15;
        this.addSlotToContainer(new SlotCrafting(this.player, this.craftMatrix, this.craftResult, 0, posX + 54, posY + 10));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, posX + j * 18, posY + i * 18));
            }
        }

        this.onCraftMatrixChanged(this.craftMatrix);
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int xOff = 8;
        int yOff = 102;

        if (this.getBagTier() == 1)
        {
            xOff += 40;
        }

        // The top/middle section of the bag inventory
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 9; j++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemModular.getMainInventory(), i * 9 + j, xOff + j * 18, yOff + i * 18));
            }
        }

        if (this.getBagTier() == 1)
        {
            int xOffXtra = 8;
            yOff = 102;

            // Left side extra slots
            for(int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemModular.getMainInventory(), 27 + i * 2, xOffXtra +  0, yOff + i * 18));
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemModular.getMainInventory(), 28 + i * 2, xOffXtra + 18, yOff + i * 18));
            }

            xOffXtra = 214;
            // Right side extra slots
            for(int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemModular.getMainInventory(), 41 + i * 2, xOffXtra +  0, yOff + i * 18));
                this.addSlotToContainer(new SlotGeneric(this.inventoryItemModular.getMainInventory(), 42 + i * 2, xOffXtra + 18, yOff + i * 18));
            }
        }

        xOff += 90;
        yOff = 69;
        int moduleSlots = this.inventory.getSizeInventory();
        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModule(this.inventory, i, xOff + i * 18, yOff, ModuleType.TYPE_MEMORY_CARD, this));
        }
    }

    @Override
    public ItemStack getModularItem()
    {
        return this.inventoryItemModular.getContainerItemStack();
    }

    @Override
    protected int getNumMergableSlots(int invSize)
    {
        // Our inventory, player item inventory and armor slots
        return invSize + this.inventoryPlayer.getSizeInventory();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
        this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.player.worldObj));
    }

    public void dropCraftingGridContents()
    {
        for (int i = 0; i < 4; ++i)
        {
            ItemStack stack = this.craftMatrix.getStackInSlotOnClosing(i);

            if (stack != null)
            {
                player.dropPlayerItemWithRandomChoice(stack, false);
            }
        }

        this.craftResult.setInventorySlotContents(0, (ItemStack)null);
    }

    public int getBagTier()
    {
        if (this.inventoryItemModular.getContainerItemStack() != null)
        {
            return this.inventoryItemModular.getContainerItemStack().getItemDamage() == 1 ? 1 : 0;
        }

        return 0;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player)
    {
        return true;
    }

    @Override
    public void onContainerClosed(EntityPlayer player)
    {
        super.onContainerClosed(player);

        // Drop the items in the crafting grid
        this.dropCraftingGridContents();

        this.inventoryItemModular.closeInventory();
    }

    @Override
    public boolean func_94530_a(ItemStack stack, Slot slot)
    {
        return slot.inventory != this.craftResult && super.func_94530_a(stack, slot);
    }

    @Override
    public ItemStack slotClick(int slotNum, int key, int type, EntityPlayer player)
    {
        ItemStack containerStack = this.inventoryItemModular.getContainerItemStack();
        ItemStack stackPre = slotNum >= 0 && slotNum < this.inventorySlots.size() ? this.getSlot(slotNum).getStack() : null;

        ItemStack stack = super.slotClick(slotNum, key, type, player);

        ItemStack stackPost = slotNum >= 0 && slotNum < this.inventorySlots.size() ? this.getSlot(slotNum).getStack() : null;

        // FIXME BROKEN AFTER THE REFACTORING !!!!!
        // The Bag's stack changed to or from null, re-read the inventory contents.
        if ((containerStack == stackPre || containerStack == stackPost) && stackPre != stackPost)
        {
            System.out.println("slotClick() - updating container");
            UUID uuid = this.inventoryItemModular.getContainerUUID();
            this.inventoryItemModular.setContainerItemStack(InventoryUtils.getItemStackByUUID(this.inventoryPlayer, uuid, "UUID"));
            //this.inventoryItemModular.readFromContainerItemStack();
        }

        this.detectAndSendChanges();

        return stack;
    }
}
