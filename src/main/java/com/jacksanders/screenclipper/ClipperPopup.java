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

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to handle the {@link PopupMenu} that appears when the tray icon is right-clicked.
 */
class ClipperPopup extends PopupMenu {
    /** Language management frame */
    LanguageManager lm;

    /** The controlling instance of {@link ScreenClipper} */
    ScreenClipper app;

    /**
     * Construct a basic popup menu
     */
    protected ClipperPopup(ScreenClipper sc) {
        app = sc;
        reset();
    }

    /**
     * Get details of currently installed languages, and constructs a language selection submenu.
     * @return The language selection submenu
     */
    protected Menu updateLanguages() {
        Menu languageSub = new Menu("Select Language");

        String[] f = new File(ScreenClipper.RESOURCE_DIR + "/tessdata").list();

        if (f == null) {
            ScreenClipper.LOG.error("Language data files not found. Please ensure they haven't been deleted");
            return null;
        }

        // Filter to get only valid language files, and sort these by their language names.
        List<String> langs = Arrays.stream(f).map(s -> s.replace(".traineddata", "")) // Remove file extension
                                                .filter(ScreenClipper.LANG_MAP::containsKey) // Filters only valid languages
                                                .sorted(new Comparator<String>() {
                                                    @Override
                                                    public int compare(String o1, String o2) {
                                                        Map<String, String> m = ScreenClipper.LANG_MAP;
                                                        return m.get(o1).compareToIgnoreCase(m.get(o2));
                                                    }
                                                })
                                                .collect(Collectors.toList());

        if (langs.size() == 0) {
            languageSub.add(new MenuItem("No Languages Found!"));
        }

        for (String lang : langs) {
            CheckboxMenuItem tempItem = new CheckboxMenuItem(ScreenClipper.LANG_MAP.get(lang));
            if (app.getLang().equals(lang)) {
                tempItem.setState(true); // Tick current language
            }
            tempItem.addItemListener(e -> {
                app.setLang(lang);

                // Ensure that all checkboxes other than current one are disabled.
                for (int i=0; i<langs.size(); i++) {
                    CheckboxMenuItem check = (CheckboxMenuItem) languageSub.getItem(i);
                    check.setState(false);
                }

                tempItem.setState(true);
            });
            languageSub.add(tempItem);
        }

        if (lm == null) {
            lm = new LanguageManager(langs, this);
        } else {
            lm.updateLangs(langs);
        }

        return languageSub;
    }

    /**
     * Reset the popup menu, and update languages
     */
    protected void reset() {
        removeAll();

        // Language Config
        Menu languageSub = updateLanguages();

        // Manage Languages
        MenuItem addMoreMenuItem = new MenuItem("Install/Remove Languages");
        addMoreMenuItem.addActionListener(e -> {
            lm.setVisible(true);
        });

        MenuItem takeScreenshot = new MenuItem("Take Clipping");
        takeScreenshot.addActionListener(e -> {
            app.onHotKey(1001); // Spoof a hotkey press
        });

        // Exit Button
        MenuItem exit = new MenuItem("Close ScreenClipper");
        exit.addActionListener(e -> {
            System.exit(0);
        });

        add(languageSub);
        add(addMoreMenuItem);
        add(takeScreenshot);
        add(exit);
    }
}
