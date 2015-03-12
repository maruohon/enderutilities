package fi.dy.masa.enderutilities.item;

import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.BlockEnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelBlock;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemEnderArrow extends ItemEnderUtilities
{
    public ItemEnderArrow()
    {
        super();
        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_ARROW);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getBaseModelName(String variant)
    {
        return ReferenceNames.NAME_ITEM_ENDERTOOL;
    }

    // FIXME debugging:
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            ModelBlock modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation("minecraft:block/cube"), EnderUtilitiesModelRegistry.models);
            EnderUtilities.logger.info("minecraft:block/cube:");
            EnderUtilitiesModelBlock.printModelBlock(modelBlock);

            modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation("minecraft:block/orientable"), EnderUtilitiesModelRegistry.models);
            EnderUtilities.logger.info("minecraft:block/orientable:");
            EnderUtilitiesModelBlock.printModelBlock(modelBlock);

            modelBlock = EnderUtilitiesModelBlock.readModel(new ResourceLocation("minecraft:block/furnace"), EnderUtilitiesModelRegistry.models);
            EnderUtilities.logger.info("minecraft:block/furnace:");
            EnderUtilitiesModelBlock.printModelBlock(modelBlock);



            //EnderUtilities.logger.info("modelBlockBaseItems:");
            //EnderUtilitiesModelBlock.printModelBlock(EnderUtilitiesModelRegistry.modelBlockBaseItems);

            String name = Reference.MOD_ID + ":" + "block/cube";
            EnderUtilities.logger.info(name + ":");
            EnderUtilitiesModelBlock.printModelBlock(EnderUtilitiesModelRegistry.models.get(new ResourceLocation(name)));

            EnderUtilities.logger.info("modelBlockBaseBlocks:");
            EnderUtilitiesModelBlock.printModelBlock(EnderUtilitiesModelRegistry.modelBlockBaseBlocks);

            name = Reference.MOD_ID + ":" + "block/enderinfuser";
            EnderUtilities.logger.info(name + ":");
            EnderUtilitiesModelBlock.printModelBlock(EnderUtilitiesModelRegistry.models.get(new ResourceLocation(name)));

            EnderUtilities.logger.info("registered models:");
            Iterator iter = EnderUtilitiesModelRegistry.models.entrySet().iterator();
            while (iter.hasNext())
            {
                Entry entry = (Entry)iter.next();
                String key = entry.getKey().toString();
                String val = entry.getValue().toString();
                if (key.contains("item") == false)
                {
                    EnderUtilities.logger.info("key: " + key + " val: " + val);
                }
            }

            if ((world.getBlockState(pos).getBlock() instanceof BlockEnderUtilities) == false)
            {
                return true;
            }

            ModelManager mm = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager();
            BlockModelShapes bms = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
            BlockStateMapper bsm = bms.getBlockStateMapper();

            EnderUtilities.logger.info("BMS.getModelForState(): " + bms.getModelForState(world.getBlockState(pos)));

            Iterator iterator = bsm.putAllStateModelLocations().entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry entry = (Entry)iterator.next();
                String s = "this.bakedModelStore.put(" + entry.getKey() + ", this.modelManager.getModel((ModelResourceLocation)" + entry.getValue() + "))";
                if (s.contains("ender"))
                {
                    EnderUtilities.logger.info(s);
                }
            }
        }

        return true;
    }
}
