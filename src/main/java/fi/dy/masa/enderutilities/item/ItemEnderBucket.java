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
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBucket extends ItemLocationBoundModular implements IKeyBound, IFluidContainerItem
{
    public static final byte OPERATION_MODE_NORMAL = 0;
    public static final byte OPERATION_MODE_FILL_BUCKET = 1;
    public static final byte OPERATION_MODE_DRAIN_BUCKET = 2;
    public static final byte OPERATION_MODE_BINDING = 3;
    public static final byte LINK_MODE_DISABLED = 0;
    public static final byte LINK_MODE_ENABLED = 1;

    protected int capacity;

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

    // Note to future self: onItemUseFirst() just messes stuff up. Seems that I can't prevent onItemRightClick() from being called after it.
    // Thus the use logic just breaks when trying to use it. (ExU Drums work, but in-world fluids don't. Or something...)

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        System.out.println("onItemUseFirst() - " + (world.isRemote ? "client" : "server"));
        // Do nothing on the client side
        if (world.isRemote == true)
        {
            return false;
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
        /*else
        {
            this.useBucketOnBlock(stack, player, world, x, y, z, side, hitX, hitY, hitZ, this.getBucketMode(stack));
        }*/

        return false;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        //System.out.println("onItemUse() - " + (world.isRemote ? "client" : "server"));
        if (world.isRemote == true)
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

        System.out.println("onItemRightClick() - " + (world.isRemote ? "client" : "server"));

        this.useBucketOnFluidBlock(stack, world, player, this.getBucketMode(stack));
        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        FluidStack fluidStack = this.getFluid(stack);

        if (fluidStack != null && fluidStack.amount > 0 && fluidStack.getFluid() != null)
        {
            return super.getItemStackDisplayName(stack) + " (" + fluidStack.getFluid().getLocalizedName(fluidStack) + ")";
        }

        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        FluidStack fluidStack = this.getFluid(stack);
        String fluidName;
        String pre = "" + EnumChatFormatting.BLUE;
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;
        int amount = 0;

        if (fluidStack != null && fluidStack.getFluid() != null)
        {
            amount = fluidStack.amount;
            fluidName = pre + fluidStack.getFluid().getLocalizedName(fluidStack) + rst;
        }
        else
        {
            fluidName = "<" + StatCollector.translateToLocal("gui.tooltip.empty") + ">";
        }

        byte mode = this.getBucketMode(stack);

        String modeStr = "gui.tooltip.bucket.mode.normal";
        if (mode == OPERATION_MODE_FILL_BUCKET) { modeStr = "gui.tooltip.bucket.mode.fill"; }
        else if (mode == OPERATION_MODE_DRAIN_BUCKET) { modeStr = "gui.tooltip.bucket.mode.drain"; }
        else if (mode == OPERATION_MODE_BINDING) { modeStr = "gui.tooltip.bucket.mode.bind"; }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.cached.fluid") + ": " + fluidName);
            list.add(StatCollector.translateToLocal("gui.tooltip.cached.amount") + String.format(": %d mB", amount));
            list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + StatCollector.translateToLocal(modeStr));

            if (EnderUtilities.proxy.isShiftKeyDown() == false)
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.holdshift"));
                return;
            }

            super.addInformation(stack, player, list, par4);
        }
        else
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.fluid") + ": " + fluidName);
            list.add(StatCollector.translateToLocal("gui.tooltip.amount") + String.format(": %d mB", amount));
            list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + StatCollector.translateToLocal(modeStr));
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
        System.out.println("useBucketOnTank(): start; isRemote: " + world.isRemote);
        if (this.isTargetUsable(stack, player, world, x, y, z, side) == false)
        {
            return false;
        }

        this.setCapacity(EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));
        TileEntity te = world.getTileEntity(x, y, z);

        // Is this a TileEntity that is also some sort of a fluid storage device?
        if (te != null && te instanceof IFluidHandler)
        {
            System.out.println("useBucketOnTank(): tank start");
            IFluidHandler iFluidHandler = (IFluidHandler)te;
            FluidStack fluidStack;
            ForgeDirection fDir = ForgeDirection.getOrientation(side);

            // Get the stored fluid, if any
            FluidStack storedFluidStack = this.getStoredOrLinkedFluid(stack, true);
            int storedFluidAmount = 0;

            if (storedFluidStack != null)
            {
                storedFluidAmount = storedFluidStack.amount;
            }

            // With tanks we pick up fluid when not sneaking
            if (bucketMode == OPERATION_MODE_FILL_BUCKET || (bucketMode == OPERATION_MODE_NORMAL && player.isSneaking() == false))
            {
                System.out.println("useBucketOnTank(): fill bucket start");
                fluidStack = iFluidHandler.drain(fDir, FluidContainerRegistry.BUCKET_VOLUME, false); // simulate
                int amount = this.getCapacityAvailable(stack, fluidStack);

                // We can still store more fluid
                if (amount > 0)
                {
                    System.out.println("useBucketOnTank(): amount > 0: " + amount);
                    if (amount > FluidContainerRegistry.BUCKET_VOLUME)
                    {
                        amount = FluidContainerRegistry.BUCKET_VOLUME;
                    }

                    // If the bucket is currently empty, or the tank's fluid is the same we currently have
                    if (fluidStack != null && (storedFluidAmount == 0 || fluidStack.isFluidEqual(storedFluidStack) == true))
                    {
                        System.out.println("useBucketOnTank(): fill bucket actual, amount: " + amount);
                        fluidStack = iFluidHandler.drain(fDir, amount, true); // actually drain
                        this.fill(stack, fluidStack, true);
                        return true;
                    }
                }
            }
            // Sneaking or in drain-only mode, try to drain fluid from the bucket to the tank
            else
            {
                System.out.println("useBucketOnTank(): drain bucket to tank start");
                // Some fluid stored (we allow depositing less than a buckets worth of fluid into _tanks_)
                if (storedFluidAmount > 0)
                {
                    // simulate, we try to deposit up to one bucket per use
                    fluidStack = this.drain(stack, FluidContainerRegistry.BUCKET_VOLUME, false);

                    // Check if we can deposit (at least some) the fluid we have stored
                    if (fluidStack != null && iFluidHandler.fill(fDir, fluidStack, false) > 0) // simulate
                    {
                        int amount = iFluidHandler.fill(fDir, fluidStack, true);
                        System.out.println("useBucketOnTank(): drain bucket to tank actual, amount: " + amount);
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
        System.out.println("useBucketOnBlock(): start; isRemote: " + world.isRemote);

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
        FluidStack storedFluidStack = this.getStoredOrLinkedFluid(stack, true);
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
                System.out.println("useBucketOnBlock(): not fluid; placing in world");
                if (this.tryPlaceFluidBlock(world, x, y, z, storedFluidStack) == true)
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
        System.out.println("useBucketOnFluidBlock(): start; isRemote: " + world.isRemote);
        this.setCapacity(EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));

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

        return this.useBucketOnFluidBlock(stack, world, player, x, y, z, mop.sideHit, bucketMode);
    }

    public boolean useBucketOnFluidBlock(ItemStack stack, World world, EntityPlayer player, int x, int y, int z, int side, byte bucketMode)
    {
        Block targetBlock = world.getBlock(x, y, z);
        System.out.printf("useBucketOnFluidBlock(): targetBlock: %s %s %d (meta: %d) @ %d, %d, %d\n", targetBlock.getUnlocalizedName(), Block.blockRegistry.getNameForObject(targetBlock), Block.getIdFromBlock(targetBlock), world.getBlockMetadata(x, y, z), x, y, z);

        // Spawn safe zone checks etc.
        if (this.isTargetUsable(stack, player, world, x, y, z, side) == false || targetBlock.getMaterial().isLiquid() == false)
        {
            System.out.println("useBucketOnFluidBlock(): not usable or not fluid");
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
        FluidStack storedFluidStack = this.getStoredOrLinkedFluid(stack, true);
        FluidStack targetFluidStack = null;
        IFluidBlock iFluidBlock = null;
        int storedFluidAmount = 0;

        if (storedFluidStack != null)
        {
            storedFluidAmount = storedFluidStack.amount;
        }

        if (targetBlock instanceof IFluidBlock)
        {
            System.out.println("useBucketOnFluidBlock(): is IFluidBlock");
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
            System.out.println("useBucketOnFluidBlock(): fill bucket start");
            // Implements IFluidBlock
            if (iFluidBlock != null)
            {
                if (iFluidBlock.canDrain(world, x, y, z) == true)
                {
                    targetFluidStack = iFluidBlock.drain(world, x, y, z, false); // simulate

                    // Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
                    if (targetFluidStack != null && this.fill(stack, targetFluidStack, false) == targetFluidStack.amount)
                    {
                        System.out.println("useBucketOnFluidBlock(): fill from IFluidBlock actual");
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
                    System.out.println("useBucketOnFluidBlock(): fill from regular block actual");
                    this.fill(stack, targetFluidStack, true);

                    return true;
                }
            }
        }

        // Fluid stored and not in fill-only mode, try to place fluid
        if (storedFluidStack != null && storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_FILL_BUCKET)
        {
            System.out.println("useBucketOnFluidBlock(): drain bucket start");
            // (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
            // The meta check is for ignoring flowing fluid blocks (ie. non-source blocks)
            if (storedFluidStack.isFluidEqual(targetFluidStack) == false || player.isSneaking() == true || world.getBlockMetadata(x, y, z) != 0)
            {
                System.out.println("useBucketOnFluidBlock(): drain bucket trying to place");
                if (this.tryPlaceFluidBlock(world, x, y, z, storedFluidStack) == true)
                {
                    System.out.println("useBucketOnFluidBlock(): drain bucket, place fluid actual");
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
            System.out.println("tryPlaceFluidBlock(): bail out: error perror");
            return false;
        }

        Block block = fluidStack.getFluid().getBlock();

        // We need to convert water and lava to the flowing variant, otherwise we get non-flowing source blocks
        if (block == Blocks.water) { block = Blocks.flowing_water; }
        else if (block == Blocks.lava) { block = Blocks.flowing_lava; }

        Material material = world.getBlock(x, y, z).getMaterial();

        if (world.isAirBlock(x, y, z) == false && material.isSolid() == true)
        {
            System.out.println("tryPlaceFluidBlock(): bail out: solid");
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

    @Override
    public int getCapacity(ItemStack stack)
    {
        // TODO add a storage upgrade and store the capacity in NBT
        return this.capacity;
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
                fluidStack = tank.drain(ForgeDirection.getOrientation(targetData.blockFace), FluidContainerRegistry.BUCKET_VOLUME, false);

                if (fluidStack != null)
                {
                    fluidStack.amount = Integer.MAX_VALUE;

                    // Simulate filling as much as possible, and return the amount that the tank reports would have been filled
                    return tank.fill(ForgeDirection.getOrientation(targetData.blockFace), fluidStack, false);
                }
                else
                {
                    FluidStack fs = fluidStackIn.copy();
                    fs.amount = Integer.MAX_VALUE;
                    return tank.fill(ForgeDirection.getOrientation(targetData.blockFace), fs, false);
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

    @Override
    public FluidStack getFluid(ItemStack stack)
    {
        if (stack.getTagCompound() == null)
        {
            return null;
        }

        // The Bucket has been linked to a tank
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
            if (moduleStack != null && moduleStack.getTagCompound() != null
                && moduleStack.getTagCompound().hasKey("FluidCached", Constants.NBT.TAG_COMPOUND) == true)
            {
                return FluidStack.loadFluidStackFromNBT(moduleStack.getTagCompound().getCompoundTag("FluidCached"));
            }

            return null;
        }

        // Accessing the bucket's own storage
        if (stack.getTagCompound().hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == true)
        {
            return FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("Fluid"));
        }

        return null;
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
    }

    public FluidStack getStoredOrLinkedFluid(ItemStack stack, boolean doCache)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            System.out.println("getting linked fluid");
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidStack fluidStack = tank.drain(ForgeDirection.getOrientation(targetData.blockFace), Integer.MAX_VALUE, false);

                // Cache the fluid stack into the link crystal's NBT for easier/faster access for tooltip and rendering stuffs
                if (doCache == true)
                {
                    this.cacheFluid(stack, fluidStack);
                }

                return fluidStack;
            }

            return null;
        }

        // Not linked to a tank at the moment, get the internal FluidStack
        return this.getFluid(stack);
    }

    public NBTHelperTarget getLinkedTankTargetData(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null)
        {
            return null;
        }

        NBTTagCompound moduleNbt = moduleStack.getTagCompound();
        if (moduleNbt == null)
        {
            return null;
        }

        NBTHelperTarget targetData = new NBTHelperTarget();
        if (targetData.readTargetTagFromNBT(moduleNbt) == null)
        {
            return null;
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

        TileEntity te = world.getTileEntity(targetData.posX, targetData.posY, targetData.posZ);
        if (te == null || (te instanceof IFluidHandler) == false)
        {
            return null;
        }

        return (IFluidHandler)te;
    }

    @Override
    public FluidStack drain(ItemStack stack, int maxDrain, boolean doDrain)
    {
        System.out.println("drain() start; maxDrain: " + maxDrain + "; doDrain: " + doDrain);
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            System.out.println("remote tank drain start");
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                System.out.println("remote tank actual drain");
                FluidStack fluidStack = tank.drain(ForgeDirection.getOrientation(targetData.blockFace), maxDrain, doDrain);
                this.cacheFluid(stack, tank.drain(ForgeDirection.getOrientation(targetData.blockFace), Integer.MAX_VALUE, false));

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
        System.out.println("fill() start; doFill: " + doFill);
        if (fluidStackIn == null)
        {
            return 0;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            System.out.println("remote tank fill start");
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                System.out.println("remote tank actual fill");
                int amount = tank.fill(ForgeDirection.getOrientation(targetData.blockFace), fluidStackIn, doFill);
                this.cacheFluid(stack, tank.drain(ForgeDirection.getOrientation(targetData.blockFace), Integer.MAX_VALUE, false));

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
        this.iconParts = new IIcon[3];
        this.iconParts[0] = iconRegister.registerIcon(ReferenceTextures.getTextureName(this.getUnlocalizedName()) + ".32.main");
        this.iconParts[1] = iconRegister.registerIcon(ReferenceTextures.getTextureName(this.getUnlocalizedName()) + ".32.windowbg");
        this.iconParts[2] = iconRegister.registerIcon(ReferenceTextures.getTextureName(this.getUnlocalizedName()) + ".32.inside");
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

        // Just Toggle mode key: Change operation mode between normal, pickup-only, deposit-only
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
