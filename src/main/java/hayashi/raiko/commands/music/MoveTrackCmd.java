package hayashi.raiko.commands.music;


import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.audio.AudioHandler;
import hayashi.raiko.audio.QueuedTrack;
import hayashi.raiko.commands.MusicCommand;
import hayashi.raiko.queue.DoubleDealingQueue;

/**
 * Command that provides users the ability to move a track in the playlist.
 */
public class MoveTrackCmd extends MusicCommand {

    public MoveTrackCmd(Bot bot) {
        super(bot);
        this.name = "movetrack";
        this.help = "move a track in the current queue to a different position";
        this.arguments = "<from> <to>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int from, to;

        String[] parts = COMPILE.split(event.getArgs(), 2);
        try {
            // Validate the args
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            event.replyError("Please provide two valid indexes.");
            return;
        }
        if (from == to) {
            event.replyError("Can't move a track to the same position.");
            return;
        }
        // Validate that from and to are available
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        DoubleDealingQueue<QueuedTrack> queue = handler.getQueue();
        if (isUnavailablePosition(queue, from) || isUnavailablePosition(queue, to)) {
            event.replyError("Provide a valid position in the queue!");
        } else {
            // Move the track
            event.replySuccess(String.format("Moved **%s** from position `%d` to `%d`.", queue.moveItem(from - 1, to - 1).getTrack().getInfo().title, from, to));
        }
    }

    private static boolean isUnavailablePosition(DoubleDealingQueue<QueuedTrack> queue, int position) {
        return (position < 1 || position > queue.size());
    }
}