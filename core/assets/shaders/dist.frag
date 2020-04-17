#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_scale;
uniform vec4 u_shadow;

varying vec4 v_color;
varying vec2 v_texCoord;


//const float smoothing = 4.0/16.0;
const float outlineWidth = 6.0/16.0;
const float outerEdgeCenter = 0.6 - outlineWidth;

void main() {
    float smoothing = 4. * u_scale / 16.;
    float distance = texture2D(u_texture, v_texCoord).a;
    float border = smoothstep(0.5 - smoothing, 0.5 + smoothing, distance);
    float alpha = smoothstep(outerEdgeCenter - smoothing, outerEdgeCenter + smoothing, distance);
    gl_FragColor = vec4( mix(u_shadow.rgb, v_color.rgb, border), alpha * v_color.a ) * v_color;
}