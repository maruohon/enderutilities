package fi.dy.masa.enderutilities.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ILocationBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.setup.Registry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderBag extends ItemLocationBoundModular implements IChunkLoadingItem, IKeyBound
{
    public static final int ENDER_CHARGE_COST = 200;

    @SideOnly(Side.CLIENT)
    private IIcon iconArray[];

    public ItemEnderBag()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_BAG);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (stack == null || stack.getTagCompound() == null || world.isRemote == true)
        {
            return stack;
        }

        NBTTagCompound bagNbt = stack.getTagCompound();
        NBTHelperTarget targetData = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (targetData == null || targetData.blockName == null)
        {
            return stack;
        }

        // Access is allowed for everyone to a vanilla Ender Chest
        if (targetData.blockName.equals("minecraft:ender_chest") == true)
        {
            if (UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST, true) == false)
            {
                return stack;
            }

            bagNbt.setBoolean("IsOpen", true);
            player.displayGUIChest(player.getInventoryEnderChest());
            return stack;
        }

        // For other targets, access is only allowed if the mode is set to public, or if the player is the owner
        if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return stack;
        }

        // Target block is not whitelisted, so it is known to not work unless within the client's loaded range
        // FIXME: How should we properly check if the player is within range?
        if (this.isTargetBlockWhitelisted(targetData.blockName, targetData.blockMeta) == false &&
            (targetData.dimension != player.dimension || player.getDistanceSq(targetData.posX, targetData.posY, targetData.posZ) >= 10000.0d))
        {
            player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("enderutilities.chat.message.enderbag.outofrange")));
            return stack;
        }

        // The target block has changed since binding the bag, remove the bind (not for vanilla Ender Chests)
        if (targetData.isTargetBlockUnchanged() == false)
        {
            NBTHelperTarget.removeTargetTagFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
            bagNbt.removeTag("ChunkLoadingRequired");
            bagNbt.removeTag("IsOpen");

            player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.enderbag.blockchanged"));

            return stack;
        }

        // Check that we have sufficient charge left to use the bag.
        if (UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST, false) == false)
        {
            return stack;
        }

        // Only open the GUI if the chunk loading succeeds. 60 second unload delay.
        if (ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 60) == true)
        {
            MinecraftServer server = MinecraftServer.getServer();
            if (server == null)
            {
                return stack;
            }

            World targetWorld = server.worldServerForDimension(targetData.dimension);
            if (targetWorld == null)
            {
                return stack;
            }

            // Actually use the charge. This _shouldn't_ be able to fail due to the above simulation...
            if (UtilItemModular.useEnderCharge(stack, player, ENDER_CHARGE_COST, true) == false)
            {
                // Remove the chunk loading delay FIXME this doesn't take into account possible overlapping chunk loads...
                //ChunkLoading.getInstance().refreshChunkTimeout(targetData.dimension, targetData.posX >> 4, targetData.posZ >> 4, 0, false);
                return stack;
            }

            bagNbt.setBoolean("ChunkLoadingRequired", true);
            bagNbt.setBoolean("IsOpen", true);

            float hx = (float)targetData.dPosX - targetData.posX;
            float hy = (float)targetData.dPosY - targetData.posY;
            float hz = (float)targetData.dPosZ - targetData.posZ;

            Block block = world.getBlock(targetData.posX, targetData.posY, targetData.posZ);
            // Access is allowed in onPlayerOpenContainer(PlayerOpenContainerEvent event) in PlayerEventHandler
            block.onBlockActivated(targetWorld, targetData.posX, targetData.posY, targetData.posZ, player, targetData.blockFace, hx, hy, hz);
        }

        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking() == false || world.isRemote == true)
        {
            return world.isRemote; // hah, saved an extra if() by returning this :p~
        }

        // If the player trying to set/modify the bag is not the owner of the selected Link Crystal and it is not set to be public
        if (NBTHelperPlayer.canAccessSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player) == false)
        {
            return false;
        }

        TileEntity te = world.getTileEntity(x, y, z);
        if (te != null && (te instanceof IInventory || te.getClass() == TileEntityEnderChest.class))
        {
            /*if (this.isTargetBlockWhitelisted(Block.blockRegistry.getNameForObject(block), meta) == false)
            {
                player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("enderutilities.chat.message.enderbag.blocknotwhitelisted")
                        + " '" + Block.blockRegistry.getNameForObject(block) + ":" + meta + "'"));
                return true;
            }*/

            if (NBTHelperPlayer.selectedModuleHasPlayerTag(stack, ModuleType.TYPE_LINKCRYSTAL) == false)
            {
                NBTHelperPlayer.writePlayerTagToSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, player, true);
            }

            NBTHelperTarget.writeTargetTagToSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, x, y, z, world.provider.dimensionId, side, hitX, hitY, hitZ, false);
        }

        return true;
    }

    /* Returns the maximum number of the given module that can be installed on this item.
     * This is for exact module checking, instead of the general module type. */
    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        ModuleType moduleType = ((IModule) moduleStack.getItem()).getModuleType(moduleStack);

        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            // Only allow the inventory/block type Link Crystals
            if (((IModule) moduleStack.getItem()).getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
            {
                return 3;
            }
        }

        return 0;
    }

    private boolean isTargetBlockWhitelisted(String name, int meta)
    {
        List<String> list;

        // FIXME add metadata handling
        // Black list
        if (Configs.enderBagListType.getString().equalsIgnoreCase("blacklist") == true)
        {
            list = Registry.getEnderbagBlacklist();
            if (list.contains(name) == true)
            {
                return false;
            }

            return true;
        }
        // White list
        else
        {
            list = Registry.getEnderbagWhitelist();
            if (list.contains(name) == true)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (target != null)
        {
            ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);

            if (targetStack != null && targetStack.getItem() != null)
            {
                String pre = EnumChatFormatting.GREEN.toString();
                String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();
                return StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim() + " " + pre + targetStack.getDisplayName() + rst;
            }
        }

        return StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (linkCrystalStack != null && linkCrystalStack.getItem() instanceof ILocationBound)
        {
            String textPre = EnumChatFormatting.DARK_GREEN.toString();
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();

            NBTHelperTarget target = ((ILocationBound)linkCrystalStack.getItem()).getTarget(linkCrystalStack);
            if (target != null)
            {
                NBTHelperPlayer playerData = NBTHelperPlayer.getPlayerDataFromItem(linkCrystalStack);
                ItemStack targetStack = new ItemStack(Block.getBlockFromName(target.blockName), 1, target.blockMeta & 0xF);
                String targetName = (targetStack != null && targetStack.getItem() != null ? targetStack.getDisplayName() : "");

                if ("minecraft:ender_chest".equals(target.blockName))
                {
                    list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.target") + ": " + textPre + targetName + rst);
                }
                else if (playerData != null && playerData.canAccess(player) == true)
                {
                    list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.target") + ": " + textPre + targetName + rst);

                    super.addInformationSelective(stack, player, list, advancedTooltips, verbose);

                    String mode = StatCollector.translateToLocal((playerData.isPublic == true ? "enderutilities.tooltip.item.public" : "enderutilities.tooltip.item.private"));
                    list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + mode);
                    list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.owner") + ": " + playerData.getPlayerName());
                }
            }
            else
            {
                super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
            }
        }
        else
        {
            super.addInformationSelective(stack, player, list, advancedTooltips, verbose);
        }
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
    {
        return false;
    }

    public void toggleBagMode(EntityPlayer player, ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack == null)
        {
            return;
        }

        NBTTagCompound moduleNbt = moduleStack.getTagCompound();
        if (moduleNbt == null)
        {
            return;
        }

        NBTHelperPlayer playerData = NBTHelperPlayer.getPlayerDataFromNBT(moduleNbt);
        if (playerData == null || playerData.isOwner(player) == false)
        {
            return;
        }

        playerData.isPublic = ! playerData.isPublic;
        playerData.writeToNBT(moduleNbt);
        this.setSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL, moduleStack);
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

        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.toggleBagMode(player, stack);
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
        return 2;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".regular.closed");
        this.iconArray = new IIcon[6];

        this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".regular.closed");
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".regular.open");
        this.iconArray[2] = iconRegister.registerIcon(this.getIconString() + ".enderchest.closed");
        this.iconArray[3] = iconRegister.registerIcon(this.getIconString() + ".enderchest.open");
        this.iconArray[4] = iconRegister.registerIcon(this.getIconString() + ".locked.closed");
        this.iconArray[5] = iconRegister.registerIcon(this.getIconString() + ".locked.open");
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
        NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);

        if (target != null)
        {
            int isOpen = 0;

            // Bag currently open
            if (stack.getTagCompound().getBoolean("IsOpen") == true)
            {
                isOpen = 1;
                index += 1;
            }

            // Currently linked to a vanilla Ender Chest
            if ("minecraft:ender_chest".equals(target.blockName))
            {
                index += 2;
            }

            // The is-locked layer
            if (renderPass == 1)
            {
                NBTHelperPlayer playerData = NBTHelperPlayer.getPlayerDataFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
                if (playerData != null && playerData.isPublic == false)
                {
                    index = 4 + isOpen;
                }
            }
        }

        // NOTE: We don't have an empty texture for the lock overlay, so we use the same bag texture in case it is not locked
        return this.iconArray[(index < this.iconArray.length ? index : 0)];
    }
}
