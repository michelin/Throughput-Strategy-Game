# Coding Specification: JavaFX Pipeline Workstation UI

## 1. Purpose

Build a JavaFX UI component for a production-flow game where:

* **Backlog** feeds the first workstation.
* **Workstations** process work items.
* **Queues** buffer work between workstations.
* **Finished Goods** receives completed work from the last workstation.
* **Workers** are assigned to workstations.
* **Workers have skills**, initially aligned to workstation colors.
* **Workers can gain new skills** and later move between workstations.
* **Workstations can have upgrades**, including:

  * an automated worker,
  * a pair worker,
  * future upgrades as needed.

The UI should preserve the color-coded identity of the current design while allowing configurable workstation names.

Example:

```text
Default:
GREEN WORKSTATION

Configured:
SPECIFICATIONS
```

The color remains green, but the displayed name becomes configurable.

---

# 2. Conceptual UI Model

## 2.1 Pipeline Elements

The production line should be modeled as four distinct UI concepts.

```text
Backlog → Workstation → Queue → Workstation → Queue → Workstation → Finished Goods
```

| Element        | Meaning                        | Example            |
| -------------- | ------------------------------ | ------------------ |
| Backlog        | Source of unstarted work       | Backlog: 12        |
| Workstation    | Processor that performs work   | Green Workstation  |
| Queue          | Buffer between processors      | Green Queue: 3     |
| Finished Goods | Destination for completed work | Finished Goods: 20 |

## 2.2 Workstation Types

Each workstation has a role in the pipeline.

```java
public enum WorkstationRole {
    FIRST,
    MIDDLE,
    LAST
}
```

| Role   | Input          | Output         |
| ------ | -------------- | -------------- |
| FIRST  | Backlog        | Next queue     |
| MIDDLE | Previous queue | Next queue     |
| LAST   | Previous queue | Finished Goods |

---

# 3. Visual Rules

## 3.1 Preserve workstation colors

The existing color identity should remain.

Example color assignments:

```java
public enum WorkstationColor {
    GREEN,
    VIOLET,
    ROSE,
    BLUE,
    YELLOW
}
```

Each color controls:

* card header color,
* station accent color,
* queue token color,
* default workstation label,
* skill chip color.

## 3.2 Workstation names are configurable

Each workstation has:

```java
String defaultName = color.name() + " WORKSTATION";
String displayName = configuredName != null ? configuredName : defaultName;
```

Example:

```java
WorkstationConfig green = new WorkstationConfig(
    WorkstationColor.GREEN,
    "SPECIFICATIONS"
);
```

Result:

```text
SPECIFICATIONS
```

The workstation remains green, but the visible title changes.

## 3.3 Workers are separate from workstations

Workers should be rendered as movable resources inside workstation slots.

A workstation should not visually imply that the worker and workstation are the same object.

Recommended structure:

```text
┌────────────────────────────┐
│ GREEN WORKSTATION           │
│ Input: Backlog              │
│ Queue / Output              │
│                            │
│ [Human Worker Tile]         │
│ [Automated Worker Tile]     │
│                            │
│ Upgrades: [icons/images]    │
│ Attempts: 1                 │
│ Status: Working             │
└────────────────────────────┘
```

## 3.4 Upgrades should use images

The “Upgrades” row should display images instead of plain text buttons.

Two acceptable approaches:

1. Use `ImageView` inside a clickable container.
2. Use JavaFX `Button` with a graphic image.

The second approach is usually better because it preserves button behavior, focus handling, accessibility, keyboard navigation, and disabled state.

---

# 4. Data Model

## 4.1 Workstation configuration

```java
public class WorkstationConfig {

    private final WorkstationColor color;
    private final String displayName;

    public WorkstationConfig(WorkstationColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public WorkstationColor getColor() {
        return color;
    }

    public String getDisplayName() {
        if (displayName == null || displayName.isBlank()) {
            return color.name() + " WORKSTATION";
        }
        return displayName;
    }
}
```

Usage:

```java
WorkstationConfig config = new WorkstationConfig(
    WorkstationColor.GREEN,
    "SPECIFICATIONS"
);
```

If no name is supplied:

```java
WorkstationConfig config = new WorkstationConfig(
    WorkstationColor.GREEN,
    null
);
```

Displays:

```text
GREEN WORKSTATION
```

---

## 4.2 Workstation view model

```java
public class WorkstationViewModel {

    private final ObjectProperty<WorkstationColor> color =
        new SimpleObjectProperty<>();

    private final StringProperty displayName =
        new SimpleStringProperty();

    private final ObjectProperty<WorkstationRole> role =
        new SimpleObjectProperty<>();

    private final IntegerProperty inputCount =
        new SimpleIntegerProperty();

    private final IntegerProperty outputCount =
        new SimpleIntegerProperty();

    private final ObjectProperty<WorkerViewModel> humanWorker =
        new SimpleObjectProperty<>();

    private final ObjectProperty<WorkerViewModel> automatedWorker =
        new SimpleObjectProperty<>();

    private final BooleanProperty hasAutoUpgrade =
        new SimpleBooleanProperty(false);

    private final BooleanProperty hasPairUpgrade =
        new SimpleBooleanProperty(false);

    private final IntegerProperty attempts =
        new SimpleIntegerProperty(1);

    private final StringProperty status =
        new SimpleStringProperty("Idle");

    public ObjectProperty<WorkstationColor> colorProperty() {
        return color;
    }

    public StringProperty displayNameProperty() {
        return displayName;
    }

    public ObjectProperty<WorkstationRole> roleProperty() {
        return role;
    }

    public IntegerProperty inputCountProperty() {
        return inputCount;
    }

    public IntegerProperty outputCountProperty() {
        return outputCount;
    }

    public ObjectProperty<WorkerViewModel> humanWorkerProperty() {
        return humanWorker;
    }

    public ObjectProperty<WorkerViewModel> automatedWorkerProperty() {
        return automatedWorker;
    }

    public BooleanProperty hasAutoUpgradeProperty() {
        return hasAutoUpgrade;
    }

    public BooleanProperty hasPairUpgradeProperty() {
        return hasPairUpgrade;
    }

    public IntegerProperty attemptsProperty() {
        return attempts;
    }

    public StringProperty statusProperty() {
        return status;
    }
}
```

---

## 4.3 Worker view model

```java
public class WorkerViewModel {

    private final StringProperty name =
        new SimpleStringProperty();

    private final ObjectProperty<WorkstationColor> originalSkill =
        new SimpleObjectProperty<>();

    private final ObservableSet<WorkstationColor> currentSkills =
        FXCollections.observableSet();

    private final BooleanProperty automated =
        new SimpleBooleanProperty(false);

    public WorkerViewModel(
        String name,
        WorkstationColor originalSkill,
        boolean automated
    ) {
        this.name.set(name);
        this.originalSkill.set(originalSkill);
        this.automated.set(automated);
        this.currentSkills.add(originalSkill);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObjectProperty<WorkstationColor> originalSkillProperty() {
        return originalSkill;
    }

    public ObservableSet<WorkstationColor> getCurrentSkills() {
        return currentSkills;
    }

    public BooleanProperty automatedProperty() {
        return automated;
    }
}
```

---

# 5. Pipeline Layout

## 5.1 Recommended layout

Use an `HBox` for the full production line.

```java
HBox pipeline = new HBox(16);
pipeline.setAlignment(Pos.CENTER);
pipeline.getStyleClass().add("pipeline-root");
```

The pipeline should contain alternating nodes:

```text
BacklogView
ArrowView
WorkstationView
ArrowView
QueueView
ArrowView
WorkstationView
ArrowView
QueueView
ArrowView
WorkstationView
ArrowView
FinishedGoodsView
```

Example:

```java
pipeline.getChildren().addAll(
    new BacklogView(backlogModel),
    new ArrowView(),

    new WorkstationView(greenStation),
    new ArrowView(),

    new QueueView(greenQueue),
    new ArrowView(),

    new WorkstationView(violetStation),
    new ArrowView(),

    new QueueView(violetQueue),
    new ArrowView(),

    new WorkstationView(yellowStation),
    new ArrowView(),

    new FinishedGoodsView(finishedGoodsModel)
);
```

---

# 6. Workstation View

## 6.1 View structure

```java
public class WorkstationView extends VBox {

    private final WorkstationViewModel model;

    public WorkstationView(WorkstationViewModel model) {
        this.model = model;

        getStyleClass().add("workstation-card");
        getStyleClass().add(colorStyle(model.colorProperty().get()));

        setSpacing(8);
        setPadding(new Insets(10));
        setPrefWidth(260);

        getChildren().addAll(
            buildHeader(),
            buildInputSection(),
            buildWorkerSection(),
            buildUpgradeSection(),
            buildStatusSection(),
            buildOutputSection()
        );
    }

    private Node buildHeader() {
        Label title = new Label();
        title.textProperty().bind(model.displayNameProperty());
        title.getStyleClass().add("workstation-title");

        HBox header = new HBox(title);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("workstation-header");

        return header;
    }

    private Node buildInputSection() {
        Label inputLabel = new Label();
        inputLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            WorkstationRole role = model.roleProperty().get();

            return switch (role) {
                case FIRST -> "Input: Backlog";
                case MIDDLE, LAST -> "Input Queue";
            };
        }, model.roleProperty()));

        Label count = new Label();
        count.textProperty().bind(model.inputCountProperty().asString());

        HBox row = new HBox(8, inputLabel, count);
        row.getStyleClass().add("workstation-input-row");

        return row;
    }

    private Node buildWorkerSection() {
        HBox workers = new HBox(8);
        workers.setAlignment(Pos.CENTER_LEFT);

        workers.getChildren().add(new WorkerSlotView(model.humanWorkerProperty()));
        workers.getChildren().add(new WorkerSlotView(model.automatedWorkerProperty()));

        return workers;
    }

    private Node buildUpgradeSection() {
        HBox upgrades = new HBox(8);
        upgrades.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Upgrades:");
        label.getStyleClass().add("section-label");

        UpgradeButton auto = new UpgradeButton(
            "Auto",
            "/images/upgrades/auto.png",
            model.hasAutoUpgradeProperty()
        );

        UpgradeButton pair = new UpgradeButton(
            "Pair",
            "/images/upgrades/pair.png",
            model.hasPairUpgradeProperty()
        );

        upgrades.getChildren().addAll(label, auto, pair);

        return upgrades;
    }

    private Node buildStatusSection() {
        Label attempts = new Label();
        attempts.textProperty().bind(
            model.attemptsProperty().asString("Attempts: %d")
        );

        Label status = new Label();
        status.textProperty().bind(
            Bindings.concat("Status: ", model.statusProperty())
        );

        HBox row = new HBox(16, attempts, status);
        row.getStyleClass().add("status-row");

        return row;
    }

    private Node buildOutputSection() {
        Label output = new Label();
        output.textProperty().bind(Bindings.createStringBinding(() -> {
            WorkstationRole role = model.roleProperty().get();

            return switch (role) {
                case FIRST, MIDDLE -> "Output: Next Queue";
                case LAST -> "Output: Finished Goods";
            };
        }, model.roleProperty()));

        HBox row = new HBox(output);
        row.getStyleClass().add("workstation-output-row");

        return row;
    }

    private String colorStyle(WorkstationColor color) {
        return "workstation-" + color.name().toLowerCase();
    }
}
```

---

# 7. Worker Slot View

## 7.1 Purpose

A worker slot represents the place where a human or automated worker appears.

If the slot is empty, show an empty placeholder.

```java
public class WorkerSlotView extends StackPane {

    private final ObjectProperty<WorkerViewModel> workerProperty;

    public WorkerSlotView(ObjectProperty<WorkerViewModel> workerProperty) {
        this.workerProperty = workerProperty;

        getStyleClass().add("worker-slot");
        setMinSize(100, 120);

        workerProperty.addListener((obs, oldWorker, newWorker) -> {
            render();
        });

        render();
    }

    private void render() {
        getChildren().clear();

        WorkerViewModel worker = workerProperty.get();

        if (worker == null) {
            getChildren().add(buildEmptySlot());
        } else {
            getChildren().add(new WorkerTile(worker));
        }
    }

    private Node buildEmptySlot() {
        Label empty = new Label("Empty\nSlot");
        empty.getStyleClass().add("empty-worker-slot");
        empty.setTextAlignment(TextAlignment.CENTER);
        return empty;
    }
}
```

---

## 7.2 Worker tile

```java
public class WorkerTile extends VBox {

    public WorkerTile(WorkerViewModel worker) {
        setSpacing(6);
        setPadding(new Insets(8));
        getStyleClass().add("worker-tile");

        if (worker.automatedProperty().get()) {
            getStyleClass().add("automated-worker");
        } else {
            getStyleClass().add("human-worker");
        }

        Label name = new Label();
        name.textProperty().bind(worker.nameProperty());
        name.getStyleClass().add("worker-name");

        ImageView icon = buildWorkerIcon(worker);

        HBox skills = buildSkills(worker);

        getChildren().addAll(icon, name, skills);
    }

    private ImageView buildWorkerIcon(WorkerViewModel worker) {
        String imagePath = worker.automatedProperty().get()
            ? "/images/workers/robot.png"
            : "/images/workers/human.png";

        Image image = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream(imagePath))
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);

        return imageView;
    }

    private HBox buildSkills(WorkerViewModel worker) {
        HBox skills = new HBox(4);
        skills.setAlignment(Pos.CENTER_LEFT);

        for (WorkstationColor skill : worker.getCurrentSkills()) {
            Label chip = new Label(skill.name().substring(0, 1));
            chip.getStyleClass().add("skill-chip");
            chip.getStyleClass().add("skill-" + skill.name().toLowerCase());

            Tooltip.install(chip, new Tooltip(skill.name() + " skill"));

            skills.getChildren().add(chip);
        }

        return skills;
    }
}
```

---

# 8. Upgrade Images

## 8.1 Preferred implementation: image button

Use a JavaFX `Button` with an `ImageView` graphic.

```java
public class UpgradeButton extends Button {

    public UpgradeButton(
        String tooltipText,
        String imageResourcePath,
        BooleanProperty activeProperty
    ) {
        Image image = new Image(
            Objects.requireNonNull(
                getClass().getResourceAsStream(imageResourcePath)
            )
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(28);
        imageView.setFitHeight(28);
        imageView.setPreserveRatio(true);

        setGraphic(imageView);
        setText(null);

        getStyleClass().add("upgrade-button");

        activeProperty.addListener((obs, wasActive, isActive) -> {
            pseudoClassStateChanged(
                PseudoClass.getPseudoClass("active"),
                isActive
            );
        });

        pseudoClassStateChanged(
            PseudoClass.getPseudoClass("active"),
            activeProperty.get()
        );

        Tooltip.install(this, new Tooltip(tooltipText));

        setOnAction(event -> activeProperty.set(!activeProperty.get()));
    }
}
```

This gives you clickable image-based upgrades without losing button behavior.

---

## 8.2 Alternative: image-only node

Use this only if the upgrade image is not interactive.

```java
public class UpgradeIcon extends StackPane {

    public UpgradeIcon(String imageResourcePath, String tooltipText) {
        Image image = new Image(
            Objects.requireNonNull(
                getClass().getResourceAsStream(imageResourcePath)
            )
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(28);
        imageView.setFitHeight(28);
        imageView.setPreserveRatio(true);

        getChildren().add(imageView);
        getStyleClass().add("upgrade-icon");

        Tooltip.install(this, new Tooltip(tooltipText));
    }
}
```

---

# 9. Queue View

Queues should appear between workstations.

```java
public class QueueView extends VBox {

    public QueueView(String queueName, WorkstationColor color, IntegerProperty count) {
        getStyleClass().add("queue-card");
        getStyleClass().add("queue-" + color.name().toLowerCase());

        setAlignment(Pos.CENTER);
        setSpacing(8);

        Label title = new Label(queueName);
        title.getStyleClass().add("queue-title");

        Label value = new Label();
        value.textProperty().bind(count.asString());
        value.getStyleClass().add("queue-count");

        HBox tokens = new HBox(4);
        tokens.setAlignment(Pos.CENTER);

        count.addListener((obs, oldValue, newValue) -> {
            renderTokens(tokens, color, newValue.intValue());
        });

        renderTokens(tokens, color, count.get());

        getChildren().addAll(title, value, tokens);
    }

    private void renderTokens(HBox container, WorkstationColor color, int count) {
        container.getChildren().clear();

        int visibleTokens = Math.min(count, 5);

        for (int i = 0; i < visibleTokens; i++) {
            Circle token = new Circle(5);
            token.getStyleClass().add("queue-token");
            token.getStyleClass().add("token-" + color.name().toLowerCase());
            container.getChildren().add(token);
        }

        if (count > visibleTokens) {
            Label overflow = new Label("+" + (count - visibleTokens));
            overflow.getStyleClass().add("queue-overflow");
            container.getChildren().add(overflow);
        }
    }
}
```

---

# 10. Backlog and Finished Goods Views

## 10.1 Backlog

```java
public class BacklogView extends VBox {

    public BacklogView(IntegerProperty backlogCount) {
        getStyleClass().add("endpoint-card");
        getStyleClass().add("backlog-card");

        setAlignment(Pos.CENTER);
        setSpacing(8);

        Label title = new Label("BACKLOG");
        title.getStyleClass().add("endpoint-title");

        ImageView icon = loadIcon("/images/endpoints/backlog.png");

        Label count = new Label();
        count.textProperty().bind(backlogCount.asString());
        count.getStyleClass().add("endpoint-count");

        getChildren().addAll(title, icon, count);
    }

    private ImageView loadIcon(String path) {
        Image image = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream(path))
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);

        return imageView;
    }
}
```

## 10.2 Finished Goods

```java
public class FinishedGoodsView extends VBox {

    public FinishedGoodsView(IntegerProperty finishedGoodsCount) {
        getStyleClass().add("endpoint-card");
        getStyleClass().add("finished-card");

        setAlignment(Pos.CENTER);
        setSpacing(8);

        Label title = new Label("FINISHED\nGOODS");
        title.getStyleClass().add("endpoint-title");
        title.setTextAlignment(TextAlignment.CENTER);

        ImageView icon = loadIcon("/images/endpoints/finished-goods.png");

        Label count = new Label();
        count.textProperty().bind(finishedGoodsCount.asString());
        count.getStyleClass().add("endpoint-count");

        getChildren().addAll(title, icon, count);
    }

    private ImageView loadIcon(String path) {
        Image image = new Image(
            Objects.requireNonNull(getClass().getResourceAsStream(path))
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        imageView.setPreserveRatio(true);

        return imageView;
    }
}
```

---

# 11. Arrow View

```java
public class ArrowView extends Label {

    public ArrowView() {
        super("➜");
        getStyleClass().add("flow-arrow");
        setAlignment(Pos.CENTER);
    }
}
```

For a more polished option, replace the label with SVG later.

---

# 12. CSS Specification

## 12.1 Base layout

```css
.pipeline-root {
    -fx-background-color: #f4f6f8;
    -fx-padding: 18;
    -fx-spacing: 16;
}

.flow-arrow {
    -fx-font-size: 28px;
    -fx-text-fill: #4b5563;
}
```

---

## 12.2 Workstation card

```css
.workstation-card {
    -fx-background-color: white;
    -fx-background-radius: 10;
    -fx-border-radius: 10;
    -fx-border-width: 2;
    -fx-padding: 10;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 8, 0.2, 0, 2);
}

.workstation-header {
    -fx-padding: 6 8 6 8;
    -fx-background-radius: 8 8 0 0;
}

.workstation-title {
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-text-fill: white;
}

.workstation-input-row,
.workstation-output-row,
.status-row {
    -fx-font-size: 12px;
    -fx-text-fill: #111827;
}

.section-label {
    -fx-font-size: 12px;
    -fx-font-weight: bold;
}
```

---

## 12.3 Workstation colors

```css
.workstation-green {
    -fx-border-color: #2e7d32;
}

.workstation-green .workstation-header {
    -fx-background-color: #2e7d32;
}

.workstation-violet {
    -fx-border-color: #6a3fa0;
}

.workstation-violet .workstation-header {
    -fx-background-color: #6a3fa0;
}

.workstation-rose {
    -fx-border-color: #d6336c;
}

.workstation-rose .workstation-header {
    -fx-background-color: #d6336c;
}

.workstation-blue {
    -fx-border-color: #1565c0;
}

.workstation-blue .workstation-header {
    -fx-background-color: #1565c0;
}

.workstation-yellow {
    -fx-border-color: #f4b400;
}

.workstation-yellow .workstation-header {
    -fx-background-color: #f4b400;
}

.workstation-yellow .workstation-title {
    -fx-text-fill: #111827;
}
```

---

## 12.4 Worker tiles

```css
.worker-slot {
    -fx-background-color: #f8fafc;
    -fx-background-radius: 8;
    -fx-border-color: #cbd5e1;
    -fx-border-radius: 8;
    -fx-border-style: dashed;
}

.empty-worker-slot {
    -fx-text-fill: #9ca3af;
    -fx-font-size: 12px;
}

.worker-tile {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-border-color: #d1d5db;
    -fx-border-radius: 8;
    -fx-padding: 8;
}

.human-worker {
    -fx-border-color: #64748b;
}

.automated-worker {
    -fx-border-color: #2563eb;
}

.worker-name {
    -fx-font-size: 12px;
    -fx-font-weight: bold;
}
```

---

## 12.5 Skill chips

```css
.skill-chip {
    -fx-background-radius: 999;
    -fx-padding: 2 6 2 6;
    -fx-font-size: 10px;
    -fx-font-weight: bold;
    -fx-text-fill: white;
}

.skill-green {
    -fx-background-color: #2e7d32;
}

.skill-violet {
    -fx-background-color: #6a3fa0;
}

.skill-rose {
    -fx-background-color: #d6336c;
}

.skill-blue {
    -fx-background-color: #1565c0;
}

.skill-yellow {
    -fx-background-color: #f4b400;
    -fx-text-fill: #111827;
}
```

---

## 12.6 Upgrade image buttons

```css
.upgrade-button {
    -fx-background-color: #f8fafc;
    -fx-background-radius: 6;
    -fx-border-color: #cbd5e1;
    -fx-border-radius: 6;
    -fx-padding: 4;
    -fx-cursor: hand;
}

.upgrade-button:hover {
    -fx-background-color: #e5e7eb;
}

.upgrade-button:active {
    -fx-background-color: #dcfce7;
    -fx-border-color: #16a34a;
}

.upgrade-icon {
    -fx-padding: 4;
}
```

---

## 12.7 Queue cards

```css
.queue-card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-border-radius: 8;
    -fx-border-width: 2;
    -fx-padding: 10;
    -fx-min-width: 90;
}

.queue-title {
    -fx-font-size: 12px;
    -fx-font-weight: bold;
}

.queue-count {
    -fx-font-size: 24px;
    -fx-font-weight: bold;
}

.queue-green {
    -fx-border-color: #2e7d32;
}

.queue-violet {
    -fx-border-color: #6a3fa0;
}

.queue-rose {
    -fx-border-color: #d6336c;
}

.queue-blue {
    -fx-border-color: #1565c0;
}

.queue-yellow {
    -fx-border-color: #f4b400;
}

.token-green {
    -fx-fill: #2e7d32;
}

.token-violet {
    -fx-fill: #6a3fa0;
}

.token-rose {
    -fx-fill: #d6336c;
}

.token-blue {
    -fx-fill: #1565c0;
}

.token-yellow {
    -fx-fill: #f4b400;
}

.queue-overflow {
    -fx-font-size: 10px;
    -fx-font-weight: bold;
}
```

---

## 12.8 Endpoints

```css
.endpoint-card {
    -fx-background-color: white;
    -fx-background-radius: 8;
    -fx-border-radius: 8;
    -fx-border-width: 2;
    -fx-padding: 10;
    -fx-min-width: 100;
}

.backlog-card {
    -fx-border-color: #6b7280;
}

.finished-card {
    -fx-border-color: #1565c0;
}

.endpoint-title {
    -fx-font-size: 13px;
    -fx-font-weight: bold;
}

.endpoint-count {
    -fx-font-size: 24px;
    -fx-font-weight: bold;
}
```

---

# 13. Example Assembly

```java
IntegerProperty backlogCount = new SimpleIntegerProperty(12);
IntegerProperty greenQueueCount = new SimpleIntegerProperty(3);
IntegerProperty violetQueueCount = new SimpleIntegerProperty(6);
IntegerProperty finishedGoodsCount = new SimpleIntegerProperty(20);

WorkerViewModel alex = new WorkerViewModel(
    "Alex",
    WorkstationColor.GREEN,
    false
);

WorkerViewModel greenBot = new WorkerViewModel(
    "Auto",
    WorkstationColor.GREEN,
    true
);

WorkstationViewModel green = new WorkstationViewModel();
green.colorProperty().set(WorkstationColor.GREEN);
green.displayNameProperty().set("SPECIFICATIONS");
green.roleProperty().set(WorkstationRole.FIRST);
green.inputCountProperty().bind(backlogCount);
green.outputCountProperty().bind(greenQueueCount);
green.humanWorkerProperty().set(alex);
green.automatedWorkerProperty().set(greenBot);
green.hasAutoUpgradeProperty().set(true);
green.hasPairUpgradeProperty().set(false);
green.attemptsProperty().set(1);
green.statusProperty().set("Working");

HBox pipeline = new HBox(16);
pipeline.setAlignment(Pos.CENTER);
pipeline.getStyleClass().add("pipeline-root");

pipeline.getChildren().addAll(
    new BacklogView(backlogCount),
    new ArrowView(),
    new WorkstationView(green),
    new ArrowView(),
    new QueueView("GREEN QUEUE", WorkstationColor.GREEN, greenQueueCount),
    new ArrowView(),
    new QueueView("VIOLET QUEUE", WorkstationColor.VIOLET, violetQueueCount),
    new ArrowView(),
    new FinishedGoodsView(finishedGoodsCount)
);
```

---

# 14. Resource Folder Structure

Recommended layout:

```text
src/main/resources/
servers/
  server_black.jpg
  robot_green.jpg
  robot_rose.jpg
  robot_yellow.jpg
  server_pair.jpg
  pair.png
```

Load the stylesheet:


---

# 15. Upgrade Image Behavior

## 15.1 Auto upgrade

When active:

* show the auto image in active state,
* show an automated worker tile if assigned,
* optionally show “Auto: ON”.

```java
autoButton.disableProperty().bind(
    model.hasAutoUpgradeProperty().not()
);
```

Alternative: allow the image button to purchase or toggle the upgrade:

```java
autoButton.setOnAction(event -> {
    if (!model.hasAutoUpgradeProperty().get()) {
        gameService.purchaseAutoUpgrade(model);
    }
});
```

## 15.2 Pair upgrade

When active:

* show pair image in active state,
* set attempts to `2`,
* do not display the pair worker as a producer,
* clarify that pair gives the human worker two chances to succeed.

```java
model.attemptsProperty().bind(
    Bindings.when(model.hasPairUpgradeProperty())
        .then(2)
        .otherwise(1)
);
```

---

# 16. Naming Behavior

## 16.1 Workstation display name rule

```java
public static String resolveWorkstationName(
    WorkstationColor color,
    String configuredName
) {
    if (configuredName == null || configuredName.isBlank()) {
        return color.name() + " WORKSTATION";
    }

    return configuredName;
}
```

Examples:

```java
resolveWorkstationName(WorkstationColor.GREEN, null);
// GREEN WORKSTATION

resolveWorkstationName(WorkstationColor.GREEN, "");
// GREEN WORKSTATION

resolveWorkstationName(WorkstationColor.GREEN, "SPECIFICATIONS");
// SPECIFICATIONS

resolveWorkstationName(WorkstationColor.ROSE, "QA REVIEW");
// QA REVIEW
```

---

# 17. Acceptance Criteria

The implementation is complete when:

1. The UI renders the production flow from Backlog to Finished Goods.
2. The first workstation clearly shows that it is fed by Backlog.
3. Middle workstations clearly show that they are fed by a previous queue and feed the next queue.
4. The last workstation clearly shows that it feeds Finished Goods.
5. Workstation colors remain visually consistent.
6. Workstation names are configurable.
7. If no workstation name is provided, the default is `{COLOR} WORKSTATION`.
8. Workers appear as separate assignable entities within workstation slots.
9. Human and automated workers are visually distinct.
10. Worker skill chips show one or more skills.
11. Upgrade controls use images or image-based buttons.
12. Upgrade active/inactive states are visually clear.
13. Pair upgrade changes attempts from `1` to `2`.
14. Queue counts update dynamically through JavaFX bindings.
15. Backlog and Finished Goods are visually distinct from queues and workstations.

---

# 18. Core Design Rule

Use this separation throughout the code and UI:

```text
Endpoints hold source/destination counts.
Queues hold waiting work.
Workstations process work.
Workers are movable resources.
Skills belong to workers.
Upgrades belong to workstations.
Colors identify the original workstation/skill family.
Names are configurable display labels.
```

