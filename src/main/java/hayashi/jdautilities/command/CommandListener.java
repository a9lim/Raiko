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
package hayashi.jdautilities.command;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * An implementable "Listener" that can be added to a {@link CommandClient CommandClient}
 * and used to handle events relating to {@link Command Command}s.
 *
 * @author John Grosh (jagrosh)
 */
public interface CommandListener {
    default void onCommand(CommandEvent event, Command command) {}

    default void onCompletedCommand(CommandEvent event, Command command) {}

    default void onTerminatedCommand(CommandEvent event, Command command) {}

    default void onNonCommandMessage(MessageReceivedEvent event) {}

    default void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        // Default rethrow as a runtime exception.
        throw throwable instanceof RuntimeException r ? r : new RuntimeException(throwable);
    }
}
