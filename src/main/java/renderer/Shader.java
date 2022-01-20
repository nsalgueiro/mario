package renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

  private int shaderProgramID;
  private String vertexSource;
  private String fragmentSource;
  private String filepath;

  private boolean beingUsed = false;

  public Shader(String filepath) {
    this.filepath = filepath;
    try {
      String source = new String(Files.readAllBytes(Paths.get(filepath)));
      String[] splitString = source.split("(//#type)( )+([a-zA-Z]+)");

      int index = source.indexOf("//#type") + 8;
      int eol = source.indexOf("\n");
      String firstPattern = source.substring(index, eol).trim();

      index = source.indexOf("//#type", eol) + 8;
      eol = source.indexOf("\n", index);
      String secondPattern = source.substring(index, eol).trim();

      if (firstPattern.equals("vertex")) {
        vertexSource = splitString[1];
      } else if (firstPattern.equals("fragment")) {
        fragmentSource = splitString[1];
      } else {
        throw new IOException("Unexpected token '" + firstPattern + "'");
      }

      if (secondPattern.equals("vertex")) {
        vertexSource = splitString[2];
      } else if (secondPattern.equals("fragment")) {
        fragmentSource = splitString[2];
      } else {
        throw new IOException("Unexpected token '" + secondPattern + "'");
      }
    } catch (IOException e) {
      e.printStackTrace();
      assert false : "Error: Could not open file for shader: '" + filepath + "'";
    }
  }

  public void compile() {
    int vertexID, fragmentID;
    // Compile and link shaders

    // First load and compile the vertex shader
    vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
    // Pass the shader source to the GPU
    GL20.glShaderSource(vertexID, vertexSource);
    GL20.glCompileShader(vertexID);

    // Check for errors in compilation
    int success = GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS);
    if (success == GL11.GL_FALSE) {
      int len = GL20.glGetShaderi(vertexID, GL20.GL_INFO_LOG_LENGTH);
      System.out.println("ERROR: '" + filepath + "'\n\tVertex shader compilation failed.");
      System.out.println(GL20.glGetShaderInfoLog(vertexID, len));
      assert false : "";
    }

    // First load and compile the fragment shader
    fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
    // Pass the shader source to the GPU
    GL20.glShaderSource(fragmentID, fragmentSource);
    GL20.glCompileShader(fragmentID);

    // Check for errors in compilation
    success = GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS);
    if (success == GL11.GL_FALSE) {
      int len = GL20.glGetShaderi(fragmentID, GL20.GL_INFO_LOG_LENGTH);
      System.out.println("ERROR: '" + filepath + "'\n\tVertex shader compilation failed.");
      System.out.println(GL20.glGetShaderInfoLog(fragmentID, len));
      assert false : "";
    }

    // Link shaders and check for errors
    shaderProgramID = GL20.glCreateProgram();
    GL20.glAttachShader(shaderProgramID, vertexID);
    GL20.glAttachShader(shaderProgramID, fragmentID);
    GL20.glLinkProgram(shaderProgramID);

    // Check for linking errors
    success = glGetProgrami(shaderProgramID, GL20.GL_LINK_STATUS);
    if (success == GL11.GL_FALSE) {
      int len = GL20.glGetProgrami(shaderProgramID, GL20.GL_INFO_LOG_LENGTH);
      System.out.println("ERROR: '" + filepath + "'\n\tLinking of shaders failed.");
      System.out.println(GL20.glGetProgramInfoLog(shaderProgramID, len));
      assert false : "";
    }
  }

  public void use() {
    if (!beingUsed) {
      glUseProgram(shaderProgramID);
      beingUsed = true;
    }
  }

  public void detach() {
    glUseProgram(0);
    beingUsed = false;
  }

  public void uploadMat4f(String varName, Matrix4f mat4) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
    mat4.get(matBuffer);
    glUniformMatrix4fv(varLocation, false, matBuffer);
  }

  public void uploadMat3f(String varName, Matrix3f mat3) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
    mat3.get(matBuffer);
    glUniformMatrix3fv(varLocation, false, matBuffer);
  }

  public void uploadVec4f(String varName, Vector4f vec) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform4f(varLocation, vec.x, vec.y, vec.z, vec.w);
  }

  public void uploadVec3f(String varName, Vector3f vec) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform3f(varLocation, vec.x, vec.y, vec.z);
  }

  public void uploadVec2f(String varName, Vector2f vec) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform2f(varLocation, vec.x, vec.y);
  }

  public void uploadFloat(String varName, float val) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform1f(varLocation, val);
  }

  public void uploadInt(String varName, int val) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform1i(varLocation, val);
  }

  public void uploadTexture(String varName, int slot) {
    int varLocation = glGetUniformLocation(shaderProgramID, varName);
    use();
    glUniform1i(varLocation, slot);
  }
}
