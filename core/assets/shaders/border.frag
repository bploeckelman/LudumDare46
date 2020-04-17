#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_time;

//input from vertex shader
varying vec2 v_texCoords;

void main() {
    vec4 finalColor = texture2D(u_texture, v_texCoords);

    gl_FragColor = finalColor;

}