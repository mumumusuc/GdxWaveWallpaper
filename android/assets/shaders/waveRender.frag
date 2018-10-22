varying vec4 vColor;
varying vec2 vTexCoord;
varying vec4 vPosition;

uniform sampler2D buffer_0;
uniform sampler2D buffer_1;
uniform vec2 res;
uniform vec2 res1;

#define RENDERER 1

void main(){
    vec2 q = gl_FragCoord.xy/res;
    float h = texture2D(buffer_0, q).x;
#if RENDERER == 0
    float sh = 1.35 - h * 2.;
    vec3 c = vec3(
        exp(pow(sh - .75, 2.) * -10.),
        exp(pow(sh - .50, 2.) * -20.),
        exp(pow(sh - .25, 2.) * -10.)
    );
    gl_FragColor = vec4(c, 1.);
#elif RENDERER == 1
    vec3 e = vec3(vec2(1.)/res, 0.);
    float p10 = texture2D(buffer_0, q-e.zy).x;
    float p01 = texture2D(buffer_0, q-e.xz).x;
    float p21 = texture2D(buffer_0, q+e.xz).x;
    float p12 = texture2D(buffer_0, q+e.zy).x;
    vec3 grad = normalize(vec3(p21 - p01, p12 - p10, 1.));
    vec4 c = texture2D(buffer_1, gl_FragCoord.xy * 2. / res1 + grad.xy * .35);
    vec3 light = normalize(vec3(.2, -.5, .7));
    float diffuse = dot(grad, light);
    float spec = pow(max(0., -reflect(light, grad).z), 32.);
    gl_FragColor = mix(c, vec4(.7, .8, 1., 1.), .25) * max(diffuse, 0.) + spec;
#elif RENDERER == 2
    gl_FragColor = mix(texture2D(buffer_0,q), texture2D(buffer_1,q), .5);
#elif RENDERER == 3
    gl_FragColor = texture2D(buffer_0,q);
#else
    gl_FragColor = texture2D(buffer_1,q);
#endif
}

