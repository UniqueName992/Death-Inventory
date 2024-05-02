package com.DeathInventory;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.ui.overlay.OverlayManager;

import net.runelite.client.util.HotkeyListener;

import java.util.Arrays;

@Slf4j
@PluginDescriptor(
	name = "Death Inventory"
)

public class DeathInventoryPlugin extends Plugin
{
	private static final int INVENTORY_SIZE = 28;
	@Getter
    @Inject
	private Client client;
	@Inject
	private ConfigManager configMan;
	@Inject
	private DeathInventoryConfig config;
	@Inject
	private DeathInventoryOverlay overlay;
	@Inject
	private OverlayManager overlayManager;
	@Inject
	private KeyManager keyManager;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		keyManager.unregisterKeyListener(hotkeyListener);
	}

	@Subscribe
	public void onActorDeath(ActorDeath actorDeath)
	{
		Actor actor = actorDeath.getActor();
		if (actor instanceof Player)
		{
			Player player = (Player) actor;
			if (player == client.getLocalPlayer()) {
				final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
				if (itemContainer == null) {
					return;
				}
				final Item[] items = itemContainer.getItems();
				int[] itemIDs = new int[INVENTORY_SIZE];
				int[] itemQuantites = new int[INVENTORY_SIZE];

				for (int i = 0; i < INVENTORY_SIZE; i++) {
					if (i < items.length) {
						final Item item = items[i];

						if (item.getQuantity() > 0) {
							itemIDs[i] = item.getId();
							itemQuantites[i] = item.getQuantity();
						}
					}
				}
				configMan.setConfiguration("Death-Inventory","state", "0");
				configMan.setConfiguration("Death-Inventory", "itemQuantites", Arrays.toString(itemQuantites));
				configMan.setConfiguration("Death-Inventory", "itemIDs", Arrays.toString(itemIDs));

            }
		}
	}

	@Provides
	DeathInventoryConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DeathInventoryConfig.class);
	}

	private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleKeybind())
	{
		@Override
		public void hotkeyPressed()
		{
			overlay.toggle();
		}
	};
}
