// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright 2018 John Grosh (jagrosh) <john.a.grosh@gmail.com>.
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

import a9lim.jdautilities.command.AliasSource;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import a9lim.raiko.entities.Prompt;
import a9lim.raiko.utils.FormatUtil;
import a9lim.raiko.utils.OtherUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BotConfig implements AliasSource {
    private final Prompt prompt;
    private final static String CONTEXT = "Config";
    private final static String START_TOKEN = "/// START OF RAIKO CONFIG ///";
    private final static String END_TOKEN = "/// END OF RAIKO CONFIG ///";

    private Path path;
    private String token, playlistsFolder,
            successEmoji, warningEmoji, errorEmoji, loadingEmoji, searchingEmoji,
            cgpttoken, preprompt, ytemail, ytpw;
    private List<String> prefixes;
    private boolean stayInChannel, songInGame, npImages,
            model, valid;
    private long owner, maxSeconds, aloneTimeUntilStop;
    private OnlineStatus status;
    private Activity game;
    private Config aliases;

    public BotConfig(Prompt p) {
        prompt = p;
    }

    public void load() {
        valid = false;

        // read config from file
        try {
            // get the path to the config, default config.txt
            path = getConfigPath();

            // load in the config file, plus the default values
            //Config config = ConfigFactory.parseFile(path.toFile()).withFallback(ConfigFactory.load());
            Config config = ConfigFactory.load();

            // set values
            token = config.getString("token");
            prefixes = config.getStringList("prefixes");
            owner = config.getLong("owner");
            successEmoji = config.getString("success");
            warningEmoji = config.getString("warning");
            errorEmoji = config.getString("error");
            loadingEmoji = config.getString("loading");
            searchingEmoji = config.getString("searching");
            game = OtherUtil.parseGame(config.getString("game"));
            status = OtherUtil.parseStatus(config.getString("status"));
            stayInChannel = config.getBoolean("stayinchannel");
            songInGame = config.getBoolean("songinstatus");
            npImages = config.getBoolean("npimages");
            maxSeconds = config.getLong("maxtime");
            aloneTimeUntilStop = config.getLong("alonetimeuntilstop");
            playlistsFolder = config.getString("playlistsfolder");
            aliases = config.getConfig("aliases");

            cgpttoken = noneblank(config.getString("gpttoken"));
            model = config.getBoolean("cheapmodel");
            preprompt = config.getString("preprompt");

            // we may need to write a new config file
            boolean write = false;

            // validate bot token
            if (token == null || token.isEmpty() || "BOT_TOKEN_HERE".equalsIgnoreCase(token)) {
                token = prompt.prompt("""
                        Please provide a bot token.
                        Instructions for obtaining a token can be found here:
                        https://github.com/jagrosh/MusicBot/wiki/Getting-a-Bot-Token.
                        Bot Token:\s""");
                if (token == null) {
                    prompt.alert(Prompt.Level.WARNING, CONTEXT, "No token provided! Exiting.\n\nConfig Location: " + path.toAbsolutePath());
                    return;
                } else {
                    write = true;
                }
            }

            // validate bot owner
            if (owner <= 0) {
                try {
                    owner = Long.parseLong(prompt.prompt("""
                            Owner ID was missing, or the provided owner ID is not valid.
                            Please provide the User ID of the bot's owner.
                            Instructions for obtaining your User ID can be found here:
                            https://github.com/jagrosh/MusicBot/wiki/Finding-Your-User-ID
                            Owner User ID:\s"""));
                } catch (NumberFormatException | NullPointerException ex) {
                    owner = 0;
                }
                if (owner <= 0) {
                    prompt.alert(Prompt.Level.ERROR, CONTEXT, "Invalid User ID! Exiting.\n\nConfig Location: " + path.toAbsolutePath());
                    return;
                }
                write = true;
            }

            if (write)
                writeToFile();

            // if we get through the whole config, it's good to go
            valid = true;

        } catch (ConfigException ex) {
            prompt.alert(Prompt.Level.ERROR, CONTEXT, ex + ": " + ex.getMessage() + "\n\nConfig Location: " + path.toAbsolutePath());
        }
    }

    //todo: fix this

    private void writeToFile() {
        byte[] bytes = loadDefaultConfig().replace("BOT_TOKEN_HERE", token)
                .replace("0 // OWNER ID", Long.toString(owner))
                .trim().getBytes();
        try {
            Files.write(path, bytes);
        } catch (IOException ex) {
            prompt.alert(Prompt.Level.WARNING, CONTEXT, "Failed to write new config options to config.txt: " + ex
                    + "\nPlease make sure that the files are not on your desktop or some other restricted area.\n\nConfig Location: "
                    + path.toAbsolutePath());
        }
    }

    //uhh
    private static String loadDefaultConfig() {
        String original = OtherUtil.loadResource("/reference.conf");
        return original == null
                ? "token = BOT_TOKEN_HERE\r\nowner = 0 // OWNER ID"
                : original.substring(original.indexOf(START_TOKEN) + START_TOKEN.length(), original.indexOf(END_TOKEN)).trim();
    }

    private static Path getConfigPath() {
        Path path = OtherUtil.getPath(System.getProperty("config.file", System.getProperty("config", "config.txt")));
        if (path.toFile().exists()) {
            if (System.getProperty("config.file") == null)
                System.setProperty("config.file", System.getProperty("config", path.toAbsolutePath().toString()));
            ConfigFactory.invalidateCaches();
        }
        return path;
    }

    public static void writeDefaultConfig() {
        Prompt prompt = new Prompt(null, null, true, true);
        prompt.alert(Prompt.Level.INFO, "Raiko Config", "Generating default config file");
        Path path = getConfigPath();
        try {
            prompt.alert(Prompt.Level.INFO, "Raiko Config", "Writing default config file to " + path.toAbsolutePath());
            Files.write(path, loadDefaultConfig().getBytes());
        } catch (Exception ex) {
            prompt.alert(Prompt.Level.ERROR, "Raiko Config", "An error occurred writing the default config file: " + ex.getMessage());
        }
    }

    public static String noneblank(String s){
        return "none".equalsIgnoreCase(s) || s.isBlank() ? null : s;
    }
    public boolean isValid() {
        return valid;
    }

    public String getConfigLocation() {
        return path.toFile().getAbsolutePath();
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public String getToken() {
        return token;
    }

    public long getOwnerId() {
        return owner;
    }

    public String getSuccess() {
        return successEmoji;
    }

    public String getWarning() {
        return warningEmoji;
    }

    public String getError() {
        return errorEmoji;
    }

    public String getLoading() {
        return loadingEmoji;
    }

    public String getSearching() {
        return searchingEmoji;
    }

    public Activity getGame() {
        return game;
    }

    public OnlineStatus getStatus() {
        return status;
    }

    public boolean getStay() {
        return stayInChannel;
    }

    public boolean getSongInStatus() {
        return songInGame;
    }

    public String getPlaylistsFolder() {
        return playlistsFolder;
    }

    public boolean useNPImages() {
        return npImages;
    }

    public long getMaxSeconds() {
        return maxSeconds;
    }

    public String getMaxTime() {
        return FormatUtil.formatTime(maxSeconds * 1000);
    }

    public long getAloneTimeUntilStop() {
        return aloneTimeUntilStop;
    }

    public boolean isTooLong(AudioTrack track) {
        return maxSeconds > 0 && (track.getDuration() / 1000 > maxSeconds);
    }

    public String[] getAliases(String command) {
        try {
            return aliases.getStringList(command).toArray(new String[0]);
        } catch (NullPointerException | ConfigException.Missing e) {
            return new String[0];
        }
    }

    public String getCgpttoken(){
        return cgpttoken;
    }

    public boolean getModel(){
        return model;
    }

    public String getPreprompt() {
        return preprompt;
    }

}
