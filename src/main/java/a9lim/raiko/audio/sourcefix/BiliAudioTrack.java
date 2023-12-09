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

public class BiliAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(BiliAudioTrack.class);

    private final BiliAudioSourceManager sourceManager;

    private final String playbackUrl;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */
    public BiliAudioTrack(AudioTrackInfo trackInfo, BiliAudioSourceManager sourceManager, String url) {
        super(trackInfo);

        this.sourceManager = sourceManager;
        playbackUrl = url;
    }

    public BiliAudioTrack(AudioTrackInfo trackInfo, BiliAudioSourceManager sourceManager) {
        super(trackInfo);
        String playbackUrl1;

        this.sourceManager = sourceManager;
        try {
            playbackUrl1 = loadPlaybackUrl(sourceManager.getHttpInterface());
        } catch (IOException ex){
            playbackUrl1 = null;
        }
        playbackUrl = playbackUrl1;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {

        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {

            log.debug("Starting BiliBili track from URL: {}", playbackUrl);

            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(playbackUrl), null)) {

                // format is weird!!
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        HttpGet request = new HttpGet(trackInfo.uri);
        request.addHeader("Host", "www.bilibili.com");
        request.addHeader("Connection","keep-alive");

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from video main page: " + statusCode);
            }

            Document mainPage = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "");
            String playInfo = mainPage.selectFirst("script:containsData(window.__playinfo__)").data().substring(20);

            return JsonBrowser.parse(playInfo).get("data").get("dash").get("audio").index(0).get("baseUrl").text();
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new BiliAudioTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}