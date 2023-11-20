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

package a9lim.jdautilities.command;

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
