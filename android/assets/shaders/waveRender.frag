varying vec2 v_texCoord0;
varying vec2 v_texCoord1;

uniform sampler2D texture_0;
uniform sampler2D texture_1;
uniform vec2 size;

#define RENDERER 1

float clampToEdge(vec2);

void main(){
    vec2 tex_0 = v_texCoord0;
    vec2 tex_1 = v_texCoord1;
    float h = texture2D(texture_0, tex_0).x * clampToEdge(tex_0);

#if RENDERER == 0
    float sh = 1.35 - h * 2.;
    vec3 c = vec3(
        exp(pow(sh - .75, 2.) * -10.),
        exp(pow(sh - .50, 2.) * -20.),
        exp(pow(sh - .25, 2.) * -10.)
    );
    gl_FragColor = vec4(c, 1.);

#elif RENDERER == 1
    vec3 e = vec3(vec2(1.)/size.xy, 0.);
    float edge = clampToEdge(tex_0);
    float p10 = texture2D(texture_0, tex_0 - e.zy).x * edge;
    float p01 = texture2D(texture_0, tex_0 - e.xz).x * edge;
    float p21 = texture2D(texture_0, tex_0 + e.xz).x * edge;
    float p12 = texture2D(texture_0, tex_0 + e.zy).x * edge;
    vec3 grad = normalize(vec3(p21 - p01, p12 - p10, 1.));
    vec4 c = texture2D(texture_1, tex_1 + grad.xy * .35) * clampToEdge(tex_1);
    vec3 light = normalize(vec3(.2, -.5, .7));
    float diffuse = dot(grad, light);
    float spec = pow(max(0., -reflect(light, grad).z), 32.);
    gl_FragColor = c * max(diffuse, 0.) + spec;

#elif RENDERER == 2
    gl_FragColor = mix(
                    texture2D(texture_0,tex_0) * clampToEdge(tex_0),
                    texture2D(texture_1,tex_1) * clampToEdge(tex_1),
                    .5
                    );

#elif RENDERER == 3
    gl_FragColor = texture2D(texture_0,tex_0) * clampToEdge(tex_0) ;

#else
    gl_FragColor = texture2D(texture_1,tex_1) * clampToEdge(tex_1);

#endif
}

float clampToEdge(vec2 tex_coord){
    vec4 edge = step(vec4(0.,0.,1.,1.), vec4(tex_coord, tex_coord));
    return edge.x * edge.y * (1.-edge.z) * (1.-edge.w);
}