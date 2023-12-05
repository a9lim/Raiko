// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>.
// Copyright [sometime before] sedmelluq, Walkyst, and the LavaLink developers.
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

package a9lim.raiko.audio.sourcefix;

import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class FixNicoAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(FixNicoAudioTrack.class);
    private final FixNicoAudioSourceManager sourceManager;
    private String id;
    private JSONObject info;
    private MpegAudioTrack track;

    public FixNicoAudioTrack(AudioTrackInfo trackInfo, FixNicoAudioSourceManager sourceManager) {
        super(trackInfo);
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            String playbackUrl = loadPlaybackUrl(httpInterface);
            log.info("Starting NicoNico track from URL: {}", playbackUrl);
            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(playbackUrl), null)) {
                track = new MpegAudioTrack(trackInfo, stream);
                int heartbeat = heartBeatDuration() - 1000;
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        try {
                            refreshPlayback(httpInterface);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                },heartbeat,heartbeat);
                processDelegate(track, localExecutor);
                t.cancel();
            }
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        HttpPost p = new HttpPost("https://api.dmc.nico/api/sessions?_format=json");
        p.addHeader("Host", "api.dmc.nico");
        p.addHeader("Connection","keep-alive");
        p.addHeader("Content-Type","application/json");
        p.addHeader("Origin","https://www.nicovideo.jp");
        p.setEntity(new StringEntity(processJSON(processResponse(
                httpInterface.execute(new HttpGet(trackInfo.uri)).getEntity().getContent())).toString()));
        info = new JSONObject(new JSONTokener(httpInterface.execute(p).getEntity().getContent())).getJSONObject("data");
        id = (String) info.query("/session/id");
        log.info("NicoNico Video ID: {}", id);
        return (String) info.query("/session/content_uri");
    }
    private void refreshPlayback(HttpInterface httpInterface) throws IOException {
        HttpPost p = new HttpPost("https://api.dmc.nico/api/sessions/"+id+"?_format=json&_method=PUT");
        p.addHeader("Host", "api.dmc.nico");
        p.addHeader("Connection","keep-alive");
        p.addHeader("Content-Type","application/json");
        p.addHeader("Origin","https://www.nicovideo.jp");
        p.setEntity(new StringEntity(info.toString()));
        info = new JSONObject(new JSONTokener(httpInterface.execute(p).getEntity().getContent())).getJSONObject("data");
    }

    private static JSONObject processResponse(InputStream data) throws IOException {
        return (JSONObject) new JSONObject(
                Jsoup.parse(data, StandardCharsets.UTF_8.name(), "")
                        .getElementById("js-initial-watch-data").attributes().get("data-api-data"))
                .query("/media/delivery/movie/session");
    }

    // this should be 2 minutes
    private int heartBeatDuration(){
        return (int) info.query("/session/keep_method/heartbeat/lifetime");
    }

    private static JSONObject processJSON(JSONObject input){
        return new JSONObject().put("session",new JSONObject()
                .put("content_type","movie")
                .put("keep_method",new JSONObject()
                        .put("heartbeat",new JSONObject()
                                .put("lifetime",input.getInt("heartbeatLifetime"))
                        )
                )
                .put("timing_constraint","unlimited")
                .put("content_src_id_sets", new JSONArray()
                        .put(new JSONObject()
                                .put("content_src_ids",new JSONArray()
                                        .put(new JSONObject()
                                                .put("src_id_to_mux",new JSONObject()
                                                        .put("video_src_ids",input.getJSONArray("videos"))
                                                        .put("audio_src_ids",input.getJSONArray("audios"))
                                                )
                                        )
                                )
                        )
                )
                .put("recipe_id",input.getString("recipeId"))
                .put("priority",input.getInt("priority"))
                .put("protocol",new JSONObject()
                        .put("name","http")
                        .put("parameters",new JSONObject()
                                .put("http_parameters", new JSONObject()
                                        .put("parameters",new JSONObject()
                                                .put("http_output_download_parameters",new JSONObject()
                                                        .put("use_well_known_port",
                                                                (boolean) input.query("/urls/0/isWellKnownPort")
                                                                        ? "yes" : "no")
                                                        .put("use_ssl",
                                                                (boolean) input.query("/urls/0/isSsl")
                                                                        ? "yes" : "no")
                                                        .put("transfer_preset",""))
                                        )
                                )
                        )
                )
                .put("content_uri","")
                .put("session_operation_auth",new JSONObject()
                        .put("session_operation_auth_by_signature", new JSONObject()
                                .put("token",input.getString("token"))
                                .put("signature",input.getString("signature"))
                        )
                )
                .put("content_id",input.getString("contentId"))
                .put("content_auth",new JSONObject()
                        .put("auth_type",input.query("/authTypes/http"))
                        .put("content_key_timeout",input.getInt("contentKeyTimeout"))
                        .put("service_id","nicovideo")
                        .put("service_user_id",input.getString("serviceUserId"))
                )
                .put("client_info",new JSONObject()
                        .put("player_id",input.getString("playerId"))
                )
        );
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new FixNicoAudioTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}

