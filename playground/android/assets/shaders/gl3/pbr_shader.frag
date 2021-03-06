#ifdef GL_ES
    #ifdef GPU_MALI
        #define LOWP lowp
        #define MEDP mediump
        #define HIGHP highp
    #elif defined GPU_ADRENO
        #define LOWP
        #define MEDP
        #define HIGHP
    #else
        #define MEDP
        #define LOWP
        #define HIGHP
    #endif

    precision mediump float;
#else
    #define MEDP
    #define LOWP
    #define HIGHP
#endif

#define M_PI 3.14159265359
#define MAX_REFLECTION_LOD 4.0

out vec4 fragmentColor;

uniform LOWP float u_ambient;
uniform LOWP int u_graphicsWidth;
uniform LOWP int u_graphicsHeight;
uniform HIGHP mat4 u_viewMatrix;

#ifdef numDirectionalLights
    #if numDirectionalLights > 0
        struct DirectionalLight {
            LOWP vec3 color;
            LOWP vec3 direction;
            LOWP float intensity;
        };

        uniform DirectionalLight u_dirLights[numDirectionalLights];
    #endif
#endif

#ifdef numPointLights
    #if numPointLights > 0
        struct PointLight {
            LOWP vec3 color;
            LOWP vec3 position;
            LOWP float intensity;
            LOWP float radius;
        };

        uniform PointLight u_pointLights[numPointLights];
    #endif
#endif

#ifdef numSpotLights
    #if numSpotLights > 0
        struct SpotLight {
            LOWP vec3 color;
            LOWP vec3 position;
            LOWP vec3 direction;
            LOWP float intensity;
            LOWP float innerAngle;
            LOWP float outerAngle;
        };

        uniform SpotLight u_spotLights[numSpotLights];
    #endif
#endif

#ifdef defAlbedo
    uniform LOWP sampler2D u_albedo;
    uniform LOWP vec2 u_albedoTiles;
#endif

#ifdef defMetalness
    uniform LOWP sampler2D u_metalness;
    uniform LOWP vec2 u_metalnessTiles;
#endif

#ifdef defRoughness
    uniform LOWP sampler2D u_roughness;
    uniform LOWP vec2 u_roughnessTiles;
#endif

#ifdef defNormal
    uniform LOWP sampler2D u_normal;
    uniform LOWP vec2 u_normalTiles;
#endif

#ifdef defAmbientOcclusion
    uniform LOWP sampler2D u_ambientOcclusion;
    uniform LOWP vec2 u_ambientOcclusionTiles;
#endif

#ifdef defImageBasedLighting
    uniform LOWP samplerCube u_irradiance;
    uniform LOWP samplerCube u_prefilter;
    uniform LOWP sampler2D u_brdf;
#endif

in HIGHP vec3 v_position;
in HIGHP vec3 v_binormal;
in HIGHP vec3 v_tangent;
in LOWP vec2 v_texCoord;
in LOWP vec3 v_normal;

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(1.0 - cosTheta, 5.0);
}

float distributionGGX(vec3 N, vec3 H, float rough) {
    float a = rough*rough;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom = a2;

    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = M_PI * denom * denom;
    denom = max(0.0001, denom);

    return nom / denom;
}

float geometrySchlickGGX(float NdotV, float rough) {
    float r = (rough + 1.0);
    float k = (r*r) / 8.0;

    float nom = NdotV;

    float denom = NdotV * (1.0 - k) + k;
    denom = max(0.0001, denom);

    return nom / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float rough) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = geometrySchlickGGX(NdotV, rough);
    float ggx1 = geometrySchlickGGX(NdotL, rough);

    return ggx1 * ggx2;
}

vec3 saturation(vec3 rgb, float adjustment) {
    const vec3 W = vec3(0.2125, 0.7154, 0.0721);
    vec3 intensity = vec3(dot(rgb, W));
    return mix(intensity, rgb, adjustment);
}

void main() {
    // Nota: NON SETTARE MAI METALNESS O ROUGHNESS A 0.0

    #ifdef defAlbedo
        LOWP vec4 albedo = texture(u_albedo, fract(v_texCoord / u_albedoTiles));
        if (albedo.a < 0.01) discard;
        albedo = pow(albedo, vec4(2.2));
    #else
        LOWP vec4 albedo = vec4(1.0);
    #endif

    LOWP vec3 color;

    #ifdef defMetalness
        LOWP float metalness = texture(u_metalness, fract(v_texCoord / u_metalnessTiles)).r;
    #else
        LOWP float metalness = 0.5;
    #endif

    #ifdef defRoughness
        LOWP float roughness = texture(u_roughness, fract(v_texCoord / u_roughnessTiles)).r;
    #else
        LOWP float roughness = 0.1;
    #endif

    #ifdef defAmbientOcclusion
        LOWP float ambientOcclusion = texture(u_ambientOcclusion, fract(v_texCoord / u_ambientOcclusionTiles)).r;
    #else
        LOWP float ambientOcclusion = 1.0;
    #endif

    #ifdef defNormal
        LOWP vec3 normalMap = texture(u_normal, fract(v_texCoord / u_normalTiles)).rgb;

        LOWP vec3 N = normalize(v_normal);
        LOWP vec3 tangent = normalize(v_tangent);
        LOWP vec3 bitangent = cross(tangent, N);

        tangent = normalize(tangent - dot(tangent, N) * N);
        LOWP mat3 TBN = mat3(tangent, bitangent, N);

        N = normalize(TBN * (normalMap * 2.0 - 1.0));
    #else
        LOWP vec3 N = normalize(v_normal);
    #endif

    LOWP vec3 V = normalize(-v_position);

    LOWP vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo.rgb, metalness);

    LOWP vec3 Lo = vec3(0.0);

    // Directional lights
    #ifdef numDirectionalLights
        #if numDirectionalLights > 0
            for (int i = 0; i < numDirectionalLights; i++) {
                LOWP vec3 direction = -u_dirLights[i].direction;
                LOWP float distance = length(direction);
                LOWP vec3 radiance = u_dirLights[i].color;

                LOWP vec3 L = normalize(direction);
                LOWP vec3 H = normalize(V + L);

                // cook-torrance brdf
                LOWP float NDF = distributionGGX(N, H, roughness);
                LOWP float G = geometrySmith(N, V, L, roughness);
                LOWP vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);

                LOWP vec3 kS = F;
                LOWP vec3 kD = vec3(1.0) - kS;
                kD *= 1.0 - metalness;

                LOWP vec3 numerator = NDF * G * F;
                LOWP float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0);
                LOWP vec3 specular = numerator / max(denominator, 0.001);

                // add to outgoing radiance Lo
                LOWP float NdotL = max(dot(N, L), 0.0) * u_dirLights[i].intensity;
                Lo += (kD * albedo.rgb / M_PI + specular) * radiance * NdotL;
            }
        #endif
    #endif

    // Point lights
    #ifdef numPointLights
        #if numPointLights > 0
            for (int i = 0; i < numPointLights; i++) {
                LOWP vec3 direction = u_pointLights[i].position - v_position;
                LOWP float distance = length(direction);
                LOWP float attenuation = 1.0 / (distance * distance);
                LOWP vec3 radiance = u_pointLights[i].color * attenuation;

                LOWP vec3 L = normalize(direction);
                LOWP vec3 H = normalize(V + L);

                // cook-torrance brdf
                LOWP float NDF = distributionGGX(N, H, roughness);
                LOWP float G = geometrySmith(N, V, L, roughness);
                LOWP vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);

                LOWP vec3 kS = F;
                LOWP vec3 kD = vec3(1.0) - kS;
                kD *= 1.0 - metalness;

                LOWP vec3 numerator = NDF * G * F;
                LOWP float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0);
                LOWP vec3 specular = numerator / max(denominator, 0.001);

                // add to outgoing radiance Lo
                LOWP float NdotL = max(dot(N, L), 0.0) * u_pointLights[i].intensity;
                Lo += (kD * albedo.rgb / M_PI + specular) * radiance * NdotL;
            }
        #endif
    #endif

    // Spot lights
    #ifdef numSpotLights
        #if numSpotLights > 0
            for (int i = 0; i < numPointLights; i++) {
                LOWP vec3 direction = u_spotLights[i].position - v_position;
                LOWP float distance = length(direction);
                LOWP float attenuation = 1.0 / (distance * distance);
                LOWP vec3 radiance = u_spotLights[i].color * attenuation;

                float currentAngle = dot(-normalize(direction), normalize(u_spotLights[i].direction));
                float innerConeAngle = cos(radians(u_spotLights[i].innerAngle));
                float outerConeAngle = cos(radians(u_spotLights[i].outerAngle));
                float conesAngleDiff = abs(innerConeAngle - outerConeAngle);

                float spotEffect = clamp((currentAngle - outerConeAngle) / conesAngleDiff, 0.0, 1.0);
                radiance *= spotEffect;

                LOWP vec3 L = normalize(direction);
                LOWP vec3 H = normalize(V + L);

                // cook-torrance brdf
                LOWP float NDF = distributionGGX(N, H, roughness);
                LOWP float G = geometrySmith(N, V, L, roughness);
                LOWP vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);

                LOWP vec3 kS = F;
                LOWP vec3 kD = vec3(1.0) - kS;
                kD *= 1.0 - metalness;

                LOWP vec3 numerator = NDF * G * F;
                LOWP float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0);
                LOWP vec3 specular = numerator / max(denominator, 0.001);

                // add to outgoing radiance Lo
                LOWP float NdotL = max(dot(N, L), 0.0) * u_spotLights[i].intensity;
                Lo += (kD * albedo.rgb / M_PI + specular) * radiance * NdotL;
            }
        #endif
    #endif

    #ifdef defImageBasedLighting
        LOWP vec3 R = reflect(-V, N);
        R = vec3(inverse(u_viewMatrix) * vec4(R, 0.0));

        LOWP vec3 F = fresnelSchlickRoughness(max(dot(N, V), 0.0), F0, roughness);

        LOWP vec3 kS = F;
        LOWP vec3 kD = 1.0 - kS;
        kD *= 1.0 - metalness;

        LOWP vec3 irradiance = texture(u_irradiance, N).rgb;
        LOWP vec3 diffuse = irradiance * albedo.rgb;

        LOWP vec3 prefilteredColor = textureLod(u_prefilter, R, roughness * MAX_REFLECTION_LOD).rgb;
        LOWP vec2 brdf = texture(u_brdf, vec2(max(dot(N, V), 0.0), roughness)).rg;
        LOWP vec3 specular = prefilteredColor * (F * brdf.x + brdf.y);

        LOWP vec3 ambient = (kD * diffuse + specular) * ambientOcclusion;
    #else
        LOWP vec3 ambient = vec3(u_ambient) * albedo.rgb;
    #endif

    color = ambient + Lo;

    #ifdef defGammaCorrection
        color = color / (color + vec3(1.0));
        color = pow(color, vec3(1.0 / 2.2));
    #endif

    fragmentColor = vec4(color.rgb, albedo.a);
}