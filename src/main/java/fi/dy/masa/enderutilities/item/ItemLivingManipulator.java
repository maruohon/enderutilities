package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemLivingManipulator extends ItemModular implements IKeyBound
{
    public static final int MAX_ENTITIES_PER_CARD = 16;
    public static final String WRAPPER_TAG_NAME = "LivingManipulator";
    public static final String TAG_NAME_MODE = "Mode";
    public static final String TAG_NAME_POS = "Position";

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemLivingManipulator()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_LIVING_MANIPULATOR);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return false;
        }

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.NORMAL || mode == Mode.RELEASE)
        {
            return this.releaseEntity(stack, world, x + hitX, y + hitY, z + hitZ, side);
        }

        return false;
    }

    public boolean handleInteraction(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        if (player.worldObj.isRemote == true)
        {
            return true;
        }

        Mode mode = Mode.getMode(stack);
        if (mode == Mode.RELEASE)
        {
            return this.releaseEntity(stack, player.worldObj, livingBase.posX, livingBase.posY, livingBase.posZ, 1);
        }

        return this.captureEntity(stack, player, livingBase);
    }

    public boolean captureEntity(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        if (livingBase == null || livingBase instanceof EntityPlayer || livingBase instanceof IBossDisplayData)
        {
            return false;
        }

        int count = this.getStoredEntityCount(stack);
        if (count < MAX_ENTITIES_PER_CARD)
        {
            this.storeEntity(stack, livingBase);
        }
        else
        {
            player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.memorycard.full"));
        }

        return false;
    }

    public boolean releaseEntity(ItemStack containerStack, World world, double x, double y, double z, int side)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return false;
        }

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);
        if (tagList == null || tagList.tagCount() == 0)
        {
            return false;
        }

        int current = NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current");
        int numEntities = tagList.tagCount();
        if (current >= numEntities)
        {
            current = (numEntities > 0) ? numEntities - 1 : 0;
        }

        NBTTagCompound tag = tagList.getCompoundTagAt(current);
        if (tag != null)
        {
            Entity entity = EntityList.createEntityFromNBT(tag, world);
            PositionHelper pos = new PositionHelper(x, y, z);
            pos.adjustPositionToTouchFace(entity, side);
            entity.setLocationAndAngles(pos.posX, pos.posY, pos.posZ, entity.rotationYaw, entity.rotationPitch);
            entity.motionY = 0.0;
            entity.fallDistance = 0.0f;
            entity.onGround = true;

            if (entity instanceof EntityLiving && this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                EntityUtils.applyMobPersistence((EntityLiving)entity);
            }

            world.spawnEntityInWorld(entity);
            tagList.removeTag(current);
        }

        numEntities = tagList.tagCount();
        if (current >= numEntities)
        {
            current = (numEntities > 0) ? numEntities - 1 : 0;
        }

        NBTUtils.setByte(moduleStack, WRAPPER_TAG_NAME, "Current", (byte)current);
        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD, moduleStack);

        return true;
    }

    public boolean storeEntity(ItemStack containerStack, EntityLivingBase livingBase)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return false;
        }

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, true);

        NBTTagCompound nbtEntity = new NBTTagCompound();
        livingBase.writeToNBTOptional(nbtEntity);

        tagList = NBTUtils.insertToTagList(tagList, nbtEntity, NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current"));
        NBTUtils.setTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", tagList);

        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD, moduleStack);

        //livingBase.setDead();
        livingBase.isDead = true;

        return true;
    }

    public int getStoredEntityCount(ItemStack containerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return 0;
        }

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);
        return tagList != null ? tagList.tagCount() : 0;
    }

    /**
     * Gets the current index in the currently selected Memory Card
     */
    public int getCurrentIndex(ItemStack containerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return 0;
        }

        return NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current");
    }

    public void setCurrentIndex(ItemStack containerStack, byte index)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return;
        }

        NBTUtils.setByte(moduleStack, WRAPPER_TAG_NAME, "Current", index);
        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD, moduleStack);
    }

    public String getEntityName(ItemStack containerStack, int index)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return null;
        }

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);
        if (tagList != null && tagList.tagCount() > index)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(index);
            if (tag != null)
            {
                String pre = EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC.toString();
                String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

                String name = tag.getString("CustomName");
                if (tag.hasKey("id", Constants.NBT.TAG_STRING))
                {
                    name = name.length() > 0 ? pre + name + rst + " (" + tag.getString("id") + ")" : tag.getString("id");
                }

                return name;
            }
        }

        return null;
    }

    public void changeEntitySelection(ItemStack containerStack, boolean reverse)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack == null)
        {
            return;
        }

        int numEntities = this.getStoredEntityCount(containerStack);
        int current = this.getCurrentIndex(containerStack);

        if (reverse == true)
        {
            if (--current < 0)
            {
                current = numEntities > 0 ? numEntities - 1 : 0;
            }
        }
        else
        {
            if (++current >= numEntities)
            {
                current = 0;
            }
        }

        NBTUtils.setByte(moduleStack, WRAPPER_TAG_NAME, "Current", (byte)current);
        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD, moduleStack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String str = super.getItemStackDisplayName(stack);
        String preGreen = EnumChatFormatting.GREEN.toString();
        String preGreenIta = preGreen + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD);
        if (moduleStack != null)
        {
            if (str.length() >= 14)
            {
                str = EUStringUtils.getInitialsWithDots(str);
            }

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                str = str + " " + preGreenIta + moduleStack.getDisplayName() + rst;
            }
            else
            {
                str = str + " MC: " + preGreen + (UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD) + 1) + rst;
            }

            int index = this.getCurrentIndex(stack);
            int count = this.getStoredEntityCount(stack);
            str = str + " E: " + preGreen + (index + 1) + "/" + count + rst;
            String entity = this.getEntityName(stack, index);
            if (entity != null)
            {
                str = str + " -> " + entity;
            }
        }

        return str;
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

        String preDGreen = EnumChatFormatting.DARK_GREEN.toString();
        String preBlue = EnumChatFormatting.BLUE.toString();
        String preWhiteIta = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + preDGreen + Mode.getMode(stack).getDisplayName() + rst);

        if (verbose == true)
        {
            // Item supports Jailer modules, show if one is installed
            if (this.getMaxModules(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                String s;
                if (this.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                {
                    s = StatCollector.translateToLocal("enderutilities.tooltip.item.jailer") + ": " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    s = StatCollector.translateToLocal("enderutilities.tooltip.item.jailer") + ": " + EnumChatFormatting.RED + StatCollector.translateToLocal("enderutilities.tooltip.item.no") + rst;
                }

                list.add(s);
            }
        }

        // Memory Cards installed
        if (memoryCardStack != null)
        {
            if (verbose == true)
            {
                int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MEMORY_CARD);
                int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD) + 1;
                String dName = (memoryCardStack.hasDisplayName() ? preWhiteIta + memoryCardStack.getDisplayName() + rst + " " : "");
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.selectedmemorycard.short") + String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));

                NBTTagList tagList = NBTUtils.getTagList(memoryCardStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);
                if (tagList == null)
                {
                    return;
                }

                int current = this.getCurrentIndex(stack);
                for (int i = 0; i < tagList.tagCount(); i++)
                {
                    NBTTagCompound tag = tagList.getCompoundTagAt(i);
                    if (tag != null)
                    {
                        String pre = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
                        String name = tag.getString("CustomName");

                        if (tag.hasKey("id", Constants.NBT.TAG_STRING))
                        {
                            name = name.length() > 0 ? pre + name + rst + " (" + tag.getString("id") + ")" : tag.getString("id");
                        }

                        name = (i == current) ? "-> " + name : "   " + name;
                        list.add(name);
                    }
                }
            }
        }
        else
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nomemorycards"));
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
        if (ReferenceKeys.keypressContainsControl(key) == true &&
            ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
        // Shift + Toggle Mode: Change entity selection within the current module
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == true &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeEntitySelection(stack, ReferenceKeys.keypressActionIsReversed(key));
        }
        // Just Toggle key, cycle the mode
        else if (ReferenceKeys.keypressContainsControl(key) == false &&
                 ReferenceKeys.keypressContainsShift(key) == false &&
                 ReferenceKeys.keypressContainsAlt(key) == false)
        {
            NBTUtils.cycleByteValue(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE, Mode.values().length - 1);
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
        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD))
        {
            return 4;
        }

        if (moduleType.equals(ModuleType.TYPE_MOBPERSISTENCE))
        {
            return 1;
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

    public enum Mode
    {
        NORMAL ("enderutilities.tooltip.item.normal"),
        CAPTURE ("enderutilities.tooltip.item.capture"),
        RELEASE ("enderutilities.tooltip.item.release");

        private String unlocName;

        Mode (String unlocName)
        {
            this.unlocName = unlocName;
        }

        public String getDisplayName()
        {
            return StatCollector.translateToLocal(this.unlocName);
        }

        public static Mode getMode(ItemStack stack)
        {
            return getMode(NBTUtils.getByte(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE));
        }

        public static Mode getMode(int id)
        {
            return (id >= 0 && id < values().length) ? values()[id] : NORMAL;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString());
        this.iconArray = new IIcon[3];
        this.iconArray[0] = iconRegister.registerIcon(this.getIconString());
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".capture");
        this.iconArray[2] = iconRegister.registerIcon(this.getIconString() + ".release");
    }

    @Override
    public IIcon getIconIndex(ItemStack stack)
    {
        return this.iconArray[Mode.getMode(stack).ordinal()];
    }
}
