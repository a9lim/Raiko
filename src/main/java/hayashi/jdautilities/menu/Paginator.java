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
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.utils.Checks;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class Paginator extends DirectionalMenu {
    private final int columns, itemsPerPage;
    private final boolean showPageNumbers, numberItems;
    private final List<String> strings;

    Paginator(EventWaiter waiter, Set<User> users, Set<Role> roles, long timeout, TimeUnit unit,
              BiFunction<Integer, Integer, Color> c, BiFunction<Integer, Integer, String> function,
              Consumer<Message> consumer, int i, int i1, boolean b,
              boolean b1, List<String> items, boolean b2, int i2,
              boolean b3, String lt, String rt, boolean b4) {
        super(waiter, users, roles, timeout, unit, c, function, consumer,
                (int) Math.ceil((double) items.size() / i1), b2, i2, b3, lt, rt, b4);
        columns = i;
        itemsPerPage = i1;
        showPageNumbers = b;
        numberItems = b1;
        strings = items;
    }

    protected MessageEditData renderPage(int pageNum) {
        MessageEditBuilder mbuilder = new MessageEditBuilder();
        EmbedBuilder ebuilder = new EmbedBuilder();
        int start = (pageNum - 1) * itemsPerPage;
        int end = Math.min(strings.size(), pageNum * itemsPerPage);
        if (columns == 1) {
            StringBuilder sbuilder = new StringBuilder();
            for (int i = start; i < end; i++)
                sbuilder.append("\n").append(numberItems ? "`" + (i + 1) + ".` " : "").append(strings.get(i));
            ebuilder.setDescription(sbuilder.toString());
        } else {
            int per = (int) Math.ceil((double) (end - start) / columns);
            for (int k = 0; k < columns; k++) {
                StringBuilder strbuilder = new StringBuilder();
                for (int i = start + k * per; i < end && i < start + (k + 1) * per; i++)
                    strbuilder.append("\n").append(numberItems ? (i + 1) + ". " : "").append(strings.get(i));
                ebuilder.addField("", strbuilder.toString(), true);
            }
        }

        ebuilder.setColor(color.apply(pageNum, pages));
        if (showPageNumbers)
            ebuilder.setFooter("Page " + pageNum + "/" + pages, null);
        mbuilder.setEmbeds(ebuilder.build());
        if (text != null)
            mbuilder.setContent(text.apply(pageNum, pages));
        return mbuilder.build();
    }

    public static class Builder extends DirectionalMenu.Builder<Builder, Paginator> {
        private int columns = 1;
        private int itemsPerPage = 12;
        private boolean numberItems;

        @Override
        public Paginator build() {
            Checks.check(waiter != null, "Must set an EventWaiter");
            Checks.check(!strings.isEmpty(), "Must include at least one item to paginate");

            return new Paginator(waiter, users, roles, timeout, unit, color, text, finalAction,
                columns, itemsPerPage, showPageNumbers, numberItems, strings, waitOnSinglePage,
                bulkSkipNumber, wrapPageEnds, textToLeft, textToRight, allowTextInput);
        }
        public Builder setColumns(int i) {
            if (i < 1 || i > 3)
                throw new IllegalArgumentException("Only 1, 2, or 3 columns are supported");
            columns = i;
            return this;
        }

        public Builder setItemsPerPage(int num) {
            if (num < 1)
                throw new IllegalArgumentException("There must be at least one item per page");
            itemsPerPage = num;
            return this;
        }
        public Builder useNumberedItems(boolean number) {
            numberItems = number;
            return this;
        }
    }
}
