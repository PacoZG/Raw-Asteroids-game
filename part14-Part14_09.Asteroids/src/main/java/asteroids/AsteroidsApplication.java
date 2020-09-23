package asteroids;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AsteroidsApplication extends Application {

    public static int WIDTH = 600;
    public static int HEIGHT = 400;
    int highestScore;

    @Override
    public void start(Stage stage){

        //Adding sound effects
        AudioClip fire = new AudioClip("file:fire.wav");
        fire.setPriority(3);
        AudioClip thrust = new AudioClip("file:thrust.wav");
        thrust.setPriority(3);

        AudioClip bigBang = new AudioClip("file:bangLarge.wav");
        AudioClip bang = new AudioClip("file:bangMedium.wav");
        AudioClip extraShip = new AudioClip("file:extraShip.wav");

        AudioClip aster = new AudioClip("file:Asteroids.mp3");
        aster.setCycleCount(AudioClip.INDEFINITE);


        Pane pane = new Pane();

        pane.setPrefSize(WIDTH, HEIGHT);
        Text text = new Text(10, 20, "Points: 0");
        pane.getChildren().add(text);

        Button playAgain = new Button("Play");
        playAgain.setAlignment(Pos.TOP_CENTER);
        Button exit = new Button ("Exit");
        exit.setAlignment(Pos.CENTER);
        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);

        buttons.getChildren().addAll(playAgain, exit);

        Label highScore = new Label("0");
        highScore.setAlignment(Pos.CENTER);
        Label lastScore = new Label("0");
        lastScore.setAlignment(Pos.CENTER);

        VBox layout = new VBox();
        layout.setPrefSize(300, 100);
        layout.setSpacing(20);
        layout.setPadding(new Insets(20, 20, 20, 20));
        layout.getChildren().add(highScore);
        layout.getChildren().add(lastScore);
        layout.getChildren().add(buttons);


        Scene scene1 = new Scene(pane); // game scene
        Scene scene2 = new Scene(layout); // score and buttons scene


        Ship ship = new Ship(WIDTH / 2, HEIGHT / 2);
        List<Projectile> projectiles = new ArrayList<>();
        List<Asteroid> asteroids = new ArrayList<>();
        AtomicInteger points = new AtomicInteger();
        AtomicInteger blasts = new AtomicInteger();
        int maxBlasts = 5;

        for (int i = 0; i < 2; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            asteroids.add(asteroid);
        }

        pane.getChildren().add(ship.getCharacter());
        asteroids.forEach(asteroid -> pane.getChildren().add(asteroid.getCharacter()));

        Map<KeyCode, Boolean> pressedKeys = new HashMap<>();
        scene1.setOnKeyPressed(event -> {
            pressedKeys.put(event.getCode(), Boolean.TRUE);
        });

        scene1.setOnKeyReleased(event -> {
            pressedKeys.put(event.getCode(), Boolean.FALSE);
        });


        new AnimationTimer() {

            int lastPoints = 0;

            @Override
            public void handle(long now) {

                if (!aster.isPlaying()){
                    aster.play(0.8);
                }

                if(pressedKeys.getOrDefault(KeyCode.LEFT, false)) {
                    ship.turnLeft();
                }

                if(pressedKeys.getOrDefault(KeyCode.RIGHT, false)) {
                    ship.turnRight();
                }

                if(pressedKeys.getOrDefault(KeyCode.UP, false)) {
                    //thrust.play(0.1);
                    ship.accelerate();
                }


                if (pressedKeys.getOrDefault(KeyCode.SPACE, false) && projectiles.size() <= 1) {
                    scene1.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        if (event.getCode() == KeyCode.SPACE) {
                            // we shoot
                            Projectile projectile = new Projectile((int) ship.getCharacter().getTranslateX(), (int) ship.getCharacter().getTranslateY());
                            projectile.getCharacter().setRotate(ship.getCharacter().getRotate());
                            if (blasts.get() <= maxBlasts){
                                fire.play(1.0);
                                projectiles.add(projectile);
                                projectile.accelerate();
                                projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                                pane.getChildren().add(projectile.getCharacter());
                            }
                        }
                    });
                }

                ship.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                if(Math.random() < 0.010) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    if(!asteroid.collide(ship)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());

                    }
                }

                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if(projectile.collide(asteroid)) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                            bang.play(1.0);
                        }
                    });

                    if(!projectile.isAlive()) {
                        lastPoints = points.addAndGet(100);
                        text.setText("Points: " + lastPoints);
                        lastScore.setText("Score: " + String.valueOf(lastPoints));
                        if (lastPoints > highestScore){
                            highestScore = lastPoints;
                        }
                        highScore.setText("High score: " + String.valueOf(highestScore));
                    }
                });

                projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
                projectiles.removeAll(projectiles.stream()
                        .filter(projectile -> !projectile.isAlive())
                        .collect(Collectors.toList()));

                asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
                asteroids.removeAll(asteroids.stream()
                        .filter(asteroid -> !asteroid.isAlive())
                        .collect(Collectors.toList()));

                asteroids.forEach(asteroid -> {
                    if (ship.collide(asteroid)) {
                        bigBang.play(1.0);
                        stop();
                        playAgain.setText("Play again");
                        stage.setScene(scene2);
                        aster.stop();

                    }
                });
            }

        }.start();

        playAgain.setOnMouseClicked(even -> {
            points.set(0);
            stage.setScene(scene1);
            start(stage);
            extraShip.play(1.0);
        });

        exit.setOnMouseClicked(event ->{
            stage.close();
        });


        stage.setTitle("Asteroids!");
        stage.setScene(scene1);
        aster.play();
        stage.show();

    }
}

class App {
    public static void main (String[] ars){
        Application.launch(AsteroidsApplication.class);
    }
}