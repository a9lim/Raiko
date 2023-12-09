package a9lim.raiko;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class JTest {
    public static void main(String[] args) throws IOException {

    }
    public static JSONObject process(JSONObject jason) {
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
                        .put("auth_type",jason.getJSONObject("authTypes").getString("http"))
                        .put("content_key_timeout",jason.getInt("contentKeyTimeout"))
                        .put("service_id","nicovideo")
                        .put("service_user_id",jason.getString("serviceUserId")))
                .put("client_info",new JSONObject()
                        .put("player_id",jason.getString("playerId"))));
    }
    public static JSONObject buildJSON(JSONObject input){
        return new JSONObject().put("session",new JSONObject()
                .put("content_type","movie")
                .put("keep_method",new JSONObject()
                        .put("heartbeat",new JSONObject()
                                .put("lifetime",input.getInt("heartbeatLifetime"))))
                .put("timing_constraint","unlimited")
                .put("content_src_id_sets", new JSONArray()
                        .put(new JSONObject()
                                .put("content_src_ids",new JSONArray()
                                        .put(new JSONObject()
                                                .put("src_id_to_mux",new JSONObject()
                                                        .put("video_src_ids",input.getJSONArray("videos"))
                                                        .put("audio_src_ids",input.getJSONArray("audios")))))))
                .put("recipe_id",input.getString("recipeId"))
                .put("priority",input.getInt("priority"))
                .put("protocol",new JSONObject()
                        .put("name","http")
                        .put("parameters",new JSONObject()
                                .put("http_parameters", new JSONObject()
                                        .put("parameters",new JSONObject()
                                                .put("http_output_download_parameters",new JSONObject()
                                                        .put("use_well_known_port",
                                                                (boolean) input.query("/urls/0/isWellKnownPort")
                                                                        ? "yes" : "no")
                                                        .put("use_ssl",
                                                                (boolean) input.query("/urls/0/isSsl")
                                                                        ? "yes" : "no")
                                                        .put("transfer_preset",""))))))
                .put("content_uri","")
                .put("session_operation_auth",new JSONObject()
                        .put("session_operation_auth_by_signature", new JSONObject()
                                .put("token",input.getString("token"))
                                .put("signature",input.getString("signature"))))
                .put("content_id",input.getString("contentId"))
                .put("content_auth",new JSONObject()
                        .put("auth_type",input.query("/authTypes/http"))
                        .put("content_key_timeout",input.getInt("contentKeyTimeout"))
                        .put("service_id","nicovideo")
                        .put("service_user_id",input.getString("serviceUserId")))
                .put("client_info",new JSONObject()
                        .put("player_id",input.getString("playerId"))));
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

}
