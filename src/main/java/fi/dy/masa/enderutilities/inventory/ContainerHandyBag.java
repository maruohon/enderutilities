package fi.dy.masa.enderutilities.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;

public class ContainerHandyBag extends ContainerLargeStacks implements IContainerModularItem
{
    public final InventoryItemModular inventoryItemModular;

    private final InventoryItemCrafting craftMatrix;
    private final ItemStackHandlerBasic craftResult = new ItemStackHandlerBasic(1);

    public ContainerHandyBag(EntityPlayer player, ItemStack containerStack)
    {
        super(player, new InventoryItemModular(containerStack, player, true, ModuleType.TYPE_MEMORY_CARD_ITEMS));
        this.inventoryItemModular = (InventoryItemModular)this.inventory;
        this.inventoryItemModular.setHostInventory(new PlayerMainInvWrapper(player.inventory));
        // Dummy container itemstack of sticks so that we can use this same class
        // (items are valid to be inserted - we just don't actually save the contents anywhere now)
        this.craftMatrix = new InventoryItemCrafting(this, 2, 2, new ItemStack(Items.stick), false, player, null, "CraftMatrix");

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

        int playerArmorStart = this.inventorySlots.size();

        IItemHandlerModifiable inv = new PlayerArmorInvWrapper(this.inventoryPlayer);
        // Player armor slots
        posY = 15;
        for (int i = 0; i < 4; i++)
        {
            final int slotNum = i;
            this.addSlotToContainer(new SlotItemHandlerGeneric(inv, 3 - i, posX, posY + i * 18)
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

                @SideOnly(Side.CLIENT)
                @Override
                public String getSlotTexture()
                {
                    return ItemArmor.EMPTY_SLOT_NAMES[slotNum];
                }
            });
        }

        this.playerArmorSlots = new MergeSlotRange(playerArmorStart, 4);

        // Player crafting slots
        posX += 90;
        posY = 15;
        this.addSlotToContainer(new SlotItemHandlerCraftresult(this.player, this.craftMatrix, this.craftResult, 0, posX + 54, posY + 10));

        for (int i = 0; i < 2; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.craftMatrix, j + i * 2, posX + j * 18, posY + i * 18));
            }
        }
    }

    @Override
    protected void addCustomInventorySlots()
    {
        int customInvStart = this.inventorySlots.size();
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
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, i * 9 + j, xOff + j * 18, yOff + i * 18));
            }
        }

        if (this.getBagTier() == 1)
        {
            int xOffXtra = 8;
            yOff = 102;

            // Left side extra slots
            for(int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 27 + i * 2, xOffXtra +  0, yOff + i * 18));
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 28 + i * 2, xOffXtra + 18, yOff + i * 18));
            }

            xOffXtra = 214;
            // Right side extra slots
            for(int i = 0; i < 7; i++)
            {
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 41 + i * 2, xOffXtra +  0, yOff + i * 18));
                this.addSlotToContainer(new SlotItemHandlerGeneric(this.inventory, 42 + i * 2, xOffXtra + 18, yOff + i * 18));
            }
        }

        this.customInventorySlots = new MergeSlotRange(customInvStart, this.inventorySlots.size() - customInvStart);

        xOff += 90;
        yOff = 69;
        int moduleSlots = this.inventoryItemModular.getModuleInventory().getSlots();
        // Add the Memory Card slots as a priority merge slot range
        this.addMergeSlotRangePlayerToExt(this.inventorySlots.size(), moduleSlots);

        // The Storage Module slots
        for (int i = 0; i < moduleSlots; i++)
        {
            this.addSlotToContainer(new SlotModuleModularItem(this.inventoryItemModular.getModuleInventory(), i, xOff + i * 18, yOff, ModuleType.TYPE_MEMORY_CARD_ITEMS, this));
        }
    }

    @Override
    public ItemStack getModularItem()
    {
        return this.inventoryItemModular.getModularItemStack();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inv)
    {
        this.craftResult.setStackInSlot(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.player.worldObj));

        this.detectAndSendChanges();
    }

    public void dropCraftingGridContents()
    {
        for (int i = 0; i < 4; ++i)
        {
            ItemStack stack = this.craftMatrix.removeStackFromSlot(i);

            if (stack != null)
            {
                player.dropPlayerItemWithRandomChoice(stack, false);
            }
        }

        this.craftResult.setStackInSlot(0, (ItemStack)null);
    }

    public int getBagTier()
    {
        if (this.inventoryItemModular.getModularItemStack() != null)
        {
            return this.inventoryItemModular.getModularItemStack().getItemDamage() == 1 ? 1 : 0;
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
    }

    @Override
    public ItemStack slotClick(int slotNum, int key, int type, EntityPlayer player)
    {
        ItemStack modularStackPre = this.inventoryItemModular.getModularItemStack();

        ItemStack stack = super.slotClick(slotNum, key, type, player);

        ItemStack modularStackPost = this.inventoryItemModular.getModularItemStack();

        // The Bag's stack changed after the click, re-read the inventory contents.
        if (modularStackPre != modularStackPost)
        {
            //System.out.println("slotClick() - updating container");
            this.inventoryItemModular.readFromContainerItemStack();
        }

        this.detectAndSendChanges();

        return stack;
    }
}
