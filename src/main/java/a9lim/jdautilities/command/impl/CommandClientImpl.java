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

package a9lim.jdautilities.command.impl;

import a9lim.jdautilities.command.*;
import a9lim.jdautilities.commons.utils.FixedSizeCache;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static a9lim.jdautilities.command.Command.COMPILE;

public class CommandClientImpl implements CommandClient, EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(CommandClient.class);
    private final OffsetDateTime start;
    private final Activity activity;
    private final OnlineStatus status;
    private final long[] coOwnerIds;
    private final long ownerId;
    private final String serverInvite, success, warning, error, botsKey, carbonKey, defaultprefix;
    private final List<String> prefixes;
    private final HashMap<String, Integer> uses;
    private final HashMap<String, Command> commandIndex;
    private final TreeSet<Command> commandSet;
    private final HashMap<String, OffsetDateTime> cooldowns;
    private final FixedSizeCache<Long, Set<Message>> linkMap;
    private final boolean shutdownAutomatically;
    private final ScheduledExecutorService executor;
    private final AnnotatedModuleCompiler compiler;
    private final GuildSettingsManager<?> manager;
    private final MediaType mediaType = MediaType.parse("application/json");

    private CommandListener listener;
    private int totalGuilds;
    private String helpword;

    public CommandClientImpl(long inownerId, long[] incoOwnerIds, List<String> inprefix, Activity inactivity, OnlineStatus instatus, String inserverInvite,
                             String insuccess, String inwarning, String inerror, String incarbonKey, String inbotsKey, List<Command> incommands,
                             boolean inshutdownAutomatically, ScheduledExecutorService inexecutor,
                             int linkedCacheSize, AnnotatedModuleCompiler incompiler, GuildSettingsManager<?> inmanager) {

        if (inownerId < 0)
            LOG.warn(String.format("The provided Owner ID (%s) was found unsafe! Make sure ID is a non-negative long!", inownerId));

        if (incoOwnerIds != null)
            for (long coOwnerId : incoOwnerIds)
                if (coOwnerId < 0)
                    LOG.warn(String.format("The provided CoOwner ID (%s) was found unsafe! Make sure ID is a non-negative long!", coOwnerId));

        start = OffsetDateTime.now();

        ownerId = inownerId;
        coOwnerIds = incoOwnerIds;
        prefixes = inprefix;
        defaultprefix = (prefixes == null || prefixes.isEmpty()? "@mention" : prefixes.getFirst());
        activity = inactivity;
        status = instatus;
        serverInvite = inserverInvite;
        success = insuccess == null ? "" : insuccess;
        warning = inwarning == null ? "" : inwarning;
        error = inerror == null ? "" : inerror;
        carbonKey = incarbonKey;
        botsKey = inbotsKey;
        commandIndex = new HashMap<>();
        commandSet = new TreeSet<>();
        cooldowns = new HashMap<>();
        uses = new HashMap<>();
        linkMap = linkedCacheSize > 0 ? new FixedSizeCache<>(linkedCacheSize) : null;
        shutdownAutomatically = inshutdownAutomatically;
        executor = inexecutor == null ? Executors.newSingleThreadScheduledExecutor() : inexecutor;
        compiler = incompiler;
        manager = inmanager;

        // Load commands
        incommands.forEach(this::addCommand);
    }

    public void setHelp(Command c) {
        if(helpword != null)
            removeCommand(helpword);
        helpword = c.getName();
        addCommand(c);
    }

    public String getHelpWord() {
        return helpword;
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
    public Set<Command> getCommands() {
        return commandSet;
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
            .forEach(cooldowns::remove);
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
        synchronized (commandIndex) {
            String name = command.getName().toLowerCase();
            //check for collision
            if (commandIndex.containsKey(name))
                throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \"" + name + "\"!");
            command.loadAliases();
            for (String alias : command.getAliases())
                if (commandIndex.containsKey(alias.toLowerCase()))
                    throw new IllegalArgumentException("Command added has a name or alias that has already been indexed: \"" + alias + "\"!");
            //add
            commandIndex.put(name, command);
            commandSet.add(command);
            for (String alias : command.getAliases())
                commandIndex.put(alias.toLowerCase(), command);

        }
    }

    @Override
    public void removeCommand(String name) {
        // huh
        synchronized (commandIndex) {
            name = name.toLowerCase();
            if (!commandIndex.containsKey(name))
                throw new IllegalArgumentException("Name provided is not indexed: \"" + name + "\"!");
            Command c = commandIndex.remove(name);
            commandSet.remove(c);
            for (String alias : c.getAliases())
                commandIndex.remove(alias.toLowerCase());
        }
    }

    @Override
    public void addAnnotatedModule(Object module) {
        compiler.compile(module).forEach(this::addCommand);
    }

    @Override
    public long getOwnerId() {
        return ownerId;
    }

    @Override
    public long[] getCoOwnerIds() {
        // Thought about using java.util.Arrays#setAll(T[], IntFunction<T>)
        // here, but as it turns out it's actually the same thing as this but
        // it throws an error if null. Go figure.
        return coOwnerIds;
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

    public List<String> getPrefixes() {
        return prefixes;
    }

    @Override
    public String getDefaultPrefix() {
        return defaultprefix;
    }

    @Override
    public int getTotalGuilds() {
        return totalGuilds;
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
    public <M extends GuildSettingsManager<?>> M getSettingsManager() {
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

        else if (usesLinkedDeletion() && event instanceof MessageDeleteEvent e)
            onMessageDelete(e);

        else if (event instanceof GuildLeaveEvent ||
                (event instanceof GuildJoinEvent e &&
                        e.getGuild().getSelfMember().getTimeJoined().plusMinutes(10).isAfter(OffsetDateTime.now())))
                sendStats(event.getJDA());
        else if (event instanceof ReadyEvent e)
            onReady(e);
        else if (shutdownAutomatically && event instanceof ShutdownEvent) {
            shutdown();
        }
    }

    private void onReady(ReadyEvent event) {
        if (!event.getJDA().getSelfUser().isBot()) {
            LOG.error("JDA-Utilities does not support CLIENT accounts.");
            event.getJDA().shutdown();
            return;
        }
        // todo: look at this
        event.getJDA().getPresence().setPresence(status,
            activity == null ? null : "default".equals(activity.getName()) ? Activity.playing("Type " + defaultprefix + helpword) : activity);

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

        // Check for @mention
        if (event.getMessage().getMentions().isMentioned(event.getJDA().getSelfUser()))
            parts = splitOnPrefixLength(rawContent, rawContent.indexOf('>') + 1);
        // Check for prefix
        else if(prefixes != null)
            for (String s: prefixes)
                if (rawContent.toLowerCase().startsWith(s.toLowerCase()))
                    parts = splitOnPrefixLength(rawContent, s.length());
        // Check for guild specific prefixes
        if(parts == null && event.isFromType(ChannelType.TEXT)) {
            Collection<String> prefixes = provideSettings(event.getGuild()).getPrefixes();
            if (prefixes != null)
                for (String prefix : prefixes)
                    if (rawContent.toLowerCase().startsWith(prefix.toLowerCase())) {
                        parts = splitOnPrefixLength(rawContent, prefix.length());
                        break;
                    }
        }

        //starts with valid prefix
        if (parts != null) {
            if (event.isFromType(ChannelType.PRIVATE) || event.getChannel().canTalk()) {
                final Command command; // this will be null if it's not a command
                // this may be cleanable
                synchronized (commandIndex) {
                    command = commandIndex.get(parts[0].toLowerCase());
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

    // this is pretty silly, remove???

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

        if (botsKey == null) {
            totalGuilds = (int) (jda.getShardManager() != null ? jda.getShardManager().getGuildCache().size() : jda.getGuildCache().size());
            return;
        }

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
            }});
    }

    private void onMessageDelete(MessageDeleteEvent event) {
        // We don't need to cover whether or not this client usesLinkedDeletion() because
        // that is checked in onEvent(Event) before this is even called.
        synchronized (linkMap) {
            if (linkMap.contains(event.getMessageIdLong())) {
                Set<Message> messages = linkMap.get(event.getMessageIdLong());
                if (messages.size() > 1 && event.getGuild().getSelfMember()
                    .hasPermission(event.getChannel().asTextChannel(), Permission.MESSAGE_MANAGE))
                    event.getChannel().asTextChannel().deleteMessages(messages).queue(unused -> {}, ignored -> {});
                else if (!messages.isEmpty())
                    messages.forEach(m -> m.delete().queue(unused -> {}, ignored -> {}));
            }
        }
    }

    private GuildSettingsProvider provideSettings(Guild guild) {
        return getSettingsFor(guild) instanceof GuildSettingsProvider e ? e : null;
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
