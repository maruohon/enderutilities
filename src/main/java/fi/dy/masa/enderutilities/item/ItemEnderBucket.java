package fi.dy.masa.enderutilities.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
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
import fi.dy.masa.enderutilities.util.fluid.FluidHandlerEnderBucket;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBucket extends ItemLocationBoundModular implements IKeyBound
{
    public static final double ENDER_CHARGE_COST = 0.2d; // charge cost per 1 mB of fluid transferred to/from a linked tank

    public ItemEnderBucket()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BUCKET);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
    {
        return new FluidHandlerEnderBucket(this, stack);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
            EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if (LinkMode.fromStack(stack) == LinkMode.ENABLED &&
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return EnumActionResult.FAIL;
        }

        if (world.isRemote == false)
        {
            if (this.useBucketOnFluidBlock(world, player, stack) == EnumActionResult.SUCCESS)
            {
                return EnumActionResult.SUCCESS;
            }

            TileEntity te = world.getTileEntity(pos);

            if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side))
            {
                // If we are in bind mode, bind the bucket to the targeted tank and then return
                if (BucketMode.fromStack(stack) == BucketMode.BIND)
                {
                    return super.onItemUse(stack, player, world, pos, hand, side, hitX, hitY, hitZ);
                }

                return this.useBucketOnTank(world, pos, side, player, stack);
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == false)
        {
            if (LinkMode.fromStack(stack) == LinkMode.ENABLED &&
                OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return EnumActionResult.FAIL;
            }

            // First try to use the bucket on a fluid block, if any.
            // If that fails (not targeting fluid), then we use it on a block (see below).
            if (this.useBucketOnFluidBlock(world, player, stack) == EnumActionResult.SUCCESS)
            {
                return EnumActionResult.SUCCESS;
            }

            return this.useBucketOnBlock(world, pos, side, player, stack);
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        // Do nothing on the client side
        if (world.isRemote || (LinkMode.fromStack(stack) == LinkMode.ENABLED &&
            OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false))
        {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
        }

        EnumActionResult result = this.useBucketOnFluidBlock(world, player, stack);
        return new ActionResult<ItemStack>(result, stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = this.getFluidCached(stack);
        String baseName = LinkMode.fromStack(stack) == LinkMode.ENABLED ?
                super.getItemStackDisplayName(stack) : this.getBaseItemDisplayName(stack);

        if (fluidStack != null && fluidStack.amount > 0 && fluidStack.getFluid() != null)
        {
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();
            String fluidName = fluidStack.getFluid().getLocalizedName(fluidStack);
            return baseName + " - " + TextFormatting.GREEN.toString() + fluidName + rst;
        }

        return baseName;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list,
            boolean advancedTooltips, boolean verbose)
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

        BucketMode mode = BucketMode.fromStack(stack);
        LinkMode link = LinkMode.fromStack(stack);

        String amountStr = String.format("%s%s%s mB / %s%s%s mB",
                preNr, EUStringUtils.formatNumberWithKSeparators(amount), rst,
                preNr, EUStringUtils.formatNumberWithKSeparators(capacity), rst);

        if (verbose)
        {
            if (link == LinkMode.ENABLED)
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
            if (link == LinkMode.ENABLED)
            {
                list.add(I18n.format("enderutilities.tooltip.item.cached.fluid.compact") + ": " + fluidName + " - " + amountStr);
            }
            else
            {
                list.add(fluidName + " - " + amountStr);
            }
        }

        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + I18n.format(mode.getUnlocalizedName()));

        if (link == LinkMode.ENABLED)
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return oldStack.equals(newStack) == false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        addTooltips(super.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    /**
     * Checks if the player can edit the target block
     */
    public boolean isTargetUsable(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        // Spawn safe zone checks etc.
        return world.canMineBlockBody(player, pos) && player.canPlayerEdit(pos, side, stack);
    }

    public EnumActionResult useBucketOnTank(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        TileEntity te = world.getTileEntity(pos);

        if (this.isTargetUsable(world, pos, side, player, stack) == false ||
            te == null || te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side) == false)
        {
            return EnumActionResult.PASS;
        }

        IFluidHandler iFluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
        String blockName = ForgeRegistries.BLOCKS.getKey(world.getBlockState(pos).getBlock()).toString();

        // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
        // work if we target a blue (= input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
        // but the top side should ever only be blue aka. input.
        if (blockName.equals("ThermalExpansion:Tank"))
        {
            side = EnumFacing.UP;
        }

        // Get the stored fluid, if any
        FluidStack storedFluidStack = this.getFluid(stack, player);
        BucketMode mode = BucketMode.fromStack(stack);
        int storedFluidAmount = 0;

        if (storedFluidStack != null)
        {
            storedFluidAmount = storedFluidStack.amount;
        }

        // With tanks we pick up fluid when not sneaking
        if (mode == BucketMode.FILL || (mode == BucketMode.NORMAL && player.isSneaking() == false))
        {
            FluidStack fluidStack = iFluidHandler.drain(Fluid.BUCKET_VOLUME, false); // simulate

            if (fluidStack != null)
            {
                int amount = Math.min(this.getCapacityAvailable(stack, fluidStack, player), Fluid.BUCKET_VOLUME);

                // If the bucket is currently empty, or the tank's fluid is the same we currently have
                if (amount > 0 && (storedFluidAmount == 0 || fluidStack.isFluidEqual(storedFluidStack)))
                {
                    fluidStack = iFluidHandler.drain(amount, false);

                    if (fluidStack != null && this.fill(stack, fluidStack, false, player) == fluidStack.amount)
                    {
                        fluidStack = iFluidHandler.drain(amount, true); // actually drain
                        this.fill(stack, fluidStack, true, player);
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
                FluidStack fluidStack = this.drain(stack, Fluid.BUCKET_VOLUME, false, player);

                // Check if we can deposit (at least some) the fluid we have stored
                if (fluidStack != null && iFluidHandler.fill(fluidStack, false) > 0) // simulate
                {
                    int amount = iFluidHandler.fill(fluidStack, true);
                    // actually drain fluid from the bucket (the amount that was filled into the tank)
                    this.drain(stack, amount, true, player);
                    return EnumActionResult.SUCCESS;
                }
            }
        }

        return EnumActionResult.PASS;
    }

    public EnumActionResult useBucketOnBlock(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        // Adjust the target block position to be the block touching the side of the block we targeted
        pos = pos.offset(side);

        if (this.isTargetUsable(world, pos, side, player, stack) == false)
        {
            return EnumActionResult.PASS;
        }

        // Check if there is a fluid block on the side of the targeted block
        if (world.getBlockState(pos).getMaterial().isLiquid())
        {
            // Note: the side is technically wrong unless we ray trace it again, but it won't matter with fluid blocks... right?
            return this.useBucketOnFluidBlock(world, pos, side, player, stack);
        }

        // There was no fluid block where we are targeting

        // Get the stored fluid, if any
        FluidStack fluidStack = this.getFluid(stack, player);
        int storedFluidAmount = fluidStack != null ? fluidStack.amount : 0;

        // target block is not fluid, try to place a fluid block in world in the adjusted block position
        if (storedFluidAmount >= Fluid.BUCKET_VOLUME && BucketMode.fromStack(stack) != BucketMode.FILL)
        {
            fluidStack = this.drain(stack, Fluid.BUCKET_VOLUME, false, player);

            if (fluidStack != null && fluidStack.amount == Fluid.BUCKET_VOLUME &&
                this.tryPlaceFluidBlock(world, pos, fluidStack) == true)
            {
                this.drain(stack, Fluid.BUCKET_VOLUME, true, player);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    public EnumActionResult useBucketOnFluidBlock(World world, EntityPlayer player, ItemStack stack)
    {
        // First find out what block we are targeting
        RayTraceResult rayTrace = this.rayTrace(world, player, true);

        if (rayTrace == null || rayTrace.typeOfHit != RayTraceResult.Type.BLOCK)
        {
            return EnumActionResult.PASS;
        }

        return this.useBucketOnFluidBlock(world, rayTrace.getBlockPos(), rayTrace.sideHit, player, stack);
    }

    public EnumActionResult useBucketOnFluidBlock(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
    {
        IBlockState state = world.getBlockState(pos);
        Block targetBlock = state.getBlock();

        // Spawn safe zone checks etc.
        if (this.isTargetUsable(world, pos, side, player, stack) == false || state.getMaterial().isLiquid() == false)
        {
            return EnumActionResult.PASS;
        }

        // Get the stored fluid, if any
        FluidStack storedFluidStack = this.getFluid(stack, player);
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
            Fluid fluid;
            // We need to convert flowing water and lava to the still variant for logic stuffs
            // We will always convert them to the flowing variant before placing
            if (targetBlock == Blocks.FLOWING_WATER)
            {
                targetBlock = Blocks.WATER;
                fluid = FluidRegistry.WATER;
            }
            else if (targetBlock == Blocks.FLOWING_LAVA)
            {
                targetBlock = Blocks.LAVA;
                fluid = FluidRegistry.LAVA;
            }
            else
            {
                fluid = FluidRegistry.lookupFluidForBlock(targetBlock);
            }

            if (fluid != null)
            {
                targetFluidStack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
            }
        }

        BucketMode mode = BucketMode.fromStack(stack);
        // Not in drain-only mode && (empty || (space && same fluid && (not sneaking || fill-only mode))) => trying to pick up fluid
        if (mode != BucketMode.DRAIN &&
            (storedFluidAmount == 0 || (this.getCapacityAvailable(stack, targetFluidStack, player) >= Fluid.BUCKET_VOLUME &&
                storedFluidStack.isFluidEqual(targetFluidStack) && (player.isSneaking() == false || mode == BucketMode.FILL))))
        {
            // Implements IFluidBlock
            if (iFluidBlock != null)
            {
                if (iFluidBlock.canDrain(world, pos))
                {
                    targetFluidStack = iFluidBlock.drain(world, pos, false); // simulate

                    // Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
                    if (targetFluidStack != null && this.fill(stack, targetFluidStack, false, player) == targetFluidStack.amount)
                    {
                        targetFluidStack = iFluidBlock.drain(world, pos, true);
                        this.fill(stack, targetFluidStack, true, player);

                        SoundEvent sound = targetBlock == Blocks.LAVA ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

                        return EnumActionResult.SUCCESS;
                    }
                }

                return EnumActionResult.PASS;
            }

            // Does not implement IFluidBlock

            // Check that the fluid block we are trying to pick up is a source block, and that we can store that amount
            // FIXME What is the proper generic way to detect source blocks?
            if (targetFluidStack != null && state.getBlock().getMetaFromState(state) == 0 &&
                this.fill(stack, targetFluidStack, false, player) == targetFluidStack.amount)
            {
                if (world.setBlockToAir(pos))
                {
                    this.fill(stack, targetFluidStack, true, player);

                    SoundEvent sound = targetBlock == Blocks.LAVA ? SoundEvents.ITEM_BUCKET_FILL_LAVA : SoundEvents.ITEM_BUCKET_FILL;
                    world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0F, 1.0F);

                    return EnumActionResult.SUCCESS;
                }
            }
        }

        // Fluid stored and not in fill-only mode, try to place fluid
        if (storedFluidStack != null && storedFluidAmount >= Fluid.BUCKET_VOLUME && mode != BucketMode.FILL)
        {
            // (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
            // The meta check is for ignoring flowing fluid blocks (ie. non-source blocks)
            if (player.isSneaking() || storedFluidStack.isFluidEqual(targetFluidStack) == false ||
                    state.getBlock().getMetaFromState(state) != 0)
            {
                FluidStack fluidStack = this.drain(stack, Fluid.BUCKET_VOLUME, false, player);

                if (fluidStack != null && fluidStack.amount == Fluid.BUCKET_VOLUME &&
                    this.tryPlaceFluidBlock(world, pos, storedFluidStack))
                {
                    this.drain(stack, Fluid.BUCKET_VOLUME, true, player);
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

        if (world.isAirBlock(pos) == false && material.isSolid())
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

    public int getCapacityCached(ItemStack stack, EntityPlayer player)
    {
        if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);

            if (moduleStack != null)
            {
                NBTTagCompound moduleNbt = moduleStack.getTagCompound();

                if (moduleNbt != null && moduleNbt.hasKey("CapacityCached", Constants.NBT.TAG_INT))
                {
                    return moduleNbt.getInteger("CapacityCached");
                }
            }

            return 0; // without this the client side code would try to read the tank info, and then crash
        }

        return this.getCapacity(stack, player);
    }

    public int getCapacityAvailable(ItemStack stack, FluidStack fluidStackIn, EntityPlayer player)
    {
        FluidStack existingFluidStack;

        // Linked to a tank
        if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
        {
            IFluidHandler handler = this.getLinkedTank(stack);

            if (handler != null)
            {
                IFluidTankProperties[] tank = handler.getTankProperties();

                if (tank == null || tank.length < 1 || tank[0] == null)
                {
                    return 0;
                }

                existingFluidStack = tank[0].getContents();

                if (fluidStackIn == null)
                {
                    return tank[0].getCapacity() - (existingFluidStack != null ? existingFluidStack.amount : 0);
                }
                else
                {
                    fluidStackIn = fluidStackIn.copy();
                    fluidStackIn.amount = Integer.MAX_VALUE;

                    return handler.fill(fluidStackIn, false);
                }
            }

            return 0;
        }

        // Not linked to a tank, get the bucket's internal free capacity
        existingFluidStack = this.getFluid(stack, player);

        if (existingFluidStack != null)
        {
            return this.getCapacity(stack, player) - existingFluidStack.amount;
        }

        return this.getCapacity(stack, player);
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
        if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);

            if (moduleStack != null && moduleStack.getTagCompound() != null)
            {
                NBTTagCompound moduleNbt = moduleStack.getTagCompound();
                IFluidHandler handler = this.getLinkedTank(stack);

                if (handler != null)
                {
                    IFluidTankProperties[] tank = handler.getTankProperties();

                    if (tank != null && tank.length > 0 && tank[0] != null)
                    {
                        moduleNbt.setInteger("CapacityCached", tank[0].getCapacity());
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

        if (targetData != null)
        {
            // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
            // work if we target a blue (=input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
            // but the top side should ever only be blue aka. input.
            if ("ThermalExpansion:Tank".equals(targetData.blockName))
            {
                targetData.facing = EnumFacing.UP;
            }

            return targetData;
        }

        return null;
    }

    public IFluidHandler getLinkedTank(ItemStack stack)
    {
        TargetData targetData = this.getLinkedTankTargetData(stack);
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

        if (targetData != null && server != null)
        {
            World world = server.worldServerForDimension(targetData.dimension);

            if (world != null)
            {
                // Force load the target chunk where the tank is located with a 30 second unload delay.
                if (ChunkLoading.getInstance().loadChunkForcedWithModTicket(targetData.dimension,
                        targetData.pos.getX() >> 4, targetData.pos.getZ() >> 4, 30))
                {
                    TileEntity te = world.getTileEntity(targetData.pos);

                    if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetData.facing))
                    {
                        return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, targetData.facing);
                    }
                }
            }
        }

        return null;
    }

    public FluidStack getFluidCached(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt != null)
        {
            // The Bucket has been linked to a tank
            if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
            {
                ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);

                if (moduleStack != null && moduleStack.getTagCompound() != null)
                {
                    if (moduleStack.getTagCompound().hasKey("FluidCached", Constants.NBT.TAG_COMPOUND))
                    {
                        return FluidStack.loadFluidStackFromNBT(moduleStack.getTagCompound().getCompoundTag("FluidCached"));
                    }
                }
            }
            // Not linked to a tank, get the internal FluidStack
            else if (nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND))
            {
                return FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));
            }
        }

        return null;
    }

    public int getCapacity(ItemStack stack, @Nullable EntityPlayer player)
    {
        if (LinkMode.fromStack(stack) == LinkMode.DISABLED)
        {
            return Configs.enderBucketCapacity;
        }

        // Linked to a tank

        if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return 0;
        }

        TargetData targetData = this.getLinkedTankTargetData(stack);
        IFluidHandler handler = this.getLinkedTank(stack);

        if (targetData != null && handler != null)
        {
            IFluidTankProperties[] tank = handler.getTankProperties();

            // If we have tank info, it is the easiest and simplest way to get the tank capacity
            if (tank != null && tank.length > 0 && tank[0] != null)
            {
                return tank[0].getCapacity();
            }

            // No tank info available, get the capacity via simulating filling

            FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, false);

            // Tank has fluid
            if (fluidStack != null)
            {
                FluidStack fs = fluidStack.copy();
                fs.amount = Integer.MAX_VALUE;

                // Filled amount plus existing amount
                return handler.fill(fs, false) + fluidStack.amount;
            }
            // Tank has no fluid
            else
            {
                // Since we have no fluid stored, get the capacity via simulating filling water into the tank
                return handler.fill(new FluidStack(FluidRegistry.WATER, Integer.MAX_VALUE), false);
            }
        }

        return 0;
    }

    public FluidStack getFluid(ItemStack stack, @Nullable EntityPlayer player)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt == null)
        {
            return null;
        }

        // The Bucket has been linked to a tank
        if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
        {
            if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return null;
            }

            IFluidHandler handler = this.getLinkedTank(stack);

            if (handler != null)
            {
                FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, false);

                // Cache the fluid stack into the link crystal's NBT for easier/faster access for tooltip and rendering stuffs
                this.cacheFluid(stack, fluidStack);

                return fluidStack;
            }

            return null;
        }

        // Not linked to a tank, get the internal FluidStack
        if (nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND))
        {
            return FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));
        }

        return null;
    }

    public FluidStack drain(ItemStack stack, int maxDrain, boolean doDrain, @Nullable EntityPlayer player)
    {
        if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
        {
            if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return null;
            }

            IFluidHandler handler = this.getLinkedTank(stack);

            if (handler != null)
            {
                if (UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * maxDrain), doDrain == false) == false)
                {
                    return null;
                }

                FluidStack fluidStack = handler.drain(maxDrain, doDrain);
                this.cacheFluid(stack, handler.drain(Integer.MAX_VALUE, false));

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
        if (doDrain)
        {
            // Drained all the fluid
            if (drained >= fluidStack.amount)
            {
                nbt.removeTag("Fluid");

                if (nbt.hasNoTags())
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

    public int fill(ItemStack stack, FluidStack fluidStackIn, boolean doFill, @Nullable EntityPlayer player)
    {
        if (fluidStackIn == null)
        {
            return 0;
        }

        if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
        {
            if (OwnerData.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
            {
                return 0;
            }

            IFluidHandler handler = this.getLinkedTank(stack);

            if (handler != null)
            {
                if (UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * fluidStackIn.amount), doFill == false) == false)
                {
                    return 0;
                }

                int amount = handler.fill(fluidStackIn, doFill);
                this.cacheFluid(stack, handler.drain(Integer.MAX_VALUE, false));

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
        int filled = 0;

        if (storedFluidStack.isFluidEqual(fluidStackIn) == false)
        {
            return 0;
        }

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

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode key: Change operation mode between normal, fill-only, drain-only and bind-to-tanks
        if (key == HotKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            BucketMode.cycleMode(stack);
        }
        // Shift + Toggle mode: Toggle the bucket's link mode between regular-bucket-mode and linked-to-a-tank
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            LinkMode.cycleMode(stack);
        }
        // Ctrl + (Shift +) Toggle mode: Change the selected link crystal, if we are in tank mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            if (LinkMode.fromStack(stack) == LinkMode.ENABLED)
            {
                super.doKeyBindingAction(player, stack, key);
            }
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    public static enum LinkMode
    {
        DISABLED,
        ENABLED;

        public static LinkMode fromStack(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            return (tag != null && tag.getBoolean("Linked")) ? ENABLED : DISABLED;
        }

        public static void cycleMode(ItemStack stack)
        {
            int mode = (fromStack(stack).ordinal() & 0x01) ^ 0x01;
            NBTUtils.getCompoundTag(stack, null, true).setByte("Linked", (byte) mode);
        }
    }

    public static enum BucketMode
    {
        NORMAL ("enderutilities.tooltip.item.bucket.mode.normal"),
        FILL ("enderutilities.tooltip.item.bucket.mode.fill"),
        DRAIN ("enderutilities.tooltip.item.bucket.mode.drain"),
        BIND ("enderutilities.tooltip.item.bucket.mode.bind");

        private final String unlocName;

        private BucketMode(String unlocName)
        {
            this.unlocName = unlocName;
        }

        public String getUnlocalizedName()
        {
            return this.unlocName;
        }

        public static BucketMode fromStack(ItemStack stack)
        {
            NBTTagCompound tag = stack.getTagCompound();
            return tag == null ? NORMAL : values()[MathHelper.clamp(tag.getByte("Mode"), 0, values().length - 1)];
        }

        public static void cycleMode(ItemStack stack)
        {
            int mode = fromStack(stack).ordinal() + 1;

            if (mode >= values().length || (mode == BIND.ordinal() && LinkMode.fromStack(stack) == LinkMode.DISABLED))
            {
                mode = 0;
            }

            NBTUtils.getCompoundTag(stack, null, true).setByte("Mode", (byte) mode);
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
