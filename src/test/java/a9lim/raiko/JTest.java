package a9lim.raiko;

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
    public static JSONObject t2(JSONObject o) {
        return new JSONObject(
        "{\"session\":{\"keep_method\":{\"heartbeat\":{\"lifetime\":" + o.getInt("heartbeatLifetime") +
                "}},\"protocol\":{\"name\":\"http\",\"parameters\":{\"http_parameters\":{\"parameters\":{\"http_output_download_parameters\":{\"use_ssl\":" +
                ((boolean) o.query("/urls/0/isSsl") ? "yes" : "no") + ",\"transfer_preset\":\"\",\"use_well_known_port\":" +
                ((boolean) o.query("/urls/0/isWellKnownPort") ? "yes" : "no") +
                "}}}}},\"content_auth\":{\"content_key_timeout\":" + o.getInt("contentKeyTimeout") + ",\"auth_type\":" +
                o.query("/authTypes/http") + ",\"service_id\":\"nicovideo\",\"service_user_id\":" +
                o.getString("serviceUserId") + "},\"client_info\":{\"player_id\":" + o.getString("playerId") + "},\"recipe_id\":" +
                o.getString("recipeId") + ",\"content_type\":\"movie\",\"session_operation_auth\":{\"session_operation_auth_by_signature\":{\"signature\":" +
                o.getString("signature") + ",\"token\":" + o.getString("token") + "}},\"content_uri\":\"\",\"content_id\":" + o.getString("contentId") +
                ",\"content_src_id_sets\":[{\"content_src_ids\":[{\"src_id_to_mux\":{\"video_src_ids\":" + o.getJSONArray("videos") + ",\"audio_src_ids\":" +
                o.getJSONArray("audios") + "}}]}],\"priority\":" + o.getInt("priority") + ",\"timing_constraint\":\"unlimited\"}}\n"
        );
    }
}
