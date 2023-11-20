// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2020 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

package a9lim.raiko.settings;

public enum RepeatMode {
    OFF(null, "Off"),
    ALL("\uD83D\uDD01", "All"), // 🔁
    SINGLE("\uD83D\uDD02", "Single"); // 🔂

    private final String emoji, userFriendlyName;

    RepeatMode(String e, String s) {
        emoji = e;
        userFriendlyName = s;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getUserFriendlyName() {
        return userFriendlyName;
    }
}
