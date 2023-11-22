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

package a9lim.raiko.playlist;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import a9lim.raiko.BotConfig;
import a9lim.raiko.utils.OtherUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static a9lim.jdautilities.command.Command.COMPILE;

public class PlaylistLoader {
    private final BotConfig config;

    public PlaylistLoader(BotConfig c) {
        config = c;
    }

    public List<String> getPlaylistNames() {
        if (folderExists()) {
            File folder = new File(OtherUtil.getPath(config.getPlaylistsFolder()).toString());
            return Arrays.stream(folder.listFiles((pathname) -> pathname.getName().endsWith(".txt"))).map(f -> f.getName().substring(0, f.getName().length() - 4)).collect(Collectors.toList());
        }
        createFolder();
        return Collections.emptyList();
    }

    public void createFolder() {
        try {
            Files.createDirectory(OtherUtil.getPath(config.getPlaylistsFolder()));
        } catch (IOException ignore) {}
    }

    public boolean folderExists() {
        return Files.exists(OtherUtil.getPath(config.getPlaylistsFolder()));
    }

    public void createPlaylist(String name) throws IOException {
        Files.createFile(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt"));
    }

    public void deletePlaylist(String name) throws IOException {
        Files.delete(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt"));
    }

    public void writePlaylist(String name, String text) throws IOException {
        Files.write(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt"), text.trim().getBytes());
    }

    public Playlist getPlaylist(String name) {
        if (!getPlaylistNames().contains(name))
            return null;
        try {
            if (folderExists()) {
                AtomicBoolean shuffle = new AtomicBoolean(false);
                List<String> list = new ArrayList<>();
                Files.readAllLines(OtherUtil.getPath(config.getPlaylistsFolder() + File.separator + name + ".txt")).forEach(str -> {
                    String s = str.trim();
                    if (s.isEmpty())
                        return;
                    if (s.charAt(0) == '#' || s.startsWith("//")) {
                        s = COMPILE.matcher(s).replaceAll("");
                        if ("#shuffle".equalsIgnoreCase(s) || "//shuffle".equalsIgnoreCase(s))
                            shuffle.set(true);
                    } else
                        list.add(s);
                });
                if (shuffle.get())
                    Collections.shuffle(list);
                return new Playlist(name, list, shuffle.get());
            }
            createFolder();
        } catch (IOException ignored) {}
        return null;
    }


    public final class Playlist {
        private final String name;
        private final List<String> items;
        private final boolean shuffle;
        private final List<AudioTrack> tracks = new LinkedList<>();
        private final List<PlaylistLoadError> errors = new LinkedList<>();
        private boolean loaded;

        private Playlist(String n, List<String> list, boolean b) {
            name = n;
            items = list;
            shuffle = b;
        }

        public void loadTracks(AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
            if (loaded)
                return;
            loaded = true;
            int i = 0;
            for (String item : items) {
                int finalI = i;
                boolean last = ++i == items.size();
                manager.loadItemOrdered(name, item, new AudioLoadResultHandler() {
                    private void done() {
                        if (last) {
                            if (shuffle)
                                Collections.shuffle(tracks);
                            if (callback != null)
                                callback.run();
                        }
                    }

                    @Override
                    public void trackLoaded(AudioTrack at) {
                        if (config.isTooLong(at))
                            errors.add(new PlaylistLoadError(finalI, item, "This track is longer than the allowed maximum"));
                        else {
                            at.setUserData(0L);
                            tracks.add(at);
                            consumer.accept(at);
                        }
                        done();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist ap) {
                        if (ap.isSearchResult()) {
                            trackLoaded(ap.getTracks().getFirst());
                        } else if (ap.getSelectedTrack() != null) {
                            trackLoaded(ap.getSelectedTrack());
                        } else {
                            List<AudioTrack> loaded = new ArrayList<>(ap.getTracks());
                            if (shuffle)
                                Collections.shuffle(loaded);
                            loaded.removeIf(config::isTooLong);
                            loaded.forEach(at -> at.setUserData(0L));
                            tracks.addAll(loaded);
                            loaded.forEach(consumer);
                        }
                        done();
                    }

                    @Override
                    public void noMatches() {
                        errors.add(new PlaylistLoadError(finalI, item, "No matches found."));
                        done();
                    }

                    @Override
                    public void loadFailed(FriendlyException fe) {
                        errors.add(new PlaylistLoadError(finalI, item, "Failed to load track: " + fe.getLocalizedMessage()));
                        done();
                    }
                });
            }
        }

        public String getName() {
            return name;
        }

        public List<String> getItems() {
            return items;
        }

        public List<AudioTrack> getTracks() {
            return tracks;
        }

        public List<PlaylistLoadError> getErrors() {
            return errors;
        }
    }

    public static final class PlaylistLoadError {
        private final int number;
        private final String item, reason;

        private PlaylistLoadError(int i, String s, String r) {
            number = i;
            item = s;
            reason = r;
        }

        public int getIndex() {
            return number;
        }

        public String getItem() {
            return item;
        }

        public String getReason() {
            return reason;
        }
    }
}
