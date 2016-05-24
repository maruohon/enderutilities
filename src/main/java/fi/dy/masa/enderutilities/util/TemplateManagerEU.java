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
    protected final String baseFolder;

    public TemplateManagerEU(String baseFolder)
    {
        this.templates = Maps.<String, TemplateEnderUtilities>newHashMap();
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
}
