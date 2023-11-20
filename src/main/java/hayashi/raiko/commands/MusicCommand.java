// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
// Copyright 2018 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

package hayashi.raiko.commands;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.settings.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

public abstract class MusicCommand extends Command {
    protected final Bot bot;
    protected boolean bePlaying, beListening;

    public MusicCommand(Bot b) {
        bot = b;
        guildOnly = true;
        category = new Category("Music");
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchannel = settings.getTextChannel(event.getGuild());
        if (tchannel != null && !event.getTextChannel().equals(tchannel)) {
            try {
                event.getMessage().delete().queue();
            } catch (PermissionException ignore) {}
            event.replyInDm(event.getClient().getError() + " You can only use that command in " + tchannel.getAsMention() + "!");
            return;
        }
        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later
        if (bePlaying && ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).noMusicPlaying(event.getJDA())) {
            event.replyError(" There must be music playing to use that!");
            return;
        }
        if (beListening) {
            VoiceChannel current;
            try {
                current = event.getGuild().getSelfMember().getVoiceState().getChannel().asVoiceChannel();
            } catch (Exception ex) {
                current = settings.getVoiceChannel(event.getGuild());
            }
            GuildVoiceState userState = event.getMember().getVoiceState();
            if (!userState.inAudioChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
                event.replyError("You must be listening in " + (current == null ? "a voice channel" : current.getAsMention()) + " to use that!");
                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.replyError("You cannot use that command in an AFK channel!");
                return;
            }

            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.replyError(" I am unable to connect to " + userState.getChannel().getAsMention() + "!");
                    return;
                }
            }
        }

        doCommand(event);
    }

    public abstract void doCommand(CommandEvent event);
}
