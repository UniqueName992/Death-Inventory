package com.DeathInventory;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.api.widgets.ComponentID;

public class DeathInventoryOverlay extends OverlayPanel{
    private static final ImageComponent PLACEHOLDER_IMAGE = new ImageComponent(
            new BufferedImage(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));

    private final Client client;
    private final ItemManager itemManager;
    private final ConfigManager configMan;
    private final DeathInventoryConfig config;

    @Inject
    private DeathInventoryOverlay(Client client, ItemManager itemManager, ConfigManager configMan, DeathInventoryConfig config)
    {
        setPosition(OverlayPosition.BOTTOM_RIGHT);
        panelComponent.setWrap(true);
        panelComponent.setGap(new Point(6, 4));
        panelComponent.setPreferredSize(new Dimension(4 * (Constants.ITEM_SPRITE_WIDTH + 6), 0));
        panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
        this.itemManager = itemManager;
        this.client = client;
        this.configMan = configMan;
        this.config = config;
    }

    private static int[] fromString(String string) {
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        int[] result = new int[strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(strings[i]);
        }
        return result;
    }

    private void putState(String val) {
        configMan.setConfiguration("Death-Inventory","state", val);
    }

    private String getState () {
        return configMan.getConfiguration("Death-Inventory", "state");
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // if bank is open, log the state
        if (client.getWidget(ComponentID.BANK_CONTAINER) != null && getState().equals("0")) { putState("1"); }

        // if bank was just open and now closed set state to banked
        if (client.getWidget(ComponentID.BANK_CONTAINER) == null && getState().equals("1")) { putState("2"); }

        // hide after death if configured and not always shown before banking
        if (getState().equals("0") && !config.showAfterDeath() && !config.alwaysShow()) { return null; }

        // Hide in the bank after death if not showing and not always shown and not alwyas shown in bank
        if (config.showInBank().toString().equals("Never") && getState().equals("1") && !config.alwaysShow()) { return null; }

        // Hide in the bank always if not showing and not always shown and not alwyas shown in bank
        if (client.getWidget(ComponentID.BANK_CONTAINER) != null &&  !config.showInBank().toString().equals("Always") && getState().equals("2") && !config.alwaysShow()) { return null; }

        // Hide in the world always if not showing and not always shown and not alwyas shown in bank
        if (client.getWidget(ComponentID.BANK_CONTAINER) == null &&  !config.showAfterBank() && getState().equals("2") && !config.alwaysShow()) { return null; }

        // Retrieve the inventory when died
        final int[] itemQuantites = fromString(configMan.getConfiguration("Death-Inventory", "itemQuantites"));
        final int[] itemIDs = fromString(configMan.getConfiguration("Death-Inventory", "itemIDs"));

        // Loop over and draw each item
        for (int i = 0; i < itemIDs.length; i++) {
            if (itemQuantites[i] > 0) {
                final BufferedImage image = itemManager.getImage(itemIDs[i], itemQuantites[i], itemQuantites[i] > 1);
                if (image != null) {
                    panelComponent.getChildren().add(new ImageComponent(image));
                    continue;
                }
            }

            // put a placeholder image so each item is aligned properly and the panel is not resized
            panelComponent.getChildren().add(PLACEHOLDER_IMAGE);
        }

        return super.render(graphics);
    }

    public void toggle() {
        configMan.setConfiguration("Death-Inventory", "alwaysShow", !config.alwaysShow());
    }
}

