#version 150

in vec3 localDir;

uniform float GameTime;
uniform vec2 CameraYawPitch;

out vec4 fragColor;

#define PI 3.14159265359

float hash11(float n) {
    return fract(sin(n) * 43758.5453123);
}

float hash21(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float hash31(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453123);
}

vec2 hash22(vec2 p) {
    return fract(sin(vec2(
    dot(p, vec2(127.1, 311.7)),
    dot(p, vec2(269.5, 183.3))
    )) * 43758.5453123);
}

vec3 hash33(vec3 p) {
    return fract(sin(vec3(
    dot(p, vec3(127.1, 311.7, 74.7)),
    dot(p, vec3(269.5, 183.3, 246.1)),
    dot(p, vec3(113.5, 271.9, 124.6))
    )) * 43758.5453123);
}

float noise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    return mix(
    mix(
    mix(hash31(i + vec3(0.0, 0.0, 0.0)), hash31(i + vec3(1.0, 0.0, 0.0)), f.x),
    mix(hash31(i + vec3(0.0, 1.0, 0.0)), hash31(i + vec3(1.0, 1.0, 0.0)), f.x),
    f.y
    ),
    mix(
    mix(hash31(i + vec3(0.0, 0.0, 1.0)), hash31(i + vec3(1.0, 0.0, 1.0)), f.x),
    mix(hash31(i + vec3(0.0, 1.0, 1.0)), hash31(i + vec3(1.0, 1.0, 1.0)), f.x),
    f.y
    ),
    f.z
    );
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;

    for (int i = 0; i < 5; i++) {
        v += noise(p) * a;
        p *= 2.0;
        a *= 0.5;
    }

    return v;
}

vec3 rotateX(vec3 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(
    p.x,
    c * p.y - s * p.z,
    s * p.y + c * p.z
    );
}

vec3 rotateY(vec3 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(
    c * p.x + s * p.z,
    p.y,
    -s * p.x + c * p.z
    );
}

vec3 rotateZ(vec3 p, float a) {
    float c = cos(a);
    float s = sin(a);
    return vec3(
    c * p.x - s * p.y,
    s * p.x + c * p.y,
    p.z
    );
}

vec3 rotateCameraToWorld(vec3 p, vec2 yawPitch) {
    float yaw = -yawPitch.x;
    float pitch = -yawPitch.y;

    p = rotateX(p, pitch);
    p = rotateY(p, yaw);

    return normalize(p);
}

vec3 cubemapUV(vec3 d) {
    vec3 a = abs(d);
    vec2 uv;
    float face;

    if (a.x >= a.y && a.x >= a.z) {
        if (d.x > 0.0) {
            uv = vec2(-d.z, d.y) / a.x;
            face = 0.0;
        } else {
            uv = vec2(d.z, d.y) / a.x;
            face = 1.0;
        }
    } else if (a.y >= a.x && a.y >= a.z) {
        if (d.y > 0.0) {
            uv = vec2(d.x, -d.z) / a.y;
            face = 2.0;
        } else {
            uv = vec2(d.x, d.z) / a.y;
            face = 3.0;
        }
    } else {
        if (d.z > 0.0) {
            uv = vec2(d.x, d.y) / a.z;
            face = 4.0;
        } else {
            uv = vec2(-d.x, d.y) / a.z;
            face = 5.0;
        }
    }

    uv = uv * 0.5 + 0.5;
    return vec3(uv, face);
}

float starCubeLayer(vec3 dir, float grid, float threshold, float radius) {
    vec3 cuv = cubemapUV(dir);

    vec2 p = cuv.xy * grid;
    vec2 cell = floor(p);
    vec2 local = fract(p);

    vec2 id = cell + vec2(cuv.z * 1000.0, cuv.z * 217.0);

    vec2 starPos = hash22(id);
    starPos = mix(vec2(0.18), vec2(0.82), starPos);

    float rnd = hash21(id + vec2(17.13, 8.91));
    float appear = step(threshold, rnd);

    float dist = length(local - starPos);
    float star = smoothstep(radius, 0.0, dist);

    float bright = mix(0.45, 1.8, hash21(id + vec2(91.7, 44.2)));

    return star * appear * bright;
}

vec3 stars(vec3 dir, float t) {
    float s = 0.0;

    s += starCubeLayer(dir, 42.0, 0.82, 0.055);
    s += starCubeLayer(dir, 78.0, 0.90, 0.040);
    s += starCubeLayer(dir, 138.0, 0.955, 0.030);

    float twinkle = 0.88 + 0.12 * sin(t * 2.2 + noise(dir * 90.0) * 18.0);
    s *= twinkle;

    vec3 col = vec3(
    0.78 + 0.22 * noise(dir * 70.0 + vec3(1.0)),
    0.80 + 0.20 * noise(dir * 80.0 + vec3(2.0)),
    0.90 + 0.10 * noise(dir * 90.0 + vec3(3.0))
    );

    return s * col;
}

vec3 galaxy(vec3 dir, float t) {
    vec3 gdir = dir;

    vec3 normal = normalize(vec3(0.22, 0.58, 0.78));
    vec3 center = normalize(vec3(-0.68, 0.16, 0.72));

    float plane = abs(dot(gdir, normal));

    float wide = exp(-plane * plane * 12.0);
    float core = exp(-plane * plane * 64.0);

    float centerGlow = pow(max(dot(gdir, center) * 0.5 + 0.5, 0.0), 3.2);

    float cloud =
    fbm(gdir * 3.5 + vec3(0.0, t * 0.015, 0.0)) * 0.50 +
    fbm(gdir * 9.0 + vec3(t * 0.010, 0.0, -t * 0.008)) * 0.35 +
    fbm(gdir * 24.0 + vec3(0.0, 0.0, t * 0.006)) * 0.15;

    float dust =
    fbm(gdir * 15.0 + vec3(4.1, 2.7 + t * 0.06, 1.3)) *
    fbm(gdir * 38.0 + vec3(1.7 - t * 0.04, 8.2, 3.4));

    float brightness = wide * cloud;
    brightness += core * 0.22;
    brightness *= 0.35 + centerGlow * 1.35;
    brightness *= 1.0 - dust * core * 0.75;

    vec3 cold = vec3(0.26, 0.36, 0.85);
    vec3 warm = vec3(0.85, 0.62, 0.42);

    return mix(cold, warm, centerGlow) * brightness * 0.85;
}


void main() {
    float t = GameTime * 24000.0;

    vec2 cam = radians(CameraYawPitch);
    vec3 dir = rotateCameraToWorld(normalize(localDir), cam);

    vec3 color = vec3(0.0);

    color += galaxy(dir, t);
    color += stars(dir, t);

    fragColor = vec4(color, 1.0);
}