package ntnu.idatt2003.group15.utilities;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Maintains a small index of the user's recent save files so the main menu can
 * show a "Continue Playing" card without having to scan a directory.
 *
 * <p>The index lives at {@code ~/.millions/recent-saves.tsv}. Each line is a
 * tab-separated record: {@code path \t playerName \t cash \t savedAt}.
 * Newest entry first; same {@code path} replaces any older copy; capped at
 * {@link #MAX_ENTRIES} rows.
 */
public final class SaveIndex {

  public static final int MAX_ENTRIES = 10;

  /** One row in the index — describes a known save file. */
  public record Entry(String path, String playerName, BigDecimal cash, Instant savedAt) {}

  private SaveIndex() {}

  private static Path indexPath() {
    String home = System.getProperty("user.home", ".");
    return Paths.get(home, ".millions", "recent-saves.tsv");
  }

  /** Read the index; missing/malformed file -> empty list. */
  public static List<Entry> list() {
    Path p = indexPath();
    if (!Files.exists(p)) return List.of();
    try {
      List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
      List<Entry> out = new ArrayList<>(lines.size());
      for (String line : lines) {
        if (line.isBlank()) continue;
        String[] parts = line.split("\t", -1);
        if (parts.length < 4) continue;
        try {
          BigDecimal cash = parts[2].isBlank() ? null : new BigDecimal(parts[2]);
          Instant when = parts[3].isBlank() ? null : Instant.parse(parts[3]);
          out.add(new Entry(parts[0], parts[1], cash, when));
        } catch (RuntimeException ignored) { /* skip bad row */ }
      }
      return out;
    } catch (IOException e) {
      return List.of();
    }
  }

  /** Insert/update an entry. Newest first; older duplicates by path are removed. */
  public static void record(Entry entry) {
    List<Entry> existing = new ArrayList<>(list());
    existing.removeIf(e -> e.path().equals(entry.path()));
    existing.addFirst(entry);
    while (existing.size() > MAX_ENTRIES) existing.removeLast();
    write(existing);
  }

  /** Remove any entries pointing to paths that no longer exist on disk. */
  public static List<Entry> prune() {
    List<Entry> kept = new ArrayList<>();
    Set<String> seen = new LinkedHashSet<>();
    for (Entry e : list()) {
      if (seen.add(e.path()) && Files.exists(Paths.get(e.path()))) {
        kept.add(e);
      }
    }
    write(kept);
    return kept;
  }

  private static void write(List<Entry> entries) {
    Path p = indexPath();
    try {
      Files.createDirectories(p.getParent());
      StringBuilder sb = new StringBuilder();
      for (Entry e : entries) {
        sb.append(e.path()).append('\t')
            .append(e.playerName() == null ? "" : e.playerName()).append('\t')
            .append(e.cash() == null ? "" : e.cash().toPlainString()).append('\t')
            .append(e.savedAt() == null ? "" : e.savedAt().toString())
            .append('\n');
      }
      Files.writeString(p, sb.toString(), StandardCharsets.UTF_8);
    } catch (IOException ignored) {
      // best-effort, the index isn't critical state
    }
  }
}
