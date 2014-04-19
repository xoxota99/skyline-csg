package com.skyline.csg.jme3;

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
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.*;
import com.jme3.system.*;
import com.skyline.csg.*;
import com.skyline.csg.geom.*;
import com.skyline.csg.geom.Box;

public class IcoTest extends SimpleApplication {

	private Geometry g;

	public static void main(String[] args) {
		AppSettings settings = new AppSettings(true);

		IcoTest app = new IcoTest();
		app.setSettings(settings);
		app.setShowSettings(false);
		app.start();
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
		// g = setupCSG();
		g = setupBuilding();
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
		mat.setBoolean("UseMaterialColors", true);
		mat.setBoolean("HighQuality", true);
		mat.setColor("Specular", ColorRGBA.White);
		mat.setColor("Diffuse", ColorRGBA.White);// new ColorRGBA(0.266f,
													// 0.266f, 0.266f, 1f));
													// //setting this has no
													// effect.
		mat.setColor("Ambient", ColorRGBA.White); // Basically not used. I think
													// this is broken...
		mat.setFloat("Shininess", 0f); // [0,128]
		mat.getAdditionalRenderState().setWireframe(wireframe);
		return mat;
	}

	/**
	 * 
	 * @return
	 */
	private Geometry setupCSG() {
		// CSG csg = new Icosahedron(1);
		CSG csg = new com.skyline.csg.geom.Sphere();
		// CSG csg = new Box();
		// Cone cone = new Cone(1,3,20);

		Mesh m = JmeAdapter.fromCSG(csg);

		Geometry results = new Geometry("results", m);

		return results;
	}

	private Geometry setupBuilding_old() {
		float r2 = 0.706f * 2;

		// example of transitivity (daisy-chaining).
		CSG csg = new com.skyline.csg.geom.Sphere(1)	//Create a sphere
				.subtract(new Box(2)
					.translate(0, -.5, 0, "floorCutter")
				, "dome")								// Cut the bottom 3/4 or so off, to make a solid dome.
				.intersect(new Box(r2, 20d, r2), "cutDome")		// Cut the sides off.
				.subtract(new com.skyline.csg.geom.Sphere(1)	
					.translate(0, -0.01, 0, "hollow")			// Hollow out the inside of the dome.
				, "hollowCutDome");

		Mesh m = JmeAdapter.fromCSG(csg);

		Geometry results = new Geometry("results", m);

		return results;
	}
	
	private Geometry setupBuilding() {
		float r2 = 0.706f * 2;

		// example of transitivity (daisy-chaining).
		CSG csg = new com.skyline.csg.geom.Sphere(1)	//Create a sphere
				.intersect(new Box(r2, 2, r2)
					.translate(0,1.5,0), "cutDome")		// Cut the bottom and sides off.
				.subtract(new com.skyline.csg.geom.Sphere(1)	
					.translate(0, -0.01, 0, "hollow")			// Hollow out the inside of the dome.
				, "hollowCutDome");

		Mesh m = JmeAdapter.fromCSG(csg);

		Geometry results = new Geometry("results", m);

		return results;
	}
}
