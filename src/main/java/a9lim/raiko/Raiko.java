// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2016 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

package a9lim.raiko;

import a9lim.jdautilities.command.CommandClient;
import a9lim.jdautilities.command.CommandClientBuilder;
import a9lim.jdautilities.commons.waiter.EventWaiter;
import a9lim.raiko.chat.ChatBot;
import a9lim.raiko.commands.BotCommand;
import a9lim.raiko.commands.ChatCommand;
import a9lim.raiko.commands.admin.PrefixCmd;
import a9lim.raiko.commands.admin.SettcCmd;
import a9lim.raiko.commands.admin.SetvcCmd;
import a9lim.raiko.commands.chat.*;
import a9lim.raiko.commands.general.*;
import a9lim.raiko.commands.music.*;
import a9lim.raiko.commands.owner.*;
import a9lim.raiko.entities.Prompt;
import a9lim.raiko.gui.GUI;
import a9lim.raiko.settings.SettingsManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.Arrays;

public class Raiko {
    public final static Logger LOG = LoggerFactory.getLogger(Raiko.class);
    public final static Permission[] RECOMMENDED_PERMS = {Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI,
            Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE};
    public final static GatewayIntent[] INTENTS = {GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EMOJIS_AND_STICKERS};

    public static void main(String[] args) {
        if (args.length > 0 && "generate-config".equalsIgnoreCase(args[0])) {
            BotConfig.writeDefaultConfig();
            return;
        }
        startBot();
    }

    private static void startBot() {
        // create prompt to handle startup
        Prompt prompt = new Prompt("Raiko");

        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if (!config.isValid())
            return;
        LOG.info("loaded config from " + config.getConfigLocation());

        // set up the listener
        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        Bot bot = new Bot(waiter, config, settings);
        CommandClient client = createCommandClient(config, settings, bot);

        if (!prompt.isNoGUI()) {
            try {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } catch (Exception e) {
                LOG.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }

        // attempt to log in and start
        try {
            bot.setJDA(JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOJI, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
                    .addEventListeners(client, waiter, new Listener(bot))
                    .setBulkDeleteSplittingEnabled(true).build());
        } catch (IllegalArgumentException ex) {
            prompt.alert(Prompt.Level.ERROR, "Raiko", "Some aspect of the configuration is "
                    + "invalid: " + ex + "\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        } catch (ErrorResponseException ex) {
            prompt.alert(Prompt.Level.ERROR, "Raiko", ex + "\nInvalid reponse returned when "
                    + "attempting to connect, please make sure you're connected to the internet");
            System.exit(1);
        }
    }
    private static CommandClient createCommandClient(BotConfig config, SettingsManager settings, Bot bot){
        AboutCmd aboutCmd = new AboutCmd(Color.BLUE.brighter(),
                "Touhou-themed Music Bot",
                new String[]{"High-quality (Touhou) music playback", "DoubleDealingQueue™ Technology (Literally just a deque)", "(Somewhat) Easy to host yourself"},
                RECOMMENDED_PERMS);
        aboutCmd.setIsAuthor(false);
        aboutCmd.setReplacementCharacter("\uD83C\uDFB6"); // 🎶

        BotCommand.setBot(bot);

        // set up the command client
        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefixes(config.getPrefixes())
                .setOwnerId(config.getOwnerId())
                .setEmojis(config.getSuccess(), config.getWarning(), config.getError())
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(settings)
                .addCommands(aboutCmd,
                        new PingCmd(),
                        new SettingsCmd(),
                        new EchoCmd(),
                        new RollCmd(),

                        new NowplayingCmd(),
                        new PlayCmd(),
                        new PlaylistsCmd(),
                        new QueueCmd(),
                        new RemoveCmd(),
                        new SearchCmd(),
                        new SCSearchCmd(),
                        new ShuffleCmd(),
                        new SkipCmd(),
                        new MoveTrackCmd(),
                        new PauseCmd(),
                        new PlaynextCmd(),
                        new RepeatCmd(),
                        new StopCmd(),
                        new VolumeCmd(),
                        new SwapTrackCmd(),
                        new ReverseQueueCmd(),
                        new SeekCmd(),

                        new PrefixCmd(),
                        new SettcCmd(),
                        new SetvcCmd(),

                        new AutoplaylistCmd(),
                        new DebugCmd(),
                        new PlaylistCmd(),
                        new SetavatarCmd(),
                        new SetgameCmd(),
                        new SetnameCmd(),
                        new SetstatusCmd(),
                        new ShutdownCmd());

        if (config.getCgpttoken() != null) {
            ChatCommand.setChatBot(new ChatBot(config));
            cb.addCommands(
                    new ChatCmd(),
                    new ClearChatCmd(),
                    new ToggleModelCmd(),
                    new RemoveChatCmd(),
                    new RewindChatCmd(),
                    new SetPrepromptCmd());
        }

        cb.setStatus(config.getStatus());
        cb.setActivity(config.getGame());
        return cb.build();
    }
}
