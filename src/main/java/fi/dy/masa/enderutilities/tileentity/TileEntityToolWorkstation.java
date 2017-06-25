package fi.dy.masa.enderutilities.tileentity;

import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemCallback;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class TileEntityToolWorkstation extends TileEntityEnderUtilitiesInventory implements IModularInventoryHolder
{
    private final ItemStackHandlerTileEntity itemHandlerToolSlot;
    private final ItemStackHandlerTileEntity itemHandlerRenameSlot;
    private final IItemHandler itemHandlerWrapperToolSlot;
    private final IItemHandler itemHandlerWrapperModuleStorage;
    private InventoryItemCallback itemHandlerWrapperInstalledModules;

    public TileEntityToolWorkstation()
    {
        super(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);

        this.itemHandlerBase        = new ItemStackHandlerTileEntity(0, 9, this);
        this.itemHandlerToolSlot    = new ItemStackHandlerTileEntity(1, 1,  1, false, "ToolItems", this);
        this.itemHandlerRenameSlot  = new ItemStackHandlerTileEntity(2, 1, 64, false, "RenameItems", this);

        this.itemHandlerWrapperToolSlot = new ItemHandlerWrapperToolSlot(this.itemHandlerToolSlot);
        this.itemHandlerWrapperModuleStorage    = new ItemHandlerWrapperModuleStorage(this.itemHandlerBase);
        // itemHandlerExternal is left as null, because this block should not expose it's inventory externally
        this.initStorage(false);
    }

    private void initStorage(boolean isRemote)
    {
        this.itemHandlerWrapperInstalledModules = new ItemHandlerWrapperInstalledModules(this.itemHandlerToolSlot, 10, isRemote, this);
        this.itemHandlerWrapperInstalledModules.setContainerItemStack(this.getContainerStack());
    }

    @Override
    public void onLoad()
    {
        super.onLoad();

        this.initStorage(this.getWorld().isRemote);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return this.itemHandlerWrapperModuleStorage;
    }

    public IItemHandler getToolSlotInventory()
    {
        return this.itemHandlerWrapperToolSlot;
    }

    public IItemHandler getRenameSlotInventory()
    {
        return this.itemHandlerRenameSlot;
    }

    public IItemHandler getInstalledModulesInventory()
    {
        return this.itemHandlerWrapperInstalledModules;
    }

    @Override
    public ItemStack getContainerStack()
    {
        return this.itemHandlerToolSlot.getStackInSlot(0);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        // Tool inventory/slot
        if (inventoryId == 1)
        {
            this.itemHandlerWrapperInstalledModules.setContainerItemStack(this.getContainerStack());
        }
    }

    @Override
    protected void readItemsFromNBT(NBTTagCompound nbt)
    {
        super.readItemsFromNBT(nbt);

        this.itemHandlerToolSlot.deserializeNBT(nbt);
        this.itemHandlerRenameSlot.deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("DataVersion", DATA_VERSION);
        return nbt;
    }

    @Override
    public void writeItemsToNBT(NBTTagCompound nbt)
    {
        super.writeItemsToNBT(nbt);

        nbt.merge(this.itemHandlerToolSlot.serializeNBT());
        nbt.merge(this.itemHandlerRenameSlot.serializeNBT());
    }

    public void renameItem(String name)
    {
        ItemStack stack = this.itemHandlerRenameSlot.getStackInSlot(0);

        if (stack.isEmpty() == false)
        {
            if (StringUtils.isBlank(name))
            {
                stack.clearCustomName();
            }
            else
            {
                stack.setStackDisplayName(name);
            }

            this.itemHandlerRenameSlot.setStackInSlot(0, stack);
        }
    }

    public String getItemName()
    {
        ItemStack stack = this.itemHandlerRenameSlot.getStackInSlot(0);
        return stack.isEmpty() == false ? stack.getDisplayName() : EUStringUtils.EMPTY;
    }

    private class ItemHandlerWrapperToolSlot extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapperToolSlot(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            return stack.isEmpty() == false && stack.getItem() instanceof IModular;
        }
    }

    private class ItemHandlerWrapperInstalledModules extends InventoryItemCallback
    {
        private final TileEntityToolWorkstation te;

        public ItemHandlerWrapperInstalledModules(IItemHandlerModifiable baseHandler, int invSize, boolean isRemote, TileEntityToolWorkstation te)
        {
            super(null, invSize, 1, false, isRemote, te, "Items");
            this.te = te;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack.isEmpty() || super.isItemValidForSlot(slot, stack) == false)
            {
                return false;
            }

            if (stack.getItem() instanceof IModule)
            {
                ModuleType type = ((IModule) stack.getItem()).getModuleType(stack);
                return type != ModuleType.TYPE_INVALID && type == this.getModuleTypeForSlot(slot, this.te.getContainerStack());
            }

            return false;
        }

        /**
         * Finds the ModuleType of the given slot.
         */
        private ModuleType getModuleTypeForSlot(int slot, ItemStack toolStack)
        {
            if (toolStack.isEmpty() == false && (toolStack.getItem() instanceof IModular))
            {
                IModular iModular = (IModular) toolStack.getItem();
                int modules = 0;

                for (ModuleType moduleType : ModuleType.values())
                {
                    if (moduleType.equals(ModuleType.TYPE_INVALID))
                    {
                        continue;
                    }

                    int maxOfType = iModular.getMaxModules(toolStack, moduleType);

                    if (slot < modules + maxOfType)
                    {
                        return moduleType;
                    }

                    modules += maxOfType;
                }
            }

            return ModuleType.TYPE_INVALID;
        }
    }

    private class ItemHandlerWrapperModuleStorage extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapperModuleStorage(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            return stack.isEmpty() == false && (stack.getItem() instanceof IModule) &&
                   UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false;
        }
    }

    @Override
    public ContainerToolWorkstation getContainer(EntityPlayer player)
    {
        return new ContainerToolWorkstation(player, this);
    }

    @Override
    public Object getGui(EntityPlayer player)
    {
        return new GuiToolWorkstation(getContainer(player), this);
    }
}
