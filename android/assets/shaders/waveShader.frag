varying vec4 vColor;
varying vec2 vTexCoord;
varying vec4 vPosition;

uniform sampler2D buffer_0;
uniform sampler2D buffer_1;
uniform vec2 res;
uniform vec3 point;
uniform float time;

void main(){
    vec2 q = gl_FragCoord.xy/res;
    vec3 e = vec3(vec2(1.)/res, 0.);

    float p11 = texture2D(buffer_0, q).x;
    float p10 = texture2D(buffer_1, q-e.zy).x;  // down
    float p01 = texture2D(buffer_1, q-e.xz).x;  // left
    float p21 = texture2D(buffer_1, q+e.xz).x;  // right
    float p12 = texture2D(buffer_1, q+e.zy).x;  // up

    float d = 0.;
    if(point.z > 0.){                   // when touched
        float len = length(point.xy - vPosition.xy);
        d = smoothstep(4.5,.5, len) * -1.;
    }else{                              // simulate rain drop
        float t = time * 2.;
        vec2 pos = fract(floor(t) * vec2(0.456665, 0.708618)) * res;
        float amp = 1.5 - step(.05, fract(t));
        d = -amp * smoothstep(2.5, .5, length(pos - gl_FragCoord.xy));
    }                                  // x1 <- (l0 + t0 + r0 + b0) / 2 - x0
    d += (p10 + p01 + p21 + p12) * .5 - p11;
    d *= 0.99;
    gl_FragColor = vec4(d, 0.,0.,0.);
}

