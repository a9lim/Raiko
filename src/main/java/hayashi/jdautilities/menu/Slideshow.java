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
package hayashi.jdautilities.menu;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

public class Slideshow extends Menu {
    private final BiFunction<Integer, Integer, Color> color;
    private final BiFunction<Integer, Integer, String> text, description;
    private final boolean showPageNumbers, waitOnSinglePage, wrapPageEnds, allowTextInput;
    private final List<String> urls;
    private final Consumer<Message> finalAction;
    private final int bulkSkipNumber;
    private final String leftText, rightText;

    public static final String BIG_LEFT = "\u23EA";
    public static final String LEFT = "\u25C0";
    public static final String STOP = "\u23F9";
    public static final String RIGHT = "\u25B6";
    public static final String BIG_RIGHT = "\u23E9";

    Slideshow(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
              BiFunction<Integer, Integer, Color> c, BiFunction<Integer, Integer, String> t,
              BiFunction<Integer, Integer, String> d, Consumer<Message> consumer,
              boolean spn, List<String> items, boolean b,
              int i, boolean b1, String lt, String rt,
              boolean b2) {
        super(waiter, users, roles, timeout, unit);
        color = c;
        text = t;
        description = d;
        showPageNumbers = spn;
        urls = items;
        finalAction = consumer;
        waitOnSinglePage = b;
        bulkSkipNumber = i;
        wrapPageEnds = b1;
        leftText = lt;
        rightText = rt;
        allowTextInput = b2;
    }

    @Override
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
        else if (pageNum > urls.size())
            pageNum = urls.size();
        Message msg = renderPage(pageNum);
        initialize(channel.sendMessage(msg), pageNum);
    }

    public void paginate(Message message, int pageNum) {
        if (pageNum < 1)
            pageNum = 1;
        else if (pageNum > urls.size())
            pageNum = urls.size();
        Message msg = renderPage(pageNum);
        initialize(message.editMessage(msg), pageNum);
    }

    private void initialize(RestAction<Message> action, int pageNum) {
        action.queue(m -> {
            if (urls.size() > 1) {
                if (bulkSkipNumber > 1)
                    m.addReaction(BIG_LEFT).queue();
                m.addReaction(LEFT).queue();
                m.addReaction(STOP).queue();
                if (bulkSkipNumber > 1)
                    m.addReaction(RIGHT).queue();
                m.addReaction(bulkSkipNumber > 1 ? BIG_RIGHT : RIGHT)
                    .queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            } else if (waitOnSinglePage) {
                m.addReaction(STOP).queue(v -> pagination(m, pageNum), t -> pagination(m, pageNum));
            } else {
                finalAction.accept(m);
            }
        });
    }

    private void pagination(Message message, int pageNum) {
        if (allowTextInput || (leftText != null && rightText != null))
            paginationWithTextInput(message, pageNum);
        else
            paginationWithoutTextInput(message, pageNum);
    }

    private void paginationWithTextInput(Message message, int pageNum) {
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
                        if (1 <= i && i <= urls.size() && i != pageNum)
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
                MessageReceivedEvent mre = ((MessageReceivedEvent) event);
                String rawContent = mre.getMessage().getContentRaw().trim();

                int pages = urls.size();
                final int targetPage = (rawContent.equalsIgnoreCase(leftText) && (1 < pageNum || wrapPageEnds)) ?
                    (pageNum - 1 < 1 && wrapPageEnds ? pages : pageNum - 1) :
                    (rawContent.equalsIgnoreCase(rightText) && (pageNum < pages || wrapPageEnds) ?
                        (pageNum + 1 > pages && wrapPageEnds ? 1 : pageNum + 1) :
                        Integer.parseInt(rawContent) );

                message.editMessage(renderPage(targetPage)).queue(m -> pagination(m, targetPage));
                mre.getMessage().delete().queue(v -> {}, t -> {}); // delete the calling message so it doesn't get spammy
            }
        }, timeout, unit, () -> finalAction.accept(message));
    }

    private void paginationWithoutTextInput(Message message, int pageNum) {
        waiter.waitForEvent(MessageReactionAddEvent.class,
            event -> checkReaction(event, message.getIdLong()),
            event -> handleMessageReactionAddAction(event, message, pageNum),
            timeout, unit, () -> finalAction.accept(message));
    }

    // Private method that checks MessageReactionAddEvents
    private boolean checkReaction(MessageReactionAddEvent event, long messageId) {
        if (event.getMessageIdLong() != messageId)
            return false;
        return switch (event.getReactionEmote().getName()) {
            // LEFT, STOP, RIGHT, BIG_LEFT, BIG_RIGHT all fall-through to
            // return if the User is valid or not. If none trip, this defaults
            // and returns false.
            case LEFT, STOP, RIGHT -> isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
            case BIG_LEFT, BIG_RIGHT ->
                    bulkSkipNumber > 1 && isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
            default -> false;
        };
    }

    // Private method that handles MessageReactionAddEvents
    private void handleMessageReactionAddAction(MessageReactionAddEvent event, Message message, int pageNum) {
        int newPageNum = pageNum;
        int pages = urls.size();
        switch (event.getReaction().getReactionEmote().getName()) {
            case LEFT -> {
                if (newPageNum == 1 && wrapPageEnds)
                    newPageNum = pages + 1;
                if (newPageNum > 1)
                    newPageNum--;
            }
            case RIGHT -> {
                if (newPageNum == pages && wrapPageEnds)
                    newPageNum = 0;
                if (newPageNum < pages)
                    newPageNum++;
            }
            case BIG_LEFT -> {
                if (newPageNum > 1 || wrapPageEnds) {
                    for (int i = 1; (newPageNum > 1 || wrapPageEnds) && i < bulkSkipNumber; i++) {
                        if (newPageNum == 1 && wrapPageEnds)
                            newPageNum = pages + 1;
                        newPageNum--;
                    }
                }
            }
            case BIG_RIGHT -> {
                if (newPageNum < pages || wrapPageEnds) {
                    for (int i = 1; (newPageNum < pages || wrapPageEnds) && i < bulkSkipNumber; i++) {
                        if (newPageNum == pages && wrapPageEnds)
                            newPageNum = 0;
                        newPageNum++;
                    }
                }
            }
            case STOP -> {
                finalAction.accept(message);
                return;
            }
        }

        try {
            event.getReaction().removeReaction(event.getUser()).queue();
        } catch (PermissionException ignored) {}

        int n = newPageNum;
        message.editMessage(renderPage(newPageNum)).queue(m -> pagination(m, n));
    }

    private Message renderPage(int pageNum) {
        MessageBuilder mbuilder = new MessageBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();
        ebuilder.setImage(urls.get(pageNum - 1));
        ebuilder.setColor(color.apply(pageNum, urls.size()));
        ebuilder.setDescription(description.apply(pageNum, urls.size()));
        if (showPageNumbers)
            ebuilder.setFooter("Image " + pageNum + "/" + urls.size(), null);
        mbuilder.setEmbeds(ebuilder.build());
        if (text != null)
            mbuilder.append(text.apply(pageNum, urls.size()));
        return mbuilder.build();
    }

    public static class Builder extends Menu.Builder<Builder, Slideshow> {
        private BiFunction<Integer, Integer, Color> color = (page, pages) -> null;
        private BiFunction<Integer, Integer, String> text = (page, pages) -> null;
        private BiFunction<Integer, Integer, String> description = (page, pages) -> null;
        private Consumer<Message> finalAction = m -> m.delete().queue();
        private boolean showPageNumbers = true;
        private boolean waitOnSinglePage, wrapPageEnds, allowTextInput;
        private int bulkSkipNumber = 1;
        private String textToLeft, textToRight;

        private final List<String> strings = new LinkedList<>();

        @Override
        public Slideshow build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!strings.isEmpty(), "Must include at least one item to paginate");

            return new Slideshow(
                waiter, users, roles, timeout, unit, color, text, description, finalAction,
                showPageNumbers, strings, waitOnSinglePage, bulkSkipNumber, wrapPageEnds,
                textToLeft, textToRight, allowTextInput);
        }

        public Builder setColor(Color c) {
            color = (i0, i1) -> c;
            return this;
        }

        public Builder setColor(BiFunction<Integer, Integer, Color> colorBiFunction) {
            color = colorBiFunction;
            return this;
        }

        public Builder setText(String t) {
            text = (i0, i1) -> t;
            return this;
        }

        public Builder setText(BiFunction<Integer, Integer, String> textBiFunction) {
            text = textBiFunction;
            return this;
        }

        public Builder setDescription(String d) {
            description = (i0, i1) -> d;
            return this;
        }

        public Builder setDescription(BiFunction<Integer, Integer, String> descriptionBiFunction) {
            description = descriptionBiFunction;
            return this;
        }

        public Builder setFinalAction(Consumer<Message> consumer) {
            finalAction = consumer;
            return this;
        }

        public Builder showPageNumbers(boolean show) {
            showPageNumbers = show;
            return this;
        }

        public Builder waitOnSinglePage(boolean wait) {
            waitOnSinglePage = wait;
            return this;
        }

        public Builder addItems(String... items) {
            strings.addAll(Arrays.asList(items));
            return this;
        }

        public Builder setUrls(String... items) {
            strings.clear();
            strings.addAll(Arrays.asList(items));
            return this;
        }

        public Builder setBulkSkipNumber(int bsn) {
            bulkSkipNumber = Math.max(bsn, 1);
            return this;
        }

        public Builder wrapPageEnds(boolean b) {
            wrapPageEnds = b;
            return this;
        }

        public Builder allowTextInput(boolean ati) {
            allowTextInput = ati;
            return this;
        }

        public Builder setLeftRightText(String left, String right) {
            if (left == null || right == null) {
                textToLeft = null;
                textToRight = null;
            } else {
                textToLeft = left;
                textToRight = right;
            }
            return this;
        }
    }

}
