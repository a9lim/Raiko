// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
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

package a9lim.jdautilities.commons.waiter;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.internal.utils.Checks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventWaiter implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventWaiter.class);
    private final HashMap<Class<?>, Set<WaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadpool;
    private final boolean shutdownAutomatically;

    public EventWaiter() {
        this(Executors.newSingleThreadScheduledExecutor(), true);
    }

    public EventWaiter(ScheduledExecutorService tp, boolean b) {
        Checks.notNull(tp, "ScheduledExecutorService");
        Checks.check(!tp.isShutdown(), "Cannot construct EventWaiter with a closed ScheduledExecutorService!");

        waitingEvents = new HashMap<>();
        threadpool = tp;

        // "Why is there no default constructor?"
        //
        // When a developer uses this constructor we want them to be aware that this
        // is putting the task on them to shut down the threadpool if they set this to false,
        // or to avoid errors being thrown when ShutdownEvent is fired if they set it true.
        //
        // It is YOUR fault if you have a rogue threadpool that doesn't shut down if you
        // forget to dispose of it and set this false, or that certain tasks may fail
        // if you use this executor for other things and set this true.
        //
        // NOT MINE
        // bro said you problem
        shutdownAutomatically = b;
    }

    public boolean isShutdown() {
        return threadpool.isShutdown();
    }

    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action) {
        waitForEvent(classType, condition, action, -1, null, null);
    }

    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action,
                                               long timeout, TimeUnit unit, Runnable timeoutAction) {
        Checks.check(!isShutdown(), "Attempted to register a WaitingEvent while the EventWaiter's threadpool was already shut down!");
        Checks.notNull(classType, "The provided class type");
        Checks.notNull(condition, "The provided condition predicate");
        Checks.notNull(action, "The provided action consumer");

        WaitingEvent we = new WaitingEvent<>(condition, action);
        Set<WaitingEvent> set = waitingEvents.computeIfAbsent(classType, c -> new HashSet<>());
        set.add(we);

        if (timeout > 0 && unit != null) {
            threadpool.schedule(() -> {
                try {
                    if (set.remove(we) && timeoutAction != null)
                        timeoutAction.run();
                } catch (Exception ex) {
                    LOG.error("Failed to run timeoutAction", ex);
                }
            }, timeout, unit);
        }
    }

    @Override
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public final void onEvent(GenericEvent event) {
        Class c = event.getClass();

        // Runs at least once for the fired Event, at most
        // once for each superclass (excluding Object) because
        // Class#getSuperclass() returns null when the superclass
        // is primitive, void, or (in this case) Object.
        while (c != null) {
            if (waitingEvents.containsKey(c)) {
                Set<WaitingEvent> set = waitingEvents.get(c);

                // WaitingEvent#attempt invocations that return true have passed their condition tests
                // and executed the action. We filter the ones that return false out of the toRemove and
                // remove them all from the set.
                set.removeAll(set.stream().filter(i -> i.attempt(event)).collect(Collectors.toSet()));
            }
            if (event instanceof ShutdownEvent && shutdownAutomatically)
                threadpool.shutdown();
            c = c.getSuperclass();
        }
    }

    public void shutdown() {
        if (shutdownAutomatically)
            throw new UnsupportedOperationException("Shutting down EventWaiters that are set to automatically close is unsupported!");

        threadpool.shutdown();
    }

    private record WaitingEvent<T extends GenericEvent>(Predicate<T> condition, Consumer<T> action) {

        boolean attempt(T event) {
                if (!condition.test(event))
                    return false;
                action.accept(event);
                return true;
            }
        }
}
