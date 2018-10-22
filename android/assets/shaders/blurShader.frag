#define LOWP lowp
precision mediump float;

varying vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D u_texture;
uniform int radius;
uniform vec2 size;
uniform vec2 dir;

const int kernel_size = 20;
const float pi  = 3.14159;
const float e   = 2.71828;
const float d   = 1.5;
const float d2  = 2.0 * d * d;
const float t   = 1.0 / d / sqrt(2.0 * pi);

float gaussNum(float x) {
    return pow(e, -(x * x) / d2) * t;
}

void main(){
    vec4 sum = vec4(0.0);
    vec2 step = float(radius) / size * dir;
    if(radius <= 0){
        sum =  texture2D(u_texture, vTexCoord);
    }else{
        float weight[kernel_size];
        for (int i = 0; i < weight.length(); i++) {
                weight[i] = gaussNum(float(i));
        }
        for(int i = -radius ; i <= radius ; i++){
            sum += texture2D(u_texture, vTexCoord + float(i)*step) * weight[abs(i)];
        }
    }
    gl_FragColor = vColor * vec4(sum.rgb,sum.a);
}

