package com.DeathInventory;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.ComponentID;
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
	static final int INVENTORY_SIZE = 28;
	private boolean playerDied = false;
	boolean forceDisplayed = false;
	boolean shown = false;
	boolean hotKey = false;
	int[] ditemIDs = new int[INVENTORY_SIZE];;
	int[] ditemQuantites = new int[INVENTORY_SIZE];;

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
				playerDied = true;
            }
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		if (statChanged.getSkill() == Skill.HITPOINTS) {
			if (statChanged.getBoostedLevel() == 0 && playerDied) {
				final ItemContainer itemContainer = client.getItemContainer(InventoryID.INVENTORY);
				if (itemContainer == null) {
					return;
				}
				final Item[] items = itemContainer.getItems();
				ditemIDs = new int[INVENTORY_SIZE];;
				ditemQuantites = new int[INVENTORY_SIZE];;

				for (int i = 0; i < INVENTORY_SIZE; i++) {
					if (i < items.length) {
						final Item item = items[i];

						if (item.getQuantity() > 0) {
							ditemIDs[i] = item.getId();
							ditemQuantites[i] = item.getQuantity();
						}
					}
				}
			}
			else if (statChanged.getLevel() == statChanged.getBoostedLevel()) {
				if ( playerDied ) {
					playerDied = false;
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
					if (!Arrays.equals(itemIDs, ditemIDs) || !Arrays.equals(itemQuantites, ditemQuantites)) {
						configMan.setConfiguration("Death-Inventory", "itemQuantites", Arrays.toString(ditemQuantites));
						configMan.setConfiguration("Death-Inventory", "itemIDs", Arrays.toString(ditemIDs));
						putState("0");
						forceDisplayed = false;
					}
				}
			}
		}
	}

	static int[] fromString(String string) {
		String[] strings = string.replace("[", "").replace("]", "").split(", ");
		int[] result = new int[strings.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(strings[i]);
		}
		return result;
	}

	void putState(String val) {
		configMan.setConfiguration("Death-Inventory","state", val);
	}

	String getState () {
		return configMan.getConfiguration("Death-Inventory", "state");
	}

	boolean shouldShow() {
		if (getState().equals("0") && !config.showAfterDeath()) { return false; }
		if (config.showInBank().toString().equals("Never") && getState().equals("1")) { return false; }
		if (isBankOpen() &&  !config.showInBank().toString().equals("Always") && getState().equals("2")) { return false; }
		if (!isBankOpen() && !config.showAfterBank() && getState().equals("2") ) { return false; }
		return true;
	}

	boolean isBankOpen() {
		if (client.getWidget(ComponentID.BANK_CONTAINER) == null) {
			return false;
		}
		return !client.getWidget(ComponentID.BANK_CONTAINER).isHidden();
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
			hotKey = true;;
		}
	};
}
