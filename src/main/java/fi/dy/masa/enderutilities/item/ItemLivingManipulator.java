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
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
    public static final int MAX_ENTITIES_PER_CARD = 16;
    public static final String WRAPPER_TAG_NAME = "LivingManipulator";
    public static final String TAG_NAME_MODE = "Mode";
    public static final String TAG_NAME_POS = "Position";

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
            return this.releaseEntity(stack, player.getEntityWorld(), livingBase.getPosition(), livingBase.posX, livingBase.posY, livingBase.posZ, EnumFacing.UP);
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

        player.sendMessage(new TextComponentTranslation("enderutilities.chat.message.memorycard.full"));

        return EnumActionResult.FAIL;
    }

    private EnumActionResult releaseEntity(ItemStack containerStack, World world, BlockPos pos, double x, double y, double z, EnumFacing side)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        if (moduleStack == null)
        {
            return EnumActionResult.PASS;
        }

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);
        if (tagList == null || tagList.tagCount() == 0)
        {
            return EnumActionResult.PASS;
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
            boolean isShulker = false;

            if (tag.getString("id").equals("Shulker"))
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

            if (entity instanceof EntityLiving && this.getInstalledModuleCount(containerStack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                EntityUtils.applyMobPersistence((EntityLiving)entity);
            }

            world.spawnEntity(entity);
            tagList.removeTag(current);
        }

        numEntities = tagList.tagCount();
        if (current >= numEntities)
        {
            current = (numEntities > 0) ? numEntities - 1 : 0;
        }

        NBTUtils.setByte(moduleStack, WRAPPER_TAG_NAME, "Current", (byte)current);
        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);

        return EnumActionResult.SUCCESS;
    }

    private EnumActionResult storeEntity(ItemStack containerStack, EntityLivingBase livingBase)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (this.canStoreEntity(livingBase) == false || moduleStack == null)
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

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, true);

        tagList = NBTUtils.insertToTagList(tagList, nbtEntity, NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current"));
        NBTUtils.setTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", tagList);

        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);

        livingBase.isDead = true;

        return EnumActionResult.SUCCESS;
    }

    private int getStoredEntityCount(ItemStack containerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
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
    private int getCurrentIndex(ItemStack containerStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        if (moduleStack == null)
        {
            return 0;
        }

        return NBTUtils.getByte(moduleStack, WRAPPER_TAG_NAME, "Current");
    }

    /*
    public void setCurrentIndex(ItemStack containerStack, byte index)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        if (moduleStack == null)
        {
            return;
        }

        NBTUtils.setByte(moduleStack, WRAPPER_TAG_NAME, "Current", index);
        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);
    }
    */

    @Nullable
    private String getEntityName(ItemStack containerStack, int index, boolean useDisplayNameFormatting)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);

        if (moduleStack == null)
        {
            return null;
        }

        NBTTagList tagList = NBTUtils.getTagList(moduleStack, WRAPPER_TAG_NAME, "Entities", Constants.NBT.TAG_COMPOUND, false);

        if (tagList != null && tagList.tagCount() > index)
        {
            return this.getNameForEntityFromTag(tagList.getCompoundTagAt(index), useDisplayNameFormatting);
        }

        return null;
    }

    @Nullable
    private String getNameForEntityFromTag(NBTTagCompound tag, boolean useDisplayNameFormatting)
    {
        if (tag != null)
        {
            String pre = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
            String rst = TextFormatting.RESET.toString() + (useDisplayNameFormatting ? TextFormatting.WHITE.toString() : TextFormatting.GRAY.toString());

            String name = tag.getString("CustomName");

            if (tag.hasKey("id", Constants.NBT.TAG_STRING))
            {
                String id = tag.getString("id");
                String translated = I18n.format("entity." + id + ".name");

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

    private void changeEntitySelection(ItemStack containerStack, boolean reverse)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC);
        if (moduleStack == null)
        {
            return;
        }

        int numEntities = this.getStoredEntityCount(containerStack);
        int current = this.getCurrentIndex(containerStack);

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
        this.setSelectedModuleStack(containerStack, ModuleType.TYPE_MEMORY_CARD_MISC, moduleStack);
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
    public String getItemStackDisplayName(ItemStack stack)
    {
        String str = super.getItemStackDisplayName(stack);
        String preGreen = TextFormatting.GREEN.toString();
        String preGreenIta = preGreen + TextFormatting.ITALIC.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();

        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);
        if (moduleStack != null)
        {
            if (str.length() >= 14)
            {
                str = EUStringUtils.getInitialsWithDots(str);
            }

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName())
            {
                str = str + " " + preGreenIta + moduleStack.getDisplayName() + rst;
            }
            else
            {
                str = str + " MC: " + preGreen + (UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD_MISC) + 1) + rst;
            }

            int index = this.getCurrentIndex(stack);
            int count = this.getStoredEntityCount(stack);
            str = str + " E: " + preGreen + (index + 1) + "/" + count + rst;
            String entityName = this.getEntityName(stack, index, true);

            if (entityName != null)
            {
                str = str + " -> " + entityName;
            }
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

        ItemStack memoryCardStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_MEMORY_CARD_MISC);

        String preDGreen = TextFormatting.DARK_GREEN.toString();
        String preBlue = TextFormatting.BLUE.toString();
        String preWhiteIta = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();

        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preDGreen + Mode.getMode(stack).getDisplayName() + rst);

        if (verbose)
        {
            // Item supports Jailer modules, show if one is installed
            if (this.getMaxModules(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
            {
                String s;
                if (this.getInstalledModuleCount(stack, ModuleType.TYPE_MOBPERSISTENCE) > 0)
                {
                    s = I18n.format("enderutilities.tooltip.item.jailer") + ": " +
                            TextFormatting.GREEN + I18n.format("enderutilities.tooltip.item.yes") + rst;
                }
                else
                {
                    s = I18n.format("enderutilities.tooltip.item.jailer") + ": " +
                            TextFormatting.RED + I18n.format("enderutilities.tooltip.item.no") + rst;
                }

                list.add(s);
            }
        }

        // Memory Cards installed
        if (memoryCardStack != null)
        {
            if (verbose)
            {
                int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_MEMORY_CARD_MISC);
                int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_MEMORY_CARD_MISC) + 1;
                String dName = (memoryCardStack.hasDisplayName() ? preWhiteIta + memoryCardStack.getDisplayName() + rst + " " : "");
                list.add(I18n.format("enderutilities.tooltip.item.selectedmemorycard.short") +
                         String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));

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
                        String name = this.getNameForEntityFromTag(tag, false);
                        name = (i == current) ? "=> " + name : "   " + name;
                        list.add(name);
                    }
                }
            }
        }
        else
        {
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
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
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
