package fi.dy.masa.enderutilities.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.IGuiHandler;

import fi.dy.masa.enderutilities.gui.client.GuiHandyBag;
import fi.dy.masa.enderutilities.gui.client.GuiInventorySwapper;
import fi.dy.masa.enderutilities.gui.client.GuiPickupManager;
import fi.dy.masa.enderutilities.gui.client.GuiQuickStacker;
import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.ContainerQuickStacker;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemQuickStacker;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class EnderUtilitiesGUIHandler implements IGuiHandler
{

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (player == null || world == null)
        {
            return null;
        }

        ItemStack stack;

        switch (id)
        {
            case ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC:
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
                {
                    return ((TileEntityEnderUtilitiesInventory)te).getContainer(player);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                stack = ItemHandyBag.getOpenableBag(player);
                if (stack != null)
                {
                    return new ContainerHandyBag(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.handyBag);
                if (stack != null)
                {
                    return new ContainerHandyBag(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.inventorySwapper);
                if (stack != null)
                {
                    return new ContainerInventorySwapper(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.pickupManager);
                if (stack != null)
                {
                    return new ContainerPickupManager(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_QUICK_STACKER:
                stack = ItemQuickStacker.getEnabledItem(player);
                if (stack != null)
                {
                    return new ContainerQuickStacker(player, stack);
                }
                break;

            default:
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        if (player == null || world == null)
        {
            return null;
        }

        switch (id)
        {
            case ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC:
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
                {
                    return ((TileEntityEnderUtilitiesInventory)te).getGui(player);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                ItemStack stack = ItemHandyBag.getOpenableBag(player);
                if (stack != null)
                {
                    return new GuiHandyBag(new ContainerHandyBag(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.handyBag);
                if (stack != null)
                {
                    return new GuiHandyBag(new ContainerHandyBag(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.inventorySwapper);
                if (stack != null)
                {
                    return new GuiInventorySwapper(new ContainerInventorySwapper(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.pickupManager);
                if (stack != null)
                {
                    return new GuiPickupManager(new ContainerPickupManager(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_QUICK_STACKER:
                stack = ItemQuickStacker.getEnabledItem(player);
                if (stack != null)
                {
                    return new GuiQuickStacker(new ContainerQuickStacker(player, stack));
                }
                break;

            default:
        }

        return null;
    }

}
