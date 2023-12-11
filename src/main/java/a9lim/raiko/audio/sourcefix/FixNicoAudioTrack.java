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
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FixNicoAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(NicoAudioTrack.class);

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final FixNicoAudioSourceManager sourceManager;

    private String heartbeatURL;

    private JsonBrowser info;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */
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
                long heartbeat = info.get("session").get("keep_method").get("heartbeat").get("lifetime").asLong(120000) - 5000;
                ScheduledFuture<?> heartbeatFuture = executorService.scheduleAtFixedRate(() -> {
                    try {
                        sendHeartbeat(httpInterface);
                    } catch (Exception ex) {
                        log.error("Heartbeat error!", ex);
                        localExecutor.stop();
                    }
                },heartbeat,heartbeat, TimeUnit.MILLISECONDS);
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
                heartbeatFuture.cancel(false);
            }
        }
    }

    private JsonBrowser loadVideoMainPage(HttpInterface httpInterface) throws IOException {
        HttpGet request = new HttpGet(trackInfo.uri);

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from video main page: " + statusCode);
            }

            Document mainPage = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "");
            String watchdata = mainPage.getElementById("js-initial-watch-data").attr("data-api-data");

            return JsonBrowser.parse(watchdata).get("media").get("delivery").get("movie").get("session");
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        JSONObject watchdata = processJSON(loadVideoMainPage(httpInterface));

        HttpPost request = new HttpPost("https://api.dmc.nico/api/sessions?_format=json");
        request.addHeader("Host", "api.dmc.nico");
        request.addHeader("Connection","keep-alive");
        request.addHeader("Content-Type","application/json");
        request.addHeader("Origin","https://www.nicovideo.jp");
        request.setEntity(new StringEntity(watchdata.toString()));

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_CREATED) {
                throw new IOException("Unexpected status code from playback parameters page: " + statusCode);
            }

            info = JsonBrowser.parse(response.getEntity().getContent()).get("data");
            heartbeatURL = "https://api.dmc.nico/api/sessions/" + info.get("session").get("id").text() + "?_format=json&_method=PUT";
            log.debug("NicoNico heartbeat URL: {}", heartbeatURL);
            return info.get("session").get("content_uri").text();
        }
    }

    private void sendHeartbeat(HttpInterface httpInterface) throws IOException {
        HttpPost request = new HttpPost(heartbeatURL);
        request.addHeader("Host", "api.dmc.nico");
        request.addHeader("Connection","keep-alive");
        request.addHeader("Content-Type","application/json");
        request.addHeader("Origin","https://www.nicovideo.jp");
        request.setEntity(new StringEntity(info.format()));

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from heartbeat page: " + statusCode);
            }

            info = JsonBrowser.parse(response.getEntity().getContent()).get("data");
        }
    }

    private JSONObject processJSON(JsonBrowser input) {
        JSONObject session = new JSONObject()
                .put("content_type","movie")
                .put("timing_constraint","unlimited")
                .put("recipe_id",input.get("recipeId").text())
                .put("content_id",input.get("contentId").text());


        JSONObject lifetime = new JSONObject()
                .put("lifetime",input.get("heartbeatLifetime").asLong(120000));

        JSONObject heartbeat = new JSONObject()
                .put("heartbeat",lifetime);

        session.put("keep_method",heartbeat);

        JSONArray videos = new JSONArray(input.get("videos").format());
        JSONArray audios = new JSONArray(input.get("audios").format());

        JSONObject src_ids = new JSONObject()
                .put("video_src_ids",videos)
                .put("audio_src_ids",audios);

        JSONObject src_id_to_mux = new JSONObject()
                .put("src_id_to_mux",src_ids);

        JSONArray array = new JSONArray()
                .put(src_id_to_mux);

        JSONObject content_src_ids = new JSONObject()
                .put("content_src_ids",array);

        JSONArray content_src_id_sets = new JSONArray()
                .put(content_src_ids);

        session.put("content_src_id_sets", content_src_id_sets);


        JSONObject http_download_parameters = new JSONObject();

        if(input.get("urls").index(0).get("isWellKnownPort").asBoolean(false))
            http_download_parameters.put("use_well_known_port","yes");
        else
            http_download_parameters.put("use_well_known_port","no");

        if(input.get("urls").index(0).get("isSsl").asBoolean(false))
            http_download_parameters.put("use_ssl","yes");
        else
            http_download_parameters.put("use_ssl","no");

        JSONObject inner_parameters = new JSONObject()
                .put("http_output_download_parameters", http_download_parameters);

        JSONObject http_parameters = new JSONObject()
                .put("parameters", inner_parameters);

        JSONObject outer_parameters = new JSONObject()
                .put("http_parameters", http_parameters);

        JSONObject protocol = new JSONObject()
                .put("name","http")
                .put("parameters", outer_parameters);

        session.put("protocol",protocol);

        JSONObject session_operation_auth_by_signature = new JSONObject()
                .put("token",input.get("token").text())
                .put("signature",input.get("signature").text());

        JSONObject session_operation_auth = new JSONObject()
                .put("session_operation_auth_by_signature",session_operation_auth_by_signature);

        session.put("session_operation_auth",session_operation_auth);


        JSONObject content_auth = new JSONObject()
                .put("auth_type",input.get("authTypes").get("http").text())
                .put("content_key_timeout",input.get("contentKeyTimeout").asLong(120000))
                .put("service_id","nicovideo")
                .put("service_user_id",input.get("serviceUserId").text());

        session.put("content_auth", content_auth);


        JSONObject client_info = new JSONObject()
                .put("player_id",input.get("playerId").text());

        session.put("client_info",client_info);

        return new JSONObject()
                .put("session",session);
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