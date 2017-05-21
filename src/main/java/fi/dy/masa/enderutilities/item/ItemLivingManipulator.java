package fi.dy.masa.enderutilities.item;

import java.util.List;
import javax.annotation.Nullable;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PositionHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemLivingManipulator extends ItemModular implements IKeyBound
{
    private static final int MAX_ENTITIES_PER_CARD = 16;
    private static final String WRAPPER_TAG_NAME = "LivingManipulator";
    private static final String TAG_NAME_CACHE = "InfoCache";
    private static final String TAG_NAME_MODE = "Mode";

    public ItemLivingManipulator()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_LIVING_MANIPULATOR);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        Mode mode = Mode.getMode(stack);

        if (mode == Mode.NORMAL || mode == Mode.RELEASE)
        {
            if (world.isRemote)
            {
                return EnumActionResult.SUCCESS;
            }

            return this.releaseEntity(stack, world, pos, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, side);
        }

        return EnumActionResult.PASS;
    }

    public EnumActionResult handleInteraction(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        if (player.getEntityWorld().isRemote)
        {
            return EnumActionResult.SUCCESS;
        }

        Mode mode = Mode.getMode(stack);

        if (mode == Mode.RELEASE)
        {
            return this.releaseEntity(stack, player.getEntityWorld(), livingBase.getPosition(),
                    livingBase.posX, livingBase.posY, livingBase.posZ, EnumFacing.UP);
        }

        return this.captureEntity(stack, player, livingBase);
    }

    private EnumActionResult captureEntity(ItemStack stack, EntityPlayer player, EntityLivingBase livingBase)
    {
        if (livingBase == null || livingBase instanceof EntityPlayer)
        {
            return EnumActionResult.PASS;
        }

        int count = this.getStoredEntityCount(stack);

        if (count < MAX_ENTITIES_PER_CARD)
        {
            return this.storeEntity(stack, livingBase);
        }

        player.sendStatusMessage(new TextComponentTranslation("enderutilities.chat.message.memorycard.full"), true);

        return EnumActionResult.FAIL;
    }

    private EnumActionResult releaseEntity(ItemStack manipulatorStack, World world, BlockPos pos, double x, double y, double z, EnumFacing side)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty())
        {
            return EnumActionResult.PASS;
        }

        NBTTagList listEntities = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);

        if (listEntities == null || listEntities.tagCount() == 0)
        {
            return EnumActionResult.PASS;
        }

        int current = NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current");
        int numEntities = listEntities.tagCount();

        if (current >= numEntities)
        {
            current = (numEntities > 0) ? numEntities - 1 : 0;
        }

        NBTTagCompound tag = listEntities.getCompoundTagAt(current);

        if (tag != null)
        {
            boolean isShulker = false;

            if (tag.getString("id").equals("minecraft:shulker") || tag.getString("id").equals("Shulker"))
            {
                // Special case to update the Shulker's attached position and position
                if (tag.hasKey("APX", Constants.NBT.TAG_INT))
                {
                    int xi = pos.getX() + side.getFrontOffsetX();
                    int yi = pos.getY() + side.getFrontOffsetY();
                    int zi = pos.getZ() + side.getFrontOffsetZ();

                    tag.setTag("Pos", NBTUtils.writeDoubles(new double[] {xi + 0.5d, yi, zi + 0.5d}));
                    tag.setInteger("APX", xi);
                    tag.setInteger("APY", yi);
                    tag.setInteger("APZ", zi);
                    tag.setByte("AttachFace", (byte)side.getIndex());
                    isShulker = true;
                }
            }

            Entity entity = EntityList.createEntityFromNBT(tag, world);

            if (entity == null)
            {
                return EnumActionResult.FAIL;
            }

            if (isShulker == false)
            {
                PositionHelper posHelper = new PositionHelper(x, y, z);
                posHelper.adjustPositionToTouchFace(entity, side);
                entity.setLocationAndAngles(posHelper.posX, posHelper.posY, posHelper.posZ, entity.rotationYaw, entity.rotationPitch);
            }

            entity.motionY = 0.0;
            entity.fallDistance = 0.0f;
            entity.onGround = true;

            if (entity instanceof EntityLiving && this.getInstalledModuleCount(manipulatorStack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                EntityUtils.applyMobPersistence((EntityLiving)entity);
            }

            world.spawnEntity(entity);
            listEntities.removeTag(current);
        }

        numEntities = listEntities.tagCount();

        if (current >= numEntities)
        {
            current = (numEntities > 0) ? numEntities - 1 : 0;
        }

        NBTUtils.setByte(moduleStack, WRAPPER_TAG_NAME, "Current", (byte)current);
        this.setSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);

        int selectedCard = this.getSelectedCardIndex(manipulatorStack, false);
        this.updateCachedEntityInfo(manipulatorStack, listEntities, selectedCard, current);

        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult storeEntity(ItemStack manipulatorStack, EntityLivingBase livingBase)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty() || this.canStoreEntity(livingBase) == false)
        {
            return EnumActionResult.PASS;
        }

        // Dismount the entity from its rider and the entity it's riding, if any
        livingBase.dismountRidingEntity();
        livingBase.removePassengers();

        NBTTagCompound nbtEntity = new NBTTagCompound();

        if (livingBase.writeToNBTOptional(nbtEntity) == false)
        {
            return EnumActionResult.FAIL;
        }

        String str = EntityList.getEntityString(livingBase);

        if (str != null)
        {
            nbtEntity.setString("EntityString", str);
        }

        NBTTagList listEntities = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, true);

        int index = NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current");
        listEntities = NBTUtils.insertToTagList(listEntities, nbtEntity, index);
        NBTUtils.setTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", listEntities);

        int selectedCard = this.getSelectedCardIndex(manipulatorStack, false);
        this.updateCachedEntityInfo(manipulatorStack, listEntities, selectedCard, index);
        this.setSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);

        livingBase.isDead = true;

        return EnumActionResult.SUCCESS;
    }

    private void updateCachedEntityInfo(ItemStack manipulatorStack, @Nullable NBTTagList listEntities, int cardIndex, int selectedEntity)
    {
        int count = 0;

        NBTUtils.setByte(manipulatorStack, TAG_NAME_CACHE, "Current_" + cardIndex, (byte) selectedEntity);

        if (listEntities != null)
        {
            NBTTagList listInfo = new NBTTagList();
            count = listEntities.tagCount();

            for (int i = 0; i < count; i++)
            {
                String str = this.getDisplayStringForEntityFromEntityNBT(listEntities.getCompoundTagAt(i), false);

                if (str == null)
                {
                    str = "ERROR: Could not get entity name";
                }

                listInfo.appendTag(new NBTTagString(str));
            }

            NBTUtils.setTagList(manipulatorStack, TAG_NAME_CACHE, "Entities_" + cardIndex, listInfo);
        }
        else
        {
            NBTTagCompound tag = NBTUtils.getCompoundTag(manipulatorStack, TAG_NAME_CACHE, true);
            tag.removeTag("Entities_" + cardIndex);
        }

        NBTUtils.setByte(manipulatorStack, TAG_NAME_CACHE, "Count_" + cardIndex, (byte) count);
    }

    private int getStoredEntityCount(ItemStack manipulatorStack)
    {
        int cardIndex = this.getSelectedCardIndex(manipulatorStack, false);
        return NBTUtils.getByte(manipulatorStack, TAG_NAME_CACHE, "Count_" + cardIndex);
    }

    /**
     * Gets the current index in the currently selected Memory Card
     */
    private int getCurrentIndex(ItemStack manipulatorStack, boolean cached)
    {
        if (cached)
        {
            int cardIndex = this.getSelectedCardIndex(manipulatorStack, false);
            return NBTUtils.getByte(manipulatorStack, TAG_NAME_CACHE, "Current_" + cardIndex);
        }
        else
        {
            ItemStack moduleStack = this.getSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);
            return moduleStack.isEmpty() == false ? NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current") : 0;
        }
    }

    @Nullable
    private String getEntityName(ItemStack manipulatorStack, int index, boolean useDisplayNameFormatting)
    {
        int cardIndex = this.getSelectedCardIndex(manipulatorStack, false);
        NBTTagList tagList = NBTUtils.getTagList(manipulatorStack, TAG_NAME_CACHE, "Entities_" + cardIndex, Constants.NBT.TAG_STRING, false);
        return tagList != null && tagList.tagCount() > index ? tagList.getStringTagAt(index) : null;
    }

    @Nullable
    private String getDisplayStringForEntityFromEntityNBT(NBTTagCompound tag, boolean useDisplayNameFormatting)
    {
        if (tag != null)
        {
            String pre = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
            String rst = TextFormatting.RESET.toString() + (useDisplayNameFormatting ? TextFormatting.WHITE.toString() : TextFormatting.GRAY.toString());

            String name = tag.getString("CustomName");
            String id = null;

            // EntityString added in the 1.11.2 port, because the id is now the
            // registry name/ResourceLocation, and is not usable in the translation anymore
            if (tag.hasKey("EntityString", Constants.NBT.TAG_STRING))
            {
                id = tag.getString("EntityString");
            }
            else if (tag.hasKey("id", Constants.NBT.TAG_STRING))
            {
                id = tag.getString("id");
            }

            if (id != null)
            {
                @SuppressWarnings("deprecation")
                String translated = net.minecraft.util.text.translation.I18n.translateToLocal("entity." + id + ".name");

                // Translation found
                if (id.equals(translated) == false)
                {
                    id = translated;
                }

                name = name.length() > 0 ? pre + name + rst + " (" + id + ")" : id;

                if (tag.hasKey("Health"))
                {
                    name += String.format(" (%.1f HP)", tag.getFloat("Health"));
                }
            }

            return name;
        }

        return null;
    }

    private void changeEntitySelection(ItemStack manipulatorStack, boolean reverse)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack.isEmpty())
        {
            return;
        }

        int numEntities = this.getStoredEntityCount(manipulatorStack);
        int current = this.getCurrentIndex(manipulatorStack, false);

        if (reverse)
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
        this.setSelectedModuleStack(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);

        int cardIndex = this.getSelectedCardIndex(manipulatorStack, false);
        NBTUtils.setByte(manipulatorStack, TAG_NAME_CACHE, "Current_" + cardIndex, (byte) current);
    }

    private boolean canStoreEntity(Entity entity)
    {
        if (Configs.lmmListIsWhitelist)
        {
            return Sets.newHashSet(Configs.lmmWhitelist).contains(EntityList.getEntityString(entity));
        }
        else
        {
            return Sets.newHashSet(Configs.lmmBlacklist).contains(EntityList.getEntityString(entity)) == false;
        }
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        return nbt != null ? NBTUtils.getCompoundExcludingTags(nbt, false, "Items") : null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String str = super.getItemStackDisplayName(stack);
        String preGreen = TextFormatting.GREEN.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        if (str.length() >= 14)
        {
            str = EUStringUtils.getInitialsWithDots(str);
        }

        str = str + " - MC: " + preGreen + (this.getSelectedCardIndex(stack, true) + 1) + rst;
        int index = this.getCurrentIndex(stack, true);
        int count = this.getStoredEntityCount(stack);
        str = str + " E: " + preGreen + (index + 1) + "/" + count + rst;
        String entityName = this.getEntityName(stack, index, true);

        if (entityName != null)
        {
            str = str + " -> " + entityName;
        }

        return str;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        if (stack.getTagCompound() == null)
        {
            list.add(I18n.format("enderutilities.tooltip.item.usetoolworkstation"));
            return;
        }

        String preDGreen = TextFormatting.DARK_GREEN.toString();
        String preBlue = TextFormatting.BLUE.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preDGreen + Mode.getMode(stack).getDisplayName() + rst);

        if (verbose)
        {
            NBTTagCompound tag = NBTUtils.getCompoundTag(stack, TAG_NAME_CACHE, false);

            if (tag != null)
            {
                if (tag.getBoolean("Jailer"))
                {
                    list.add(I18n.format("enderutilities.tooltip.item.jailer") + ": " +
                            TextFormatting.GREEN + I18n.format("enderutilities.tooltip.item.yes") + rst);
                }
                else
                {
                    list.add(I18n.format("enderutilities.tooltip.item.jailer") + ": " +
                            TextFormatting.RED + I18n.format("enderutilities.tooltip.item.no") + rst);
                }

                int num = tag.getByte("NumCards");

                if (num > 0)
                {
                    int max = this.getMaxModules(stack, ModuleType.TYPE_MEMORY_CARD_MISC);
                    int sel = this.getSelectedCardIndex(stack, false);
                    sel = Math.max(MathHelper.clamp(sel, 0, num - 1), 0);

                    list.add(I18n.format("enderutilities.tooltip.item.selectedmemorycard.short") +
                             String.format(" (%s%d%s / %s%d%s)", preBlue, sel + 1, rst, preBlue, num, rst));

                    NBTTagList tagList = null;

                    // Get the selected-th valid cached list, same way the modules are retrieved
                    for (int i = 0, valid = -1; i < max && valid < sel; i++)
                    {
                        tagList = NBTUtils.getTagList(stack, TAG_NAME_CACHE, "Entities_" + i, Constants.NBT.TAG_STRING, false);

                        if (tagList != null)
                        {
                            valid++;
                        }
                    }

                    if (tagList == null || tagList.hasNoTags())
                    {
                        return;
                    }

                    int current = this.getCurrentIndex(stack, true);
                    int count = tagList.tagCount();

                    for (int i = 0; i < count; i++)
                    {
                        String name = tagList.getStringTagAt(i);

                        if (name != null)
                        {
                            if (i == current)
                            {
                                list.add("=> " + name);
                            }
                            else
                            {
                                list.add("   " + name);
                            }
                        }
                    }

                    return;
                }
            }

            list.add(I18n.format("enderutilities.tooltip.item.nomemorycards"));
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Ctrl + (Shift + ) Toggle mode: Change selected Memory Card
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_CTRL, HotKeys.MOD_SHIFT) ||
            EnumKey.SCROLL.matches(key, HotKeys.MOD_CTRL))
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_MEMORY_CARD_MISC,
                    EnumKey.keypressActionIsReversed(key) || EnumKey.keypressContainsShift(key));
        }
        // Shift + Toggle Mode: Change entity selection within the current module
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT) ||
                 EnumKey.SCROLL.matches(key, HotKeys.MOD_SHIFT))
        {
            this.changeEntitySelection(stack, EnumKey.keypressActionIsReversed(key));
        }
        // Just Toggle key, cycle the mode
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_NONE))
        {
            NBTUtils.cycleByteValue(stack, WRAPPER_TAG_NAME, TAG_NAME_MODE, Mode.values().length - 1);
        }
    }

    private int getSelectedCardIndex(ItemStack manipulatorStack, boolean clamped)
    {
        if (clamped)
        {
            return UtilItemModular.getClampedModuleSelection(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        }
        else
        {
            return UtilItemModular.getStoredModuleSelection(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        }
    }

    @Override
    public void onModulesChanged(ItemStack manipulatorStack)
    {
        int firstSlot = UtilItemModular.getFirstIndexOfModuleType(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        int numModules = this.getMaxModules(manipulatorStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        int numInstalled = 0;

        for (int i = 0; i < numModules; i++)
        {
            int selected = 0;
            int slotNum = i + firstSlot;
            ItemStack moduleStack = UtilItemModular.getModuleStackBySlotNumber(manipulatorStack, slotNum, ModuleType.TYPE_MEMORY_CARD_MISC);
            NBTTagList listEntities = null;

            if (moduleStack.isEmpty() == false)
            {
                selected = NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current");
                listEntities = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);
                this.updateCachedEntityInfo(manipulatorStack, listEntities, numInstalled, selected);
                numInstalled++;
            }
        }

        NBTUtils.setByte(manipulatorStack, TAG_NAME_CACHE, "NumCards", (byte) numInstalled);
        NBTUtils.setBoolean(manipulatorStack, TAG_NAME_CACHE, "Jailer",
                this.getInstalledModuleCount(manipulatorStack, ModuleType.TYPE_MOBPERSISTENCE) > 0);
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 5;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_MEMORY_CARD_MISC))
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
        if (moduleStack.isEmpty() || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        return this.getMaxModules(containerStack, ((IModule) moduleStack.getItem()).getModuleType(moduleStack));
    }

    public enum Mode
    {
        NORMAL ("enderutilities.tooltip.item.normal", "normal"),
        CAPTURE ("enderutilities.tooltip.item.capture", "capture"),
        RELEASE ("enderutilities.tooltip.item.release", "release");

        private String unlocName;
        private String variant;

        Mode (String unlocName, String variant)
        {
            this.unlocName = unlocName;
            this.variant = variant;
        }

        @SideOnly(Side.CLIENT)
        public String getDisplayName()
        {
            return I18n.format(this.unlocName);
        }

        public String getVariant()
        {
            return this.variant;
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
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "mode=normal"),
                new ModelResourceLocation(rl, "mode=capture"),
                new ModelResourceLocation(rl, "mode=release")
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;
        return new ModelResourceLocation(rl, "mode=" + Mode.getMode(stack).getVariant());
    }
}
