package fi.dy.masa.enderutilities.item;

import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IRegistry;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Maps;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelBlock;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelFactory;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBucket extends ItemLocationBoundModular implements IKeyBound, IFluidContainerItem
{
    public static final double ENDER_CHARGE_COST = 0.2d; // charge cost per 1 mB of fluid transferred to/from a linked tank
    public static final byte OPERATION_MODE_NORMAL = 0;
    public static final byte OPERATION_MODE_FILL_BUCKET = 1;
    public static final byte OPERATION_MODE_DRAIN_BUCKET = 2;
    public static final byte OPERATION_MODE_BINDING = 3;
    public static final byte LINK_MODE_DISABLED = 0;
    public static final byte LINK_MODE_ENABLED = 1;
    public static final int ENDER_BUCKET_MAX_AMOUNT = 16000; // Can contain 16 buckets

    protected int capacity;

    @SideOnly(Side.CLIENT)
    public ModelBlock modelBlocks[];

    public ItemEnderBucket()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BUCKET);
        this.setCapacity(Configs.enderBucketCapacity.getInt(ENDER_BUCKET_MAX_AMOUNT));
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        // Do nothing on the client side
        if (world.isRemote == true || (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED
                && NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false))
        {
            return false;
        }

        if (this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack)) == true)
        {
            return true;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof IFluidHandler)
        {
            // If we are in bind mode, bind the bucket to the targeted tank and then return
            if (this.getBucketMode(stack) == OPERATION_MODE_BINDING)
            {
                super.onItemUse(stack, player, world, pos, face, hitX, hitY, hitZ);
                return true;
            }

            this.useBucketOnTank(stack, player, world, pos, face, this.getBucketMode(stack));
            return true;
        }

        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return true;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED && NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return false;
        }

        // First try to use the bucket on a fluid block, if any.
        // If that fails (not targeting fluid), then we use it on a block (see below).
        if (this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack)) == true)
        {
            return true;
        }

        return this.useBucketOnBlock(stack, player, world, pos, face, hitX, hitY, hitZ, this.getBucketMode(stack));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        // Do nothing on the client side
        if (world.isRemote == true || (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED
                && NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false))
        {
            return stack;
        }

        this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack));
        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = this.getFluidCached(stack);

        if (fluidStack != null && fluidStack.amount > 0 && fluidStack.getFluid() != null)
        {
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();
            String fluidName = EnumChatFormatting.GREEN.toString() + fluidStack.getFluid().getLocalizedName(fluidStack) + rst;
            return StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim() + " " + fluidName;
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
            return;
        }

        FluidStack fluidStack = this.getFluidCached(stack);
        String fluidName;
        String preNr = EnumChatFormatting.BLUE.toString();
        String preTxt = EnumChatFormatting.DARK_GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        int amount = 0;
        int capacity = this.getCapacityCached(stack, player);

        if (fluidStack != null && fluidStack.getFluid() != null)
        {
            amount = fluidStack.amount;
            fluidName = preTxt + fluidStack.getFluid().getLocalizedName(fluidStack) + rst;
        }
        else
        {
            fluidName = StatCollector.translateToLocal("enderutilities.tooltip.item.empty");
        }

        byte mode = this.getBucketMode(stack);
        byte linkMode = this.getBucketLinkMode(stack);

        String amountStr = String.format("%s%s%s mB / %s%s%s mB", preNr, EUStringUtils.formatNumberWithKSeparators(amount), rst, preNr, EUStringUtils.formatNumberWithKSeparators(capacity), rst);
        String modeStr;
        if (mode == OPERATION_MODE_NORMAL) { modeStr = "enderutilities.tooltip.item.bucket.mode.normal"; }
        else if (mode == OPERATION_MODE_FILL_BUCKET) { modeStr = "enderutilities.tooltip.item.bucket.mode.fill"; }
        else if (mode == OPERATION_MODE_DRAIN_BUCKET) { modeStr = "enderutilities.tooltip.item.bucket.mode.drain"; }
        else if (mode == OPERATION_MODE_BINDING) { modeStr = "enderutilities.tooltip.item.bucket.mode.bind"; }
        else { modeStr = ""; }

        if (verbose == true)
        {
            if (linkMode == LINK_MODE_ENABLED)
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.cached.fluid") + ": " + fluidName);
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.cached.amount") + ": " + amountStr);
            }
            else
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.fluid") + ": " + fluidName);
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.amount") + ": " + amountStr);
            }
        }
        else
        {
            if (linkMode == LINK_MODE_ENABLED)
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.cached.fluid.compact") + ": " + fluidName + " - " + amountStr);
            }
            else
            {
                list.add(fluidName + " - " + amountStr);
            }
        }

        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + StatCollector.translateToLocal(modeStr));

        if (linkMode == LINK_MODE_ENABLED)
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
        }
    }

    public byte getBucketMode(ItemStack stack)
    {
        if (stack != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey("Mode") == true)
            {
                byte mode = nbt.getByte("Mode");
                if (mode >= OPERATION_MODE_NORMAL && mode <= OPERATION_MODE_BINDING)
                {
                    return mode;
                }
            }
        }

        return OPERATION_MODE_NORMAL;
    }

    /**
     *  Returns whether the bucket is currently set to link to a tank.
     */
    public byte getBucketLinkMode(ItemStack stack)
    {
        if (stack != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey("Linked") == true)
            {
                byte mode = nbt.getByte("Linked");
                if (mode == LINK_MODE_DISABLED || mode == LINK_MODE_ENABLED)
                {
                    return mode;
                }
            }
        }

        return LINK_MODE_DISABLED;
    }

    /**
     * Checks if the player can edit the target block
     */
    public boolean isTargetUsable(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face)
    {
        if (world == null)
        {
            return false;
        }

        Block targetBlock = world.getBlockState(pos).getBlock();
        // Spawn safe zone checks etc.
        if (targetBlock == null || targetBlock.getMaterial() == null
            || world.canMineBlockBody(player, pos) == false || player.canPlayerEdit(pos, face, stack) == false) // TODO 1.8 update: canMineBlock -> canMineBlockBody, (or isBlockModifiable?)
        {
            return false;
        }

        return true;
    }

    public boolean useBucketOnTank(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, byte bucketMode)
    {
        if (this.isTargetUsable(stack, player, world, pos, face) == false)
        {
            return false;
        }

        this.setCapacity(Configs.enderBucketCapacity.getInt(ENDER_BUCKET_MAX_AMOUNT));
        TileEntity te = world.getTileEntity(pos);

        // Is this a TileEntity that is also some sort of a fluid storage device?
        if (te != null && te instanceof IFluidHandler)
        {
            IFluidHandler iFluidHandler = (IFluidHandler)te;
            FluidStack fluidStack;
            String blockName = Block.blockRegistry.getNameForObject(world.getBlockState(pos).getBlock()).toString();

            // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
            // work if we target a blue (= input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
            // but the top side should ever only be blue aka. input.
            if (blockName != null && blockName.equals("ThermalExpansion:Tank"))
            {
                face = EnumFacing.UP;
            }

            // Get the stored fluid, if any
            FluidStack storedFluidStack = this.getFluidWorker(stack, player);
            int storedFluidAmount = 0;

            if (storedFluidStack != null)
            {
                storedFluidAmount = storedFluidStack.amount;
            }

            // With tanks we pick up fluid when not sneaking
            if (bucketMode == OPERATION_MODE_FILL_BUCKET || (bucketMode == OPERATION_MODE_NORMAL && player.isSneaking() == false))
            {
                fluidStack = iFluidHandler.drain(face, FluidContainerRegistry.BUCKET_VOLUME, false); // simulate
                int amount = this.getCapacityAvailable(stack, fluidStack, player);

                // We can still store more fluid
                if (amount > 0)
                {
                    if (amount > FluidContainerRegistry.BUCKET_VOLUME)
                    {
                        amount = FluidContainerRegistry.BUCKET_VOLUME;
                    }

                    // If the bucket is currently empty, or the tank's fluid is the same we currently have
                    if (fluidStack != null && (storedFluidAmount == 0 || fluidStack.isFluidEqual(storedFluidStack) == true))
                    {
                        fluidStack = iFluidHandler.drain(face, amount, false);
                        if (fluidStack != null && this.fillWorker(stack, fluidStack, false, player) == fluidStack.amount)
                        {
                            fluidStack = iFluidHandler.drain(face, amount, true); // actually drain
                            this.fillWorker(stack, fluidStack, true, player);
                            return true;
                        }
                    }
                }
            }
            // Sneaking or in drain-only mode, try to drain fluid from the bucket to the tank
            else
            {
                // Some fluid stored (we allow depositing less than a buckets worth of fluid into _tanks_)
                if (storedFluidAmount > 0)
                {
                    // simulate, we try to deposit up to one bucket per use
                    fluidStack = this.drainWorker(stack, FluidContainerRegistry.BUCKET_VOLUME, false, player);

                    // Check if we can deposit (at least some) the fluid we have stored
                    if (fluidStack != null && iFluidHandler.fill(face, fluidStack, false) > 0) // simulate
                    {
                        int amount = iFluidHandler.fill(face, fluidStack, true);
                        this.drainWorker(stack, amount, true, player); // actually drain fluid from the bucket (the amount that was filled into the tank)
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean useBucketOnBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ, byte bucketMode)
    {
        if (this.isTargetUsable(stack, player, world, pos, face) == false)
        {
            return false;
        }

        this.setCapacity(Configs.enderBucketCapacity.getInt(ENDER_BUCKET_MAX_AMOUNT));

        // Non-fluid block, adjust the target block position to be the block touching the side we targeted
        pos = pos.offset(face);

        // Get the stored fluid, if any
        FluidStack storedFluidStack = this.getFluidWorker(stack, player);
        int storedFluidAmount = 0;

        if (storedFluidStack != null)
        {
            storedFluidAmount = storedFluidStack.amount;
        }

        if (world.getBlockState(pos).getBlock().getMaterial().isLiquid() == true)
        {
            // Note: the side is technically wrong unless we ray trace it again, but it won't matter with fluid blocks... right?
            return this.useBucketOnFluidBlock(stack, world, player, pos, face, bucketMode);
        }
        else
        {
            // target block is not fluid, try to place a fluid block in world in the adjusted block position
            if (storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
            {
                FluidStack fs = this.drainWorker(stack, FluidContainerRegistry.BUCKET_VOLUME, false, player);
                if (fs != null && fs.amount == FluidContainerRegistry.BUCKET_VOLUME && this.tryPlaceFluidBlock(world, pos, storedFluidStack) == true)
                {
                    this.drainWorker(stack, FluidContainerRegistry.BUCKET_VOLUME, true, player);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean useBucketOnFluidBlock(ItemStack stack, World world, EntityPlayer player, byte bucketMode)
    {
        // First find out what block we are targeting
        // FIXME the boolean flag does what exactly? In vanilla it seems to indicate that the bucket is empty.
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
        {
            return false;
        }

        return this.useBucketOnFluidBlock(stack, world, player, mop.getBlockPos(), mop.sideHit, bucketMode);
    }

    public boolean useBucketOnFluidBlock(ItemStack stack, World world, EntityPlayer player, BlockPos pos, EnumFacing face, byte bucketMode)
    {
        IBlockState iBlockState = world.getBlockState(pos);
        Block targetBlock = iBlockState.getBlock();

        // Spawn safe zone checks etc.
        if (this.isTargetUsable(stack, player, world, pos, face) == false || targetBlock.getMaterial().isLiquid() == false)
        {
            return false;
        }

        // Flowing fluid does not get detected by ray tracing
        /*if (targetBlock.getMaterial().isLiquid() == false)
        {
            ForgeDirection dir = ForgeDirection.getOrientation(side);
            x += dir.offsetX;
            y += dir.offsetY;
            z += dir.offsetZ;
            targetBlock = world.getBlock(x, y, z);
        }*/

        // Get the stored fluid, if any
        FluidStack storedFluidStack = this.getFluidWorker(stack, player);
        FluidStack targetFluidStack = null;
        IFluidBlock iFluidBlock = null;
        int storedFluidAmount = 0;

        if (storedFluidStack != null)
        {
            storedFluidAmount = storedFluidStack.amount;
        }

        if (targetBlock instanceof IFluidBlock)
        {
            iFluidBlock = (IFluidBlock)targetBlock;
            targetFluidStack = iFluidBlock.drain(world, pos, false); // simulate
        }
        else
        {
            // We need to convert flowing water and lava to the still variant for logic stuffs
            // We will always convert them to the flowing variant before placing
            if (targetBlock == Blocks.flowing_water) { targetBlock = Blocks.water; }
            else if (targetBlock == Blocks.flowing_lava) { targetBlock = Blocks.lava; }

            Fluid fluid = FluidRegistry.lookupFluidForBlock(targetBlock);
            if (fluid != null)
            {
                targetFluidStack = FluidRegistry.getFluidStack(fluid.getName(), FluidContainerRegistry.BUCKET_VOLUME);
            }
        }

        // Not in drain-only mode && (Empty || (space && same fluid && (not sneaking || fill-only mode))) => trying to pick up fluid
        if (bucketMode != OPERATION_MODE_DRAIN_BUCKET
            && (storedFluidAmount == 0 || (this.getCapacityAvailable(stack, targetFluidStack, player) >= FluidContainerRegistry.BUCKET_VOLUME && storedFluidStack.isFluidEqual(targetFluidStack) &&
            (player.isSneaking() == false || bucketMode == OPERATION_MODE_FILL_BUCKET))))
        {
            // Implements IFluidBlock
            if (iFluidBlock != null)
            {
                if (iFluidBlock.canDrain(world, pos) == true)
                {
                    targetFluidStack = iFluidBlock.drain(world, pos, false); // simulate

                    // Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
                    if (targetFluidStack != null && this.fillWorker(stack, targetFluidStack, false, player) == targetFluidStack.amount)
                    {
                        targetFluidStack = iFluidBlock.drain(world, pos, true);
                        this.fillWorker(stack, targetFluidStack, true, player);

                        return true;
                    }
                }

                return false;
            }

            // Does not implement IFluidBlock

            int meta = targetBlock.getMetaFromState(iBlockState);
            // Check that the fluid block we are trying to pick up is a source block, and that we can store that amount
            if (targetFluidStack != null && meta == 0 && this.fillWorker(stack, targetFluidStack, false, player) == targetFluidStack.amount)
            {
                if (world.setBlockToAir(pos) == true)
                {
                    this.fillWorker(stack, targetFluidStack, true, player);

                    return true;
                }
            }
        }

        // Fluid stored and not in fill-only mode, try to place fluid
        if (storedFluidStack != null && storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
        {
            // (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
            // The meta check is for ignoring flowing fluid blocks (ie. non-source blocks)
            int meta = targetBlock.getMetaFromState(iBlockState);
            if (storedFluidStack.isFluidEqual(targetFluidStack) == false || player.isSneaking() == true || meta != 0)
            {
                FluidStack fs = this.drainWorker(stack, FluidContainerRegistry.BUCKET_VOLUME, false, player);
                if (fs != null && fs.amount == FluidContainerRegistry.BUCKET_VOLUME && this.tryPlaceFluidBlock(world, pos, storedFluidStack) == true)
                {
                    this.drainWorker(stack, FluidContainerRegistry.BUCKET_VOLUME, true, player);
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    /**
     *  Attempts to place one fluid block in the world, identified by the given FluidStack
     */
    public boolean tryPlaceFluidBlock(World world, BlockPos pos, FluidStack fluidStack)
    {
        if (fluidStack == null || fluidStack.getFluid() == null || fluidStack.getFluid().canBePlacedInWorld() == false)
        {
            return false;
        }

        Block block = fluidStack.getFluid().getBlock();

        // We need to convert water and lava to the flowing variant, otherwise we get non-flowing source blocks
        if (block == Blocks.water) { block = Blocks.flowing_water; }
        else if (block == Blocks.lava) { block = Blocks.flowing_lava; }

        Material material = world.getBlockState(pos).getBlock().getMaterial();

        if (world.isAirBlock(pos) == false && material.isSolid() == true)
        {
            return false;
        }

        if (world.provider.doesWaterVaporize() == true && block == Blocks.flowing_water)
        {
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            world.playSoundEffect(x + 0.5d, y + 0.5d, z + 0.5d, "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l)
            {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }
        else
        {
            if (world.isRemote == false && material.isSolid() == false && material.isLiquid() == false)
            {
                // Set a replaceable block to air, and drop the items
                world.destroyBlock(pos, true);
            }

            world.setBlockState(pos, block.getStateFromMeta(0), 3);
        }

        return true;
    }

    public ItemEnderBucket setCapacity(int capacity)
    {
        this.capacity = capacity;

        return this;
    }

    public int getCapacityCached(ItemStack stack, EntityPlayer player)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
            if (moduleStack != null)
            {
                NBTTagCompound moduleNbt = moduleStack.getTagCompound();
                if (moduleNbt != null && moduleNbt.hasKey("CapacityCached", Constants.NBT.TAG_INT) == true)
                {
                    return moduleNbt.getInteger("CapacityCached");
                }
            }

            return 0; // without this the client side code would try to read the tank info, and then crash
        }

        return this.getCapacityWorker(stack, player);
    }

    public int getCapacityAvailable(ItemStack stack, FluidStack fluidStackIn, EntityPlayer player)
    {
        FluidStack fluidStack;

        // Linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidTankInfo[] info = tank.getTankInfo(targetData.facing);
                fluidStack = tank.drain(targetData.facing, Integer.MAX_VALUE, false);

                // Tank has fluid
                if (fluidStack != null)
                {
                    FluidStack fs;

                    if (fluidStackIn != null)
                    {
                        if (fluidStack.isFluidEqual(fluidStackIn) == false)
                        {
                            return 0;
                        }

                        fs = fluidStackIn.copy();
                    }
                    else
                    {
                        fs = fluidStack.copy();
                    }

                    if (info != null && info[0] != null)
                    {
                        return info[0].capacity - fluidStack.amount;
                    }

                    fs.amount = Integer.MAX_VALUE;

                    return tank.fill(targetData.facing, fs, false);
                }
                // Tank has no fluid
                else
                {
                    if (info != null && info[0] != null)
                    {
                        return info[0].capacity;
                    }

                    if (fluidStackIn != null)
                    {
                        FluidStack fs = fluidStackIn.copy();
                        fs.amount = Integer.MAX_VALUE;

                        return tank.fill(targetData.facing, fs, false);
                    }

                    // Since we have no fluid stored, get the capacity via simulating filling water into the tank
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(Blocks.water);
                    if (fluid != null)
                    {
                        fluidStack = FluidRegistry.getFluidStack(fluid.getName(), Integer.MAX_VALUE);
                        if (fluidStack != null)
                        {
                            return tank.fill(targetData.facing, fluidStack, false);
                        }
                    }
                }
            }

            return 0;
        }

        // Not linked to a tank, get the bucket's own free capacity
        fluidStack = this.getFluidWorker(stack, player);
        if (fluidStack != null)
        {
            return this.getCapacityWorker(stack, player) - fluidStack.amount;
        }

        return this.getCapacityWorker(stack, player);
    }

    public void cacheFluid(ItemStack stack, FluidStack fluidStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null)
        {
            NBTTagCompound moduleNbt = moduleStack.getTagCompound();
            if (moduleNbt == null)
            {
                moduleNbt = new NBTTagCompound();
            }

            if (fluidStack != null)
            {
                moduleNbt.setTag("FluidCached", fluidStack.writeToNBT(new NBTTagCompound()));
            }
            else
            {
                moduleNbt.removeTag("FluidCached");
            }

            moduleStack.setTagCompound(moduleNbt);
            this.setSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL, moduleStack);
        }

        this.cacheCapacity(stack);
    }

    public void cacheCapacity(ItemStack stack)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
            if (moduleStack != null)
            {
                NBTTagCompound moduleNbt = moduleStack.getTagCompound();
                if (moduleNbt == null)
                {
                    return;
                }

                IFluidHandler tank = this.getLinkedTank(stack);
                NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
                if (tank != null && targetData != null)
                {
                    FluidTankInfo[] info = tank.getTankInfo(targetData.facing);
                    if (info != null && info[0] != null)
                    {
                        moduleNbt.setInteger("CapacityCached", info[0].capacity);
                    }
                    else
                    {
                        moduleNbt.setInteger("CapacityCached", 0);
                    }
                }
                else
                {
                    moduleNbt.removeTag("CapacityCached");
                }

                moduleStack.setTagCompound(moduleNbt);
                this.setSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL, moduleStack);
            }
        }
    }

    public NBTHelperTarget getLinkedTankTargetData(ItemStack stack)
    {
        NBTHelperTarget targetData = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (targetData == null)
        {
            return null;
        }

        // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
        // work if we target a blue (=input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
        // but the top side should ever only be blue aka. input.
        if ("ThermalExpansion:Tank".equals(targetData.blockName))
        {
            targetData.facing = EnumFacing.UP;
        }

        return targetData;
    }

    public IFluidHandler getLinkedTank(ItemStack stack)
    {
        NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
        if (targetData == null || MinecraftServer.getServer() == null)
        {
            return null;
        }

        World world = MinecraftServer.getServer().worldServerForDimension(targetData.dimension);
        if (world == null)
        {
            return null;
        }

        // Force load the target chunk where the tank is located with a 30 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithModTicket(targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 30) == false)
        {
            return null;
        }

        TileEntity te = world.getTileEntity(targetData.pos);
        if (te == null || (te instanceof IFluidHandler) == false)
        {
            return null;
        }

        return (IFluidHandler)te;
    }

    public FluidStack getFluidCached(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            return null;
        }

        // The Bucket has been linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
            if (moduleStack != null && moduleStack.getTagCompound() != null)
            {
                if (moduleStack.getTagCompound().hasKey("FluidCached", Constants.NBT.TAG_COMPOUND) == true)
                {
                    return FluidStack.loadFluidStackFromNBT(moduleStack.getTagCompound().getCompoundTag("FluidCached"));
                }
            }

            return null;
        }

        // Not linked to a tank, get the internal FluidStack
        if (nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == true)
        {
            return FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));
        }

        return null;
    }

    @Override
    public int getCapacity(ItemStack stack)
    {
        return this.getCapacityWorker(stack, null);
    }

    private int getCapacityWorker(ItemStack stack, EntityPlayer player)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_DISABLED)
        {
            return this.capacity;
        }

        // Linked to a tank

        if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return 0;
        }

        FluidStack fluidStack;
        NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
        IFluidHandler tank = this.getLinkedTank(stack);

        if (targetData != null && tank != null)
        {
            FluidTankInfo[] info = tank.getTankInfo(targetData.facing);

            // If we have tank info, it is the easiest and simplest way to get the tank capacity
            if (info != null && info[0] != null)
            {
                return info[0].capacity;
            }

            // No tank info available, get the capacity via simulating filling

            fluidStack = tank.drain(targetData.facing, Integer.MAX_VALUE, false);

            // Tank has fluid
            if (fluidStack != null)
            {
                FluidStack fs = fluidStack.copy();
                fs.amount = Integer.MAX_VALUE;
                int space = tank.fill(targetData.facing, fs, false);

                return space + fluidStack.amount;
            }
            // Tank has no fluid
            else
            {
                // Since we have no fluid stored, get the capacity via simulating filling water into the tank
                Fluid fluid = FluidRegistry.lookupFluidForBlock(Blocks.water);
                if (fluid != null)
                {
                    fluidStack = FluidRegistry.getFluidStack(fluid.getName(), Integer.MAX_VALUE);
                    if (fluidStack != null)
                    {
                        return tank.fill(targetData.facing, fluidStack, false);
                    }
                }
            }
        }

        return 0;
    }

    @Override
    public FluidStack getFluid(ItemStack stack)
    {
        return this.getFluidWorker(stack, null);
    }

    private FluidStack getFluidWorker(ItemStack stack, EntityPlayer player)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            return null;
        }

        // The Bucket has been linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return null;
            }

            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidStack fluidStack = tank.drain(targetData.facing, Integer.MAX_VALUE, false);

                // Cache the fluid stack into the link crystal's NBT for easier/faster access for tooltip and rendering stuffs
                this.cacheFluid(stack, fluidStack);

                return fluidStack;
            }

            return null;
        }

        // Not linked to a tank, get the internal FluidStack
        if (nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == true)
        {
            return FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));
        }

        return null;
    }

    @Override
    public FluidStack drain(ItemStack stack, int maxDrain, boolean doDrain)
    {
        return this.drainWorker(stack, maxDrain, doDrain, null);
    }

    private FluidStack drainWorker(ItemStack stack, int maxDrain, boolean doDrain, EntityPlayer player)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return null;
            }

            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                if (UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * maxDrain), doDrain) == false)
                {
                    return null;
                }

                FluidStack fluidStack = tank.drain(targetData.facing, maxDrain, doDrain);
                this.cacheFluid(stack, tank.drain(targetData.facing, Integer.MAX_VALUE, false));

                return fluidStack;
            }

            return null;
        }

        int drained = 0;

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == false)
        {
            return null;
        }

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));
        if (fluidStack == null)
        {
            return null;
        }

        // Amount that will or would be drained
        drained = Math.min(fluidStack.amount, maxDrain);

        // If not just simulating
        if (doDrain == true)
        {
            // Drained all the fluid
            if (drained >= fluidStack.amount)
            {
                nbt.removeTag("Fluid");
                if (nbt.hasNoTags() == true)
                {
                    stack.setTagCompound(null);
                }
            }
            else
            {
                NBTTagCompound fluidTag = nbt.getCompoundTag("Fluid");
                fluidTag.setInteger("Amount", fluidTag.getInteger("Amount") - drained);
                nbt.setTag("Fluid", fluidTag);
            }
        }

        fluidStack.amount = drained;

        return fluidStack; // Return the FluidStack that was or would be drained from the item
    }

    @Override
    public int fill(ItemStack stack, FluidStack fluidStackIn, boolean doFill)
    {
        return this.fillWorker(stack, fluidStackIn, doFill, null);
    }

    private int fillWorker(ItemStack stack, FluidStack fluidStackIn, boolean doFill, EntityPlayer player)
    {
        if (fluidStackIn == null)
        {
            return 0;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return 0;
            }

            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                if (fluidStackIn != null && UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * fluidStackIn.amount), doFill) == false)
                {
                    return 0;
                }

                int amount = tank.fill(targetData.facing, fluidStackIn, doFill);
                this.cacheFluid(stack, tank.drain(targetData.facing, Integer.MAX_VALUE, false));

                return amount;
            }

            return 0;
        }

        int capacityAvailable = this.getCapacityAvailable(stack, fluidStackIn, player);
        NBTTagCompound nbt = stack.getTagCompound();

        if (doFill == false)
        {
            if (nbt == null || nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == false)
            {
                return Math.min(capacityAvailable, fluidStackIn.amount);
            }

            FluidStack storedFluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));

            if (storedFluidStack == null)
            {
                return Math.min(capacityAvailable, fluidStackIn.amount);
            }

            if (storedFluidStack.isFluidEqual(fluidStackIn) == false)
            {
                return 0;
            }

            return Math.min(capacityAvailable, fluidStackIn.amount);
        }

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        if (nbt.hasKey("Fluid") == false)
        {
            NBTTagCompound fluidTag = fluidStackIn.writeToNBT(new NBTTagCompound());

            if (capacityAvailable < fluidStackIn.amount)
            {
                fluidTag.setInteger("Amount", capacityAvailable);
                nbt.setTag("Fluid", fluidTag);

                return capacityAvailable;
            }

            nbt.setTag("Fluid", fluidTag);

            return fluidStackIn.amount;
        }

        NBTTagCompound fluidTag = nbt.getCompoundTag("Fluid");
        FluidStack storedFluidStack = FluidStack.loadFluidStackFromNBT(fluidTag);

        if (storedFluidStack.isFluidEqual(fluidStackIn) == false)
        {
            return 0;
        }

        int filled = 0;
        if (fluidStackIn.amount < capacityAvailable)
        {
            storedFluidStack.amount += fluidStackIn.amount;
            filled = fluidStackIn.amount;
        }
        else
        {
            storedFluidStack.amount += capacityAvailable;
        }

        nbt.setTag("Fluid", storedFluidStack.writeToNBT(fluidTag));

        return filled;
    }

    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Only allow the block/inventory type Link Crystals
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == false || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
        {
            return this.getMaxModules(toolStack, moduleType);
        }

        return 0;
    }

    /**
     * Toggles the linked-to-tank mode ON/OFF.
     */
    private void changeLinkMode(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        nbt.setBoolean("Linked", ! nbt.getBoolean("Linked"));
        stack.setTagCompound(nbt);
    }

    /**
     * Cycles through the operation modes: Normal, Fill, Drain, Bind to tanks
     */
    private void changeOperationMode(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        // 0: Normal, 1: Pickup only, 2: Deposit only, 3: Bind to tanks
        byte val = (byte)(nbt.getByte("Mode") + 1);
        if (val > OPERATION_MODE_BINDING || (val == OPERATION_MODE_BINDING && this.getBucketLinkMode(stack) == LINK_MODE_DISABLED))
        {
            val = OPERATION_MODE_NORMAL;
        }

        nbt.setByte("Mode", val);
        stack.setTagCompound(nbt);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Just Toggle mode key: Change operation mode between normal, fill-only, drain-only and bind-to-tanks
        if (key == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.changeOperationMode(stack);
        }
        // Control + Toggle mode: Toggle the bucket's link mode between regular-bucket-mode and linked-to-a-tank
        else if (ReferenceKeys.keypressContainsControl(key) == true
                && ReferenceKeys.keypressContainsShift(key) == false
                && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeLinkMode(stack);
        }
        // Shift + (Control +) Toggle mode: Change the selected link crystal, if we are in tank mode
        else if (ReferenceKeys.keypressContainsShift(key) == true)
        {
            if (ReferenceKeys.keypressContainsAlt(key) == false && this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
            {
                super.doKeyBindingAction(player, stack, key);
            }
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher, TextureMap textureMap, Map<ResourceLocation, ModelBlock> modelMap)
    {
        itemModelMesher.register(this, EnderUtilitiesModelRegistry.baseItemMeshDefinition);
        //ItemModelGenerator itemModelGenerator = new ItemModelGenerator();

        String modelNames[] = new String[] {
                                    this.name + ".32.normal",
                                    this.name + ".32.linked",
                                    this.name + ".32.mode.fill",
                                    this.name + ".32.mode.drain",
                                    this.name + ".32.mode.bind",
                                    this.name + ".32.normal.inside",
                                    this.name + ".32.linked.inside"
        };

        int len = modelNames.length;
        this.models = new IFlexibleBakedModel[len];

        // Read and bake the static parts of the bucket's model
        for (int i = 0; i < len; ++i)
        {
            // Get the name of the model with the correct translation/rotation/scale etc.
            String modelName = Reference.MOD_ID + ":item/" + modelNames[i];
            ModelBlock modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation(modelName), modelMap, false);
            if (modelBlock == null)
            {
                EnderUtilities.logger.fatal("Failed to read ModelBlock for " + modelName);
                modelBlock = EnderUtilitiesModelRegistry.modelBlockBaseItems;
            }

            if (modelBlock != null)
            {
                // Create a new version of the ModelBlock with isAmbientOcclusion and isGui3d set to false.
                // Normally for items they are set to false in the ItemModelGenerator.makeItemModel() method
                modelBlock = EnderUtilitiesModelBlock.createNewModelBlock(modelBlock, modelBlock.name, modelBlock.getElements(), modelBlock.textures, modelBlock.getParentLocation(), false, false, modelMap, false);
                this.models[i] = EnderUtilitiesModelFactory.instance.bakeModel(modelBlock, ModelRotation.X0_Y0, false); // FIXME: rotation and uv-lock ??
                //modelRegistry.putObject(new ModelResourceLocation(Reference.MOD_ID + ":" + this.variants[i], "inventory"), this.models[i]);
            }
            else
            {
                EnderUtilities.logger.fatal("ModelBlock from makeItemModel() was null when trying to bake item model for " + this.variants[i]);
            }
        }

        // Read the dynamic parts of the bucket's model and store them in the ModelBlock form for later use with fluid rendering
        //modelNames = new String[] { this.name + ".32.fluid", this.name + ".32.normal.inside", this.name + ".32.linked.inside" };
        modelNames = new String[] { this.name + ".32.fluid" };
        len = modelNames.length;
        this.modelBlocks = new ModelBlock[len];

        for (int i = 0; i < len; ++i)
        {
            String modelName = Reference.MOD_ID + ":item/" + modelNames[i];
            this.modelBlocks[i] = EnderUtilitiesModelBlock.readModel(new ResourceLocation(modelName), modelMap, false);
            // Create a new version of the ModelBlock with isAmbientOcclusion and isGui3d set to false.
            // Normally for items they are set to false in the ItemModelGenerator.makeItemModel() method
            this.modelBlocks[i] = EnderUtilitiesModelBlock.createNewModelBlock(this.modelBlocks[i], this.modelBlocks[i].name, this.modelBlocks[i].getElements(), this.modelBlocks[i].textures, this.modelBlocks[i].getParentLocation(), false, false, modelMap, false);
            if (this.modelBlocks[i] == null)
            {
                EnderUtilities.logger.fatal("Failed to read ModelBlock for " + modelName);
                this.modelBlocks[i] = EnderUtilitiesModelRegistry.modelBlockBaseItems;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        textureMap.registerSprite(new ResourceLocation(ReferenceTextures.getItemTextureName(this.name + ".32.normal.hollow")));
        textureMap.registerSprite(new ResourceLocation(ReferenceTextures.getItemTextureName(this.name + ".32.linked.hollow")));
        textureMap.registerSprite(new ResourceLocation(ReferenceTextures.getItemTextureName(this.name + ".32.parts")));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerVariants()
    {
        this.addVariants(   this.name + ".32.normal.hollow",
                            this.name + ".32.linked.hollow"
                            );
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IFlexibleBakedModel getItemModel(ItemStack stack)
    {
        if (stack == null)
        {
            return this.models[0];
        }

        byte op_mode = this.getBucketMode(stack);
        byte link_mode = this.getBucketLinkMode(stack);

        IFlexibleBakedModel model;
        // Get the main part of the bucket
        model = this.models[link_mode == LINK_MODE_ENABLED ? 1 : 0];

        // Merge the inside background model
        model = EnderUtilitiesModelFactory.mergeModelsSimple(model, this.models[link_mode + 5]);
        //model = this.models[link_mode + 5];

        // Merge the operation mode model
        if (op_mode > OPERATION_MODE_NORMAL && op_mode <= OPERATION_MODE_BINDING)
        {
            model = EnderUtilitiesModelFactory.mergeModelsSimple(model, this.models[op_mode + 1]);
        }

        model = this.mergeFluidModel(model, stack);

        return model;
    }

    private IFlexibleBakedModel mergeFluidModel(IFlexibleBakedModel modelIn, ItemStack stack)
    {
        IFlexibleBakedModel model = modelIn;
        FluidStack fluidStack = this.getFluidCached(stack);

        if (fluidStack != null && fluidStack.amount > 0)
        {
            Fluid fluid = fluidStack.getFluid();
            if (fluid != null)
            {
                if (fluid.getIcon(fluidStack) != null)
                {
                    float capacity = (float)this.getCapacityCached(stack, null);
                    if (capacity == 0.0f)
                    {
                        capacity = 1.0f;
                    }

                    float amount = (float)fluidStack.amount;
                    float scale = Math.min((amount / capacity), 1.0f);

                    // TODO: Render the bucket upside down if the fluid is a gas
                    if (fluid.isGaseous() == true)
                    {
                    }

                    Map<String, String> map = Maps.newHashMap();
                    map.put("texture", fluid.getIcon(fluidStack).getIconName());

                    ModelBlock modelBlock = EnderUtilitiesModelBlock.createNewModelBlockForTextures(this.modelBlocks[0], this.modelBlocks[0].name, map, null, false);

                    modelBlock = EnderUtilitiesModelBlock.scaleModelHeight(modelBlock, scale, null, false);
                    IFlexibleBakedModel fluidModel = EnderUtilitiesModelFactory.instance.bakeModel(modelBlock, ModelRotation.X0_Y0, false);
                    model = EnderUtilitiesModelFactory.mergeModelsSimple(model, fluidModel);
                }
            }
        }

        return model;
    }
}
