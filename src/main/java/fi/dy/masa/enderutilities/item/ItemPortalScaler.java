package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionUtils;
import fi.dy.masa.enderutilities.util.WorldUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;
import fi.dy.masa.enderutilities.util.teleport.TeleportEntityNetherPortal;

public class ItemPortalScaler extends ItemModular implements IKeyBound
{
    public static final int ENDER_CHARGE_COST_PORTAL_ACTIVATION = 500;
    // Ender Charge cost per block of distance change compared to a vanilla portal use at the same location
    // 1 EC per ~77 blocks "saved". This should just about allow for the maximum distance at scale 64 at x and z axis
    // at the world's edge, with the Advanced capacitor (500k EC).
    public static final float TELEPORTATION_EC_COST = 0.013f;

    public ItemPortalScaler(String name)
    {
        super(name);

        this.setMaxStackSize(1);
        this.setMaxDamage(0);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        // If the player is standing inside a portal, then we try to activate the teleportation in onItemRightClick()
        if (EntityUtils.isEntityCollidingWithBlockSpace(world, player, Blocks.PORTAL))
        {
            return EnumActionResult.PASS;
        }

        Block block = world.getBlockState(pos).getBlock();

        // When right clicking on a Nether Portal block, shut down the portal
        if (block == Blocks.PORTAL)
        {
            if (world.isRemote == false && world.isBlockModifiable(player, pos))
            {
                world.destroyBlock(pos, false);
            }

            return EnumActionResult.SUCCESS;
        }

        if (world.isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        ItemStack stack = player.getHeldItem(hand);

        // When right clicking on Obsidian, try to light a Nether Portal
        if (block == Blocks.OBSIDIAN && world.isAirBlock(pos.offset(side)) && world.isBlockModifiable(player, pos.offset(side)) &&
            UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_PORTAL_ACTIVATION, true) &&
            Blocks.PORTAL.trySpawnPortal(world, pos.offset(side)))
        {
            UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_PORTAL_ACTIVATION, false);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.MASTER, 0.8f, 1.0f);

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);

        if (EntityUtils.isEntityCollidingWithBlockSpace(world, player, Blocks.PORTAL) == false)
        {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        }

        if (world.isRemote == false)
        {
            this.usePortalWithPortalScaler(stack, world, player);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrent)
    {
        int dim = world.provider.getDimension();

        if (world.isRemote == false &&
            EnergyBridgeTracker.dimensionHasEnergyBridge(dim) &&
            (WorldUtils.isEndDimension(world) || EnergyBridgeTracker.endHasEnergyBridges()))
        {
            UtilItemModular.addEnderCharge(stack, ItemEnderCapacitor.CHARGE_RATE_FROM_ENERGY_BRIDGE, true);
        }
    }

    public boolean usePortalWithPortalScaler(ItemStack stack, World world, EntityPlayer player)
    {
        int dimSrc = player.getEntityWorld().provider.getDimension();

        if ((dimSrc != 0 && dimSrc != -1) || this.itemHasScaleFactor(stack) == false)
        {
            return false;
        }

        int dimDst = dimSrc == 0 ? -1 : 0;
        Vec3d normalDest = this.getNormalDestinationPosition(player, dimDst);
        Vec3d posDest = this.getDestinationPosition(stack, player, dimDst);
        int cost = this.getTeleportCostEstimate(player, posDest, dimDst);
        //System.out.printf("cost estimate: %d normal: %s new: %s\n", cost, normalDest, posDest);

        if (UtilItemModular.useEnderCharge(stack, cost, true))
        {
            TeleportEntityNetherPortal tp = new TeleportEntityNetherPortal();
            Entity entity = tp.travelToDimension(player, dimDst, new BlockPos(posDest), 32, false);

            if (entity != null)
            {
                cost = this.getTeleportCost(normalDest, entity.getPositionVector());
                UtilItemModular.useEnderCharge(stack, cost, false);
                return true;
            }
        }
        else
        {
            player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.notenoughendercharge"), true);
        }

        return false;
    }

    public Vec3d getDestinationPosition(ItemStack stack, EntityPlayer player, int destDimension)
    {
        ItemStack cardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);
        NBTTagCompound moduleNbt = cardStack.getTagCompound();
        NBTTagCompound tag = moduleNbt.getCompoundTag("PortalScaler");
        byte scaleX = tag.getByte("scaleX");
        byte scaleY = tag.getByte("scaleY");
        byte scaleZ = tag.getByte("scaleZ");

        // Don't divide by zero on accident!!
        if (scaleX == 0) { scaleX = 8; }
        if (scaleY == 0) { scaleY = 1; }
        if (scaleZ == 0) { scaleZ = 8; }

        double dScaleX = scaleX;
        double dScaleY = scaleY;
        double dScaleZ = scaleZ;

        if (scaleX < 0) { dScaleX = -1.0d / (double)scaleX; }
        if (scaleY < 0) { dScaleY = -1.0d / (double)scaleY; }
        if (scaleZ < 0) { dScaleZ = -1.0d / (double)scaleZ; }

        // Going from the Overworld to the Nether
        if (destDimension == DimensionType.NETHER.getId())
        {
            dScaleX = 1.0d / dScaleX;
            dScaleY = 1.0d / dScaleY;
            dScaleZ = 1.0d / dScaleZ;
        }

        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(destDimension);
        return PositionUtils.getScaledClampedPosition(player.getPositionVector(), world, dScaleX, dScaleY, dScaleZ, 32);
    }

    public Vec3d getNormalDestinationPosition(EntityPlayer player, int destDimension)
    {
        double scale = destDimension == DimensionType.OVERWORLD.getId() ? 8d : 1d / 8d;
        return PositionUtils.getScaledClampedPosition(player.getPositionVector(), null, scale, 1d, scale, 32);
    }

    /**
     * Returns the cost of teleportation for the amount of distance between the given coordinates
     */
    public int getTeleportCost(Vec3d pos1, Vec3d pos2)
    {
        double xDiff = pos1.x - pos2.x;
        double yDiff = pos1.y - pos2.y;
        double zDiff = pos1.z - pos2.z;

        return (int)(TELEPORTATION_EC_COST * Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff));
    }

    public int getTeleportCostEstimate(EntityPlayer player, Vec3d dest, int destDim)
    {
        Vec3d normalDest = this.getNormalDestinationPosition(player, destDim);
        return this.getTeleportCost(normalDest, dest);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String str = "";
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() == false)
        {
            String preGreen = TextFormatting.GREEN.toString();
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName())
            {
                str = " " + preGreen + TextFormatting.ITALIC.toString() + moduleStack.getDisplayName() + rst;
            }

            if (this.memoryCardHasScaleFactor(moduleStack))
            {
                NBTTagCompound tag = moduleStack.getTagCompound().getCompoundTag("PortalScaler");
                byte x = tag.getByte("scaleX");
                byte y = tag.getByte("scaleY");
                byte z = tag.getByte("scaleZ");
                String sx = preGreen + (x < 0 ? "1/" + (-x) : String.valueOf(x)) + rst;
                String sy = preGreen + (y < 0 ? "1/" + (-y) : String.valueOf(y)) + rst;
                String sz = preGreen + (z < 0 ? "1/" + (-z) : String.valueOf(z)) + rst;
                str = str + String.format(" x: %s y: %s z: %s", sx, sy, sz);

                return super.getItemStackDisplayName(stack) + str + rst;
            }
        }

        return super.getItemStackDisplayName(stack) + str;
    }

    @Override
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(I18n.format("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        ItemStack memoryCardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);

        String preBlue = TextFormatting.BLUE.toString();
        String preWhiteIta = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        // Memory Cards installed
        if (memoryCardStack.isEmpty() == false)
        {
            if (this.memoryCardHasScaleFactor(memoryCardStack))
            {
                NBTTagCompound tag = memoryCardStack.getTagCompound().getCompoundTag("PortalScaler");
                byte x = tag.getByte("scaleX");
                byte y = tag.getByte("scaleY");
                byte z = tag.getByte("scaleZ");
                String sx = x < 0 ? "1/" + (-x) : String.valueOf(x);
                String sy = y < 0 ? "1/" + (-y) : String.valueOf(y);
                String sz = z < 0 ? "1/" + (-z) : String.valueOf(z);

                list.add(String.format("x: %s%s%s y: %s%s%s z: %s%s%s", preBlue, sx, rst, preBlue, sy, rst, preBlue, sz, rst));
            }
            else
            {
                list.add(I18n.format("enderutilities.tooltip.item.nodata"));
            }

            if (verbose)
            {
                int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MEMORY_CARD_MISC);
                int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD_MISC) + 1;
                String dName = (memoryCardStack.hasDisplayName() ? preWhiteIta + memoryCardStack.getDisplayName() + rst + " " : "");

                list.add(I18n.format("enderutilities.tooltip.item.selectedmemorycard.short") +
                         String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
            }
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.nomemorycards"));
        }

        if (verbose)
        {
            // Ender Capacitor charge, if one has been installed
            ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);

            if (capacitorStack.isEmpty() == false && capacitorStack.getItem() instanceof ItemEnderCapacitor)
            {
                ((ItemEnderCapacitor) capacitorStack.getItem()).addTooltipLines(capacitorStack, player, list, verbose);
            }
        }
    }

    @Override
    public boolean doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Ctrl + (Shift + ) Toggle mode: Change selected Memory Card
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
            EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_MISC,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
            return true;
        }
        // Shift + (Ctrl + ) Alt + Toggle Mode: Change scaling factor
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_ALT, HotKeys.MOD_CTRL))
        {
            int amount = EnumKey.keypressContainsControl(key) ? 1 : -1;
            this.changeCoordinateScaleFactor(stack, player, amount);
            return true;
        }
        // Shift + Alt + Scroll: Change scaling factor
        else if (EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT_ALT))
        {
            int amount = EnumKey.keypressActionIsReversed(key) ? 1 : -1;
            this.changeCoordinateScaleFactor(stack, player, amount);
            return true;
        }

        return false;
    }

    public boolean memoryCardHasScaleFactor(ItemStack cardStack)
    {
        NBTTagCompound tag = NBTUtils.getCompoundTag(cardStack, "PortalScaler", false);

        return tag != null && tag.hasKey("scaleX", Constants.NBT.TAG_BYTE) &&
            tag.hasKey("scaleY", Constants.NBT.TAG_BYTE) && tag.hasKey("scaleZ", Constants.NBT.TAG_BYTE);
    }

    public boolean itemHasScaleFactor(ItemStack stack)
    {
        ItemStack cardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);

        return cardStack.isEmpty() == false && this.memoryCardHasScaleFactor(cardStack);
    }

    public void changeCoordinateScaleFactor(ItemStack stack, EntityPlayer player, int amount)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty())
        {
            return;
        }

        NBTTagCompound tag = NBTUtils.getCompoundTag(moduleStack, "PortalScaler", true);

        int x = tag.hasKey("scaleX", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleX") : 8;
        int y = tag.hasKey("scaleY", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleY") : 1;
        int z = tag.hasKey("scaleZ", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleZ") : 8;

        EnumFacing facing = EntityUtils.getLookingDirection(player);
        x += Math.abs(facing.getXOffset()) * amount;
        y += Math.abs(facing.getYOffset()) * amount;
        z += Math.abs(facing.getZOffset()) * amount;

        // Hop over zero and 1/1 (ie. -1)
        if (x == -1) { x += Math.abs(facing.getXOffset()) * amount * 2; }
        if (y == -1) { y += Math.abs(facing.getYOffset()) * amount * 2; }
        if (z == -1) { z += Math.abs(facing.getZOffset()) * amount * 2; }

        // Hop over zero
        if (x == 0) { x += Math.abs(facing.getXOffset()) * amount * 2; }
        if (y == 0) { y += Math.abs(facing.getYOffset()) * amount * 2; }
        if (z == 0) { z += Math.abs(facing.getZOffset()) * amount * 2; }

        x = MathHelper.clamp(x, -64, 64);
        y = MathHelper.clamp(y, -64, 64);
        z = MathHelper.clamp(z, -64, 64);

        tag.setByte("scaleX", (byte)x);
        tag.setByte("scaleY", (byte)y);
        tag.setByte("scaleZ", (byte)z);

        this.setSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 5;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD_MISC))
        {
            return 4;
        }

        return 0;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack.isEmpty() || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        return this.getMaxModules(containerStack, ((IModule) moduleStack.getItem()).getModuleType(moduleStack));
    }
}
