package astronomicalsystem.view;

import java.io.InputStream;
import java.util.Objects;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * A specialized graphical component responsible for generating and displaying a realistic, 
 * three dimensional planetary ring system, similar to the iconic rings of Saturn. 
 * 
 * Instead of relying on pre built standard shapes, this class mathematically calculates its 
 * own custom geometry from scratch. It builds a flat, circular disc by stitching together 
 * hundreds of tiny triangles to form a complete ring. Because it inherits directly from the 
 * standard JavaFX mesh view class, it can be seamlessly inserted into the three dimensional 
 * space of the simulation alongside regular spheres and planets, acting just like any other 
 * native graphical object.
 */
public class SaturnRing extends MeshView {

    /**
     * Constructs a fully textured planetary ring by mathematically generating the required 
     * three dimensional geometry and painting it with the provided image file.
     * 
     * During construction, this method attempts to load a high quality image resource to stretch 
     * across the flat surface of the ring. To prevent the entire application from abruptly crashing 
     * if the image file goes missing, is renamed, or becomes corrupted, it includes a robust safety 
     * net. If the image cannot be loaded, it catches the resulting error, logs a warning, and instead 
     * paints the ring with a reliable, semi transparent golden color. This ensures the simulation 
     * always continues to run smoothly regardless of missing visual assets.
     *
     * @param innerRadius the exact distance from the center point of the host planet to the empty 
     *                    inside edge where the ring begins.
     * @param outerRadius the exact distance from the center point of the host planet to the solid 
     *                    outside edge where the ring ends in open space.
     * @param divisions   the total number of straight line segments used to draw the circular curve. 
     *                    A higher number creates a perfectly smooth, realistic circle but requires 
     *                    more graphical processing power, while a lower number creates a blocky, 
     *                    angular polygon.
     * @param texturePath the exact location and name of the image file stored within the application 
     *                    resources, which will be wrapped around the ring geometry.
     * @throws NullPointerException     if the provided texture path string is completely missing or uninitialized.
     * @throws IllegalArgumentException if the sizes provided for the ring create physical impossibilities, 
     *                                  such as the inside edge being larger than the outside edge, or if 
     *                                  the number of divisions is less than three, which is the absolute 
     *                                  minimum required to draw any closed flat shape.
     */
    public SaturnRing(double innerRadius, double outerRadius, int divisions, String texturePath) {
        validateParameters(innerRadius, outerRadius, divisions);
        Objects.requireNonNull(texturePath, "Texture path cannot be null");

        PhongMaterial material = new PhongMaterial();

        try (InputStream stream = getClass().getResourceAsStream(texturePath)) {
            if (stream != null) {
                Image texture = new Image(stream);
                material.setDiffuseMap(texture);
                material.setSelfIlluminationMap(texture);
            } else {
                throw new IllegalArgumentException("Texture resource stream returned null for path: " + texturePath);
            }
        } catch (Exception e) {
            System.err.println("Warning: Ring texture resolution failed (" + e.getMessage() + "). Applying fallback color.");
            material.setDiffuseColor(Color.rgb(210, 200, 160, 0.6));
        }

        this.setMaterial(material);
        
        this.setCullFace(CullFace.NONE); 
        this.setMesh(createRingMesh(innerRadius, outerRadius, divisions));
    }

    /**
     * Acts as a strict mathematical gatekeeper that verifies the physical dimensions requested 
     * for the ring before the computer wastes processing power trying to draw an impossible shape. 
     * 
     * It ensures that the ring has a logical physical size where the outer boundary is strictly 
     * further away than the inner boundary. It also verifies that the geometry has at least three 
     * sides, as it is mathematically impossible to draw a two dimensional enclosed shape with only 
     * one or two sides.
     *
     * @param innerRadius the proposed starting distance of the inner edge of the ring.
     * @param outerRadius the proposed ending distance of the outer edge of the ring.
     * @param divisions   the proposed number of straight edges that will make up the circular shape.
     * @throws IllegalArgumentException if the divisions are too low to form a valid polygon, or if 
     *                                  the radii are negative, zero, or overlapping in an impossible way.
     */
    static void validateParameters(double innerRadius, double outerRadius, int divisions) {
        if (divisions < 3) {
            throw new IllegalArgumentException("Divisions must be at least 3 to form a valid polygon");
        }
        if (innerRadius <= 0 || outerRadius <= innerRadius) {
            throw new IllegalArgumentException("OuterRadius must be strictly greater than innerRadius, and both must be positive");
        }
    }

    /**
     * Operates as the mathematical engine that actually builds the physical structure of the ring. 
     * 
     * It works by calculating the exact three dimensional coordinates for every single point along 
     * the inner and outer edges using trigonometry to form a perfect circle. Once the points are placed, 
     * it teaches the graphics engine how to connect these dots to form solid triangles, which are the 
     * basic building blocks of all 3D graphics. 
     * 
     * Finally, it provides painting instructions to the graphics card, ensuring that the far left side 
     * of the loaded texture image is pinned to the inside edge of the ring, and the far right side of 
     * the image is pinned to the outside edge. This stretches the image perfectly across the flat surface 
     * like a painted canvas.
     *
     * @param innerRadius the exact distance from the center to the inner empty edge of the ring.
     * @param outerRadius the exact distance from the center to the solid outer edge of the ring.
     * @param divisions   the number of radial segments used to define how smooth the circular curve will be.
     * @return a completely built custom three dimensional mesh containing all the points, connected triangles, 
     *         and painting instructions needed to physically draw the ring on the screen.
     */
    private TriangleMesh createRingMesh(double innerRadius, double outerRadius, int divisions) {
        TriangleMesh mesh = new TriangleMesh();

        float[] texCoords = {
                0f, 0.5f,
                1f, 0.5f 
        };
        mesh.getTexCoords().addAll(texCoords);

        float[] points = new float[divisions * 2 * 3]; 
        int[] faces = new int[divisions * 2 * 6];      

        double angleStep = (Math.PI * 2.0) / divisions;

        int pIndex = 0;
        int fIndex = 0;

        for (int i = 0; i < divisions; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            points[pIndex++] = (float) (innerRadius * cos);
            points[pIndex++] = 0;
            points[pIndex++] = (float) (innerRadius * sin);

            points[pIndex++] = (float) (outerRadius * cos);
            points[pIndex++] = 0; 
            points[pIndex++] = (float) (outerRadius * sin);

            int currentInner = i * 2;
            int currentOuter = i * 2 + 1;
            
            int nextInner = ((i + 1) % divisions) * 2;
            int nextOuter = ((i + 1) % divisions) * 2 + 1;

            faces[fIndex++] = currentInner; faces[fIndex++] = 0;
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;

            faces[fIndex++] = currentOuter; faces[fIndex++] = 1;
            faces[fIndex++] = nextOuter;    faces[fIndex++] = 1;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;
        }

        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);

        return mesh;
    }
}