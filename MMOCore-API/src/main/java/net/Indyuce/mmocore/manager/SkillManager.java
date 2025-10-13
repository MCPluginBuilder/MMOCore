package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.util.FileUtils;
import io.lumine.mythic.lib.util.config.YamlFile;
import io.lumine.mythic.lib.util.lang3.Validate;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.skill.list.Ambers;
import net.Indyuce.mmocore.skill.list.Neptune_Gift;
import net.Indyuce.mmocore.skill.list.Sneaky_Picky;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class SkillManager implements MMOCoreManager {
    private final Map<String, RegisteredSkill> skills = new LinkedHashMap<>();

    public void registerSkill(RegisteredSkill skill) {
        skills.put(skill.getHandler().getId().toUpperCase(), skill);
    }

    @Nullable
    public RegisteredSkill getSkill(String id) {
        return skills.get(id.toUpperCase());
    }

    @NotNull
    public RegisteredSkill getSkillOrThrow(String id) {
        return Objects.requireNonNull(skills.get(id), "Could not find skill with ID '" + id + "'");
    }

    public Collection<RegisteredSkill> getAll() {
        return skills.values();
    }

    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            MythicLib.plugin.getSkills().initialize(true);
            skills.clear();
        }

        // Register MMOCore specific skills
        MythicLib.plugin.getSkills().registerSkillHandler(new Ambers());
        MythicLib.plugin.getSkills().registerSkillHandler(new Neptune_Gift());
        MythicLib.plugin.getSkills().registerSkillHandler(new Sneaky_Picky());

        // Save default files if necessary
        final File skillFolder = FileUtils.getFile(MMOCore.plugin, "skills");
        if (!skillFolder.exists()) try {
            skillFolder.mkdir();

            for (SkillHandler<?> handler : MythicLib.plugin.getSkills().getHandlers()) {
                final InputStream res = MMOCore.plugin.getResource("default/skills/" + handler.getLowerCaseId() + ".yml");
                if (res != null)
                    Files.copy(res, new File(MMOCore.plugin.getDataFolder() + "/skills/" + handler.getLowerCaseId() + ".yml").getAbsoluteFile().toPath());
            }
        } catch (IOException exception) {
            MMOCore.plugin.getLogger().log(Level.WARNING, "Could not save default skill configs: " + exception.getMessage());
        }

        // Generate at least once a config file for registered skills
        // TODO remove temporary solution after stable release
        {
            final var generated = new YamlFile(MMOCore.plugin, "_generated_skill_configs");
            final List<String> generatedSkillHandlerIds = generated.getContent().getStringList("list");
            for (SkillHandler<?> handler : MythicLib.plugin.getSkills().getHandlers()) {
                if (generatedSkillHandlerIds.contains(handler.getId())) continue;

                generatedSkillHandlerIds.add(handler.getId());

                // generate default config
                final var config = new YamlFile(MMOCore.plugin, "skills", handler.getLowerCaseId());
                if (!config.exists()) {
                    config.getContent().set("name", UtilityMethods.caseOnWords(handler.getId().replace("_", " ").replace("-", " ").toLowerCase()));
                    config.getContent().set("lore", Arrays.asList("This is the default skill description", "", "&e{cooldown}s Cooldown", "&9Costs {mana} {mana_name}"));
                    config.getContent().set("material", "BOOK");
                    for (Object param : handler.getParameters()) {
                        config.getContent().set(param + ".base", 0);
                        config.getContent().set(param + ".per-level", 0);
                        config.getContent().set(param + ".min", 0);
                        config.getContent().set(param + ".max", 0);
                    }
                    config.save();
                }
            }

            generated.getContent().set("list", generatedSkillHandlerIds);
            generated.save();
        }

        // Load skills
        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "skills", true, (name, config) -> {
            final SkillHandler<?> handler = MythicLib.plugin.getSkills().getHandler(UtilityMethods.enumName(name));

            try {
                Validate.isTrue(handler == null);
                for (var script : MythicLib.plugin.getSkills().getScripts())
                    if (UtilityMethods.kebabCase(script.getId()).equals(name)) return;
            } catch (Throwable ignored) {
                // TODO j'ai chié dans la colle, plein de .yml en trop a cause des scripts non publics!
                // A absolument enlever lors de la maj centralisation des skills ML/MMOItems/MMOCores
            }

            Validate.notNull(handler, "Could not find skill handler with ID '" + UtilityMethods.enumName(name) + "'");
            final RegisteredSkill skill = new RegisteredSkill(handler, config);
            this.skills.put(handler.getId(), skill);
        }, "Could not load skill from file '%s': %s");
    }
}
