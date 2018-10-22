package com.mumumusuc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static com.badlogic.gdx.Gdx.gl20;

public class Wave extends ApplicationAdapter {
    private static final String TAG = "Wave";
    private static final String VERTEX_SHADER = "shaders/waveShader.vert";
    private static final String FRAGMENT_SHADER = "shaders/waveShader.frag";
    private static final String FRAGMENT_RENDER = "shaders/waveRender.frag";
    private static final String TEST_FRAGMENT_SHADER = "shaders/testShader.frag";
    private static final String BACKGROUND = "background.jpg";

    private float time = 0;
    private int sWidth, sHeight, BUFF_W, BUFF_H;
    private ShaderProgram shader, renderer;
    private Texture background;
    private RendererMesh mesh;
    private RendererBuffers buffers;
    private Matrix4 projection = new Matrix4();
    private float[] touchPoint = new float[3];
    private float SCALE = 2f;

    @Override
    public void create() {
        //Gdx.graphics.setContinuousRendering(false);
        sWidth = Gdx.graphics.getWidth();
        sHeight = Gdx.graphics.getHeight();

        shader = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER),
                Gdx.files.internal(FRAGMENT_SHADER)
        );
        if (!shader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
        }

        renderer = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER),
                Gdx.files.internal(FRAGMENT_RENDER)
        );
        if (!renderer.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + renderer.getLog());
        }

        background = new Texture(BACKGROUND);
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        BUFF_W = 1024;
        BUFF_H = 512;
        SCALE = Math.max(sWidth / (float) BUFF_W, sHeight / (float) BUFF_H);
        buffers = new RendererBuffers(3, BUFF_W, BUFF_H, 0, 0, 0);

        buffers.previous().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        buffers.previous().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        buffers.current().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        buffers.current().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        buffers.next().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        buffers.next().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        mesh = new RendererMesh(BUFF_W, BUFF_H);
        IntBuffer buffer = ByteBuffer.allocateDirect(Integer.SIZE / 8 * 16).asIntBuffer();
        buffer.position(0);
        Gdx.gl20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buffer);
        Gdx.app.log(TAG, "MAX_TEXTURE_SIZE = " + buffer.get(0));
    }

    @Override
    public void render() {

        buffers.step();

        Texture texture0 = buffers.previous().getColorBufferTexture();
        int tex_handle_0 = texture0.getTextureObjectHandle();
        Texture texture1 = buffers.current().getColorBufferTexture();
        int tex_handle_1 = texture1.getTextureObjectHandle();
        Texture texture2 = buffers.next().getColorBufferTexture();
        int tex_handle_2 = texture2.getTextureObjectHandle();

        buffers.next().begin();
        projection.setToOrtho2D(0, 0, BUFF_W, BUFF_H);
        gl20.glClearColor(0f, 0f, 0f, 1.0f);
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        texture0.bind(tex_handle_0);
        texture1.bind(tex_handle_1);
        shader.begin();
        shader.setUniformMatrix("u_projTrans", projection);
        shader.setUniformi("buffer_0", tex_handle_0);
        shader.setUniformi("buffer_1", tex_handle_1);
        //shader.setUniformi("option", 2);
        shader.setUniformf("res", BUFF_W, BUFF_H);
        shader.setUniformf("time", time);
        getTouchPoint();
        shader.setUniformf("point", touchPoint[0], touchPoint[1], touchPoint[2]);
        mesh.render(shader, 1, 1, true);
        shader.end();
        buffers.next().end();

        projection.setToOrtho2D(0, 0, sWidth, sHeight);
        Gdx.gl.glViewport(0, 0, sWidth, sHeight);
        gl20.glClearColor(0, 0, 0, 0);
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        texture2.bind(tex_handle_2);
        background.bind(background.getTextureObjectHandle());
        renderer.begin();
        renderer.setUniformMatrix("u_projTrans", projection);
        renderer.setUniformi("buffer_0", tex_handle_2);
        renderer.setUniformi("buffer_1", background.getTextureObjectHandle());
        renderer.setUniformf("res", BUFF_W * SCALE, BUFF_H * SCALE);
        renderer.setUniformf("res1", background.getWidth() * 1.1f, background.getHeight() * 1.1f);
        mesh.render(renderer, SCALE, SCALE, true);
        renderer.end();

        time += Gdx.graphics.getDeltaTime();
    }

    @Override
    public void dispose() {
        shader.dispose();
        renderer.dispose();
        buffers.dispose();
        background.dispose();
        mesh.dispose();
    }

    private float[] getTouchPoint() {
        if (Gdx.input.isTouched()) {
            touchPoint[0] = Gdx.input.getX();
            touchPoint[1] = Gdx.graphics.getHeight() - Gdx.input.getY();
            touchPoint[2] = 1;
        } else {
            touchPoint[0] = 0;
            touchPoint[1] = 0;
            touchPoint[2] = 0;
        }
        touchPoint[0] /= SCALE;
        touchPoint[1] /= SCALE;
        return touchPoint;
    }

    class RendererMesh implements Disposable {
        private final int VERTICES_SIZE = 16;
        private final int INDICES_SIZE = 4;
        private float[] vertices;
        private short[] indices;
        private float[] temp;
        private Mesh mesh;

        RendererMesh(float w, float h) {
            mesh = new Mesh(true, VERTICES_SIZE, INDICES_SIZE,
                    new VertexAttribute(Usage.Position, 2, "a_position"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
            vertices = new float[]{
                    0, 0, 0, 1,
                    0, h, 0, 0,
                    w, h, 1, 0,
                    w, 0, 1, 1
            };
            temp = vertices.clone();
            indices = new short[]{0, 1, 3, 2};
            mesh.setIndices(indices);
        }

        public void render(ShaderProgram shader, float scaleX, float scaleY, boolean flipY) {
            if (flipY) {
                temp[3] = 1 - vertices[3];
                temp[7] = 1 - vertices[7];
                temp[11] = 1 - vertices[11];
                temp[15] = 1 - vertices[15];
            }
            temp[5] = vertices[5] * scaleY;
            temp[8] = vertices[8] * scaleX;
            temp[9] = vertices[9] * scaleY;
            temp[12] = vertices[12] * scaleX;
            mesh.setVertices(temp);
            mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
        }

        public void render(ShaderProgram shader) {
            render(shader, 1, 1, false);
        }

        @Override
        public void dispose() {
            mesh.dispose();
        }
    }

    class RendererBuffers implements Disposable {
        private int count = 2;
        private int index = 0;
        private GLFrameBuffer.FrameBufferBuilder builder;
        private FrameBuffer[] buffers;

        RendererBuffers(final int num, int w, int h, float r, float g, float b) {
            count = num;
            buffers = new FrameBuffer[count];
            builder = new GLFrameBuffer.FrameBufferBuilder(w, h);
            builder.addColorTextureAttachment(GL30.GL_RGB16F, GL30.GL_RGB, GL30.GL_FLOAT);
            for (int i = 0; i < count; i++) {
                buffers[i] = builder.build();
                buffers[i].begin();
                gl20.glClearColor(r, g, b, 1);
                gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
                buffers[i].end();
            }
        }

        FrameBuffer get(int i) {
            return buffers[i];
        }

        FrameBuffer next() {
            return get((index + 1) % count);
        }

        FrameBuffer current() {
            return get(index);
        }

        FrameBuffer previous() {
            return get((index + count - 1) % count);
        }

        int step() {
            index = (index + 1) % count;
            return index;
        }

        @Override
        public void dispose() {
            for (FrameBuffer buffer : buffers) {
                buffer.dispose();
            }
        }
    }
}
