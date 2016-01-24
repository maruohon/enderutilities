package fi.dy.masa.enderutilities.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import fi.dy.masa.enderutilities.gui.client.GuiHandyBag;
import fi.dy.masa.enderutilities.gui.client.GuiInventorySwapper;
import fi.dy.masa.enderutilities.gui.client.GuiPickupManager;
import fi.dy.masa.enderutilities.inventory.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.InventoryItemModular;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;

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
                TileEntity te = world.getTileEntity(x, y, z);
                if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
                {
                    return ((TileEntityEnderUtilitiesInventory)te).getContainer(player);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                stack = ItemHandyBag.getOpenableBag(player);
                if (stack != null)
                {
                    return new ContainerHandyBag(player, new InventoryItemModular(stack, player, ModuleType.TYPE_MEMORY_CARD));
                }
                break;

            case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                stack = player.getCurrentEquippedItem();
                if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
                {
                    return new ContainerInventorySwapper(player, new InventoryItemModular(stack, player, ModuleType.TYPE_MEMORY_CARD));
                }
                break;

            case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                stack = player.getCurrentEquippedItem();
                if (stack != null && stack.getItem() == EnderUtilitiesItems.pickupManager)
                {
                    return new ContainerPickupManager(player, stack);
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
                TileEntity te = world.getTileEntity(x, y, z);
                if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
                {
                    return ((TileEntityEnderUtilitiesInventory)te).getGui(player);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                ItemStack stack = ItemHandyBag.getOpenableBag(player);
                if (stack != null)
                {
                    return new GuiHandyBag(new ContainerHandyBag(player, new InventoryItemModular(stack, player, ModuleType.TYPE_MEMORY_CARD)));
                }
                break;

            case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                stack = player.getCurrentEquippedItem();
                if (stack != null && stack.getItem() == EnderUtilitiesItems.inventorySwapper)
                {
                    return new GuiInventorySwapper(new ContainerInventorySwapper(player, new InventoryItemModular(stack, player, ModuleType.TYPE_MEMORY_CARD)));
                }
                break;

            case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                stack = player.getCurrentEquippedItem();
                if (stack != null && stack.getItem() == EnderUtilitiesItems.pickupManager)
                {
                    return new GuiPickupManager(new ContainerPickupManager(player, stack));
                }
                break;

            default:
        }

        return null;
    }

}
