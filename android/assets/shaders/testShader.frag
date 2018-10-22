varying vec4 vColor;
varying vec2 vTexCoord;
varying vec4 vPosition;

uniform sampler2D buffer_0;
uniform sampler2D buffer_1;
uniform vec2 res;
uniform int option;

void main(){
    vec2 tex_coord = (gl_FragCoord.xy)/res;
    if(option == 0){
        gl_FragColor = texture2D(buffer_0, tex_coord);
    }else if(option == 1){
        gl_FragColor = texture2D(buffer_1, tex_coord);
    }else if(option == 2){
        vec4 c1 = texture2D(buffer_0, tex_coord);
        vec4 c2 = texture2D(buffer_1, tex_coord);
        gl_FragColor = mix(c1, c2, .5);
    }else {
        gl_FragColor = vec4(0., 0., 1., 1.);
    }
}

