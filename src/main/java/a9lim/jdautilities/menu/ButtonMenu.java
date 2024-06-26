// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
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

package a9lim.jdautilities.menu;

import a9lim.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
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
import java.util.function.Consumer;
public class ButtonMenu extends Menu {
    private final Color color;
    private final String text, description;
    private final List<Emoji> choices;
    private final Consumer<Emoji> action;
    private final Consumer<Message> finalAction;

    ButtonMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
               Color incolor, String intext, String desc, List<Emoji> inchoices, Consumer<Emoji> inaction, Consumer<Message> infinalAction) {
        super(waiter, users, roles, timeout, unit);
        color = incolor;
        text = intext;
        description = desc;
        choices = inchoices;
        action = inaction;
        finalAction = infinalAction;
    }

    @Override
    public void display(MessageChannel channel) {
        initialize(channel.sendMessage(MessageCreateData.fromEditData(getMessage())));
    }

    @Override
    public void display(Message message) {
        initialize(message.editMessage(getMessage()));
    }

    // Initializes the ButtonMenu using a Message RestAction
    // This is either through editing a previously existing Message
    // OR through sending a new one to a TextChannel.
    private void initialize(RestAction<Message> ra) {
        ra.queue(m -> {
            int i = 1;
            for (Emoji emote : choices) {
                // Get the emote to display.
                RestAction<Void> r = m.addReaction(emote);
                if (i++ < choices.size()) {
                    r.queue(); // If there is still more reactions to add we delay using the EventWaiter
                    continue;
                }
                // This is the last reaction added.
                r.queue(v -> waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
                    // If the message is not the same as the ButtonMenu
                    // currently being displayed.

                    // If the reaction is an Emote we get the Snowflake,
                    // otherwise we get the unicode value.

                    // If the value we got is not registered as a button to
                    // the ButtonMenu being displayed we return false.

                    // Last check is that the person who added the reaction
                    // is a valid user.
                    return event.getMessageId().equals(m.getId()) &&
                            choices.contains(event.getReaction().getEmoji()) &&
                            isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
                }, (MessageReactionAddEvent event) -> {
                    // What happens next is after a valid event
                    // is fired and processed above.

                    // Preform the specified action with the ReactionEmote
                    action.accept(event.getReaction().getEmoji());
                    finalAction.accept(m);
                }, timeout, unit, () -> finalAction.accept(m)));
            }
        });
    }

    // Generates a ButtonMenu message
    private MessageEditData getMessage() {
        MessageEditBuilder mbuilder = new MessageEditBuilder();
        if (text != null)
            mbuilder.setContent(text);
        if (description != null)
            mbuilder.setEmbeds(new EmbedBuilder().setColor(color).setDescription(description).build());
        return mbuilder.build();
    }

    public static class Builder extends Menu.Builder<Builder, ButtonMenu> {
        private Color color;
        private String text, description;
        private final List<Emoji> choices = new LinkedList<>();
        private Consumer<Emoji> action;
        private Consumer<Message> finalAction = (m) -> {};

        @Override
        public ButtonMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check(text != null || description != null, "Either text or description must be set");

            return new ButtonMenu(waiter, users, roles, timeout, unit, color, text, description, choices, action, finalAction);
        }

        public Builder setColor(Color c) {
            color = c;
            return this;
        }

        public Builder setText(String t) {
            text = t;
            return this;
        }

        public Builder setDescription(String desc) {
            description = desc;
            return this;
        }

        public Builder setAction(Consumer<Emoji> consumer) {
            action = consumer;
            return this;
        }

        public Builder setFinalAction(Consumer<Message> consumer) {
            finalAction = consumer;
            return this;
        }

        public Builder addChoice(Emoji emoji) {
            choices.add(emoji);
            return this;
        }

        public Builder addChoices(Emoji... emojis) {
            for (Emoji emoji : emojis)
                addChoice(emoji);
            return this;
        }

        public Builder setChoices(Emoji... emojis) {
            choices.clear();
            return addChoices(emojis);
        }
    }
}