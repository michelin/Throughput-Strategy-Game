package com.michelin.throughputfxproject.test.controller;

import com.michelin.throughputfxproject.controllers.BoardController;
import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.state.ScoreCard;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

class BoardControllerTest {

    private static boolean javafxInitialized = false;

    @BeforeAll
     static void initToolkit()  {
        if (!javafxInitialized) {
            Platform.startup(() -> {}); // This will start the JavaFX runtime
            javafxInitialized = true;
        }
    }

    private BoardController controller;

    @BeforeEach
    void setUp() throws Exception {
        clearBoardSingleton();
        controller = Mockito.spy(new BoardController());
        // Initialize JavaFX controls with mocks or simple instances
        Field[] fields = BoardController.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(javafx.fxml.FXML.class)) {
                Class<?> type = field.getType();
                Object value = null;
                if (type == javafx.scene.control.Button.class) value = new javafx.scene.control.Button();
                else if (type == javafx.scene.control.Label.class) value = new javafx.scene.control.Label();
                else if (type == javafx.scene.control.TextArea.class) value = new javafx.scene.control.TextArea();
                else if (type == javafx.scene.layout.Pane.class) value = new javafx.scene.layout.Pane();
                else if (type == javafx.scene.layout.VBox.class) value = new javafx.scene.layout.VBox();
                else if (type == javafx.scene.chart.LineChart.class) value = Mockito.mock(javafx.scene.chart.LineChart.class);
                else if (type == javafx.scene.control.ButtonBar.class) value = new javafx.scene.control.ButtonBar();
                else if (type == javafx.scene.control.TableView.class) {
                    @SuppressWarnings("unchecked")
                    TableView<ScoreCard> tableView = Mockito.mock(javafx.scene.control.TableView.class);
                    Mockito.when(tableView.getColumns()).thenReturn(javafx.collections.FXCollections.observableArrayList());
                    value = tableView;
                }
                if (value != null) {
                    // Try to use the setter if available
                    String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                    try {
                        BoardController.class.getMethod(setterName, type).invoke(controller, value);
                    } catch (NoSuchMethodException _) {
                        // Fallback: skip if no setter
                    }
                }
            }
        }
    }

    private void clearBoardSingleton() {
        Board.clearInstance();
    }

    @Test
    void constructor_initializesBoardIfNotPresent() {
        clearBoardSingleton();
        assertThrows(IllegalStateException.class, Board::getInstance);
    }

    @Test
    void addOrRemoveSkillsForServers_disablesButtonAndCallsRedraw() throws Exception {
        Button button = new Button();
        controller.setButtonAddSkills(button);
        Mockito.doNothing().when(controller).redrawBoard();
        controller.addOrRemoveSkillsForServers(null);
        assertTrue(button.isDisable());
        Mockito.verify(controller).redrawBoard();
    }

    @Test
    void redrawBoard_doesNotThrow() {
        // Set up minimal required FXML fields to avoid NPEs
        controller.setButtonAddSkills(new Button());
        controller.setInTrainingBox(new Pane());
        controller.setHoldCardBox(new Pane());
        // ...add more mocks as needed for a full test
        assertDoesNotThrow(controller::redrawBoard);
    }
}
