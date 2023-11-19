/*
 * Copyright 2021 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
