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
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Slideshow extends DirectionalMenu {
    private final BiFunction<Integer, Integer, String> description;
    private final boolean showPageNumbers;
    private final List<String> strings;

    Slideshow(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
              BiFunction<Integer, Integer, Color> c, BiFunction<Integer, Integer, String> t,
              BiFunction<Integer, Integer, String> d, Consumer<Message> consumer,
              boolean spn, List<String> items, boolean b,
              int i, boolean b1, String lt, String rt,
              boolean b2) {
        super(waiter, users, roles, timeout, unit, c, t, consumer,
                items.size(), b, i, b1, lt, rt, b2);
        description = d;
        showPageNumbers = spn;
        strings = items;
    }


    public void paginate(MessageChannel channel, int pageNum) {
        pages = strings.size();
        super.paginate(channel,pageNum);
    }

    public void paginate(Message message, int pageNum) {
        pages = strings.size();
        super.paginate(message,pageNum);
    }

    protected void initialize(RestAction<Message> action, int pageNum) {
        pages = strings.size();
        super.initialize(action,pageNum);
    }

    protected void paginationWithTextInput(Message message, int pageNum) {
        pages = strings.size();
        super.paginationWithTextInput(message,pageNum);
    }

    protected void paginationWithoutTextInput(Message message, int pageNum) {
        pages = strings.size();
        super.paginationWithTextInput(message,pageNum);
    }

    // Private method that handles MessageReactionAddEvents
    protected void handleMessageReactionAddAction(MessageReactionAddEvent event, Message message, int pageNum) {
        pages = strings.size();
        super.handleMessageReactionAddAction(event,message,pageNum);
    }

    protected MessageEditData renderPage(int pageNum) {
        MessageEditBuilder mbuilder = new MessageEditBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();
        ebuilder.setImage(strings.get(pageNum - 1));
        ebuilder.setColor(color.apply(pageNum, strings.size()));
        ebuilder.setDescription(description.apply(pageNum, strings.size()));
        if (showPageNumbers)
            ebuilder.setFooter("Image " + pageNum + "/" + strings.size(), null);
        mbuilder.setEmbeds(ebuilder.build());
        if (text != null)
            mbuilder.setContent(text.apply(pageNum, strings.size()));
        return mbuilder.build();
    }

    public static class Builder extends DirectionalMenu.Builder<Builder, Slideshow> {
        private BiFunction<Integer, Integer, String> description = (page, pages) -> null;

        @Override
        public Slideshow build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!strings.isEmpty(), "Must include at least one item to paginate");

            return new Slideshow(
                waiter, users, roles, timeout, unit, color, text, description, finalAction,
                showPageNumbers, strings, waitOnSinglePage, bulkSkipNumber, wrapPageEnds,
                textToLeft, textToRight, allowTextInput);
        }

        public Builder setDescription(String d) {
            description = (i0, i1) -> d;
            return this;
        }

        public Builder setDescription(BiFunction<Integer, Integer, String> descriptionBiFunction) {
            description = descriptionBiFunction;
            return this;
        }
    }

}
