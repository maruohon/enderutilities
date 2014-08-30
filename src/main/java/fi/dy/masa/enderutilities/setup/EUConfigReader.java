package fi.dy.masa.enderutilities.setup;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.item.ReferenceItem;

public class EUConfigReader
{
	public static final int CURRENT_CONFIG_VERSION = 32;
	public static int confVersion = 0;

	public static void loadConfigsAll(File baseConfigDir)
	{
		// minecraft/config/enderutilities/something.cfg
		File configDir = new File(baseConfigDir.getAbsolutePath().concat("/").concat(Reference.MOD_ID));
		configDir.mkdirs();

		EnderUtilities.logger.info("Loading configuration...");
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

		EUConfigs.enderBucketCapacity = conf.get(category, "EnderBucketCapacity", ReferenceItem.ENDER_BUCKET_MAX_AMOUNT).setRequiresMcRestart(true);
		EUConfigs.enderBucketCapacity.comment = "Maximum amount the Ender Bucket can hold, in millibuckets. Default: 16000 mB (= 16 buckets).";

		EUConfigs.enderLassoAllowPlayers = conf.get(category, "EnderLassoAllowPlayers", false).setRequiresMcRestart(true);
		EUConfigs.enderLassoAllowPlayers.comment = "Is the Ender Lasso allowed to teleport players (directly or in a 'stack' riding something)";

		category = "Version";
		// 0.3.1 was the version where the configs were first added, use that as the default (note that the version number itself was added later in 0.3.2)
		EUConfigs.configFileVersion = conf.get(category, "ConfigFileVersion", 31).setRequiresMcRestart(true);
		EUConfigs.configFileVersion.comment = "Internal config file version tracking. DO NOT CHANGE!!";
		confVersion = EUConfigs.configFileVersion.getInt();

		// Update the version in the config to the current version
		EUConfigs.configFileVersion.setValue(CURRENT_CONFIG_VERSION);

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

		EUConfigs.enderBagWhitelist = conf.get(category, "WhiteList", new String[] {"minecraft:chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:ender_chest", "minecraft:furnace", "minecraft:hopper", "minecraft:trapped_chest"}).setRequiresMcRestart(true);
		EUConfigs.enderBagWhitelist.comment = "Block types the Ender Bag is allowed to (= should properly) work with.";

		category = "Teleporting";
		EUConfigs.teleportBlacklist = conf.get(category, "EntityBlackList", new String[] {"EntityDragon", "EntityDragonPart", "EntityEnderCrystal", "EntityWither"}).setRequiresMcRestart(true);
		EUConfigs.teleportBlacklist.comment = "Entities that are not allowed to be teleported using any methods";

		updateConfigLists(conf);

		if (conf.hasChanged() == true)
		{
			conf.save();
		}
	}

	public static void updateConfigLists(Configuration conf)
	{
		boolean found = false;
		int i = 0;

		// 0.3.2: Add EntityEnderCrystal to teleport blacklist
		if (confVersion < 32)
		{
			EnderUtilities.logger.info("Updating configuration lists to 0.3.2");

			String[] strs = EUConfigs.teleportBlacklist.getStringList();
			String[] strsNew = new String[strs.length + 1];
			for (i = 0; i < strs.length; ++i)
			{
				strsNew[i] = strs[i];
				if (strs[i].equals("EntityEnderCrystal") == true)
				{
					found = true;
				}
			}

			if (found == false)
			{
				strsNew[i] = "EntityEnderCrystal";
				EUConfigs.teleportBlacklist.setValues(strsNew);
			}
		}
	}
}
