package me.itsghost.jdiscord.internal.request.poll;

import me.itsghost.jdiscord.Server;
import org.json.JSONObject;

/**
 * Created by Ghost on 15/10/2015.
 */
public interface Poll {
    void process(JSONObject content, JSONObject rawRequest, Server server);
}
