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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

public class SelectionDialog extends Menu {
    private final List<String> choices;
    private final String leftEnd, rightEnd, defaultLeft, defaultRight;
    private final Function<Integer, Color> color;
    private final boolean loop, singleSelectionMode;
    private final Function<Integer, String> text;
    private final BiConsumer<Message, Integer> success;
    private final Consumer<Message> cancel;

    public static final String UP = "\uD83D\uDD3C";
    public static final String DOWN = "\uD83D\uDD3D";
    public static final String SELECT = "\u2705";
    public static final String CANCEL = "\u274E";

    SelectionDialog(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                    List<String> c, String le, String re, String dl, String dr,
                    Function<Integer, Color> co, boolean l, BiConsumer<Message, Integer> succ,
                    Consumer<Message> can, Function<Integer, String> t, boolean b) {
        super(waiter, users, roles, timeout, unit);
        choices = c;
        leftEnd = le;
        rightEnd = re;
        defaultLeft = dl;
        defaultRight = dr;
        color = co;
        loop = l;
        success = succ;
        cancel = can;
        text = t;
        singleSelectionMode = b;
    }

    @Deprecated
    SelectionDialog(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                    List<String> choices, String leftEnd, String rightEnd, String defaultLeft, String defaultRight,
                    Function<Integer, Color> color, boolean loop, BiConsumer<Message, Integer> success,
                    Consumer<Message> cancel, Function<Integer, String> text) {
        this(waiter, users, roles, timeout, unit, choices, leftEnd, rightEnd, defaultLeft, defaultRight, color, loop, success, cancel, text, false);
    }

    @Override
    public void display(MessageChannel channel) {
        showDialog(channel, 1);
    }

    @Override
    public void display(Message message) {
        showDialog(message, 1);
    }

    public void showDialog(MessageChannel channel, int selection) {
        if (selection < 1)
            selection = 1;
        else if (selection > choices.size())
            selection = choices.size();
        initialize(channel.sendMessage(render(selection)), selection);
    }

    public void showDialog(Message message, int selection) {
        if (selection < 1)
            selection = 1;
        else if (selection > choices.size())
            selection = choices.size();
        initialize(message.editMessage(render(selection)), selection);
    }

    private void initialize(RestAction<Message> action, int selection) {
        action.queue(m -> {
            if (choices.size() > 1) {
                m.addReaction(UP).queue();
                m.addReaction(SELECT).queue();
                m.addReaction(CANCEL).queue();
                m.addReaction(DOWN).queue(v -> selectionDialog(m, selection), v -> selectionDialog(m, selection));
            } else {
                m.addReaction(SELECT).queue();
                m.addReaction(CANCEL).queue(v -> selectionDialog(m, selection), v -> selectionDialog(m, selection));
            }
        });
    }

    private void selectionDialog(Message message, int selection) {
        waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
            if (!event.getMessageId().equals(message.getId()))
                return false;
            if (!(UP.equals(event.getReaction().getReactionEmote().getName())
                || DOWN.equals(event.getReaction().getReactionEmote().getName())
                || CANCEL.equals(event.getReaction().getReactionEmote().getName())
                || SELECT.equals(event.getReaction().getReactionEmote().getName())))
                return false;
            return isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
        }, event -> {
            int newSelection = selection;
            switch (event.getReaction().getReactionEmote().getName()) {
                case UP -> {
                    if (newSelection > 1)
                        newSelection--;
                    else if (loop)
                        newSelection = choices.size();
                }
                case DOWN -> {
                    if (newSelection < choices.size())
                        newSelection++;
                    else if (loop)
                        newSelection = 1;
                }
                case SELECT -> {
                    success.accept(message, selection);
                    if (singleSelectionMode)
                        return;
                }
                case CANCEL -> {
                    cancel.accept(message);
                    return;
                }
            }
            try {
                event.getReaction().removeReaction(event.getUser()).queue();
            } catch (PermissionException ignored) {}
            int n = newSelection;
            message.editMessage(render(n)).queue(m -> selectionDialog(m, n));
        }, timeout, unit, () -> cancel.accept(message));
    }

    private Message render(int selection) {
        StringBuilder sbuilder = new StringBuilder();
        selection--;
        for (int i = 0; i < choices.size(); i++)
            if (i == selection)
                sbuilder.append("\n").append(leftEnd).append(choices.get(i)).append(rightEnd);
            else
                sbuilder.append("\n").append(defaultLeft).append(choices.get(i)).append(defaultRight);
        MessageBuilder mbuilder = new MessageBuilder();
        String content = text.apply(++selection);
        if (content != null)
            mbuilder.append(content);
        return mbuilder.setEmbeds(new EmbedBuilder()
            .setColor(color.apply(selection))
            .setDescription(sbuilder.toString())
            .build()).build();
    }

    public static class Builder extends Menu.Builder<Builder, SelectionDialog> {
        private final List<String> choices = new LinkedList<>();
        private String leftEnd = "";
        private String rightEnd = "";
        private String defaultLeft = "";
        private String defaultRight = "";
        private Function<Integer, Color> color = i -> null;
        private boolean loop = true;
        private Function<Integer, String> text = i -> null;
        private BiConsumer<Message, Integer> selection;
        private Consumer<Message> cancel = (m) -> {};
        private boolean singleSelectionMode;

        @Override
        public SelectionDialog build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(selection != null, "Must provide a selection consumer");

            return new SelectionDialog(waiter, users, roles, timeout, unit, choices, leftEnd, rightEnd,
                defaultLeft, defaultRight, color, loop, selection, cancel, text, singleSelectionMode);
        }

        public Builder setColor(Color c) {
            color = i -> c;
            return this;
        }

        public Builder setColor(Function<Integer, Color> c) {
            color = c;
            return this;
        }

        public Builder setText(String t) {
            text = i -> t;
            return this;
        }

        public Builder setText(Function<Integer, String> t) {
            text = t;
            return this;
        }

        public Builder setSelectedEnds(String left, String right) {
            leftEnd = left;
            rightEnd = right;
            return this;
        }

        public Builder setDefaultEnds(String left, String right) {
            defaultLeft = left;
            defaultRight = right;
            return this;
        }

        public Builder useLooping(boolean l) {
            loop = l;
            return this;
        }

        public Builder useSingleSelectionMode(boolean b) {
            singleSelectionMode = b;
            return this;
        }

        public Builder setSelectionConsumer(BiConsumer<Message, Integer> consumer) {
            selection = consumer;
            return this;
        }

        public Builder setCanceled(Consumer<Message> consumer) {
            cancel = consumer;
            return this;
        }

        public Builder clearChoices() {
            choices.clear();
            return this;
        }

        public Builder setChoices(String... c) {
            choices.clear();
            choices.addAll(Arrays.asList(c));
            return this;
        }

        public Builder addChoices(String... c) {
            choices.addAll(Arrays.asList(c));
            return this;
        }
    }
}
