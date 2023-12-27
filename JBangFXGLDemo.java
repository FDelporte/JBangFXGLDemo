///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS com.github.almasb:fxgl:17.3
//FILES resources/

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameScene;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.components.AutoRotationComponent;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;

import java.awt.*;
import java.util.Map;

import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.IrremovableComponent;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class JBangFXGLDemo extends GameApplication {

    /**
     * Reference to the factory which will defines how all the types must be created.
     */
    private final GameFactory gameFactory = new GameFactory();

    /**
     * Player object we are going to use to provide to the factory, so it can start a bullet from the player center.
     */
    private Entity player;

    private static double screenWidth;
    private static double screenHeight;

    /**
     * Main entry point where the application starts.
     *
     * @param args Start-up arguments
     */
    public static void main(String[] args) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.getWidth();
        screenHeight = screenSize.getHeight();
        launch(args);
    }

    /**
     * General game settings.
     *
     * @param settings The settings of the game which can be further extended here.
     */
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setHeight((int) screenHeight);
        settings.setWidth((int) screenWidth);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setTitle("FXGL with JBang");
    }

    /**
     * Input configuration, here you configure all the input events like key presses, mouse clicks, etc.
     */
    @Override
    protected void initInput() {
        onKey(KeyCode.LEFT, "left", () -> this.player.getComponent(PlayerComponent.class).left());
        onKey(KeyCode.RIGHT, "right", () -> this.player.getComponent(PlayerComponent.class).right());
        onKey(KeyCode.UP, "up", () -> this.player.getComponent(PlayerComponent.class).up());
        onKey(KeyCode.DOWN, "down", () -> this.player.getComponent(PlayerComponent.class).down());
        onKeyDown(KeyCode.SPACE, "Bullet", () -> this.player.getComponent(PlayerComponent.class).shoot());
    }

    /**
     * General game variables. Used to hold the points and lives.
     *
     * @param vars The variables of the game which can be further extended here.
     */
    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("lives", 5);
    }

    /**
     * Initialization of the game by providing the {@link EntityFactory}.
     */
    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(this.gameFactory);
        spawn("background", new SpawnData(0, 0).put("width", getAppWidth())
                .put("height", getAppHeight()));
        int circleRadius = 80;
        spawn("center", new SpawnData((getAppWidth() / 2D) - (circleRadius / 2D), (getAppHeight() / 2D) - (circleRadius / 2D))
                .put("x", (circleRadius / 2))
                .put("y", (circleRadius / 2))
                .put("radius", circleRadius));

        // Add the player
        this.player = spawn("duke", 0, 0);
    }

    /**
     * Initialization of the collision handlers.
     */
    @Override
    protected void initPhysics() {
        onCollisionBegin(GameFactory.EntityType.DUKE,
                GameFactory.EntityType.CENTER,
                (duke, center) -> this.player.getComponent(PlayerComponent.class).die());
        onCollisionBegin(GameFactory.EntityType.DUKE,
                GameFactory.EntityType.CLOUD,
                (duke, cloud) -> this.player.getComponent(PlayerComponent.class).die());
        onCollisionBegin(GameFactory.EntityType.BULLET,
                GameFactory.EntityType.CLOUD,
                (bullet, cloud) -> {
                    inc("score", 1);
                    bullet.removeFromWorld();
                    cloud.removeFromWorld();
                });
    }

    /**
     * Configuration of the user interface.
     */
    @Override
    protected void initUI() {
        Text scoreLabel = getUIFactoryService().newText("Score", Color.BLACK, 22);
        Text scoreValue = getUIFactoryService().newText("", Color.BLACK, 22);
        Text livesLabel = getUIFactoryService().newText("Lives", Color.BLACK, 22);
        Text livesValue = getUIFactoryService().newText("", Color.BLACK, 22);

        scoreLabel.setTranslateX(20);
        scoreLabel.setTranslateY(20);

        scoreValue.setTranslateX(90);
        scoreValue.setTranslateY(20);

        livesLabel.setTranslateX(getAppWidth() - 150);
        livesLabel.setTranslateY(20);

        livesValue.setTranslateX(getAppWidth() - 80);
        livesValue.setTranslateY(20);

        scoreValue.textProperty().bind(getWorldProperties().intProperty("score").asString());
        livesValue.textProperty().bind(getWorldProperties().intProperty("lives").asString());

        getGameScene().addUINodes(scoreLabel, scoreValue, livesLabel, livesValue);
    }

    /**
     * Gets called every frame _only_ in Play state.
     */
    @Override
    protected void onUpdate(double tpf) {
        if (getGameWorld().getEntitiesByType(GameFactory.EntityType.CLOUD).size() < 10) {
            spawn("cloud", getAppWidth() / 2D, getAppHeight() / 2D);
        }
    }

    public static class GameFactory implements EntityFactory {

        /**
         * Types of objects we are going to use in our game.
         */
        public enum EntityType {
            BACKGROUND, CENTER, DUKE, CLOUD, BULLET
        }

        @Spawns("background")
        public Entity spawnBackground(SpawnData data) {
            return entityBuilder(data)
                    .type(EntityType.BACKGROUND)
                    .view(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height"), Color.YELLOWGREEN))
                    .with(new IrremovableComponent())
                    .zIndex(-100)
                    .build();
        }

        @Spawns("center")
        public Entity spawnCenter(SpawnData data) {
            return entityBuilder(data)
                    .type(EntityType.CENTER)
                    .collidable()
                    .viewWithBBox(new Circle(data.<Integer>get("x"), data.<Integer>get("y"), data.<Integer>get("radius"), Color.DARKRED))
                    .with(new IrremovableComponent())
                    .zIndex(-99)
                    .build();
        }

        @Spawns("duke")
        public Entity newDuke(SpawnData data) {
            return entityBuilder(data)
                    .type(EntityType.DUKE)
                    .viewWithBBox(texture("duke.png", 50, 50))
                    .collidable()
                    .with((new AutoRotationComponent()).withSmoothing())
                    .with(new PlayerComponent())
                    .build();
        }

        @Spawns("cloud")
        public Entity newCloud(SpawnData data) {
            return entityBuilder(data)
                    .type(EntityType.CLOUD)
                    .viewWithBBox(texture("cloud-network.png", 50, 50))
                    .with((new AutoRotationComponent()).withSmoothing())
                    .with(new CloudComponent())
                    .collidable()
                    .build();
        }

        @Spawns("bullet")
        public Entity newBullet(SpawnData data) {
            return entityBuilder(data)
                    .type(EntityType.BULLET)
                    .viewWithBBox(texture("sprite_bullet.png", 22, 11))
                    .collidable()
                    .with(new ProjectileComponent(data.get("direction"), 350), new OffscreenCleanComponent())
                    .build();
        }
    }

    public static class PlayerComponent extends Component {

        private static final double ROTATION_CHANGE = 0.01;

        private Point2D direction = new Point2D(1, 1);

        @Override
        public void onUpdate(double tpf) {
            entity.translate(direction.multiply(1));
            checkForBounds();
        }

        private void checkForBounds() {
            if (entity.getX() < 0) {
                die();
            }
            if (entity.getX() >= getAppWidth()) {
                die();
            }
            if (entity.getY() < 0) {
                die();
            }
            if (entity.getY() >= getAppHeight()) {
                die();
            }
        }

        public void shoot() {
            spawn("bullet", new SpawnData(
                    getEntity().getPosition().getX() + 20,
                    getEntity().getPosition().getY() - 5)
                    .put("direction", direction));
        }

        public void die() {
            inc("lives", -1);

            if (geti("lives") <= 0) {
                getDialogService().showMessageBox("Game Over",
                        () -> getGameController().startNewGame());
                return;
            }

            entity.setPosition(0, 0);
            direction = new Point2D(1, 1);
            right();
        }

        public void up() {
            if (direction.getY() > -1) {
                direction = new Point2D(direction.getX(), direction.getY() - ROTATION_CHANGE);
            }
        }

        public void down() {
            if (direction.getY() < 1) {
                direction = new Point2D(direction.getX(), direction.getY() + ROTATION_CHANGE);
            }
        }

        public void left() {
            if (direction.getX() > -1) {
                direction = new Point2D(direction.getX() - ROTATION_CHANGE, direction.getY());
            }
        }

        public void right() {
            if (direction.getX() < 1) {
                direction = new Point2D(direction.getX() + ROTATION_CHANGE, direction.getY());
            }
        }
    }

    public static class CloudComponent extends Component {

        private final Point2D direction = new Point2D(FXGLMath.random(-1D, 1D), FXGLMath.random(-1D, 1D));

        @Override
        public void onUpdate(double tpf) {
            entity.translate(direction.multiply(3));
            checkForBounds();
        }

        private void checkForBounds() {
            if (entity.getX() < 0) {
                remove();
            }
            if (entity.getX() >= getAppWidth()) {
                remove();
            }
            if (entity.getY() < 0) {
                remove();
            }
            if (entity.getY() >= getAppHeight()) {
                remove();
            }
        }

        public void remove() {
            entity.removeFromWorld();
        }
    }
}