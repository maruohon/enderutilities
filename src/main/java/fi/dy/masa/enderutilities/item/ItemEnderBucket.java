package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EUStringUtils;
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

    protected int capacity;

    @SideOnly(Side.CLIENT)
    public IIcon itemIconLinked;
    @SideOnly(Side.CLIENT)
    public IIcon[] iconParts;

    public ItemEnderBucket()
    {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(ReferenceBlocksItems.NAME_ITEM_ENDER_BUCKET);
        this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()) + ".32");
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        //this.setCapacity(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT);
        this.setCapacity(EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        // Do nothing on the client side
        if (world.isRemote == true)
        {
            return false;
        }

        if (this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack)) == true)
        {
            return true;
        }

        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && te instanceof IFluidHandler)
        {
            // If we are in bind mode, bind the bucket to the targeted tank and then bail out
            if (this.getBucketMode(stack) == OPERATION_MODE_BINDING)
            {
                super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
                return true;
            }

            this.useBucketOnTank(stack, player, world, x, y, z, side, this.getBucketMode(stack));

            return true;
        }

        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return true;
        }

        if (this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack)) == true)
        {
            return true;
        }

        return this.useBucketOnBlock(stack, player, world, x, y, z, side, hitX, hitY, hitZ, this.getBucketMode(stack));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        // Do nothing on the client side
        if (world.isRemote == true)
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
        FluidStack fluidStack = this.getFluidCached(stack);
        String fluidName;
        String preNr = EnumChatFormatting.BLUE.toString();
        String preTxt = EnumChatFormatting.DARK_GREEN.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        int amount = 0;
        int capacity = this.getCapacityCached(stack);

        if (fluidStack != null && fluidStack.getFluid() != null)
        {
            amount = fluidStack.amount;
            fluidName = preTxt + fluidStack.getFluid().getLocalizedName(fluidStack) + rst;
        }
        else
        {
            fluidName = StatCollector.translateToLocal("gui.tooltip.empty");
        }

        byte mode = this.getBucketMode(stack);
        byte linkMode = this.getBucketLinkMode(stack);

        String amountStr = String.format("%s%s%s mB / %s%s%s mB", preNr, EUStringUtils.formatNumberWithKSeparators(amount), rst, preNr, EUStringUtils.formatNumberWithKSeparators(capacity), rst);
        String modeStr = "gui.tooltip.bucket.mode.normal";
        if (mode == OPERATION_MODE_FILL_BUCKET) { modeStr = "gui.tooltip.bucket.mode.fill"; }
        else if (mode == OPERATION_MODE_DRAIN_BUCKET) { modeStr = "gui.tooltip.bucket.mode.drain"; }
        else if (mode == OPERATION_MODE_BINDING) { modeStr = "gui.tooltip.bucket.mode.bind"; }

        if (verbose == true)
        {
            if (linkMode == LINK_MODE_ENABLED)
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.cached.fluid") + ": " + fluidName);
                list.add(StatCollector.translateToLocal("gui.tooltip.cached.amount") + ": " + amountStr);

                ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
                if (linkCrystalStack != null)
                {
                    NBTHelperTarget target = new NBTHelperTarget();
                    if (target.readTargetTagFromNBT(linkCrystalStack.getTagCompound()) != null)
                    {
                        String targetName = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF).getDisplayName();
                        list.add(StatCollector.translateToLocal("gui.tooltip.linkedto") + ": " + preTxt + targetName + rst);
                    }
                }
            }
            else
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.fluid") + ": " + fluidName);
                list.add(StatCollector.translateToLocal("gui.tooltip.amount") + ": " + amountStr);
            }
        }
        else
        {
            if (linkMode == LINK_MODE_ENABLED)
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.cached.fluid.compact") + ": " + fluidName + " - " + amountStr);
            }
            else
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.fluid") + ": " + fluidName + " - " + amountStr);
            }
        }

        list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + StatCollector.translateToLocal(modeStr));

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

    /* Returns whether the bucket is currently set to link to a tank. */
    public byte getBucketLinkMode(ItemStack stack)
    {
        if (stack != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey("LinkMode") == true)
            {
                byte mode = nbt.getByte("LinkMode");
                if (mode == LINK_MODE_DISABLED || mode == LINK_MODE_ENABLED)
                {
                    return mode;
                }
            }
        }

        return LINK_MODE_DISABLED;
    }

    public boolean isTargetUsable(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side)
    {
        Block targetBlock = world.getBlock(x, y, z);
        // Spawn safe zone checks etc.
        if (targetBlock == null || targetBlock.getMaterial() == null
            || world.canMineBlock(player, x, y, x) == false
            || player.canPlayerEdit(x, y, z, side, stack) == false)
        {
            return false;
        }

        return true;
    }

    public boolean useBucketOnTank(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, byte bucketMode)
    {
        if (this.isTargetUsable(stack, player, world, x, y, z, side) == false)
        {
            return false;
        }

        this.setCapacity(EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));
        TileEntity te = world.getTileEntity(x, y, z);

        // Is this a TileEntity that is also some sort of a fluid storage device?
        if (te != null && te instanceof IFluidHandler)
        {
            IFluidHandler iFluidHandler = (IFluidHandler)te;
            FluidStack fluidStack;
            ForgeDirection forgeDir = ForgeDirection.getOrientation(side);
            Block block = world.getBlock(x, y, z);
            String blockName = Block.blockRegistry.getNameForObject(block);

            // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
            // work if we target a blue (=input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
            // but the top side should ever only be blue aka. input.
            if (blockName != null && blockName.equals("ThermalExpansion:Tank"))
            {
                forgeDir = ForgeDirection.UP;
            }

            // Get the stored fluid, if any
            FluidStack storedFluidStack = this.getFluid(stack);
            int storedFluidAmount = 0;

            if (storedFluidStack != null)
            {
                storedFluidAmount = storedFluidStack.amount;
            }

            // With tanks we pick up fluid when not sneaking
            if (bucketMode == OPERATION_MODE_FILL_BUCKET || (bucketMode == OPERATION_MODE_NORMAL && player.isSneaking() == false))
            {
                fluidStack = iFluidHandler.drain(forgeDir, FluidContainerRegistry.BUCKET_VOLUME, false); // simulate
                int amount = this.getCapacityAvailable(stack, fluidStack);

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
                        fluidStack = iFluidHandler.drain(forgeDir, amount, false);
                        if (fluidStack != null && this.fill(stack, fluidStack, false) == fluidStack.amount)
                        {
                            fluidStack = iFluidHandler.drain(forgeDir, amount, true); // actually drain
                            this.fill(stack, fluidStack, true);
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
                    fluidStack = this.drain(stack, FluidContainerRegistry.BUCKET_VOLUME, false);

                    // Check if we can deposit (at least some) the fluid we have stored
                    if (fluidStack != null && iFluidHandler.fill(forgeDir, fluidStack, false) > 0) // simulate
                    {
                        int amount = iFluidHandler.fill(forgeDir, fluidStack, true);
                        this.drain(stack, amount, true); // actually drain fluid from the bucket (the amount that was filled into the tank)
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean useBucketOnBlock(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, byte bucketMode)
    {
        // Non-fluid block, adjust the target block position to be the block touching the side we targeted
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        x += dir.offsetX;
        y += dir.offsetY;
        z += dir.offsetZ;

        if (this.isTargetUsable(stack, player, world, x, y, z, side) == false)
        {
            return false;
        }

        this.setCapacity(EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));

        // Get the stored fluid, if any
        FluidStack storedFluidStack = this.getFluid(stack);
        int storedFluidAmount = 0;

        if (storedFluidStack != null)
        {
            storedFluidAmount = storedFluidStack.amount;
        }

        if (world.getBlock(x, y, z).getMaterial().isLiquid() == true)
        {
            // Note: the side is technically wrong unless we ray trace it again, but it won't matter with fluid blocks... right?
            return this.useBucketOnFluidBlock(stack, world, player, x, y, z, side, bucketMode);
        }
        else
        {
            // target block is not fluid, try to place a fluid block in world in the adjusted block position
            if (storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
            {
                FluidStack fs = this.drain(stack, FluidContainerRegistry.BUCKET_VOLUME, false);
                if (fs != null && fs.amount == FluidContainerRegistry.BUCKET_VOLUME && this.tryPlaceFluidBlock(world, x, y, z, storedFluidStack) == true)
                {
                    this.drain(stack, FluidContainerRegistry.BUCKET_VOLUME, true);
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

        int x = mop.blockX;
        int y = mop.blockY;
        int z = mop.blockZ;

        Block block = world.getBlock(x, y, z);
        if (block != null && block.getMaterial() != null && block.getMaterial().isLiquid() == false)
        {
            return false;
        }

        return this.useBucketOnFluidBlock(stack, world, player, x, y, z, mop.sideHit, bucketMode);
    }

    public boolean useBucketOnFluidBlock(ItemStack stack, World world, EntityPlayer player, int x, int y, int z, int side, byte bucketMode)
    {
        Block targetBlock = world.getBlock(x, y, z);

        // Spawn safe zone checks etc.
        if (this.isTargetUsable(stack, player, world, x, y, z, side) == false || targetBlock.getMaterial().isLiquid() == false)
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
        FluidStack storedFluidStack = this.getFluid(stack);
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
            targetFluidStack = iFluidBlock.drain(world, x, y, z, false); // simulate
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
            && (storedFluidAmount == 0 || (this.getCapacityAvailable(stack, targetFluidStack) >= FluidContainerRegistry.BUCKET_VOLUME && storedFluidStack.isFluidEqual(targetFluidStack) &&
            (player.isSneaking() == false || bucketMode == OPERATION_MODE_FILL_BUCKET))))
        {
            // Implements IFluidBlock
            if (iFluidBlock != null)
            {
                if (iFluidBlock.canDrain(world, x, y, z) == true)
                {
                    targetFluidStack = iFluidBlock.drain(world, x, y, z, false); // simulate

                    // Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
                    if (targetFluidStack != null && this.fill(stack, targetFluidStack, false) == targetFluidStack.amount)
                    {
                        targetFluidStack = iFluidBlock.drain(world, x, y, z, true);
                        this.fill(stack, targetFluidStack, true);

                        return true;
                    }
                }

                return false;
            }

            // Does not implement IFluidBlock

            // Check that the fluid block we are trying to pick up is a source block, and that we can store that amount
            if (targetFluidStack != null && world.getBlockMetadata(x, y, z) == 0 && this.fill(stack, targetFluidStack, false) == targetFluidStack.amount)
            {
                if (world.setBlockToAir(x, y, z) == true)
                {
                    this.fill(stack, targetFluidStack, true);

                    return true;
                }
            }
        }

        // Fluid stored and not in fill-only mode, try to place fluid
        if (storedFluidStack != null && storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
        {
            // (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
            // The meta check is for ignoring flowing fluid blocks (ie. non-source blocks)
            if (storedFluidStack.isFluidEqual(targetFluidStack) == false || player.isSneaking() == true || world.getBlockMetadata(x, y, z) != 0)
            {
                FluidStack fs = this.drain(stack, FluidContainerRegistry.BUCKET_VOLUME, false);
                if (fs != null && fs.amount == FluidContainerRegistry.BUCKET_VOLUME && this.tryPlaceFluidBlock(world, x, y, z, storedFluidStack) == true)
                {
                    this.drain(stack, FluidContainerRegistry.BUCKET_VOLUME, true);
                    return true;
                }
            }

            return false;
        }

        return false;
    }

    /*
     *  Attempts to place one fluid block in the world, identified by the given FluidStack
     */
    public boolean tryPlaceFluidBlock(World world, int x, int y, int z, FluidStack fluidStack)
    {
        if (fluidStack == null || fluidStack.getFluid() == null || fluidStack.getFluid().canBePlacedInWorld() == false)
        {
            return false;
        }

        Block block = fluidStack.getFluid().getBlock();

        // We need to convert water and lava to the flowing variant, otherwise we get non-flowing source blocks
        if (block == Blocks.water) { block = Blocks.flowing_water; }
        else if (block == Blocks.lava) { block = Blocks.flowing_lava; }

        Material material = world.getBlock(x, y, z).getMaterial();

        if (world.isAirBlock(x, y, z) == false && material.isSolid() == true)
        {
            return false;
        }

        if (world.provider.isHellWorld && block == Blocks.flowing_water)
        {
            world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), (double)((float)z + 0.5F), "random.fizz", 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

            for (int l = 0; l < 8; ++l)
            {
                world.spawnParticle("largesmoke", (double)x + Math.random(), (double)y + Math.random(), (double)z + Math.random(), 0.0D, 0.0D, 0.0D);
            }
        }
        else
        {
            if (world.isRemote == false && material.isSolid() == false && material.isLiquid() == false)
            {
                // Set a replaceable block to air, and drop the items
                world.func_147480_a(x, y, z, true);
            }

            world.setBlock(x, y, z, block, 0, 3);
        }

        return true;
    }

    public ItemEnderBucket setCapacity(int capacity)
    {
        this.capacity = capacity;

        return this;
    }

    public int getCapacityCached(ItemStack stack)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
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

        return this.getCapacity(stack);
    }

    public int getCapacityAvailable(ItemStack stack, FluidStack fluidStackIn)
    {
        FluidStack fluidStack;

        // Linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidTankInfo[] info = tank.getTankInfo(targetData.forgeDir);
                fluidStack = tank.drain(targetData.forgeDir, Integer.MAX_VALUE, false);

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
                    return tank.fill(targetData.forgeDir, fs, false);
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

                        return tank.fill(targetData.forgeDir, fs, false);
                    }

                    // Since we have no fluid stored, get the capacity via simulating filling water into the tank
                    Fluid fluid = FluidRegistry.lookupFluidForBlock(Blocks.water);
                    if (fluid != null)
                    {
                        fluidStack = FluidRegistry.getFluidStack(fluid.getName(), Integer.MAX_VALUE);
                        if (fluidStack != null)
                        {
                            return tank.fill(targetData.forgeDir, fluidStack, false);
                        }
                    }
                }
            }

            return 0;
        }

        // Not linked to a tank, get the bucket's own free capacity
        fluidStack = this.getFluid(stack);
        if (fluidStack != null)
        {
            return this.getCapacity(stack) - fluidStack.amount;
        }

        return this.getCapacity(stack);
    }

    public void cacheFluid(ItemStack stack, FluidStack fluidStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
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
            this.setSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, moduleStack);
        }

        this.cacheCapacity(stack);
    }

    public void cacheCapacity(ItemStack stack)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
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
                    FluidTankInfo[] info = tank.getTankInfo(targetData.forgeDir);
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
                this.setSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, moduleStack);
            }
        }
    }

    public NBTHelperTarget getLinkedTankTargetData(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null)
        {
            return null;
        }

        NBTHelperTarget targetData = new NBTHelperTarget();
        if (targetData.readTargetTagFromNBT(moduleStack.getTagCompound()) == null)
        {
            return null;
        }

        // We fake always targeting the top side of Thermal Expansion Portable Tanks, because they only
        // work if we target a blue (=input) side. Only top and bottom sides are even possible, and bottom might be orange aka auto-output,
        // but the top side should ever only be blue aka. input.
        if (targetData.blockName != null && targetData.blockName.equals("ThermalExpansion:Tank"))
        {
            targetData.forgeDir = ForgeDirection.UP;
        }

        return targetData;
    }

    public IFluidHandler getLinkedTank(ItemStack stack)
    {
        NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
        if (targetData == null)
        {
            return null;
        }

        World world = MinecraftServer.getServer().worldServerForDimension(targetData.dimension);
        if (world == null)
        {
            return null;
        }

        // Force load the target chunk where the tank is located and for a 30 grace period after use
        if (ChunkLoading.getInstance().loadChunkForcedWithModTicket(targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 30) == false)
        {
            return null;
        }

        TileEntity te = world.getTileEntity(targetData.posX, targetData.posY, targetData.posZ);
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
            ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
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
        // Linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            FluidStack fluidStack;
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidTankInfo[] info = tank.getTankInfo(targetData.forgeDir);

                // If we have tank info, it is the easiest and simplest way to get the tank capacity
                if (info != null && info[0] != null)
                {
                    return info[0].capacity;
                }

                // No tank info available, get the capacity via simulating filling

                fluidStack = tank.drain(targetData.forgeDir, Integer.MAX_VALUE, false);

                // Tank has fluid
                if (fluidStack != null)
                {
                    FluidStack fs = fluidStack.copy();
                    fs.amount = Integer.MAX_VALUE;
                    int space = tank.fill(targetData.forgeDir, fs, false);

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
                            return tank.fill(targetData.forgeDir, fluidStack, false);
                        }
                    }
                }
            }

            return 0;
        }

        return this.capacity;
    }

    @Override
    public FluidStack getFluid(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            return null;
        }

        // The Bucket has been linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidStack fluidStack = tank.drain(targetData.forgeDir, Integer.MAX_VALUE, false);

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
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                if (UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * maxDrain), doDrain) == false)
                {
                    return null;
                }

                FluidStack fluidStack = tank.drain(targetData.forgeDir, maxDrain, doDrain);
                this.cacheFluid(stack, tank.drain(targetData.forgeDir, Integer.MAX_VALUE, false));

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
        if (fluidStackIn == null)
        {
            return 0;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                if (fluidStackIn != null && UtilItemModular.useEnderCharge(stack, (int)(ENDER_CHARGE_COST * fluidStackIn.amount), doFill) == false)
                {
                    return 0;
                }

                int amount = tank.fill(targetData.forgeDir, fluidStackIn, doFill);
                this.cacheFluid(stack, tank.drain(targetData.forgeDir, Integer.MAX_VALUE, false));

                return amount;
            }

            return 0;
        }

        int capacityAvailable = this.getCapacityAvailable(stack, fluidStackIn);
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
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString());
        this.itemIconLinked = iconRegister.registerIcon(this.getIconString() + ".linked");

        this.iconParts = new IIcon[12];
        this.iconParts[0] = iconRegister.registerIcon(this.getIconString() + ".main.normal");
        this.iconParts[1] = iconRegister.registerIcon(this.getIconString() + ".main.fill");
        this.iconParts[2] = iconRegister.registerIcon(this.getIconString() + ".main.drain");
        this.iconParts[3] = iconRegister.registerIcon(this.getIconString() + ".main.bind");
        this.iconParts[4] = iconRegister.registerIcon(this.getIconString() + ".inside");
        this.iconParts[5] = iconRegister.registerIcon(this.getIconString() + ".window");
        this.iconParts[6] = iconRegister.registerIcon(this.getIconString() + ".linked.main.normal");
        this.iconParts[7] = iconRegister.registerIcon(this.getIconString() + ".linked.main.fill");
        this.iconParts[8] = iconRegister.registerIcon(this.getIconString() + ".linked.main.drain");
        this.iconParts[9] = iconRegister.registerIcon(this.getIconString() + ".linked.main.bind");
        this.iconParts[10] = iconRegister.registerIcon(this.getIconString() + ".linked.inside");
        this.iconParts[11] = iconRegister.registerIcon(this.getIconString() + ".linked.window");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconPart(int i)
    {
        if (i >= this.iconParts.length)
        {
            i = 0;
        }
        return this.iconParts[i];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int metadata)
    {
        return 1;
    }

    /**
     * Return the correct icon for rendering based on the supplied ItemStack and render pass.
     *
     * Defers to {@link #getIconFromDamageForRenderPass(int, int)}
     * @param stack to render for
     * @param pass the multi-render pass
     * @return the icon
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        // FIXME we would actually need the whole texture for each mode...
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED && this.getBucketMode(stack) == OPERATION_MODE_NORMAL)
        {
            return this.itemIconLinked;
        }

        return this.itemIcon;
    }

    /* Returns the maximum number of modules that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 4;
    }

    /* Returns the maximum number of modules of the given type that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack, UtilItemModular.ModuleType moduleType)
    {
        if (moduleType.equals(UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(UtilItemModular.ModuleType.TYPE_LINKCRYSTAL))
        {
            return 3;
        }

        return 0;
    }

    /* Returns the maximum number of the given module that can be installed on this item.
     * This is for exact module checking, instead of the general module type. */
    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (UtilItemModular.getModuleType(moduleStack).equals(UtilItemModular.ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (UtilItemModular.getModuleType(moduleStack).equals(UtilItemModular.ModuleType.TYPE_LINKCRYSTAL))
        {
            // Only allow the inventory type Link Crystals
            if (moduleStack.getItemDamage() == 1)
            {
                return 3;
            }
        }

        return 0;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Control + Shift + Toggle mode: Toggle the bucket's link mode between regular-bucket-mode and linked-to-a-tank
        if (ReferenceKeys.keypressContainsShift(key) == true && ReferenceKeys.keypressContainsControl(key) == true
            && ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            byte val = LINK_MODE_DISABLED;
            NBTTagCompound nbt = stack.getTagCompound();

            if (nbt != null)
            {
                val = nbt.getByte("LinkMode");
            }
            else
            {
                nbt = new NBTTagCompound();
            }

            if (++val > LINK_MODE_ENABLED)
            {
                val = LINK_MODE_DISABLED;
            }

            nbt.setByte("LinkMode", val);
            stack.setTagCompound(nbt);

            return;
        }

        // Shift + Toggle mode: Change the selected link crystal
        if (ReferenceKeys.keypressContainsShift(key) == true)
        {
            super.doKeyBindingAction(player, stack, key);
            return;
        }

        // Just Toggle mode key: Change operation mode between normal, fill-only, drain-only and bind-to-tanks
        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            // 0: Normal, 1: Pickup only, 2: Deposit only
            byte val = OPERATION_MODE_NORMAL;
            NBTTagCompound nbt = stack.getTagCompound();

            if (nbt != null)
            {
                val = nbt.getByte("Mode");
            }
            else
            {
                nbt = new NBTTagCompound();
            }

            if (++val > OPERATION_MODE_BINDING)
            {
                val = OPERATION_MODE_NORMAL;
            }

            nbt.setByte("Mode", val);
            stack.setTagCompound(nbt);
        }
    }
}
