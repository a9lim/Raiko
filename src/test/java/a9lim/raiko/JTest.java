package a9lim.raiko;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class JTest {
    public static void main(String[] args) throws IOException {
        System.out.println(JSONS.E.getJSONObject("data").getString("title"));
        System.out.println(JSONS.E.getJSONObject("data").getJSONObject("owner"));
        System.out.println(JSONS.E.getJSONObject("data").getInt("duration"));
        System.out.println(JSONS.E.getJSONObject("data").getString("pic"));

        System.out.println(JSONS.E.getJSONObject("data").keySet());

        System.out.println(JSONS.D.getJSONObject("data").getJSONArray("durl").getJSONObject(0).getString("url"));
    }
    private static JsonBrowser processJSON(JsonBrowser input) throws IOException {
        JsonBrowser session = JsonBrowser.newMap();
        session.put("content_type","movie");
        session.put("timing_constraint","unlimited");
        session.put("recipe_id",input.get("recipeId"));
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

    private static JSONObject processJSON2(JsonBrowser input) throws IOException {
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

}
