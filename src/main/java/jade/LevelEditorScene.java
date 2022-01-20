package jade;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import renderer.Shader;
import renderer.Texture;
import util.Time;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class LevelEditorScene extends Scene {

  private Shader defaultShader;

  private float[] vertexArray = {
           // position          // color                    // UV Coordinates
           100f, 0f,   0.0f,    1.0f, 0.0f, 0.0f, 1.0f,     1, 1, // Bottom right
           0f,   100f, 0.0f,    0.0f, 1.0f, 0.0f, 1.0f,     0, 0, // Top left
           100f, 100f, 0.0f,    0.0f, 0.0f, 1.0f, 1.0f,     1, 0, // Top right
           0f,   0f,   0.0f,    1.0f, 1.0f, 0.0f, 1.0f,     0, 1  // Bottom left
  };

  // IMPORTANT: Must be in a counter-clockwise order
  private int[] elementArray = {
        2, 1, 0, // Top right triangle
        0, 1, 3
  };

  private int vaoID, vboID, eboID;

  private Texture testTexture;

  public LevelEditorScene() {}

  @Override
  public void init() {

    System.out.println("GL Version: " + glGetString(GL_VERSION));
    this.camera = new Camera(new Vector2f());
    defaultShader = new Shader("assets/shaders/default.glsl");
    defaultShader.compile();
    this.testTexture = new Texture("assets/images/testimage.png");

    // Generate VAO, VBO and EBO buffer objects and send to GPU
    vaoID = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(vaoID);

    // Create a float buffer of the vertices
    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
    vertexBuffer.put(vertexArray).flip();

    // Create VBO upload the vertex buffer
    vboID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vboID);
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

    // Create the indices and upload
      IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
      elementBuffer.put(elementArray).flip();

      eboID = glGenBuffers();
      glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
      glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

      // Add vertex attribute pointers
      int positionsSize = 3;
      int colorSize = 4;
      int uvSize = 2;
      int vertexSizeBites = (positionsSize + colorSize + uvSize) * Float.BYTES;
      glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBites, 0);
      glEnableVertexAttribArray(0);

      glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBites, positionsSize * Float.BYTES);
      glEnableVertexAttribArray(1);

      glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBites, (positionsSize + colorSize) * Float.BYTES);
      glEnableVertexAttribArray(2);
  }

  @Override
  public void update(float dt) {
      // Bind shader program
      defaultShader.use();

      // Upload texture to shader
      defaultShader.uploadTexture("TEX_SAMPLE", 0);
      glActiveTexture(GL_TEXTURE0);
      testTexture.bind();

      defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
      defaultShader.uploadMat4f("uView", camera.getViewMatrix());
      defaultShader.uploadFloat("uTime", Time.getTime());
      // Bind the VAO that we're using
      GL30.glBindVertexArray(vaoID);

      // Enable the vertex attribute pointers
      glEnableVertexAttribArray(0);
      glEnableVertexAttribArray(1);

      glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

      //Unbind everything
      glDisableVertexAttribArray(0);
      glDisableVertexAttribArray(1);

      GL30.glBindVertexArray(0);
      defaultShader.detach();
  }
}
