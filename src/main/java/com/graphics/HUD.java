package com.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class HUD {

    private int textura;
    private int shader;
    private int vao;
    private int vbo;

    private int ancho;
    private int alto;

    private BufferedImage imagen;
    private Graphics2D g;

    public HUD(int ancho, int alto) {

        this.ancho = ancho;
        this.alto = alto;
        imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        g = imagen.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        crearShader();
        crearQuad();
        crearTextura();
    }

    // ==================== DIBUJAR HUD ====================
    public void dibujar(float velocidad, boolean camaraAerea, boolean noche, 
                        boolean faros, int entregas, int totalEntregas) {

        limpiar();

        // ------------- PANEL SUPERIOR IZQUIERDO --------------------
        panel(20, 20, 300, 150);
        texto("CIUDAD 3D",40,55,28, Color.WHITE);
        texto(noche ? "Noche" : "Día",40,85,18, Color.WHITE);
        texto("Faros: " + (faros ? "ON" : "OFF"),40,110,18, Color.WHITE);
        texto("Camara: " + (camaraAerea? "AEREA": "VEHICULO"),
                40,135,18, Color.WHITE);

        // ------------- PANEL DE CONTROLES --------------------
        panel(20, alto - 145, 330, 110);
        texto("CONTROLES",40, alto - 115,20, Color.WHITE);
        texto("WASD / Flechas  -  Mover",
                40, alto - 85,16, Color.WHITE);
        texto("N  -  Día/Noche     F  -  Faros",
                40, alto - 58,16, Color.WHITE);


        // ------------- PANEL DE ENTREGAS --------------------
        panel(ancho / 2 - 110,20,220,65);
        textoCentrado(
                "Entregas: " + entregas + " / " + totalEntregas,
                ancho / 2,60,20, Color.WHITE);

        // ------------- PANEL DE VELOCIDAD --------------------
        panel(ancho - 240, alto - 145, 210, 110);
        int kmh = Math.round(Math.abs(velocidad) * 3.6f);
        textoCentrado(String.valueOf(kmh), ancho - 135,
                        alto - 95, 42, Color.WHITE);
        textoCentrado("KM/H", ancho - 135, alto - 58,
                        18, Color.WHITE);

        actualizarTextura(); // Actualizando textura

        // ------------- DIBUJAR EN OPENGL --------------------
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glUseProgram(shader);
        glBindVertexArray(vao);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textura);
        glDrawArrays(GL_TRIANGLES,0,6);
        glBindVertexArray(0);
        glUseProgram(0);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    // ==================== LIMPIEZA ====================
    private void limpiar() {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, ancho, alto);
        g.setComposite(AlphaComposite.SrcOver);
    }

    // ==================== PANEL ====================
    private void panel(int x, int y, int ancho, int alto) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(x, y, ancho, alto, 18, 18);
        g.setColor(new Color(255, 255, 255, 80));
        g.drawRoundRect(x, y, ancho, alto, 18, 18);
    }

    // ==================== TEXTO ====================
    private void texto(String texto, int x, int y, int tamaño, Color color) {
        g.setFont(new Font("Arial", Font.BOLD, tamaño));
        g.setColor(color);
        g.drawString(texto, x, y);
    }

    private void textoCentrado(String texto, int x, int y, int tamaño, Color color) {
        g.setFont(new Font("Arial", Font.BOLD, tamaño));
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        int anchoTexto = fm.stringWidth(texto);
        g.drawString(texto, x - anchoTexto / 2, y);
    }

    // ==================== TEXTURA ====================
    private void crearTextura() {

        textura = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textura);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void actualizarTextura() {
        int[] pixels = new int[ancho * alto];
        imagen.getRGB(0, 0, ancho, alto, pixels, 0, ancho);
        ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer(
                            ancho * alto * 4);

        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();
        glBindTexture(GL_TEXTURE_2D, textura);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, ancho, alto,
                    0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    // ==================== QUAD ====================
    private void crearQuad() {

        float[] vertices = {
            // posición      // UV
            -1f, -1f,       0f, 1f,
            1f, -1f,       1f, 1f,
            1f,  1f,       1f, 0f,

            -1f, -1f,       0f, 1f,
            1f,  1f,       1f, 0f,
            -1f,  1f,       0f, 0f
        };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER,vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false,
                                4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false,
                                4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    // ==================== SHADER ====================
    private void crearShader() {
        String vertexShader =
                """
                #version 330 core
                layout(location = 0) in vec2 posicion;
                layout(location = 1) in vec2 uv;
                out vec2 texCoord;
                void main() {
                    gl_Position = vec4(posicion, 0.0, 1.0);
                    texCoord = uv;
                }
                """;
        String fragmentShader =
                """
                #version 330 core
                in vec2 texCoord;
                out vec4 color;
                uniform sampler2D textura;
                void main() {
                    color = texture(textura, texCoord);
                }
                """;
        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vertexShader);
        glCompileShader(vs);
        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, fragmentShader);
        glCompileShader(fs);
        shader = glCreateProgram();
        glAttachShader(shader, vs);
        glAttachShader(shader, fs);
        glLinkProgram(shader);
        glDeleteShader(vs);
        glDeleteShader(fs);
    }
}