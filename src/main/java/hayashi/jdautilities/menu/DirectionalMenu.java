package hayashi.jdautilities.menu;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class DirectionalMenu extends Menu{
    protected final BiFunction<Integer,Integer, Color> color;
    protected final BiFunction<Integer, Integer, String> text;
    protected final Consumer<Message> finalAction;
    protected final int bulkSkipNumber;
    protected int pages;
    protected final boolean wrapPageEnds, allowTextInput, waitOnSinglePage;
    protected final String leftText, rightText;
    public static final Emoji BIG_LEFT = Emoji.fromUnicode("\u23EA");
    public static final Emoji LEFT = Emoji.fromUnicode("\u25C0");
    public static final Emoji STOP = Emoji.fromUnicode("\u23F9");
    public static final Emoji RIGHT = Emoji.fromUnicode("\u25B6");
    public static final Emoji BIG_RIGHT = Emoji.fromUnicode("\u23E9");
    protected DirectionalMenu(EventWaiter w, Set<User> u, Set<Role> r, long t, TimeUnit unidad,
                              BiFunction<Integer, Integer, Color> c, BiFunction<Integer, Integer, String> s,
                              Consumer<Message> consumer, int p, boolean wosp, int i, boolean b, String lt, String rt, boolean ati) {
        super(w, u, r, t, unidad);
        color = c;
        text = s;
        finalAction = consumer;
        pages = p;
        waitOnSinglePage = wosp;
        bulkSkipNumber = i;
        wrapPageEnds = b;
        leftText = lt;
        rightText = rt;
        allowTextInput = ati;
    }
    protected void handleMessageReactionAddAction(MessageReactionAddEvent event, Message message, int pageNum) {
        int newPageNum = pageNum;
        Emoji emoji = event.getReaction().getEmoji();
        if (emoji.equals(LEFT)) {
            if (newPageNum == 1 && wrapPageEnds)
                newPageNum = pages + 1;
            if (newPageNum > 1)
                newPageNum--;
        } else if (emoji.equals(RIGHT)) {
            if (newPageNum == pages && wrapPageEnds)
                newPageNum = 0;
            if (newPageNum < pages)
                newPageNum++;
        } else if (emoji.equals(BIG_LEFT)) {
            if (newPageNum > 1 || wrapPageEnds) {
                for (int i = 1; (newPageNum > 1 || wrapPageEnds) && i < bulkSkipNumber; i++) {
                    if (newPageNum == 1 && wrapPageEnds)
                        newPageNum = pages + 1;
                    newPageNum--;
                }
            }
        } else if (emoji.equals(BIG_RIGHT)) {
            if (newPageNum < pages || wrapPageEnds) {
                for (int i = 1; (newPageNum < pages || wrapPageEnds) && i < bulkSkipNumber; i++) {
                    if (newPageNum == pages && wrapPageEnds)
                        newPageNum = 0;
                    newPageNum++;
                }
            }
        } else if (emoji.equals(STOP)) {
            finalAction.accept(message);
            return;
        }

        try {
            event.getReaction().removeReaction(event.getUser()).queue();
        } catch (PermissionException ignored) {}

        int n = newPageNum;
        message.editMessage(renderPage(newPageNum)).queue(m -> pagination(m, n));
    }

    public void display(MessageChannel channel) {
        paginate(channel, 1);
    }

    @Override
    public void display(Message message) {
        paginate(message, 1);
    }

    public void paginate(MessageChannel channel, int pageNum) {
        if (pageNum < 1)
            pageNum = 1;
        else if (pageNum > pages)
            pageNum = pages;
        initialize(channel.sendMessage(MessageCreateData.fromEditData(renderPage(pageNum))), pageNum);
    }

    public void paginate(Message message, int pageNum) {
        if (pageNum < 1)
            pageNum = 1;
        else if (pageNum > pages)
            pageNum = pages;
        initialize(message.editMessage(renderPage(pageNum)), pageNum);
    }

    protected void initialize(RestAction<Message> action, int pageNum) {
        action.queue(m -> {
            if (pages > 1) {
                if (bulkSkipNumber > 1)
                    m.addReaction(BIG_LEFT).queue();
                m.addReaction(LEFT).queue();
                m.addReaction(STOP).queue();
                if (bulkSkipNumber > 1)
                    m.addReaction(RIGHT).queue();
                m.addReaction(bulkSkipNumber > 1 ? BIG_RIGHT : RIGHT)
                        .queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            } else if (waitOnSinglePage) {
                // Go straight to without text-input because only one page is available
                m.addReaction(STOP).queue(
                        v -> paginationWithoutTextInput(m, pageNum),
                        t -> paginationWithoutTextInput(m, pageNum)
                );
            } else {
                finalAction.accept(m);
            }
        });
    }

    protected void pagination(Message message, int pageNum) {
        if (allowTextInput || (leftText != null && rightText != null))
            paginationWithTextInput(message, pageNum);
        else
            paginationWithoutTextInput(message, pageNum);
    }

    protected void paginationWithTextInput(Message message, int pageNum) {
        waiter.waitForEvent(GenericMessageEvent.class, event -> {
            if (event instanceof MessageReactionAddEvent e)
                return checkReaction(e, message.getIdLong());
            else if (event instanceof MessageReceivedEvent mre) {
                // Wrong channel
                if (!mre.getChannel().equals(message.getChannel()))
                    return false;
                String rawContent = mre.getMessage().getContentRaw().trim();
                if (rawContent.equalsIgnoreCase(leftText) || rawContent.equalsIgnoreCase(rightText))
                    return isValidUser(mre.getAuthor(), mre.isFromGuild() ? mre.getGuild() : null);

                if (allowTextInput) {
                    try {
                        int i = Integer.parseInt(rawContent);
                        // Minimum 1, Maximum the number of pages, never the current page number
                        if (1 <= i && i <= pages && i != pageNum)
                            return isValidUser(mre.getAuthor(), mre.isFromGuild() ? mre.getGuild() : null);
                    } catch (NumberFormatException ignored) {}
                }
            }
            // Default return false
            return false;
        }, event -> {
            if (event instanceof MessageReactionAddEvent e) {
                handleMessageReactionAddAction(e, message, pageNum);
            } else {
                MessageReceivedEvent mre = (MessageReceivedEvent) event;
                String rawContent = mre.getMessage().getContentRaw().trim();

                final int targetPage = (rawContent.equalsIgnoreCase(leftText) && (1 < pageNum || wrapPageEnds)) ?
                        (pageNum - 1 < 1 && wrapPageEnds ? pages : pageNum - 1) :
                        ((rawContent.equalsIgnoreCase(rightText) && (pageNum < pages || wrapPageEnds)) ?
                                pageNum + 1 > pages && wrapPageEnds ? 1 : pageNum + 1 :
                                Integer.parseInt(rawContent));

                message.editMessage(renderPage(targetPage)).queue(m -> pagination(m, targetPage));
                mre.getMessage().delete().queue(v -> {}, t -> {}); // delete the calling message so it doesn't get spammy
            }
        }, timeout, unit, () -> finalAction.accept(message));
    }

    protected void paginationWithoutTextInput(Message message, int pageNum) {
        waiter.waitForEvent(MessageReactionAddEvent.class,
                event -> checkReaction(event, message.getIdLong()), // Check Reaction
                event -> handleMessageReactionAddAction(event, message, pageNum), // Handle Reaction
                timeout, unit, () -> finalAction.accept(message));
    }

    protected boolean checkReaction(MessageReactionAddEvent event, long messageId) {
        if (event.getMessageIdLong() != messageId)
            return false;
        Emoji emoji = event.getEmoji();// LEFT, STOP, RIGHT, BIG_LEFT, BIG_RIGHT all fall-through to
// return if the User is valid or not. If none trip, this defaults
// and returns false.
        if (emoji.equals(LEFT) || emoji.equals(STOP) || emoji.equals(RIGHT)) {
            return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        }
        if (emoji.equals(BIG_LEFT) || emoji.equals(BIG_RIGHT)) {
            return bulkSkipNumber > 1 && isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        }
        return false;
    }

    protected abstract MessageEditData renderPage(int pageNum);

    public abstract static class Builder<T extends DirectionalMenu.Builder<T, V>, V extends DirectionalMenu> extends Menu.Builder<T,V>{
        protected BiFunction<Integer, Integer, Color> color = (page, pages) -> null;
        protected BiFunction<Integer, Integer, String> text = (page, pages) -> null;

        protected Consumer<Message> finalAction = m -> m.delete().queue();

        protected boolean showPageNumbers = true;
        protected boolean waitOnSinglePage, wrapPageEnds, allowTextInput;
        protected int bulkSkipNumber = 1;
        protected String textToLeft, textToRight;

        protected final List<String> strings = new LinkedList<>();

        public final T setColor(Color c) {
            color = (i0, i1) -> c;
            return (T) this;
        }

        public final T setColor(BiFunction<Integer, Integer, Color> colorBiFunction) {
            color = colorBiFunction;
            return (T) this;
        }

        public final T setText(String t) {
            text = (i0, i1) -> t;
            return (T) this;
        }

        public final T setText(BiFunction<Integer, Integer, String> textBiFunction) {
            text = textBiFunction;
            return (T) this;
        }

        public final T setFinalAction(Consumer<Message> consumer) {
            finalAction = consumer;
            return (T) this;
        }


        public final T showPageNumbers(boolean show) {
            showPageNumbers = show;
            return (T) this;
        }

        public final T waitOnSinglePage(boolean wait) {
            waitOnSinglePage = wait;
            return (T) this;
        }

        public final T addItems(String... items) {
            strings.addAll(Arrays.asList(items));
            return (T) this;
        }

        public final T setItems(String... items) {
            strings.clear();
            strings.addAll(Arrays.asList(items));
            return (T) this;
        }

        public final T clearItems() {
            strings.clear();
            return (T) this;
        }

        public final T setBulkSkipNumber(int bsn) {
            bulkSkipNumber = Math.max(bsn, 1);
            return (T) this;
        }

        public final T wrapPageEnds(boolean b) {
            wrapPageEnds = b;
            return (T) this;
        }

        public final T allowTextInput(boolean ati) {
            allowTextInput = ati;
            return (T) this;
        }

        public final T setLeftRightText(String left, String right) {
            if (left == null || right == null) {
                textToLeft = null;
                textToRight = null;
            } else {
                textToLeft = left;
                textToRight = right;
            }
            return (T) this;
        }
    }

}
