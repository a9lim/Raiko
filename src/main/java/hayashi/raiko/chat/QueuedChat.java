package hayashi.raiko.chat;

import hayashi.raiko.queue.Queueable;

public record QueuedChat(String chat, long identifier) implements Queueable {

    // clean this

    @Override
    public long getIdentifier() {
        return identifier;
    }

    public String toString(){
        return chat;
    }
}