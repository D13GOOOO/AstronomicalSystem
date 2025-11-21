import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.image.Image;

import java.util.Objects;

public class SaturnRing extends MeshView {

    public SaturnRing(double innerRadius, double outerRadius, int divisions, String texturePath) {
        // 1. Setup Material (Texture)
        PhongMaterial material = new PhongMaterial();
        try {
            Image texture = new Image(Objects.requireNonNull(getClass().getResourceAsStream(texturePath)));
            material.setDiffuseMap(texture);
            // SelfIllumination makes the ring visible even in shadow (optional but nice)
            material.setSelfIlluminationMap(texture);
        } catch (Exception e) {
            System.err.println("Ring texture error: " + e.getMessage());
        }
        this.setMaterial(material);

        // Important: Draw both sides of the ring (top and bottom)
        this.setCullFace(CullFace.NONE);

        // 2. Generate Geometry (Mesh)
        setMesh(createRingMesh(innerRadius, outerRadius, divisions));
    }

    private TriangleMesh createRingMesh(double innerRadius, double outerRadius, int divisions) {
        TriangleMesh mesh = new TriangleMesh();

        // Texture Coordinates (UV)
        // Mapping the image:
        // t0 = Inner Edge (Left of image), t1 = Outer Edge (Right of image)
        float[] texCoords = {
                0f, 0.5f, // t0 (Inner)
                1f, 0.5f  // t1 (Outer)
        };
        mesh.getTexCoords().addAll(texCoords);

        // Points and Faces
        float[] points = new float[divisions * 2 * 3]; // 2 rings of vertices (inner/outer), 3 coords (x,y,z) per vertex
        int[] faces = new int[divisions * 2 * 6]; // 2 triangles per segment, 3 vertices + 3 texCoords per triangle

        double angleStep = Math.PI * 2 / divisions;

        int pIndex = 0;
        int fIndex = 0;

        for (int i = 0; i < divisions; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            // Inner Vertex (p0)
            points[pIndex++] = (float) (innerRadius * cos);
            points[pIndex++] = 0; // Flat Y
            points[pIndex++] = (float) (innerRadius * sin);

            // Outer Vertex (p1)
            points[pIndex++] = (float) (outerRadius * cos);
            points[pIndex++] = 0; // Flat Y
            points[pIndex++] = (float) (outerRadius * sin);

            // Create Faces (Triangles)
            // Indices for current and next vertices
            int currentInner = i * 2;
            int currentOuter = i * 2 + 1;
            int nextInner = (i + 1 == divisions) ? 0 : (i + 1) * 2;
            int nextOuter = (i + 1 == divisions) ? 1 : (i + 1) * 2 + 1;

            // Triangle 1 (Inner -> Outer -> Next Inner)
            faces[fIndex++] = currentInner; faces[fIndex++] = 0; // t0
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1; // t1
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0; // t0

            // Triangle 2 (Outer -> Next Outer -> Next Inner)
            faces[fIndex++] = currentOuter; faces[fIndex++] = 1; // t1
            faces[fIndex++] = nextOuter;    faces[fIndex++] = 1; // t1
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0; // t0
        }

        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);

        return mesh;
    }
}