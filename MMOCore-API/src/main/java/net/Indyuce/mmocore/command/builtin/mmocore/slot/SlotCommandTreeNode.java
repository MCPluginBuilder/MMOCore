package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeNode;


public class SlotCommandTreeNode extends CommandTreeNode {
    public SlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "slot");

        addChild(new LockSlotCommandTreeNode(this));
        addChild(new UnlockSlotCommandTreeNode(this));
        addChild(new UnbindSlotCommandTreeNode(this));
        addChild(new BindSlotCommandTreeNode(this));
    }
}
