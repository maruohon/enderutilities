package fi.dy.masa.enderutilities.client.resources;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ITransformation;
import net.minecraftforge.client.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

import fi.dy.masa.enderutilities.EnderUtilities;

@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public class EnderUtilitiesModelFactory
{
    private TextureMap textureMap;
    private FaceBakery faceBakery;
    public static EnderUtilitiesModelFactory instance;

    public EnderUtilitiesModelFactory(TextureMap textureMapIn)
    {
        this.textureMap = textureMapIn;
        this.faceBakery = new FaceBakery();
        instance = this;
    }

    public IFlexibleBakedModel bakeModel(ModelBlock modelBlockIn, ITransformation modelRotationIn, boolean uvLocked)
    {
        TextureAtlasSprite spriteParticle = this.textureMap.getTextureExtry(modelBlockIn.resolveTextureName("particle"));
        EnderUtilitiesSmartItemModelBase.Builder builder = (new EnderUtilitiesSmartItemModelBase.Builder(modelBlockIn)).setTexture(spriteParticle);
        Iterator<BlockPart> blockPartIterator = modelBlockIn.getElements().iterator();

        while (blockPartIterator.hasNext())
        {
            BlockPart blockPart = blockPartIterator.next();
            Iterator<EnumFacing> facingIterator = blockPart.mapFaces.keySet().iterator();

            while (facingIterator.hasNext())
            {
                EnumFacing facing = (EnumFacing)facingIterator.next();
                BlockPartFace blockPartFace = (BlockPartFace)blockPart.mapFaces.get(facing);
                TextureAtlasSprite spriteFace = this.textureMap.getTextureExtry(modelBlockIn.resolveTextureName(blockPartFace.texture));

                if (blockPartFace.cullFace == null || TRSRTransformation.isInteger(modelRotationIn.getMatrix()) == false)
                {
                    builder.addGeneralQuad(this.makeBakedQuad(blockPart, blockPartFace, spriteFace, facing, modelRotationIn, uvLocked));
                }
                else
                {
                    builder.addFaceQuad(modelRotationIn.rotate(blockPartFace.cullFace), this.makeBakedQuad(blockPart, blockPartFace, spriteFace, facing, modelRotationIn, uvLocked));
                }
            }
        }

        return builder.makeBakedModel();
    }

    private BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace blockPartFace, TextureAtlasSprite texture, EnumFacing facing, ITransformation transformation, boolean uvLocked)
    {
        return this.faceBakery.makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, blockPartFace, texture, facing, transformation, blockPart.partRotation, uvLocked, blockPart.shade);
    }

    public static IFlexibleBakedModel mergeModelsSimple(IFlexibleBakedModel in1, IFlexibleBakedModel in2)
    {
        List<BakedQuad> generalQuads = Lists.newArrayList();
        generalQuads.addAll(in1.getGeneralQuads());
        generalQuads.addAll(in2.getGeneralQuads());

        List<List<BakedQuad>> faceQuads = EnderUtilitiesSmartItemModelBase.newBlankFacingLists();
        for (EnumFacing facing : EnumFacing.values())
        {
            faceQuads.get(facing.ordinal()).addAll(in1.getFaceQuads(facing));
            faceQuads.get(facing.ordinal()).addAll(in2.getFaceQuads(facing));
        }

        return new EnderUtilitiesSmartItemModelBase(generalQuads, faceQuads, in1.isAmbientOcclusion(), in1.isGui3d(), in1.getTexture(), in1.getItemCameraTransforms());
    }

    public static void printModelData(String modelName)
    {
        ModelResourceLocation mrl = new ModelResourceLocation(modelName, "inventory");
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager().getModel(mrl);
        if (model == null)
        {
            EnderUtilities.logger.info("model == null");
            return;
        }

        EnderUtilities.logger.info("model name: " + modelName + " model: " + model.toString());
        EnderUtilities.logger.info("generalQuads:");
        int i = 0;
        for (Object o : model.getGeneralQuads())
        {
            BakedQuad quad = (BakedQuad) o;
            EnderUtilities.logger.info(String.format("BakedQuad: %d tintIndex: %d", i, quad.getTintIndex()));
            int[] vd = quad.getVertexData();
            //System.out.println("vertex data length: " + vd.length);
            for (int j = 0; j < (vd.length / 7); ++j)
            {
                int k = j * 7;
                float x = Float.intBitsToFloat(vd[k + 0]);
                float y = Float.intBitsToFloat(vd[k + 1]);
                float z = Float.intBitsToFloat(vd[k + 2]);
                float u = Float.intBitsToFloat(vd[k + 4]);
                float v = Float.intBitsToFloat(vd[k + 5]);
                EnderUtilities.logger.info(String.format("vertex %d: x:%f y:%f z:%f shadeColor:0x%02X u:%f v:%f unused:%d", j, x, y, z, vd[k + 3], u, v, vd[k + 6]));
            }
            i++;
        }

        EnderUtilities.logger.info("faceQuads:");
        for (EnumFacing facing : EnumFacing.values())
        {
            i = 0;
            EnderUtilities.logger.info("face: " + facing);
            for (Object o : model.getFaceQuads(facing))
            {
                BakedQuad quad = (BakedQuad) o;
                EnderUtilities.logger.info(String.format("BakedQuad: %d tintIndex: %d", i, quad.getTintIndex()));
                int[] vd = quad.getVertexData();
                //System.out.println("vertex data length: " + vd.length);
                for (int j = 0; j < (vd.length / 7); ++j)
                {
                    int k = j * 7;
                    float x = Float.intBitsToFloat(vd[k + 0]);
                    float y = Float.intBitsToFloat(vd[k + 1]);
                    float z = Float.intBitsToFloat(vd[k + 2]);
                    float u = Float.intBitsToFloat(vd[k + 4]);
                    float v = Float.intBitsToFloat(vd[k + 5]);
                    EnderUtilities.logger.info(String.format("vertex %d: x:%f y:%f z:%f shadeColor:0x%02X u:%f v:%f unused:%d", j, x, y, z, vd[k + 3], u, v, vd[k + 6]));
                }
                i++;
            }
        }
    }
}
