package fi.dy.masa.enderutilities.config;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
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
        List<IConfigElement> configElements = new ArrayList<IConfigElement>();

        Configuration config = ConfigReader.loadConfigsFromFile();
        configElements.add(new ConfigElement(config.getCategory(ConfigReader.CATEGORY_GENERIC)));
        configElements.add(new ConfigElement(config.getCategory(ConfigReader.CATEGORY_BUILDERSWAND)));
        configElements.add(new ConfigElement(config.getCategory(ConfigReader.CATEGORY_LISTS)));
        configElements.add(new ConfigElement(config.getCategory(ConfigReader.CATEGORY_CLIENT)));

        return configElements;
    }

    private static String getTitle(GuiScreen parent)
    {
        return GuiConfig.getAbridgedConfigPath(ConfigReader.getConfigFile().toString());
    }
}
