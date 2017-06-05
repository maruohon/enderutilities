package fi.dy.masa.enderutilities.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.TileUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemDolly extends ItemEnderUtilities
{
    public ItemDolly()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_DOLLY);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
            EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if (world.isRemote == false)
        {
            if (this.isCarryingBlock(stack))
            {
                this.tryPlaceDownBlock(stack, player, world, pos, side);
            }
            else
            {
                this.tryPickUpBlock(stack, player, world, pos, side);
            }
        }

        return world.isRemote ? EnumActionResult.PASS : EnumActionResult.SUCCESS;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String nameBase = super.getItemStackDisplayName(stack);;
        String name = this.getCarriedBlockName(stack);

        if (name != null)
        {
            return nameBase + " - " + name;
        }

        return nameBase;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        String name = this.getCarriedBlockName(stack);

        if (name != null)
        {
            list.add(I18n.format("enderutilities.tooltip.item.carrying", name));
        }
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, "Carrying", false);

        if (tag != null)
        {
            NBTTagCompound newTagCarrying = new NBTTagCompound();
            newTagCarrying.setString("Block", tag.getString("Block"));

            if (tag.hasKey("DisplayName", Constants.NBT.TAG_STRING))
            {
                newTagCarrying.setString("DisplayName", tag.getString("DisplayName"));
            }

            NBTTagCompound newTag = new NBTTagCompound();
            newTag.setTag("Carrying", newTagCarrying);

            return newTag;
        }

        return null;
    }

    @Nullable
    private String getCarriedBlockName(ItemStack stack)
    {
        NBTTagCompound tag = NBTUtils.getCompoundTag(stack, "Carrying", false);

        if (tag != null)
        {
            String name;

            if (tag.hasKey("DisplayName", Constants.NBT.TAG_STRING))
            {
                name = tag.getString("DisplayName");
            }
            else
            {
                name = tag.getString("Block");
            }

            return name;
        }

        return null;
    }

    private boolean shouldTryToPickUpBlock(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
    {
        if (world.isBlockModifiable(player, pos) == false)
        {
            return false;
        }

        if (player.capabilities.isCreativeMode)
        {
            return true;
        }

        TileEntity te = world.getTileEntity(pos);

        // Unbreakable block, player not in creative mode
        if (world.getBlockState(pos).getBlockHardness(world, pos) < 0)
        {
            if ((te instanceof TileEntityEnderUtilitiesInventory) == false)
            {
                return false;
            }

            TileEntityEnderUtilitiesInventory teinv = (TileEntityEnderUtilitiesInventory) te;
            // From the unbreakable blocks in this mod, only allow moving non-Creative mode storage blocks.
            return teinv.isCreative() == false && teinv.isMovableBy(player);
        }

        return te instanceof TileEntityEnderUtilitiesInventory || te instanceof IInventory ||
               (te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side));
    }

    private boolean tryPickUpBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
    {
        if (this.isCarryingBlock(stack) || this.shouldTryToPickUpBlock(world, pos, side, player) == false)
        {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        TileEntity te = world.getTileEntity(pos);
        String name = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
        int meta = state.getBlock().getMetaFromState(state);
        NBTTagCompound tagCarrying = NBTUtils.getCompoundTag(stack, "Carrying", true);
        tagCarrying.setString("Block", name);
        tagCarrying.setByte("Meta", (byte) meta);
        tagCarrying.setByte("PickupFacing", (byte) EntityUtils.getHorizontalLookingDirection(player).getIndex());

        ItemStack stackBlock = state.getBlock().getPickBlock(state, EntityUtils.getRayTraceFromPlayer(world, player, false), world, pos, player);

        if (stackBlock != null)
        {
            tagCarrying.setString("DisplayName", stackBlock.getDisplayName());
        }

        if (te != null)
        {
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToNBT(tag);
            tagCarrying.setTag("te", tag);
        }

        BlockUtils.setBlockToAirWithoutSpillingContents(world, pos);

        return true;
    }

    private boolean tryPlaceDownBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
    {
        pos = pos.offset(side);

        if (this.isCarryingBlock(stack) == false || world.isBlockModifiable(player, pos) == false)
        {
            return false;
        }

        NBTTagCompound tagCarrying = NBTUtils.getCompoundTag(stack, "Carrying", false);
        String name = tagCarrying.getString("Block");
        int meta = tagCarrying.getByte("Meta");
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));

        try
        {
            if (block != null && block != Blocks.AIR && world.canBlockBePlaced(block, pos, false, side, null, null))
            {
                @SuppressWarnings("deprecation")
                IBlockState state = block.getStateFromMeta(meta);
                EnumFacing pickupFacing = EnumFacing.getFront(tagCarrying.getByte("PickupFacing"));
                EnumFacing currentFacing = EntityUtils.getHorizontalLookingDirection(player);
                Rotation rotation = PositionUtils.getRotation(pickupFacing, currentFacing);
                state = state.withRotation(rotation);

                if (world.setBlockState(pos, state))
                {
                    TileEntity te = world.getTileEntity(pos);

                    if (te != null && tagCarrying.hasKey("te", Constants.NBT.TAG_COMPOUND))
                    {
                        NBTTagCompound nbt = tagCarrying.getCompoundTag("te");
                        TileUtils.createAndAddTileEntity(world, pos, nbt, rotation, Mirror.NONE);
                    }

                    NBTUtils.removeCompoundTag(stack, null, "Carrying");
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            EnderUtilities.logger.warn("Failed to place down a block from the Dolly", e);
        }

        return false;
    }

    private boolean isCarryingBlock(ItemStack stack)
    {
        return NBTUtils.getCompoundTag(stack, "Carrying", false) != null;
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "carrying"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return ItemDolly.this.isCarryingBlock(stack) ? 1.0F : 0.0F;
            }
        });
    }
}
