/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hayashi.raiko.commands.owner;

import java.io.IOException;
import java.io.InputStream;

import hayashi.jdautilities.command.CommandEvent;
import hayashi.raiko.Bot;
import hayashi.raiko.commands.OwnerCommand;
import hayashi.raiko.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Icon;

public class SetavatarCmd extends OwnerCommand {
    public SetavatarCmd(Bot bot) {
        name = "setavatar";
        help = "sets the avatar of the bot";
        arguments = "<url>";
        aliases = bot.getConfig().getAliases(name);
        guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String url;
        if (event.getArgs().isEmpty())
            if (!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage())
                url = event.getMessage().getAttachments().get(0).getUrl();
            else
                url = null;
        else
            url = event.getArgs();
        InputStream s = OtherUtil.imageFromUrl(url);
        if (s == null) {
            event.replyError(" Invalid or missing URL");
            return;
        }
        try {
            event.getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                    v -> event.replySuccess(" Successfully changed avatar."),
                    t -> event.replyError(" Failed to set avatar."));
        } catch (IOException e) {
            event.replyError(" Could not load from provided URL.");
        }
    }
}
