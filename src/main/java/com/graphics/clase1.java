package com.graphics;

import java.util.HashMap; // Permite guardar las ubicaciones de las variables del shader.
import java.util.Map; // Define el tipo de diccionario de ubicaciones.

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*; // Contiene las funciones de GLFW.
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * CLASE 1: CREAR LA CIUDAD.
 * Sigue el flujo de los ejemplos: iniciar, loop, dibujar y limpiar.
 * Coordenadas: X = izquierda/derecha; Y = arriba/abajo; Z = profundidad.
 * Las clases siguientes reutilizan esta base mediante extends y super.
 * Los comentarios explican las instrucciones; las llaves solo delimitan bloques.
 */

public class clase1 {

    // ==================== 1. VARIABLES DE LA APLICACIÓN ====================
    protected long ventana; // Identificador que GLFW asigna a la ventana.
    protected int programa; // Identificador del programa que une ambos shaders.
    protected int vao; // Identificador del VAO: describe cómo leer los vértices.
    protected int vbo; // Identificador del VBO: contiene los vértices en la GPU.
    protected int ancho = 1100; // Ancho inicial de la ventana; después contiene píxeles del framebuffer.
    protected int alto = 800; // Alto inicial de la ventana; después contiene píxeles del framebuffer.
    protected float orbita = 0.6f; // Ángulo inicial de la cámara alrededor de la ciudad, en radianes.
    protected boolean vistaMapa = false; // Clase4 lo activa cuando dibuja la vista superior.
    // protected static final float LIMITE = 55; // Distancia del origen a cada borde: el mapa mide 70 unidades = 35.
    protected static final float CELDA = 10; // Ancho y profundidad de cada celda del mapa.
    private final Map<String, Integer> uniforms = new HashMap<>(); // Evita buscar repetidamente el mismo uniform.

    protected static final int[][] MAPA = { // Matriz: 0 = calle, 1 = edificio, 2 = parque.
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Fila norte: calle continua.
        {0, 1, 0, 2, 0, 1, 0, 2, 0, 1, 0}, // Primera fila de manzanas, separadas por calles.
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Segunda avenida horizontal.
        {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0}, 
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // Tercera avenida horizontal.
        {0, 2, 0, 1, 0, 2, 0, 1, 0, 2, 0}, 
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 2, 0, 1, 0, 2, 0, 1, 0}, // Última fila de manzanas.
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0} // Calle del borde sur.
    };

    protected static float anchoMapa = MAPA[0].length * CELDA;
    protected static float profundidadMapa = MAPA.length * CELDA;
    protected static final float LIMITE = limiteMapa();

    // ==================== 2. INICIO, CICLO Y LIMPIEZA ====================

    /** Organiza las tres fases de la aplicación y garantiza la limpieza si ocurre un error. */
    public void run() {
        GLFWErrorCallback errores = GLFWErrorCallback.createPrint(System.err); // Muestra errores de GLFW en la consola.
        errores.set(); // Activa la captura de errores.
        try {
            iniciar(); // Inicializa GLFW, crea la ventana y compila los shaders.
            loop(); // Ciclo principal: dibuja la ciudad hasta que el usuario cierra la ventana.
        } finally {
            limpiar(); // Libera los recursos de OpenGL y destruye la ventana.
            glfwSetErrorCallback(null); // Desactiva la captura de errores.
            errores.free(); // Libera la memoria de la captura de errores.
        }
    }

    // Configura GLFW y OpenGL, igual que las clases de camara del proyecto original
    private void iniciar() {
        if (!glfwInit()) { // Inicializa GLFW; si falla, lanza excepción.
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }
        glfwDefaultWindowHints(); // Configura los valores por defecto de la ventana.
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3); // Solicita OpenGL 3.3.
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3); // Solicita OpenGL 3.3.
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE); // Perfil moderno de
        // glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE); // Necesario en MacOS.
        ventana = glfwCreateWindow(ancho, alto, "Ciudad 3D", 0, 0); // Crea la ventana.
        if (ventana == 0) { // GLFW devuelve 0 si no pudo crear la ventana; lanza excepción.
            throw new RuntimeException("No se pudo crear la ventana");
        }
        glfwMakeContextCurrent(ventana); // Hace que la ventana sea el contexto actual de OpenGL.
        glfwSwapInterval(1); // Sincroniza la presentacion con la pantalla para reducir cortes de imagen.
        GL.createCapabilities(); // Carga las funciones de OpenGL.
        glEnable(GL_DEPTH_TEST); // Activa la prueba de profundidad para que OpenGL dibuje correctamente.
        glfwSetKeyCallback(ventana, (ventanaEvento, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                tecla(key);
            }
        }); // Termina el registro del callback de teclado.
        crearPrograma(); // Compila y une los shaders; guarda el identificador del programa.
        crearCubo(); // Crea el VAO y VBO para dibujar un cubo unitario.
        
    }

    // Mantiene el ciclo continuo de eventos, actualizacion, dibujo y presentacion de la ventana.
    private void loop() {
        double tiempoAnterior = glfwGetTime(); // Guarda el instante inicial para calcular el tiempo entre cuadros.
        int cuadros = 0; // Contador de cuadros dibujados en un segundo.
        int maxCuadros = Integer.getInteger("demo.frames", 0); // Cero permite jugar; otro valor limita el arranque de prueba.
        int[] anchoReal = new int[1]; // Reserva espacio para que GLFW escriba el ancho en pixeles.
        int[] altoReal = new int[1]; // Reserva espacio para que GLFW escriba el alto en pixeles.

        while (!glfwWindowShouldClose(ventana)) { // Repite mientras no se haya solicitado cerrar.
            glfwPollEvents(); // Atiende los eventos de teclado, ratón y ventana.
            double tiempoActual = glfwGetTime(); // Obtiene el instante actual en segundos.
            float deltaTime = (float) Math.min(tiempoActual - tiempoAnterior, 0.05); // Evita saltos de mas de 50 ms.
            tiempoAnterior = tiempoActual; // Actualiza el instante anterior para el siguiente cuadro.
            actualizar(deltaTime); // Actualiza la camara o conduccion segun la etapa ejecutada.
            glfwGetFramebufferSize(ventana, anchoReal, altoReal); // Obtiene el tamaño real del framebuffer(pixeles).
            ancho = anchoReal[0]; // Copia el ancho actual al estado de la aplicacion.
            alto = altoReal[0]; // Copia el alto actual al estado de la aplicacion.
            if (ancho > 0 && alto > 0) { // Evita dibujar y dividir por cero si la ventana esta minimizada.
                dibujarFrame(); // Genera la imagen de la ciudad para este cuadro.
            }
            glfwSwapBuffers(ventana); // Presenta la imagen terminada en la ventana.
            cuadros++; // Registra que se completo una vuelta del ciclo.
            if (maxCuadros > 0 && cuadros >= maxCuadros) { // Detecta el final de un arranque automatico limitado.
                glfwSetWindowShouldClose(ventana, true); // Cierra la ventana si se alcanzó el límite de cuadros.
            }
        }
    }

    // Libera unicamente los recursos que llegan a crearse.
    private void limpiar() {
        if (programa != 0) { // Comprueba si existe un programa en la GPU.
            glDeleteProgram(programa); // Libera el programa de shaders.
        }
        if (vbo != 0) { // Comprueba si se reservo el buffer de vertices.
            glDeleteBuffers(vbo); // Libera los datos de geometria de la GPU.
        }
        if (vao != 0) { // Comprueba si se creo la descripcion de atributos.
            glDeleteVertexArrays(vao); // Libera la descripcion de los vertices de la GPU.
        }
        if (ventana != 0) { // Evita destruir una ventana que no pudo crearse.
            Callbacks.glfwFreeCallbacks(ventana); // Libera los callbacks del teclado registrado.
            glfwDestroyWindow(ventana); // Destruye la ventana y su contexto de OpenGL.
        }
        glfwTerminate(); // Finaliza y libera los recursos de GLFW.
    }

    // ==================== 3. TECLADO Y CÁMARA ====================

    /** Atiende las acciones de una sola pulsación. */
    protected void tecla(int key) {
        if (key == GLFW_KEY_ESCAPE) { // Comprueba si se presiono ESC.
            glfwSetWindowShouldClose(ventana, true); // Cierra la ventana si se presiona ESC.
        }
    }

    // Consulta si una tecla sigue presionada durante el cuadro actual.
    protected boolean pulsada(int key) {
        return glfwGetKey(ventana, key) == GLFW_PRESS; // Devuelve true si la tecla sigue presionada.
    }

    // Mueve la camara alrededor de la ciudad con las flechas horizontales.
    protected void actualizar(float deltaTime) {
        if (pulsada(GLFW_KEY_LEFT)) { // Comprueba si se mantiene presionada la flecha izquierda.
            orbita -= deltaTime; // Reduce el angulo a razon de un radian por segundo.
        }
        if (pulsada(GLFW_KEY_RIGHT)) { // Comprueba si se mantiene presionada la flecha derecha.
            orbita += deltaTime; // Aumenta el angulo a razon de un radian por segundo.
        }
    }

    // Coloca la camara elevada y orientada hacia el centro de la ciudad.
    protected void configurarCamara() {
        
        float dimension = Math.max(anchoMapa, profundidadMapa); // Obtenemos la dimensión mayor de la ciudad.
        // Calcula la distancia y altura según el tamaño del mapa.
        float radioCamara = dimension * 0.85f;
        float alturaCamara = dimension * 0.60f;

        // Mantiene la órbita alrededor del centro.
        float camaraX = (float) Math.sin(orbita) * radioCamara; // Calcula la posicion X de una orbita de radioCamara.
        float camaraZ = (float) Math.cos(orbita) * radioCamara; // Calcula la posicion Z de una orbita de radioCamara.

        vector("uOjo", camaraX, alturaCamara, camaraZ); // Envia la posicion de la camara a unidadea de alturaCamara.
        vector("uObjetivo", 0, 0, 0); // Apunta la camara hacia el origen del mundo.
        decimal("uAspecto", (float) ancho / alto); // Envia la proporcion de la imagen para evitar deformaciones.
    }

    // Reserva el punto en el que clase3 enviara las luces a la GPU.
    protected void prepararLuces() {
        // No necesita instrucciones en clase1: el shader inicial utiliza colores planos.
    }

    private static float limiteMapa() {
        return Math.max(anchoMapa, profundidadMapa) / 2.0f;
    }

    // ==================== 4. CIUDAD A PARTIR DE UNA MATRIZ ====================

    /** Convierte el índice de una fila o columna al centro de su celda. */
    protected static float centro(int indice) {
        return -LIMITE + CELDA * (indice + 0.5f); // Parte del borde negativo y avanza hasta la mitad de la celda.
    }

    // Prepara OpenGL para dibujar la imagen completa.
    protected void dibujarFrame() {
        glViewport(0, 0, ancho, alto); // Utiliza toda el area de la ventana para la escena principal.
        glClearColor(0.12f, 0.20f, 0.30f, 1.0f); // Define un fondo azul oscuro completamente opaco.
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Borra la imagen y las distancias del cuadro anterior.
        glUseProgram(programa); // Activa los shaders de esta etapa.
        glBindVertexArray(vao); // Selecciona los atributos del cubo compartido.
        entero("uMapa", 0); // Seleccciona perspectiva normal, no la proyeccion del minimapa.
        configurarCamara(); // Actualiza la posicion y el objetivo de la camara.
        prepararLuces(); // Envia iluminacion si la etapa actual la implementa.
        escena(); // Dibuja la ciudad y las ampliaciones de la leccion actual.
        
        int error = glGetError();
        if (error != GL_NO_ERROR) { // Comprueba si OpenGL reporto una operacion invalida.
            throw new IllegalStateException("Error"); // Hace visible el problema en consola.
        }
    }

    // Recorre el mapa y transforma cada celda en geometria.
    protected void escena() {
        // caja(20, -0.25f, 20, 110, 0.5f, 110, 0.16f, 0.19f, 0.23f); // Dibuja la base de asfalto con su cara superior en Y=0.
        caja(0, -0.25f, 0, anchoMapa, 0.5f, profundidadMapa, 0.16f, 0.19f, 0.23f); // Dibuja la base de asfalto con su cara superior en Y=0.
        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0; columna < MAPA[fila].length; columna++) {
                float x = centro(columna);
                float z = centro(fila);
                int tipo = MAPA[fila][columna]; // Lee el valor de la celda: 0 = calle, 1 = edificio, 2 = parque.
                if (tipo == 0) {
                    dibujarMarcasCalle(fila, columna, x, z);
                } else {
                    caja(x, 0.15f, z, 10, 0.3f, 10, 0.60f, 0.64f, 0.66f );
                    if (tipo == 1) {
                        float altura = 5 + (fila * 3 + columna * 7) %9;
                        float rojo = 0.28f + columna * 0.045f;
                        float azul = 0.48f + fila *0.025f;
                        caja(x, altura / 2 + 0.3f, z, 7, altura, 7, rojo, 0.40f, azul);
                        caja(x, altura + 0.45f, z, 7.3f, 0.3f, 7.3f, 0.20f, 0.26f, 0.32f);                      
                    } else {
                        caja(x, 0.32f, z, 9, 0.1f, 9, 0.20f, 0.45f, 0.28f);
                    }
                }
            }
        }
    }

    // Dibuja las lineas discontinuas de las calles dejando los cruces despejados.
    private void dibujarMarcasCalle(int fila, int columna, float x, float z) {
        if (fila % 2 == 0 && columna % 2 == 1) { // Identifica un tramo horizontal situado entre cruces.
            for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento += 3) { // Coloca tres marcas en la celda.
                caja(x + desplazamiento, 0.025f, z, 1.6f, 0.03f, 0.13f, 1, 0.84f, 0.35f); // Dibuja una línea alargada en X.
            }
        }
        if (columna % 2 == 0 && fila % 2 == 1) { // Identifica un tramo vertical situado entre cruces.
            for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento += 3) { // Repite las marcas sobre ese tramo.
                caja(x, 0.025f, z + desplazamiento, 0.13f, 0.03f, 1.6f, 1, 0.84f, 0.35f); // Dibuja una línea alargada en Z.
            }
        }
    }

    // ==================== 5. DIBUJAR CAJAS Y ENVIAR UNIFORMS ====================

    /** Dibuja una caja sin giro: posición XYZ, tamaño XYZ y color RGB entre 0 y 1. */
    protected void caja(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
        cajaGirada(x, y, z, sx, sy, sz, r, g, b, 0); // Reutiliza el dibujo general con un ángulo de cero.
    }

    /** Dibuja el cubo unitario con escala, giro alrededor de Y, posición y color. */
    protected void cajaGirada(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b, float angulo) {
        vector("uPos", x, y, z); // Envía la posición del centro de la caja en el mundo.
        vector("uEscala", sx, sy, sz); // Envía el ancho, alto y profundidad de la caja.
        vector("uColor", r, g, b); // Envía las intensidades roja, verde y azul del material.
        decimal("uGiro", angulo); // Envía la orientación en radianes.
        glDrawArrays(GL_TRIANGLES, 0, 36); // Dibuja 12 triángulos: dos por cada una de las seis caras.
    }

    /** Busca un uniform una vez y guarda su ubicación para los siguientes dibujos. */
    protected int uniform(String nombre) {
        if (!uniforms.containsKey(nombre)) { // Revisa si esta variable todavía no fue localizada.
            int ubicacion = glGetUniformLocation(programa, nombre); // Pregunta a OpenGL por la dirección del uniform.
            uniforms.put(nombre, ubicacion); // Guarda la respuesta; -1 significa que el shader no usa esa variable.
        }
        return uniforms.get(nombre); // Recupera la ubicación guardada; OpenGL ignora envíos a -1.
    }

    /** Envía tres números reales a una variable vec3 del shader. */
    protected void vector(String nombre, float x, float y, float z) {
        glUniform3f(uniform(nombre), x, y, z); // Escribe las tres componentes en el programa activo.
    }

    /** Envía un número real a una variable float del shader. */
    protected void decimal(String nombre, float valor) {
        glUniform1f(uniform(nombre), valor); // Actualiza el escalar identificado por su nombre.
    }

    /** Envía un número entero a una variable int del shader. */
    protected void entero(String nombre, int valor) {
        glUniform1i(uniform(nombre), valor); // Actualiza un entero, usado también para interruptores 0/1.
    }

    // ==================== 6. SHADERS: CÓDIGO QUE EJECUTA LA GPU ====================

    /** Transforma vértices del cubo al mundo y después a la pantalla. */
    private String vertexShader() {
        return """
            #version 330 core 
            // Selecciona GLSL 3.30 para el contexto OpenGL 3.3.
            layout (location = 0) in vec3 aPos; // Lee la posición local del vértice desde el VBO.
            layout (location = 1) in vec3 aNormal; // Lee la normal de la cara desde el mismo VBO.
            uniform vec3 uPos; // Recibe el centro de la caja en la ciudad.
            uniform vec3 uEscala; // Recibe el tamaño de la caja en cada eje.
            uniform vec3 uOjo; // Recibe la posición de la cámara.
            uniform vec3 uObjetivo; // Recibe el punto que observa la cámara.
            uniform float uGiro; // Recibe el giro del objeto alrededor de Y.
            uniform float uAspecto; // Recibe la relación ancho/alto de la imagen.
            uniform int uMapa; // Selecciona perspectiva (0) o vista superior ortográfica (1).
            out vec3 vMundo; // Envía la posición mundial al shader de fragmentos.
            out vec3 vNormal; // Envía la normal transformada para la iluminación de clase3.

            void main() { // OpenGL ejecuta este bloque una vez por vértice.
                float coseno = cos(uGiro); // Calcula el coseno del giro del objeto.
                float seno = sin(uGiro); // Calcula el seno del mismo giro.
                mat3 giro = mat3( // Construye la matriz de rotación; GLSL recibe sus columnas.
                    coseno, 0.0, -seno, // Primera columna: dirección del eje X rotado.
                    0.0, 1.0, 0.0, // Segunda columna: Y permanece vertical.
                    seno, 0.0, coseno // Tercera columna: dirección del eje Z rotado.
                ); // Completa la matriz de tres filas y tres columnas.
                vMundo = giro * (aPos * uEscala) + uPos; // Escala, gira y traslada el vértice al mundo.
                vNormal = normalize(giro * (aNormal / uEscala)); // Corrige la normal con la inversa transpuesta de escala y giro.

                if (uMapa == 1) { // Esta rama se usa al dibujar el minimapa en clase4.
                    float pantallaX = vMundo.x / 60.0; // Ajusta el ancho del mundo al intervalo visible -1 a 1.
                    float pantallaY = -vMundo.z / 60.0; // Coloca el norte (-Z) en la parte superior del mapa.
                    float profundidad = -vMundo.y / 100.0; // Hace que los objetos más altos se vean por encima.
                    gl_Position = vec4(pantallaX, pantallaY, profundidad, 1.0); // Proyecta sin reducir objetos lejanos.
                } else { // La escena principal utiliza una cámara con perspectiva.
                    vec3 frente = normalize(uObjetivo - uOjo); // Calcula la dirección hacia la que mira la cámara.
                    vec3 derecha = normalize(cross(frente, vec3(0.0, 1.0, 0.0))); // Obtiene el eje horizontal de la cámara.
                    vec3 arriba = cross(derecha, frente); // Obtiene el eje vertical perpendicular a los otros dos.
                    vec3 diferencia = vMundo - uOjo; // Traslada el origen del mundo hasta la cámara.
                    float vistaX = dot(diferencia, derecha); // Mide cuánto está el vértice a la derecha de la cámara.
                    float vistaY = dot(diferencia, arriba); // Mide cuánto está el vértice encima de la cámara.
                    float vistaZ = -dot(diferencia, frente); // Usa Z negativa delante de la cámara, como espera OpenGL.
                    float factor = 1.0 / tan(radians(55.0) * 0.5); // Convierte el campo visual de 55 grados en escala de perspectiva.
                    float cerca = 0.1; // Define la distancia mínima visible.
                    float lejos = 250.0; // Define la distancia máxima visible.
                    float clipX = vistaX * factor / uAspecto; // Corrige la coordenada horizontal según el ancho de la pantalla.
                    float clipY = vistaY * factor; // Aplica el campo visual a la coordenada vertical.
                    float clipZ = (lejos + cerca) / (cerca - lejos) * vistaZ; // Calcula el término de profundidad dependiente de Z.
                    clipZ += 2.0 * lejos * cerca / (cerca - lejos); // Añade el término constante de profundidad.
                    gl_Position = vec4(clipX, clipY, clipZ, -vistaZ); // OpenGL dividirá XYZ por W para producir perspectiva.
                }
            }
            """; // Termina la cadena que contiene el shader de vértices.
    }

    /** Define el color plano inicial; clase3 reemplaza este shader por la iluminación. */
    protected String fragmentShader() {
        return """
            #version 330 core 
            // Indica la versión del lenguaje del shader.
            uniform vec3 uColor; // Recibe el color RGB enviado por cajaGirada().
            out vec4 color; // Define el color final que se escribe en la imagen.
            void main() { // Se ejecuta para cada fragmento de la geometría dibujada.
                color = vec4(uColor, 1.0); // Copia el material y establece opacidad completa.
            }
            """; // Termina la cadena del shader de fragmentos.
    }

    /** Compila un shader y comunica el mensaje del controlador si hay un error GLSL. */
    private int compilar(int tipo, String fuente) {
        int shader = glCreateShader(tipo); // Reserva un shader del tipo vértice o fragmento.
        glShaderSource(shader, fuente); // Entrega a OpenGL el texto GLSL.
        glCompileShader(shader); // Solicita la compilación para esta GPU.
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) { // Consulta si la compilación falló.
            String error = glGetShaderInfoLog(shader); // Recupera el detalle del error antes de borrar el objeto.
            glDeleteShader(shader); // Libera el shader que no pudo compilarse.
            throw new IllegalStateException(error); // Detiene la aplicación con el mensaje de diagnóstico.
        }
        return shader; // Devuelve el identificador del shader compilado.
    }

    /** Une los shaders en un programa ejecutable y libera los objetos intermedios. */
    private void crearPrograma() {
        int shaderVertices = compilar(GL_VERTEX_SHADER, vertexShader()); // Compila las transformaciones geométricas.
        int shaderFragmentos = 0; // Permite saber si el segundo shader llegó a crearse.
        try { // Garantiza que ambos objetos intermedios se liberen incluso ante un fallo.
            shaderFragmentos = compilar(GL_FRAGMENT_SHADER, fragmentShader()); // Compila el color de la etapa actual.
            programa = glCreateProgram(); // Crea el contenedor que enlazará ambos shaders.
            glAttachShader(programa, shaderVertices); // Adjunta el shader que calcula posiciones.
            glAttachShader(programa, shaderFragmentos); // Adjunta el shader que calcula colores.
            glLinkProgram(programa); // Enlaza las entradas y salidas de ambos shaders.
            if (glGetProgrami(programa, GL_LINK_STATUS) == GL_FALSE) { // Comprueba que el enlace haya sido válido.
                throw new IllegalStateException(glGetProgramInfoLog(programa)); // Muestra incompatibilidades del programa.
            }
        } finally { // Ya no hacen falta objetos shader separados después del enlace.
            glDeleteShader(shaderVertices); // Libera el objeto del shader de vértices.
            if (shaderFragmentos != 0) { // Comprueba que el segundo objeto exista.
                glDeleteShader(shaderFragmentos); // Libera el objeto del shader de fragmentos.
            }
        }
    }

    // ==================== 7. GEOMETRÍA DEL CUBO: VAO Y VBO ====================

    /** Crea un cubo explícito, con posición XYZ y normal XYZ por vértice, como en los ejemplos. */
    private void crearCubo() {
        float[] vertices = { // Cada línea describe un vértice: X, Y, Z, normalX, normalY, normalZ.
            -0.5f, -0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina inferior izquierda, normal hacia +Z.
            0.5f, -0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina inferior derecha.
            0.5f, 0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina superior derecha; termina el primer triángulo.
            0.5f, 0.5f, 0.5f, 0, 0, 1, // Cara frontal: repite la esquina para el segundo triángulo.
            -0.5f, 0.5f, 0.5f, 0, 0, 1, // Cara frontal: esquina superior izquierda.
            -0.5f, -0.5f, 0.5f, 0, 0, 1, // Cara frontal: cierra el segundo triángulo.

            -0.5f, -0.5f, -0.5f, 0, 0, -1, // Cara trasera: primer vértice, normal hacia -Z.
            -0.5f, 0.5f, -0.5f, 0, 0, -1, // Cara trasera: segundo vértice.
            0.5f, 0.5f, -0.5f, 0, 0, -1, // Cara trasera: termina el primer triángulo.
            0.5f, 0.5f, -0.5f, 0, 0, -1, // Cara trasera: inicia el segundo triángulo.
            0.5f, -0.5f, -0.5f, 0, 0, -1, // Cara trasera: esquina inferior derecha.
            -0.5f, -0.5f, -0.5f, 0, 0, -1, // Cara trasera: cierra la cara.

            -0.5f, 0.5f, 0.5f, -1, 0, 0, // Cara izquierda: primer vértice, normal hacia -X.
            -0.5f, 0.5f, -0.5f, -1, 0, 0, // Cara izquierda: segundo vértice.
            -0.5f, -0.5f, -0.5f, -1, 0, 0, // Cara izquierda: termina el primer triángulo.
            -0.5f, -0.5f, -0.5f, -1, 0, 0, // Cara izquierda: inicia el segundo triángulo.
            -0.5f, -0.5f, 0.5f, -1, 0, 0, // Cara izquierda: esquina inferior frontal.
            -0.5f, 0.5f, 0.5f, -1, 0, 0, // Cara izquierda: cierra la cara.

            0.5f, 0.5f, 0.5f, 1, 0, 0, // Cara derecha: primer vértice, normal hacia +X.
            0.5f, -0.5f, 0.5f, 1, 0, 0, // Cara derecha: segundo vértice.
            0.5f, -0.5f, -0.5f, 1, 0, 0, // Cara derecha: termina el primer triángulo.
            0.5f, -0.5f, -0.5f, 1, 0, 0, // Cara derecha: inicia el segundo triángulo.
            0.5f, 0.5f, -0.5f, 1, 0, 0, // Cara derecha: esquina superior trasera.
            0.5f, 0.5f, 0.5f, 1, 0, 0, // Cara derecha: cierra la cara.

            -0.5f, 0.5f, -0.5f, 0, 1, 0, // Cara superior: primer vértice, normal hacia +Y.
            -0.5f, 0.5f, 0.5f, 0, 1, 0, // Cara superior: segundo vértice.
            0.5f, 0.5f, 0.5f, 0, 1, 0, // Cara superior: termina el primer triángulo.
            0.5f, 0.5f, 0.5f, 0, 1, 0, // Cara superior: inicia el segundo triángulo.
            0.5f, 0.5f, -0.5f, 0, 1, 0, // Cara superior: esquina trasera derecha.
            -0.5f, 0.5f, -0.5f, 0, 1, 0, // Cara superior: cierra la cara.

            -0.5f, -0.5f, -0.5f, 0, -1, 0, // Cara inferior: primer vértice, normal hacia -Y.
            0.5f, -0.5f, -0.5f, 0, -1, 0, // Cara inferior: segundo vértice.
            0.5f, -0.5f, 0.5f, 0, -1, 0, // Cara inferior: termina el primer triángulo.
            0.5f, -0.5f, 0.5f, 0, -1, 0, // Cara inferior: inicia el segundo triángulo.
            -0.5f, -0.5f, 0.5f, 0, -1, 0, // Cara inferior: esquina frontal izquierda.
            -0.5f, -0.5f, -0.5f, 0, -1, 0 // Cara inferior: cierra los 36 vértices del cubo.
        };
        vao = glGenVertexArrays(); // Reserva el objeto que recordará la disposición de los atributos.
        glBindVertexArray(vao); // Activa ese VAO para configurarlo.
        vbo = glGenBuffers(); // Reserva el buffer que almacenará los vértices.
        glBindBuffer(GL_ARRAY_BUFFER, vbo); // Selecciona el VBO como destino de datos de vértices.
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW); // Copia los datos a la GPU; el cubo no cambiará.
        int bytesPorVertice = 6 * Float.BYTES; // Cada vértice contiene seis float de cuatro bytes.
        glVertexAttribPointer(0, 3, GL_FLOAT, false, bytesPorVertice, 0L); // Describe XYZ desde el primer byte del vértice.
        glEnableVertexAttribArray(0); // Habilita la posición que recibe aPos.
        glVertexAttribPointer(1, 3, GL_FLOAT, false, bytesPorVertice, 3L * Float.BYTES); // Describe la normal después de XYZ.
        glEnableVertexAttribArray(1); // Habilita la normal que recibe aNormal.
        glBindBuffer(GL_ARRAY_BUFFER, 0); // Deja de seleccionar el VBO una vez configurado.
        glBindVertexArray(0); // Finaliza la configuración del VAO.
    }

    /** Punto de entrada para ejecutar la primera lección. */
    public static void main(String[] args) {
        clase1 aplicacion = new clase1(); // Crea la aplicación que muestra únicamente la ciudad.
        aplicacion.run(); // Inicia la ventana, el ciclo de dibujo y la limpieza final.
    }

}
