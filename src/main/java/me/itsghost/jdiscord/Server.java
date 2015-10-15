package me.itsghost.jdiscord;

import java.util.List;

/**
 * Created by Ghost on 14/10/2015.
 */
public interface Server {
    String getId();
    String getTopic();
    String getLocation();
    String getCreatorId();
    String getAvatar();
    GroupUser getGroupUserById(String id);
    GroupUser getGroupUserByUsername(String username);
    List<GroupUser> getConnectedClients();
    List<Group> getGroups();
    void kick(String user);
    void ban(String user);
    void bc(String message);
    Group getGroupById(String id);
}
