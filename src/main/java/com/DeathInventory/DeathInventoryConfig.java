package com.DeathInventory;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("Death-Inventory")
public interface DeathInventoryConfig extends Config
{
	@ConfigItem(
			keyName = "showAfterDeath",
			name = "Show after dieing before banking",
			description = "Show the inventory display of what you had when you died after you have died before you visit the bank."
	) default boolean showAfterDeath() { return true; }

	@ConfigItem(
			keyName = "showAfterBank",
			name = "Show after Banking",
			description = "Show the inventory display of what you had when you died after you have finished banking."
	) default boolean showAfterBank() { return false; }

	enum showInBankEnum
	{
		Never,
		Once,
		Always
	}

	@ConfigItem(
			keyName = "showInBank",
			name = "Show whilst in the bank",
			description = "Show the inventory display once at the bank of what you had when you died"
	)
	default showInBankEnum showInBank() { return showInBankEnum.Once; }

	@ConfigItem(
			keyName = "alwaysShow",
			name = "Always show",
			description = "Show the inventory display of what you had when you died always. Overrides other options"
	) default boolean alwaysShow() { return false; }


	@ConfigItem(
			keyName = "toggleKeybind",
			name = "Key Always Show",
			description = "Binds a key (combination) to toggle always showing the inventory display."
	)
	default Keybind toggleKeybind()	{ return Keybind.NOT_SET; }
}

