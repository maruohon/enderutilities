package fi.dy.masa.enderutilities.tileentity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import fi.dy.masa.enderutilities.block.BlockPortal;
import fi.dy.masa.enderutilities.gui.client.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiPortalPanel;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperContainer;
import fi.dy.masa.enderutilities.inventory.ItemHandlerWrapperSelectiveModifiable;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerTileEntity;
import fi.dy.masa.enderutilities.inventory.container.ContainerEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerPortalPanel;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TileEntityPortalPanel extends TileEntityEnderUtilitiesInventory
{
    private final ItemHandlerWrapper inventoryWrapper;
    private byte activeTargetId;
    private byte portalTargetId;
    private boolean active;
    private String displayName;
    private int[] colors = new int[9];

    public TileEntityPortalPanel()
    {
        super(ReferenceNames.NAME_TILE_PORTAL_PANEL);

        this.itemHandlerBase = new ItemStackHandlerTileEntity(0, 16, 1, false, "Items", this);
        this.inventoryWrapper = new ItemHandlerWrapper(this.itemHandlerBase);
    }

    @Override
    public IItemHandler getWrappedInventoryForContainer()
    {
        return new ItemHandlerWrapperContainer(this.itemHandlerBase, this.inventoryWrapper);
    }

    public int getActiveTargetId()
    {
        return this.activeTargetId;
    }

    private TargetData getActiveTarget()
    {
        int slot = this.getActiveTargetId();
        ItemStack stack = this.itemHandlerBase.getStackInSlot(slot);

        if (stack != null)
        {
            return TargetData.getTargetFromItem(stack);
        }

        return null;
    }

    public void setActiveTargetId(int target)
    {
        this.activeTargetId = (byte)MathHelper.clamp_int(target, 0, 7);
    }

    private int getColorFromItems(int target)
    {
        // The large button in the center will take the color of the active target
        if (target == 8)
        {
            target = this.activeTargetId;
        }

        if (target >= 0 && target < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(target + 8);

            if (stack != null && stack.getItem() == Items.DYE)
            {
                return EnumDyeColor.byDyeDamage(stack.getMetadata()).getMapColor().colorValue;
            }
        }

        return 0xFFFFFF;
    }

    public int getColor(int target)
    {
        target = MathHelper.clamp_int(target, 0, 8);
        return this.colors[target];
    }

    private String getActiveName()
    {
        if (this.activeTargetId >= 0 && this.activeTargetId < 8)
        {
            ItemStack stack = this.itemHandlerBase.getStackInSlot(this.activeTargetId);

            if (stack != null && stack.hasDisplayName())
            {
                return stack.getDisplayName();
            }
        }

        return null;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        this.setActiveTargetId(nbt.getByte("SelectedTarget"));
        this.portalTargetId = nbt.getByte("PortalTarget");
        this.active = nbt.getBoolean("Active");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        nbt.setByte("SelectedTarget", this.activeTargetId);
        nbt.setByte("PortalTarget", this.portalTargetId);
        nbt.setBoolean("Active", this.active);
    }

    @Override
    public NBTTagCompound getDescriptionPacketTag(NBTTagCompound nbt)
    {
        nbt = super.getDescriptionPacketTag(nbt);

        nbt.setByte("s", this.activeTargetId);
        String name = this.getActiveName();
        if (name != null)
        {
            nbt.setString("n", name);
        }

        for (int i = 0; i < 9; i++)
        {
            nbt.setInteger("c" + i, this.getColorFromItems(i));
        }

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
    {
        NBTTagCompound nbt = packet.getNbtCompound();

        this.activeTargetId = nbt.getByte("s");
        this.displayName = nbt.getString("n");

        for (int i = 0; i < 9; i++)
        {
            this.colors[i] = nbt.getInteger("c" + i);

            if (this.colors[i] == 0)
            {
                this.colors[i] = 0xFFFFFF;
            }
        }

        super.onDataPacket(net, packet);
    }

    @Override
    public void inventoryChanged(int inventoryId, int slot)
    {
        super.inventoryChanged(inventoryId, slot);

        IBlockState state = this.getWorld().getBlockState(this.getPos());
        this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        if (action == 0 && element >= 0 && element < 8)
        {
            this.setActiveTargetId(element);

            IBlockState state = this.getWorld().getBlockState(this.getPos());
            this.getWorld().notifyBlockUpdate(this.getPos(), state, state, 2);
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
            if (stack == null)
            {
                return false;
            }

            if (slot < 8)
            {
                return stack.getItem() == EnderUtilitiesItems.linkCrystal &&
                        ((IModule)stack.getItem()).getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION;
            }

            return stack.getItem() == Items.DYE;
        }
    }

    @Override
    public ContainerEnderUtilities getContainer(EntityPlayer player)
    {
        return new ContainerPortalPanel(player, this);
    }

    @Override
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiPortalPanel(this.getContainer(player), this);
    }

    public void tryTogglePortal()
    {
        if (this.active == false)
        {
            this.tryActivatePortal();
        }
        else if (this.activeTargetId != this.portalTargetId)
        {
            this.tryUpdatePortal();
        }
        else
        {
            this.tryDisablePortal();
        }
    }

    private boolean tryActivatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());
        boolean success = false;
        IBlockState statePortal = blockPortal.getDefaultState().withProperty(BlockPortal.FACING, EnumFacing.NORTH);
        TargetData destination = this.getActiveTarget();
        int color = this.getColorFromItems(8); // The active color

        if (destination == null || world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return false;
        }

        System.out.println("plop - activate");
        for (EnumFacing side : EnumFacing.values())
        {
            BlockPos pos = posFrame.offset(side);

            if (world.isAirBlock(pos))
            {
                world.setBlockState(pos, statePortal, 2);
                TileEntity te = world.getTileEntity(pos);

                if (te instanceof TileEntityPortal)
                {
                    ((TileEntityPortal) te).setDestination(destination);
                    ((TileEntityPortal) te).setColor(color);
                    success = true;
                }
            }
        }

        if (success)
        {
            world.playSound(null, posPanel, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.MASTER, 0.5f, 1.0f);
            this.active = true;
            this.portalTargetId = this.activeTargetId;
        }

        return success;
    }

    private boolean tryDisablePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());
        boolean success = false;

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return false;
        }

        System.out.println("plop - disable");
        //List<BlockPos> positions = this.getExistingPortalPositions(world, posFrame);
        //for (BlockPos pos : positions)
        //{
        for (EnumFacing side : EnumFacing.values())
        {
            BlockPos pos = posFrame.offset(side);
            if (world.getBlockState(pos).getBlock() == blockPortal)
            {
                world.setBlockToAir(pos);
                success = true;
            }
        }

        if (success)
        {
            world.playSound(null, posPanel, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.MASTER, 0.5f, 1.0f);
        }

        this.active = false;

        return success;
    }

    private void tryUpdatePortal()
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        World world = this.getWorld();
        BlockPos posPanel = this.getPos();
        BlockPos posFrame = posPanel.offset(this.getFacing().getOpposite());

        if (world.getBlockState(posFrame).getBlock() != blockFrame)
        {
            return;
        }

        TargetData destination = this.getActiveTarget();
        int color = this.getColorFromItems(8); // The active color

        System.out.println("plop - update");
        //List<BlockPos> positions = this.getExistingPortalPositions(world, posFrame);
        //for (BlockPos pos : positions)
        //{
        for (EnumFacing side : EnumFacing.values())
        {
            BlockPos pos = posFrame.offset(side);
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof TileEntityPortal)
            {
                ((TileEntityPortal) te).setDestination(destination);
                ((TileEntityPortal) te).setColor(color);
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
        }

        this.active = true;
        this.portalTargetId = this.activeTargetId;
    }

    private List<BlockPos> getExistingPortalPositions(World world, BlockPos posFrame)
    {
        Block blockPortal = EnderUtilitiesBlocks.blockPortal;
        List<BlockPos> positions = new ArrayList<BlockPos>();

        return positions;
    }

    private List<BlockPos> getPortalPositionsForCreation(World world, BlockPos posFrame)
    {
        Block blockFrame = EnderUtilitiesBlocks.blockFrame;
        List<BlockPos> positions = new ArrayList<BlockPos>();

        return positions;
    }
}
