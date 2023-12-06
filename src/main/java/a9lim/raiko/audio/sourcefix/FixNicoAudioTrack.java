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
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class FixNicoAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(NicoAudioTrack.class);

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

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
                long heartbeat = info.get("session").get("keep_method").get("heartbeat").get("lifetime").asLong(120000) - 60000;
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
            String watchdata = mainPage.getElementById("js-initial-watch-data").attributes().get("data-api-data");

            return JsonBrowser.parse(watchdata).get("media").get("delivery").get("movie").get("session");
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        JsonBrowser watchdata = processJSON(loadVideoMainPage(httpInterface));
        HttpPost request = new HttpPost("https://api.dmc.nico/api/sessions?_format=json");
        request.addHeader("Host", "api.dmc.nico");
        request.addHeader("Connection","keep-alive");
        request.addHeader("Content-Type","application/json");
        request.addHeader("Origin","https://www.nicovideo.jp");
        request.setEntity(new StringEntity(watchdata.text()));

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
        request.setEntity(new StringEntity(info.text()));

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from heartbeat page: " + statusCode);
            }

            info = JsonBrowser.parse(response.getEntity().getContent()).get("data");
        }
    }

    private static JsonBrowser processJSON(JsonBrowser input) throws IOException {
        JsonBrowser session = JsonBrowser.newMap();
        session.put("content_type","movie");
        session.put("timing_constraint","unlimited");
        session.put("recipe_id",input.get("recipeId"));
        session.put("priority",input.get("priority"));
        session.put("content_uri","");
        session.put("content_id",input.get("contentId"));


        JsonBrowser lifetime = JsonBrowser.newMap();
        lifetime.put("lifetime",input.get("heartbeatLifetime"));

        JsonBrowser heartbeat = JsonBrowser.newMap();
        heartbeat.put("heartbeat",lifetime);

        session.put("keep_method",heartbeat);


        JsonBrowser srcids = JsonBrowser.newMap();
        srcids.put("video_src_ids",input.get("videos"));
        srcids.put("audio_src_ids",input.get("audios"));

        JsonBrowser srcidtomux = JsonBrowser.newMap();
        srcidtomux.put("src_id_to_mux",srcids);

        JsonBrowser array = JsonBrowser.newList();
        array.add(srcidtomux);

        JsonBrowser contentsrcids = JsonBrowser.newMap();
        contentsrcids.put("content_src_ids",array);

        JsonBrowser contentsrcidsets = JsonBrowser.newList();
        contentsrcidsets.add(contentsrcids);

        session.put("content_src_id_sets", contentsrcidsets);


        JsonBrowser http_download_parameters = JsonBrowser.newMap();

        if(input.get("urls").index(0).get("isWellKnownPort").asBoolean(false))
            http_download_parameters.put("use_well_known_port","yes");
        else
            http_download_parameters.put("use_well_known_port","no");

        if(input.get("urls").index(0).get("isSsl").asBoolean(false))
            http_download_parameters.put("use_ssl","yes");
        else
            http_download_parameters.put("use_ssl","no");

        http_download_parameters.put("transfer_preset","");

        JsonBrowser innerparameters = JsonBrowser.newMap();
        innerparameters.put("http_output_download_parameters", http_download_parameters);

        JsonBrowser httpparameters = JsonBrowser.newMap();
        httpparameters.put("parameters", innerparameters);

        JsonBrowser outerparameters = JsonBrowser.newMap();
        outerparameters.put("http_parameters", httpparameters);

        JsonBrowser protocol = JsonBrowser.newMap();
        protocol.put("name","http");
        protocol.put("parameters", outerparameters);

        session.put("protocol",protocol);


        JsonBrowser session_operation_auth_by_signature = JsonBrowser.newMap();
        session_operation_auth_by_signature.put("token",input.get("token"));
        session_operation_auth_by_signature.put("signature",input.get("signature"));

        JsonBrowser session_operation_auth = JsonBrowser.newMap();
        session_operation_auth.put("session_operation_auth_by_signature",session_operation_auth_by_signature);

        session.put("session_operation_auth",session_operation_auth);


        JsonBrowser contentauth = JsonBrowser.newMap();
        contentauth.put("auth_type",input.get("authTypes").get("http"));
        contentauth.put("content_key_timeout",input.get("contentKeyTimeout"));
        contentauth.put("service_id","nicovideo");
        contentauth.put("service_user_id",input.get("serviceUserId"));

        session.put("content_auth", contentauth);


        JsonBrowser clientinfo = JsonBrowser.newMap();
        clientinfo.put("player_id",input.get("playerId"));

        session.put("client_info",clientinfo);


        JsonBrowser out = JsonBrowser.newMap();
        out.put("session",session);

        return out;
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