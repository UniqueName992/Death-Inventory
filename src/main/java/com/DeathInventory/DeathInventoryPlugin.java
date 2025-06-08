package com.DeathInventory;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WidgetClosed;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.util.HotkeyListener;

import java.awt.image.BufferedImage;
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
	private boolean playerDied = false;
	private int[][] deathItems = new int[2][INVENTORY_SIZE];
	private int[][] respawnItems = new int[2][INVENTORY_SIZE];
	private boolean bankOpen = false;
	static final int INVENTORY_SIZE = 28;
	ItemContainer itemContainer;
	int[][] displayItems = new int[2][INVENTORY_SIZE];;
	boolean forceDisplayed = false;
	boolean shown = false;
	ImageComponent[] imageList;
	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		keyManager.registerKeyListener(hotkeyListener);
		String r = configMan.getConfiguration("Death-Inventory", "items");
		if (r == null) return;
		String[] s = r.split("], \\[");
		String[][] t = {s[0].replace("[", "").split(", "), s[1].replace("]", "").split(", ")};
		for (int j = 0; j < 2; j++) {
			for (int i = 0; i < t[0].length; i++) {
				displayItems[j][i] = Integer.parseInt(t[j][i]);
			}
		}
		getImageList();
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
		if (statChanged.getSkill() != Skill.HITPOINTS || !playerDied) return;
		if (statChanged.getBoostedLevel() == 0) {
			deathItems = getInv();
		} else if (statChanged.getLevel() == statChanged.getBoostedLevel()) {
			playerDied = false;
			respawnItems = getInv();
			if (!Arrays.deepEquals(respawnItems, deathItems)) {
				displayItems=deathItems;
				configMan.setConfiguration("Death-Inventory", "items", Arrays.deepToString(displayItems));
				getImageList();
				putState("0");
				forceDisplayed = false;
			}
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() == InterfaceID.BANKMAIN) {
			if (getState().equals("0")) { putState("1"); }
			bankOpen = true;
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event) {
		if (event.getGroupId() == InterfaceID.BANKMAIN) {
			if (getState().equals("1")) { putState("2"); }
			bankOpen = false;
		}
	}

	private int[][] getInv() {
		itemContainer = client.getItemContainer(InventoryID.INVENTORY);
		if (itemContainer == null) return null;
		Item[] itemsArray = itemContainer.getItems();
		int[][] items = new int[2][INVENTORY_SIZE];
		for (int i = 0; i < itemsArray.length; i++) {
			final Item item = itemsArray[i];
			if (item.getQuantity() > 0) {
				items[0][i] = item.getId();
				items[1][i] = item.getQuantity();
			}
		}
		return items;
	}

	private void getImageList() {
		imageList = new ImageComponent[displayItems[0].length];
		for (int i = 0; i < displayItems[0].length; i++) {
			if (displayItems[1][i] > 0) {
				imageList[i] = new ImageComponent(itemManager.getImage(displayItems[0][i], displayItems[1][i], displayItems[1][i] > 1));
			} else {
				imageList[i] = new ImageComponent(new BufferedImage(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));
			}
		}
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
		if (bankOpen &&  !config.showInBank().toString().equals("Always") && getState().equals("2")) { return false; }
		if (!bankOpen && !config.showAfterBank() && getState().equals("2") ) { return false; }
		return true;
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
			forceDisplayed = !forceDisplayed;
			if (forceDisplayed) shown = !shouldShow();
		}
	};
}
