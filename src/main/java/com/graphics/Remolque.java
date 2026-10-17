
package com.graphics;

public class Remolque {

    private float remolqueX;
    private float remolqueZ;
    private float angulo;
    private boolean inicializado = false;

    private static final float DISTANCIA_ENGANCHE = 1.5f;

    // Distancia longitudinal desde el enganche hasta el eje.
    private static final float DISTANCIA_EJE = 2.8f; //3.0f;
    private static final float MAX_ARTICULACION = (float) Math.toRadians(35);

    public void actualizar(
        float autoX,
        float autoZ,
        float anguloAuto,
        float velocidad,
        float deltaTime) {

        float senoAuto = (float) Math.sin(anguloAuto);
        float cosenoAuto = (float) Math.cos(anguloAuto);

        // Punto de enganche detrás del vehículo.
        float engancheX = autoX + senoAuto * DISTANCIA_ENGANCHE;

        float engancheZ = autoZ + cosenoAuto * DISTANCIA_ENGANCHE;

        if (!inicializado) {
            angulo = anguloAuto;
            inicializado = true;
        } else {
            // Diferencia angular entre vehículo y remolque.
            float diferencia = anguloAuto - angulo;

            // Conservamos el modelo cinemático actual.
            float velocidadAngular = (velocidad / DISTANCIA_EJE) * (float) Math.sin(diferencia);
            angulo += velocidadAngular * deltaTime;
            // Evita que la chata se pliegue excesivamente.
            float articulacion = normalizarAngulo(angulo - anguloAuto);
            articulacion = Math.max(-MAX_ARTICULACION, Math.min(MAX_ARTICULACION, articulacion));
        }

        // Centro del remolque respecto al enganche.
        remolqueX = engancheX + (float) Math.sin(angulo) * DISTANCIA_EJE;
        remolqueZ = engancheZ + (float) Math.cos(angulo) * DISTANCIA_EJE;
    }

    public void dibujar(clase2 ciudad) {

        if (!inicializado) {
            return;
        }

        // Piso de madera.
        ciudad.pieza(remolqueX, remolqueZ, angulo,
                0, 0.72f, -0.55f,
                1.65f, 0.12f, 2.2f,
                0.48f, 0.27f, 0.12f
        );

        // Laterales de madera.
        for (float lado : new float[]{-1, 1}) {
            ciudad.pieza(
                    remolqueX, remolqueZ, angulo,
                    lado * 0.78f, 1.12f, -0.55f,
                    0.10f, 0.48f, 2.2f,
                    0.55f, 0.32f, 0.15f
            );

            // Refuerzos rojos del chasis.
            ciudad.pieza(
                    remolqueX, remolqueZ, angulo,
                    lado * 0.62f, 0.55f, -0.55f,
                    0.12f, 0.16f, 2.5f,
                    0.65f, 0.06f, 0.06f
            );

            // Ruedas.
            ciudad.pieza(
                    remolqueX, remolqueZ, angulo,
                    lado * 0.98f, 0.38f, 0,
                    0.24f, 0.58f, 0.62f,
                    0.06f, 0.06f, 0.07f
            );
        }

        // Pared delantera de la caja.
        ciudad.pieza(
                remolqueX, remolqueZ, angulo,
                0, 1.12f, -1.62f,
                1.55f, 0.48f, 0.10f,
                0.55f, 0.32f, 0.15f
        );

        // Pared trasera de la caja.
        ciudad.pieza(
                remolqueX, remolqueZ, angulo,
                0, 1.12f, 0.52f,
                1.55f, 0.48f, 0.10f,
                0.55f, 0.32f, 0.15f
        );

        // Lanza central que apunta hacia el auto.
        ciudad.pieza(
                remolqueX, remolqueZ, angulo,
                0, 0.58f, -2.15f,
                0.14f, 0.14f, 1.25f,
                0.65f, 0.06f, 0.06f
        );

        // Cabezal del enganche.
        ciudad.pieza(
                remolqueX, remolqueZ, angulo,
                0, 0.58f, -2.80f,
                0.24f, 0.16f, 0.22f,
                0.35f, 0.35f, 0.36f
        );
    }
    private float normalizarAngulo(float angulo) {
    while (angulo > Math.PI) {
        angulo -= (float) (2 * Math.PI);
    }

    while (angulo < -Math.PI) {
        angulo += (float) (2 * Math.PI);
    }

    return angulo;
}
}
