# TekkenTournaments

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=android&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-3ECF8E?style=flat&logo=supabase&logoColor=white)

TekkenTournaments es una aplicación nativa para Android desarrollada en Kotlin y Jetpack Compose. El proyecto nace con el objetivo fundamental de revitalizar la escena competitiva de la franquicia Tekken en su totalidad, proporcionando una infraestructura digital que facilite la organización de torneos tanto para las entregas más recientes como para los títulos clásicos, asegurando así la preservación de su jugabilidad y comunidad.

La plataforma ofrece una suite completa de herramientas para administradores de eventos y un sistema de progresión persistente para los jugadores.

---

## Descripción del Proyecto

El sistema ha sido diseñado bajo una arquitectura moderna para dispositivos móviles, priorizando la escalabilidad y la consistencia de datos en tiempo real. Utiliza Supabase como infraestructura de backend (BaaS), delegando la autenticación, base de datos y almacenamiento de archivos en servicios gestionados para garantizar la integridad y disponibilidad del servicio.

### Misión del Proyecto
El propósito central de TekkenTournaments es evitar que el legado competitivo de las entregas anteriores de Tekken caiga en el olvido. La aplicación permite centralizar la comunidad y ofrecer un estándar profesional para la competición en cualquier versión del juego.

### Funcionalidades Principales

#### Gestión Integral de Torneos
El módulo de administración permite a los organizadores configurar eventos adaptados a las necesidades de cada título de la saga:
* **Configuración de Eventos:** Creación de torneos con parámetros definidos como fecha, descripción y límites de participación.
* **Reglas de Enfrentamiento:** Soporte nativo para formatos competitivos estándar: Best of 3 (Bo3), Best of 5 (Bo5) y First to 5 (Ft5).
* **Generación de Brackets:** Algoritmo de emparejamiento automático y visualización de árbol de torneo interactivo.
* **Control de Flujo:** Gestión de estados del torneo (Abierto, En Curso, Finalizado) y actualización de resultados en tiempo real.

#### Sistema de Progresión de Usuario
Se ha implementado un sistema de gamificación denominado "Tekken Cards" para incentivar la participación continua:
* **Perfiles Evolutivos:** La interfaz del perfil de usuario cambia dinámicamente basándose en las estadísticas acumuladas en la base de datos.
* **Jerarquía de Rangos:** Clasificación automática de usuarios según número de victorias (Beginner, Warrior, Battle Ruler, Tekken Emperor, God of Destruction).
* **Identidad Digital:** Gestión de avatares personalizados mediante almacenamiento en la nube y campos de biografía editables.

#### Seguridad e Infraestructura
* **Autenticación:** Implementación de Supabase Auth para gestión de sesiones seguras y persistentes.
* **Seguridad a Nivel de Fila (RLS):** Políticas de base de datos PostgreSQL que restringen la escritura y edición de torneos exclusivamente a sus creadores.

---

## Especificaciones Técnicas

El desarrollo sigue los estándares y recomendaciones actuales para el desarrollo en Android.

### Stack Tecnológico
* **Lenguaje:** Kotlin 1.9+.
* **Interfaz de Usuario:** Jetpack Compose (Material Design 3).
* **Backend:** Supabase (PostgreSQL, Auth, Storage).
* **Cliente HTTP:** Ktor Client (Motor CIO/Android) para comunicación asíncrona.
* **Serialización:** Kotlinx Serialization.
* **Carga de Imágenes:** Coil.

### Arquitectura de Software
El proyecto implementa el patrón de diseño **MVVM (Model-View-ViewModel)** junto con el patrón **Repository** para asegurar la separación de responsabilidades:

1.  **Capa de UI (View):** Componentes composables reactivos que observan el estado.
2.  **Capa de Presentación (ViewModel/State):** Gestión del estado de la interfaz y lógica de presentación.
3.  **Capa de Datos (Repository):** Abstracción de las fuentes de datos (Supabase) y lógica de negocio.
4.  **Modelos de Datos:** Clases de datos inmutables (Data Classes) que reflejan el esquema de la base de datos.

---

## Desarrollo Futuro

La hoja de ruta del proyecto contempla la integración de tecnologías emergentes para automatizar y certificar los resultados:

* **Integración de Inteligencia Artificial:** Implementación de Google Gemini Flash para el análisis de imágenes, permitiendo la validación automática de resultados mediante capturas de pantalla del juego.
* **Tecnología Blockchain:** Desarrollo de un sistema de certificación de victorias mediante hashes criptográficos, proporcionando un historial de torneos inmutable y verificable.

---

## Licencia

Este proyecto se distribuye bajo la licencia MIT. Consulte el archivo `LICENSE` para obtener más información.
