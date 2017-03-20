package fi.dy.masa.enderutilities.tileentity;

import org.apache.commons.lang3.StringUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.gui.client.GuiToolWorkstation;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.IModularInventoryHolder;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerToolWorkstation;
import fi.dy.masa.enderutilities.inventory.item.InventoryItemCallback;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
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

        this.compatibilityReadItemsFromPre_0_6_6_Format(nbt);
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

    private void compatibilityReadItemsFromPre_0_6_6_Format(NBTTagCompound nbt)
    {
        // Compatibility code for reading the two last slots from the old style combined 11-slot inventory
        if (nbt.getInteger("DataVersion") < 6600)
        {
            ItemStack stacks[] = new ItemStack[11];
            NBTUtils.readStoredItemsFromTag(nbt, stacks, "Items");

            if (this.itemHandlerToolSlot.getStackInSlot(0) == null && stacks[0] != null)
            {
                this.itemHandlerToolSlot.setStackInSlot(0, stacks[0]);
            }

            for (int slot = 0; slot < 8; slot++)
            {
                this.itemHandlerBase.setStackInSlot(slot, this.itemHandlerBase.getStackInSlot(slot + 1));
            }

            if (stacks[9] != null)
            {
                this.itemHandlerBase.setStackInSlot(8, stacks[9]);
            }

            if (this.itemHandlerRenameSlot.getStackInSlot(0) == null && stacks[10] != null)
            {
                this.itemHandlerRenameSlot.setStackInSlot(0, stacks[10]);
            }
        }
    }

    public void renameItem(String name)
    {
        ItemStack stack = this.itemHandlerRenameSlot.getStackInSlot(0);

        if (stack != null)
        {
            if (StringUtils.isBlank(name))
            {
                stack.clearCustomName();
            }
            else
            {
                stack.setStackDisplayName(name);
            }
        }
    }

    public String getItemName()
    {
        ItemStack stack = this.itemHandlerRenameSlot.getStackInSlot(0);

        if (stack != null)
        {
            return stack.getDisplayName();
        }

        return "";
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
            return stack == null || stack.getItem() instanceof IModular;
        }
    }

    private class ItemHandlerWrapperInstalledModules extends InventoryItemCallback
    {
        private final TileEntityToolWorkstation te;

        public ItemHandlerWrapperInstalledModules(IItemHandlerModifiable baseHandler, int invSize, boolean isRemote, TileEntityToolWorkstation te)
        {
            super(null, invSize, 1, false, isRemote, null, te, "Items");
            this.te = te;
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (super.isItemValidForSlot(slot, stack) == false || stack == null)
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
            if (toolStack != null && (toolStack.getItem() instanceof IModular))
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
            return stack == null ||
                    ((stack.getItem() instanceof IModule) &&
                      (UtilItemModular.moduleTypeEquals(stack, ModuleType.TYPE_INVALID) == false));
        }
    }

    @Override
    public ContainerToolWorkstation getContainer(EntityPlayer player)
    {
        return new ContainerToolWorkstation(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiToolWorkstation(getContainer(player), this);
    }
}
