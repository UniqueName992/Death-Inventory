package com.DeathInventory;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.api.widgets.ComponentID;

public class DeathInventoryOverlay extends OverlayPanel {
    private static final ImageComponent PLACEHOLDER_IMAGE = new ImageComponent(
            new BufferedImage(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));

    @Inject
    private DeathInventoryPlugin plugin;
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

    @Override
    public Dimension render(Graphics2D graphics) {

        if (plugin.isBankOpen() && plugin.getState().equals("0")) { plugin.putState("1"); }
        if (!plugin.isBankOpen() && plugin.getState().equals("1")) { plugin.putState("2"); }

        if (plugin.hotKey) {
            plugin.hotKey = false;
            plugin.forceDisplayed = !plugin.forceDisplayed;
            if (plugin.forceDisplayed) { plugin.shown = !plugin.shouldShow(); }
        }

        if (plugin.forceDisplayed && !plugin.shown) { return null; }
        if (!plugin.forceDisplayed && !plugin.shouldShow()) { return null; }

        final int[] itemQuantites = plugin.fromString(configMan.getConfiguration("Death-Inventory", "itemQuantites"));
        final int[] itemIDs = plugin.fromString(configMan.getConfiguration("Death-Inventory", "itemIDs"));

        for (int i = 0; i < itemIDs.length; i++) {
            if (itemQuantites[i] > 0) {
                final BufferedImage image = itemManager.getImage(itemIDs[i], itemQuantites[i], itemQuantites[i] > 1);
                if (image != null) {
                    panelComponent.getChildren().add(new ImageComponent(image));
                    continue;
                }
            }
            panelComponent.getChildren().add(PLACEHOLDER_IMAGE);
        }
        panelComponent.setBackgroundColor(new Color(168, 42, 30, 128));
        return super.render(graphics);
    }
}