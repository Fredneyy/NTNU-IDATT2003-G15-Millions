package ntnu.idatt2003.group15.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ntnu.idatt2003.group15.view.dialog.NewsDialog;

/**
 * Top-right notification stack. Mount once into the game root StackPane;
 * each {@link NewsDialog#showIn(NewsContainer)} call inserts the dialog at
 * index 0 so the newest notification appears on top and pushes older ones
 * down. Behaves like a flex column: items size to their content and stack
 * vertically with consistent spacing.
 */
public class NewsContainer {

    private final VBox stack = new VBox();

    public NewsContainer() {
        stack.getStyleClass().add("news-container");
        stack.setSpacing(12);
        stack.setAlignment(Pos.TOP_RIGHT);

        // Don't intercept clicks in the empty parts of the column — only the
        // actual notification cards should consume mouse events.
        stack.setPickOnBounds(false);

        stack.setMaxWidth(Region.USE_PREF_SIZE);
        stack.setMaxHeight(Region.USE_PREF_SIZE);
    }

    /** Add this container to the given root if not already present. Top-right. */
    public void mountIn(StackPane root) {
        if (root == null) return;
        if (!root.getChildren().contains(stack)) {
            root.getChildren().add(stack);
            StackPane.setAlignment(stack, Pos.TOP_RIGHT);
            StackPane.setMargin(stack, new Insets(20, 20, 0, 0));
        }
    }

    /** Detach from the parent root. Children are dropped. */
    public void unmount() {
        if (stack.getParent() instanceof javafx.scene.layout.Pane parent) {
            parent.getChildren().remove(stack);
        }
        stack.getChildren().clear();
    }

    /** Insert a dialog node at the top of the stack. Older ones get pushed down. */
    public void pushTop(Node dialogNode) {
        if (!stack.getChildren().contains(dialogNode)) {
            stack.getChildren().add(0, dialogNode);
        }
    }

    /** Remove a dialog node from the stack (no animation; caller usually animates first). */
    public void remove(Node dialogNode) {
        stack.getChildren().remove(dialogNode);
    }

    /** Drop every notification currently shown. */
    public void clear() {
        stack.getChildren().clear();
    }

    public int count() { return stack.getChildren().size(); }

    public VBox getView() { return stack; }
}
