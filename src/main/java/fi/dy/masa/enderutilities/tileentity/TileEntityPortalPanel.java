package fi.dy.masa.enderutilities.tileentity;

import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.gui.client.GuiPortalPanel;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerPortalPanel;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.wrapper.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal.PortalData;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.PortalFormer;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortalPanel extends TileEntityEnderUtilitiesInventory
{
    private final ItemHandlerWrapper inventoryWrapper;
    private final String[] targetDisplayNames = new String[9];
    private final int[] colors = new int[9];
    private int activeTargetId;
    private int portalTargetId;
    private String displayName = EUStringUtils.EMPTY;

    public TileEntityPortalPanel()
    {
        super(ReferenceNames.NAME_TILE_PORTAL_PANEL);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 16, 1, false, "Items", this);
        this.inventoryWrapper = new ItemHandlerWrapper(this.itemHandlerBase);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer(EntityPlayer player)
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerBase, this.inventoryWrapper);
    }

    public int getActiveTargetId()
    {
        return this.activeTargetId;
    }

    private TargetData getActiveTarget()
    {
        ItemStack stack = this.itemHandlerBase.getStackInSlot(this.getActiveTargetId());
        return stack.isEmpty() == false ? TargetData.getTargetFromItem(stack) : null;
    }

    public boolean targetIsPortal()
    {
        ItemStack stack = this.itemHandlerBase.getStackInSlot(this.getActiveTargetId());

        if (stack.isEmpty() == false && stack.getItem() == EnderUtilitiesItems.LINK_CRYSTAL)
        {
            return ((IModule) stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_PORTAL;
        }

        return false;
    }

    private OwnerData getOwner()
    {
        ItemStack stack = this.itemHandlerBase.getStackInSlot(this.getActiveTargetId());
        return stack.isEmpty() == false ? OwnerData.getOwnerDataFromItem(stack) : null;
    }

    public void setActiveTargetId(int target)
    {
        this.activeTargetId = MathHelper.clamp(target, 0, 7);
    }

    private int getPortalColor()
    {
        int targetId = this.getActiveTargetId();
        return this.getPortalColorForTargetId(targetId);
    }

    private int getDyeDamageFromItem(int targetId)
    {
        EnumDyeColor dye = this.getDyeForTargetId(targetId);
        return dye != null ? dye.getDyeDamage() : -1;
    }

    private int getPortalColorForTargetId(int targetId)
    {
        EnumDyeColor dye = this.getDyeForTargetId(targetId);
        return dye != null ? MapColor.getBlockColor(dye).colorValue : 0xFFFFFFFF;
    }

    @Nullable
    private EnumDyeColor getDyeForTargetId(int targetId)
    {
        // The large button in the center will take the color of the active target
        if (targetId == 8)
        {
            targetId = this.getActiveTargetId();
        }

        if (targetId >= 0 && targetId < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(targetId + 8);

            if (stack.isEmpty() == false)
            {
                return InventoryUtils.getDyeColorForItem(stack);
            }
        }

        return null;
    }

    private int getColorFromDyeDamage(int dyeMeta)
    {
        return dyeMeta >= 0 && dyeMeta <= 15 ? MapColor.getBlockColor(EnumDyeColor.byDyeDamage(dyeMeta)).colorValue : 0xFFFFFF;
    }

    public int getColor(int target)
    {
        return this.colors[MathHelper.clamp(target, 0, 8)];
    }

    private String getActiveName()
    {
        return this.getTargetName(this.getActiveTargetId());
    }

    private String getTargetName(int targetId)
    {
        if (targetId >= 0 && targetId <= 7)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(targetId);

            if (stack.isEmpty() == false)
            {
                return stack.getDisplayName();
            }
        }

        return EUStringUtils.EMPTY;
    }

    public String getPanelDisplayName()
    {
        return this.displayName;
    }

    public String getTargetDisplayName(int targetId)
    {
        if (targetId >= 0 && targetId <= 7)
        {
            String name = this.targetDisplayNames[targetId];
            return name != null ? name : EUStringUtils.EMPTY;
        }

        return EUStringUtils.EMPTY;
    }

    public void setTargetName(String name)
    {
        int id = this.getActiveTargetId();

        if (id >= 0 && id < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(id);

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

                this.itemHandlerBase.setStackInSlot(id, stack);
            }
        }
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setActiveTargetId(nbt.getByte("SelectedTarget"));
        this.portalTargetId = nbt.getByte("PortalTarget");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("SelectedTarget", (byte) this.activeTargetId);
        nbt.setByte("PortalTarget", (byte) this.portalTargetId);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setByte("s", (byte) this.activeTargetId);
        String name = this.getActiveName();

        if (StringUtils.isBlank(name) == false)
        {
            nbt.setString("n", name);
        }

        for (int i = 0; i < 9; i++)
        {
            nbt.setByte("mt" + i, (byte) this.getDyeDamageFromItem(i));
        }

        for (int i = 0; i < 8; i++)
        {
            nbt.setString("n" + i, this.getTargetName(i));
        }

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        this.activeTargetId = tag.getByte("s");
        this.displayName = tag.getString("n");

        for (int i = 0; i < 9; i++)
        {
            this.colors[i] = this.getColorFromDyeDamage(tag.getByte("mt" + i));
        }

        for (int i = 0; i < 8; i++)
        {
            if (tag.hasKey("n" + i, Constants.NBT.TAG_STRING))
            {
                this.targetDisplayNames[i] = tag.getString("n" + i);
            }
        }

        super.handleUpdateTag(tag);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 0 && element >= 0 && element < 8)
        {
            this.setActiveTargetId(element);
            this.markDirty();

            this.notifyBlockUpdate(this.getPos());
        }
    }

    private class ItemHandlerWrapper extends ItemHandlerWrapperSelectiveModifiable
    {
        public ItemHandlerWrapper(IItemHandlerModifiable baseHandler)
        {
            super(baseHandler);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack)
        {
            if (stack.isEmpty())
            {
                return false;
            }

            if (slot < 8)
            {
                if (stack.getItem() == EnderUtilitiesItems.LINK_CRYSTAL)
                {
                    int tier = ((IModule) stack.getItem()).getModuleTier(stack);

                    return tier == ItemLinkCrystal.TYPE_PORTAL ||
                          (tier == ItemLinkCrystal.TYPE_LOCATION && Configs.portalOnlyAllowsPortalTypeLinkCrystals == false);
                }
                else
                {
                    return false;
                }
            }

            return InventoryUtils.doesStackOreDictNameStartWith(stack, "dye");
        }
    }

    public void tryTogglePortal()
    {
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockEnderUtilities blockPanel = EnderUtilitiesBlocks.PORTAL_PANEL;
        BlockPos posFrame = posPanel.offset(world.getBlockState(posPanel).getValue(blockPanel.propFacing).getOpposite());

        PortalFormer portalFormer = new PortalFormer(world, posFrame, EnderUtilitiesBlocks.PORTAL_FRAME, EnderUtilitiesBlocks.PORTAL);
        PortalData data = new PortalData(this.getActiveTarget(), this.getOwner(), this.getPortalColor(), this.targetIsPortal());
        portalFormer.setPortalData(data);
        portalFormer.analyzePortal();
        boolean state = portalFormer.getPortalState();
        boolean recreate = this.activeTargetId != this.portalTargetId;

        // Portal was inactive
        if (state == false)
        {
            if (portalFormer.togglePortalState(false))
            {
                this.portalTargetId = this.activeTargetId;
                world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 0.5f, 1.0f);
            }
        }
        // Portal was active
        else if (portalFormer.togglePortalState(recreate))
        {
            // Portal was active but the target id has changed, so it was just updated
            if (recreate)
            {
                this.portalTargetId = this.activeTargetId;
                world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 0.5f, 1.0f);
            }
            // Portal was active and the target id hasn't changed, so it was shut down
            else
            {
                world.playSound(null, posPanel, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.4f, 0.85f);
            }
        }
    }

    @Override
    public ContainerPortalPanel getContainer(EntityPlayer player)
    {
        return new ContainerPortalPanel(player, this);
    }

    @Override
    public Object getGui(EntityPlayer player)
    {
        return new GuiPortalPanel(this.getContainer(player), this);
    }
}
