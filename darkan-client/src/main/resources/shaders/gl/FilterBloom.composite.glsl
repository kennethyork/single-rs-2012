// Single RS 2012 post-processing: replaces the 2012 bloom composite pass.
//
// The bloom filter's last pass already reads the full-resolution scene, so this
// pass adds modern post effects on top of Jagex's tonemapping and bloom:
//   0. Ambient occl.   - darkens creases and contact areas using scene depth
//      Sun shadows     - traces towards the sun through the depth buffer, so
//                        on-screen objects cast shadows on what is behind them
//   1. FXAA            - smooths edges multisampling misses (foliage, fences)
//   2. Sharpening      - contrast-adaptive, recovers detail in low-res textures
//   3. Tonemap + bloom - Jagex's original curve and bloom add, unchanged
//   4. Colour grade    - vibrance, gentle filmic contrast, slight warmth
//   5. Vignette        - subtle darkening towards the corners
//
// Turning Bloom off in the graphics options turns this whole pass off.
// Launch with -Ddarkan.classicPostFx=true to use the original 2012 composite.
// Kept to GLSL 1.10 so it compiles on every driver the 2012 client supports.

#define FXAA_REDUCE_MIN (1.0 / 128.0)
#define FXAA_REDUCE_MUL (1.0 / 8.0)
#define FXAA_SPAN_MAX 8.0
#define SHARPEN_STRENGTH 0.35
#define VIBRANCE 0.30
#define CONTRAST 0.22
#define WARMTH vec3(1.025, 1.0, 0.965)
#define VIGNETTE_STRENGTH 0.22
#define AO_SAMPLES 16
#define AO_RADIUS 200.0
#define AO_POWER 2.2
#define AO_BIAS 0.00004
#define SHADOW_STEPS 16
#define SHADOW_DISTANCE 1500.0
#define SHADOW_THICKNESS 260.0
#define SHADOW_BIAS 0.00002
#define SHADOW_DARKNESS 0.58

uniform sampler2D sceneTex;
uniform vec4 params;
uniform vec4 pixelOffsetAndBloomScale;
uniform sampler2D bloomTex1;

// Set by PostFx when the scene depth texture is available; aoStrength stays 0
// (ambient occlusion off) otherwise.
uniform sampler2D depthTex;
uniform float aoStrength;
uniform vec4 invProj[4];
uniform vec4 proj[4];
uniform float shadowStrength;
uniform vec3 sunView;

vec3 scene(vec2 uv) {
	return texture2D(sceneTex, uv).rgb;
}

float luma(vec3 c) {
	return dot(c, vec3(0.299, 0.587, 0.114));
}

vec3 viewPosition(vec2 tc, float depth) {
	vec4 ndc = vec4(tc * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
	vec4 v = invProj[0] * ndc.x + invProj[1] * ndc.y + invProj[2] * ndc.z + invProj[3];
	return v.xyz / v.w;
}

// View-space normal from the depth buffer. Uses whichever neighbour on each
// axis is closer in depth, so silhouettes don't produce bogus normals.
vec3 normalAt(vec2 tc, vec3 p, vec2 px) {
	vec2 dx = vec2(px.x, 0.0);
	vec2 dy = vec2(0.0, px.y);
	vec3 r = viewPosition(tc + dx, texture2D(depthTex, tc + dx).r) - p;
	vec3 l = p - viewPosition(tc - dx, texture2D(depthTex, tc - dx).r);
	vec3 u = viewPosition(tc + dy, texture2D(depthTex, tc + dy).r) - p;
	vec3 d = p - viewPosition(tc - dy, texture2D(depthTex, tc - dy).r);
	vec3 h = abs(r.z) < abs(l.z) ? r : l;
	vec3 v = abs(u.z) < abs(d.z) ? u : d;
	vec3 n = normalize(cross(h, v));
	return dot(n, p) > 0.0 ? -n : n; // face the camera
}

float ambientOcclusion(vec2 uv, vec2 px) {
	vec2 tc = fract(uv);
	float depth = texture2D(depthTex, tc).r;
	if (depth >= 0.99999)
		return 1.0; // sky
	vec3 p = viewPosition(tc, depth);
	vec3 n = normalAt(tc, p, px);
	vec3 t = normalize(cross(abs(n.y) < 0.99 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0), n));
	vec3 b = cross(n, t);

	// 4x4 interleaved rotation so neighbouring pixels sample different
	// directions; FXAA and the low strength hide the remaining pattern.
	vec2 cell = mod(floor(gl_FragCoord.xy), 4.0);
	float rotation = (cell.x * 4.0 + cell.y) * 0.3927 + mod(cell.x + cell.y, 2.0) * 0.19;

	float occlusion = 0.0;
	for (int i = 0; i < AO_SAMPLES; i++) {
		float fi = (float(i) + 0.5) / float(AO_SAMPLES);
		float angle = float(i) * 2.39996 + rotation;
		float r = sqrt(fi);
		vec3 dir = vec3(cos(angle) * r, sin(angle) * r, sqrt(1.0 - fi));
		float scale = mix(0.12, 1.0, fi * fi);
		vec3 s = p + (t * dir.x + b * dir.y + n * dir.z) * AO_RADIUS * scale;

		vec4 c = proj[0] * s.x + proj[1] * s.y + proj[2] * s.z + proj[3];
		vec2 stc = (c.xy / c.w) * 0.5 + 0.5;
		float expected = (c.z / c.w) * 0.5 + 0.5;
		float actual = texture2D(depthTex, stc).r;
		if (actual < expected - AO_BIAS) {
			vec3 hit = viewPosition(stc, actual);
			float range = clamp(AO_RADIUS / max(length(hit - p), 0.001), 0.0, 1.0);
			occlusion += range * range;
		}
	}
	float ao = 1.0 - occlusion / float(AO_SAMPLES);
	return pow(clamp(ao, 0.0, 1.0), AO_POWER);
}

// Screen-space sun shadow: 1.0 where something on screen blocks the sun.
float shadowRay(vec3 origin, float jitter) {
	for (int i = 0; i < SHADOW_STEPS; i++) {
		float f = (float(i) + jitter) / float(SHADOW_STEPS);
		vec3 s = origin + sunView * (SHADOW_DISTANCE * f * f + 12.0);
		vec4 c = proj[0] * s.x + proj[1] * s.y + proj[2] * s.z + proj[3];
		if (c.w <= 0.0)
			break;
		vec3 ndc = c.xyz / c.w;
		if (abs(ndc.x) > 1.0 || abs(ndc.y) > 1.0)
			break;
		vec2 stc = ndc.xy * 0.5 + 0.5;
		float actual = texture2D(depthTex, stc).r;
		if (actual < ndc.z * 0.5 + 0.5 - SHADOW_BIAS) {
			// Only count occluders close in front of the ray, so a distant
			// object in the foreground does not shadow everything behind it.
			vec3 hit = viewPosition(stc, actual);
			float gap = length(s) - length(hit);
			if (gap < SHADOW_THICKNESS)
				return 1.0 - smoothstep(SHADOW_THICKNESS * 0.6, SHADOW_THICKNESS, gap);
		}
	}
	return 0.0;
}

float sunShadow(vec2 uv, vec2 px) {
	vec2 tc = fract(uv);
	float depth = texture2D(depthTex, tc).r;
	if (depth >= 0.99999)
		return 0.0;
	vec3 p = viewPosition(tc, depth);
	vec3 n = normalAt(tc, p, px);
	float ndotl = dot(n, sunView);
	if (ndotl <= 0.04)
		return 0.0; // faces away from the sun: the lighting already shades it

	// Two interleaved rays per pixel soften the edge dithering.
	vec2 cell = mod(floor(gl_FragCoord.xy), 4.0);
	float jitter = (cell.x * 4.0 + cell.y) / 16.0;
	vec3 origin = p + n * 6.0;
	float shadow = 0.5 * (shadowRay(origin, jitter * 0.5) + shadowRay(origin, jitter * 0.5 + 0.5));
	return shadow * smoothstep(0.04, 0.25, ndotl);
}

vec3 fxaa(vec2 uv, vec2 px) {
	vec3 rgbNW = scene(uv + vec2(-px.x, -px.y));
	vec3 rgbNE = scene(uv + vec2(px.x, -px.y));
	vec3 rgbSW = scene(uv + vec2(-px.x, px.y));
	vec3 rgbSE = scene(uv + vec2(px.x, px.y));
	vec3 rgbM = scene(uv);
	float lumaNW = luma(rgbNW);
	float lumaNE = luma(rgbNE);
	float lumaSW = luma(rgbSW);
	float lumaSE = luma(rgbSE);
	float lumaM = luma(rgbM);
	float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
	float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));

	vec2 dir = vec2(-((lumaNW + lumaNE) - (lumaSW + lumaSE)), (lumaNW + lumaSW) - (lumaNE + lumaSE));
	float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
	float rcpDirMin = 1.0 / (min(abs(dir.x), abs(dir.y)) + dirReduce);
	dir = clamp(dir * rcpDirMin, vec2(-FXAA_SPAN_MAX), vec2(FXAA_SPAN_MAX)) * px;

	vec3 rgbA = 0.5 * (scene(uv + dir * (1.0 / 3.0 - 0.5)) + scene(uv + dir * (2.0 / 3.0 - 0.5)));
	vec3 rgbB = rgbA * 0.5 + 0.25 * (scene(uv - dir * 0.5) + scene(uv + dir * 0.5));
	float lumaB = luma(rgbB);
	return (lumaB < lumaMin || lumaB > lumaMax) ? rgbA : rgbB;
}

vec3 sharpen(vec3 col, vec2 uv, vec2 px) {
	vec3 n = scene(uv + vec2(0.0, -px.y));
	vec3 s = scene(uv + vec2(0.0, px.y));
	vec3 e = scene(uv + vec2(px.x, 0.0));
	vec3 w = scene(uv + vec2(-px.x, 0.0));
	vec3 lo = min(col, min(min(n, s), min(e, w)));
	vec3 hi = max(col, max(max(n, s), max(e, w)));
	// Sharpen less where local contrast is already high, so edges don't ring.
	vec3 amount = clamp(min(lo, 1.0 - hi) / max(hi, vec3(0.0001)), 0.0, 1.0);
	vec3 blur = 0.25 * (n + s + e + w);
	return clamp(col + (col - blur) * sqrt(amount) * SHARPEN_STRENGTH, 0.0, 1.0);
}

vec3 grade(vec3 col) {
	float l = luma(col);
	float sat = max(col.r, max(col.g, col.b)) - min(col.r, min(col.g, col.b));
	col = mix(vec3(l), col, 1.0 + VIBRANCE * (1.0 - sat));
	col = clamp(col, 0.0, 1.0);
	col = mix(col, col * col * (3.0 - 2.0 * col), CONTRAST);
	return col * WARMTH;
}

void main() {
	vec2 uv = gl_TexCoord[0].xy - pixelOffsetAndBloomScale.xy;
	vec2 px = pixelOffsetAndBloomScale.zw / 256.0;
	vec3 bloom = texture2D(bloomTex1, gl_TexCoord[0].xy * pixelOffsetAndBloomScale.zw - pixelOffsetAndBloomScale.xy).rgb;

	vec3 col = fxaa(uv, px);
	col = sharpen(col, uv, px);
	if (aoStrength > 0.0)
		col *= mix(1.0, ambientOcclusion(uv, px), aoStrength);
	if (shadowStrength > 0.0)
		col *= mix(1.0, SHADOW_DARKNESS, sunShadow(uv, px) * shadowStrength);

	// Jagex's original tonemap and bloom add.
	float preLum = 0.99 * dot(vec3(0.2126, 0.7152, 0.0722), col) + 0.01;
	float postLum = (preLum * (1.0 + preLum / params.z)) / (preLum + 1.0);
	col = col * (postLum / preLum) + bloom * params.y;

	col = grade(col);

	// Screen position: the composite triangle maps the screen to x 0..1, y 0..-1.
	vec2 screen = vec2(gl_TexCoord[0].x, -gl_TexCoord[0].y) - 0.5;
	col *= 1.0 - VIGNETTE_STRENGTH * smoothstep(0.35, 0.85, length(screen * vec2(1.0, 0.85)) * 1.25);

	gl_FragColor = vec4(col, 1.0);
}
