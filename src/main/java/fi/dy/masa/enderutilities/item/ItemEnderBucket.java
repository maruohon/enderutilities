package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBucket extends ItemLocationBoundModular implements IKeyBound, IFluidContainerItem
{
    public static final int BUCKET_VOLUME = 1000;
    public static final double ENDER_CHARGE_COST = 0.2d; // charge cost per 1 mB of fluid transferred to/from a linked tank
    public static final byte OPERATION_MODE_NORMAL = 0;
    public static final byte OPERATION_MODE_FILL_BUCKET = 1;
    public static final byte OPERATION_MODE_DRAIN_BUCKET = 2;
    public static final byte OPERATION_MODE_BINDING = 3;
    public static final byte LINK_MODE_DISABLED = 0;
    public static final byte LINK_MODE_ENABLED = 1;
    public static final int ENDER_BUCKET_MAX_AMOUNT = 16000; // Can contain 16 buckets

    protected int capacity;

    public ItemEnderBucket()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BUCKET);
        this.setCapacity(Configs.enderBucketCapacity);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        // Do nothing on the client side
        if (world.isRemote == true)
        {
            return EnumActionResult.PASS;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED &&
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return EnumActionResult.FAIL;
        }

        if (this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack)) == EnumActionResult.SUCCESS)
        {
            return EnumActionResult.SUCCESS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof IFluidHandler)
        {
            // If we are in bind mode, bind the bucket to the targeted tank and then return
            if (this.getBucketMode(stack) == OPERATION_MODE_BINDING)
            {
                return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
            }

            return this.useBucketOnTank(stack, player, world, pos, side, this.getBucketMode(stack));
        }

        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        this.setCapacity(Configs.enderBucketCapacity);

        if (world.isRemote == true)
        {
            return EnumActionResult.SUCCESS;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED &&
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return EnumActionResult.FAIL;
        }

        // First try to use the bucket on a fluid block, if any.
        // If that fails (not targeting fluid), then we use it on a block (see below).
        if (this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack)) == EnumActionResult.SUCCESS)
        {
            return EnumActionResult.SUCCESS;
        }

        return this.useBucketOnBlock(stack, player, world, pos, side, hitX, hitY, hitZ, this.getBucketMode(stack));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        this.setCapacity(Configs.enderBucketCapacity);

        // Do nothing on the client side
        if (world.isRemote == true || (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED &&
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false))
        {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        EnumActionResult result = this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack));
        return new ActionResult<ItemStack>(result, stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = this.getFluidCached(stack);
        String baseName = this.getBucketLinkMode(stack) == LINK_MODE_ENABLED ? super.getItemStackDisplayName(stack) : this.getBaseItemDisplayName(stack);

        if (fluidStack != null && fluidStack.amount > 0 && fluidStack.getFluid() != null)
        {
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();
            String fluidName = fluidStack.getFluid().getLocalizedName(fluidStack);
            return baseName + " - " + TextFormatting.GREEN.toString() + fluidName + rst;
        }

        return baseName;
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
        String preNr = TextFormatting.BLUE.toString();
        String preTxt = TextFormatting.DARK_GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        int amount = 0;
        int capacity = this.getCapacityCached(stack, player);

        if (fluidStack != null && fluidStack.getFluid() != null)
        {
            amount = fluidStack.amount;
            fluidName = preTxt + fluidStack.getFluid().getLocalizedName(fluidStack) + rst;
        }
        else
        {
            fluidName = I18n.format("enderutilities.tooltip.item.empty");
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
                list.add(I18n.format("enderutilities.tooltip.item.cached.fluid") + ": " + fluidName);
                list.add(I18n.format("enderutilities.tooltip.item.cached.amount") + ": " + amountStr);
            }
            else
            {
                list.add(I18n.format("enderutilities.tooltip.item.fluid") + ": " + fluidName);
                list.add(I18n.format("enderutilities.tooltip.item.amount") + ": " + amountStr);
            }
        }
        else
        {
            if (linkMode == LINK_MODE_ENABLED)
            {
                list.add(I18n.format("enderutilities.tooltip.item.cached.fluid.compact") + ": " + fluidName + " - " + amountStr);
            }
            else
            {
                list.add(fluidName + " - " + amountStr);
            }
        }

        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + I18n.format(modeStr));

        if (linkMode == LINK_MODE_ENABLED)
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    public byte getBucketMode(ItemStack stack)
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

        return OPERATION_MODE_NORMAL;
    }

    /**
     *  Returns whether the bucket is currently set to link to a tank.
     */
    public byte getBucketLinkMode(ItemStack stack)
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

        return LINK_MODE_DISABLED;
    }

    /**
     * Checks if the player can edit the target block
     */
    public boolean isTargetUsable(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
    {
        if (world == null)
        {
            return false;
        }

        // Spawn safe zone checks etc.
        if (world.canMineBlockBody(player, pos) == false || player.canPlayerEdit(pos, side, stack) == false)
        {
            return false;
        }

        return true;
    }

    public EnumActionResult useBucketOnTank(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, byte bucketMode)
    {
        if (this.isTargetUsable(stack, player, world, pos, side) == false)
        {
            return EnumActionResult.PASS;
        }

        TileEntity te = world.getTileEntity(pos);

        // Is this a TileEntity that is also some sort of a fluid storage device?
        if (te != null && te instanceof IFluidHandler)
        {
            IFluidHandler iFluidHandler = (IFluidHandler)te;
            FluidStack fluidStack;
            String blockName = ForgeRegistries.BLOCKS.getKey(world.getBlockState(pos).getBlock()).toString();

            // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
            // work if we target a blue (= input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
            // but the top side should ever only be blue aka. input.
            if (blockName != null && blockName.equals("ThermalExpansion:Tank"))
            {
                side = EnumFacing.UP;
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
                fluidStack = iFluidHandler.drain(side, BUCKET_VOLUME, false); // simulate
                int amount = this.getCapacityAvailable(stack, fluidStack, player);

                // We can still store more fluid
                if (amount > 0)
                {
                    if (amount > BUCKET_VOLUME)
                    {
                        amount = BUCKET_VOLUME;
                    }

                    // If the bucket is currently empty, or the tank's fluid is the same we currently have
                    if (fluidStack != null && (storedFluidAmount == 0 || fluidStack.isFluidEqual(storedFluidStack) == true))
                    {
                        fluidStack = iFluidHandler.drain(side, amount, false);
                        if (fluidStack != null && this.fillWorker(stack, fluidStack, false, player) == fluidStack.amount)
                        {
                            fluidStack = iFluidHandler.drain(side, amount, true); // actually drain
                            this.fillWorker(stack, fluidStack, true, player);
                            return EnumActionResult.SUCCESS;
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
                    fluidStack = this.drainWorker(stack, BUCKET_VOLUME, false, player);

                    // Check if we can deposit (at least some) the fluid we have stored
                    if (fluidStack != null && iFluidHandler.fill(side, fluidStack, false) > 0) // simulate
                    {
                        int amount = iFluidHandler.fill(side, fluidStack, true);
                        this.drainWorker(stack, amount, true, player); // actually drain fluid from the bucket (the amount that was filled into the tank)
                        return EnumActionResult.SUCCESS;
                    }
                }
            }
        }

        return EnumActionResult.PASS;
    }

    public EnumActionResult useBucketOnBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, byte bucketMode)
    {
        if (this.isTargetUsable(stack, player, world, pos, side) == false)
        {
            return EnumActionResult.PASS;
        }

        // Adjust the target block position to be the block touching the side of the block we targeted
        pos = pos.offset(side);

        IBlockState state = world.getBlockState(pos);
        // Check if there is a fluid block on the side of the targeted block
        if (state.getMaterial().isLiquid() == true)
        {
            // Note: the side is technically wrong unless we ray trace it again, but it won't matter with fluid blocks... right?
            return this.useBucketOnFluidBlock(stack, world, player, pos, side, bucketMode);
        }

        // There was no fluid block where we are targeting

        // Get the stored fluid, if any
        FluidStack fluidStack = this.getFluidWorker(stack, player);
        int storedFluidAmount = fluidStack != null ? fluidStack.amount : 0;

        // target block is not fluid, try to place a fluid block in world in the adjusted block position
        if (storedFluidAmount >= BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
        {
            fluidStack = this.drainWorker(stack, BUCKET_VOLUME, false, player);

            if (fluidStack != null && fluidStack.amount == BUCKET_VOLUME &&
                this.tryPlaceFluidBlock(world, pos, fluidStack) == true)
            {
                this.drainWorker(stack, BUCKET_VOLUME, true, player);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    public EnumActionResult useBucketOnFluidBlock(ItemStack stack, World world, EntityPlayer player, byte bucketMode)
    {
        // First find out what block we are targeting
        RayTraceResult rayTrace = this.rayTrace(world, player, true);

        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK)
        {
            return EnumActionResult.PASS;
        }

        return this.useBucketOnFluidBlock(stack, world, player, rayTrace.getBlockPos(), rayTrace.sideHit, bucketMode);
    }

    public EnumActionResult useBucketOnFluidBlock(ItemStack stack, World world, EntityPlayer player, BlockPos pos, EnumFacing side, byte bucketMode)
    {
        IBlockState state = world.getBlockState(pos);
        Block targetBlock = state.getBlock();

        // Spawn safe zone checks etc.
        if (this.isTargetUsable(stack, player, world, pos, side) == false || state.getMaterial().isLiquid() == false)
        {
            return EnumActionResult.PASS;
        }

        // Get the stored fluid, if any
        FluidStack storedFluidStack = this.getFluidWorker(stack, player);
        FluidStack targetFluidStack = null;
        IFluidBlock iFluidBlock = null;
        int storedFluidAmount = storedFluidStack != null ? storedFluidStack.amount : 0;

        if (targetBlock instanceof IFluidBlock)
        {
            iFluidBlock = (IFluidBlock)targetBlock;
            targetFluidStack = iFluidBlock.drain(world, pos, false); // simulate
        }
        else
        {
            // We need to convert flowing water and lava to the still variant for logic stuffs
            // We will always convert them to the flowing variant before placing
            if (targetBlock == Blocks.FLOWING_WATER)
            {
                targetBlock = Blocks.WATER;
            }
            else if (targetBlock == Blocks.FLOWING_LAVA)
            {
                targetBlock = Blocks.LAVA;
            }

            Fluid fluid = FluidRegistry.lookupFluidForBlock(targetBlock);
            if (fluid != null)
            {
                targetFluidStack = FluidRegistry.getFluidStack(fluid.getName(), BUCKET_VOLUME);
            }
        }

        // Not in drain-only mode && (Empty || (space && same fluid && (not sneaking || fill-only mode))) => trying to pick up fluid
        if (bucketMode != OPERATION_MODE_DRAIN_BUCKET && (storedFluidAmount == 0 ||
                (this.getCapacityAvailable(stack, targetFluidStack, player) >= BUCKET_VOLUME &&
                    storedFluidStack.isFluidEqual(targetFluidStack) == true &&
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

                        SoundEvent sound = targetBlock == Blocks.LAVA ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        return EnumActionResult.SUCCESS;
                    }
                }

                return EnumActionResult.PASS;
            }

            // Does not implement IFluidBlock

            // Check that the fluid block we are trying to pick up is a source block, and that we can store that amount
            if (targetFluidStack != null && state.getValue(BlockLiquid.LEVEL).intValue() == 0 &&
                this.fillWorker(stack, targetFluidStack, false, player) == targetFluidStack.amount)
            {
                if (world.setBlockToAir(pos) == true)
                {
                    this.fillWorker(stack, targetFluidStack, true, player);

                    SoundEvent sound = targetBlock == Blocks.LAVA ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

                    return EnumActionResult.SUCCESS;
                }
            }
        }

        // Fluid stored and not in fill-only mode, try to place fluid
        if (storedFluidStack != null && storedFluidAmount >= BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
        {
            // (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
            // The meta check is for ignoring flowing fluid blocks (ie. non-source blocks)
            if (storedFluidStack.isFluidEqual(targetFluidStack) == false || player.isSneaking() == true ||
                state.getValue(BlockLiquid.LEVEL).intValue() != 0)
            {
                FluidStack fluidStack = this.drainWorker(stack, BUCKET_VOLUME, false, player);
                if (fluidStack != null && fluidStack.amount == BUCKET_VOLUME &&
                    this.tryPlaceFluidBlock(world, pos, storedFluidStack) == true)
                {
                    this.drainWorker(stack, BUCKET_VOLUME, true, player);
                    return EnumActionResult.SUCCESS;
                }
            }

            return EnumActionResult.PASS;
        }

        return EnumActionResult.PASS;
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
        if (block == Blocks.WATER)
        {
            block = Blocks.FLOWING_WATER;
        }
        else if (block == Blocks.LAVA)
        {
            block = Blocks.FLOWING_LAVA;
        }

        IBlockState state = world.getBlockState(pos);
        Material material = state.getMaterial();

        if (world.isAirBlock(pos) == false && material.isSolid() == true)
        {
            return false;
        }

        if (world.provider.doesWaterVaporize() && block == Blocks.FLOWING_WATER)
        {
            float x = pos.getX();
            float y = pos.getY();
            float z = pos.getZ();

            world.playSound(null, x + 0.5F, y + 0.5F, z + 0.5F, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l)
            {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x + Math.random(), y + Math.random(), z + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }
        else
        {
            if (world.isRemote == false && material.isSolid() == false && material.isLiquid() == false)
            {
                // Set a replaceable block to air, and drop the items
                world.destroyBlock(pos, true);
            }

            world.setBlockState(pos, block.getDefaultState(), 3);
            SoundEvent soundevent = block == Blocks.FLOWING_LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
            world.playSound(null, pos, soundevent, SoundCategory.BLOCKS, 1.0f, 1.0f);
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
            TargetData targetData = this.getLinkedTankTargetData(stack);
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

                    if (info != null && info.length > 0 && info[0] != null)
                    {
                        return info[0].capacity - fluidStack.amount;
                    }

                    fs.amount = Integer.MAX_VALUE;

                    return tank.fill(targetData.facing, fs, false);
                }
                // Tank has no fluid
                else
                {
                    if (info != null && info.length > 0 && info[0] != null)
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
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(Blocks.WATER);
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
                TargetData targetData = this.getLinkedTankTargetData(stack);
                if (tank != null && targetData != null)
                {
                    FluidTankInfo[] info = tank.getTankInfo(targetData.facing);
                    if (info != null && info.length > 0 && info[0] != null)
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

    public TargetData getLinkedTankTargetData(ItemStack stack)
    {
        TargetData targetData = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
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
        TargetData targetData = this.getLinkedTankTargetData(stack);
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (targetData == null || server == null)
        {
            return null;
        }

        World world = server.worldServerForDimension(targetData.dimension);
        if (world == null)
        {
            return null;
        }

        // Force load the target chunk where the tank is located with a 30 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithModTicket(targetData.dimension,
                targetData.pos.getX() >> 4, targetData.pos.getZ() >> 4, 30) == false)
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

        if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return 0;
        }

        FluidStack fluidStack;
        TargetData targetData = this.getLinkedTankTargetData(stack);
        IFluidHandler tank = this.getLinkedTank(stack);

        if (targetData != null && tank != null)
        {
            FluidTankInfo[] info = tank.getTankInfo(targetData.facing);

            // If we have tank info, it is the easiest and simplest way to get the tank capacity
            if (info != null && info.length > 0 && info[0] != null)
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
                Fluid fluid = FluidRegistry.lookupFluidForBlock(Blocks.WATER);
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
            if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return null;
            }

            TargetData targetData = this.getLinkedTankTargetData(stack);
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
            if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return null;
            }

            TargetData targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                if (UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * maxDrain), doDrain == false) == false)
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
            if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return 0;
            }

            TargetData targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                if (fluidStackIn != null && UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * fluidStackIn.amount), doFill == false) == false)
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
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
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
            return this.getMaxModules(containerStack, moduleType);
        }

        return 0;
    }

    /**
     * Toggles the linked-to-tank mode ON/OFF.
     */
    private void changeLinkMode(ItemStack stack)
    {
        NBTUtils.toggleBoolean(stack, null, "Linked");
    }

    /**
     * Cycles through the operation modes: Normal, Fill, Drain, Bind to tanks
     */
    private void changeOperationMode(ItemStack stack)
    {
        NBTTagCompound nbt = NBTUtils.getCompoundTag(stack, null, true);

        // 0: Normal, 1: Pickup only, 2: Deposit only, 3: Bind to tanks
        byte val = (byte)(nbt.getByte("Mode") + 1);
        if (val > OPERATION_MODE_BINDING || (val == OPERATION_MODE_BINDING && this.getBucketLinkMode(stack) == LINK_MODE_DISABLED))
        {
            val = OPERATION_MODE_NORMAL;
        }

        nbt.setByte("Mode", val);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode key: Change operation mode between normal, fill-only, drain-only and bind-to-tanks
        if (key == HotKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.changeOperationMode(stack);
        }
        // Shift + Toggle mode: Toggle the bucket's link mode between regular-bucket-mode and linked-to-a-tank
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            this.changeLinkMode(stack);
        }
        // Ctrl + (Shift +) Toggle mode: Change the selected link crystal, if we are in tank mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
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
    @Override
    public ResourceLocation[] getItemVariants()
    {
        return new ResourceLocation[] { new ModelResourceLocation(Reference.MOD_ID + ":item_enderbucket", "inventory") };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return new ModelResourceLocation(Reference.MOD_ID + ":item_enderbucket", "inventory");
    }
}
