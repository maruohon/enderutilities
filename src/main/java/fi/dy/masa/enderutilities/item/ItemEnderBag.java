package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EUConfigs;
import fi.dy.masa.enderutilities.setup.EURegistry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBag extends ItemLocationBoundModular implements IChunkLoadingItem, IKeyBound
{
    public static final byte BIND_TYPE_REGULAR = 0;
    public static final byte BIND_TYPE_ENDER = 1;
    public static final byte MODE_PRIVATE = 0;
    public static final byte MODE_PUBLIC = 1;

    @SideOnly(Side.CLIENT)
    private IIcon iconArray[];

    public ItemEnderBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setUnlocalizedName(ReferenceBlocksItems.NAME_ITEM_ENDER_BAG);
        this.setTextureName(ReferenceTextures.getTextureName(this.getUnlocalizedName()));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (world.isRemote == true)
        {
            return stack;
        }

        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null)
        {
            return stack;
        }
        NBTTagCompound bagNbt = stack.getTagCompound(); // Can't be null if moduleStack isn't null at this point
        NBTTagCompound moduleNbt = moduleStack.getTagCompound();
        if (moduleNbt == null)
        {
            return stack;
        }

        // Ender Chest
        if (moduleNbt.hasKey("Type", Constants.NBT.TAG_BYTE) == true && moduleNbt.getByte("Type") == BIND_TYPE_ENDER)
        {
            bagNbt.setBoolean("IsOpen", true);
            player.displayGUIChest(player.getInventoryEnderChest());
            return stack;
        }

        NBTHelperPlayer playerData = new NBTHelperPlayer();
        // If the bag is not set to public and the player trying to access the bag is not the owner
        if (moduleNbt.getByte("Mode") != MODE_PUBLIC && playerData.readPlayerTagFromNBT(moduleNbt) != null && playerData.isOwner(player) == false)
        {
            return stack;
        }

        NBTHelperTarget targetData = new NBTHelperTarget();
        // Instance of IInventory (= not Ender Chest); Get the target information
        if (targetData.readTargetTagFromNBT(moduleNbt) == null)
        {
            return stack;
        }

        // Target block is not whitelisted, so it is known to not work unless within the client's loaded region
        // FIXME: How should we properly check if the player is within range?
        if (this.isTargetBlockWhitelisted(moduleNbt.getString("BlockName"), moduleNbt.getByte("BlockMeta")) == false &&
            (targetData.dimension != player.dimension || player.getDistanceSq(targetData.posX, targetData.posY, targetData.posZ) >= 10000.0d))
        {
            player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.message.enderbag.outofrange")));
            return stack;
        }

        // Only open the GUI if the chunk loading succeeds. 60 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 60 * 20) == true)
        {
            World tgtWorld = MinecraftServer.getServer().worldServerForDimension(targetData.dimension);
            if (tgtWorld == null)
            {
                return stack;
            }
            Block block = tgtWorld.getBlock(targetData.posX, targetData.posY, targetData.posZ);
            if (block == null)
            {
                return stack;
            }

            // The target block has changed since binding the bag, remove the bind (not for Ender Chests)
            if (Block.blockRegistry.getNameForObject(block).equals(moduleNbt.getString("BlockName")) == false
                || moduleNbt.getByte("BlockMeta") != tgtWorld.getBlockMetadata(targetData.posX, targetData.posY, targetData.posZ))
            {
                moduleNbt.removeTag("BlockName");
                moduleNbt.removeTag("BlockMeta");
                moduleNbt.removeTag("Slots");
                moduleNbt = NBTHelperTarget.removeTargetTagFromNBT(moduleNbt);
                //moduleStack.setTagCompound(moduleNbt);
                bagNbt.removeTag("ChunkLoadingRequired");
                bagNbt.setBoolean("IsOpen", false);
                //stack.setTagCompound(bagNbt);
                player.addChatMessage(new ChatComponentTranslation("chat.message.enderbag.blockchanged"));
                return stack;
            }

            bagNbt.setBoolean("ChunkLoadingRequired", true);
            bagNbt.setBoolean("IsOpen", true);
            //stack.setTagCompound(bagNbt);

            // Access is allowed in onPlayerOpenContainer(PlayerOpenContainerEvent event) in PlayerEventHandler
            block.onBlockActivated(tgtWorld, targetData.posX, targetData.posY, targetData.posZ, player, targetData.blockFace, 0.5f, 0.5f, 0.5f);
        }

        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() == false)
        {
            return false;
        }

        if (world.isRemote == true)
        {
            return true;
        }

        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null)
        {
            return false;
        }

        NBTTagCompound moduleNbt = moduleStack.getTagCompound();
        if (moduleNbt == null)
        {
            moduleNbt = new NBTTagCompound();
            moduleNbt.setByte("Mode", MODE_PRIVATE);
            moduleStack.setTagCompound(moduleNbt);
            this.setSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, moduleStack);
        }

        NBTHelperPlayer playerData = new NBTHelperPlayer();
        if (playerData.readPlayerTagFromNBT(moduleNbt) == null)
        {
            moduleNbt = NBTHelperPlayer.writePlayerTagToNBT(moduleNbt, player);
            playerData.readPlayerTagFromNBT(moduleNbt);
            this.setSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, moduleStack);
        }

        // If the player trying to set/modify the bag is not the owner
        if (playerData.isOwner(player) == false)
        {
            return true;
        }

        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        if (block == null || block.isAir(world, x, y, z) == true || te == null)
        {
            return true;
        }

        if (te instanceof IInventory || te instanceof TileEntityEnderChest)
        {
            /*if (this.isTargetBlockWhitelisted(Block.blockRegistry.getNameForObject(block), meta) == false)
            {
                player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("chat.message.enderbag.blocknotwhitelisted")
                        + " '" + Block.blockRegistry.getNameForObject(block) + ":" + meta + "'"));
                return true;
            }*/

            moduleNbt.setString("BlockName", Block.blockRegistry.getNameForObject(block));
            moduleNbt.setByte("BlockMeta", (byte)meta);
            moduleNbt = NBTHelperTarget.writeTargetTagToNBT(moduleNbt, x, y, z, player.dimension, side, hitX, hitY, hitZ, false);

            if (te instanceof IInventory)
            {
                moduleNbt.setInteger("Slots", ((IInventory)te).getSizeInventory());
                moduleNbt.setByte("Type", BIND_TYPE_REGULAR);
            }
            else
            {
                moduleNbt.setInteger("Slots", player.getInventoryEnderChest().getSizeInventory());
                moduleNbt.setByte("Type", BIND_TYPE_ENDER);
            }

            this.setSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL, moduleStack);
        }

        return true;
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

    private boolean isTargetBlockWhitelisted(String name, int meta)
    {
        List<String> list;

        // FIXME add the metadata handling
        // Black list
        if (EUConfigs.enderBagListType.getString().equalsIgnoreCase("blacklist") == true)
        {
            list = EURegistry.getEnderbagBlacklist();
            if (list.contains(name) == true)
            {
                return false;
            }
            return true;
        }
        // White list
        else
        {
            list = EURegistry.getEnderbagWhitelist();
            if (list.contains(name) == true)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack toolStack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(toolStack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null)
        {
            NBTTagCompound moduleNbt = moduleStack.getTagCompound();
            if (moduleNbt != null && moduleNbt.getByte("Type") == BIND_TYPE_ENDER)
            {
                String ender = StatCollector.translateToLocal(new ItemStack(Blocks.ender_chest, 1, 0).getUnlocalizedName() + ".name");
                return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(toolStack) + ".name")).trim() + " (" + ender + ")";
            }
        }

        return super.getItemStackDisplayName(toolStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null || moduleStack.getItem() == null)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.nolinkcrystals"));
            return;
        }

        String dimPre = "" + EnumChatFormatting.DARK_GREEN;
        String numPre = "" + EnumChatFormatting.BLUE;
        String rst = "" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY;

        NBTTagCompound moduleNbt = moduleStack.getTagCompound();
        NBTHelperTarget target = new NBTHelperTarget();
        if (target.readTargetTagFromNBT(moduleNbt) == null)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.notargetset"));

            int num = UtilItemModular.getModuleCount(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
            if (num >= 1)
            {
                int sel = UtilItemModular.getClampedModuleSelection(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL) + 1;
                list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal") + String.format(" %s%d / %d%s", numPre, sel, num, rst));
                //list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal") + String.format(" %d / %d", sel, max));
            }

            return;
        }

        NBTHelperPlayer playerData = new NBTHelperPlayer();
        String locName = new ItemStack(Block.getBlockFromName(moduleNbt.getString("BlockName")), 1, moduleNbt.getByte("BlockMeta") & 0xF).getDisplayName();

        if ((playerData.readPlayerTagFromNBT(moduleNbt) != null && playerData.isOwner(player) == true)
            || moduleNbt.getByte("Type") == BIND_TYPE_ENDER)
        {
            list.add(StatCollector.translateToLocal("gui.tooltip.type") + ": " + numPre + locName + rst);
        }

        // Only show private vs. public when bound to regular inventories, not Ender Chest
        if (moduleNbt.getByte("Type") == BIND_TYPE_REGULAR)
        {
            if (EnderUtilities.proxy.isShiftKeyDown() == false)
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.holdshift"));
                return;
            }

            // Only show the location info if the bag is not bound to an ender chest, and if the player is the owner
            if (playerData != null && playerData.isOwner(player) == true)
            {
                list.add(StatCollector.translateToLocal("gui.tooltip.dimension") + ": " + numPre + target.dimension + rst
                        + " " + dimPre + TooltipHelper.getDimensionName(target.dimension, target.dimensionName, false) + rst);
                list.add(String.format("x: %s%d%s y: %s%d%s z: %s%d%s", numPre, target.posX, rst, numPre, target.posY, rst, numPre, target.posZ, rst));
            }

            String mode = StatCollector.translateToLocal((moduleNbt.getByte("Mode") == MODE_PUBLIC ? "gui.tooltip.public" : "gui.tooltip.private"));
            list.add(StatCollector.translateToLocal("gui.tooltip.mode") + ": " + mode);
            list.add(StatCollector.translateToLocal("gui.tooltip.owner") + ": " + playerData.playerName);
        }

        int num = UtilItemModular.getModuleCount(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
        if (num >= 1)
        {
            int sel = UtilItemModular.getClampedModuleSelection(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL) + 1;
            list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal") + String.format(" %s%d / %d%s", numPre, sel, num, rst));
            //list.add(StatCollector.translateToLocal("gui.tooltip.selectedlinkcrystal") + String.format(" %d / %d", sel, max));
        }
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
    {
        return false;
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Change the selected link crystal
        if (ReferenceKeys.keypressContainsShift(key) == true)
        {
            super.doKeyBindingAction(player, stack, key);
            return;
        }

        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE && player != null)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
            if (moduleStack == null)
            {
                return;
            }

            byte val = 0;
            NBTTagCompound moduleNbt = moduleStack.getTagCompound();
            if (moduleNbt != null)
            {
                NBTHelperPlayer playerData = new NBTHelperPlayer();
                if (playerData.readPlayerTagFromNBT(moduleNbt) != null && playerData.isOwner(player) == false)
                {
                    return;
                }
                val = moduleNbt.getByte("Mode");
            }
            else
            {
                moduleNbt = new NBTTagCompound();
            }
            if (++val > MODE_PUBLIC)
            {
                val = MODE_PRIVATE;
            }

            moduleNbt.setByte("Mode", val);
            moduleStack.setTagCompound(moduleNbt);
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean isCurrent)
    {
        // Ugly workaround to get the bag closing tag update to sync to the client
        // For some reason it won't sync if set directly in the PlayerOpenContainerEvent
        if (stack != null && stack.getTagCompound() != null)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt.hasKey("IsOpenDummy") == true)
            {
                nbt.removeTag("IsOpenDummy");
                nbt.setBoolean("IsOpen", false);
            }
        }
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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".regular.closed");
        this.iconArray = new IIcon[4];

        this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".regular.closed");
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".regular.open");
        this.iconArray[2] = iconRegister.registerIcon(this.getIconString() + ".enderchest.closed");
        this.iconArray[3] = iconRegister.registerIcon(this.getIconString() + ".enderchest.open");
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
        return this.getIcon(stack, renderPass, null, null, 0);
    }

    /**
     * Player, Render pass, and item usage sensitive version of getIconIndex.
     *
     * @param stack The item stack to get the icon for. (Usually this, and usingItem will be the same if usingItem is not null)
     * @param renderPass The pass to get the icon for, 0 is default.
     * @param player The player holding the item
     * @param usingItem The item the player is actively using. Can be null if not using anything.
     * @param useRemaining The ticks remaining for the active item.
     * @return The icon index
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
        int index = 0;

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
        {
            ItemStack moduleStack = this.getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
            if (moduleStack != null && moduleStack.getTagCompound() != null)
            {
                // Linked to Ender Chest
                if (moduleStack.getTagCompound().getByte("Type") == BIND_TYPE_ENDER)
                {
                    index += 2;
                }
            }

            // Bag currently open
            if (nbt.hasKey("IsOpen") == true && nbt.getBoolean("IsOpen") == true)
            {
                index += 1;
            }
        }

        return this.iconArray[(index < this.iconArray.length ? index : 0)];
    }
}
