attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec2 a_texCoord1;

varying vec2 v_texCoord0;
varying vec2 v_texCoord1;

uniform mat4 u_projTrans;

void main() {
    v_texCoord0 = a_texCoord0;
    v_texCoord1 = a_texCoord1;
    gl_Position = u_projTrans * a_position;
}
