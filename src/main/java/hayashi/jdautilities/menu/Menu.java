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

package hayashi.jdautilities.menu;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    protected boolean isValidUser(User user, Guild guild) {
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
