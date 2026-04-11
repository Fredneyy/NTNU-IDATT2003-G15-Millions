package ntnu.idatt2003.group15.view;

import javafx.scene.layout.StackPane;

public interface Dialog {
  void close();
  void show(StackPane root, String title, String message);
}
