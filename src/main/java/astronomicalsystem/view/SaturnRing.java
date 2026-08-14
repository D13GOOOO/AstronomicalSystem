package astronomicalsystem.view;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.InputStream;
import java.util.Objects;

/**
 * Generates a custom 3D mesh representation of a planetary ring system (e.g., Saturn).
 * <p>
 * This class procedurally calculates the spatial vertices, texture coordinates (UV mapping),
 * and triangular face indices required to render a flat, textured geometric disc in 3D space.
 * Inherits from {@link MeshView} to integrate directly into the JavaFX scene graph.
 * </p>
 */
public class SaturnRing extends MeshView {

    /**
     * Constructs a textured planetary ring mesh.
     * <p>
     * Attempts to apply a high-fidelity image texture. If the specified resource
     * is unavailable, it gracefully degrades by applying a semi-transparent
     * flat color fallback to ensure visual continuity and prevent crashes.
     * </p>
     *
     * @param innerRadius the spatial distance from the origin to the interior edge of the ring.
     * @param outerRadius the spatial distance from the origin to the exterior edge of the ring.
     * @param divisions   the number of radial segments defining the mesh curvature resolution.
     * @param texturePath the classpath-relative string path to the texture image resource.
     * @throws NullPointerException     if the texture path is null.
     * @throws IllegalArgumentException if radii are invalid or divisions are strictly less than 3.
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
        
        // Ensure both the top and bottom of the flat ring are rendered
        this.setCullFace(CullFace.NONE); 
        this.setMesh(createRingMesh(innerRadius, outerRadius, divisions));
    }

    /**
     * Validates the geometric parameters required for ring generation.
     *
     * @param innerRadius the inner boundary radius.
     * @param outerRadius the outer boundary radius.
     * @param divisions   the radial resolution of the mesh.
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
     * Algorithmically generates the topology for a 2D annulus (ring) in 3D space.
     * <p>
     * <b>UV Mapping Note:</b> The texture coordinates assume a 1D horizontal strip texture, 
     * where the left edge (U=0.0) corresponds to the inner radius, and the right edge (U=1.0) 
     * corresponds to the outer radius.
     * </p>
     *
     * @param innerRadius the inner boundary radius.
     * @param outerRadius the outer boundary radius.
     * @param divisions   the radial resolution of the mesh.
     * @return a fully constructed and UV-mapped {@link TriangleMesh}.
     */
    private TriangleMesh createRingMesh(double innerRadius, double outerRadius, int divisions) {
        TriangleMesh mesh = new TriangleMesh();

        // U,V mapping: inner edge = 0.0 (left side of texture), outer edge = 1.0 (right side)
        float[] texCoords = {
                0f, 0.5f, // Index 0: mapped to the inner vertex
                1f, 0.5f  // Index 1: mapped to the outer vertex
        };
        mesh.getTexCoords().addAll(texCoords);

        float[] points = new float[divisions * 2 * 3]; // 2 vertices per division, 3 coordinates (X,Y,Z) per vertex
        int[] faces = new int[divisions * 2 * 6];      // 2 triangles per division, 6 integers (VertexIndex, UVIndex) per triangle

        double angleStep = (Math.PI * 2.0) / divisions;

        int pIndex = 0;
        int fIndex = 0;

        for (int i = 0; i < divisions; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Generate inner vertex (Point index: i * 2)
            points[pIndex++] = (float) (innerRadius * cos);
            points[pIndex++] = 0; // Ring is flat on the X-Z plane
            points[pIndex++] = (float) (innerRadius * sin);

            // Generate outer vertex (Point index: i * 2 + 1)
            points[pIndex++] = (float) (outerRadius * cos);
            points[pIndex++] = 0; 
            points[pIndex++] = (float) (outerRadius * sin);

            int currentInner = i * 2;
            int currentOuter = i * 2 + 1;
            
            // Optimization: using modulo to wrap around to the first vertices and close the ring loop
            int nextInner = ((i + 1) % divisions) * 2;
            int nextOuter = ((i + 1) % divisions) * 2 + 1;

            // Define first triangle of the quad (Current Inner -> Current Outer -> Next Inner)
            faces[fIndex++] = currentInner; faces[fIndex++] = 0; // Point index, UV index (0)
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1; // Point index, UV index (1)
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;

            // Define second triangle of the quad (Current Outer -> Next Outer -> Next Inner)
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1;
            faces[fIndex++] = nextOuter;    faces[fIndex++] = 1;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;
        }

        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);

        return mesh;
    }
}