package net.Indyuce.mmocore.api.block;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmocore.api.block.BlockInfo.RegeneratingBlock;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NoteBlockType implements BlockType {
    private final Instrument instrument;
    private final Note note;

    public NoteBlockType(MMOLineConfig config) {
        config.validateKeys("note");

        instrument = config.contains("instrument")
                ? UtilityMethods.prettyValueOf(Instrument::valueOf, config.getString("instrument"), "No instrument with ID '%s'")
                : Instrument.PIANO;
        note = new Note(config.getInt("note"));
    }

    public NoteBlockType(@NotNull Block block) {
        final var state = (NoteBlock) block.getBlockData();
        this.instrument = state.getInstrument();
        this.note = state.getNote();
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Note getNote() {
        return note;
    }

    @Override
    public boolean breakRestrictions(@NotNull Block block) {
        return true;
    }

    @Override
    public void place(RegeneratingBlock block) {
        Location loc = block.getLocation();
        block.getLocation().getBlock().setType(Material.NOTE_BLOCK);

        NoteBlock state = (NoteBlock) loc.getBlock().getBlockData();
        state.setInstrument(instrument);
        state.setNote(note);
        loc.getBlock().setBlockData(state);
    }

    @Override
    public void regenerate(RegeneratingBlock block) {
        Location loc = block.getLocation();
        loc.getBlock().setType(Material.NOTE_BLOCK);
        // Sets the original blocks old data (only when regenerating)
        loc.getBlock().setBlockData(block.getBlockData());
    }

    @Override
    public @NotNull String display() {
        return "NoteBlock{instrument=" + instrument.name() + ", note=" + note.getId() + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        NoteBlockType that = (NoteBlockType) object;
        return instrument == that.instrument && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrument, note);
    }
}
