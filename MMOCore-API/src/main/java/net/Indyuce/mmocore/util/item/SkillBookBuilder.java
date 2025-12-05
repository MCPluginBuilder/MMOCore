package net.Indyuce.mmocore.util.item;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Deprecated
public class SkillBookBuilder extends AbstractItemBuilder {
    private final SkillHandler<?> skill;

    @Deprecated
    public SkillBookBuilder(RegisteredSkill skill) {
        this(skill.getHandler());
    }

    public SkillBookBuilder(SkillHandler<?> skill) {
        super(MMOCore.plugin.configItems.get("SKILL_BOOK"));

        this.skill = skill;
    }

    @Override
    public void whenBuildingMeta(ItemStack item, ItemMeta meta) {

    }

    @Override
    public void whenBuildingNBT(NBTItem nbtItem) {
        nbtItem.addTag(new ItemTag("SkillBookId", skill.getId()));
    }
}
