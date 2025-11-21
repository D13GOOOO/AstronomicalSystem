import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.CullFace;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SolarSystem3D extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private static final double POS_SCALE = 0.4;

    private AstronomicalSystem system;
    private Group root3D;
    private SmartGroup world;
    private final Map<CelestialBody, Sphere> visualObjects = new HashMap<>();

    private InfoPanel infoPanel;
    private boolean isPaused = false;

    private CelestialBody saturnBody;
    private SaturnRing saturnRingVisual;

    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    @Override
    public void start(Stage primaryStage) {
        initPhysics();

        world = new SmartGroup();
        world.getTransforms().addAll(rotateX, rotateY);
        root3D = new Group();

        createSkybox();

        saturnRingVisual = new SaturnRing(42, 80, 100, "/saturn_ring.png");
        world.getChildren().add(saturnRingVisual);

        root3D.getChildren().add(world);

        SubScene subScene = new SubScene(root3D, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-3000);
        camera.setNearClip(1);
        camera.setFarClip(50000.0);
        subScene.setCamera(camera);

        infoPanel = new InfoPanel();
        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().addAll(subScene, infoPanel);
        StackPane.setAlignment(infoPanel, Pos.TOP_LEFT);
        infoPanel.setMouseTransparent(true);

        Scene mainScene = new Scene(rootLayout, WIDTH, HEIGHT);

        new InputHandler(this, mainScene, camera, rotateX, rotateY);

        PointLight sunLight = new PointLight(Color.WHITE);
        world.getChildren().add(sunLight);
        AmbientLight ambient = new AmbientLight(Color.rgb(40, 40, 40));
        root3D.getChildren().add(ambient);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused) {
                    updateFrame();
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Interactive Solar System (Full Kepler + Inclination)");
        primaryStage.setScene(mainScene);
        primaryStage.show();

        rootLayout.requestFocus();
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    public void showInfoPanel(CelestialBody body) {
        infoPanel.showPlanet(body);
        this.isPaused = true;
    }

    public void hideInfoPanel() {
        infoPanel.hide();
        this.isPaused = false;
    }

    private void initPhysics() {
        system = new AstronomicalSystem();

        double M_EARTH_SIM = 50.0;
        double M_SUN_SIM   = M_EARTH_SIM * 200.0;
        double GM = M_SUN_SIM * Interactions.G;

        // --- SUN ---
        Star sun = new Star("Sun", M_SUN_SIM, 20.0, 0, 0, 0);
        sun.setRealData("1.989 × 10^30 kg", "696,340 km", "5,500°C (Surface)", "25 Days", "N/A");
        system.addBody(sun);

        // --- PLANETS ---

        Planet mercury = addPlanet("Mercury", M_EARTH_SIM * 0.055, 350, GM);
        mercury.setRealData("3.285 × 10^23 kg", "2,439 km", "-173°C / 427°C", "58.6 Days", "88 Days");

        Planet venus = addPlanet("Venus", M_EARTH_SIM * 0.815, 500, GM);
        venus.setRealData("4.867 × 10^24 kg", "6,051 km", "462°C (Avg)", "243 Days", "225 Days");

        Planet earth = addPlanet("Earth", M_EARTH_SIM, 700, GM);
        earth.setRealData("5.972 × 10^24 kg", "6,371 km", "15°C (Avg)", "24 Hours", "365.25 Days");

        Planet mars = addPlanet("Mars", M_EARTH_SIM * 0.107, 950, GM);
        mars.setRealData("6.39 × 10^23 kg", "3,389 km", "-65°C (Avg)", "24h 37m", "687 Days");

        Planet jupiter = addPlanet("Jupiter", M_EARTH_SIM * 318, 1600, GM);
        jupiter.setRealData("1.898 × 10^27 kg", "69,911 km", "-110°C", "9h 56m", "11.86 Years");

        saturnBody = addPlanet("Saturn", M_EARTH_SIM * 95, 2200, GM);
        saturnBody.setRealData("5.683 × 10^26 kg", "58,232 km", "-140°C", "10h 34m", "29.45 Years");

        Planet uranus = addPlanet("Uranus", M_EARTH_SIM * 14.5, 2800, GM);
        uranus.setRealData("8.681 × 10^25 kg", "25,362 km", "-195°C", "17h 14m", "84 Years");

        Planet neptune = addPlanet("Neptune", M_EARTH_SIM * 17, 3300, GM);
        neptune.setRealData("1.024 × 10^26 kg", "24,622 km", "-200°C", "16h 6m", "164.8 Years");
    }

    // METHOD TO INITIALIZE PLANETS
    private Planet addPlanet(String name, double mass, double semiMajorAxis, double GM) {
        double e = getEccentricity(name);
        double i_deg = getInclination(name);
        double i_rad = Math.toRadians(i_deg);

        // Perihelion
        double r_p = semiMajorAxis * (1.0 - e);

        // Velocity at Perihelion
        double v_total = Math.sqrt( (GM / semiMajorAxis) * ( (1.0 + e) / (1.0 - e) ) );

        double vy = v_total * Math.sin(i_rad);
        double vz = v_total * Math.cos(i_rad);

        Planet p = new Planet(name, mass, 1.0, r_p, 0, 0);

        p.setVelocity(new Point3D(0, -vy, vz));

        system.addBody(p);
        return p;
    }

    private void updateFrame() {
        system.simulate(1);
        List<CelestialBody> currentBodies = system.getBodies();

        visualObjects.keySet().removeIf(b -> !currentBodies.contains(b));
        world.getChildren().removeIf(node -> {
            if (node instanceof Sphere) return !visualObjects.containsValue(node);
            if (node instanceof Ellipse) return false;
            if (node instanceof SaturnRing) return false;
            if (node instanceof PointLight) return false;
            return false;
        });

        for (CelestialBody body : currentBodies) {
            if (!visualObjects.containsKey(body)) createSphereFor(body);

            Sphere s = visualObjects.get(body);
            s.setTranslateX(body.getPosition().x * POS_SCALE);
            s.setTranslateY(body.getPosition().y * POS_SCALE);
            s.setTranslateZ(body.getPosition().z * POS_SCALE);

            if (body instanceof Star) {
                s.setRotate(s.getRotate() + 0.05);
            } else {
                s.setRotate(s.getRotate() + 0.5);
            }

            s.setRadius(getVisualRadius(body));
        }

        if (saturnBody != null && saturnRingVisual != null) {
            saturnRingVisual.setTranslateX(saturnBody.getPosition().x * POS_SCALE);
            saturnRingVisual.setTranslateY(saturnBody.getPosition().y * POS_SCALE);
            saturnRingVisual.setTranslateZ(saturnBody.getPosition().z * POS_SCALE);

            // Default inclination for Saturn Rings
            saturnRingVisual.setRotationAxis(Rotate.X_AXIS);
            saturnRingVisual.setRotate(25);
        }
    }

    private double getVisualRadius(CelestialBody body) {
        String name = body.getName().toLowerCase();
        if (body instanceof Star) return 80;
        if (name.contains("jupiter")) return 35;
        if (name.contains("saturn"))  return 30;
        if (name.contains("uranus"))  return 20;
        if (name.contains("neptune")) return 20;
        if (name.contains("earth"))   return 12;
        if (name.contains("venus"))   return 11;
        if (name.contains("mars"))    return 8;
        if (name.contains("mercury")) return 6;
        return 5;
    }

    private void createSphereFor(CelestialBody body) {
        if (body instanceof Planet) {
            double e = getEccentricity(body.getName());
            double i_deg = getInclination(body.getName()); // Gradi

            double currentDistScaled = body.getPosition().distance(new Point3D(0,0,0)) * POS_SCALE;
            double a = currentDistScaled / (1.0 - e);
            double b = a * Math.sqrt(1 - (e * e));
            double c = a * e;

            Ellipse orbit = new Ellipse(a, b);
            orbit.setStroke(Color.rgb(135, 206, 250, 0.25));
            orbit.setStrokeWidth(1);
            orbit.setFill(null);

            // Move focus
            orbit.setTranslateX(-c);

            // Basic rotation and inclination
            orbit.setRotationAxis(Rotate.X_AXIS);
            orbit.setRotate(90 + i_deg);

            world.getChildren().addFirst(orbit);
        }

        // PLANET
        Sphere sphere = new Sphere();
        sphere.setUserData(body);
        PhongMaterial material = new PhongMaterial();
        sphere.setRotationAxis(Rotate.Y_AXIS);

        String name = body.getName().toLowerCase();
        String texturePath = "/" + name + ".jpg";
        Color fallbackColor = Color.GRAY;

        fallbackColor = switch (name) {
            case "sun" -> Color.ORANGE;
            case "mercury" -> Color.DARKGRAY;
            case "venus" -> Color.LIGHTYELLOW;
            case "earth" -> Color.DODGERBLUE;
            case "mars" -> Color.RED;
            case "jupiter" -> Color.BEIGE;
            case "saturn" -> Color.GOLD;
            case "uranus" -> Color.LIGHTBLUE;
            case "neptune" -> Color.DARKBLUE;
            default -> fallbackColor;
        };

        try {
            if (getClass().getResource(texturePath) != null) {
                Image texture = new Image(Objects.requireNonNull(getClass().getResourceAsStream(texturePath)));
                material.setDiffuseMap(texture);
                if (name.equals("sun")) material.setSelfIlluminationMap(texture);
            } else {
                throw new Exception("No texture");
            }
        } catch (Exception e) {
            material.setDiffuseColor(fallbackColor);
        }

        sphere.setMaterial(material);
        sphere.setRadius(getVisualRadius(body));

        visualObjects.put(body, sphere);
        world.getChildren().add(sphere);
    }

    private double getEccentricity(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "mercury" -> 0.205;
            case "venus" -> 0.007;
            case "earth" -> 0.017;
            case "mars" -> 0.094;
            case "jupiter" -> 0.049;
            case "saturn" -> 0.057;
            case "uranus" -> 0.046;
            case "neptune" -> 0.011;
            default -> 0.0;
        };
    }

    // REAL INFOS
    private double getInclination(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "mercury" -> 7.0;
            case "venus" -> 3.39;
            case "earth" -> 0.0; // Riferimento
            case "mars" -> 1.85;
            case "jupiter" -> 1.3;
            case "saturn" -> 2.48;
            case "uranus" -> 0.77;
            case "neptune" -> 1.77;
            default -> 0.0;
        };
    }

    private void createSkybox() {
        Sphere skybox = new Sphere(25000);
        skybox.setId("skybox");
        skybox.setCullFace(CullFace.NONE);
        PhongMaterial mat = new PhongMaterial();
        try {
            String path = getClass().getResource("/milky_way.jpg") != null ? "/milky_way.jpg" : "/stars.jpg";
            if (getClass().getResource(path) != null) {
                Image stars = new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
                mat.setDiffuseMap(stars);
                mat.setSelfIlluminationMap(stars);
            } else {
                mat.setDiffuseColor(Color.web("#050510"));
            }
        } catch (Exception e) {
            mat.setDiffuseColor(Color.BLACK);
        }
        skybox.setMaterial(mat);
        root3D.getChildren().add(skybox);
    }

    static class SmartGroup extends Group { }

    public static void main(String[] args) {
        launch(args);
    }
}