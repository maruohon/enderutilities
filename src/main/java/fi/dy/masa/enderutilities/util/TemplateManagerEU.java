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

public class TemplateManagerEU
{
    protected final Map<String, TemplateEnderUtilities> templates;
    protected final Map<String, TemplateMetadata> templateMetas;
    protected final String baseFolder;

    public TemplateManagerEU(String baseFolder)
    {
        this.templates = Maps.<String, TemplateEnderUtilities>newHashMap();
        this.templateMetas = Maps.<String, TemplateMetadata>newHashMap();
        this.baseFolder = baseFolder;
    }

    public TemplateEnderUtilities getTemplate(MinecraftServer server, ResourceLocation id)
    {
        String s = id.getResourcePath();

        if (this.templates.containsKey(s))
        {
            return this.templates.get(s);
        }

        if (server != null)
        {
            this.readTemplate(server, id);
        }

        if (this.templates.containsKey(s))
        {
            return this.templates.get(s);
        }

        TemplateEnderUtilities template = new TemplateEnderUtilities();
        this.templates.put(s, template);
        return template;
    }

    public boolean readTemplate(MinecraftServer server, ResourceLocation id)
    {
        String fileName = id.getResourcePath();
        File templateDir = server.getFile(this.baseFolder);
        File templateFile = new File(templateDir, fileName + ".nbt");
        InputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream(templateFile);
            this.readTemplateFromStream(fileName, inputStream);
            return true;
        }
        catch (Throwable var12)
        {
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
        TemplateEnderUtilities template = new TemplateEnderUtilities();
        template.read(nbt);
        this.templates.put(id, template);
    }

    public boolean writeTemplate(MinecraftServer server, ResourceLocation id)
    {
        String fileName = id.getResourcePath();

        if (this.templates.containsKey(fileName) == false)
        {
            return false;
        }
        else
        {
            File templateDir = server.getFile(this.baseFolder);

            if (templateDir.exists() == false)
            {
                if (templateDir.mkdirs() == false)
                {
                    return false;
                }
            }
            else if (templateDir.isDirectory() == false)
            {
                return false;
            }

            File templateFile = new File(templateDir, fileName + ".nbt");
            NBTTagCompound nbt = new NBTTagCompound();
            TemplateEnderUtilities template = this.templates.get(fileName);
            OutputStream outputStream = null;

            try
            {
                template.write(nbt);
                outputStream = new FileOutputStream(templateFile);
                CompressedStreamTools.writeCompressed(nbt, outputStream);
                return true;
            }
            catch (Throwable e)
            {
            }
            finally
            {
                IOUtils.closeQuietly(outputStream);
            }

            return false;
        }
    }

    public TemplateMetadata getTemplateMetadata(MinecraftServer server, ResourceLocation rl)
    {
        String s = rl.getResourcePath();

        if (this.templateMetas.containsKey(s))
        {
            return this.templateMetas.get(s);
        }

        if (server != null)
        {
            this.readTemplateMetadata(server, rl);
        }

        if (this.templateMetas.containsKey(s))
        {
            return this.templateMetas.get(s);
        }

        TemplateMetadata templateMeta = new TemplateMetadata();
        this.templateMetas.put(s, templateMeta);
        return templateMeta;
    }

    public boolean readTemplateMetadata(MinecraftServer server, ResourceLocation rl)
    {
        File templateFile = this.getTemplateMetadataFile(server, rl);
        InputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream(templateFile);
            this.readTemplateMetadataFromStream(rl.getResourcePath(), inputStream);
            return true;
        }
        catch (Throwable var12)
        {
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }

        return false;
    }

    protected File getTemplateMetadataFile(MinecraftServer server, ResourceLocation rl)
    {
        String fileName = rl.getResourcePath();
        File templateDir = server.getFile(this.baseFolder);

        return new File(templateDir, fileName + "_meta.nbt");
    }

    private void readTemplateMetadataFromStream(String id, InputStream stream) throws IOException
    {
        NBTTagCompound nbt = CompressedStreamTools.readCompressed(stream);
        TemplateMetadata templateMeta = new TemplateMetadata();
        templateMeta.read(nbt);
        this.templateMetas.put(id, templateMeta);
    }

    public boolean writeTemplateMetadata(MinecraftServer server, ResourceLocation rl)
    {
        String fileName = rl.getResourcePath();

        if (this.templateMetas.containsKey(fileName) == false)
        {
            return false;
        }
        else
        {
            File templateDir = server.getFile(this.baseFolder);

            if (templateDir.exists() == false)
            {
                if (templateDir.mkdirs() == false)
                {
                    return false;
                }
            }
            else if (templateDir.isDirectory() == false)
            {
                return false;
            }

            File templateFile = new File(templateDir, fileName + "_meta.nbt");
            NBTTagCompound nbt = new NBTTagCompound();
            TemplateMetadata templateMeta = this.templateMetas.get(fileName);
            OutputStream outputStream = null;

            try
            {
                templateMeta.write(nbt);
                outputStream = new FileOutputStream(templateFile);
                CompressedStreamTools.writeCompressed(nbt, outputStream);
                return true;
            }
            catch (Throwable e)
            {
            }
            finally
            {
                IOUtils.closeQuietly(outputStream);
            }

            return false;
        }
    }

    public FileInfo getTemplateInfo(MinecraftServer server, ResourceLocation rl)
    {
        File file = this.getTemplateMetadataFile(server, rl);

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
