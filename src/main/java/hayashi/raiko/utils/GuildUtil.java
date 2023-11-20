package hayashi.raiko.utils;

import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.settings.RepeatMode;
import net.dv8tion.jda.api.entities.Guild;

public class GuildUtil {
    public static boolean hasHandler(Guild guild) {
        return guild.getAudioManager().getSendingHandler() != null;
    }
    public static boolean isAlone(Guild guild) {
        return guild.getAudioManager().getConnectedChannel() != null && guild.getAudioManager().getConnectedChannel().getMembers().stream()
                .allMatch(x ->
                        x.getVoiceState().isDeafened()
                                || x.getUser().isBot());
    }
    public static String getQueueTitle(AudioHandler ah, String success, int songslength, long total, RepeatMode repeatmode) {
        StringBuilder sb = new StringBuilder();
        if (ah.getPlayer().getPlayingTrack() != null)
            sb.append(ah.getStatusEmoji()).append(" **")
                    .append(ah.getPlayer().getPlayingTrack().getInfo().title).append("**\n");
        return FormatUtil.filter(sb.append(success).append(" Current Queue | ").append(songslength)
                .append(" entries | `").append(FormatUtil.formatTime(total)).append("` ")
                .append(repeatmode.getEmoji() != null ? "| " + repeatmode.getEmoji() : "").toString());
    }

}
