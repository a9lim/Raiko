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
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class BiliAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(BiliAudioTrack.class);

    private final BiliAudioSourceManager sourceManager;

    private final long cid;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager which was used to find this track
     */
    public BiliAudioTrack(AudioTrackInfo trackInfo, BiliAudioSourceManager sourceManager, long url) {
        super(trackInfo);

        this.sourceManager = sourceManager;
        cid = url;
    }

    public BiliAudioTrack(AudioTrackInfo trackInfo, BiliAudioSourceManager sourceManager) {
        super(trackInfo);
        long tempCID;

        this.sourceManager = sourceManager;
        try {
            tempCID = loadCID(sourceManager.getHttpInterface());
        } catch (IOException ex){
            tempCID = 0L;
        }
        cid = tempCID;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {

        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {

            String url = loadPlaybackUrl(httpInterface);

            log.debug("Starting BiliBili track from URL: {}", url);

            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(url), null)) {

                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
        HttpGet request = new HttpGet("https://api.bilibili.com/x/player/wbi/playurl?bvid="+trackInfo.identifier+"&"+"cid="+ cid);
        request.addHeader("Origin", "https://www.bilibili.com");
        request.addHeader("Referer", "https://www.bilibili.com");
        request.addHeader("Connection","keep-alive");
        request.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.3");

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected status code from video main page: " + statusCode);
            }

            JsonBrowser data = JsonBrowser.parse(response.getEntity().getContent());

            return data.get("data").get("durl").index(0).get("url").text();
        }
    }

    private long loadCID(HttpInterface httpInterface) throws IOException{
        HttpGet request = new HttpGet("https://api.bilibili.com/x/web-interface/view?bvid="+trackInfo.identifier);

        request.addHeader("Origin", "https://www.bilibili.com");
        request.addHeader("Referer", "https://www.bilibili.com");
        request.addHeader("Connection","keep-alive");
        request.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.3");

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                throw new IOException("Unexpected response code from video info: " + statusCode);
            }

            JsonBrowser data = JsonBrowser.parse(response.getEntity().getContent()).get("data");
            return data.get("cid").asLong(0);
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