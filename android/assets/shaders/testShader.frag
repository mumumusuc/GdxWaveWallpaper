varying vec2 v_texCoord0;
varying vec2 v_texCoord1;

uniform sampler2D texture_0;
uniform sampler2D texture_1;

#define OPTION 3

float clampToEdge(vec2);

void main(){
    vec2 tex_0 = v_texCoord0;
    vec2 tex_1 = v_texCoord1;

#if   OPTION == 0
    gl_FragColor = texture2D(texture_0, tex_0) * clampToEdge(tex_0);

#elif OPTION == 1
    gl_FragColor = texture2D(texture_1, tex_1) * clampToEdge(tex_1);

#elif OPTION == 2
    vec4 c1 = texture2D(texture_0, tex_0) * clampToEdge(tex_0);
    vec4 c2 = texture2D(texture_1, tex_1) * clampToEdge(tex_1);
    gl_FragColor = mix(c1, c2, .5);

#elif OPTION == 3
    float a = 1. - clamp(texture2D(texture_0, tex_0).a, 0., 1.);
    gl_FragColor = vec4(0., a, 0., 1.);

#else
    gl_FragColor = vec4(
                        smoothstep(0.,100.,length(gl_FragCoord.xy)),
                        0.,
                        0., 1.);
#endif
}

float clampToEdge(vec2 tex_coord){
    vec4 edge = step(vec4(0.,0.,1.,1.), vec4(tex_coord, tex_coord));
    return edge.x * edge.y * (1.-edge.z) * (1.-edge.w);
}