package net.Indyuce.mmocore.api.util.input;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.player.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

public abstract class PlayerInput implements Listener {
    private final Player player;
    private final Consumer<String> output;

    public PlayerInput(Player player, Consumer<String> output) {
        this.player = player;
        this.output = output;

        Bukkit.getPluginManager().registerEvents(this, MMOCore.plugin);
    }

    public void output(String input) {
        output.accept(input);
    }

    public Player getPlayer() {
        return player;
    }

    public abstract void close();

    public enum InputType {
        FRIEND_REQUEST(Message.INPUT_FRIEND_REQUEST, Message.INPUT_CANCEL_FRIEND_REQUEST),
        PARTY_INVITE(Message.INPUT_PARTY_INVITE, Message.INPUT_CANCEL_PARTY_INVITE),
        GUILD_INVITE(Message.INPUT_GUILD_INVITE, Message.INPUT_CANCEL_GUILD_INVITE),
        GUILD_CREATION_TAG(Message.INPUT_GUILD_CREATION_TAG, Message.INPUT_CANCEL_GUILD_CREATION_TAG),
        GUILD_CREATION_NAME(Message.INPUT_GUILD_CREATION_NAME, Message.INPUT_CANCEL_GUILD_CREATION_NAME);

        public final Message inputMessage, cancelMessage;

        InputType(Message inputMessage, Message cancelMessage) {
            this.inputMessage = inputMessage;
            this.cancelMessage = cancelMessage;
        }

        public String getLowerCaseName() {
            return name().toLowerCase().replace("_", "-");
        }
    }
}
