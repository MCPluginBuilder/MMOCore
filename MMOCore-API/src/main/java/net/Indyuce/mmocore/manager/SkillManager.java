package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

@Deprecated
public class SkillManager implements MMOCoreManager {

    @Deprecated
    public static SkillManager INSTANCE;

    @NotNull
    @Deprecated
    public static SkillManager getInstance() {
        if (INSTANCE == null) INSTANCE = new SkillManager();
        return INSTANCE;
    }

    @Deprecated
    public void registerSkill(RegisteredSkill skill) {
        MythicLib.plugin.getSkills().registerSkillHandler(skill.getHandler());
    }

    @Deprecated
    public RegisteredSkill getSkill(String id) {
        var f = MythicLib.plugin.getSkills().getHandler(id);
        return f != null ? new RegisteredSkill(f) : null;
    }

    @Deprecated
    public RegisteredSkill getSkillOrThrow(String id) {
        return new RegisteredSkill(MythicLib.plugin.getSkills().getHandlerOrThrow(id));
    }

    @Deprecated
    public Collection<RegisteredSkill> getAll() {
        return MythicLib.plugin.getSkills().getHandlers().stream().map(RegisteredSkill::new).collect(Collectors.toList());
    }

    @Deprecated
    public void initialize(boolean clearBefore) {
        // Nothing
    }
}
