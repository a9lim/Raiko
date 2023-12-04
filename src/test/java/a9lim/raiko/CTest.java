// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
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


package a9lim.raiko;
import a9lim.raiko.chat.ChatBot;
import a9lim.raiko.entities.Prompt;

import java.util.Scanner;
public class CTest {
    public static void main(String[] args) {

        final BotConfig config = new BotConfig(new Prompt("Raiko"));
        config.load();
        final ChatBot chatBot = new ChatBot(config);
        Scanner s = new Scanner(System.in);
        while (true){
            System.out.println(chatBot.chat(s.nextLine(), 0L));
        }
    }
}
