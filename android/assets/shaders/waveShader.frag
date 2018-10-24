varying vec2 v_texCoord0;
varying vec2 v_texCoord1;

uniform sampler2D texture_0;
uniform sampler2D texture_1;
uniform vec2 size;
uniform vec3 point;
uniform float time;

float clampToEdge(vec2);

void main(){
    vec2 q = v_texCoord0;
    vec3 e = vec3(vec2(1.) / size.xy, 0.);

    float edge = clampToEdge(q);
    vec2 p = texture2D(texture_0, q).xy;
    float p10 = texture2D(texture_1, q-e.zy).x * edge;  // down
    float p01 = texture2D(texture_1, q-e.xz).x * edge;  // left
    float p21 = texture2D(texture_1, q+e.xz).x * edge;  // right
    float p12 = texture2D(texture_1, q+e.zy).x * edge;  // up

    float d = 0.;
    if(point.z > 0.){                   // when touched
        float len = distance(point.xy, gl_FragCoord.xy);
        d = smoothstep(1.5,.5, len) * -1.;
    }else{                              // simulate rain drop
        float t = time * 2.;
        vec2 pos = fract(floor(t) * vec2(0.456665, 0.708618)) * size;
        float amp = 1. - step(.05, fract(t));
        d = -amp * smoothstep(2.5, .5, length(pos - gl_FragCoord.xy));
    }                                  // x1 <- (l0 + t0 + r0 + b0) / 2 - x0
    d += (p10 + p01 + p21 + p12) * .5 - p.x;
    d *= 0.99 * p.y;
    gl_FragColor = vec4(d, p.y,0.,0.);
}

float clampToEdge(vec2 tex_coord){
    vec4 edge = step(vec4(0.,0.,1.,1.), vec4(tex_coord, tex_coord));
    return edge.x * edge.y * (1.-edge.z) * (1.-edge.w);
}
