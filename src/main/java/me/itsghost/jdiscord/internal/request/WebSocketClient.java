package me.itsghost.jdiscord.internal.request;


import me.itsghost.jdiscord.DiscordAPIImpl;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.request.poll.*;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    public boolean loaded = false;
    protected Thread thread;
    private DiscordAPIImpl api;
    private String server;
    private Poll readyPoll;
    private Poll banPoll;
    private Poll addUserPoll;
    private Poll messagePoll;
    private Poll kickedPoll;
    private Poll typingPoll;
    private Poll newContactOrGroupPoll;

    public WebSocketClient(DiscordAPIImpl api, String url) {
        super(URI.create(url.replace("wss", "ws"))); //this api doesn't like wss
        this.api = api;
        readyPoll = new ReadyPoll(api);
        banPoll = new BanPoll(api);
        addUserPoll = new AddUserPoll(api);
        messagePoll = new MessagePoll(api);
        kickedPoll = new KickPoll(api);
        typingPoll = new TypingPoll(api);
        newContactOrGroupPoll = new NewContactOrGroupPoll(api);
        this.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        loaded = true;
        api.log("Logged in and loaded!");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if ((code == 1000) && (server != null)) {
            api.log("Your data is on a different server");
            api.log("This error is deprecated... if you're seening this, report it.");
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject obj = new JSONObject(message);
            if (obj.getInt("op") == 7)
                return;
            JSONObject key = obj.getJSONObject("d");
            String type = obj.getString("t");
            Server server = key.isNull("guild_id") ? null : api.getGroupById(key.getString("guild_id")).getServer();
            switch (type) {
                case "READY":
                    readyPoll.process(key, obj, server);
                    api.log("Successfully loaded user data!");
                    break;
                case "GUILD_MEMBER_ADD":
                    addUserPoll.process(key, obj, server);
                    break;
                case "GUILD_MEMBER_REMOVE":
                    kickedPoll.process(key, obj, server);
                    break;
                case "GUILD_BAN_ADD":
                    banPoll.process(key, obj, server);
                    break;
                case "GUILD_BAN_REMOVE":
                    //processBan(key, server);
                    //Unban?
                    break;
                case "MESSAGE_CREATE":
                    messagePoll.process(key, obj, server);
                    break;
                case "MESSAGE_UPDATE":
                    messagePoll.process(key, obj, server);
                    break;
                case "TYPING_START":
                    typingPoll.process(key, obj, server);
                    break;
                case "CHANNEL_CREATE":
                    api.log(key.toString());
                    newContactOrGroupPoll.process(key, obj, server);
                    break;
                default:
                    api.log("Unknown type " + type);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Internal client error!");
        api.log("Attempting go log in (again?)!");
        api.stop();
        try {
            api.login();
        } catch (Exception e) {
        }
    }

    public void stop() {
        this.close();
        ((ReadyPoll)readyPoll).stop();
    }

}


