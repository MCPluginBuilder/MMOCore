package net.Indyuce.mmocore.command.builtin.mmocore.slot;

import io.lumine.mythic.lib.command.CommandTreeNode;


public class SlotCommandTreeNode extends CommandTreeNode {
    public SlotCommandTreeNode(CommandTreeNode parent) {
        super(parent, "slot");

        addChild(new LockSlotCommandTreeNode(this, "lock"));
        addChild(new UnlockSlotCommandTreeNode(this, "unlock"));
        addChild(new UnbindSlotCommandTreeNode(this, "unbind"));
        addChild(new BindSlotCommandTreeNode(this, "bind"));
    }
}
