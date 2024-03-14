package a9lim.raiko;

import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.Header;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
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
import java.util.Arrays;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class BTest {
    public static void main(String[] args) throws IOException {

        HttpInterface httpInterface = HttpClientTools.createDefaultThreadLocalManager().getInterface();

        HttpPost loginRequest = new HttpPost("https://secure.nicovideo.jp/secure/login");


        String email = "raikohorikawa31@gmail.com";
        String password = "9d^XWQQy";

        loginRequest.setEntity(new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("mail", email),
                new BasicNameValuePair("password", password)
        ), StandardCharsets.UTF_8));
        try (CloseableHttpResponse response = httpInterface.execute(loginRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 302) {
                throw new IOException("Unexpected response code " + statusCode);
            }

            for(Header h: response.getAllHeaders())
                System.out.println(h);

            System.out.println(new String(response.getEntity().getContent().readAllBytes()));

        }

    }

}
