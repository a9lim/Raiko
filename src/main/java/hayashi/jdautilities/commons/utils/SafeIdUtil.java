// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>
// Copyright 2016-2018 John Grosh (jagrosh) <john.a.grosh@gmail.com> & Kaidan Gustave (TheMonitorLizard).
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

package hayashi.jdautilities.commons.utils;

public class SafeIdUtil {

    public static long safeConvert(String id) {
        try {
            return Math.max(Long.parseLong(id.trim()),0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean badId(String id) {
        try {
            return Long.parseLong(id.trim()) < 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }

}
