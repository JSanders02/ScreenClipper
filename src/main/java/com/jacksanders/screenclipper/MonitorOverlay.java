/*
 * Copyright (c) 2022 Jack Sanders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jacksanders.screenclipper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A class used to cover the user's monitors with a translucent black color, to indicate that they are taking a
 * screenshot (Similar to Windows's screen snip tool). It also handles events relating to drawing an area to capture.
 *
 * @author Jack Sanders
 * @version 1.0.0 20/05/2022
 */
class MonitorOverlay extends JFrame {
    /** Indicates whether a screencapture is being drawn or not */
    private boolean screenshot;

    /** Where the com.jacksanders.screenclipper.MonitorOverlay should call back to when a screencap is complete */
    private final ScreenClipper parent;

    /** The {@link ScreencapController} object used to actually draw the screencap area */
    private ScreencapController screencapController;


    /**
     * Constructor for {@link MonitorOverlay}
     * @param m The bounds of the screen being covered
     * @param p The base {@link ScreenClipper} object
     */
    protected MonitorOverlay(Rectangle m, ScreenClipper p) {
        setType(Type.UTILITY); // Hide icon from taskbar
        updateCoveredMonitor(m); // Cover entire designated screen

        // Set frame to be undecorated (cannot be moved, no title bar/border), with a translucent background
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 100));
        setTitle("ScreenClipper");

        parent = p;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == 1 && !screenshot) {
                    screenshot = true;
                    screencapController = new ScreencapController(e.getPoint());
                    screencapController.setSize(getSize());
                    add(screencapController);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == 1 && screenshot) { parent.createNewScreenCapture(); }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (screenshot) {
                    screencapController.update(e);
                }
            }
        });
    }

    /**
     * @return The area of the screen being covered by this overlay
     */
    protected Rectangle getScreenArea() { return getBounds(); }

    /**
     * @return The bounds of the screen capture, from {@link ScreencapController}
     */
    protected Rectangle getCaptureRect() { return screencapController.capture(); }

    /**
     * @return Whether or not a {@link MonitorOverlay} is currently capturing anything
     */
    protected boolean hasCapture() { return screencapController != null; }

    /**
     * Toggles visibility of the overlay
     */
    protected void toggle() { setVisible(!isVisible()); }

    /**
     * Used to update which screen a {@link MonitorOverlay} object is covering
     * @param m The bounds of the new screen to cover
     */
    protected void updateCoveredMonitor(Rectangle m) {
        setLocation(m.getLocation());
        setSize(m.getSize());
    }

    /**
     * Resets instance variables of {@link MonitorOverlay}, hides it, and removes screencapController, if one exists.
     */
    protected void reset() {
        screenshot = false;
        setVisible(false);
        if (hasCapture()) {
            remove(screencapController);
            screencapController = null;
        }
    }
}
