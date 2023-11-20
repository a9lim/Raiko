// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
// Copyright 2018 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

package hayashi.raiko.entities;

import java.util.Scanner;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Prompt {
    private final String title, noguiMessage;

    private boolean nogui;
    private final boolean noprompt;
    private Scanner scanner;

    public Prompt(String title) {
        this(title, null);
    }

    public Prompt(String title, String noguiMessage) {
        this(title, noguiMessage, "true".equalsIgnoreCase(System.getProperty("nogui")), "true".equalsIgnoreCase(System.getProperty("noprompt")));
    }

    public Prompt(String s, String ng, boolean b, boolean b1) {
        title = s;
        noguiMessage = ng == null ? "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag." : ng;
        nogui = b;
        noprompt = b1;
    }

    public boolean isNoGUI() {
        return nogui;
    }

    public void alert(Level level, String context, String message) {
        if (nogui) {
            Logger log = LoggerFactory.getLogger(context);
            switch (level) {
                case WARNING -> log.warn(message);
                case ERROR -> log.error(message);
                default -> log.info(message);
            }
            return;
        }
        try {
            JOptionPane.showMessageDialog(null, "<html><body><p style='width: 400px;'>" + message, title,
                    switch (level) {
                        case INFO -> JOptionPane.INFORMATION_MESSAGE;
                        case WARNING -> JOptionPane.WARNING_MESSAGE;
                        case ERROR -> JOptionPane.ERROR_MESSAGE;
                    });
        } catch (Exception e) {
            nogui = true;
            alert(Level.WARNING, context, noguiMessage);
            alert(level, context, message);
        }
    }

    public String prompt(String content) {
        if (noprompt)
            return null;
        if (nogui) {
            if (scanner == null)
                scanner = new Scanner(System.in);
            try {
                System.out.println(content);
                if (scanner.hasNextLine())
                    return scanner.nextLine();
            } catch (Exception e) {
                alert(Level.ERROR, title, "Unable to read input from command line.");
                e.printStackTrace();
            }
            return null;
        }
        try {
            return JOptionPane.showInputDialog(null, content, title, JOptionPane.QUESTION_MESSAGE);
        } catch (Exception e) {
            nogui = true;
            alert(Level.WARNING, title, noguiMessage);
            return prompt(content);
        }
    }

    public enum Level {
        INFO, WARNING, ERROR
    }
}
