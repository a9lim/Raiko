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

import a9lim.raiko.BotConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public abstract class Command implements Comparable<Command>{
    public static final Pattern COMPILE = Pattern.compile("\\s+");

    protected static BotConfig aliasSource;

    protected String name = "null";

    protected String help = "no help available";

    protected Category category;

    protected String arguments, requiredRole;

    protected boolean guildOnly = true;

    protected boolean ownerCommand, hidden;

    protected int cooldown;

    protected Permission[] userPermissions = new Permission[0];

    protected Permission[] botPermissions = new Permission[0];

    protected String[] aliases;

    protected Command[] children = new Command[0];

    protected boolean usesTopicTags = true;

    protected CooldownScope cooldownScope = CooldownScope.USER;

    private final static String BOT_PERM = "%s I need the %s permission in this %s!";
    private final static String USER_PERM = "%s You must have the %s permission in this %s to use that!";

    protected abstract void execute(CommandEvent event);

    public final void run(CommandEvent event) {
        // child check
        if (!event.getArgs().isEmpty()) {
            String[] parts = Arrays.copyOf(COMPILE.split(event.getArgs(), 2), 2);
            for (Command cmd : children) {
                if (cmd.isCommandFor(parts[0])) {
                    event.setArgs(parts[1] == null ? "" : parts[1]);
                    cmd.run(event);
                    return;
                }
            }
        }

        // owner check
        if (ownerCommand && !(event.isOwner())) {
            terminate(event, null);
            return;
        }

        // category check
        // fix later
        if (category != null && !category.test(event)) {
            terminate(event, null);
            return;
        }

        // is allowed check
        if (event.isFromType(ChannelType.TEXT) && !isAllowed(event.getTextChannel())) {
            terminate(event, "That command cannot be used in this channel!");
            return;
        }

        // required role check
        if (requiredRole != null && (!event.isFromType(ChannelType.TEXT) || event.getMember().getRoles().stream().noneMatch(r -> r.getName().equalsIgnoreCase(requiredRole)))) {
            terminate(event, event.getClient().getError() + " You must have a role called `" + requiredRole + "` to use that!");
            return;
        }

        // availability check
        if (event.getChannelType() == ChannelType.TEXT) {
            // bot perms
            for (Permission p : botPermissions) {
                if (p.isChannel()) {
                    if (p.name().startsWith("VOICE")) {
                        GuildVoiceState gvc = event.getMember().getVoiceState();
                        VoiceChannel vc = gvc == null ? null : gvc.getChannel().asVoiceChannel();
                        if (vc == null) {
                            terminate(event, event.getClient().getError() + " You must be in a voice channel to use that!");
                            return;
                        } else if (!event.getSelfMember().hasPermission(vc, p)) {
                            terminate(event, String.format(BOT_PERM, event.getClient().getError(), p.getName(), "Voice Channel"));
                            return;
                        }
                    } else if (!event.getSelfMember().hasPermission(event.getTextChannel(), p)) {
                        terminate(event, String.format(BOT_PERM, event.getClient().getError(), p.getName(), "Channel"));
                        return;
                    }
                } else if (!event.getSelfMember().hasPermission(p)) {
                    terminate(event, String.format(BOT_PERM, event.getClient().getError(), p.getName(), "Guild"));
                    return;
                }
            }

            //user perms
            for (Permission p : userPermissions) {
                if (p.isChannel()) {
                    if (!event.getMember().hasPermission(event.getTextChannel(), p)) {
                        terminate(event, String.format(USER_PERM, event.getClient().getError(), p.getName(), "Channel"));
                        return;
                    }
                } else if (!event.getMember().hasPermission(p)) {
                    terminate(event, String.format(USER_PERM, event.getClient().getError(), p.getName(), "Guild"));
                    return;
                }
            }
        } else if (guildOnly) {
            terminate(event, event.getClient().getError() + " This command cannot be used in Direct messages");
            return;
        }

        //cooldown check
        if (cooldown > 0) {
            String key = getCooldownKey(event);
            int remaining = event.getClient().getRemainingCooldown(key);
            if (remaining > 0) {
                terminate(event, getCooldownError(event, remaining));
                return;
            }
            event.getClient().applyCooldown(key, cooldown);
        }

        // run
        try {
            execute(event);
        } catch (Throwable t) {
            if (event.getClient().getListener() != null) {
                event.getClient().getListener().onCommandException(event, this, t);
                return;
            }
            // otherwise we rethrow
            throw t;
        }

        if (event.getClient().getListener() != null)
            event.getClient().getListener().onCompletedCommand(event, this);
    }

    public boolean isCommandFor(String input) {
        if (name.equalsIgnoreCase(input))
            return true;
        for (String alias : aliases)
            if (alias.equalsIgnoreCase(input))
                return true;
        return false;
    }

    public boolean isAllowed(TextChannel channel) {
        if (!usesTopicTags || channel == null)
            return true;
        String topic = channel.getTopic();
        if (topic == null || topic.isEmpty())
            return true;
        topic = topic.toLowerCase();
        String lowerName = name.toLowerCase();
        if (topic.contains("{" + lowerName + "}"))
            return true;
        if (topic.contains("{-" + lowerName + "}"))
            return false;
        lowerName = category == null ? null : category.name().toLowerCase();
        if (lowerName != null) {
            if (topic.contains("{" + lowerName + "}"))
                return true;
            if (topic.contains("{-" + lowerName + "}"))
                return false;
        }
        return !topic.contains("{-all}");
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public Category getCategory() {
        return category;
    }

    public String getArguments() {
        return arguments;
    }

    public boolean isGuildOnly() {
        return guildOnly;
    }

    public String getRequiredRole() {
        return requiredRole;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Permission[] getUserPermissions() {
        return userPermissions;
    }

    public Permission[] getBotPermissions() {
        return botPermissions;
    }

    public void loadAliases() {
        aliases = aliasSource.getAliases(name);
    }

    public String[] getAliases() {
        return aliases;
    }

    public Command[] getChildren() {
        return children;
    }

    public boolean isOwnerCommand() {
        return ownerCommand;
    }

    public boolean isHidden() {
        return hidden;
    }

    private void terminate(CommandEvent event, String message) {
        if (message != null)
            event.reply(message);
        if (event.getClient().getListener() != null)
            event.getClient().getListener().onTerminatedCommand(event, this);
    }

    public String getCooldownKey(CommandEvent event) {
        return switch (cooldownScope) {
            case USER -> cooldownScope.genKey(name, event.getAuthor().getIdLong());
            case USER_GUILD ->
                    event.getGuild() != null ? cooldownScope.genKey(name, event.getAuthor().getIdLong(), event.getGuild().getIdLong()) :
                            CooldownScope.USER_CHANNEL.genKey(name, event.getAuthor().getIdLong(), event.getChannel().getIdLong());
            case USER_CHANNEL ->
                    cooldownScope.genKey(name, event.getAuthor().getIdLong(), event.getChannel().getIdLong());
            case GUILD -> event.getGuild() != null ? cooldownScope.genKey(name, event.getGuild().getIdLong()) :
                    CooldownScope.CHANNEL.genKey(name, event.getChannel().getIdLong());
            case CHANNEL -> cooldownScope.genKey(name, event.getChannel().getIdLong());
            case SHARD ->
                    event.getJDA().getShardInfo() != null ? cooldownScope.genKey(name, event.getJDA().getShardInfo().getShardId()) :
                            CooldownScope.GLOBAL.genKey(name, 0);
            case USER_SHARD ->
                    event.getJDA().getShardInfo() != null ? cooldownScope.genKey(name, event.getAuthor().getIdLong(), event.getJDA().getShardInfo().getShardId()) :
                            CooldownScope.USER.genKey(name, event.getAuthor().getIdLong());
            case GLOBAL -> cooldownScope.genKey(name, 0);
        };
    }

    public String getCooldownError(CommandEvent event, int remaining) {
        if (remaining <= 0)
            return null;
        String front = event.getClient().getWarning() + " That command is on cooldown for " + remaining + " more seconds";
        if (cooldownScope == CooldownScope.USER)
            return front + "!";
        if(event.getGuild() == null) {
            if (cooldownScope == CooldownScope.USER_GUILD)
                return front + " " + CooldownScope.USER_CHANNEL.errorSpecification + "!";
            if (cooldownScope == CooldownScope.GUILD)
                return front + " " + CooldownScope.CHANNEL.errorSpecification + "!";
        }
        return front + " " + cooldownScope.errorSpecification + "!";
    }

    @Override
    public int compareTo(@NotNull Command o) {
        return category.equals(o.category) ? name.compareTo(o.name) : category.compareTo(o.category);
    }

    public static void setAliasSource(BotConfig c){
        aliasSource = c;
    }

    public record Category(String name, Predicate<CommandEvent> predicate) implements Comparable<Category> {
        public Category(String n) {
            this(n,null);
        }

        public boolean test(CommandEvent event) {
            return predicate == null || predicate.test(event);
        }

        @Override
        public int compareTo(@NotNull Category o) {
            return name.compareTo(o.name);
        }
    }

    public enum CooldownScope {
        USER("U:%d", ""),

        CHANNEL("C:%d", "in this channel"),

        USER_CHANNEL("U:%d|C:%d", "in this channel"),

        GUILD("G:%d", "in this server"),

        USER_GUILD("U:%d|G:%d", "in this server"),

        SHARD("S:%d", "on this shard"),

        USER_SHARD("U:%d|S:%d", "on this shard"),

        GLOBAL("Global", "globally");

        private final String format, errorSpecification;

        CooldownScope(String f, String e) {
            format = f;
            errorSpecification = e;
        }

        String genKey(String name, long id) {
            return genKey(name, id, -1);
        }

        String genKey(String name, long idOne, long idTwo) {
            return name + "|" + (this == GLOBAL ? format :
                (idTwo == -1 ? String.format(format, idOne) : String.format(format, idOne, idTwo) ) );
        }
    }
}
