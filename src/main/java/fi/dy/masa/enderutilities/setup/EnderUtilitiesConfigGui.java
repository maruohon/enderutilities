package fi.dy.masa.enderutilities.setup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import fi.dy.masa.enderutilities.reference.Reference;

public class EnderUtilitiesConfigGui extends GuiConfig
{
    public EnderUtilitiesConfigGui(GuiScreen parent)
    {
        super(parent, getConfigElements(), Reference.MOD_ID, false, false, getTitle(parent));
    }

    private static List<IConfigElement> getConfigElements()
    {
        List<IConfigElement> configElements = new ArrayList<>();

        configElements.addAll(new ConfigElement(ConfigReader.config.getCategory(ConfigReader.CATEGORY_GENERIC)).getChildElements());
        configElements.addAll(new ConfigElement(ConfigReader.config.getCategory(ConfigReader.CATEGORY_CLIENT)).getChildElements());

        return configElements;
    }

    private static String getTitle(GuiScreen parent)
    {
        return GuiConfig.getAbridgedConfigPath(ConfigReader.configurationFile.toString());
    }
}
