package com.mumumusuc;

import com.badlogic.gdx.graphics.Texture;

public class RenderRoi {
    private int[] tmp = new int[4];
    private float[] uv = new float[4];
    private float[] roi = new float[4];
    private float[] scale = new float[2];
    private float[] save = new float[2];

    private RenderRoi() {
    }

    public int[] getRoi() {
        tmp[0] = Math.round(roi[0] * scale[0]);
        tmp[1] = Math.round(roi[1] * scale[1]);
        tmp[2] = Math.round(roi[2] * scale[0]);
        tmp[3] = Math.round(roi[3] * scale[1]);
        return tmp;
    }

    public int getRoiWidth() {
        return Math.round(roi[2] * scale[0]);
    }

    public int getRoiHeight() {
        return Math.round(roi[3] * scale[1]);
    }

    private void setRoi(float x, float y, float w, float h) {
        roi[0] = x;
        roi[1] = y;
        roi[2] = w;
        roi[3] = h;
    }

    public float[] getUV() {
        return uv;
    }

    private void setUV(float u0, float v0, float u1, float v1) {
        uv[0] = u0;
        uv[1] = v0;
        uv[2] = u1;
        uv[3] = v1;
    }

    public float[] getScale() {
        return scale;
    }

    public void scale(float scaleX, float scaleY) {
        scale[0] = scaleX;
        scale[1] = scaleY;
    }

    public void save() {
        save[0] = scale[0];
        save[1] = scale[1];
    }

    public void restore() {
        scale[0] = save[0];
        scale[1] = save[1];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\r\n(x, y, w, h) = (");
        tmp = getRoi();
        sb.append(tmp[0]).append(", ").append(tmp[1]).append(", ")
                .append(tmp[2]).append(", ").append(tmp[3]).append(")\r\n");
        sb.append("(u0, v0, u1, v1) = (");
        sb.append(uv[0]).append(", ").append(uv[1]).append(", ")
                .append(uv[2]).append(", ").append(uv[3]).append(")\r\n");
        return sb.toString();
    }

    static class Builder {
        private float w, h, scale = 1f;
        private float x, y, rw, rh;
        private boolean flipX = false;
        private boolean flipY = false;
        private Texture texture;

        public Builder setTexture(Texture texture) {
            this.texture = texture;
            return this;
        }

        public Builder setSize(float w, float h) {
            this.w = w;
            this.h = h;
            return this;
        }

        public Builder setRoi(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.rw = w;
            this.rh = h;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public Builder flip(boolean x, boolean y) {
            flipX = x;
            flipY = y;
            return this;
        }

        public RenderRoi create() {
            RenderRoi roi = new RenderRoi();
            if ((w == 0 || h == 0) && texture != null) {
                w = texture.getWidth();
                h = texture.getHeight();
                texture = null;
            }
            if (rw == 0 || rh == 0) {
                rw = w;
                rh = h;
            }
            float u0 = x / w;
            float v0 = y / h;
            float u1 = (x + rw) / w;
            float v1 = (y + rh) / h;
            roi.setUV(
                    flipX ? u1 : u0,
                    flipY ? v1 : v0,
                    flipX ? u0 : u1,
                    flipY ? v0 : v1
            );
            roi.setRoi(x / scale, y / scale, rw / scale, rh / scale);
            roi.scale(scale, scale);
            return roi;
        }
    }
}
