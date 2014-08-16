package fi.dy.masa.enderutilities.setup;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import fi.dy.masa.enderutilities.reference.Reference;

public class EUConfigReader
{
	public static void loadConfigsAll(File baseConfigDir)
	{
		// minecraft/config/enderutilities/something.cfg
		File configDir = new File(baseConfigDir.getAbsolutePath().concat("/").concat(Reference.MOD_ID));
		configDir.mkdirs();

		EUConfigReader.loadConfigsGeneric(new File(configDir, Reference.MOD_ID + "_main.cfg"));
		EUConfigReader.loadConfigsLists(new File(configDir, Reference.MOD_ID + "_lists.cfg"));
	}

	public static void loadConfigsGeneric(File configFile)
	{
		String category;
		Configuration conf = new Configuration(configFile);
		conf.load();

		category = "Generic";
		EUConfigs.enderBowAllowPlayers = conf.get(category, "EnderBowAllowPlayers", false).setRequiresMcRestart(true);
		EUConfigs.enderBowAllowPlayers.comment = "Is the Ender Bow allowed to teleport players (directly or in a 'stack' riding something)";

		EUConfigs.enderBowAllowSelfTP = conf.get(category, "EnderBowAllowSelfTP", true).setRequiresMcRestart(true);
		EUConfigs.enderBowAllowSelfTP.comment = "Can the Ender Bow be used in the 'TP Self' mode";

		EUConfigs.enderLassoAllowPlayers = conf.get(category, "EnderLassoAllowPlayers", false).setRequiresMcRestart(true);
		EUConfigs.enderLassoAllowPlayers.comment = "Is the Ender Lasso allowed to teleport players (directly or in a 'stack' riding something)";

		if (conf.hasChanged() == true)
		{
			conf.save();
		}
	}

	public static void loadConfigsLists(File configFile)
	{
		String category;
		Configuration conf = new Configuration(configFile);
		conf.load();

		category = "EnderBag";
		EUConfigs.enderBagListType = conf.get(category, "ListType", "whitelist").setRequiresMcRestart(true);
		EUConfigs.enderBagListType.comment = "Target control list type used for Ender Bag. Allowed values: blacklist, whitelist.";

		EUConfigs.enderBagBlacklist = conf.get(category, "BlackList", new String[] {}).setRequiresMcRestart(true);
		EUConfigs.enderBagBlacklist.comment = "Block types the Ender Bag is NOT allowed to (= doesn't properly) work with.";

		EUConfigs.enderBagWhitelist = conf.get(category, "WhiteList", new String[] {"chest", "dispenser", "dropper", "furnace", "hopper", "trapped_chest"}).setRequiresMcRestart(true);
		EUConfigs.enderBagWhitelist.comment = "Block types the Ender Bag is allowed to (= should properly) work with.";

		category = "Teleporting";
		EUConfigs.teleportBlacklist = conf.get(category, "EntityBlackList", new String[] {"EntityDragon", "EntityDragonPart", "EntityWither"}).setRequiresMcRestart(true);
		EUConfigs.teleportBlacklist.comment = "Entities that are not allowed to be teleported using any methods";

		if (conf.hasChanged() == true)
		{
			conf.save();
		}
	}
}
