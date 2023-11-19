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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

import hayashi.jdautilities.command.impl.AnnotatedModuleCompilerImpl;
import hayashi.jdautilities.command.impl.CommandClientImpl;

import java.util.concurrent.ScheduledExecutorService;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class CommandClientBuilder {
    private Activity activity = Activity.playing("default");
    private OnlineStatus status = OnlineStatus.ONLINE;
    private String ownerId, prefix, altprefix, serverInvite, success, warning, error, carbonKey, helpWord, botsKey;
    private String[] coOwnerIds;
    private final LinkedList<Command> commands = new LinkedList<>();
    private CommandListener listener;
    private boolean useHelp = true;
    private boolean shutdownAutomatically = true;
    private Consumer<CommandEvent> helpConsumer;
    private ScheduledExecutorService executor;
    private int linkedCacheSize;
    private AnnotatedModuleCompiler compiler = new AnnotatedModuleCompilerImpl();
    private GuildSettingsManager manager;

    public CommandClient build() {
        CommandClient client = new CommandClientImpl(ownerId, coOwnerIds, prefix, altprefix, activity, status, serverInvite,
            success, warning, error, carbonKey, botsKey, new ArrayList<>(commands), useHelp,
            shutdownAutomatically, helpConsumer, helpWord, executor, linkedCacheSize, compiler, manager);
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

    public CommandClientBuilder setPrefix(String p) {
        prefix = p;
        return this;
    }

    public CommandClientBuilder setAlternativePrefix(String prefix) {
        altprefix = prefix;
        return this;
    }

    public CommandClientBuilder useHelpBuilder(boolean b) {
        useHelp = b;
        return this;
    }

    public CommandClientBuilder setHelpConsumer(Consumer<CommandEvent> consumer) {
        helpConsumer = consumer;
        return this;
    }

    public CommandClientBuilder setHelpWord(String h) {
        helpWord = h;
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
