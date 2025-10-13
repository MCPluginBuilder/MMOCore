package net.Indyuce.mmocore.api.block;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MushroomBlockType implements BlockType {
    private final Set<BlockFace> faces;
    private final Material type;

    public MushroomBlockType(MMOLineConfig config) {
        config.validateKeys("faces", "type");

        this.type = UtilityMethods.prettyValueOf(Material::valueOf, config.getString("type"), "No material with ID %s");
        var split = config.getString("faces").split("\\,");
        faces = new HashSet<>();
        for (int j = 0; j < split.length; j++)
            faces.add(UtilityMethods.prettyValueOf(BlockFace::valueOf, split[j], "No block face with ID %s"));
    }

    public MushroomBlockType(@NotNull Block block) {
        final var state = (MultipleFacing) block.getBlockData();
        this.type = block.getType();
        this.faces = state.getAllowedFaces();
    }

    public Set<BlockFace> getFaces() {
        return faces;
    }

    @Override
    public boolean breakRestrictions(@NotNull Block block) {
        return true;
    }

    @Override
    public void place(RegeneratingBlock block) {
        Location loc = block.getLocation();
        block.getLocation().getBlock().setType(type);

        var state = (MultipleFacing) loc.getBlock().getBlockData();
        for (var face : faces) state.setFace(face, true);
        loc.getBlock().setBlockData(state);
    }

    @Override
    public void regenerate(RegeneratingBlock block) {
        Location loc = block.getLocation();
        loc.getBlock().setType(type);
        // Sets the original blocks old data (only when regenerating)
        loc.getBlock().setBlockData(block.getBlockData());
    }

    @Override
    public @NotNull String display() {
        return "Mushroom{type=" + type.name() + ", faces=[" + this.faces.stream().map(BlockFace::name).reduce((a, b) -> a + ", " + b).orElse("") + "]}";
    }

    @Override
    public String toString() {
        return display();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MushroomBlockType that = (MushroomBlockType) object;
        return Objects.equals(faces, that.faces) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(faces, type);
    }
}
