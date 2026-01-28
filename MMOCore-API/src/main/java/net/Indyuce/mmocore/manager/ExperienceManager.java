package net.Indyuce.mmocore.manager;

import io.lumine.mythic.lib.util.FileUtils;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.curve.ExperienceCurve;
import net.Indyuce.mmocore.experience.curve.ListExperienceCurve;
import net.Indyuce.mmocore.experience.droptable.ExperienceTable;
import net.Indyuce.mmocore.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.manager.profession.ExperienceSourceManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;

public class ExperienceManager implements MMOCoreManager {
    private final Map<String, ExperienceTable> expTables = new HashMap<>();
    private final Map<String, ExperienceCurve> publicExpCurves = new HashMap<>();

    /**
     * Experience sources from the exp-sources.yml config file where you can
     * input any exp source which can later be used along with the 'from'
     * exp source anywhere in the plugin.
     *
     * @deprecated TODO First needs to edit the exp-source current structure. This is going to break a lot of things
     */
    @Deprecated
    private final Map<String, List<ExperienceSource<?>>> publicExpSources = new HashMap<>();

    /**
     * Saves different experience sources based on experience source type.
     */
    private final Map<Class<?>, ExperienceSourceManager<?>> managers = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends ExperienceSource> ExperienceSourceManager<T> getManager(Class<T> t) {
        return (ExperienceSourceManager<T>) managers.get(t);
    }

    @SuppressWarnings("unchecked")
    public <T extends ExperienceSource> void registerSource(T source) {
        final Class<T> path = (Class<T>) source.getClass();
        managers.computeIfAbsent(path, unused -> source.newManager());
        getManager(path).registerSource(source);
    }

    @NotNull
    public ExperienceCurve getCurveOrThrow(String id) {
        return Objects.requireNonNull(publicExpCurves.get(id), "Could not find exp curve with ID '" + id + "'");
    }

    @NotNull
    public Collection<ExperienceCurve> getCurves() {
        return publicExpCurves.values();
    }

    public boolean hasTable(String id) {
        return expTables.containsKey(id);
    }

    @NotNull
    public ExperienceTable getTableOrThrow(String id) {
        return Objects.requireNonNull(expTables.get(id), "Could not find exp table with ID '" + id + "'");
    }

    @NotNull
    public ExperienceTable loadExperienceTable(Object obj) {

        // From configuration section
        if (obj instanceof ConfigurationSection)
            return new ExperienceTable((ConfigurationSection) obj);

        // From list of strings
        if (obj instanceof List)
            return new ExperienceTable((List<String>) obj);

        // From string (predefined table)
        if (obj instanceof String)
            return MMOCore.plugin.experience.getTableOrThrow(obj.toString());

        throw new IllegalArgumentException("Expected either a string, list of strings, or config section");
    }

    public Collection<ExperienceTable> getTables() {
        return expTables.values();
    }

    // TODO when required, define class-specific unlockable items.
    // TODO - Skill, skill slots are class-specific
    // TODO - Waypoints are not
    public boolean isClassSpecific(@NotNull String namespacedKey) {
        return true;
    }

    @Override
    public void initialize(boolean clearBefore) {
        if (clearBefore) {
            expTables.clear();
            publicExpCurves.clear();

            managers.forEach((c, manager) -> manager.close());
            managers.clear();
        }

        // Exp curves
        if (!FileUtils.getFile(MMOCore.plugin, "exp-curves").exists())
            FileUtils.copyDefaultFile(MMOCore.plugin, "exp-curves/levels.txt");

        // [Backward compatibility] Move files from old folder to new folder
        moveFilesToNewFolder();

        // Load exp curves
        FileUtils.loadRawObjectsFromFolder(MMOCore.plugin, "exp-curves", file -> {
            final var curveName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            this.publicExpCurves.put(curveName, new ListExperienceCurve(file));
        }, "Could not load exp curve from file '%s': %s");

        // Exp tables
        FileUtils.loadObjectsFromFolder(MMOCore.plugin, "exp-tables", (key, config) -> {
            final ExperienceTable table = new ExperienceTable(config);
            expTables.put(table.getId(), table);
        }, "Could not load exp table '%s' from file '%s': %s");
    }

    private void moveFilesToNewFolder() {

        var root = MMOCore.plugin.getDataFolder().toPath();
        Path sourceDir = root.resolve("expcurves");
        if (!sourceDir.toFile().exists()) return;

        Path targetDir = root.resolve("exp-curves");

        try {
            Files.createDirectories(targetDir);

            Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetPath = targetDir.resolve(sourceDir.relativize(dir));
                    if (!Files.exists(targetPath)) {
                        Files.createDirectories(targetPath);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Path targetPath = targetDir.resolve(sourceDir.relativize(file));
                    Files.move(
                            file,
                            targetPath,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                        throws IOException {
                    // Supprime le dossier source une fois vide
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            MMOCore.plugin.getLogger().log(Level.WARNING, "Could not move exp curves to new folder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //region Deprecated

    @Deprecated
    public boolean hasCurve(String id) {
        try {
            getCurveOrThrow(id);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Deprecated
    @Nullable
    public List<ExperienceSource<?>> getExperienceSourceList(String key) {
        return publicExpSources.get(key);
    }

    //endregion
}
