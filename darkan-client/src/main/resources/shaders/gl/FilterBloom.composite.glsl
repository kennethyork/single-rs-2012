// Single RS 2012 post-processing: replaces the 2012 bloom composite pass.
//
// The bloom filter's last pass already reads the full-resolution scene, so this
// pass adds modern post effects on top of Jagex's tonemapping and bloom:
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

uniform sampler2D sceneTex;
uniform vec4 params;
uniform vec4 pixelOffsetAndBloomScale;
uniform sampler2D bloomTex1;

vec3 scene(vec2 uv) {
	return texture2D(sceneTex, uv).rgb;
}

float luma(vec3 c) {
	return dot(c, vec3(0.299, 0.587, 0.114));
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
