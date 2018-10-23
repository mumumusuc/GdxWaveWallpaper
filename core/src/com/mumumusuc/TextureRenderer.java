package com.mumumusuc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.Gdx.graphics;

public class TextureRenderer implements Disposable {
    private float[] vertices;
    private short[] indices;
    private Mesh mesh;
    private ShaderProgram shader;
    private Matrix4 projection = new Matrix4();

    TextureRenderer() {
        mesh = new Mesh(true, 8 * 3, 4,
                new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord1")
        );
        vertices = new float[]{
                0, 0, 0, 0, 0, 0,
                0, 1, 0, 1, 0, 1,
                1, 1, 1, 1, 1, 1,
                1, 0, 1, 0, 1, 0
        };
        indices = new short[]{0, 1, 3, 2};
        mesh.setIndices(indices);
        projection.setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight());
    }

    public void setProjection(Matrix4 projection) {
        projection.set(projection);
    }

    public void setProjection(int x, int y, int w, int h) {
        projection.setToOrtho2D(x, y, w, h);
    }

    public void setShader(ShaderProgram shader) {
        this.shader = shader;
    }

    public void begin() {
        shader.begin();
    }

    public void end() {
        shader.end();
    }

    public void setTexture(int index, TextureRegion region) {
        vertices[2 + 2 * index] = region.getU();
        vertices[3 + 2 * index] = region.getV();
        vertices[8 + 2 * index] = region.getU();
        vertices[9 + 2 * index] = region.getV2();
        vertices[14 + 2 * index] = region.getU2();
        vertices[15 + 2 * index] = region.getV2();
        vertices[20 + 2 * index] = region.getU2();
        vertices[21 + 2 * index] = region.getV();
        Texture texture = region.getTexture();
        int handle = texture.getTextureObjectHandle();
        texture.bind(handle);
        shader.setUniformi("texture_" + index, handle);
    }

    public void render(float x, float y, float w, float h) {
        vertices[0] = x;
        vertices[1] = y;
        vertices[6] = x;
        vertices[7] = y + h;
        vertices[12] = x + w;
        vertices[13] = y + h;
        vertices[18] = x + w;
        vertices[19] = y;
        mesh.setVertices(vertices);
        shader.setUniformMatrix("u_projTrans", projection);
        //gl20.glEnable(GL20.GL_BLEND);
        //gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
    }

    public void render(TextureRegion region, float x, float y) {
        setTexture(0, region);
        render(x, y, region.getRegionWidth(), region.getRegionHeight());
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }
/*
    class TextureRegion{

    }
    */
}
