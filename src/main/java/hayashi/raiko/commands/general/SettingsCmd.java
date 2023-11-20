/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hayashi.raiko.commands.general;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.settings.RepeatMode;
import hayashi.raiko.settings.Settings;
import hayashi.raiko.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class SettingsCmd extends Command {
    private final static String EMOJI = "\uD83C\uDFA7"; // 🎧

    public SettingsCmd(Bot bot) {
        name = "settings";
        help = "shows the bots settings";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchan = s.getTextChannel(event.getGuild());
        VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        event.getChannel().sendMessage(new MessageCreateBuilder()
                .addContent(EMOJI + " **")
                .addContent(FormatUtil.filter(event.getSelfUser().getName()))
                .addContent("** settings:")
                .setEmbeds(new EmbedBuilder()
                .setColor(event.getSelfMember().getColor())
                .setDescription("Text Channel: " + (tchan == null ? "Any" : "**#" + tchan.getName() + "**")
                        + "\nVoice Channel: " + (vchan == null ? "Any" : vchan.getAsMention())
                        + "\nCustom Prefix: " + (s.getPrefix() == null ? "None" : "`" + s.getPrefix() + "`")
                        + "\nRepeat Mode: " + (s.getRepeatMode() == RepeatMode.OFF
                        ? s.getRepeatMode().getUserFriendlyName()
                        : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                        + "\nDefault Playlist: " + (s.getDefaultPlaylist() == null ? "None" : "**" + s.getDefaultPlaylist() + "**")
                )
                .setFooter(event.getJDA().getGuilds().size() + " servers | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count()
                        + " audio connections", null).build()).build()).queue();
    }

}
