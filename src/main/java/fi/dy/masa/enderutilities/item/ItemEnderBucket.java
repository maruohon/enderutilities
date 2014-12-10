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
    public static final byte OPERATION_MODE_PICKUP = 1;
    public static final byte OPERATION_MODE_DEPOSIT = 2;
    public static final int LINK_MODE_DISABLED = 0;
    public static final int LINK_MODE_ENABLED = 1;

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
        this.setCapacity(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT);
    }

    // Note to future self: onItemUseFirst() just messes stuff up. Seems that I can't prevent onItemRightClick() from being called after it.
    // Thus the use logic just breaks when trying to use it. (ExU Drums work, but in-world fluids don't. Or something...)

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        // Do nothing on the client side
        if (world.isRemote == true)
        {
            return true;
        }

        //System.out.println("onItemUse()");
        this.useBucket(stack, world, player);

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        // Do nothing on the client side
        if (world.isRemote == true)
        {
            return stack;
        }

        //System.out.println("onItemRightClick()");
        this.useBucket(stack, world, player);

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
        if (EnderUtilities.proxy.isShiftKeyDown() == false)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.holdshift"));
            return;
        }

        FluidStack fluidStack = this.getFluid(stack);
        String fluidName;
        String pre = "" + EnumChatFormatting.BLUE;
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;
        String modeStr = "gui.tooltip.bucket.mode.normal";
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

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.hasKey("Mode") == true)
        {
            byte mode = nbt.getByte("Mode");
            if (mode == OPERATION_MODE_PICKUP) { modeStr = "gui.tooltip.bucket.mode.pickup"; }
            else if (mode == OPERATION_MODE_DEPOSIT) { modeStr = "gui.tooltip.bucket.mode.deposit"; }
        }

        list.add(StatCollector.translateToLocal("gui.tooltip.fluid") + ": " + fluidName);
        list.add(StatCollector.translateToLocal("gui.tooltip.amount") + String.format(": %d mB", amount));
        list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + StatCollector.translateToLocal(modeStr));

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            super.addInformation(stack, player, list, par4);
        }
    }

    public boolean useBucket(ItemStack itemStack, World world, EntityPlayer player)
    {
        this.setCapacity(EUConfigs.enderBucketCapacity.getInt(ReferenceBlocksItems.ENDER_BUCKET_MAX_AMOUNT));

        byte bucketMode = this.getBucketMode(itemStack);
        // First, get the stored fluid, if any
        FluidStack storedFluidStack = this.getStoredOrLinkedFluid(itemStack, true);
        int storedFluidAmount = 0;

        if (storedFluidStack != null)
        {
            storedFluidAmount = storedFluidStack.amount;
        }

        // Next find out what block we are targeting
        // FIXME the boolean flag does what exactly? In vanilla it seems to indicate that the bucket is empty.
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (movingobjectposition == null || movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
        {
            return false;
        }

        int x = movingobjectposition.blockX;
        int y = movingobjectposition.blockY;
        int z = movingobjectposition.blockZ;

        Block targetBlock = world.getBlock(x, y, z);
        // Spawn safe zone checks etc.
        if (targetBlock == null || targetBlock.getMaterial() == null || world.canMineBlock(player, x, y, x) == false)
        {
            return false;
        }

        // Fluid block
        if (targetBlock.getMaterial().isLiquid() == true)
        {
            Fluid storedFluid = null;
            Fluid targetFluid = null;
            IFluidBlock iFluidBlock = null;
            FluidStack fluidStack = null;

            if (storedFluidStack != null)
            {
                storedFluid = storedFluidStack.getFluid();
            }

            if (targetBlock instanceof IFluidBlock)
            {
                iFluidBlock = (IFluidBlock)targetBlock;
                targetFluid = iFluidBlock.getFluid();
            }
            else
            {
                // We need to convert flowing water and lava to the still variant for logic stuffs
                // We will always convert them to the flowing variant before placing
                if (targetBlock == Blocks.flowing_water) { targetBlock = Blocks.water; }
                else if (targetBlock == Blocks.flowing_lava) { targetBlock = Blocks.lava; }

                //targetFluid = new Fluid(Block.blockRegistry.getNameForObject(targetBlock));
                targetFluid = FluidRegistry.lookupFluidForBlock(targetBlock);
            }

            // Empty || (space && not sneaking && same fluid) => trying to pick up fluid
            if (bucketMode != OPERATION_MODE_DEPOSIT && (storedFluidAmount == 0 ||
                (this.getCapacityAvailable(itemStack) >= FluidContainerRegistry.BUCKET_VOLUME && storedFluid.equals(targetFluid) &&
                (player.isSneaking() == false || bucketMode == OPERATION_MODE_PICKUP))))
            {
                if (player.canPlayerEdit(x, y, z, movingobjectposition.sideHit, itemStack) == false)
                {
                    return false;
                }

                // Implements IFluidBlock
                if (iFluidBlock != null)
                {
                    if (iFluidBlock.canDrain(world, x, y, z) == true)
                    {
                        fluidStack = iFluidBlock.drain(world, x, y, z, false); // simulate

                        // Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
                        if (fluidStack != null && this.fill(itemStack, fluidStack, false) == fluidStack.amount)
                        {
                            fluidStack = iFluidBlock.drain(world, x, y, z, true);
                            this.fill(itemStack, fluidStack, true);
                            return true;
                        }
                    }
                    return false;
                }

                // Does not implement IFluidBlock
                if (targetFluid != null)
                {
                    //fluidStack = new FluidStack(targetFluid, FluidContainerRegistry.BUCKET_VOLUME);
                    fluidStack = FluidRegistry.getFluidStack(targetFluid.getName(), FluidContainerRegistry.BUCKET_VOLUME);
                }

                // Check that we can store that amount and that the fluid stacks are equal (including NBT, excluding amount)
                if (this.fill(itemStack, fluidStack, false) >= FluidContainerRegistry.BUCKET_VOLUME)
                {
                    if (world.setBlockToAir(x, y, z) == true)
                    {
                        this.fill(itemStack, fluidStack, true);
                        return true;
                    }
                }
                return false;
            }

            // Fluid stored, trying to place fluid
            if (storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_PICKUP)
            {
                // (fluid stored && different fluid) || (fluid stored && same fluid && sneaking) => trying to place fluid
                if (storedFluid.equals(targetFluid) == false || player.isSneaking() == true)
                {
                    if (this.tryPlaceFluidBlock(world, x, y, z, storedFluidStack) == true)
                    {
                        this.drain(itemStack, FluidContainerRegistry.BUCKET_VOLUME, true);
                        return true;
                    }
                }
                return false;
            }
        }
        // Non-fluid block
        else
        {
            TileEntity te = world.getTileEntity(x, y, z);

            // Is this a TileEntity that is also some sort of a fluid storage device?
            if (te != null && te instanceof IFluidHandler)
            {
                IFluidHandler iFluidHandler = (IFluidHandler)te;
                FluidStack fluidStack;
                ForgeDirection fDir = ForgeDirection.getOrientation(movingobjectposition.sideHit);

                // With tanks we pick up fluid when not sneaking
                if (bucketMode == OPERATION_MODE_PICKUP || (player.isSneaking() == false && bucketMode != OPERATION_MODE_DEPOSIT))
                {
                    int amount = this.getCapacityAvailable(itemStack);

                    // We can still store more fluid
                    if (amount > 0)
                    {
                        if (amount > FluidContainerRegistry.BUCKET_VOLUME)
                        {
                            amount = FluidContainerRegistry.BUCKET_VOLUME;
                        }

                        fluidStack = iFluidHandler.drain(fDir, amount, false); // simulate

                        // If the bucket is currently empty, or the tank's fluid is the same we currently have
                        if (fluidStack != null && (storedFluidAmount == 0 || fluidStack.isFluidEqual(storedFluidStack) == true))
                        {
                            fluidStack = iFluidHandler.drain(fDir, amount, true); // actually drain
                            this.fill(itemStack, fluidStack, true);
                            return true;
                        }
                    }
                }
                // Sneaking, try to deposit fluid to the tank
                else
                {
                    // Some fluid stored (we allow depositing less than a buckets worth of fluid into _tanks_)
                    if (storedFluidAmount > 0)
                    {
                        // simulate, we try to deposit up to one bucket per use
                        fluidStack = this.drain(itemStack, FluidContainerRegistry.BUCKET_VOLUME, false);

                        // Check if we can deposit (at least some) the fluid we have stored
                        if (iFluidHandler.fill(fDir, fluidStack, false) > 0) // simulate
                        {
                            int amount = iFluidHandler.fill(fDir, fluidStack, true);
                            this.drain(itemStack, amount, true); // actually drain fluid from the bucket (the amount that was deposited into the container)
                            return true;
                        }
                    }
                }
            }

            // target block is not fluid and not a tank: try to place a fluid block in world against the targeted side
            else if (storedFluidAmount >= FluidContainerRegistry.BUCKET_VOLUME && bucketMode != OPERATION_MODE_PICKUP)
            {
                ForgeDirection dir = ForgeDirection.getOrientation(movingobjectposition.sideHit);
                x += dir.offsetX;
                y += dir.offsetY;
                z += dir.offsetZ;

                if (this.tryPlaceFluidBlock(world, x, y, z, storedFluidStack) == true)
                {
                    this.drain(itemStack, FluidContainerRegistry.BUCKET_VOLUME, true);
                    return true;
                }
            }
        }

        return false;
    }

    public byte getBucketMode(ItemStack stack)
    {
        if (stack != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null && nbt.hasKey("Mode") == true)
            {
                byte mode = nbt.getByte("Mode");
                if (mode >= OPERATION_MODE_NORMAL && mode <= OPERATION_MODE_DEPOSIT)
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

    /*
     *  Attempts to place one fluid block in the world, identified by the given FluidStack
     */
    public boolean tryPlaceFluidBlock(World world, int x, int y, int z, FluidStack fluidStack)
    {
        if (fluidStack == null || fluidStack.getFluid() == null ||
            fluidStack.getFluid().getBlock() == null || fluidStack.getFluid().canBePlacedInWorld() == false)
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

    @Override
    public int getCapacity(ItemStack stack)
    {
        // TODO add a storage upgrade and store the capacity in NBT
        return this.capacity;
    }

    public int getCapacityAvailable(ItemStack stack)
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
                fluidStack.amount = Integer.MAX_VALUE;
                // Simulate filling as much as possible, and return the amount that the tank reports would have been filled
                return tank.fill(ForgeDirection.getOrientation(targetData.blockFace), fluidStack, false);
            }

            return 0;
        }

        // Not linked to a tank, get the bucket's own free capacity
        fluidStack = this.getFluid(stack);
        if (fluidStack != null)
        {
            return this.getCapacity(stack) - fluidStack.amount;
        }

        return 0;
    }

    @Override
    public FluidStack getFluid(ItemStack stack)
    {
        if (stack.getTagCompound() == null)
        {
            return null;
        }

        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            if (stack.getTagCompound().hasKey("FluidCached", Constants.NBT.TAG_COMPOUND) == true)
            {
                return FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("FluidCached"));
            }

            return null;
        }

        if (stack.getTagCompound().hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == true)
        {
            return FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag("Fluid"));
        }

        return null;
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

    public FluidStack getStoredOrLinkedFluid(ItemStack stack, boolean doCache)
    {
        if (this.getBucketLinkMode(stack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(stack);
            IFluidHandler tank = this.getLinkedTank(stack);

            if (targetData != null && tank != null)
            {
                FluidStack fluidStack = tank.drain(ForgeDirection.getOrientation(targetData.blockFace), Integer.MAX_VALUE, false);

                // Cache the fluid stack into the bucket's NBT for easier/faster access for tooltip and rendering stuffs
                if (doCache == true)
                {
                    // Can't be null at this point
                    stack.getTagCompound().setTag("FluidCached", fluidStack.writeToNBT(new NBTTagCompound()));
                }

                return fluidStack;
            }

            return null;
        }

        // Not linked to a tank at the moment, get the internal FluidStack
        return this.getFluid(stack);
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
                return tank.drain(ForgeDirection.getOrientation(targetData.blockFace), maxDrain, doDrain);
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
    public int fill(ItemStack itemStack, FluidStack fluidStackIn, boolean doFill)
    {
        if (this.getBucketLinkMode(itemStack) == LINK_MODE_ENABLED)
        {
            NBTHelperTarget targetData = this.getLinkedTankTargetData(itemStack);
            IFluidHandler tank = this.getLinkedTank(itemStack);

            if (targetData != null && tank != null)
            {
                return tank.fill(ForgeDirection.getOrientation(targetData.blockFace), fluidStackIn, doFill);
            }

            return 0;
        }

        if (fluidStackIn == null) { return 0; }

        int capacity = this.getCapacity(itemStack);
        NBTTagCompound nbt = itemStack.getTagCompound();

        if (doFill == false)
        {
            if (nbt == null || nbt.hasKey("Fluid", Constants.NBT.TAG_COMPOUND) == false)
            {
                return Math.min(capacity, fluidStackIn.amount);
            }

            FluidStack storedFluidStack = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("Fluid"));

            if (storedFluidStack == null)
            {
                return Math.min(capacity, fluidStackIn.amount);
            }

            if (storedFluidStack.isFluidEqual(fluidStackIn) == false)
            {
                return 0;
            }

            return Math.min(capacity - storedFluidStack.amount, fluidStackIn.amount);
        }

        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            itemStack.setTagCompound(nbt);
        }

        if (nbt.hasKey("Fluid") == false)
        {
            NBTTagCompound fluidTag = fluidStackIn.writeToNBT(new NBTTagCompound());

            if (capacity < fluidStackIn.amount)
            {
                fluidTag.setInteger("Amount", capacity);
                nbt.setTag("Fluid", fluidTag);
                return capacity;
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

        int filled = capacity - storedFluidStack.amount;
        if (fluidStackIn.amount < filled)
        {
            storedFluidStack.amount += fluidStackIn.amount;
            filled = fluidStackIn.amount;
        }
        else
        {
            storedFluidStack.amount = capacity;
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

            if (++val > OPERATION_MODE_DEPOSIT)
            {
                val = OPERATION_MODE_NORMAL;
            }

            nbt.setByte("Mode", val);
            stack.setTagCompound(nbt);
        }
    }
}
