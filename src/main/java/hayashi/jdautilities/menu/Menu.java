/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hayashi.jdautilities.menu;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.*;

import javax.annotation.Nullable;

public abstract class Menu {
    protected final EventWaiter waiter;
    protected Set<User> users;
    protected Set<Role> roles;
    protected final long timeout;
    protected final TimeUnit unit;

    protected Menu(EventWaiter w, Set<User> u, Set<Role> r, long t, TimeUnit unidad) {
        waiter = w;
        users = u;
        roles = r;
        timeout = t;
        unit = unidad;
    }

    public abstract void display(MessageChannel channel);

    public abstract void display(Message message);

    protected boolean isValidUser(User user) {
        return isValidUser(user, null);
    }

    protected boolean isValidUser(User user, @Nullable Guild guild) {
        if (user.isBot())
            return false;
        if ((users.isEmpty() && roles.isEmpty()) || users.contains(user))
            return true;
        if (guild == null || !guild.isMember(user))
            return false;

        return guild.getMember(user).getRoles().stream().anyMatch(roles::contains);
    }

    @SuppressWarnings("unchecked")
    public abstract static class Builder<T extends Builder<T, V>, V extends Menu> {
        protected EventWaiter waiter;
        protected Set<User> users = new HashSet<>();
        protected Set<Role> roles = new HashSet<>();
        protected long timeout = 1;
        protected TimeUnit unit = TimeUnit.MINUTES;

        public abstract V build();

        public final T setEventWaiter(EventWaiter w) {
            waiter = w;
            return (T) this;
        }

        public final T addUsers(User... u) {
            users.addAll(Arrays.asList(u));
            return (T) this;
        }

        public final T setUsers(User... u) {
            users.clear();
            users.addAll(Arrays.asList(u));
            return (T) this;
        }

        public final T addRoles(Role... r) {
            roles.addAll(Arrays.asList(r));
            return (T) this;
        }

        public final T setRoles(Role... r) {
            roles.clear();
            roles.addAll(Arrays.asList(r));
            return (T) this;
        }

        public final T setTimeout(long t, TimeUnit u) {
            timeout = t;
            unit = u;
            return (T) this;
        }
    }
}
