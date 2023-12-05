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
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class FixNicoAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(FixNicoAudioTrack.class);
    private final FixNicoAudioSourceManager sourceManager;
    private String heartbeatURL;
    private JSONObject info;

    public FixNicoAudioTrack(AudioTrackInfo trackInfo, FixNicoAudioSourceManager sourceManager) {
        super(trackInfo);

        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {

        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            String playbackUrl = loadPlaybackUrl(httpInterface);

            log.debug("Starting NicoNico track from URL: {}", playbackUrl);

            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(playbackUrl), null)) {
                int heartbeat = (int) info.query("/session/keep_method/heartbeat/lifetime") - 1000;
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    public void run() {
                        try {
                            sendHeartbeat(httpInterface);
                        } catch (Exception ex) {
                            log.error("Heartbeat error!",ex);
                        }
                    }
                },heartbeat,heartbeat);
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
                t.cancel();
            }
        }
    }

    private JSONObject loadVideoMainPage(HttpInterface httpInterface) throws IOException {
        HttpGet request = new HttpGet(trackInfo.uri);

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from video main page: " + statusCode);
            }

            return (JSONObject) new JSONObject(
                    Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "")
                            .getElementById("js-initial-watch-data").attributes().get("data-api-data"))
                    .query("/media/delivery/movie/session");
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        HttpPost request = new HttpPost("https://api.dmc.nico/api/sessions?_format=json");
        request.addHeader("Host", "api.dmc.nico");
        request.addHeader("Connection","keep-alive");
        request.addHeader("Content-Type","application/json");
        request.addHeader("Origin","https://www.nicovideo.jp");
        request.setEntity(new StringEntity(processJSON(loadVideoMainPage(httpInterface)).toString()));

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                throw new IOException("Unexpected status code from playback parameters page: " + statusCode);
            }

            info = new JSONObject(new JSONTokener(response.getEntity().getContent())).getJSONObject("data");
            heartbeatURL = "https://api.dmc.nico/api/sessions/" + info.query("/session/id") + "?_format=json&_method=PUT";
            log.debug("NicoNico heartbeat URL: {}", heartbeatURL);
            return (String) info.query("/session/content_uri");
        }
    }

    private void sendHeartbeat(HttpInterface httpInterface) throws IOException {
        HttpPost request = new HttpPost(heartbeatURL);
        request.addHeader("Host", "api.dmc.nico");
        request.addHeader("Connection","keep-alive");
        request.addHeader("Content-Type","application/json");
        request.addHeader("Origin","https://www.nicovideo.jp");
        request.setEntity(new StringEntity(info.toString()));

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from heartbeat: " + statusCode);
            }

            info = new JSONObject(new JSONTokener(response.getEntity().getContent())).getJSONObject("data");
        }
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

