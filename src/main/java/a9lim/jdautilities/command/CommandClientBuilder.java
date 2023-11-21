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

import a9lim.jdautilities.command.impl.AnnotatedModuleCompilerImpl;
import a9lim.jdautilities.command.impl.CommandClientImpl;
import a9lim.raiko.commands.general.HelpCmd;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class CommandClientBuilder {
    private Activity activity = Activity.playing("default");
    private OnlineStatus status = OnlineStatus.ONLINE;
    private String ownerId, serverInvite, success, warning, error, carbonKey, botsKey;
    private String[] coOwnerIds;
    private List<String> prefixes;
    private final LinkedList<Command> commands = new LinkedList<>();
    private CommandListener listener;
    private boolean shutdownAutomatically = true;
    private ScheduledExecutorService executor;
    private int linkedCacheSize;
    private AnnotatedModuleCompiler compiler = new AnnotatedModuleCompilerImpl();
    private GuildSettingsManager manager;

    public CommandClient build() {
        CommandClient client = new CommandClientImpl(ownerId, coOwnerIds, prefixes, activity, status, serverInvite,
            success, warning, error, carbonKey, botsKey, commands,
            shutdownAutomatically, executor, linkedCacheSize, compiler, manager);
        client.setHelp(new HelpCmd(client));
        if (listener != null)
            client.setListener(listener);
        return client;
    }

    public CommandClientBuilder setOwnerId(String o) {
        ownerId = o;
        return this;
    }

    public CommandClientBuilder setCoOwnerIds(String... co) {
        coOwnerIds = co;
        return this;
    }

    public CommandClientBuilder setPrefixes(List<String> p) {
        prefixes = p;
        return this;
    }

    public CommandClientBuilder addPrefix(String prefix) {
        prefixes.add(prefix);
        return this;
    }

    public CommandClientBuilder setServerInvite(String s) {
        serverInvite = s;
        return this;
    }

    public CommandClientBuilder setEmojis(String succ, String war, String err) {
        success = succ;
        warning = war;
        error = err;
        return this;
    }

    public CommandClientBuilder setActivity(Activity a) {
        activity = a;
        return this;
    }

    public CommandClientBuilder useDefaultGame() {
        activity = Activity.playing("default");
        return this;
    }

    public CommandClientBuilder setStatus(OnlineStatus stat) {
        status = stat;
        return this;
    }

    public CommandClientBuilder addCommand(Command command) {
        commands.add(command);
        return this;
    }

    public CommandClientBuilder addCommands(Command... commands) {
        for (Command command : commands)
            addCommand(command);
        return this;
    }

    public CommandClientBuilder addAnnotatedModule(Object module) {
        commands.addAll(compiler.compile(module));
        return this;
    }

    public CommandClientBuilder addAnnotatedModules(Object... modules) {
        for (Object command : modules)
            addAnnotatedModule(command);
        return this;
    }

    public CommandClientBuilder setAnnotatedCompiler(AnnotatedModuleCompiler comp) {
        compiler = comp;
        return this;
    }

    public CommandClientBuilder setCarbonitexKey(String key) {
        carbonKey = key;
        return this;
    }

    public CommandClientBuilder setDiscordBotsKey(String key) {
        botsKey = key;
        return this;
    }

    public CommandClientBuilder setListener(CommandListener list) {
        listener = list;
        return this;
    }

    public CommandClientBuilder setScheduleExecutor(ScheduledExecutorService service) {
        executor = service;
        return this;
    }

    public CommandClientBuilder setShutdownAutomatically(boolean b) {
        shutdownAutomatically = b;
        return this;
    }

    public CommandClientBuilder setLinkedCacheSize(int size) {
        linkedCacheSize = size;
        return this;
    }

    public CommandClientBuilder setGuildSettingsManager(GuildSettingsManager man) {
        manager = man;
        return this;
    }
}
