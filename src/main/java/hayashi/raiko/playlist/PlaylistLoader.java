/*
 * Copyright 2018 John Grosh (jagrosh).
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
package hayashi.raiko.playlist;

import hayashi.raiko.BotConfig;
import hayashi.raiko.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static hayashi.jdautilities.command.Command.COMPILE;

public class PlaylistLoader {
    private final BotConfig config;

    public PlaylistLoader(BotConfig config) {
        this.config = config;
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

        private Playlist(String name, List<String> items, boolean shuffle) {
            this.name = name;
            this.items = items;
            this.shuffle = shuffle;
        }

        public void loadTracks(AudioPlayerManager manager, Consumer<AudioTrack> consumer, Runnable callback) {
            if (loaded)
                return;
            loaded = true;
            for (int i = 0; i < items.size(); i++) {
                boolean last = i + 1 == items.size();
                int index = i;
                manager.loadItemOrdered(name, items.get(i), new AudioLoadResultHandler() {
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
                            errors.add(new PlaylistLoadError(index, items.get(index), "This track is longer than the allowed maximum"));
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
                            trackLoaded(ap.getTracks().get(0));
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
                        errors.add(new PlaylistLoadError(index, items.get(index), "No matches found."));
                        done();
                    }

                    @Override
                    public void loadFailed(FriendlyException fe) {
                        errors.add(new PlaylistLoadError(index, items.get(index), "Failed to load track: " + fe.getLocalizedMessage()));
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

        private PlaylistLoadError(int number, String item, String reason) {
            this.number = number;
            this.item = item;
            this.reason = reason;
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
