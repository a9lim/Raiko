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

    public CommandClientBuilder setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public CommandClientBuilder setCoOwnerIds(String... coOwnerIds) {
        this.coOwnerIds = coOwnerIds;
        return this;
    }

    public CommandClientBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public CommandClientBuilder setAlternativePrefix(String prefix) {
        this.altprefix = prefix;
        return this;
    }

    public CommandClientBuilder useHelpBuilder(boolean useHelp) {
        this.useHelp = useHelp;
        return this;
    }

    public CommandClientBuilder setHelpConsumer(Consumer<CommandEvent> helpConsumer) {
        this.helpConsumer = helpConsumer;
        return this;
    }

    public CommandClientBuilder setHelpWord(String helpWord) {
        this.helpWord = helpWord;
        return this;
    }

    public CommandClientBuilder setServerInvite(String serverInvite) {
        this.serverInvite = serverInvite;
        return this;
    }

    public CommandClientBuilder setEmojis(String success, String warning, String error) {
        this.success = success;
        this.warning = warning;
        this.error = error;
        return this;
    }

    public CommandClientBuilder setActivity(Activity activity) {
        this.activity = activity;
        return this;
    }

    public CommandClientBuilder useDefaultGame() {
        this.activity = Activity.playing("default");
        return this;
    }

    public CommandClientBuilder setStatus(OnlineStatus status) {
        this.status = status;
        return this;
    }

    public CommandClientBuilder addCommand(Command command) {
        commands.add(command);
        return this;
    }

    public CommandClientBuilder addCommands(Command... commands) {
        for (Command command : commands)
            this.addCommand(command);
        return this;
    }

    public CommandClientBuilder addAnnotatedModule(Object module) {
        this.commands.addAll(compiler.compile(module));
        return this;
    }

    public CommandClientBuilder addAnnotatedModules(Object... modules) {
        for (Object command : modules)
            addAnnotatedModule(command);
        return this;
    }

    public CommandClientBuilder setAnnotatedCompiler(AnnotatedModuleCompiler compiler) {
        this.compiler = compiler;
        return this;
    }

    public CommandClientBuilder setCarbonitexKey(String key) {
        this.carbonKey = key;
        return this;
    }

    public CommandClientBuilder setDiscordBotsKey(String key) {
        this.botsKey = key;
        return this;
    }

    @Deprecated
    public CommandClientBuilder setDiscordBotListKey(String key) {
        // this.botsOrgKey = key;
        return this;
    }

    public CommandClientBuilder setListener(CommandListener listener) {
        this.listener = listener;
        return this;
    }

    public CommandClientBuilder setScheduleExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public CommandClientBuilder setShutdownAutomatically(boolean shutdownAutomatically) {
        this.shutdownAutomatically = shutdownAutomatically;
        return this;
    }

    public CommandClientBuilder setLinkedCacheSize(int linkedCacheSize) {
        this.linkedCacheSize = linkedCacheSize;
        return this;
    }

    public CommandClientBuilder setGuildSettingsManager(GuildSettingsManager manager) {
        this.manager = manager;
        return this;
    }
}
