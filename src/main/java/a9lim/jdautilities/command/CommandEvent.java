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

package a9lim.jdautilities.command;

import a9lim.jdautilities.command.impl.CommandClientImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.utils.Checks;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class CommandEvent {
    private static final Pattern COMPILE = Pattern.compile("<a?:(.+):(\\d+)>");
    public static final int MAX_MESSAGES = 2;

    private final MessageReceivedEvent event;
    private String args;
    private final CommandClient client;

    public CommandEvent(MessageReceivedEvent e, String a, CommandClient c) {
        event = e;
        args = a == null ? "" : a;
        client = c;
    }

    public String getArgs() {
        return args;
    }

    void setArgs(String a) {
        args = a;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public CommandClient getClient() {
        return client;
    }

    public void linkId(Message message) {
        Checks.check(message.getAuthor().equals(getSelfUser()), "Attempted to link a Message who's author was not the bot!");
        ((CommandClientImpl) client).linkIds(event.getMessageIdLong(), message);
    }

    // functional calls

    // try not to convert messagecreatedata into these
    public void reply(String message) {
        sendMessage(event.getChannel(), message);
    }

    public void reply(String message, Consumer<Message> success) {
        sendMessage(event.getChannel(), message, success);
    }

    public void reply(String message, Consumer<Message> success, Consumer<Throwable> failure) {
        sendMessage(event.getChannel(), message, success, failure);
    }

    public void reply(MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue(m -> {
            if (event.isFromType(ChannelType.TEXT))
                linkId(m);
        });
    }

    public void reply(MessageEmbed embed, Consumer<Message> success) {
        event.getChannel().sendMessageEmbeds(embed).queue(m -> {
            if (event.isFromType(ChannelType.TEXT))
                linkId(m);
            success.accept(m);
        });
    }

    public void reply(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure) {
        event.getChannel().sendMessageEmbeds(embed).queue(m -> {
            if (event.isFromType(ChannelType.TEXT))
                linkId(m);
            success.accept(m);
        }, failure);
    }

    public void reply(MessageCreateData message) {
        event.getChannel().sendMessage(message).queue(m -> {
            if (event.isFromType(ChannelType.TEXT))
                linkId(m);
        });
    }

    public void reply(MessageCreateData message, Consumer<Message> success) {
        event.getChannel().sendMessage(message).queue(m -> {
            if (event.isFromType(ChannelType.TEXT))
                linkId(m);
            success.accept(m);
        });
    }

    public void reply(MessageCreateData message, Consumer<Message> success, Consumer<Throwable> failure) {
        event.getChannel().sendMessage(message).queue(m -> {
            if (event.isFromType(ChannelType.TEXT))
                linkId(m);
            success.accept(m);
        }, failure);
    }

    public void reply(File file, String filename) {
        event.getChannel().sendFiles(FileUpload.fromData(file, filename)).queue();
    }

    public void reply(String message, File file, String filename) {
        event.getChannel().sendFiles(FileUpload.fromData(file, filename)).setContent(message).queue();
    }

    public void replyFormatted(String format, Object... args) {
        sendMessage(event.getChannel(), String.format(format, args));
    }

    public void replyOrAlternate(MessageEmbed embed, String alternateMessage) {
        try {
            event.getChannel().sendMessageEmbeds(embed).queue();
        } catch (PermissionException e) {
            reply(alternateMessage);
        }
    }

    public void replyOrAlternate(String message, File file, String filename, String alternateMessage) {
        try {
            event.getChannel().sendFiles(FileUpload.fromData(file, filename)).setContent(message).queue();
        } catch (Exception e) {
            reply(alternateMessage);
        }
    }

    public void replyInDm(String message) {
        if (event.isFromType(ChannelType.PRIVATE))
            reply(message);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message));
    }

    public void replyInDm(String message, Consumer<Message> success) {
        if (event.isFromType(ChannelType.PRIVATE))
            reply(message, success);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message, success));
    }

    public void replyInDm(String message, Consumer<Message> success, Consumer<Throwable> failure) {
        if (event.isFromType(ChannelType.PRIVATE))
            reply(message, success, failure);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> sendMessage(pc, message, success, failure), failure);
    }

    public void replyInDm(MessageEmbed embed) {
        if (event.isFromType(ChannelType.PRIVATE))
            reply(embed);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());
    }

    public void replyInDm(MessageEmbed embed, Consumer<Message> success) {
        if (event.isFromType(ChannelType.PRIVATE))
            getPrivateChannel().sendMessageEmbeds(embed).queue(success);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue(success));
    }

    public void replyInDm(MessageEmbed embed, Consumer<Message> success, Consumer<Throwable> failure) {
        if (event.isFromType(ChannelType.PRIVATE))
            getPrivateChannel().sendMessageEmbeds(embed).queue(success, failure);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue(success, failure), failure);
    }

    public void replyInDm(MessageCreateData message) {
        if (event.isFromType(ChannelType.PRIVATE))
            reply(message);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue());
    }

    public void replyInDm(MessageCreateData message, Consumer<Message> success) {
        if (event.isFromType(ChannelType.PRIVATE))
            getPrivateChannel().sendMessage(message).queue(success);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue(success));
    }

    public void replyInDm(MessageCreateData message, Consumer<Message> success, Consumer<Throwable> failure) {
        if (event.isFromType(ChannelType.PRIVATE))
            getPrivateChannel().sendMessage(message).queue(success, failure);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue(success, failure), failure);
    }

    public void replyInDm(String message, File file, String filename) {
        if (event.isFromType(ChannelType.PRIVATE))
            reply(message, file, filename);
        else
            event.getAuthor().openPrivateChannel().queue(pc -> pc.sendFiles(FileUpload.fromData(file, filename)).setContent(message).queue());
    }

    public void replySuccess(String message) {
        reply(client.getSuccess() + " " + message);
    }

    public void replySuccess(String message, Consumer<Message> queue) {
        reply(client.getSuccess() + " " + message, queue);
    }

    public void replyWarning(String message) {
        reply(client.getWarning() + " " + message);
    }

    public void replyWarning(String message, Consumer<Message> queue) {
        reply(client.getWarning() + " " + message, queue);
    }

    public void replyError(String message) {
        reply(client.getError() + " " + message);
    }

    public void replyError(String message, Consumer<Message> queue) {
        reply(client.getError() + " " + message, queue);
    }

    public void reactSuccess() {
        react(client.getSuccess());
    }

    public void reactWarning() {
        react(client.getWarning());
    }

    public void reactError() {
        react(client.getError());
    }

    public void async(Runnable runnable) {
        Checks.notNull(runnable, "Runnable");
        client.getScheduleExecutor().submit(runnable);
    }


    //private methods

    private void react(String reaction) {
        if (reaction != null && !reaction.isEmpty())
            try {
                event.getMessage().addReaction(Emoji.fromFormatted(COMPILE.matcher(reaction).replaceAll("$1:$2"))).queue();
            } catch (PermissionException ignored) {}
    }

    private void sendMessage(MessageChannel chan, String message) {
        ArrayList<String> messages = splitMessage(message);
        int min = Math.min(MAX_MESSAGES,messages.size());
        for (int i = 0; i < min; i++) {
            chan.sendMessage(messages.get(i)).queue(m -> {
                if (event.isFromType(ChannelType.TEXT))
                    linkId(m);
            });
        }
    }

    private void sendMessage(MessageChannel chan, String message, Consumer<Message> success) {
        ArrayList<String> messages = splitMessage(message);
        int mindec = Math.min(MAX_MESSAGES,messages.size())-1;
        for (int i = 0; i <= mindec; i++) {
            if (i == mindec) {
                chan.sendMessage(messages.get(i)).queue(m -> {
                    if (event.isFromType(ChannelType.TEXT))
                        linkId(m);
                    success.accept(m);
                });
            } else {
                chan.sendMessage(messages.get(i)).queue(m -> {
                    if (event.isFromType(ChannelType.TEXT))
                        linkId(m);
                });
            }
        }
    }

    private void sendMessage(MessageChannel chan, String message, Consumer<Message> success, Consumer<Throwable> failure) {
        ArrayList<String> messages = splitMessage(message);
        int mindec = Math.min(MAX_MESSAGES,messages.size())-1;
        for (int i = 0; i <= mindec; i++) {
            if (i == mindec) {
                chan.sendMessage(messages.get(i)).queue(m -> {
                    if (event.isFromType(ChannelType.TEXT))
                        linkId(m);
                    success.accept(m);
                }, failure);
            } else {
                chan.sendMessage(messages.get(i)).queue(m -> {
                    if (event.isFromType(ChannelType.TEXT))
                        linkId(m);
                });
            }
        }
    }

    public static ArrayList<String> splitMessage(String stringtoSend) {
        ArrayList<String> msgs = new ArrayList<>();
        if (stringtoSend != null) {
            stringtoSend = stringtoSend.replace("@everyone", "@\u0435veryone").replace("@here", "@h\u0435re").trim();
            while (stringtoSend.length() > 2000) {
                int leeway = 2000 - (stringtoSend.length() % 2000);
                int index = stringtoSend.lastIndexOf('\n', 2000);
                if (index < leeway)
                    index = stringtoSend.lastIndexOf(' ', 2000);
                if (index < leeway)
                    index = 2000;
                String temp = stringtoSend.substring(0, index).trim();
                if (!temp.isEmpty())
                    msgs.add(temp);
                stringtoSend = stringtoSend.substring(index).trim();
            }
            if (!stringtoSend.isEmpty())
                msgs.add(stringtoSend);
        }
        return msgs;
    }


    // custom shortcuts

    public SelfUser getSelfUser() {
        return event.getJDA().getSelfUser();
    }

    public Member getSelfMember() {
        return event.getGuild().getSelfMember();
    }

    public boolean isOwner() {
        if (event.getAuthor().getIdLong() == client.getOwnerId())
            return true;
        if (client.getCoOwnerIds() == null)
            return false;
        for (long id : client.getCoOwnerIds())
            if (id == event.getAuthor().getIdLong())
                return true;
        return false;
    }

    // shortcuts

    public User getAuthor() {
        return event.getAuthor();
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }

    public ChannelType getChannelType() {
        return event.getChannelType();
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public JDA getJDA() {
        return event.getJDA();
    }

    public Member getMember() {
        return event.getMember();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public PrivateChannel getPrivateChannel() {
        return event.getChannel().asPrivateChannel();
    }

    public long getResponseNumber() {
        return event.getResponseNumber();
    }

    public TextChannel getTextChannel() {
        return event.getChannel().asTextChannel();
    }

    public boolean isFromType(ChannelType channelType) {
        return event.isFromType(channelType);
    }
}
