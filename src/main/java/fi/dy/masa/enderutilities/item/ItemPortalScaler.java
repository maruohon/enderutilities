package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
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
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemPortalScaler extends ItemModular implements IKeyBound
{
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
            if (memoryCardHasScaleFactor(moduleStack) == true)
            {
                NBTTagCompound tag = moduleNbt.getCompoundTag("PortalScaler");
                str = str + String.format(" (x: %d y: %d z: %d)", tag.getByte("scaleX"), tag.getByte("scaleY"), tag.getByte("scaleZ"));
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
            if (memoryCardHasScaleFactor(memoryCardStack) == true)
            {
                NBTTagCompound tag = memoryCardStack.getTagCompound().getCompoundTag("PortalScaler");
                int x = tag.getByte("scaleX");
                int y = tag.getByte("scaleY");
                int z = tag.getByte("scaleZ");
                list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", preBlue, x, rst, preBlue, y, rst, preBlue, z, rst));
            }
            else
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nodata"));
            }

            if (verbose == true)
            {
                int num = UtilItemModular.getModuleCount(stack, ModuleType.TYPE_MEMORY_CARD);
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
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressContainsShift(key));
        }
        // Shift + (Ctrl + ) Alt + Toggle Mode: Change scaling factor
        else if (ReferenceKeys.keypressContainsShift(key) == true && ReferenceKeys.keypressContainsAlt(key) == true)
        {
            int amount = ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsControl(key) ? -1 : 1;
            this.changeCoordinateScaleFactor(stack, player, amount);
        }
    }

    public static boolean memoryCardHasScaleFactor(ItemStack cardStack)
    {
        NBTTagCompound nbt = cardStack.getTagCompound();
        if (nbt != null && nbt.hasKey("PortalScaler", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound tag = nbt.getCompoundTag("PortalScaler");
            if (tag.hasKey("scaleX", Constants.NBT.TAG_BYTE) && tag.hasKey("scaleY", Constants.NBT.TAG_BYTE) && tag.hasKey("scaleZ", Constants.NBT.TAG_BYTE))
            {
                return true;
            }
        }

        return false;
    }

    public void changeCoordinateScaleFactor(ItemStack stack, EntityPlayer player, int amount)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            NBTTagCompound moduleNbt = moduleStack.getTagCompound();
            if (moduleNbt == null)
            {
                moduleNbt = new NBTTagCompound();
                moduleStack.setTagCompound(moduleNbt);
            }

            NBTTagCompound tag = moduleNbt.getCompoundTag("PortalScaler");
            // getCompoundTag() is stupid and returns a new NBTTagCompound() if the requested key doesn't exist,
            // but IT DOESN'T ADD it to the map!!!
            if (moduleNbt.hasKey("PortalScaler", Constants.NBT.TAG_COMPOUND) == false)
            {
                //tag = new NBTTagCompound();
                moduleNbt.setTag("PortalScaler", tag);
            }

            int x = tag.hasKey("scaleX", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleX") : 8;
            int y = tag.hasKey("scaleY", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleY") : 8;
            int z = tag.hasKey("scaleZ", Constants.NBT.TAG_BYTE) ? tag.getByte("scaleZ") : 8;

            ForgeDirection dir = EntityUtils.getLookingDirection(player);
            x += Math.abs(dir.offsetX) * amount;
            y += Math.abs(dir.offsetY) * amount;
            z += Math.abs(dir.offsetZ) * amount;

            x = MathHelper.clamp_int(x, 1, 64);
            y = MathHelper.clamp_int(y, 1, 64);
            z = MathHelper.clamp_int(z, 1, 64);

            tag.setByte("scaleX", (byte)x);
            tag.setByte("scaleY", (byte)y);
            tag.setByte("scaleZ", (byte)z);

            this.setSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD, moduleStack);
        }
    }

    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 5;
    }

    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
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
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
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

        return this.getMaxModules(toolStack, moduleType);
    }
}
