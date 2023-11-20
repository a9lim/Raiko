// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
// Copyright 2021 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

package hayashi.raiko.audio;

import net.dv8tion.jda.api.entities.User;

public class RequestMetadata {
    public static final RequestMetadata EMPTY = new RequestMetadata(null);

    public final UserInfo user;

    public RequestMetadata(User u) {
        user = u == null ? null : new UserInfo(u.getIdLong(), u.getName(), u.getDiscriminator(), u.getEffectiveAvatarUrl());
    }

    public long getOwner() {
        return user == null ? 0L : user.id;
    }

    public static class RequestInfo {
        public final String query, url;

        private RequestInfo(String q, String u) {
            query = q;
            url = u;
        }
    }

    public static class UserInfo {
        public final long id;
        public final String username, discrim, avatar;

        private UserInfo(long l, String u, String d, String a) {
            id = l;
            username = u;
            discrim = d;
            avatar = a;
        }
    }
}
