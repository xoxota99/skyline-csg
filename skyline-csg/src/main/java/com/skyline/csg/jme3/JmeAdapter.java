package com.skyline.csg.jme3;

import java.nio.*;
import java.util.*;

import javax.vecmath.*;

import com.jme3.math.*;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.*;
import com.jme3.util.*;
import com.skyline.csg.*;

/**
 * An extremely rudimentary CSG adapter for JME3. Only uses vBuffers for
 * position and normal, and dereferences transformations of input geometries
 * into the actual vertex positions (translate, rotate, scale). If you want
 * additional information tracked, you'll need to subclass the various Vertex
 * and CSG classes, in order to track this information.
 * 
 * @author philippd
 * 
 */
public class JmeAdapter {

	// NOTE: The reason we support Geometries, instead of Meshes, is because
	// Geometries provide Transform information (as well as a ton of other crap
	// we don't use).

	private JmeAdapter() {
	}

	/**
	 * Convert a JME3 Geometry to a CSG representation. NOTE: CSG
	 * representations do not track transform. While the conversion will make
	 * use of the JME3 transform, it will not be returned in calls to fromCSG
	 * later on.
	 * 
	 * @param g
	 * @return
	 */
	public static CSG toCSG(Geometry g) {
		return toCSG(g, g.getLocalTransform());
	}

	/**
	 * Create a CSG from the provided mesh, and apply the provided
	 * transformation in order (translation, rotation and scale). NOTE: CSG
	 * representations do not track transform. The transform will not be
	 * returned in calls to fromCSG later on.
	 * 
	 * @param m
	 * @param t
	 * @return
	 */
	public static CSG toCSG(Geometry g, Transform t) {
		return toCSG(g, t.getTranslation(), t.getRotation(), t.getScale());
	}

	/**
	 * Create a CSG from the provided mesh, and apply the provided
	 * transformations in order (translation, rotation and scale). NOTE: CSG
	 * representations do not track transform. The transform will not be
	 * returned in calls to fromCSG later on.
	 * 
	 * @param g
	 * @param translation
	 * @param rotation
	 * @param scale
	 * @return
	 */
	public static CSG toCSG(Geometry g, Vector3f translation, Quaternion rotation, Vector3f scale) {
		CSG retval = null;
		// TODO: We don't correctly handle textureCoords

		Mesh m = g.getMesh();

		if (m != null) {
			long t0 = System.currentTimeMillis();
			retval = toCSG(m);
			long t2 = 0, t1 = System.currentTimeMillis() - t0;
			if (retval != null) {
				if (translation != null && (translation.x != 0d || translation.y != 0d || translation.z != 0d))
					retval.translate(new Vector3d(translation.x, translation.y, translation.z));
				if (rotation != null && (rotation.getX() != 0d || rotation.getY() != 0d || rotation.getZ() != 0d))
					retval.rotate(new Quat4d(rotation.getX(), rotation.getY(), rotation.getZ(), rotation.getW()));
				if (scale != null && (scale.x != 1d || scale.y != 1d || scale.z != 1d))
					retval.scale(new Vector3d(scale.x, scale.y, scale.z));
				t2 = System.currentTimeMillis() - t0;
			}
			System.out.printf("toCSG time: %d\ntransform time:%d\n", t1, t2 - t1);
		}
		return retval;
	}

	public static CSG toCSG(Mesh m) {
		CSG retval = null;
		List<Polygon> polygons = new ArrayList<Polygon>();
		if (m.getMode() == Mode.Triangles) {
			VertexBuffer pb = m.getBuffer(Type.Position);
			VertexBuffer nb = m.getBuffer(Type.Normal);
			VertexBuffer tb = m.getBuffer(Type.TexCoord);
			IndexBuffer ib = m.getIndicesAsList();

			FloatBuffer pfb = (FloatBuffer) pb.getData();
			FloatBuffer nfb = (FloatBuffer) nb.getData();
			FloatBuffer tsb = (FloatBuffer) tb.getData();

			int vCount = ib.size();

			List<Vertex> vertices = new ArrayList<Vertex>();
			for (int i = 0; i < vCount; i++) {
				int rawIdx = ib.get(i);
				int idx = rawIdx * 3;

				Vector3d pos = new Vector3d(pfb.get(idx), pfb.get(idx + 1), pfb.get(idx + 2));
				Vector3d norm = new Vector3d(nfb.get(idx), nfb.get(idx + 1), nfb.get(idx + 2));
				TexCoord2f tex = new TexCoord2f(tsb.get(rawIdx * 2), tsb.get(rawIdx * 2 + 1));

				// System.out.printf("%d\t%f\t%f\t%f\t%f\t%f\t%f\n",rawIdx,pos.x,pos.y,pos.z,norm.x,norm.y,norm.z);
				Vertex v = new Vertex(pos, norm, tex);
				vertices.add(v);

				if ((i + 1) % 3 == 0) { // every three points
					polygons.add(new Polygon(vertices));
					vertices = new ArrayList<Vertex>();
				}
			}
			// System.out.println("========");
			// verified up to here that Vertex information is correct.
			retval = CSG.fromPolygons(polygons);
		}
		return retval;
	}

	/**
	 * Return a JME3 Mesh object, representing the CSG operation. NOTE: No Local
	 * Transform information is provided.
	 * 
	 * @param csg
	 * @return
	 * @verified: Deduping works correctly.
	 */
	public static Mesh fromCSG(CSG csg) {
		Mesh m = new Mesh();

		List<Polygon> pList = csg.getPolygons();

		// Temp vars, to hold position, normals, and indices.
		// We can't write this directly to a FloatBuffer, since we don't know
		// the size of the un-duped buffers
		List<Float> positions = new ArrayList<Float>();
		List<Float> normals = new ArrayList<Float>();
		List<Float> texCoords = new ArrayList<Float>();
		List<Short> indices = new ArrayList<Short>();

		short vCount = 0; // vertex count
		// Set of unique vertices, and their indices in the position and normal
		// lists.
		Map<String, Short> uniqueVerts = new HashMap<String, Short>();
		int ddCount = 0;
		for (Polygon p : pList) { // for each polygon ...
			// clone the vertex list, so we can remove dupes.
			List<Vertex> vList = new ArrayList<Vertex>(p.getVertices());
			assert vList.size() >= 3;

			if (vList.get(0).equals(vList.get(vList.size() - 1))) {
				// from THREE.csg.js. I guess the first vertex can appear more
				// than once?
				System.out.println("    First=last.");
				vList = vList.subList(0, vList.size() - 2); // remove the extra
															// vertex.
			}

			assert vList.size() >= 3;

			for (int j = 2; j < vList.size(); j++) {
				// A poly can have more than three vertices. Here we break it
				// into triangles if necessary.
				int[] idx = new int[] { 0, j - 1, j };

				for (int i = 0; i < 3; i++) {
					Vertex v = vList.get(idx[i]);
					short putIdx = vCount; // count of unique vertices.
					if (uniqueVerts.containsKey(v.toString())) {
						// we already have this vertex.
						putIdx = uniqueVerts.get(v.toString());
						ddCount++;
					} else {
						// new vertex.
						uniqueVerts.put(v.toString(), putIdx);
						// put position
						positions.add((float) v.getPos().x);
						positions.add((float) v.getPos().y);
						positions.add((float) v.getPos().z);

						// put normal
						normals.add((float) v.getNormal().x);
						normals.add((float) v.getNormal().y);
						normals.add((float) v.getNormal().z);

						// put texCoord
						texCoords.add((float) v.getTex().x);
						texCoords.add((float) v.getTex().y);

						vCount++;
						assert vCount == positions.size();
					}
					// System.out.printf("%d\t%f\t%f\t%f\t%f\t%f\t%f\n",putIdx,v.getPos().x,v.getPos().y,v.getPos().z,v.getNormal().x,v.getNormal().y,v.getNormal().z);
					// put index
					indices.add(putIdx);
				}
			}
		}

		// System.out.printf("deduped %d vertices. Total %d vertices.\n",
		// ddCount, uniqueVerts.size());
		FloatBuffer fpb = createFloatBuffer(positions);
		FloatBuffer fnb = createFloatBuffer(normals);
		FloatBuffer ftb = createFloatBuffer(texCoords);
		ShortBuffer sib = createShortBuffer(indices);

		m.setBuffer(Type.Position, 3, fpb);
		m.setBuffer(Type.Normal, 3, fnb);
		m.setBuffer(Type.TexCoord, 2, ftb);
		m.setBuffer(Type.Index, 3, sib);

		m.updateBound();
		return m;
	}

	private static ShortBuffer createShortBuffer(List<Short> data) {
		short[] sh = new short[data.size()];
		for (int i = 0; i < data.size(); i++) {
			sh[i] = data.get(i);
		}
		return BufferUtils.createShortBuffer(sh);
	}

	private static FloatBuffer createFloatBuffer(List<Float> data) {
		float[] f = new float[data.size()];
		for (int i = 0; i < data.size(); i++) {
			f[i] = data.get(i);
		}
		return BufferUtils.createFloatBuffer(f);
	}
}
