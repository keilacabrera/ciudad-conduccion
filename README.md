# Ciudad 3D — Simulador de conducción

## Descripción

Ciudad 3D es un proyecto desarrollado en Java que permite explorar una ciudad tridimensional mediante un vehículo interactivo. El proyecto utiliza OpenGL para la representación gráfica de la ciudad y proporciona diferentes elementos que enriquecen la experiencia de conducción.

## Características principales

- Representación tridimensional de una ciudad.
- Vehículo controlable por el usuario.
- Sistema de movimiento y detección de colisiones.
- Cámara para visualizar el entorno.
- Iluminación diurna y nocturna.
- Farolas y luces del vehículo.
- Semáforos y pasos peatonales.
- Sistema de entregas con temporizador.
- Minimap para orientarse dentro de la ciudad.
- HUD (*Heads-Up Display*) con información de la conducción.

## Tecnologías utilizadas

- **Java 17:** lenguaje de programación.
- **Maven:** gestión de dependencias y compilación.
- **LWJGL 3.3.3:** acceso a bibliotecas gráficas y de entrada.
- **OpenGL:** renderizado de gráficos tridimensionales.
- **GLFW:** creación de ventanas y gestión de entrada del usuario.

## Requisitos

Para ejecutar el proyecto se necesita:

- Java Development Kit (JDK) 17.
- Apache Maven.
- Un sistema compatible con las bibliotecas nativas de LWJGL configuradas en el proyecto.
- Controladores gráficos compatibles con la versión de OpenGL utilizada.

## Instalación

1. Clonar el repositorio:

   ```bash
   git clone URL_DEL_REPOSITORIO
   ```

2. Entrar en la carpeta del proyecto:

   ```bash
   cd ciudad-conduccion
   ```

3. Compilar el proyecto:

   ```bash
   mvn clean compile
   ```

## Ejecución

Para ejecutar la aplicación mediante Maven:

```bash
mvn exec:exec
```

La ejecución requiere que la configuración de `exec-maven-plugin` esté correctamente establecida en el archivo `pom.xml`.

## Controles

Los controles permiten conducir el vehículo e interactuar con el entorno.

- **Movimiento del vehículo:** [teclas: AWSD].
- **Faros ON/OFF:** [tecla: F].
- **Freno:** [tecla: ESP].
- **Cámara Vehículo/Cámara Ciudad:** [tecla: C].

## Estructura del proyecto

```text
ciudad-conduccion/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/graphics/
│   │   └── resources/
├── pom.xml
├── .gitignore
└── README.md
```

La estructura mostrada es orientativa; debe ajustarse a la organización real de los archivos del proyecto.

## Estado del proyecto

Proyecto en desarrollo. Las funcionalidades y los componentes gráficos pueden evolucionar con futuras mejoras.
