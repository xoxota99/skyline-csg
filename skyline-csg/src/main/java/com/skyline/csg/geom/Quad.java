package com.skyline.csg.geom;

import java.util.*;

import javax.vecmath.*;

import com.skyline.csg.*;

/**
 * <code>Quad</code> represents a rectangular plane in space defined by 4
 * vertices. The quad's lower-left side is contained at the local space origin
 * (0, 0, 0), while the upper-right side is located at the width/height
 * coordinates (width, height, 0).
 * 
 * @author Kirill Vainer
 */
public class Quad extends CSG {

	private float width;
	private float height;

	/**
	 * Create a quad with the given width and height. The quad is always created
	 * in the XY plane.
	 * 
	 * @param width
	 *            The X extent or width
	 * @param height
	 *            The Y extent or width
	 */
	public Quad(float width, float height) {
		generatePolys(width, height, false);
	}

	/**
	 * Create a quad with the given width and height. The quad is always created
	 * in the XY plane.
	 * 
	 * @param width
	 *            The X extent or width
	 * @param height
	 *            The Y extent or height
	 * @param flipCoords
	 *            If true, the texture coordinates will be flipped along the Y
	 *            axis.
	 */
	public Quad(float width, float height, boolean flipCoords) {
		generatePolys(width, height, flipCoords);
	}

	public float getHeight() {
		return height;
	}

	public float getWidth() {
		return width;
	}

	private void generatePolys(float width, float height, boolean flipCoords) {
		this.width = width;
		this.height = height;
		float[][] pos = new float[][] { { -width/2, -height/2, 0 },
				{ width/2, -height/2, 0 },
				{ width/2, height/2, 0 },
				{ -width/2, height/2, 0 }
		};

		float[][] texs;
		if (flipCoords) {
			texs = new float[][] { { 0, 1 },
					{ 1, 1 },
					{ 1, 0 },
					{ 0, 0 } };
		} else {
			texs = new float[][] { { 0, 0 },
					{ 1, 0 },
					{ 1, 1 },
					{ 0, 1 } };
		}
		float[][] norms = new float[][] { { 0, 0, 1 },
				{ 0, 0, 1 },
				{ 0, 0, 1 },
				{ 0, 0, 1 } };

		if (height < 0) {
			polygons.add(new Polygon(
					new Vertex(
							new Vector3d(pos[0][0], pos[0][1], pos[0][2]),
							new Vector3d(norms[0][0], norms[0][1], norms[0][2]),
							new TexCoord2f(texs[0][0], texs[0][1])
					),
					new Vertex(
							new Vector3d(pos[2][0], pos[2][1], pos[2][2]),
							new Vector3d(norms[2][0], norms[2][1], norms[2][2]),
							new TexCoord2f(texs[2][0], texs[2][1])
					),
					new Vertex(
							new Vector3d(pos[1][0], pos[1][1], pos[1][2]),
							new Vector3d(norms[1][0], norms[1][1], norms[1][2]),
							new TexCoord2f(texs[1][0], texs[1][1])
					)
					));
			polygons.add(new Polygon(
					new Vertex(
							new Vector3d(pos[0][0], pos[0][1], pos[0][2]),
							new Vector3d(norms[0][0], norms[0][1], norms[0][2]),
							new TexCoord2f(texs[0][0], texs[0][1])
					),
					new Vertex(
							new Vector3d(pos[3][0], pos[3][1], pos[3][2]),
							new Vector3d(norms[3][0], norms[3][1], norms[3][2]),
							new TexCoord2f(texs[3][0], texs[3][1])
					),
					new Vertex(
							new Vector3d(pos[2][0], pos[2][1], pos[2][2]),
							new Vector3d(norms[2][0], norms[2][1], norms[2][2]),
							new TexCoord2f(texs[2][0], texs[2][1])
					)
					));
		} else {
			polygons.add(new Polygon(
					new Vertex(
							new Vector3d(pos[0][0], pos[0][1], pos[0][2]),
							new Vector3d(norms[0][0], norms[0][1], norms[0][2]),
							new TexCoord2f(texs[0][0], texs[0][1])
					),
					new Vertex(
							new Vector3d(pos[1][0], pos[1][1], pos[1][2]),
							new Vector3d(norms[1][0], norms[1][1], norms[1][2]),
							new TexCoord2f(texs[1][0], texs[1][1])
					),
					new Vertex(
							new Vector3d(pos[2][0], pos[2][1], pos[2][2]),
							new Vector3d(norms[2][0], norms[2][1], norms[2][2]),
							new TexCoord2f(texs[2][0], texs[2][1])
					)
					));
			polygons.add(new Polygon(
					new Vertex(
							new Vector3d(pos[0][0], pos[0][1], pos[0][2]),
							new Vector3d(norms[0][0], norms[0][1], norms[0][2]),
							new TexCoord2f(texs[0][0], texs[0][1])
					),
					new Vertex(
							new Vector3d(pos[2][0], pos[2][1], pos[2][2]),
							new Vector3d(norms[2][0], norms[2][1], norms[2][2]),
							new TexCoord2f(texs[2][0], texs[2][1])
					),
					new Vertex(
							new Vector3d(pos[3][0], pos[3][1], pos[3][2]),
							new Vector3d(norms[3][0], norms[3][1], norms[3][2]),
							new TexCoord2f(texs[3][0], texs[3][1])
					)
					));
		}

	}

}
