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

        HttpInterface httpInterface = HttpClientTools.createDefaultThreadLocalManager().getInterface();

        String videoId = "BV1ne411X7vw";

        HttpGet request = new HttpGet("https://api.bilibili.com/x/web-interface/view?bvid="+videoId);
        CloseableHttpResponse response = httpInterface.execute(request);
//        System.out.println(new String(response.getEntity().getContent().readAllBytes()));

        String cid = JsonBrowser.parse(response.getEntity().getContent()).get("data").get("cid").text();

        request = new HttpGet("https://api.bilibili.com/x/player/wbi/playurl?bvid="+videoId+"&"+"cid="+cid);
        request.addHeader( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.3");
        request.addHeader("Origin", "https://www.bilibili.com");
        request.addHeader("Referer", "https://www.bilibili.com");


        response = httpInterface.execute(request);

        JsonBrowser.parse(response.getEntity().getContent()).get("data").get("durl").index(0).get("url").text();
    }

}
