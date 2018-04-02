package fi.dy.masa.enderutilities.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.ThreadedFileIOBase;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import fi.dy.masa.enderutilities.EnderUtilities;

public class TemplateManagerEU
{
    protected final MinecraftServer server;
    protected final Map<String, TemplateEnderUtilities> templates;
    protected final Map<String, TemplateMetadata> templateMetas;
    protected final File directory;
    private final DataFixer fixer;

    public TemplateManagerEU(File directory, DataFixer dataFixer)
    {
        this.server = FMLCommonHandler.instance().getMinecraftServerInstance();
        this.templates = Maps.<String, TemplateEnderUtilities>newHashMap();
        this.templateMetas = Maps.<String, TemplateMetadata>newHashMap();
        this.directory = directory;
        this.fixer = dataFixer;
    }

    public TemplateEnderUtilities getTemplate(ResourceLocation id)
    {
        String s = id.getResourcePath();

        if (this.templates.containsKey(s))
        {
            return this.templates.get(s);
        }

        this.readTemplate(id);

        if (this.templates.containsKey(s))
        {
            return this.templates.get(s);
        }

        TemplateEnderUtilities template = new TemplateEnderUtilities();
        this.templates.put(s, template);
        return template;
    }

    public boolean readTemplate(ResourceLocation id)
    {
        String fileName = id.getResourcePath();
        File templateFile = new File(this.directory, fileName + ".nbt");
        InputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream(templateFile);
            this.readTemplateFromStream(fileName, inputStream);
            return true;
        }
        catch (Throwable e)
        {
            //EnderUtilities.logger.warn("Failed to read template from file '{}'", templateFile);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }

        return false;
    }

    private void readTemplateFromStream(String id, InputStream stream) throws IOException
    {
        NBTTagCompound nbt = CompressedStreamTools.readCompressed(stream);

        if (nbt.hasKey("DataVersion", Constants.NBT.TAG_ANY_NUMERIC) == false)
        {
            nbt.setInteger("DataVersion", 500);
        }

        TemplateEnderUtilities template = new TemplateEnderUtilities();
        template.read(this.fixer.process(FixTypes.STRUCTURE, nbt));
        this.templates.put(id, template);
    }

    public boolean writeTemplate(ResourceLocation id)
    {
        String fileName = id.getResourcePath();

        if (this.templates.containsKey(fileName) == false)
        {
            return false;
        }
        else
        {
            if (this.directory.exists() == false)
            {
                if (this.directory.mkdirs() == false)
                {
                    return false;
                }
            }
            else if (this.directory.isDirectory() == false)
            {
                return false;
            }

            final File templateFile = new File(this.directory, fileName + ".nbt");
            final NBTTagCompound nbt = new NBTTagCompound();
            final TemplateEnderUtilities template = this.templates.get(fileName);

            template.write(nbt);

            ThreadedFileIOBase.getThreadedIOInstance().queueIO(() ->
            {
                try
                {
                    OutputStream outputStream = new FileOutputStream(templateFile);
                    CompressedStreamTools.writeCompressed(nbt, outputStream);
                    outputStream.close();
                }
                catch (IOException e)
                {
                    EnderUtilities.logger.warn("Failed to write template to file '{}'", templateFile, e);
                }

                return false;
            });

            return true;
        }
    }

    public TemplateMetadata getTemplateMetadata(ResourceLocation rl)
    {
        String s = rl.getResourcePath();

        if (this.templateMetas.containsKey(s))
        {
            return this.templateMetas.get(s);
        }

        this.readTemplateMetadata(rl);

        if (this.templateMetas.containsKey(s))
        {
            return this.templateMetas.get(s);
        }

        TemplateMetadata templateMeta = new TemplateMetadata();
        this.templateMetas.put(s, templateMeta);
        return templateMeta;
    }

    public boolean readTemplateMetadata(ResourceLocation rl)
    {
        File templateFile = this.getTemplateMetadataFile(rl);
        InputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream(templateFile);
            this.readTemplateMetadataFromStream(rl.getResourcePath(), inputStream);
            return true;
        }
        catch (Throwable e)
        {
            //EnderUtilities.logger.warn("Failed to read template metadata from file '{}'", templateFile);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }

        return false;
    }

    protected File getTemplateMetadataFile(ResourceLocation rl)
    {
        return new File(this.directory, rl.getResourcePath() + "_meta.nbt");
    }

    private void readTemplateMetadataFromStream(String id, InputStream stream) throws IOException
    {
        NBTTagCompound nbt = CompressedStreamTools.readCompressed(stream);
        TemplateMetadata templateMeta = new TemplateMetadata();
        templateMeta.read(nbt);
        this.templateMetas.put(id, templateMeta);
    }

    public boolean writeTemplateMetadata(ResourceLocation rl)
    {
        String fileName = rl.getResourcePath();

        if (this.templateMetas.containsKey(fileName) == false)
        {
            return false;
        }
        else
        {
            if (this.directory.exists() == false)
            {
                if (this.directory.mkdirs() == false)
                {
                    return false;
                }
            }
            else if (this.directory.isDirectory() == false)
            {
                return false;
            }

            final File templateFile = new File(this.directory, fileName + "_meta.nbt");
            final NBTTagCompound nbt = new NBTTagCompound();
            final TemplateMetadata templateMeta = this.templateMetas.get(fileName);

            templateMeta.write(nbt);

            ThreadedFileIOBase.getThreadedIOInstance().queueIO(() ->
            {
                try
                {
                    OutputStream outputStream = new FileOutputStream(templateFile);
                    CompressedStreamTools.writeCompressed(nbt, outputStream);
                    outputStream.close();
                }
                catch (IOException e)
                {
                    EnderUtilities.logger.warn("Failed to write template metadata to file '{}'", templateFile);
                }

                return false;
            });

            return true;
        }
    }

    public FileInfo getTemplateInfo(ResourceLocation rl)
    {
        File file = this.getTemplateMetadataFile(rl);

        return new FileInfo(file.lastModified(), file.length());
    }

    public class FileInfo
    {
        public final long timestamp;
        public final long fileSize;

        public FileInfo(long timestamp, long fileSize)
        {
            this.timestamp = timestamp;
            this.fileSize = fileSize;
        }
    }
}
