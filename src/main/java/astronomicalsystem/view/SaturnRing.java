package astronomicalsystem.view;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.InputStream;

/**
 * Generates a custom 3D mesh representation of a planetary ring system.
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
     * flat color fallback to ensure visual continuity.
     * </p>
     *
     * @param innerRadius the spatial distance from the origin to the interior edge of the ring.
     * @param outerRadius the spatial distance from the origin to the exterior edge of the ring.
     * @param divisions   the number of radial segments defining the mesh curvature resolution.
     * @param texturePath the classpath-relative string path to the texture image resource.
     */
    public SaturnRing(double innerRadius, double outerRadius, int divisions, String texturePath) {
        PhongMaterial material = new PhongMaterial();

        try {
            InputStream stream = getClass().getResourceAsStream(texturePath);
            if (stream != null) {
                Image texture = new Image(stream);
                material.setDiffuseMap(texture);
                material.setSelfIlluminationMap(texture); // Eliminates shading anomalies on the flat mesh
            } else {
                throw new IllegalArgumentException("Texture resource stream returned null.");
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
     * Algorithmically generates the topology for a 2D annulus (ring) in 3D space.
     *
     * @param innerRadius the inner boundary radius.
     * @param outerRadius the outer boundary radius.
     * @param divisions   the radial resolution of the mesh.
     * @return a fully constructed and UV-mapped {@link TriangleMesh}.
     */
    private TriangleMesh createRingMesh(double innerRadius, double outerRadius, int divisions) {
        TriangleMesh mesh = new TriangleMesh();

        // 1D UV Mapping: Maps the inner radius to the left edge of the texture (0f)
        // and the outer radius to the right edge (1f).
        float[] texCoords = {
                0f, 0.5f,
                1f, 0.5f
        };
        mesh.getTexCoords().addAll(texCoords);

        float[] points = new float[divisions * 2 * 3];
        int[] faces = new int[divisions * 2 * 6];

        double angleStep = (Math.PI * 2) / divisions;

        int pIndex = 0;
        int fIndex = 0;

        for (int i = 0; i < divisions; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Inner vertex calculation
            points[pIndex++] = (float) (innerRadius * cos);
            points[pIndex++] = 0;
            points[pIndex++] = (float) (innerRadius * sin);

            // Outer vertex calculation
            points[pIndex++] = (float) (outerRadius * cos);
            points[pIndex++] = 0;
            points[pIndex++] = (float) (outerRadius * sin);

            int currentInner = i * 2;
            int currentOuter = i * 2 + 1;
            int nextInner = (i + 1 == divisions) ? 0 : (i + 1) * 2;
            int nextOuter = (i + 1 == divisions) ? 1 : (i + 1) * 2 + 1;

            // First triangle of the quad segment
            faces[fIndex++] = currentInner; faces[fIndex++] = 0;
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;

            // Second triangle of the quad segment
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1;
            faces[fIndex++] = nextOuter;    faces[fIndex++] = 1;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;
        }

        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);

        return mesh;
    }
}