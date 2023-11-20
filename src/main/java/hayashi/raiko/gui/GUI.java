// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
// Copyright 2016 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.

package hayashi.raiko.gui;

import hayashi.raiko.Bot;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class GUI extends JFrame {
    private final ConsolePanel console;
    private final Bot bot;

    public GUI(Bot b) {
        bot = b;
        console = new ConsolePanel();
    }

    public void init() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Raiko");
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Console", console);
        getContentPane().add(tabs);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) { /* unused */ }

            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    bot.shutdown();
                } catch (Exception ex) {
                    System.exit(0);
                }
            }

            @Override
            public void windowClosed(WindowEvent e) { /* unused */ }

            @Override
            public void windowIconified(WindowEvent e) { /* unused */ }

            @Override
            public void windowDeiconified(WindowEvent e) { /* unused */ }

            @Override
            public void windowActivated(WindowEvent e) { /* unused */ }

            @Override
            public void windowDeactivated(WindowEvent e) { /* unused */ }
        });
    }
}
