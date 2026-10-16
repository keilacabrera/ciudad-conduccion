package com.graphics;

public class VehiculoAutonomo {
    
    private float x;
    private float z;
    private float velocidad;
    private float velocidadGiro = 2.0f; // Radianes por segundo.
    private float direccion;

    private final float[][] ruta;
    private int siguientePunto = 1;

    public VehiculoAutonomo(float[][] ruta, float velocidad) {
        if (ruta == null || ruta.length < 2) {
            throw new IllegalArgumentException(
                "La ruta debe contener al menos dos puntos."
            );
        }

        this.ruta = ruta;
        this.velocidad = velocidad;

        this.x = ruta[0][0];
        this.z = ruta[0][1];

        actualizarDireccion();
    }
    
    public void dibujar(clase2 ciudad) {
        ciudad.dibujarAutoEn(x, z, direccion);
    }
    
    public void actualizar(float deltaTime, clase4 ciudad) {

        if (deltaTime <= 0 || !Float.isFinite(deltaTime)) {
            return;
        }

        float objetivoX = ruta[siguientePunto][0];
        float objetivoZ = ruta[siguientePunto][1];

        float dx = objetivoX - x;
        float dz = objetivoZ - z;

        float distancia = (float) Math.sqrt(dx * dx + dz * dz);

        // Cuando llegamos cerca del punto, avanzamos al siguiente.
        if (distancia < 2.0f) {
            avanzarPunto();
            objetivoX = ruta[siguientePunto][0];
            objetivoZ = ruta[siguientePunto][1];

            dx = objetivoX - x;
            dz = objetivoZ - z;
        }

        // Orientación deseada hacia el siguiente punto.
        float objetivoDireccion = (float) Math.atan2(-dx, -dz);

        // Girar gradualmente hacia esa orientación.
        girarHacia(objetivoDireccion, deltaTime);

        // Avanzar en la dirección hacia la que apunta el vehículo.
        float avance = velocidad * deltaTime;

        float siguienteX = x - (float) Math.sin(direccion) * avance;
        float siguienteZ = z - (float) Math.cos(direccion) * avance;

        // Mantener la protección contra colisiones con el jugador.
        if (cercaDelJugador(siguienteX, siguienteZ, ciudad)) {
            return;
        }
        // Mantener la protección contra edificios y límites.
        if (ciudad.puedeCircularAutonomo(siguienteX, siguienteZ)) {
            x = siguienteX;
            z = siguienteZ;
        }
    }

    private void girarHacia(float objetivo, float deltaTime) {
        // Diferencia angular normalizada entre -PI y PI.
        float diferencia = (float) Math.atan2(Math.sin(objetivo - direccion), Math.cos(objetivo - direccion));
        // Limita el giro para evitar cambios bruscos.
        float giroMaximo = velocidadGiro * deltaTime;
        float giro = Math.max(-giroMaximo, Math.min(giroMaximo, diferencia));
        direccion += giro;
    }

    private boolean cercaDelJugador(float x, float z, clase2 ciudad) {
        float dx = x - ciudad.autoX;
        float dz = z - ciudad.autoZ;
        float distanciaCuadrada = dx * dx + dz * dz;
        // Distancia mínima entre los centros de ambos vehículos.
        float distanciaSegura = 3.0f;
        return distanciaCuadrada < distanciaSegura * distanciaSegura;
    }

    private void avanzarPunto() {
        siguientePunto = (siguientePunto + 1) % ruta.length;
        // actualizarDireccion();
    }

    private void actualizarDireccion() {
        float dx = ruta[siguientePunto][0] - x;
        float dz = ruta[siguientePunto][1] - z;
        // El frente del modelo apunta hacia Z negativo.
        direccion = (float) Math.atan2(-dx, -dz);
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public float getDireccion() {
        return direccion;
    }

    public float getVelocidad() {
        return velocidad;
    }

}
