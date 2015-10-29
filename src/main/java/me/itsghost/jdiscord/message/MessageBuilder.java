package me.itsghost.jdiscord.message;

import me.itsghost.jdiscord.internal.impl.MessageImpl;
import me.itsghost.jdiscord.talkable.Group;
import me.itsghost.jdiscord.talkable.GroupUser;
import me.itsghost.jdiscord.talkable.User;
import org.json.JSONArray;

public class MessageBuilder {

    private StringBuilder sb = new StringBuilder();
    private JSONArray mentions = new JSONArray();

    public MessageBuilder addString(String string) {
        sb.append(string);
        return this;
    }

    public MessageBuilder addObject(Object obj) {
        sb.append(obj);
        return this;
    }

    public MessageBuilder addUserTag(GroupUser user, Group server) {
        return addUserTag(user.getUser().getUsername(), server);
    }

    public MessageBuilder addUserTag(User user, Group server) {
        return addUserTag(user.getUsername(), server);
    }

    public MessageBuilder addUserTag(String username, Group server) {
        sb.append("@" + username);

        GroupUser gp = server.getServer().getGroupUserByUsername(username);
        if (gp == null)
            return this;

        sb = new StringBuilder(sb.toString().replace("@" + username, "<@" + gp.getUser().getId() + ">")); //

        mentions.put(gp.getUser().getId().toString());
        return this;
    }

    public Message build() {
        MessageImpl message = new MessageImpl(sb.toString());
        message.setMentions(mentions);
        return message;
    }
}
