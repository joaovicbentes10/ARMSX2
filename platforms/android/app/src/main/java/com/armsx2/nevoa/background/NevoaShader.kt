package com.armsx2.nevoa.background

// The BIOS-boot "universe": sky, rising fog, distant towers of light, two light ribbons, twinkling motes.
// Colors come in as uniforms from NevoaPalette; calm = 1 halves speed and brightness.
const val NEVOA_AGSL = """
uniform float2 iResolution;
uniform float iTime;
uniform float calm;
layout(color) uniform half4 cSky;
layout(color) uniform half4 cFog;
layout(color) uniform half4 cBlue;
layout(color) uniform half4 cViolet;
layout(color) uniform half4 cCyan;

float hash2(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// Multiply/add only: a sin()-based hash gives different values on phone GPUs than on the desktop
// that renders the intro, which moved the towers at the hand-off. Keep in sync with intro.html.
float hash1(float n) {
    n = fract(n * 0.1031);
    n *= n + 33.33;
    n *= n + n;
    return fract(n);
}

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash2(i), hash2(i + float2(1.0, 0.0)), u.x),
               mix(hash2(i + float2(0.0, 1.0)), hash2(i + float2(1.0, 1.0)), u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p = p * 2.03 + float2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / iResolution;
    float aspect = iResolution.x / iResolution.y;
    float h = 1.0 - uv.y;
    float t = iTime * mix(1.0, 0.5, calm);
    float glow = mix(1.0, 0.5, calm);
    float3 col = float3(cSky.rgb);

    for (int i = 0; i < 12; i++) {
        float fi = float(i);
        float x = (fi + 0.5) / 12.0 + (hash1(fi * 7.13) - 0.5) * 0.06;
        float w = 0.0025 + hash1(fi * 3.7) * 0.004;
        float top = 0.35 + hash1(fi * 1.91) * 0.4;
        float dx = abs(uv.x - x) * aspect;
        float shape = (1.0 - smoothstep(w * 0.3, w, dx)) + exp(-dx / (w * 6.0)) * 0.25;
        float fade = pow(clamp(1.0 - h / top, 0.0, 1.0), 1.5);
        float pulse = 0.6 + 0.4 * sin(t * 0.6 + fi * 1.7);
        float k = mod(fi, 3.0);
        float3 tc = k < 0.5 ? float3(cBlue.rgb) : (k < 1.5 ? float3(cViolet.rgb) : float3(cCyan.rgb));
        col += tc * shape * fade * pulse * 0.55 * glow;
    }

    for (int j = 0; j < 2; j++) {
        float fj = float(j);
        float y = 0.55 + 0.12 * sin(uv.x * (3.0 + fj * 1.2) + t * 0.15 * (fj + 1.0) + fj * 2.0);
        float d = h - y;
        float band = exp(-d * d / 0.0008) * (0.5 + 0.5 * sin(uv.x * 2.0 - t * 0.1 + fj * 3.0));
        col += float3(cFog.rgb) * band * 0.35 * glow;
    }

    float f = fbm(float2(uv.x * aspect * 1.6 + t * 0.02, h * 2.5 - t * 0.06));
    float density = f * (1.0 - smoothstep(0.0, 0.75, h));
    col = mix(col, float3(cFog.rgb), density * 0.55 * glow);

    // ~60 motes: one per cell of a 10x6 grid.
    float2 grid = float2(10.0, 6.0);
    float2 cell = floor(uv * grid);
    float r = hash2(cell);
    float2 pos = 0.2 + 0.6 * float2(hash2(cell + 3.1), hash2(cell + 7.7));
    pos.y += 0.1 * sin(t * 0.2 + r * 6.28);
    float2 dp = (fract(uv * grid) - pos) * (iResolution / grid);
    float twinkle = pow(0.5 + 0.5 * sin(t * (0.8 + r * 1.5) + r * 30.0), 4.0);
    col += float3(cCyan.rgb) * exp(-dot(dp, dp) / 4.0) * twinkle * 0.8 * glow;

    return half4(half3(clamp(col, 0.0, 1.0)), 1.0);
}
"""
