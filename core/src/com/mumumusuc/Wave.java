package com.mumumusuc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;


import static com.badlogic.gdx.Gdx.gl20;

public class Wave extends ApplicationAdapter {
    private static final String TAG = "Wave";
    private static final String VERTEX_SHADER = "shaders/waveShader.vert";
    private static final String FRAGMENT_SHADER = "shaders/waveShader.frag";
    private static final String FRAGMENT_RENDER = "shaders/waveRender.frag";
    private static final String TEST_FRAGMENT_SHADER = "shaders/testShader.frag";
    private static final String BACKGROUND = "background.png";
    private static final String MASK = "mask.png";

    private float time = 0;
    private int sWidth, sHeight, BUFF_W, BUFF_H;
    private ShaderProgram shader, renderShader, testShader;
    private Texture background, mask, testTexture;
    private TextureRenderer mesh;
    private RendererBuffers buffers;
    private float[] touchPoint = new float[3];
    private float BF_SCALE = 1f;
    private float BG_SCALE = 1f;
    private TextureRegion bgRegion;
    private TextureRegion maskRegion;
    private TextureRegion testRegion;
    private TextureRegion bufferRegion;
    private TextureRegion bufferScreenRegion;

    @Override
    public void create() {
        sWidth = Gdx.graphics.getWidth();
        sHeight = Gdx.graphics.getHeight();

        shader = new ShaderProgram(
                Gdx.files.internal(VERTEX_SHADER),
                Gdx.files.internal(FRAGMENT_SHADER)
        );
        if (!shader.isCompiled()) {
            throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
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

        BUFF_W = 1024;
        BUFF_H = 512;

        mask = new Texture(MASK);
        background = new Texture(BACKGROUND);
        int w = background.getWidth();
        int h = background.getHeight();
        bgRegion = new TextureRegion(background, (w - sWidth) / 2, (h - sHeight) / 2, sWidth, sHeight);
        bgRegion.flip(false, true);
        testTexture = new Texture("badlogic.jpg");
        background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        mask.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        mask.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        maskRegion = new TextureRegion(mask, (w - sWidth) / 2, (h - sHeight) / 2, sWidth, sHeight);
        maskRegion.flip(false, true);
        testRegion = new TextureRegion(testTexture);
        testRegion.flip(false, true);

        BF_SCALE = Math.max(sWidth / (float) BUFF_W, sHeight / (float) BUFF_H);
        BG_SCALE = Math.max(sWidth / (float) background.getWidth(), sHeight / (float) background.getHeight());

        buffers = new RendererBuffers(3, BUFF_W, BUFF_H, 0, 1, 0);
        buffers.previous().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        buffers.previous().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        buffers.current().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        buffers.current().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        buffers.next().getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        buffers.next().getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

        float r = Math.max(sWidth / (float) BUFF_W, sHeight / (float) BUFF_H);
        w = Math.round(BUFF_W * r);
        h = Math.round(BUFF_H * r);
        Gdx.app.log(TAG, "buffer_region = " + w + " , " + h);

        bufferRegion = new TextureRegion(buffers.current().getColorBufferTexture());
        Texture t = new Texture(w, h, Pixmap.Format.RGBA8888);
        bufferScreenRegion = new TextureRegion(
                t,
                (w - sWidth) / 2,
                (h - sHeight) / 2,
                sWidth, sHeight
        );
        t.dispose();


        mesh = new TextureRenderer();

        for (FrameBuffer buffer : buffers.getAll()) {
            renderBuffer(buffer, bufferScreenRegion, maskRegion, r);
        }

    }

    @Override
    public void render() {

        buffers.step();

        Texture texture0 = buffers.previous().getColorBufferTexture();
        Texture texture1 = buffers.current().getColorBufferTexture();
        Texture texture2 = buffers.next().getColorBufferTexture();

        buffers.next().begin();
        mesh.setProjection(0, 0, BUFF_W, BUFF_H);
        mesh.setShader(shader);
        mesh.begin();
        bufferRegion.setTexture(texture0);
        mesh.setTexture(0, bufferRegion);
        bufferRegion.setTexture(texture1);
        mesh.setTexture(1, bufferRegion);
        shader.setUniformf("size", BUFF_W, BUFF_H);
        shader.setUniformf("time", time);
        getTouchPoint();
        shader.setUniformf("point", touchPoint[0], touchPoint[1], touchPoint[2]);
        mesh.render(0, 0, BUFF_W, BUFF_H);
        mesh.end();
        buffers.next().end();

        bufferRegion.setTexture(texture2);
        bufferScreenRegion.setTexture(texture2);
        gl20.glClearColor(0, 0, 0, 0);
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        gl20.glViewport(0, 0, sWidth, sHeight);
        mesh.setProjection(0, 0, sWidth, sHeight);
        mesh.setShader(renderShader);
        mesh.begin();
        renderShader.setUniformf("size", BUFF_W, BUFF_H);
        mesh.setTexture(0, bufferScreenRegion);
        mesh.setTexture(1, bgRegion);
        mesh.render(0, 0, sWidth, sHeight);
        mesh.end();

        time += Gdx.graphics.getDeltaTime();
        //Gdx.app.log(TAG, Gdx.graphics.getFramesPerSecond() + "FPS");
    }

    @Override
    public void dispose() {
        shader.dispose();
        renderShader.dispose();
        testTexture.dispose();
        buffers.dispose();
        mask.dispose();
        background.dispose();
        mesh.dispose();
    }

    private TextureRegion makeTextureRegion(Texture texture) {
        TextureRegion region = new TextureRegion(texture);
        return region;
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
        return touchPoint;
    }

    private void renderBuffer(FrameBuffer buffer, TextureRegion buffer_region, TextureRegion region, float r) {
        buffer.begin();
        gl20.glClearColor(0, 0, 0, 1);
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mesh.setShader(testShader);
        mesh.setProjection(0, 0, buffer.getWidth(), buffer.getHeight());
        mesh.begin();
        mesh.setTexture(0, region);
        float x = buffer_region.getRegionX();
        float y = buffer_region.getRegionY();
        float w = buffer_region.getRegionWidth();
        float h = buffer_region.getRegionHeight();
        mesh.render(x/r, y/r, w/r, h/r);
        mesh.end();
        buffer.end();
    }

    private void renderBackground(TextureRegion region) {
        Gdx.gl.glViewport(0, 0, sWidth, sHeight);
        gl20.glClearColor(0, 0f, 0.3f, 0);
        gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        mesh.setShader(testShader);
        mesh.begin();
        mesh.setProjection(0, 0, sWidth, sHeight);
        mesh.setTexture(0, region);
        mesh.setTexture(1, bgRegion);
        mesh.render(0, 0, sWidth, sHeight);
        mesh.end();
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
