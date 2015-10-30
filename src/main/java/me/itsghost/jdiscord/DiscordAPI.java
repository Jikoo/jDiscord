package me.itsghost.jdiscord;

import me.itsghost.jdiscord.event.EventManager;
import me.itsghost.jdiscord.exception.BadUsernamePasswordException;
import me.itsghost.jdiscord.exception.DiscordFailedToConnectException;
import me.itsghost.jdiscord.exception.NoLoginDetailsException;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.User;

import java.util.List;
import java.util.Map;

public interface DiscordAPI {
    DiscordAPI login() throws NoLoginDetailsException, BadUsernamePasswordException, DiscordFailedToConnectException;
    DiscordAPI login(String email, String password) throws BadUsernamePasswordException, DiscordFailedToConnectException;
    void stop();
    void setAllowLogMessages(boolean val);
    void setDebugMode(boolean val);
    boolean isDebugMode();
    boolean isAllowLogMessages();
    boolean isLoaded();
    EventManager getEventManager();
    User getUserById(String id);
    User getUserByUsername(String id);
    Group getGroupById(String id);
    Server getServerById(String id);
    List<User> getAvailableDms();
    List<Server> getAvailableServers();
    Map<String, Group> getUserGroups();
    SelfData getSelfInfo();

}
