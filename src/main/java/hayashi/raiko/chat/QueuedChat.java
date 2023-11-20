// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>.
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