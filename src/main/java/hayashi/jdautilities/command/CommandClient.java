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

import hayashi.jdautilities.command.annotation.JDACommand;
import hayashi.jdautilities.command.impl.CommandClientImpl;
import net.dv8tion.jda.api.entities.Guild;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

public interface CommandClient {

    String getPrefix();

    String getAltPrefix();

    String getTextualPrefix();

    void addCommand(Command command);

    void addCommand(Command command, int index);

    void removeCommand(String name);

    void addAnnotatedModule(Object module);

    void addAnnotatedModule(Object module, Function<Command, Integer> mapFunction);

    void setListener(CommandListener listener);

    CommandListener getListener();

    List<Command> getCommands();

    OffsetDateTime getStartTime();

    OffsetDateTime getCooldown(String name);

    int getRemainingCooldown(String name);

    void applyCooldown(String name, int seconds);

    void cleanCooldowns();

    int getCommandUses(Command command);

    int getCommandUses(String name);

    String getOwnerId();

    long getOwnerIdLong();

    String[] getCoOwnerIds();

    long[] getCoOwnerIdsLong();

    String getSuccess();

    String getWarning();

    String getError();

    ScheduledExecutorService getScheduleExecutor();

    String getServerInvite();

    int getTotalGuilds();

    String getHelpWord();

    boolean usesLinkedDeletion();

    <S> S getSettingsFor(Guild guild);

    <M extends GuildSettingsManager> M getSettingsManager();

    void shutdown();
}
