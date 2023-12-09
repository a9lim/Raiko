package a9lim.raiko;

import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BTest {
    public static void main(String[] args) throws IOException {
        InputStream i = BTest.class.getResourceAsStream("/HTML.txt");
        Document document = Jsoup.parse(i, StandardCharsets.UTF_8.name(), "");
        // skip "window.__playinfo__="
        String playInfo = document.selectFirst("script:containsData(window.__playinfo__)").data().substring(20);
        JSONObject data = new JSONObject(playInfo).getJSONObject("data").getJSONObject("dash");
        System.out.println(data.getJSONArray("video").getJSONObject(0).get("base_url"));
        System.out.println(data.getJSONArray("audio").getJSONObject(0).get("base_url"));

    }

}
