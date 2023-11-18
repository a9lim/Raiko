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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import hayashi.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.internal.utils.Checks;

public class ButtonMenu extends Menu {
    private final Color color;
    private final String text, description;
    private final List<String> choices;
    private final Consumer<ReactionEmote> action;
    private final Consumer<Message> finalAction;

    ButtonMenu(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
               Color color, String text, String description, List<String> choices, Consumer<ReactionEmote> action, Consumer<Message> finalAction) {
        super(waiter, users, roles, timeout, unit);
        this.color = color;
        this.text = text;
        this.description = description;
        this.choices = choices;
        this.action = action;
        this.finalAction = finalAction;
    }

    @Override
    public void display(MessageChannel channel) {
        initialize(channel.sendMessage(getMessage()));
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
            for (int i = 0; i < choices.size(); i++) {
                // Get the emote to display.
                Emote emote;
                try {
                    emote = m.getJDA().getEmoteById(choices.get(i));
                } catch (Exception e) {
                    emote = null;
                }
                // If the emote is null that means that it might be an emoji.
                // If it's neither, that's on the developer and we'll let it
                // throw an error when we queue a rest action.
                RestAction<Void> r = emote == null ? m.addReaction(choices.get(i)) : m.addReaction(emote);
                if (i + 1 < choices.size()) {
                    r.queue(); // If there is still more reactions to add we delay using the EventWaiter
                    continue;
                }
                // This is the last reaction added.
                r.queue(v -> waiter.waitForEvent(MessageReactionAddEvent.class, event -> {
                    // If the message is not the same as the ButtonMenu
                    // currently being displayed.

                    // If the reaction is an Emote we get the Snowflake,
                    // otherwise we get the unicode value.
//                        String re = event.getReaction().getReactionEmote().isEmote()
//                                ? event.getReaction().getReactionEmote().getId()
//                                : event.getReaction().getReactionEmote().getName();

                    // If the value we got is not registered as a button to
                    // the ButtonMenu being displayed we return false.
//                        if (!choices.contains(re))
//                            return false;

                    // Last check is that the person who added the reaction
                    // is a valid user.
                    return event.getMessageId().equals(m.getId()) &&
                            choices.contains(event.getReaction().getReactionEmote().isEmote()
                            ? event.getReaction().getReactionEmote().getId()
                            : event.getReaction().getReactionEmote().getName()) &&
                            isValidUser(event.getUser(), event.isFromGuild() ? event.getGuild() : null);
                }, (MessageReactionAddEvent event) -> {
                    // What happens next is after a valid event
                    // is fired and processed above.

                    // Preform the specified action with the ReactionEmote
                    action.accept(event.getReaction().getReactionEmote());
                    finalAction.accept(m);
                }, timeout, unit, () -> finalAction.accept(m)));
            }
        });
    }

    // Generates a ButtonMenu message
    private Message getMessage() {
        MessageBuilder mbuilder = new MessageBuilder();
        if (text != null)
            mbuilder.append(text);
        if (description != null)
            mbuilder.setEmbeds(new EmbedBuilder().setColor(color).setDescription(description).build());
        return mbuilder.build();
    }

    public static class Builder extends Menu.Builder<Builder, ButtonMenu> {
        private Color color;
        private String text;
        private String description;
        private final List<String> choices = new LinkedList<>();
        private Consumer<ReactionEmote> action;
        private Consumer<Message> finalAction = (m) -> {
        };

        @Override
        public ButtonMenu build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!choices.isEmpty(), "Must have at least one choice");
            Checks.check(action != null, "Must provide an action consumer");
            Checks.check(text != null || description != null, "Either text or description must be set");

            return new ButtonMenu(waiter, users, roles, timeout, unit, color, text, description, choices, action, finalAction);
        }

        public Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setAction(Consumer<ReactionEmote> action) {
            this.action = action;
            return this;
        }

        public Builder setFinalAction(Consumer<Message> finalAction) {
            this.finalAction = finalAction;
            return this;
        }

        public Builder addChoice(String emoji) {
            this.choices.add(emoji);
            return this;
        }

        public Builder addChoice(Emote emote) {
            return addChoice(emote.getId());
        }

        public Builder addChoices(String... emojis) {
            for (String emoji : emojis)
                addChoice(emoji);
            return this;
        }

        public Builder addChoices(Emote... emotes) {
            for (Emote emote : emotes)
                addChoice(emote);
            return this;
        }

        public Builder setChoices(String... emojis) {
            this.choices.clear();
            return addChoices(emojis);
        }

        public Builder setChoices(Emote... emotes) {
            this.choices.clear();
            return addChoices(emotes);
        }
    }
}