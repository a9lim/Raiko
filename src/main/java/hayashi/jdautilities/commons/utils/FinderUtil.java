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
package hayashi.jdautilities.commons.utils;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView;
import net.dv8tion.jda.api.entities.channel.concrete.*;

import java.util.*;
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

        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();
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
        ArrayList<User> exact = new ArrayList<>();
        ArrayList<User> wrongcase = new ArrayList<>();
        ArrayList<User> startswith = new ArrayList<>();
        ArrayList<User> contains = new ArrayList<>();
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
        ArrayList<Member> exact = new ArrayList<>();
        ArrayList<Member> wrongcase = new ArrayList<>();
        ArrayList<Member> startswith = new ArrayList<>();
        ArrayList<Member> contains = new ArrayList<>();
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
        ArrayList<TextChannel> exact = new ArrayList<>();
        ArrayList<TextChannel> wrongcase = new ArrayList<>();
        ArrayList<TextChannel> startswith = new ArrayList<>();
        ArrayList<TextChannel> contains = new ArrayList<>();
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
        ArrayList<VoiceChannel> exact = new ArrayList<>();
        ArrayList<VoiceChannel> wrongcase = new ArrayList<>();
        ArrayList<VoiceChannel> startswith = new ArrayList<>();
        ArrayList<VoiceChannel> contains = new ArrayList<>();
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
        ArrayList<Category> exact = new ArrayList<>();
        ArrayList<Category> wrongcase = new ArrayList<>();
        ArrayList<Category> startswith = new ArrayList<>();
        ArrayList<Category> contains = new ArrayList<>();
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
        ArrayList<Role> exact = new ArrayList<>();
        ArrayList<Role> wrongcase = new ArrayList<>();
        ArrayList<Role> startswith = new ArrayList<>();
        ArrayList<Role> contains = new ArrayList<>();
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
        ArrayList<RichCustomEmoji> exact = new ArrayList<>();
        ArrayList<RichCustomEmoji> wrongcase = new ArrayList<>();
        ArrayList<RichCustomEmoji> startswith = new ArrayList<>();
        ArrayList<RichCustomEmoji> contains = new ArrayList<>();
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
