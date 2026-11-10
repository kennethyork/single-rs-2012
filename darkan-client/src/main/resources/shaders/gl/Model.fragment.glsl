// Single RS 2012: model fragment shader with per-pixel lighting.
// See Model.vertex.glsl for the varyings. ShaderMode 0 is ordinary lit
// geometry, 1 adds a specular highlight, 2 an environment reflection, 3 is
// unlit, and 4 is underwater (fog applied here instead of per vertex).

#define WRAP 0.08
#define HEMI_LOW 0.72
#define HEMI_HIGH 1.0
#define RIM_STRENGTH 0.10
#define SHEEN_STRENGTH 0.07
#define SHEEN_POWER 28.0

uniform sampler2D DiffuseSampler;
uniform samplerCube EnvironmentSampler;
uniform vec3 DistanceFogColour;
uniform vec4 HeightFogPlane;
uniform vec3 HeightFogColour;
uniform vec4 SpecularExponent;
uniform vec3 SunDir;
uniform vec3 SunColour;
uniform vec3 AntiSunColour;
uniform vec3 AmbientColour;

vec3 lighting(vec3 rawNormal, vec3 v, out vec3 extra) {
	// The engine's normals are not unit length and the 2012 lighting depends on
	// their length, so diffuse uses the raw (interpolated) normal like the
	// original; only the view-dependent terms use the normalised one.
	vec3 n = normalize(rawNormal);
	vec3 l = SunDir;
	float ndotl = dot(rawNormal, l);
	// Wrapped diffuse: light rolls off gradually instead of stopping dead.
	float sun = clamp((ndotl + WRAP) / (1.0 + WRAP), 0.0, 1.0);
	float antiSun = clamp(-ndotl, 0.0, 1.0);
	// Hemisphere ambient: the sun is always above the horizon, so its
	// vertical component tells us which way is up in this model's space.
	vec3 up = vec3(0.0, l.y >= 0.0 ? 1.0 : -1.0, 0.0);
	float hemi = mix(HEMI_LOW, HEMI_HIGH, dot(n, up) * 0.5 + 0.5);
	ndotl = clamp(ndotl, -1.0, 1.0);

	float facing = clamp(dot(n, v), 0.0, 1.0);
	float rim = pow(1.0 - facing, 3.0) * RIM_STRENGTH * clamp(ndotl + 0.6, 0.0, 1.0);
	float sheen = pow(clamp(dot(n, normalize(normalize(l) + v)), 0.0, 1.0), SHEEN_POWER) * SHEEN_STRENGTH * clamp(ndotl * 4.0, 0.0, 1.0);
	extra = SunColour * (rim + sheen);
	return AmbientColour * hemi + sun * SunColour + antiSun * AntiSunColour;
}

void main() {
	vec4 vertexColour = gl_Color;
	vec4 add = gl_SecondaryColor;
	float fogMul = gl_TexCoord[3].w;
	vec4 diffuse = texture2D(DiffuseSampler, gl_TexCoord[0].xy);

	vec3 light = vec3(1.0);
	vec3 extra = vec3(0.0);
	if (ShaderMode != 3) {
		light = lighting(gl_TexCoord[3].xyz, normalize(gl_TexCoord[4].xyz), extra);
	}
	light += gl_TexCoord[5].xyz;
	vec3 mulColour = light * vertexColour.rgb * fogMul;
	if (ShaderMode == 0)
		add.rgb += extra * fogMul;

	if (ShaderMode == 1) {
		float rdotv = clamp(dot(normalize(gl_TexCoord[1].xyz), normalize(gl_TexCoord[2].xyz)), 0.0, 1.0);
		vec3 spec = vec3(pow(rdotv, SpecularExponent.x));
		vec3 tinted = diffuse.rgb * mulColour;
		add.rgb += spec * gl_TexCoord[2].w * (vec3(1.0) + SpecularExponent.y * (tinted - vec3(1.0))) * diffuse.a;
	}
	if (ShaderMode == 2) {
		add.rgb += textureCube(EnvironmentSampler, gl_TexCoord[1].xyz).rgb * diffuse.a * add.a;
		mulColour *= 1.0 - diffuse.a;
	}
	if (IgnoreAlpha)
		diffuse.a = 1.0;
	if (ShaderMode == 4) {
		float heightFog = clamp(dot(vec4(0.0, gl_TexCoord[0].z, 0.0, 0.0), HeightFogPlane), 0.0, 1.0);
		mulColour *= 1.0 - heightFog;
		add.rgb = add.rgb * (1.0 - heightFog) + HeightFogColour * heightFog;
		float distanceFog = gl_TexCoord[0].w;
		mulColour *= 1.0 - distanceFog;
		add.rgb = add.rgb * (1.0 - distanceFog) + DistanceFogColour * distanceFog;
	}

	vec4 colour = diffuse * vec4(mulColour, vertexColour.a) + vec4(add.rgb, 0.0);
	if (ShaderMode == 2 || ShaderMode == 1)
		colour.a = vertexColour.a;
	gl_FragColor = colour;
}
