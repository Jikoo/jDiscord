package me.itsghost.jdiscord.internal.request;


import me.itsghost.jdiscord.internal.impl.AccountManagerImpl;
import me.itsghost.jdiscord.internal.impl.DiscordAPIImpl;
import me.itsghost.jdiscord.Server;
import me.itsghost.jdiscord.internal.impl.ServerImpl;
import me.itsghost.jdiscord.internal.impl.VoiceGroupImpl;
import me.itsghost.jdiscord.internal.request.poll.*;
import org.java_websocket.WebSocketImpl;
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
    private Poll statusPoll;
    private Poll updateSettings;
    private Poll channelRemovePoll;
    private Poll channelUpdatePoll;
    private Poll guildAddPoll;
    private Poll userUpdatePoll;
    private Poll deletePoll;

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
        statusPoll = new StatusPoll(api);
        updateSettings = new UpdateSettings(api);
        channelRemovePoll = new ChannelRemove(api);
        channelUpdatePoll = new ChannelUpdatePoll(api);
        guildAddPoll = new GuildAdd(api);
        userUpdatePoll = new UserUpdatePoll(api);
        deletePoll = new DeleteMessagePoll(api);
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
        if (code == 4001){
            System.out.println("\n");
            api.log("Uh... Some other client sent invalid data and timed everyone out!");
            try {
                api.getLoginTokens().process(api);
             } catch(Exception e){
                api.log("Failed to reconnect: " + e.getCause());
                api.stop();
            } finally{
                System.out.println("\n");
            }
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

            Server a = key.isNull("guild_id") ? null : api.getServerById(key.getString("guild_id"));
            Server server = key.isNull("guild_id") ? null : (a != null ? a : api.getGroupById(key.getString("guild_id")).getServer());
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
                    newContactOrGroupPoll.process(key, obj, server);
                    break;
                case "PRESENCE_UPDATE":
                    statusPoll.process(key, obj, server);
                    break;
                case "USER_UPDATE":
                    updateSettings.process(key, obj, server);
                    break;
                case "CHANNEL_DELETE":
                    channelRemovePoll.process(key, obj, server);
                    break;
                case "CHANNEL_UPDATE":
                    channelUpdatePoll.process(key, obj, server);
                    break;
                case "GUILD_CREATE":
                    guildAddPoll.process(key, obj, server);
                    break;
                case "MESSAGE_DELETE":
                    userUpdatePoll.process(key, obj, server);
                    break;
                case "GUILD_MEMBER_UPDATE":
                    deletePoll.process(key, obj, server);
                    break;
                case "VOICE_STATE_UPDATE":
                    try{
                        //The longest password token is shared with the public...
                        //Nice fucking work discord team.
                        if (key.getString("user_id").equals(api.getSelfInfo().getId())){
                            VoiceGroupImpl voice = api.getVoiceGroupById(key.getString("channel_id"));
                            voice.setSession(key.getString("session_id"));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "VOICE_SERVER_UPDATE":
                    try{
                        ((ServerImpl)server).setToken(key.getString("token"));
                        ((ServerImpl)server).setServer(key.getString("endpoint"));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    api.log("Unknown type " + type + "\n >" + obj);
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

    public void send(JSONObject obj){
        this.send(obj.toString());
    }

}


