package com.mumumusuc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;

import static com.badlogic.gdx.Gdx.app;
import static com.badlogic.gdx.Gdx.gl20;
import static com.badlogic.gdx.Gdx.graphics;

public class Wave extends ApplicationAdapter {
    private static final String TAG = "Wave";
    private static final boolean DEBUG = false;
    private static final String VERTEX_SHADER = "shaders/waveShader.vert";
    private static final String FRAGMENT_SHADER = "shaders/waveShader.frag";
    private static final String FRAGMENT_RENDER = "shaders/waveRender.frag";
    private static final String TEST_FRAGMENT_SHADER = "shaders/testShader.frag";
    private static final String BACKGROUND = "background1.jpg";
    private static final String MASK = "mask1.png";
    private static final String TEST = "badlogic.jpg";

    private float time = 0;
    private int sWidth, sHeight, BUFF_W, BUFF_H;
    private ShaderProgram simulateShader, renderShader, testShader;
    private Texture background, mask, testTexture;
    private TextureRenderer mesh;
    private RendererBuffers buffers;
    private float[] touchPoint = new float[3];
    private RenderRoi testRegion;
    protected RenderRoi backgroundRegion;
    protected RenderRoi bufferRegion;
    protected float screenOffsetX, screenOffsetY;

    @Override
    public void create() {
        sWidth = Gdx.graphics.getWidth();
        sHeight = Gdx.graphics.getHeight();
        BUFF_W = 1024;
        BUFF_H = 512;

        simulateShader = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER),
                Gdx.files.internal(FRAGMENT_SHADER)
        );
        if (!simulateShader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + simulateShader.getLog());
        }
        renderShader = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER),
                Gdx.files.internal(FRAGMENT_RENDER)
        );
        if (!renderShader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + renderShader.getLog());
        }
        testShader = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER),
                Gdx.files.internal(TEST_FRAGMENT_SHADER)
        );
        if (!testShader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + testShader.getLog());
        }

        mesh = new TextureRenderer();
        mask = new Texture(MASK);
        background = new Texture(BACKGROUND);
        if (DEBUG) testTexture = new Texture(TEST);
        buffers = new RendererBuffers(3, BUFF_W, BUFF_H, 0, 1, 0);
        //buffers.previous().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        //buffers.previous().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        //buffers.current().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        //buffers.current().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        //buffers.next().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        //buffers.next().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        if (DEBUG)
            testRegion = makeRenderRoi(testTexture, testTexture.getWidth(), testTexture.getHeight(), true);
        bufferRegion = makeRenderRoi(buffers.current().getColorBufferTexture(), sWidth, sHeight, false);
        backgroundRegion = makeRenderRoi(background, sWidth, sHeight, true);

        bufferRegion.save();
        bufferRegion.scale(1, 1);
        int[] roi = bufferRegion.getRoi();
        bufferRegion.restore();
        for (FrameBuffer buffer : buffers.getAll()) {
            buffer.begin();
            gl20.glClearColor(0, 0, 1, 1);
            gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
            mesh.setShader(testShader);
            mesh.setProjection(0, 0, BUFF_W, BUFF_H);
            mesh.begin();
            mesh.setRenderRoi(0, backgroundRegion);
            mesh.bindTexture(0, mask);
            mesh.render(roi[0], roi[1], roi[2], roi[3]);
            mesh.end();
            buffer.end();
        }
        mask.dispose();
    }

    @Override
    public void render() {

        buffers.step();

        Texture texture0 = buffers.previous().getColorBufferTexture();
        Texture texture1 = buffers.current().getColorBufferTexture();
        Texture texture2 = buffers.next().getColorBufferTexture();

        buffers.next().begin();
        mesh.setProjection(0, 0, BUFF_W, BUFF_H);
        mesh.setShader(simulateShader);
        mesh.begin();
        mesh.setRenderRoi(0, null);
        mesh.setRenderRoi(1, null);
        mesh.bindTexture(0, texture0);
        mesh.bindTexture(1, texture1);
        simulateShader.setUniformf("size", BUFF_W, BUFF_H);
        simulateShader.setUniformf("time", time);
        getTouchPoint();
        simulateShader.setUniformf("point", touchPoint[0], touchPoint[1], touchPoint[2]);
        mesh.render(0, 0, BUFF_W, BUFF_H);
        mesh.end();
        buffers.next().end();

        //bufferRegion.save();
        //bufferRegion.scale(1, 1);
        gl20.glClearColor(1, 1, 1, 1);
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        gl20.glViewport(0, 0, sWidth, sHeight);
        mesh.setProjection(0, 0, sWidth, sHeight);
        mesh.setShader(renderShader);
        mesh.begin();
        renderShader.setUniformf("size", BUFF_W, BUFF_H);
        mesh.setRenderRoi(0, bufferRegion);
        mesh.setRenderRoi(1, backgroundRegion);
        mesh.bindTexture(0, texture2);
        mesh.bindTexture(1, background);
        mesh.render(screenOffsetX, screenOffsetY, bufferRegion.getRoiWidth(), bufferRegion.getRoiHeight());
        mesh.end();
        //bufferRegion.restore();

        time += Gdx.graphics.getDeltaTime();
        if (time > 1000.f) time = 0;
        if (DEBUG) {
            gl20.glClearColor(0, 0, 0, 1);
            app.log(TAG, "FPS:" + graphics.getFramesPerSecond());
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        //TODO: resize screen and rois
    }

    @Override
    public void dispose() {
        simulateShader.dispose();
        renderShader.dispose();
        testShader.dispose();
        if (testTexture != null) testTexture.dispose();
        mask.dispose();
        background.dispose();
        buffers.dispose();
        mesh.dispose();
    }

    private RenderRoi makeRenderRoi(Texture texture, float width, float height, boolean flipY) {
        float tw = texture.getWidth();
        float th = texture.getHeight();
        float r = Math.max(width / tw, height / th);
        int w = Math.round(tw * r);
        int h = Math.round(th * r);
        return new RenderRoi.Builder()
                .scale(r)
                .setSize(w, h)
                .flip(false, flipY)
                .setRoi((w - width) / 2, (h - height) / 2, width, height)
                .create();
    }

    private float[] getTouchPoint() {
        if (Gdx.input.isTouched()) {
            float s = bufferRegion.getScale()[0];
            int[] a = bufferRegion.getRoi();
            touchPoint[0] = (Gdx.input.getX() + a[0]) / s;
            touchPoint[1] = (Gdx.graphics.getHeight() - Gdx.input.getY() + a[1]) / s;
            touchPoint[2] = 1;
        } else {
            touchPoint[0] = 0;
            touchPoint[1] = 0;
            touchPoint[2] = 0;
        }
        return touchPoint;
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

        FrameBuffer[] getAll() {
            return buffers;
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
