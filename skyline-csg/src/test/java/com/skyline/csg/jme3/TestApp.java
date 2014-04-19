package com.skyline.csg.jme3;

import com.jme3.app.*;
import com.jme3.light.*;
import com.jme3.material.*;
import com.jme3.math.*;
import com.jme3.post.*;
import com.jme3.post.filters.*;
import com.jme3.post.ssao.*;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.*;
import com.jme3.scene.shape.*;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.*;
import com.skyline.csg.*;
import com.skyline.csg.geom.*;

public class TestApp extends SimpleApplication {

	private Geometry g;

	public static void main(String[] args) {
		new TestApp().start();
	}

	@Override
	public void simpleInitApp() {
		viewPort.setBackgroundColor(new ColorRGBA(0.266f, 0.266f, 0.4f, 1f));
		attachSimpleShape();
		DirectionalLight dl = attachSun();
		attachAmbientLight();
		// attachShadows(dl);
		// attachSSAO();
		antialias();
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
		al.setColor(ColorRGBA.White.mult(0.1f));
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
		g = setupCSG();

		// Geometry gSphere = new Geometry("sphere", new Sphere(10, 10, .1f));

		Material mat = setupLightedMaterial(false);
		// Material mat = setupNormalMaterial(false);

		g.setMaterial(mat);
		g.setShadowMode(ShadowMode.CastAndReceive);
		long t0 = System.currentTimeMillis();
		// TangentBinormalGenerator.generate(g);
		long t1 = System.currentTimeMillis() - t0;
		System.out.printf("Time to add binormalgenerator: %d", t1);
		// gSphere.setMaterial(mat);
		// gSphere.setShadowMode(ShadowMode.CastAndReceive);
		// TangentBinormalGenerator.generate(gSphere);
		// rootNode.attachChild(gSphere);
		rootNode.attachChild(g);

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
		mat.setBoolean("UseMaterialColors",true);
		mat.setColor("Specular", ColorRGBA.White);	//setting this has no effect.
		mat.setColor("Diffuse", new ColorRGBA(0.266f, 0.266f, 0.266f, 1f));	//setting this has no effect.
		mat.setColor("Ambient", ColorRGBA.White); //setting this has no effect.
		
		mat.setFloat("Shininess", 10f); // [0,128]
		mat.getAdditionalRenderState().setWireframe(wireframe);
		return mat;
	}

	private Geometry setupCSG() {
		CSG cube = new Box(1);
		CSG sphere = new com.skyline.csg.geom.Sphere(1.4,4);
		CSG cyl = new com.skyline.csg.geom.Cylinder(.95,3,50);
		CSG cyl2 = new com.skyline.csg.geom.Cylinder(.95,3,50);
		cyl2.rotate((float) (Math.PI / 2),0,0);
		CSG cyl3 = new com.skyline.csg.geom.Cylinder(.95,3,50);
		cyl3.rotate(0,0, (float) (Math.PI / 2));

		CSG csg = cube.intersect(sphere)
				.subtract(cyl)
				.subtract(cyl2)
				.subtract(cyl3);
		
		Mesh m = JmeAdapter.fromCSG(csg);

		Geometry results = new Geometry("results", m);

		return results;
	}
	/**
	 * 
	 * @return
	 */
	private Geometry setupCSGFromJME() {
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

	private Geometry setupBuilding() {
		Mesh dome = new Dome(new Vector3f(0, 0, 0), 50, 50, 10f, false);
		Mesh cutter = new Box(1f, 50f, 1f);
		Mesh cube = new Box(1f, 1f, 1f);

		Geometry gCube = new Geometry("cube", cube);
		Geometry gDome = new Geometry("dome", dome);

		CSG csgDome = JmeAdapter.toCSG(gDome).subtract((JmeAdapter.toCSG(cutter).inverse()));

		return new Geometry("results", JmeAdapter.fromCSG(JmeAdapter.toCSG(gCube).union(csgDome)));
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
