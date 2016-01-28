package fi.dy.masa.enderutilities.item;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.BlockPosEU;
import fi.dy.masa.enderutilities.util.EntityUtils;
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

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemPortalScaler()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_PORTAL_SCALER);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        // If the player is standing inside a portal, then we try to activate the teleportation in onItemRightClick()
        if (EntityUtils.isEntityCollidingWithBlockSpace(world, player, Blocks.portal) == true)
        {
            return false;
        }

        Block block = world.getBlock(x, y, z);
        // When right clicking on a Nether Portal block, shut down the portal
        if (block == Blocks.portal)
        {
            if (world.isRemote == false)
            {
                world.setBlockToAir(x, y, z);
                world.playSoundEffect(x + 0.5d, y + 0.5d, z + 0.5d, block.stepSound.getBreakSound(), block.stepSound.getVolume() - 0.5f, block.stepSound.getPitch() * 0.8f);
            }

            return true;
        }

        ForgeDirection dir = ForgeDirection.getOrientation(side);
        x += dir.offsetX;
        y += dir.offsetY;
        z += dir.offsetZ;

        // When right clicking on Obsidian, try to light a Nether Portal
        if (block == Blocks.obsidian && world.isAirBlock(x, y, z) == true &&
            UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_PORTAL_ACTIVATION, false) == true && Blocks.portal.func_150000_e(world, x, y, z) == true)
        {
            if (world.isRemote == false)
            {
                UtilItemModular.useEnderCharge(stack, ENDER_CHARGE_COST_PORTAL_ACTIVATION, true);
                world.playAuxSFXAtEntity((EntityPlayer)null, 1009, x, y, z, 0); // Blaze fireball shooting sound
            }
            return true;
        }

        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == true || (player.dimension != 0 && player.dimension != -1) || this.itemHasScaleFactor(stack) == false)
        {
            return stack;
        }

        int dim = player.dimension == 0 ? -1 : 0;
        BlockPosEU normalDest = this.getNormalDestinationPosition(player, dim);
        BlockPosEU posDest = this.getDestinationPosition(stack, player, dim);
        int cost = this.getTeleportCost(player, posDest, dim);

        if (EntityUtils.isEntityCollidingWithBlockSpace(world, player, Blocks.portal) == true
            && UtilItemModular.useEnderCharge(stack, cost, false) == true)
        {
            TeleportEntityNetherPortal tp = new TeleportEntityNetherPortal();
            Entity entity = tp.travelToDimension(player, dim, posDest.posX, posDest.posY, posDest.posZ, 64, false);
            if (entity != null)
            {
                cost = this.getTeleportCost(normalDest.posX, normalDest.posY, normalDest.posZ, entity.posX, entity.posY, entity.posZ);
                UtilItemModular.useEnderCharge(stack, cost, true);
            }
        }

        return stack;
    }

    public BlockPosEU getDestinationPosition(ItemStack stack, EntityPlayer player, int dimension)
    {
        ItemStack cardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);
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
        if (dimension == -1)
        {
            dScaleX = 1.0d / dScaleX;
            dScaleY = 1.0d / dScaleY;
            dScaleZ = 1.0d / dScaleZ;
        }

        return new BlockPosEU((int)(player.posX * dScaleX), (int)(player.posY * dScaleY), (int)(player.posZ * dScaleZ));
    }

    public BlockPosEU getNormalDestinationPosition(EntityPlayer player, int destDim)
    {
        double x;
        double y;
        double z;

        // Going from Nether to Overworld
        if (destDim == 0)
        {
            x = player.posX * 8;
            y = player.posY;
            z = player.posZ * 8;
        }
        // Going from Overworld to Nether
        else
        {
            x = player.posX / 8;
            y = player.posY;
            z = player.posZ / 8;
        }

        return new BlockPosEU((int)x, (int)y, (int)z);
    }

    public int getTeleportCost(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        x1 = x1 - x2;
        y1 = y1 - y2;
        z1 = z1 - z2;
        double distDiff = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);

        return (int)(TELEPORTATION_EC_COST * distDiff);
    }

    public int getTeleportCost(EntityPlayer player, BlockPosEU dest, int destDim)
    {
        BlockPosEU normalDest = this.getNormalDestinationPosition(player, destDim);

        return this.getTeleportCost(normalDest.posX, normalDest.posY, normalDest.posZ, dest.posX, dest.posY, dest.posZ);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String str = "";
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                str = " " + EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC.toString() + moduleStack.getDisplayName() + rst;
            }

            NBTTagCompound moduleNbt = moduleStack.getTagCompound();
            if (this.memoryCardHasScaleFactor(moduleStack) == true)
            {
                NBTTagCompound tag = moduleNbt.getCompoundTag("PortalScaler");
                byte x = tag.getByte("scaleX");
                byte y = tag.getByte("scaleY");
                byte z = tag.getByte("scaleZ");
                String sx = x < 0 ? "1/" + (-x) : String.valueOf(x);
                String sy = y < 0 ? "1/" + (-y) : String.valueOf(y);
                String sz = z < 0 ? "1/" + (-z) : String.valueOf(z);
                str = str + String.format(" x: %s y: %s z: %s", sx, sy, sz);
                return super.getItemStackDisplayName(stack) + str + rst;
            }
        }

        return super.getItemStackDisplayName(stack) + str;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        ItemStack memoryCardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);

        String preBlue = EnumChatFormatting.BLUE.toString();
        String preWhiteIta = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        // Memory Cards installed
        if (memoryCardStack != null)
        {
            if (this.memoryCardHasScaleFactor(memoryCardStack) == true)
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
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nodata"));
            }

            if (verbose == true)
            {
                int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MEMORY_CARD);
                int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD) + 1;
                String dName = (memoryCardStack.hasDisplayName() ? preWhiteIta + memoryCardStack.getDisplayName() + rst + " " : "");
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short") + String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
            }
        }
        else
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nomemorycards"));
        }

        if (verbose == true)
        {
            // Ender Capacitor charge, if one has been installed
            ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
            if (capacitorStack != null && capacitorStack.getItem() instanceof ItemEnderCapacitor)
            {
                ((ItemEnderCapacitor)capacitorStack.getItem()).addInformation(capacitorStack, player, list, advancedTooltips);
            }
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Ctrl + (Shift + ) Toggle mode: Change selected Memory Card
        if (ReferenceKeys.keypressContainsControl(key) == true && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
        // Shift + (Ctrl + ) Alt + Toggle Mode: Change scaling factor
        else if (ReferenceKeys.keypressContainsShift(key) == true && ReferenceKeys.keypressContainsAlt(key) == true)
        {
            int amount = ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsControl(key) ? -1 : 1;
            this.changeCoordinateScaleFactor(stack, player, amount);
        }
    }

    public boolean memoryCardHasScaleFactor(ItemStack cardStack)
    {
        NBTTagCompound tag = NBTUtils.getCompoundTag(cardStack, "PortalScaler", false);
        if (tag != null &&
            tag.hasKey("scaleX", Constants.NBT.TAG_BYTE) &&
            tag.hasKey("scaleY", Constants.NBT.TAG_BYTE) &&
            tag.hasKey("scaleZ", Constants.NBT.TAG_BYTE))
        {
            return true;
        }

        return false;
    }

    public boolean itemHasScaleFactor(ItemStack stack)
    {
        ItemStack cardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);

        return cardStack != null && this.memoryCardHasScaleFactor(cardStack);
    }

    public void changeCoordinateScaleFactor(ItemStack stack, EntityPlayer player, int amount)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            NBTTagCompound tag = NBTUtils.getCompoundTag(moduleStack, "PortalScaler", true);

            int x = tag.hasKey("scaleX", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleX") : 8;
            int y = tag.hasKey("scaleY", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleY") : 1;
            int z = tag.hasKey("scaleZ", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleZ") : 8;

            ForgeDirection dir = EntityUtils.getLookingDirection(player);
            x += Math.abs(dir.offsetX) * amount;
            y += Math.abs(dir.offsetY) * amount;
            z += Math.abs(dir.offsetZ) * amount;

            // Hop over zero
            if (x == 0) { x += Math.abs(dir.offsetX) * amount; }
            if (y == 0) { y += Math.abs(dir.offsetY) * amount; }
            if (z == 0) { z += Math.abs(dir.offsetZ) * amount; }

            x = MathHelper.clamp_int(x, -64, 64);
            y = MathHelper.clamp_int(y, -64, 64);
            z = MathHelper.clamp_int(z, -64, 64);

            tag.setByte("scaleX", (byte)x);
            tag.setByte("scaleY", (byte)y);
            tag.setByte("scaleZ", (byte)z);

            this.setSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD, moduleStack);
        }
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

        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD))
        {
            return 4;
        }

        return 0;
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

        // Only allow the "Miscellaneous" type Memory Cards
        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD) == true && imodule.getModuleTier(moduleStack) != ItemEnderPart.MEMORY_CARD_TYPE_MISC)
        {
            return 0;
        }

        return this.getMaxModules(containerStack, moduleType);
    }
}
