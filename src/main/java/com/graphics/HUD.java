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

    // Tiempo durante el cual se muestran los controles al iniciar.
    private static final long DURACION_CONTROLES = 6_000_000_000L;
    // Instante en el que se creó el HUD.
    private final long tiempoInicio = System.nanoTime();

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
        // panel(20, 20, 190, 105);
        // Estado de iluminación
        texto(noche ? "NOCHE" : "DIA", 35, 47, 18, Color.WHITE);
        // Estado de los faros
        texto("Faros: " + (faros ? "ON" : "OFF"), 35, 75, 18, Color.WHITE);
        // Modo de cámara
        texto("Camara: " + (camaraAerea ? "AEREA" : "VEHICULO"), 35, 102, 18, Color.WHITE);

        // ------------- PANEL DE CONTROLES --------------------
        // Calculamos cuánto tiempo ha transcurrido desde que se creó el HUD.
        long tiempoTranscurrido = System.nanoTime() - tiempoInicio;
        // Mostramos los controles solamente durante los primeros 6 segundos.
        if (tiempoTranscurrido < DURACION_CONTROLES) {
            panel(20, alto - 200, 265, 165);
            texto("CONTROLES", 40, alto - 170, 20, Color.WHITE);
            texto("N  -  Día/Noche   M  -  Mapa", 40, alto - 140, 16, Color.WHITE);
            texto("C  -  Cámara        F  -  Faros", 40, alto - 110, 16, Color.WHITE);
            texto("R  -  Reiniciar      ESP  -  Freno", 40, alto - 80, 16, Color.WHITE);
            texto("WASD  -  Conducir     ", 40, alto - 50, 16, Color.WHITE);
        }

        // ------------- INDICADOR DE ENTREGAS --------------------
        textoCentrado("ENTREGAS", ancho / 2, 47, 18, Color.WHITE); // Título pequeño y discreto.
        textoCentrado(entregas + " / " + totalEntregas, ancho / 2, 72, 20, Color.WHITE); // Contador de entregas.
        // Tamaño y separación de los indicadores.
        int tamañoPunto = 15;
        int separacion = 22;

        // Calculamos el ancho total para centrar los indicadores.
        int anchoIndicadores = Math.max(0, totalEntregas - 1) * separacion;
        int inicioX = ancho / 2 - anchoIndicadores / 2;

        // Dibujamos un círculo por cada entrega.
        for (int i = 0; i < totalEntregas; i++) {
            // Las entregas completadas aparecen claras.
            if (i < entregas) {
                g.setColor(new Color(120, 230, 160));
            } else {
                // Las pendientes aparecen en gris oscuro.
                g.setColor(new Color(75, 85, 95));
            }
            int x = inicioX + i * separacion - tamañoPunto / 2;
            int y = 78;
            g.fillOval(x, y, tamañoPunto, tamañoPunto);
        }

        // ------------- PANEL DE VELOCIDAD --------------------
        g.setColor(new Color(0, 0, 0, 75)); // Fondo oscuro discreto, sin borde.
        g.fillRoundRect(ancho - 160, alto - 110, 140, 80, 18, 18);
        int kmh = Math.round(Math.abs(velocidad) * 3.6f); // Calculamos la velocidad en km/h.     
        textoCentrado(String.valueOf(kmh), ancho - 90, alto - 68, 36, Color.WHITE); // Velocidad: texto grande y protagonista.
        textoCentrado("KM/H", ancho - 90, alto - 42, 14, new Color(190, 200, 210)); // Unidad: texto pequeño y secundario.

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
        g.setColor(new Color(0, 0, 0, 75));
        g.fillRoundRect(x, y, ancho, alto, 18, 18);
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