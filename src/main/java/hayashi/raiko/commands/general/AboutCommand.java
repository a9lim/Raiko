package hayashi.raiko.commands.general;

import hayashi.jdautilities.command.Command;
import hayashi.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;

public class AboutCommand extends Command {
    private boolean IS_AUTHOR = true;
    private String REPLACEMENT_ICON = "+";
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private String oauthLink;
    private final String[] features;

    public AboutCommand(Color c, String s, String[] strings, Permission... permissions) {
        color = c;
        description = s;
        features = strings;
        name = "about";
        help = "shows info about the bot";
        guildOnly = false;
        perms = permissions;
        botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    public void setIsAuthor(boolean value) {
        IS_AUTHOR = value;
    }

    public void setReplacementCharacter(String value) {
        REPLACEMENT_ICON = value;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (oauthLink == null) {
            try {
                ApplicationInfo info = event.getJDA().retrieveApplicationInfo().complete();
                oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            } catch (Exception e) {
                LoggerFactory.getLogger("OAuth2").error("Could not generate invite link ", e);
                oauthLink = "";
            }
        }
        // fix this
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : color);
        builder.setAuthor("All about " + event.getSelfUser().getName() + "!", null, event.getSelfUser().getAvatarUrl());
        boolean join = !(event.getClient().getServerInvite() == null || event.getClient().getServerInvite().isEmpty());
        boolean inv = !oauthLink.isEmpty();
        StringBuilder descr = new StringBuilder().append("Hello! I am **").append(event.getSelfUser().getName()).append("**, ")
                .append(description).append("\nI ").append(IS_AUTHOR ? "was written in Java" : "am owned").append(" by **")
                .append(event.getJDA().getUserById(event.getClient().getOwnerId()) == null ? "<@" + event.getClient().getOwnerId() + ">"
                        : event.getJDA().getUserById(event.getClient().getOwnerId()).getName())
                .append("** using the [JDA library](https://github.com/DV8FromTheWorld/JDA) (")
                .append(JDAInfo.VERSION).append(")\nType `").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord())
                .append("` to see my commands!").append(join || inv ? "\n" + (join ? "Join my server [`here`](" + event.getClient().getServerInvite() + ")" : (inv ? "Please " : ""))
                        + (inv ? (join ? ", or " : "") + "[`invite`](" + oauthLink + ") me to your server" : "") + "!" : "").append("\n\nSome of my features include: ```css");
        for (String feature : features)
            descr.append("\n").append(!event.getClient().getSuccess().isEmpty() && event.getClient().getSuccess().charAt(0) == '<' ?
                    REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == null) {
            builder.addField("Stats", event.getJDA().getGuilds().size() + " servers\n1 shard", true);
            builder.addField("Users", event.getJDA().getUsers().size() + " unique\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total", true);
            builder.addField("Channels", event.getJDA().getTextChannels().size() + " Text\n" + event.getJDA().getVoiceChannels().size() + " Voice", true);
        } else {
            builder.addField("Stats", (event.getClient()).getTotalGuilds() + " Servers\nShard " + (event.getJDA().getShardInfo().getShardId() + 1)
                    + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("This shard", event.getJDA().getUsers().size() + " Users\n" + event.getJDA().getGuilds().size() + " Servers", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " Text Channels\n" + event.getJDA().getVoiceChannels().size() + " Voice Channels", true);
        }
        builder.setFooter("Last restart", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }

}
