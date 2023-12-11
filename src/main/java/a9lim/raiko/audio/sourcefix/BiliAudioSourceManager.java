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

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpConfigurable;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class BiliAudioSourceManager implements AudioSourceManager, HttpConfigurable {
    private static final String TRACK_URL_REGEX = "^(?:http://|https://|)(?:www\\.|)bilibili\\.(?:com|tv)/video/(.+)(?:\\?.*|/)$";

    private static final Pattern trackUrlPattern = Pattern.compile(TRACK_URL_REGEX);
    private final HttpInterfaceManager httpInterfaceManager;

    public BiliAudioSourceManager() {
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
    }

    @Override
    public String getSourceName() {
        return "bilibili";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        Matcher trackMatcher = trackUrlPattern.matcher(reference.identifier);


        if (trackMatcher.matches()) {
            return loadTrack(trackMatcher.group(1));
        }

        return null;
    }

    private AudioTrack loadTrack(String videoId) {
        HttpGet request = new HttpGet("https://api.bilibili.com/x/web-interface/view?bvid="+videoId);

        request.addHeader("Origin", "https://www.bilibili.com");
        request.addHeader("Referer", "https://www.bilibili.com");
        request.addHeader("Connection","keep-alive");
        request.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.3");

        try (HttpInterface httpInterface = getHttpInterface()) {
            try (CloseableHttpResponse response = httpInterface.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                    throw new IOException("Unexpected response code from video info: " + statusCode);
                }

                JsonBrowser data = JsonBrowser.parse(response.getEntity().getContent()).get("data");
                long duration = DataFormatTools.durationTextToMillis(data.get("duration").text());
                String uploader = data.get("owner").get("name").text();
                String title = data.get("title").text();
                String thumbnailUrl = data.get("pic").text();
                long cid = data.get("cid").asLong(0);

                return new BiliAudioTrack(new AudioTrackInfo(title,
                        uploader,
                        duration,
                        videoId,
                        false,
                        getWatchUrl(videoId),
                        thumbnailUrl,
                        null
                ), this, cid);
            }
        } catch (IOException e) {
            throw new FriendlyException("Error occurred when extracting video info.", SUSPICIOUS, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // No extra information to save
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return new BiliAudioTrack(trackInfo, this);
    }

    @Override
    public void shutdown() {
        // Nothing to shut down
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    @Override
    public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
        httpInterfaceManager.configureRequests(configurator);
    }

    @Override
    public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
        httpInterfaceManager.configureBuilder(configurator);
    }

    private static String getWatchUrl(String videoId) {
        return "https://www.bilibili.com/video/" + videoId +"/";
    }
}