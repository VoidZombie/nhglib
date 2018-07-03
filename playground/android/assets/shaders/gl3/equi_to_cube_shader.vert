#version 300 es

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

in vec3 a_position;
in vec2 a_texCoord0;

out vec3 v_localPos;
out vec2 v_texCoord;

uniform mat4 u_projection;
uniform mat4 u_view;

void main()
{
    v_localPos = a_position;
    v_texCoord = a_texCoord0;
    gl_Position =  u_projection * u_view * vec4(v_localPos, 1.0);
}