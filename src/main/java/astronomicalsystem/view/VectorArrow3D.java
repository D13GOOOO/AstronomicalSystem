package astronomicalsystem.view;

import astronomicalsystem.model.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * A highly specialized graphical component that visually represents a mathematical physics vector 
 * (such as velocity or gravitational force) in the three dimensional simulation space.
 * 
 * Because the native JavaFX 3D library lacks a built-in "Arrow" or "Cone" primitive, this class 
 * mathematically constructs one from scratch. It uses a standard {@link Cylinder} for the shaft and 
 * generates a custom {@link TriangleMesh} to form the pointed arrowhead. It utilizes advanced linear 
 * algebra—specifically vector cross-products and arc-cosines—to continuously calculate the exact 
 * three dimensional rotation required to make the arrow point dynamically toward any arbitrary 
 * X, Y, Z coordinate in real-time.
 */
public class VectorArrow3D extends Group {

    /**
     * The primary cylindrical body of the vector arrow, visually representing the magnitude 
     * of the physical force through its dynamic length.
     */
    private final Cylinder shaft;
    
    /**
     * The custom generated mathematical cone forming the tip of the arrow, indicating the 
     * precise spatial direction of the represented force.
     */
    private final MeshView arrowhead;
    
    /**
     * The geometric transformation responsible for anchoring the entire arrow structure 
     * to the exact physical coordinates of the targeted celestial body.
     */
    private final Translate positionTransform;
    
    /**
     * The geometric transformation responsible for applying the calculated mathematical 
     * rotations, allowing the arrow to pivot freely in three dimensional space.
     */
    private final Rotate directionRotate;
    
    /**
     * A structural alignment transformation used internally to offset the visual components, 
     * ensuring the base of the arrow rests neatly on the surface of the planet rather than 
     * clipping into its core.
     */
    private final Translate alignmentTransform;

    /**
     * Constructs a new three dimensional vector arrow, completely initializing its custom mesh 
     * geometry, applying the specified color material, and preparing its spatial transforms for 
     * future dynamic updates. The arrow is rendered as a sleek, semi-transparent holographic laser.
     *
     * @param color the solid color identifier to paint the entire arrow structure, visually 
     *              differentiating force types (e.g., Green for Velocity, Red for Gravity).
     */
    public VectorArrow3D(Color color) {
        Color holoColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.6);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(holoColor);
        material.setSpecularColor(Color.WHITE);

        this.shaft = new Cylinder(0.4, 10);
        this.shaft.setMaterial(material);

        this.arrowhead = createCone(1.5, 6.0);
        this.arrowhead.setMaterial(material);

        Group arrowGroup = new Group(this.shaft, this.arrowhead);

        this.positionTransform = new Translate();
        this.directionRotate = new Rotate();
        this.alignmentTransform = new Translate(); 
        
        arrowGroup.getTransforms().add(this.alignmentTransform);

        this.getChildren().add(arrowGroup);
        this.getTransforms().addAll(this.positionTransform, this.directionRotate);
        
        this.setVisible(false);
    }

    /**
     * Dynamically modifies the arrow's length, spatial position, and three dimensional rotation 
     * based on incoming raw physics data from the simulation engine.
     * 
     * This method evaluates the magnitude of the provided physics vector. If the magnitude is 
     * negligible (approaching zero), the arrow hides itself to prevent visual clutter. Otherwise, 
     * it calculates the required visual scaling, positions the base of the arrow at the surface 
     * of the celestial body, and utilizes a vector cross-product against the standard vertical 
     * Y-axis to determine the exact angular rotation needed to point the arrow toward the 
     * destination coordinate.
     *
     * @param origin       the exact spatial coordinates representing the center of the targeted celestial body.
     * @param vector       the mathematical vector containing the exact direction and magnitude of the physical force.
     * @param visualScale  a visual scaling multiplier used to ensure the generated arrow remains visible 
     *                     and proportionate relative to the massive distances of the simulated universe.
     * @param planetRadius the visual graphical radius of the targeted celestial body, utilized to perfectly 
     *                     offset the arrow's starting position to the body's outer surface.
     */
    public void update(Point3D origin, Point3D vector, double visualScale, double planetRadius) {
        
        double magnitude = vector.magnitude();
        
        if (magnitude < 1e-9) {
            this.setVisible(false);
            return;
        }

        double length = (planetRadius * 1.2) + (magnitude * visualScale);
        this.shaft.setHeight(length);
        
        this.arrowhead.setTranslateY(-length / 2.0);
        
        this.alignmentTransform.setY(-(length / 2.0) - planetRadius - 1.0);

        this.positionTransform.setX(origin.x());
        this.positionTransform.setY(origin.y());
        this.positionTransform.setZ(origin.z());

        Point3D up = new Point3D(0, -1, 0);
        Point3D dir = vector.normalize();
        
        double dot = up.x() * dir.x() + up.y() * dir.y() + up.z() * dir.z();
        double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));
        
        double crossX = up.y() * dir.z() - up.z() * dir.y();
        double crossY = up.z() * dir.x() - up.x() * dir.z();
        double crossZ = up.x() * dir.y() - up.y() * dir.x();

        if (Math.abs(crossX) < 0.0001 && Math.abs(crossY) < 0.0001 && Math.abs(crossZ) < 0.0001) {
            crossX = 1;
        }

        this.directionRotate.setAngle(angle);
        this.directionRotate.setAxis(new javafx.geometry.Point3D(crossX, crossY, crossZ));
    }

    /**
     * Programmatically generates a customized three dimensional cone shape using an array of 
     * calculated vertices and connecting triangle faces.
     * 
     * By manually defining the geometry, this method bypasses the limitations of the standard 
     * JavaFX 3D shape library, constructing a perfect arrowhead that seamlessly scales and 
     * attaches to the cylindrical shaft.
     *
     * @param radius the desired maximum width of the cone's circular base.
     * @param height the desired total vertical length from the base to the pointed tip.
     * @return a fully constructed and renderable TriangleMesh representing the arrowhead.
     */
    private MeshView createCone(double radius, double height) {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0, 0);

        int divisions = 36;
        float[] points = new float[(divisions + 2) * 3];
        int[] faces = new int[divisions * 2 * 6];

        points[0] = 0; points[1] = (float) -height; points[2] = 0;
        
        points[3] = 0; points[4] = 0; points[5] = 0;

        for (int i = 0; i < divisions; i++) {
            double angle = (2 * Math.PI * i) / divisions;
            points[(i + 2) * 3] = (float) (radius * Math.cos(angle));
            points[(i + 2) * 3 + 1] = 0;
            points[(i + 2) * 3 + 2] = (float) (radius * Math.sin(angle));
        }

        mesh.getPoints().addAll(points);

        int fIndex = 0;
        for (int i = 0; i < divisions; i++) {
            int current = i + 2;
            int next = (i + 1) % divisions + 2;

            faces[fIndex++] = 0;       faces[fIndex++] = 0;
            faces[fIndex++] = next;    faces[fIndex++] = 0;
            faces[fIndex++] = current; faces[fIndex++] = 0;

            faces[fIndex++] = 1;       faces[fIndex++] = 0;
            faces[fIndex++] = current; faces[fIndex++] = 0;
            faces[fIndex++] = next;    faces[fIndex++] = 0;
        }

        mesh.getFaces().addAll(faces);
        
        MeshView cone = new MeshView(mesh);
        cone.setCullFace(CullFace.NONE);
        return cone;
    }
}