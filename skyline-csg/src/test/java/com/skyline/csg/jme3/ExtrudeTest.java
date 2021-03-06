package com.skyline.csg.jme3;

import java.util.*;

import javax.vecmath.*;

import com.jme3.app.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.math.Vector3f;
import com.jme3.post.*;
import com.jme3.post.filters.*;
import com.jme3.post.ssao.*;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.*;
import com.jme3.util.*;
import com.skyline.csg.*;
import com.skyline.csg.geom.*;

/**
 * Make a Sphere, then use a Quad to cut a piece off.
 * 
 * @author phil
 * 
 */
public class ExtrudeTest extends SimpleApplication {

	private Geometry g;

	public static void main(String[] args) {
		new ExtrudeTest().start();
	}

	@Override
	public void simpleInitApp() {
		viewPort.setBackgroundColor(new ColorRGBA(0.266f,0.266f,0.266f,1f));
		attachSimpleShape();
		DirectionalLight dl = attachSun();
		attachAmbientLight();
		viewPort.setBackgroundColor(ColorRGBA.Blue);
		// attachShadows(dl);
		// attachSSAO();
		// antialias();
		flyCam.setMoveSpeed(10f);
	}

	private void antialias() {
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		fpp.addFilter(new FXAAFilter());
		viewPort.addProcessor(fpp);
	}

	private void attachSSAO() {
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		SSAOFilter ssao = new SSAOFilter(0.92f, 2.2f, 0.46f, 0.2f);
		fpp.addFilter(ssao);

		viewPort.addProcessor(fpp);
	}

	private void attachShadows(DirectionalLight sun) {
		// DirectionalLightShadowRenderer dlsr = new
		// DirectionalLightShadowRenderer(assetManager, 1024, 4);
		// dlsr.setLight(sun);
		// dlsr.setLambda(0.55f);
		// dlsr.setShadowIntensity(0.6f);
		// dlsr.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
		// // dlsr.displayDebug();
		// viewPort.addProcessor(dlsr);

		DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 1024, 4);
		dlsf.setLight(sun);
		dlsf.setLambda(0.55f);
		dlsf.setShadowIntensity(0.6f);
		dlsf.setEdgeFilteringMode(EdgeFilteringMode.Nearest);
		dlsf.setEnabled(true);

		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		fpp.addFilter(dlsf);

		viewPort.addProcessor(fpp);

	}

	private void attachAmbientLight() {
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1f));
		rootNode.addLight(al);
	}

	private DirectionalLight attachSun() {
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(200, -200, -200).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);
		return sun;
	}

	public void attachSimpleShape() {
		// g = setupCSG();
		g = setupExtrusion();
		Geometry g2 = new Geometry("cube", JmeAdapter.fromCSG(new com.skyline.csg.geom.Box(1, new Vector3d(-2, 0, 0))));

		// Geometry gSphere = new Geometry("sphere", new Sphere(10, 10, .1f));

		 Material mat = setupLightedMaterial(false);
//		Material mat = setupNormalMaterial(false);

		g.setMaterial(mat);
		g2.setMaterial(mat);
		// g.setShadowMode(ShadowMode.CastAndReceive);
		long t0 = System.currentTimeMillis();
		TangentBinormalGenerator.generate(g);
		long t1 = System.currentTimeMillis() - t0;
		System.out.printf("Time to add binormalgenerator: %d", t1);
		// gSphere.setMaterial(mat);
		// g.setShadowMode(ShadowMode.CastAndReceive);
		// TangentBinormalGenerator.generate(g);
		// rootNode.attachChild(gSphere);
		rootNode.attachChild(g);
		rootNode.attachChild(g2);

	}

	private Geometry setupExtrusion() {
		// Here, we're deliberately flipping the starting poly (by setting the normals inverted), so we create a
		// prism, with end polys facing "outwards".
		Polygon poly = new Polygon(
				new Vertex(
						new Vector3d(0, 0, 0),
						new Vector3d(0, -1, 0),
						new TexCoord2f()),
				new Vertex(
						new Vector3d(2, 0, 0),
						new Vector3d(0, -1, 0),
						new TexCoord2f()),
				new Vertex(
						new Vector3d(2, 0, 2),
						new Vector3d(0, -1, 0),
						new TexCoord2f()),
				new Vertex(
						new Vector3d(0, 0, 2),
						new Vector3d(0, -1, 0),
						new TexCoord2f())
				);

		// List<Polygon> polys = Arrays.asList(new Polygon[]{poly});
		// CSG csg = CSG.fromPolygons(polys);
		CSG csg = poly.extrude(-10);	//negative, because the poly is flipped.
		Mesh m = JmeAdapter.fromCSG(csg);
		return new Geometry("results", m);
	}

	private Material setupNormalMaterial(boolean wireframe) {
		Material mat = new Material(assetManager, "res/Common/MatDefs/Misc/ShowNormals.j3md");
		mat.getAdditionalRenderState().setWireframe(wireframe);
		return mat;
	}

	private Material setupLightedMaterial(boolean wireframe) {

		Material mat = new Material(assetManager, "res/Common/MatDefs/Light/Lighting.j3md");
		// mat.setBoolean("UseMaterialColors", true);
		mat.setBoolean("HighQuality", true);
		mat.setColor("Specular", ColorRGBA.White);
		mat.setColor("Diffuse", new ColorRGBA(0.984313f, 0.941176f, 0.858823f, 1f));
		mat.setColor("Ambient", ColorRGBA.White); // Basically not used. I think
													// this is broken...
		mat.setFloat("Shininess", 0f); // [0,128]
		mat.getAdditionalRenderState().setWireframe(wireframe);
		return mat;
	}

	private Geometry setupSlicedSphere() {
		com.skyline.csg.geom.Sphere sphere = new com.skyline.csg.geom.Sphere();
		com.skyline.csg.geom.Quad q = new com.skyline.csg.geom.Quad(10, 10, true);
		// q.translate(new Vector3d(-1,-1,0));
		q.rotate(new Quat4d(0, 1, 1, Math.PI)); // 45 degrees around Y axis.
		q.translate(new Vector3d(1, 1, 0));

		CSG csg = sphere.intersect(q);
		Mesh m = JmeAdapter.fromCSG(csg);
		return new Geometry("results", m);
	}

	private Geometry setupScoopedSphere() {
		com.skyline.csg.geom.Sphere s1 = new com.skyline.csg.geom.Sphere();
		com.skyline.csg.geom.Sphere s2 = new com.skyline.csg.geom.Sphere();
		s2.translate(new Vector3d(.75, .75, 0));

		CSG csg = s1.subtract(s2);
		Mesh m = JmeAdapter.fromCSG(csg);
		return new Geometry("results", m);
	}

	/**
	 * 
	 * @return
	 */
	private Geometry setupCSG() {
		long t0 = System.currentTimeMillis();
		Mesh cube = new Box(1f, 1f, 1f);
		Mesh sphere = new Sphere(50, 50, 1.4f);
		Mesh cyl = new Cylinder(4, 50, .95f, 3);
		// Mesh tor = new Torus(50, 50, 1.025f, 0.05f);

		Geometry gCube = new Geometry("cube", cube);
		Geometry gSphere = new Geometry("sphere", sphere);
		Geometry gCyl = new Geometry("cyl", cyl);
		Geometry gCyl2 = new Geometry("cyl2", cyl);
		gCyl2.rotate((float) (Math.PI / 2), 0, 0);
		Geometry gCyl3 = new Geometry("cyl3", cyl);
		gCyl3.rotate(0, (float) (Math.PI / 2), 0);

		// Geometry gTor = new Geometry("tor1",tor);
		// gTor.setLocalTranslation(0, 0, 1);

		long t1 = System.currentTimeMillis() - t0;
		CSG csgCube = JmeAdapter.toCSG(gCube)
				.intersect(JmeAdapter.toCSG(gSphere))
				.subtract(JmeAdapter.toCSG(gCyl))
				.subtract(JmeAdapter.toCSG(gCyl2))
				.subtract(JmeAdapter.toCSG(gCyl3));
		// .union(JmeAdapter.toCSG(gTor));
		// 7 seconds
		long t2 = System.currentTimeMillis() - t0;
		Mesh m = JmeAdapter.fromCSG(csgCube);
		// 12 seconds
		long t3 = System.currentTimeMillis() - t0;
		Geometry results = new Geometry("results", m);
		// 12 seconds
		long t4 = System.currentTimeMillis() - t0;

		System.out.printf("t1: %d\nt2: %d\nt3: %d\nt4: %d\n", t1, t2, t3, t4);
		return results;
	}

	private Geometry setupCSGWithTranslation() {
		Mesh cube = new Box(1f, 1f, 1f);
		Mesh sphere = new Sphere(50, 50, 1.4f);
		Mesh sphere2 = new Sphere(50, 50, 1.375f);

		Geometry gCube = new Geometry("cube", cube);
		Geometry gSphere = new Geometry("sphere", sphere);
		Geometry gSphere2 = new Geometry("sphere2", sphere2);

		gCube.setLocalTranslation(.5f, .5f, .5f);
		gSphere.setLocalTranslation(.5f, -.5f, .5f);
		gSphere2.setLocalTranslation(.5f, 1f, .5f);

		CSG csgCube = JmeAdapter.toCSG(gCube)
				.intersect(JmeAdapter.toCSG(gSphere))
				.subtract(JmeAdapter.toCSG(gSphere2));
		Mesh m = JmeAdapter.fromCSG(csgCube);

		return new Geometry("results", m);
	}

	private Geometry setupCSGWithScale() {
		Mesh cube = new Box(1f, 1f, 1f);
		Mesh sphere = new Sphere(50, 50, 1.4f);
		Mesh sphere2 = new Sphere(50, 50, 1.375f);

		Geometry gCube = new Geometry("cube", cube);
		Geometry gSphere = new Geometry("sphere", sphere);
		Geometry gSphere2 = new Geometry("sphere2", sphere2);

		gCube.setLocalScale(2f);
		gSphere.setLocalScale(2f);
		gSphere2.setLocalScale(2f);

		CSG csgCube = JmeAdapter.toCSG(gCube)
				.intersect(JmeAdapter.toCSG(gSphere))
				.subtract(JmeAdapter.toCSG(gSphere2));
		Mesh m = JmeAdapter.fromCSG(csgCube);

		return new Geometry("results", m);
	}
}