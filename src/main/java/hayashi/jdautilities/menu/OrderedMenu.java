// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>
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

package hayashi.jdautilities.menu;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OrderedMenu extends Menu {
    private final Color color;
    private final String text, description;
    private final List<String> choices;
    private final BiConsumer<Message, Integer> action;
    private final Consumer<Message> cancel;
    private final boolean useLetters, allowTypedInput, useCancel;

    public final static Emoji[] NUMBERS = {Emoji.fromUnicode("1\u20E3"),
            Emoji.fromUnicode("2\u20E3"), Emoji.fromUnicode("3\u20E3"),
            Emoji.fromUnicode("4\u20E3"), Emoji.fromUnicode("5\u20E3"),
            Emoji.fromUnicode("6\u20E3"), Emoji.fromUnicode("7\u20E3"),
            Emoji.fromUnicode("8\u20E3"), Emoji.fromUnicode("9\u20E3"),
            Emoji.fromUnicode("\uD83D\uDD1F")};

    public final static Emoji[] LETTERS = {Emoji.fromUnicode("\uD83C\uDDE6"),
            Emoji.fromUnicode("\uD83C\uDDE7"), Emoji.fromUnicode("\uD83C\uDDE8"),
            Emoji.fromUnicode("\uD83C\uDDE9"), Emoji.fromUnicode("\uD83C\uDDEA"),
            Emoji.fromUnicode("\uD83C\uDDEB"), Emoji.fromUnicode("\uD83C\uDDEC"),
            Emoji.fromUnicode("\uD83C\uDDED"), Emoji.fromUnicode("\uD83C\uDDEE"),
            Emoji.fromUnicode("\uD83C\uDDEF")};

    public final static Emoji CANCEL = Emoji.fromUnicode("\u274C");

    OrderedMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
                Color c, String t, String d, List<String> ch, BiConsumer<Message, Integer> biConsumer,
                Consumer<Message> consumer, boolean b, boolean b1, boolean b2) {
        super(waiter, users, roles, timeout, unit);
        color = c;
        text = t;
        description = d;
        choices = ch;
        action = biConsumer;
        cancel = consumer;
        useLetters = b;
        allowTypedInput = b1;
        useCancel = b2;
    }

    @Override
    public void display(MessageChannel channel) {
        // This check is basically for whether or not the menu can even display.
        // Is from text channel
        // Does not allow typed input
        // Does not have permission to add reactions
        if (channel.getType() == ChannelType.TEXT
            && !allowTypedInput
            && !((TextChannel) channel).getGuild().getSelfMember().hasPermission((TextChannel) channel, Permission.MESSAGE_ADD_REACTION))
            throw new PermissionException("Must be able to add reactions if not allowing typed input!");
        initialize(channel.sendMessage(MessageCreateData.fromEditData(getMessage())));
    }

    @Override
    public void display(Message message) {
        // This check is basically for whether or not the menu can even display.
        // Is from text channel
        // Does not allow typed input
        // Does not have permission to add reactions
        if (message.getChannelType() == ChannelType.TEXT
            && !allowTypedInput
            && !message.getGuild().getSelfMember().hasPermission(message.getChannel().asTextChannel(), Permission.MESSAGE_ADD_REACTION))
            throw new PermissionException("Must be able to add reactions if not allowing typed input!");
        initialize(message.editMessage(getMessage()));
    }

    // Initializes the OrderedMenu using a Message RestAction
    // This is either through editing a previously existing Message
    // OR through sending a new one to a TextChannel.
    private void initialize(RestAction<Message> ra) {
        ra.queue(m -> {
            try {
                // From 0 until the number of choices.
                // The last run of this loop will be used to queue
                // the last reaction and possibly a cancel emoji
                // if useCancel was set true before this OrderedMenu
                // was built.
                for (int i = 0; i < choices.size(); i++) {
                    // If this is not the last run of this loop
                    if (i < choices.size() - 1) {
                        m.addReaction(getEmoji(i)).queue();
                        continue;
                    }
                    // If this is the last run of this loop

                    RestAction<Void> re = m.addReaction(getEmoji(i));
                    // If we're using the cancel function we want
                    // to add a "step" so we queue the last emoji being
                    // added and then make the RestAction to start waiting
                    // on the cancel reaction being added.
                    if (useCancel) {
                        re.queue(); // queue the last emoji
                        re = m.addReaction(CANCEL);
                    }
                    // queue the last emoji or the cancel button
                    re.queue(v -> {
                        // Depending on whether we are allowing text input,
                        // we call a different method.
                        if (allowTypedInput)
                            waitGeneric(m);
                        else
                            waitReactionOnly(m);
                    });
                }
            } catch (PermissionException ex) {
                // If there is a permission exception mid process, we'll still
                // attempt to make due with what we have.
                if (allowTypedInput)
                    waitGeneric(m);
                else
                    waitReactionOnly(m);
            }
        });
    }

    // Waits for either a button being pushed OR a typed input
    private void waitGeneric(Message m) {
        // Wait for a GenericMessageEvent
        waiter.waitForEvent(GenericMessageEvent.class, e -> {
            // If we're dealing with a message reaction being added we return whether it's valid
            // If we're dealing with a received message being added we return whether it's valid
            // Otherwise return false
            return (e instanceof MessageReactionAddEvent h && isValidReaction(m, h)) ||
                (e instanceof MessageReceivedEvent i && isValidMessage(m, i));
        }, e -> {
            m.delete().queue();
            // If it's a valid MessageReactionAddEvent
            if (e instanceof MessageReactionAddEvent event) {
                // Process which reaction it is
                if (event.getReaction().getEmoji().equals(CANCEL))
                    cancel.accept(m);
                else
                    // The int provided in the success consumer is not indexed from 0 to number of choices - 1,
                    // but from 1 to number of choices. So the first choice will correspond to 1, the second
                    // choice to 2, etc.
                    action.accept(m, getNumber(event.getReaction().getEmoji()));
            }
            // If it's a valid MessageReceivedEvent
            else if (e instanceof MessageReceivedEvent event) {
                // Get the number in the message and process
                int num = getMessageNumber(event.getMessage().getContentRaw());
                if (num < 0 || num > choices.size())
                    cancel.accept(m);
                else
                    action.accept(m, num);
            }
        }, timeout, unit, () -> cancel.accept(m));
    }

    // Waits only for reaction input
    private void waitReactionOnly(Message m) {
        // This one is only for reactions
        waiter.waitForEvent(MessageReactionAddEvent.class, e -> isValidReaction(m, e), e -> {
            m.delete().queue();
            if (e.getReaction().getEmoji().equals(CANCEL))
                cancel.accept(m);
            else
                // The int provided in the success consumer is not indexed from 0 to number of choices - 1,
                // but from 1 to number of choices. So the first choice will correspond to 1, the second
                // choice to 2, etc.
                action.accept(m, getNumber(e.getReaction().getEmoji()));
        }, timeout, unit, () -> cancel.accept(m));
    }

    // This is where the displayed message for the OrderedMenu is built.
    private MessageEditData getMessage() {
        MessageEditBuilder mbuilder = new MessageEditBuilder();
        if (text != null)
            mbuilder.setContent(text);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < choices.size(); i++)
            sb.append("\n").append(getEmoji(i).getFormatted()).append(" ").append(choices.get(i));
        mbuilder.setEmbeds(new EmbedBuilder().setColor(color)
            .setDescription(description == null ? sb.toString() : description + sb).build());
        return mbuilder.build();
    }

    private boolean isValidReaction(Message m, MessageReactionAddEvent e) {
        // The message is not the same message as the menu
        // The user is not valid
        if (!e.getMessageId().equals(m.getId()) || !isValidUser(e.getUser(), e.isFromGuild() ? e.getGuild() : null))
            return false;
        // The reaction is the cancel reaction
        if (e.getReaction().getEmoji().equals(CANCEL))
            return true;

        int num = getNumber(e.getReaction().getEmoji());
        return !(num < 0 || num > choices.size());
    }

    private boolean isValidMessage(Message m, MessageReceivedEvent e) {
        // If the channel is not the same channel
        // Otherwise if it's a valid user or not
        return e.getChannel().equals(m.getChannel()) && isValidUser(e.getAuthor(), e.isFromGuild() ? e.getGuild() : null);
    }

    private Emoji getEmoji(int number) {
        return useLetters ? LETTERS[number] : NUMBERS[number];
    }

    // Gets the number emoji by the name.
    // This is kinda the opposite of the getEmoji method
    // except it's implementation is to provide the number
    // to the selection consumer when a choice is made.


    // this should be cleanable
    private int getNumber(Emoji emoji) {
        Emoji[] array = useLetters ? LETTERS : NUMBERS;
        for (int i = 0; i < array.length; i++)
            if (array[i].equals(emoji))
                return i + 1;
        return -1;
    }

    private int getMessageNumber(String message) {
            // This doesn't look good, but bear with me for a second:
            // So the maximum number of letters you can have as reactions
            // is 10 (the maximum number of choices in general even).
            // If you look carefully, you'll see that a corresponds to the
            // index 1, b to the index 2, and so on.
        // The same as above applies here, albeit in a different way.
        return useLetters ? (message.length() == 1 ? " abcdefghij".indexOf(message.toLowerCase()) : -1) :
            (message.length() == 1 ? " 123456789".indexOf(message) : ("10".equals(message) ? 10 : -1));
    }

    public static class Builder extends Menu.Builder<Builder, OrderedMenu> {
        private Color color;
        private String text, description;
        private final List<String> choices = new LinkedList<>();
        private BiConsumer<Message, Integer> selection;
        private Consumer<Message> cancel = (m) -> {
        };
        private boolean useLetters, addCancel;
        private boolean allowTypedInput = true;

        @Override
        public OrderedMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(choices.size() <= 10, "Must have no more than ten choices");
            Checks.check(selection != null, "Must provide an selection consumer");
            Checks.check(text != null || description != null, "Either text or description must be set");
            return new OrderedMenu(waiter, users, roles, timeout, unit, color, text, description, choices,
                selection, cancel, useLetters, allowTypedInput, addCancel);
        }

        public Builder setColor(Color c) {
            color = c;
            return this;
        }

        public Builder useLetters() {
            useLetters = true;
            return this;
        }

        public Builder useNumbers() {
            useLetters = false;
            return this;
        }

        public Builder allowTextInput(boolean allow) {
            allowTypedInput = allow;
            return this;
        }

        public Builder useCancelButton(boolean use) {
            addCancel = use;
            return this;
        }

        public Builder setText(String t) {
            text = t;
            return this;
        }

        public Builder setDescription(String d) {
            description = d;
            return this;
        }

        public Builder setSelection(BiConsumer<Message, Integer> consumer) {
            selection = consumer;
            return this;
        }

        public Builder setCancel(Consumer<Message> consumer) {
            cancel = consumer;
            return this;
        }

        public Builder addChoice(String choice) {
            Checks.check(choices.size() < 10, "Cannot set more than 10 choices");

            choices.add(choice);
            return this;
        }

        public Builder addChoices(String... choices) {
            for (String choice : choices)
                addChoice(choice);
            return this;
        }

        public Builder setChoices(String... choices) {
            clearChoices();
            return addChoices(choices);
        }

        public Builder clearChoices() {
            choices.clear();
            return this;
        }
    }
}
