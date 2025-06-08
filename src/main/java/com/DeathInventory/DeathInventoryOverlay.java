package com.DeathInventory;
import java.awt.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.*;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;

public class DeathInventoryOverlay extends OverlayPanel {
    private static final ImageComponent PLACEHOLDER_IMAGE = new ImageComponent(
            new BufferedImage(Constants.ITEM_SPRITE_WIDTH, Constants.ITEM_SPRITE_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR));

    @Inject
    private DeathInventoryPlugin plugin;

    @Inject
    private DeathInventoryOverlay()
    {
        setPosition(OverlayPosition.BOTTOM_RIGHT);
        panelComponent.setWrap(true);
        panelComponent.setGap(new Point(6, 4));
        panelComponent.setPreferredSize(new Dimension(4 * (Constants.ITEM_SPRITE_WIDTH + 6), 0));
        panelComponent.setOrientation(ComponentOrientation.HORIZONTAL);
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        if (plugin.hotKey) {
            plugin.hotKey = false;
            plugin.forceDisplayed = !plugin.forceDisplayed;
            if (plugin.forceDisplayed) { plugin.shown = !plugin.shouldShow(); }
        }

        if (plugin.forceDisplayed && !plugin.shown) { return null; }
        if (!plugin.forceDisplayed && !plugin.shouldShow()) { return null; }

        for (int i = 0; i < plugin.imageList.length; i++) {
            panelComponent.getChildren().add(plugin.imageList[i]);
        }

        panelComponent.setBackgroundColor(new Color(168, 42, 30, 128));
        return super.render(graphics);
    }
}