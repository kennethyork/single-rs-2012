// Single RS 2012: model vertex shader with per-pixel lighting.
//
// The 2012 shader lit each vertex and blended the result across triangles,
// which flattens shading. This one passes the normal and view vector to the
// fragment shader, which lights every pixel. Point lights, fog, and the
// specular/environment modes behave as before. Positions, the sun direction
// and the eye position are all in the model's own space, as in 2012.
//
// Varyings:
//   gl_FrontColor          vertex colour (+ ColourModifier), alpha
//   gl_FrontSecondaryColor fog colour to add; .w = how much survives the fog
//   gl_TexCoord[0]         texture coords; .z water depth, .w distance fog (mode 4)
//   gl_TexCoord[1]         reflection / view vector (modes 1 and 2)
//   gl_TexCoord[2]         reflected sun vector (mode 1)
//   gl_TexCoord[3]         normal; .w = fog multiplier for lit colour
//   gl_TexCoord[4]         direction to the eye
//   gl_TexCoord[5]         point light colour (summed per vertex)

uniform vec4 WVPMatrix[4];
uniform vec2 TexCoordMatrix[4];
uniform vec3 EyePos;
uniform vec4 DistanceFogPlane;
uniform vec3 DistanceFogColour;
uniform vec4 HeightFogPlane;
uniform vec3 HeightFogColour;
uniform vec3 SunDir;
uniform vec3 SunColour;
uniform vec3 AntiSunColour;
uniform vec3 AmbientColour;
uniform vec4 ColourModifier;
uniform vec3 WorldMatrix[3];
uniform vec4 PointLightsPosAndRadiusSq[4];
uniform vec4 PointLightsDiffuseColour[4];

void main() {
	gl_Position = gl_Vertex.x * WVPMatrix[0] + gl_Vertex.y * WVPMatrix[1] + gl_Vertex.z * WVPMatrix[2] + gl_Vertex.w * WVPMatrix[3];
	vec2 uv = gl_MultiTexCoord0.x * TexCoordMatrix[0] + gl_MultiTexCoord0.y * TexCoordMatrix[1]
			+ gl_MultiTexCoord0.z * TexCoordMatrix[2] + gl_MultiTexCoord0.w * TexCoordMatrix[3];
	vec4 colour = gl_Color + ColourModifier;
	vec3 toEye = EyePos.xyz - gl_Vertex.xyz;

	vec4 texCoord = vec4(uv, 0.0, 0.0);
	vec3 reflected = vec3(0.0);
	vec4 specular = vec4(0.0);
	if (ShaderMode == 1) {
		specular = vec4(reflect(-SunDir, gl_Normal), 1.0);
		reflected = normalize(toEye);
	}
	if (ShaderMode == 2) {
		vec3 r = reflect(normalize(toEye), gl_Normal);
		reflected = r.x * WorldMatrix[0] + r.y * WorldMatrix[1] + r.z * WorldMatrix[2];
	}

	vec3 pointLight = vec3(0.0);
	for (int i = 0; i < PointLightCount; i++) {
		vec3 l = PointLightsPosAndRadiusSq[i].xyz - gl_Vertex.xyz;
		float distSq = dot(l, l);
		float ndotl = clamp(dot(gl_Normal, l * inversesqrt(distSq)), 0.0, 1.0);
		pointLight += PointLightsDiffuseColour[i].xyz * (PointLightsPosAndRadiusSq[i].w / distSq) * ndotl;
	}

	float fogMul = 1.0;
	vec4 add = vec4(0.0, 0.0, 0.0, 1.0);
	if (ShaderMode == 4) {
		texCoord.z = gl_MultiTexCoord1.x;
		texCoord.w = clamp(dot(gl_Vertex, DistanceFogPlane), 0.0, 1.0);
	} else {
		float heightFog = clamp(dot(gl_Vertex, HeightFogPlane), 0.0, 1.0);
		float distanceFog = clamp(dot(gl_Vertex, DistanceFogPlane), 0.0, 1.0);
		fogMul = (1.0 - heightFog) * (1.0 - distanceFog);
		add.xyz = heightFog * HeightFogColour * (1.0 - distanceFog) + DistanceFogColour * distanceFog;
		add.w = fogMul;
		specular.w *= fogMul;
	}

	gl_FrontColor = colour;
	gl_FrontSecondaryColor = add;
	gl_TexCoord[0] = texCoord;
	gl_TexCoord[1] = vec4(reflected, 0.0);
	gl_TexCoord[2] = specular;
	gl_TexCoord[3] = vec4(gl_Normal, fogMul);
	gl_TexCoord[4] = vec4(toEye, 0.0);
	gl_TexCoord[5] = vec4(pointLight, 0.0);
}
