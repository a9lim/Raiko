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

package a9lim.jdautilities.commons.utils;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class FinderUtil {
    public final static Pattern DISCORD_ID = Pattern.compile("\\d{17,20}"); // ID
    public final static Pattern FULL_USER_REF = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})"); // $1 -> username, $2 -> discriminator
    public final static Pattern USER_MENTION = Pattern.compile("<@!?(\\d{17,20})>"); // $1 -> ID
    public final static Pattern CHANNEL_MENTION = Pattern.compile("<#(\\d{17,20})>"); // $1 -> ID
    public final static Pattern ROLE_MENTION = Pattern.compile("<@&(\\d{17,20})>"); // $1 -> ID
    public final static Pattern EMOTE_MENTION = Pattern.compile("<:(.{2,32}):(\\d{17,20})>");

    public static List<User> findUsers(String query, JDA jda) {
        return jdaUserSearch(query, jda, true);
    }

    public static List<User> findShardUsers(String query, JDA jda) {
        return jdaUserSearch(query, jda, false);
    }

    private static List<User> jdaUserSearch(String query, JDA jda, boolean useShardManager) {
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);

        ShardManager manager = useShardManager ? jda.getShardManager() : null;

        if (userMention.matches()) {
            User user = manager != null ? manager.getUserById(userMention.group(1)) :
                jda.getUserById(userMention.group(1));
            if (user != null)
                return Collections.singletonList(user);
        } else if (fullRefMatch.matches()) {
            List<User> users = (manager != null ? manager.getUserCache() : jda.getUserCache())
                .stream().filter(user -> user.getName().equalsIgnoreCase(fullRefMatch.group(1))
                    && user.getDiscriminator().equals(fullRefMatch.group(2)))
                .collect(Collectors.toList());
            if (!users.isEmpty())
                return users;
        } else if (DISCORD_ID.matcher(query).matches()) {
            User user = (manager != null ? manager.getUserById(query) : jda.getUserById(query));
            if (user != null)
                return Collections.singletonList(user);
        }

        List<User> exact = new ArrayList<>();
        List<User> wrongcase = new ArrayList<>();
        List<User> startswith = new ArrayList<>();
        List<User> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        (manager != null ? manager.getUserCache() : jda.getUserCache()).forEach(user -> {
            String name = user.getName();
            if (name.equals(query))
                exact.add(user);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(user);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(user);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(user);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<User> findBannedUsers(String query, Guild guild) {
        List<User> bans;
        try {
            bans = guild.retrieveBanList().complete().stream()
                .map(Guild.Ban::getUser)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return null;
        }
        String discrim = null;
        Matcher userMention = USER_MENTION.matcher(query);
        if (userMention.matches()) {
            String id = userMention.group(1);
            User user = guild.getJDA().getUserById(id);
            if (user != null && bans.contains(user))
                return Collections.singletonList(user);
            for (User u : bans)
                if (u.getId().equals(id))
                    return Collections.singletonList(u);
        } else if (FULL_USER_REF.matcher(query).matches()) {
            discrim = query.substring(query.length() - 4);
            query = query.substring(0, query.length() - 5).trim();
        } else if (DISCORD_ID.matcher(query).matches()) {
            User user = guild.getJDA().getUserById(query);
            if (user != null && bans.contains(user))
                return Collections.singletonList(user);
            for (User u : bans)
                if (u.getId().equals(query))
                    return Collections.singletonList(u);
        }
        List<User> exact = new ArrayList<>();
        List<User> wrongcase = new ArrayList<>();
        List<User> startswith = new ArrayList<>();
        List<User> contains = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (User u : bans) {
            // If a discrim is specified then we skip all users without it.
            if (discrim != null && !u.getDiscriminator().equals(discrim))
                continue;

            if (u.getName().equals(query))
                exact.add(u);
            else if (exact.isEmpty() && u.getName().equalsIgnoreCase(query))
                wrongcase.add(u);
            else if (wrongcase.isEmpty() && u.getName().toLowerCase().startsWith(lowerQuery))
                startswith.add(u);
            else if (startswith.isEmpty() && u.getName().toLowerCase().contains(lowerQuery))
                contains.add(u);
        }
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<Member> findMembers(String query, Guild guild) {
        Matcher userMention = USER_MENTION.matcher(query);
        Matcher fullRefMatch = FULL_USER_REF.matcher(query);
        if (userMention.matches()) {
            Member member = guild.getMemberById(userMention.group(1));
            if (member != null)
                return Collections.singletonList(member);
        } else if (fullRefMatch.matches()) {
            List<Member> members = guild.getMemberCache().stream()
                .filter(member -> member.getUser().getName().equalsIgnoreCase(fullRefMatch.group(1))
                    && member.getUser().getDiscriminator().equals(fullRefMatch.group(2)))
                .collect(Collectors.toList());
            if (!members.isEmpty())
                return members;
        } else if (DISCORD_ID.matcher(query).matches()) {
            Member member = guild.getMemberById(query);
            if (member != null)
                return Collections.singletonList(member);
        }
        List<Member> exact = new ArrayList<>();
        List<Member> wrongcase = new ArrayList<>();
        List<Member> startswith = new ArrayList<>();
        List<Member> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        guild.getMemberCache().forEach(member -> {
            String name = member.getUser().getName();
            String effName = member.getEffectiveName();
            if (name.equals(query) || effName.equals(query))
                exact.add(member);
            else if ((name.equalsIgnoreCase(query) || effName.equalsIgnoreCase(query)) && exact.isEmpty())
                wrongcase.add(member);
            else if ((name.toLowerCase().startsWith(lowerquery) || effName.toLowerCase().startsWith(lowerquery)) && wrongcase.isEmpty())
                startswith.add(member);
            else if ((name.toLowerCase().contains(lowerquery) || effName.toLowerCase().contains(lowerquery)) && startswith.isEmpty())
                contains.add(member);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<TextChannel> findTextChannels(String query, JDA jda) {
        return jdaTextChannelSearch(query, jda, true);
    }

    public static List<TextChannel> findShardTextChannels(String query, JDA jda) {
        return jdaTextChannelSearch(query, jda, false);
    }

    public static List<TextChannel> findTextChannels(String query, Guild guild) {
        Matcher channelMention = CHANNEL_MENTION.matcher(query);
        TextChannel tc = channelMention.matches() ? guild.getTextChannelById(channelMention.group(1)) :
            (DISCORD_ID.matcher(query).matches() ? guild.getTextChannelById(query) : null);
        return tc != null ? Collections.singletonList(tc) : genericTextChannelSearch(query, guild.getTextChannelCache());
    }

    private static List<TextChannel> jdaTextChannelSearch(String query, JDA jda, boolean useShardManager) {
        Matcher channelMention = CHANNEL_MENTION.matcher(query);
        ShardManager manager = useShardManager ? jda.getShardManager() : null;
        TextChannel tc = channelMention.matches() ? (manager != null ? manager.getTextChannelById(channelMention.group(1)) :
                jda.getTextChannelById(channelMention.group(1))) : ( DISCORD_ID.matcher(query).matches() ?
                ( manager != null ? manager.getTextChannelById(query) : jda.getTextChannelById(query) ) : null );
        return tc != null ? Collections.singletonList(tc) : genericTextChannelSearch(query, manager != null ? manager.getTextChannelCache() : jda.getTextChannelCache());
    }

    private static List<TextChannel> genericTextChannelSearch(String query, SnowflakeCacheView<TextChannel> cache) {
        List<TextChannel> exact = new ArrayList<>();
        List<TextChannel> wrongcase = new ArrayList<>();
        List<TextChannel> startswith = new ArrayList<>();
        List<TextChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach((tc) -> {
            String name = tc.getName();
            if (name.equals(query))
                exact.add(tc);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(tc);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(tc);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(tc);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<VoiceChannel> findVoiceChannels(String query, JDA jda) {
        return jdaVoiceChannelSearch(query, jda, true);
    }

    public static List<VoiceChannel> findShardVoiceChannels(String query, JDA jda) {
        return jdaVoiceChannelSearch(query, jda, false);
    }

    public static List<VoiceChannel> findVoiceChannels(String query, Guild guild) {
        VoiceChannel vc = DISCORD_ID.matcher(query).matches() ? guild.getVoiceChannelById(query) : null;
        return vc != null ? Collections.singletonList(vc) : genericVoiceChannelSearch(query, guild.getVoiceChannelCache());
    }

    private static List<VoiceChannel> jdaVoiceChannelSearch(String query, JDA jda, boolean useShardManager) {
        ShardManager manager = useShardManager ? jda.getShardManager() : null;
        VoiceChannel vc = DISCORD_ID.matcher(query).matches() ? (manager != null ? manager.getVoiceChannelById(query) : jda.getVoiceChannelById(query) ): null;
        return vc != null ? Collections.singletonList(vc) : genericVoiceChannelSearch(query, manager != null ? manager.getVoiceChannelCache() : jda.getVoiceChannelCache());
    }

    private static List<VoiceChannel> genericVoiceChannelSearch(String query, SnowflakeCacheView<VoiceChannel> cache) {
        List<VoiceChannel> exact = new ArrayList<>();
        List<VoiceChannel> wrongcase = new ArrayList<>();
        List<VoiceChannel> startswith = new ArrayList<>();
        List<VoiceChannel> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach((vc) -> {
            String name = vc.getName();
            if (name.equals(query))
                exact.add(vc);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(vc);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(vc);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(vc);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<Category> findCategories(String query, JDA jda) {
        return jdaCategorySearch(query, jda, true);
    }

    public static List<Category> findShardCategories(String query, JDA jda) {
        return jdaCategorySearch(query, jda, false);
    }

    public static List<Category> findCategories(String query, Guild guild) {
        Category cat = DISCORD_ID.matcher(query).matches() ? guild.getCategoryById(query) : null;
        return cat != null ? Collections.singletonList(cat) : genericCategorySearch(query, guild.getCategoryCache());
    }

    private static List<Category> jdaCategorySearch(String query, JDA jda, boolean useShardManager) {
        ShardManager manager = useShardManager ? jda.getShardManager() : null;
        Category cat = DISCORD_ID.matcher(query).matches() ? (manager != null ? manager.getCategoryById(query) : jda.getCategoryById(query)) : null;
        return cat != null ? Collections.singletonList(cat) : genericCategorySearch(query, jda.getCategoryCache());
    }

    private static List<Category> genericCategorySearch(String query, SnowflakeCacheView<Category> cache) {
        List<Category> exact = new ArrayList<>();
        List<Category> wrongcase = new ArrayList<>();
        List<Category> startswith = new ArrayList<>();
        List<Category> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach(cat -> {
            String name = cat.getName();
            if (name.equals(query))
                exact.add(cat);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(cat);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(cat);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(cat);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<Role> findRoles(String query, Guild guild) {
        Matcher roleMention = ROLE_MENTION.matcher(query);
        if (roleMention.matches()) {
            Role role = guild.getRoleById(roleMention.group(1));
            if (role != null)
                return Collections.singletonList(role);
        } else if (DISCORD_ID.matcher(query).matches()) {
            Role role = guild.getRoleById(query);
            if (role != null)
                return Collections.singletonList(role);
        }
        List<Role> exact = new ArrayList<>();
        List<Role> wrongcase = new ArrayList<>();
        List<Role> startswith = new ArrayList<>();
        List<Role> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        guild.getRoleCache().forEach((role) -> {
            String name = role.getName();
            if (name.equals(query))
                exact.add(role);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(role);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(role);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(role);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

    public static List<RichCustomEmoji> findEmotes(String query, JDA jda) {
        return jdaFindEmotes(query, jda, true);
    }

    public static List<RichCustomEmoji> findShardEmotes(String query, JDA jda) {
        return jdaFindEmotes(query, jda, false);
    }

    public static List<RichCustomEmoji> findEmotes(String query, Guild guild) {
        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);
        if (DISCORD_ID.matcher(query).matches()) {
            RichCustomEmoji emote = guild.getEmojiById(query);
            if (emote != null)
                return Collections.singletonList(emote);
        } else if (mentionMatcher.matches()) {
            RichCustomEmoji emote = guild.getEmojiById(mentionMatcher.group(2));
            if (emote != null && emote.getName().equals(mentionMatcher.group(1)))
                return Collections.singletonList(emote);
        }
        return genericEmoteSearch(query, guild.getEmojiCache());
    }

    private static List<RichCustomEmoji> jdaFindEmotes(String query, JDA jda, boolean useShardManager) {
        Matcher mentionMatcher = EMOTE_MENTION.matcher(query);
        ShardManager manager = useShardManager ? jda.getShardManager() : null;
        if (DISCORD_ID.matcher(query).matches()) {
            RichCustomEmoji emote = manager != null ? manager.getEmojiById(query) : jda.getEmojiById(query);
            if (emote != null)
                return Collections.singletonList(emote);
        } else if (mentionMatcher.matches()) {
            String emoteId = mentionMatcher.group(2);
            RichCustomEmoji emote = manager != null ? manager.getEmojiById(emoteId) : jda.getEmojiById(emoteId);
            if (emote != null && emote.getName().equals(mentionMatcher.group(1)))
                return Collections.singletonList(emote);
        }

        return genericEmoteSearch(query, jda.getEmojiCache());
    }

    private static List<RichCustomEmoji> genericEmoteSearch(String query, SnowflakeCacheView<RichCustomEmoji> cache) {
        List<RichCustomEmoji> exact = new ArrayList<>();
        List<RichCustomEmoji> wrongcase = new ArrayList<>();
        List<RichCustomEmoji> startswith = new ArrayList<>();
        List<RichCustomEmoji> contains = new ArrayList<>();
        String lowerquery = query.toLowerCase();
        cache.forEach(emote -> {
            String name = emote.getName();
            if (name.equals(query))
                exact.add(emote);
            else if (name.equalsIgnoreCase(query) && exact.isEmpty())
                wrongcase.add(emote);
            else if (name.toLowerCase().startsWith(lowerquery) && wrongcase.isEmpty())
                startswith.add(emote);
            else if (name.toLowerCase().contains(lowerquery) && startswith.isEmpty())
                contains.add(emote);
        });
        if (!exact.isEmpty())
            return Collections.unmodifiableList(exact);
        if (!wrongcase.isEmpty())
            return Collections.unmodifiableList(wrongcase);
        if (!startswith.isEmpty())
            return Collections.unmodifiableList(startswith);
        return Collections.unmodifiableList(contains);
    }

}
