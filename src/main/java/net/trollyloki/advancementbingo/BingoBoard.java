package net.trollyloki.advancementbingo;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a 2-D bingo board of advancements to be completed.
 */
public class BingoBoard implements Cloneable {

    private @NotNull Component title;
    private final @NotNull NamespacedKey @NotNull [] @NotNull [] advancements;
    private final boolean @NotNull [] @NotNull [] completed;
    private final int requiredRows;
    private @Nullable Inventory gui;

    /**
     * Creates a new bingo board.
     *
     * @param title title
     * @param advancements 2-D array of advancement keys
     * @param completed 2-D array of booleans indicating which advancements have been completed
     * @param requiredRows number of completed rows required before the board is considered winning
     * @throws IllegalArgumentException if either of the 2-D arrays are not square
     */
    public BingoBoard(@NotNull Component title, @NotNull NamespacedKey @NotNull [] @NotNull [] advancements, boolean @NotNull [] @NotNull [] completed, int requiredRows) {
        validateArray(advancements, "Advancements");
        validateArray(completed, "Completed");
        if (advancements.length != completed.length)
            throw new IllegalArgumentException("Advancements and completed arrays differ in size: " + advancements.length + " vs " + completed.length);
        this.title = title;
        this.advancements = advancements;
        this.completed = completed;
        this.requiredRows = requiredRows;
    }

    /**
     * Creates a new uncompleted bingo board.
     *
     * @param title title
     * @param advancements 2-D array of advancement keys
     * @param requiredRows number of completed rows required before the board is considered winning
     * @throws IllegalArgumentException if the advancements array is not square
     */
    public BingoBoard(@NotNull Component title, @NotNull NamespacedKey @NotNull [] @NotNull [] advancements, int requiredRows) {
        this(title, advancements, new boolean[advancements.length][advancements[0].length], requiredRows);
    }

    /**
     * Creates a new uncompleted bingo board.
     *
     * @param title title
     * @param advancements 2-D array of advancement keys
     * @throws IllegalArgumentException if the advancements array is not square
     */
    public BingoBoard(@NotNull Component title, @NotNull NamespacedKey @NotNull [] @NotNull [] advancements) {
        this(title, advancements, 1);
    }

    /**
     * Generates a random bingo board.
     *
     * @param size size of the board
     * @param options set of advancement keys that can be used
     * @return random uncompleted bingo board
     */
    public static @NotNull BingoBoard generateRandom(@NotNull Component title, int size, @NotNull Set<@NotNull NamespacedKey> options, int requiredRows) {
        List<NamespacedKey> optionsList = new ArrayList<>(options);
        Random random = ThreadLocalRandom.current();

        NamespacedKey[][] advancements = new NamespacedKey[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int i = random.nextInt(optionsList.size());
                advancements[r][c] = optionsList.remove(i);
            }
        }

        return new BingoBoard(title, advancements, requiredRows);
    }

    /**
     * Checks if a 2-D array is valid for a bingo board.
     *
     * @param array 2-D array
     * @throws IllegalArgumentException if the array is invalid
     */
    protected static void validateArray(@NotNull Object array, @NotNull String name) {
        int rows = Array.getLength(array);

        // Check array is not empty
        if (rows == 0)
            throw new IllegalArgumentException(name + " array is empty");

        // Check array is square
        for (int i = 0; i < rows; i++) {
            int columns = Array.getLength(Array.get(array, i));
            if (columns != rows)
                throw new IllegalArgumentException(name + " array is not square, length " + columns + " of row " + i + " is not equal to height " + rows);
        }
    }

    /**
     * Creates a GUI displaying this bingo board.
     *
     * @return GUI inventory
     */
    protected @NotNull Inventory createGUI() {
        if (advancements.length > 6)
            throw new IllegalArgumentException("Can not create a GUI for a board of size " + advancements.length);

        Inventory inventory = Bukkit.createInventory(null, advancements.length * 9, title);

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        filler.editMeta(meta -> meta.displayName(Component.empty()));
        for (int i = 0; i < inventory.getSize(); i++)
            inventory.setItem(i, filler);

        int offset = (9 - advancements.length) / 2;
        for (int r = 0; r < advancements.length; r++) {
            for (int c = 0; c < advancements.length; c++) {
                inventory.setItem(r * 9 + c + offset, createItem(r, c));
            }
        }

        return inventory;
    }

    protected @NotNull ItemStack createItem(int r, int c) {
        Advancement advancement = Bukkit.getAdvancement(advancements[r][c]);
        if (advancement == null)
            throw new IllegalArgumentException("Invalid advancement key: " + advancements[r][c]);

        Material material = Material.GRAY_DYE;
        if (advancement.getDisplay() != null)
            material = advancement.getDisplay().icon().getType();
        if (completed[r][c])
            material = Material.LIME_DYE;
        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            AdvancementDisplay display = advancement.getDisplay();
            List<Component> lore = new LinkedList<>();

            if (display != null) {

                meta.displayName(display.title().color(display.frame().color()));
                lore.add(display.description().color(NamedTextColor.GRAY));

            } else {

                meta.displayName(Component.text(advancement.getKey().toString()));

            }

            if (completed[r][c])
                lore.add(Component.text("COMPLETED", Style.style(NamedTextColor.GREEN, TextDecoration.BOLD)));

            //noinspection DataFlowIssue
            meta.displayName(meta.displayName().decoration(TextDecoration.ITALIC, false));
            meta.lore(lore.stream().map(line -> line.decoration(TextDecoration.ITALIC, false)).toList());
        });

        return item;
    }

    public void updateItem(int r, int c) {
        if (gui == null)
            return;

        int offset = (9 - advancements.length) / 2;
        gui.setItem(r * 9 + c + offset, createItem(r, c));
    }

    public @NotNull Component getTitle() {
        return title;
    }

    public void setTitle(@NotNull Component title) {
        this.title = title;
        gui = null;
    }

    public @NotNull Inventory getGUI() {
        if (gui == null)
            gui = createGUI();
        return gui;
    }

    public int getLength() {
        return advancements.length;
    }

    /**
     * Searches this bingo board for an advancement.
     *
     * @param advancementKey advancement key
     * @return integer array containing the row and column index of the advancement, or {@code null} if it is not present
     */
    protected int @Nullable [] findAdvancement(@NotNull NamespacedKey advancementKey) {
        for (int r = 0; r < advancements.length; r++) {
            for (int c = 0; c < advancements.length; c++) {
                if (advancements[r][c].equals(advancementKey)) {
                    return new int[] {r, c};
                }
            }
        }
        return null;
    }

    /**
     * Marks an advancement as completed.
     *
     * @param advancementKey advancement key
     * @return {@code true} if this board contains the advancement, and it was not already completed, otherwise {@code false}
     */
    public boolean complete(@NotNull NamespacedKey advancementKey) {
        int[] found = findAdvancement(advancementKey);
        if (found == null)
            return false;

        int r = found[0];
        int c = found[1];
        if (completed[r][c])
            return false;

        completed[r][c] = true;
        updateItem(r, c);
        return true;
    }

    public int getRowProgress(int row) {
        int count = 0;
        for (int c = 0; c < completed.length; c++) {
            if (completed[row][c])
                count++;
        }
        return count;
    }

    public int getColumnProgress(int column) {
        int count = 0;
        for (int r = 0; r < completed.length; r++) {
            if (completed[r][column])
                count++;
        }
        return count;
    }

    public int getTopLeftDiagonalProgress() {
        int count = 0;
        for (int i = 0; i < completed.length; i++) {
            if (completed[i][i])
                count++;
        }
        return count;
    }

    public int getTopRightDiagonalProgress() {
        int count = 0;
        for (int i = 0; i < completed.length; i++) {
            if (completed[i][completed.length - 1 - i])
                count++;
        }
        return count;
    }

    private boolean isRowComplete(int row) {
        for (int c = 0; c < completed.length; c++) {
            if (!completed[row][c])
                return false;
        }
        return true;
    }

    private boolean isColumnComplete(int column) {
        for (int r = 0; r < completed.length; r++) {
            if (!completed[r][column])
                return false;
        }
        return true;
    }

    private boolean isTopLeftDiagonalComplete() {
        for (int i = 0; i < completed.length; i++) {
            if (!completed[i][i])
                return false;
        }
        return true;
    }

    private boolean isTopRightDiagonalComplete() {
        for (int i = 0; i < completed.length; i++) {
            if (!completed[i][completed.length - 1 - i])
                return false;
        }
        return true;
    }

    /**
     * Checks if this bingo board is in a winning state.
     *
     * @return {@code true} if the board is winning, {@code false} otherwise
     */
    public boolean isWinning() {
        return getCompletedRows() >= requiredRows;
    }

    public int getCompletedRows() {
        int completedRows = 0;

        for (int i = 0; i < completed.length; i++) {
            if (isRowComplete(i))
                completedRows++;
            if (isColumnComplete(i))
                completedRows++;
        }
        if (isTopLeftDiagonalComplete())
            completedRows++;
        if (isTopRightDiagonalComplete())
            completedRows++;

        return completedRows;
    }

    @SuppressWarnings({"MethodDoesntCallSuperMethod", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    protected BingoBoard clone() {
        NamespacedKey[][] advancementsCopy = Arrays.stream(advancements).map(NamespacedKey[]::clone).toArray(NamespacedKey[][]::new);

        boolean[][] completedCopy = new boolean[completed.length][completed[0].length];
        for (int i = 0; i < completed.length; i++)
            completedCopy[i] = completed[i].clone();

        return new BingoBoard(title, advancementsCopy, completedCopy, requiredRows);
    }

}
