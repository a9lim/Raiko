// Copyright 2023 Aidan Lim (a9lim) <aidanlim192@gmail.com>
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

package a9lim.raiko;

import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import okhttp3.MediaType;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class NTest {
    public static void main(String[] args) throws IOException {
        HttpInterface httpInterface = HttpClientTools.createDefaultThreadLocalManager().getInterface();

        CloseableHttpResponse response = httpInterface.execute(new HttpGet("https://www.nicovideo.jp/watch/sm42901948"));
        MediaType mediaType = MediaType.parse("application/json");

        // get the watch data thing
        String data = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "", Parser.htmlParser()).getElementById("js-initial-watch-data").attributes().get("data-api-data");
        System.out.println(data);

        HttpPost p = new HttpPost("https://api.dmc.nico/api/sessions?_format=json");
        p.addHeader("Host", "api.dmc.nico");
        p.addHeader("Connection","keep-alive");
        p.addHeader("Content-Type","application/json");
        p.addHeader("Origin","https://www.nicovideo.jp");
        p.setEntity(new StringEntity(buildJSON(new JSONObject(data).getJSONObject("media")
                .getJSONObject("delivery").getJSONObject("movie").getJSONObject("session")).toString()));
        response = httpInterface.execute(p);

        String id = new JSONObject(new String(response.getEntity().getContent().readAllBytes())).getJSONObject("data").getJSONObject("session").getString("id");
        System.out.println("ID:\t"+id);

        HttpOptions r = new HttpOptions("https://api.dmc.nico/api/sessions/"+id+"?_format=json");
        r.addHeader("Host", "api.dmc.nico");
        r.addHeader("Connection","keep-alive");
        r.addHeader("Content-Type","application/json");
        r.addHeader("Origin","https://www.nicovideo.jp");

        response = httpInterface.execute(r);

        for(Header h : response.getAllHeaders())
            System.out.println(h.getName() + "\t" + h.getValue());
        System.out.println("entity seid\t" + response.getEntity());
        BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while((line = in.readLine()) != null)
            System.out.println(line);
        EntityUtils.consume(response.getEntity());


    }

    public static JSONObject buildJSON(JSONObject jason){
        return new JSONObject().put("session",new JSONObject()
                .put("content_type","movie")
                .put("keep_method",new JSONObject()
                        .put("heartbeat",new JSONObject()
                                .put("lifetime",jason.getInt("heartbeatLifetime"))))
                .put("timing_constraint","unlimited")
                .put("content_src_id_sets", new JSONArray()
                        .put(new JSONObject()
                                .put("content_src_ids",new JSONArray()
                                        .put(new JSONObject()
                                                .put("src_id_to_mux",new JSONObject()
                                                        .put("video_src_ids",jason.getJSONArray("videos"))
                                                        .put("audio_src_ids",jason.getJSONArray("audios")))))))
                .put("recipe_id",jason.getString("recipeId"))
                .put("priority",jason.getInt("priority"))
                .put("protocol",new JSONObject()
                        .put("name","http")
                        .put("parameters",new JSONObject()
                                .put("http_parameters", new JSONObject()
                                        .put("parameters",new JSONObject()
                                                .put("http_output_download_parameters",new JSONObject()
                                                        .put("use_well_known_port",
                                                                jason.getJSONArray("urls").getJSONObject(0).getBoolean("isWellKnownPort")
                                                                        ? "yes" : "no")
                                                        .put("use_ssl",
                                                                jason.getJSONArray("urls").getJSONObject(0).getBoolean("isSsl")
                                                                        ? "yes" : "no")
                                                        .put("transfer_preset",""))))))
                .put("content_uri","")
                .put("session_operation_auth",new JSONObject()
                        .put("session_operation_auth_by_signature", new JSONObject()
                                .put("token",jason.getString("token"))
                                .put("signature",jason.getString("signature"))))
                .put("content_id",jason.getString("contentId"))
                .put("content_auth",new JSONObject()
                        .put("auth_type",jason.getJSONObject("authTypes").get("http"))
                        .put("content_key_timeout",jason.getInt("contentKeyTimeout"))
                        .put("service_id","nicovideo")
                        .put("service_user_id",jason.getString("serviceUserId")))
                .put("client_info",new JSONObject()
                        .put("player_id",jason.getString("playerId"))));
    }
}