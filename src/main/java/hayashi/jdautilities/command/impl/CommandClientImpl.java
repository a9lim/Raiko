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
package hayashi.jdautilities.command.impl;

import hayashi.jdautilities.command.*;
import hayashi.jdautilities.command.Command.Category;
import hayashi.jdautilities.commons.utils.FixedSizeCache;
import hayashi.jdautilities.commons.utils.SafeIdUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.*;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.utils.Checks;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.dv8tion.jda.api.entities.channel.*;
import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import static hayashi.jdautilities.command.Command.COMPILE;

public class CommandClientImpl implements CommandClient, EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(CommandClient.class);
    private static final String DEFAULT_PREFIX = "@mention";

    private final OffsetDateTime start;
    private final Activity activity;
    private final OnlineStatus status;
    private final String[] coOwnerIds;
    private final String ownerId, prefix, altprefix, serverInvite, success, warning, error, botsKey, carbonKey, helpWord;
    private final HashMap<String, Integer> commandIndex, uses;
    private final ArrayList<Command> commands;
    private final HashMap<String, OffsetDateTime> cooldowns;
    private final FixedSizeCache<Long, Set<Message>> linkMap;
    private final boolean useHelp, shutdownAutomatically;
    private final Consumer<CommandEvent> helpConsumer;
    private final ScheduledExecutorService executor;
    private final AnnotatedModuleCompiler compiler;
    private final GuildSettingsManager manager;
    private final MediaType mediaType = MediaType.parse("application/json");

    private String textPrefix;
    private CommandListener listener;
    private int totalGuilds;

    public CommandClientImpl(String inownerId, String[] incoOwnerIds, String inprefix, String inaltprefix, Activity inactivity, OnlineStatus instatus, String inserverInvite,
                             String insuccess, String inwarning, String inerror, String incarbonKey, String inbotsKey, ArrayList<Command> incommands,
                             boolean inuseHelp, boolean inshutdownAutomatically, Consumer<CommandEvent> inhelpConsumer, String inhelpWord, ScheduledExecutorService inexecutor,
                             int linkedCacheSize, AnnotatedModuleCompiler incompiler, GuildSettingsManager inmanager) {
        Checks.check(inownerId != null, "Owner ID was set null or not set! Please provide an User ID to register as the owner!");

        if (!SafeIdUtil.checkId(inownerId))
            LOG.warn(String.format("The provided Owner ID (%s) was found unsafe! Make sure ID is a non-negative long!", inownerId));

        if (incoOwnerIds != null) {
            for (String coOwnerId : incoOwnerIds) {
                if (!SafeIdUtil.checkId(coOwnerId))
                    LOG.warn(String.format("The provided CoOwner ID (%s) was found unsafe! Make sure ID is a non-negative long!", coOwnerId));
            }
        }

        start = OffsetDateTime.now();

        ownerId = inownerId;
        coOwnerIds = incoOwnerIds;
        prefix = inprefix == null || inprefix.isEmpty() ? DEFAULT_PREFIX : inprefix;
        altprefix = inaltprefix == null || inaltprefix.isEmpty() ? null : inaltprefix;
        textPrefix = inprefix;
        activity = inactivity;
        status = instatus;
        serverInvite = inserverInvite;
        success = insuccess == null ? "" : insuccess;
        warning = inwarning == null ? "" : inwarning;
        error = inerror == null ? "" : inerror;
        carbonKey = incarbonKey;
        botsKey = inbotsKey;
        commandIndex = new HashMap<>();
        commands = new ArrayList<>();
        cooldowns = new HashMap<>();
        uses = new HashMap<>();
        linkMap = linkedCacheSize > 0 ? new FixedSizeCache<>(linkedCacheSize) : null;
        useHelp = inuseHelp;
        shutdownAutomatically = inshutdownAutomatically;
        helpWord = inhelpWord == null ? "help" : inhelpWord;
        executor = inexecutor == null ? Executors.newSingleThreadScheduledExecutor() : inexecutor;
        compiler = incompiler;
        manager = inmanager;
        helpConsumer = inhelpConsumer == null ? (event) -> {
            StringBuilder builder = new StringBuilder("**" + event.getSelfUser().getName() + "** commands:\n");
            Category category = null;
            for (Command command : incommands) {
                if (!command.isHidden() && (!command.isOwnerCommand() || event.isOwner())) {
                    if (!Objects.equals(category, command.getCategory())) {
                        category = command.getCategory();
                        builder.append("\n\n  __").append(category == null ? "No Category" : category.getName()).append("__:\n");
                    }
                    builder.append("\n`").append(textPrefix).append(inprefix == null ? " " : "").append(command.getName())
                        .append(command.getArguments() == null ? "`" : " " + command.getArguments() + "`")
                        .append(" - ").append(command.getHelp());
                }
            }
            User owner = event.getJDA().getUserById(inownerId);
            if (owner != null) {
                builder.append("\n\nFor additional help, contact **").append(owner.getName()).append("**#").append(owner.getDiscriminator());
                if (inserverInvite != null)
                    builder.append(" or join ").append(inserverInvite);
            }
            event.replyInDm(builder.toString(), unused -> {
                if (event.isFromType(ChannelType.TEXT))
                    event.reactSuccess();
            }, t -> event.replyWarning("Help cannot be sent because you are blocking Direct Messages."));
        } : inhelpConsumer;

        // Load commands
        for (Command command : incommands) {
            addCommand(command);
        }
    }

    @Override
    public void setListener(CommandListener list) {
        listener = list;
    }

    @Override
    public CommandListener getListener() {
        return listener;
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public OffsetDateTime getStartTime() {
        return start;
    }

    @Override
    public OffsetDateTime getCooldown(String name) {
        return cooldowns.get(name);
    }

    @Override
    public int getRemainingCooldown(String name) {
        if (!cooldowns.containsKey(name))
            return 0;
        int time = (int) OffsetDateTime.now().until(cooldowns.get(name), ChronoUnit.SECONDS);
        if (time <= 0) {
            cooldowns.remove(name);
            return 0;
        }
        return time;
    }

    @Override
    public void applyCooldown(String name, int seconds) {
        cooldowns.put(name, OffsetDateTime.now().plusSeconds(seconds));
    }

    @Override
    public void cleanCooldowns() {
        cooldowns.keySet().stream().filter((str) -> (cooldowns.get(str).isBefore(OffsetDateTime.now())))
            .toList().forEach(cooldowns::remove);
    }

    @Override
    public int getCommandUses(Command command) {
        return getCommandUses(command.getName());
    }

    @Override
    public int getCommandUses(String name) {
        return uses.getOrDefault(name, 0);
    }

    @Override
    public void addCommand(Command command) {
        addCommand(command, commands.size());
    }

    @Override
    public void addCommand(Command command, int index) {
        if (index > commands.size() || index < 0)
            throw new ArrayIndexOutOfBoundsException("Index specified is invalid: [" + index + "/" + commands.size() + "]");
        synchronized (commandIndex) {
            String name = command.getName().toLowerCase();
            //check for collision
            if (commandIndex.containsKey(name))
                throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \"" + name + "\"!");
            for (String alias : command.getAliases()) {
                if (commandIndex.containsKey(alias.toLowerCase()))
                    throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \"" + alias + "\"!");
            }
            //shift if not append
            if (index < commands.size())
                commandIndex.entrySet().stream().filter(entry -> entry.getValue() >= index).toList()
                    .forEach(entry -> commandIndex.put(entry.getKey(), entry.getValue() + 1));
            //add
            commandIndex.put(name, index);
            for (String alias : command.getAliases())
                commandIndex.put(alias.toLowerCase(), index);
        }
        commands.add(index, command);
    }

    @Override
    public void removeCommand(String name) {
        synchronized (commandIndex) {
            if (!commandIndex.containsKey(name.toLowerCase()))
                throw new IllegalArgumentException("Name provided is not indexed: \"" + name + "\"!");
            int targetIndex = commandIndex.remove(name.toLowerCase());
            for (String alias : commands.remove(targetIndex).getAliases()) {
                commandIndex.remove(alias.toLowerCase());
            }
            commandIndex.entrySet().stream().filter(entry -> entry.getValue() > targetIndex).toList()
                .forEach(entry -> commandIndex.put(entry.getKey(), entry.getValue() - 1));
        }
    }

    @Override
    public void addAnnotatedModule(Object module) {
        compiler.compile(module).forEach(this::addCommand);
    }

    @Override
    public void addAnnotatedModule(Object module, Function<Command, Integer> mapFunction) {
        compiler.compile(module).forEach(command -> addCommand(command, mapFunction.apply(command)));
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public long getOwnerIdLong() {
        return Long.parseLong(ownerId);
    }

    @Override
    public String[] getCoOwnerIds() {
        return coOwnerIds;
    }

    @Override
    public long[] getCoOwnerIdsLong() {
        // Thought about using java.util.Arrays#setAll(T[], IntFunction<T>)
        // here, but as it turns out it's actually the same thing as this but
        // it throws an error if null. Go figure.
        if (coOwnerIds == null)
            return null;
        long[] ids = new long[coOwnerIds.length];
        for (int i = 0; i < ids.length; i++)
            ids[i] = Long.parseLong(coOwnerIds[i]);
        return ids;
    }

    @Override
    public String getSuccess() {
        return success;
    }

    @Override
    public String getWarning() {
        return warning;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public ScheduledExecutorService getScheduleExecutor() {
        return executor;
    }

    @Override
    public String getServerInvite() {
        return serverInvite;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getAltPrefix() {
        return altprefix;
    }

    @Override
    public String getTextualPrefix() {
        return textPrefix;
    }

    @Override
    public int getTotalGuilds() {
        return totalGuilds;
    }

    @Override
    public String getHelpWord() {
        return helpWord;
    }

    @Override
    public boolean usesLinkedDeletion() {
        return linkMap != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> S getSettingsFor(Guild guild) {
        return manager != null ? (S) manager.getSettings(guild) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends GuildSettingsManager> M getSettingsManager() {
        return (M) manager;
    }

    @Override
    public void shutdown() {
        GuildSettingsManager<?> manager = getSettingsManager();
        if (manager != null)
            manager.shutdown();
        executor.shutdown();
    }

    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof MessageReceivedEvent e)
            onMessageReceived(e);

        else if (event instanceof MessageDeleteEvent e && usesLinkedDeletion())
            onMessageDelete(e);

        else if (event instanceof GuildJoinEvent e) {
            if (e.getGuild().getSelfMember().getTimeJoined()
                .plusMinutes(10).isAfter(OffsetDateTime.now()))
                sendStats(event.getJDA());
        } else if (event instanceof GuildLeaveEvent)
            sendStats(event.getJDA());
        else if (event instanceof ReadyEvent e)
            onReady(e);
        else if (event instanceof ShutdownEvent && shutdownAutomatically) {
            shutdown();
        }
    }

    private void onReady(ReadyEvent event) {
        if (!event.getJDA().getSelfUser().isBot()) {
            LOG.error("JDA-Utilities does not support CLIENT accounts.");
            event.getJDA().shutdown();
            return;
        }
        textPrefix = prefix.equals(DEFAULT_PREFIX) ? "@" + event.getJDA().getSelfUser().getName() + " " : prefix;
        event.getJDA().getPresence().setPresence(status == null ? OnlineStatus.ONLINE : status,
            activity == null ? null : "default".equals(activity.getName()) ? Activity.playing("Type " + textPrefix + helpWord) : activity);

        // Start SettingsManager if necessary
        GuildSettingsManager<?> manager = getSettingsManager();
        if (manager != null)
            manager.init();

        sendStats(event.getJDA());
    }

    private void onMessageReceived(MessageReceivedEvent event) {
        // Return if it's a bot
        if (event.getAuthor().isBot())
            return;

        String[] parts = null;
        String rawContent = event.getMessage().getContentRaw();


        // Check for prefix or alternate prefix (@mention cases)
        if ((prefix.equals(DEFAULT_PREFIX) || (DEFAULT_PREFIX.equals(altprefix))) &&
            (rawContent.startsWith("<@" + event.getJDA().getSelfUser().getId() + ">") ||
                rawContent.startsWith("<@!" + event.getJDA().getSelfUser().getId() + ">")))
                parts = splitOnPrefixLength(rawContent, rawContent.indexOf('>') + 1);
        // Check for prefix
        if (parts == null && rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
            parts = splitOnPrefixLength(rawContent, prefix.length());
        // Check for alternate prefix
        if (parts == null && altprefix != null && rawContent.toLowerCase().startsWith(altprefix.toLowerCase()))
            parts = splitOnPrefixLength(rawContent, altprefix.length());
        // Check for guild specific prefixes
        GuildSettingsProvider settings = event.isFromType(ChannelType.TEXT) ? provideSettings(event.getGuild()) : null;
        if (parts == null && settings != null) {
            Collection<String> prefixes = settings.getPrefixes();
            if (prefixes != null) {
                for (String prefix : prefixes) {
                    if (parts == null && rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
                        parts = splitOnPrefixLength(rawContent, prefix.length());
                }
            }
        }

        //starts with valid prefix
        if (parts != null) {
            if (useHelp && parts[0].equalsIgnoreCase(helpWord)) {
                CommandEvent cevent = new CommandEvent(event, parts[1] == null ? "" : parts[1], this);
                if (listener != null)
                    listener.onCommand(cevent, null);
                helpConsumer.accept(cevent); // Fire help consumer
                if (listener != null)
                    listener.onCompletedCommand(cevent, null);
                return; // Help Consumer is done
            }
            if (event.isFromType(ChannelType.PRIVATE) || event.getChannel().canTalk()) {
                final Command command; // this will be null if it's not a command
                // this may be cleanable
                synchronized (commandIndex) {
                    int i = commandIndex.getOrDefault(parts[0].toLowerCase(), -1);
                    command = i == -1 ? null : commands.get(i);
                }
                if (command != null) {
                    CommandEvent cevent = new CommandEvent(event, parts[1] == null ? "" : parts[1], this);
                    if (listener != null)
                        listener.onCommand(cevent, command);
                    uses.put(command.getName(), uses.getOrDefault(command.getName(), 0) + 1);
                    command.run(cevent);
                    return; // Command is done
                }
            }
        }

        if (listener != null)
            listener.onNonCommandMessage(event);
    }

    private void sendStats(JDA jda) {
        OkHttpClient client = jda.getHttpClient();

        if (carbonKey != null) {
            FormBody.Builder bodyBuilder = new FormBody.Builder()
                .add("key", carbonKey)
                .add("servercount", Integer.toString(jda.getGuilds().size()));

            if (jda.getShardInfo() != null) {
                bodyBuilder.add("shard_id", Integer.toString(jda.getShardInfo().getShardId()))
                    .add("shard_count", Integer.toString(jda.getShardInfo().getShardTotal()));
            }

            client.newCall(new Request.Builder()
                    .post(bodyBuilder.build())
                    .url("https://www.carbonitex.net/discord/data/botdata.php")
                    .build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    LOG.info("Successfully send information to carbonitex.net");
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    LOG.error("Failed to send information to carbonitex.net ", e);
                }
            });
        }

        if (botsKey != null) {
            JSONObject body = new JSONObject().put("guildCount", jda.getGuilds().size());
            if (jda.getShardInfo() != null) {
                body.put("shardId", jda.getShardInfo().getShardId())
                    .put("shardCount", jda.getShardInfo().getShardTotal());
            }

            client.newCall(new Request.Builder()
                    .post(RequestBody.create(body.toString(),mediaType))
                    .url("https://discord.bots.gg/api/v1/bots/" + jda.getSelfUser().getId() + "/stats")
                    .header("Authorization", botsKey)
                    .header("Content-Type", "application/json")
                    .build()).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        LOG.info("Successfully sent information to discord.bots.gg");
                        try (Reader reader = response.body().charStream()) {
                            totalGuilds = new JSONObject(new JSONTokener(reader)).getInt("guildCount");
                        } catch (Exception ex) {
                            LOG.error("Failed to retrieve bot shard information from discord.bots.gg ", ex);
                        }
                    } else
                        LOG.error("Failed to send information to discord.bots.gg: " + response.body().string());
                    response.close();
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    LOG.error("Failed to send information to discord.bots.gg ", e);
                }
            });
        } else {
            totalGuilds = (int) (jda.getShardManager() != null ? jda.getShardManager().getGuildCache().size() : jda.getGuildCache().size());
        }
    }

    private void onMessageDelete(MessageDeleteEvent event) {
        // We don't need to cover whether or not this client usesLinkedDeletion() because
        // that is checked in onEvent(Event) before this is even called.
        synchronized (linkMap) {
            if (linkMap.contains(event.getMessageIdLong())) {
                Set<Message> messages = linkMap.get(event.getMessageIdLong());
                if (messages.size() > 1 && event.getGuild().getSelfMember()
                    .hasPermission(event.getChannel().asGuildMessageChannel(), Permission.MESSAGE_MANAGE))
                    event.getChannel().asGuildMessageChannel().deleteMessages(messages).queue(unused -> {
                    }, ignored -> {
                    });
                else if (!messages.isEmpty())
                    messages.forEach(m -> m.delete().queue(unused -> {
                    }, ignored -> {
                    }));
            }
        }
    }

    private GuildSettingsProvider provideSettings(Guild guild) {
        Object settings = getSettingsFor(guild);
        return settings instanceof GuildSettingsProvider e ? e : null;
    }

    private static String[] splitOnPrefixLength(String rawContent, int length) {
        return Arrays.copyOf(COMPILE.split(rawContent.substring(length).trim(), 2), 2);
    }

    public void linkIds(long callId, Message message) {
        if (usesLinkedDeletion()) {
            synchronized (linkMap) {
                Set<Message> stored = linkMap.get(callId);
                if (stored != null)
                    stored.add(message);
                else {
                    stored = new HashSet<>();
                    stored.add(message);
                    linkMap.add(callId, stored);
                }
            }
        }
    }
}
