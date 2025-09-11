package net.Indyuce.mmocore.api.player.social;

import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.player.Message;

public class FriendRequest extends Request {
    public FriendRequest(PlayerData creator, PlayerData target) {
        super(creator, target);
    }

    @Override
    public void whenDenied() {

        // Notify target
        Message.FRIEND_REQUEST_DENIED.send(getTarget(), "player", getCreator().getPlayer().getName());

        // Notify creator
        if (getCreator().isOnline()) {
            Message.FRIEND_REQUEST_DENIED_CREATOR.send(getCreator(), "player", getTarget().getPlayer().getName());
        }
    }

    @Override
    public void whenAccepted() {

        // TODO there's most likely a problem if creator goes offline
        getCreator().setLastActivity(PlayerActivity.FRIEND_REQUEST, 0);
        getCreator().addFriend(getTarget().getUniqueId());
        getTarget().addFriend(getCreator().getUniqueId());

        // Notify target
        Message.FRIEND_NOW.send(getTarget(), "player", getCreator().getMMOPlayerData().getPlayerName());

        // Notify creator
        if (getCreator().isOnline()) {
            Message.FRIEND_NOW.send(getCreator(), "player", getTarget().getPlayer().getName());
        }
    }
}