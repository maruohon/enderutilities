package fi.dy.masa.enderutilities.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import fi.dy.masa.enderutilities.gui.client.GuiHandyBag;
import fi.dy.masa.enderutilities.gui.client.GuiInventorySwapper;
import fi.dy.masa.enderutilities.gui.client.GuiNullifier;
import fi.dy.masa.enderutilities.gui.client.GuiPickupManager;
import fi.dy.masa.enderutilities.gui.client.GuiQuickStacker;
import fi.dy.masa.enderutilities.inventory.container.ContainerHandyBag;
import fi.dy.masa.enderutilities.inventory.container.ContainerInventorySwapper;
import fi.dy.masa.enderutilities.inventory.container.ContainerNullifier;
import fi.dy.masa.enderutilities.inventory.container.ContainerPickupManager;
import fi.dy.masa.enderutilities.inventory.container.ContainerQuickStacker;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemQuickStacker;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
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

                if (te != null && te instanceof TileEntityEnderUtilities)
                {
                    return ((TileEntityEnderUtilities) te).getContainer(player);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                stack = ItemHandyBag.getOpenableBag(player);
                if (stack.isEmpty() == false)
                {
                    return new ContainerHandyBag(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.HANDY_BAG);
                if (stack.isEmpty() == false)
                {
                    return new ContainerHandyBag(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.INVENTORY_SWAPPER);
                if (stack.isEmpty() == false)
                {
                    return new ContainerInventorySwapper(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.PICKUP_MANAGER);
                if (stack.isEmpty() == false)
                {
                    return new ContainerPickupManager(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_QUICK_STACKER:
                stack = ItemQuickStacker.getEnabledItem(player);
                if (stack.isEmpty() == false)
                {
                    return new ContainerQuickStacker(player, stack);
                }
                break;

            case ReferenceGuiIds.GUI_ID_NULLIFIER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.NULLIFIER);
                if (stack.isEmpty() == false)
                {
                    return new ContainerNullifier(player, stack);
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

                if (te != null && te instanceof TileEntityEnderUtilities)
                {
                    return ((TileEntityEnderUtilities) te).getGui(player);
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                ItemStack stack = ItemHandyBag.getOpenableBag(player);
                if (stack.isEmpty() == false)
                {
                    return new GuiHandyBag(new ContainerHandyBag(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_HANDY_BAG_RIGHT_CLICK:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.HANDY_BAG);
                if (stack.isEmpty() == false)
                {
                    return new GuiHandyBag(new ContainerHandyBag(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.INVENTORY_SWAPPER);
                if (stack.isEmpty() == false)
                {
                    return new GuiInventorySwapper(new ContainerInventorySwapper(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_PICKUP_MANAGER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.PICKUP_MANAGER);
                if (stack.isEmpty() == false)
                {
                    return new GuiPickupManager(new ContainerPickupManager(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_QUICK_STACKER:
                stack = ItemQuickStacker.getEnabledItem(player);
                if (stack.isEmpty() == false)
                {
                    return new GuiQuickStacker(new ContainerQuickStacker(player, stack));
                }
                break;

            case ReferenceGuiIds.GUI_ID_NULLIFIER:
                stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.NULLIFIER);
                if (stack.isEmpty() == false)
                {
                    return new GuiNullifier(new ContainerNullifier(player, stack));
                }
                break;

            default:
        }

        return null;
    }

}
