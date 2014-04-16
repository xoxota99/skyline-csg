package com.skyline.csg.jme3;

import static org.junit.Assert.assertEquals;

import java.nio.*;

import javax.vecmath.*;

import org.junit.*;

import com.jme3.scene.*;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.mesh.*;
import com.jme3.scene.shape.*;
import com.skyline.csg.*;

public class jmeAdapterTest {

	@Test
	public void meshConversion() {
		Box box = new Box(.5f, .5f, .5f); // unit cube.
		Geometry cube = new Geometry("box", box);

		cube.setLocalScale(1f);
//		printVertices(box);

		CSG csg = JmeAdapter.toCSG(cube); // convert to CSG

		Mesh m = JmeAdapter.fromCSG(csg); // convert back.

//		System.out.println("\n\n");
//		printVertices(m);

		FloatBuffer boxPos = (FloatBuffer) cube.getMesh().getBuffer(Type.Position).getData();
		FloatBuffer boxNormal = (FloatBuffer) cube.getMesh().getBuffer(Type.Normal).getData();
		IndexBuffer boxIdx = cube.getMesh().getIndicesAsList();

		FloatBuffer csgPos = (FloatBuffer) m.getBuffer(Type.Position).getData();
		FloatBuffer csgNormal = (FloatBuffer) m.getBuffer(Type.Normal).getData();
		IndexBuffer csgIdx = m.getIndicesAsList();

		assertEquals("Input and Output Meshes have a different number of vertices.", boxIdx.size(), csgIdx.size());

		for (int i = 0; i < boxIdx.size(); i++) {
			double boxPosX = boxPos.get(boxIdx.get(i) * 3);
			double boxPosY = boxPos.get(boxIdx.get(i) * 3 + 1);
			double boxPosZ = boxPos.get(boxIdx.get(i) * 3 + 2);
			double boxNormX = boxNormal.get(boxIdx.get(i) * 3);
			double boxNormY = boxNormal.get(boxIdx.get(i) * 3 + 1);
			double boxNormZ = boxNormal.get(boxIdx.get(i) * 3 + 2);
			double csgPosX = csgPos.get(csgIdx.get(i) * 3);
			double csgPosY = csgPos.get(csgIdx.get(i) * 3 + 1);
			double csgPosZ = csgPos.get(csgIdx.get(i) * 3 + 2);
			double csgNormX = csgNormal.get(csgIdx.get(i) * 3);
			double csgNormY = csgNormal.get(csgIdx.get(i) * 3 + 1);
			double csgNormZ = csgNormal.get(csgIdx.get(i) * 3 + 2);
			String posMsg = String.format("Vertex #%d position is different between meshes: \nBox[%d]: {%f, %f, %f}\nCsg[%d]: {%f, %f, %f}",
					i, boxIdx.get(i), boxPosX, boxPosY, boxPosZ, csgIdx.get(i), csgPosX, csgPosY, csgPosZ);
			String normMsg = String.format("Vertex #%d normal is different between meshes: \nBox[%d]: {%f, %f, %f}\nCsg[%d]: {%f, %f, %f}",
					i, boxIdx.get(i), boxNormX, boxNormY, boxNormZ, csgIdx.get(i), csgNormX, csgNormY, csgNormZ);
			assertEquals(posMsg, boxPosX, csgPosX, 1e-6);
			assertEquals(posMsg, boxPosY, csgPosY, 1e-6);
			assertEquals(posMsg, boxPosZ, csgPosZ, 1e-6);
			assertEquals(normMsg, boxNormX, csgNormX, 1e-6);
			assertEquals(normMsg, boxNormY, csgNormY, 1e-6);
			assertEquals(normMsg, boxNormZ, csgNormZ, 1e-6);
		}

	}

	private void printVertices(Mesh m) {

		VertexBuffer pb = m.getBuffer(Type.Position);
		VertexBuffer nb = m.getBuffer(Type.Normal);
		IndexBuffer ib = m.getIndicesAsList();

		FloatBuffer pfb = (FloatBuffer) pb.getData();
		FloatBuffer nfb = (FloatBuffer) nb.getData();

		int vCount = ib.size();

		for (int i = 0; i < vCount; i++) {
			int idx = ib.get(i) * 3;
			Point3d p = new Point3d(pfb.get(idx), pfb.get(idx + 1), pfb.get(idx + 2));
			Point3d n = new Point3d(nfb.get(idx), nfb.get(idx + 1), nfb.get(idx + 2));

			System.out.println("p" + ib.get(i) + ": {" + p.x + ", " + p.y + ", " + p.z + "}, n" + ib.get(i) + ": {" + n.x + ", " + n.y + ", " + n.z + "}");
		}

	}
}
