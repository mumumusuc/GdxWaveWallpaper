uniform mat4 u_projTrans;

attribute vec4 a_position;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

varying vec4 vColor;
varying vec2 vTexCoord;

void main() {
    gl_Position = u_projTrans * a_position;
    vColor = a_color;
    vTexCoord= a_texCoord0;
}
