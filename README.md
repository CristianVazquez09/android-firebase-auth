# Android Firebase Auth

Aplicación Android de ejemplo para autenticación y gestión de notas usando Firebase.

## Descripción

Esta app demuestra un flujo completo de autenticación con Firebase y manejo de datos en Firestore. Incluye:

- Registro e inicio de sesión por correo y contraseña.
- Inicio de sesión con Google Sign-In.
- Recuperación de contraseña por correo.
- Gestión de notas personales.
- Gestión de materias/categorías para notas.
- Recordatorios programados con notificaciones.
- Sesión segura basada en token con expiración de 3 horas.

## Funcionalidades principales

- Autenticación Firebase:
  - Registro de usuario con datos personales (nombre, apellido, teléfono, email).
  - Login con email/password.
  - Login con Google.
  - Reset de contraseña.
- Notas:
  - Crear, editar y eliminar notas.
  - Búsqueda de notas.
  - Filtro por notas favoritas.
  - Compartir notas.
  - Recordatorios con notificaciones push (requiere permiso POST_NOTIFICATIONS en Android 13+).
- Materias:
  - Agregar, editar y eliminar materias.
  - Activar/desactivar materias.
  - Elegir color e ícono para cada materia.
- Estructura de datos en Firestore:
  - `users/{uid}/notas`
  - `users/{uid}/materias`

## Tecnologías usadas

- Kotlin
- Android SDK (compileSdk 36, minSdk 26, targetSdk 36)
- Firebase Authentication
- Firebase Firestore
- Google Sign-In
- Jetpack ViewBinding
- Coroutines + LiveData
- EncryptedSharedPreferences
- Material Components

## Estructura del proyecto

- `app/src/main/java/com/wolfpack`
  - `data`: modelos, repositorios y manejo de sesión.
  - `notifications`: programación de recordatorios.
  - `ui`: actividades y vistas de login, registro, home, notas, materias y recuperación de contraseña.
  - `ui/base/BaseActivity.kt`: controla la sesión y la expiración.
- `app/build.gradle.kts`: dependencias y configuración de Android.
- `build.gradle.kts`: configuración del plugin de Gradle.

## Configuración necesaria

1. Abrir el proyecto en Android Studio.
2. Añadir `google-services.json` en `app/` desde la consola de Firebase.
3. Configurar la aplicación de Android en Firebase con el `applicationId` `com.wolfpack`.
4. Actualizar el `WEB_CLIENT_ID` en `app/src/main/java/com/wolfpack/ui/login/LoginActivity.kt` con el ID de cliente OAuth 2.0 de la app de Firebase.
5. Sincronizar Gradle y ejecutar el proyecto.

## Ejecución

- Ejecutar desde Android Studio con un emulador o dispositivo físico.
- Asegurarse de tener conexión a Internet para Firebase.

## Consideraciones

- El app usa `EncryptedSharedPreferences` para almacenar el token de sesión de Firebase.
- La sesión se considera válida hasta 3 horas después del último inicio de sesión.
- Las notas y materias se guardan por usuario en Firestore.
- Las notificaciones de recordatorio requieren permiso de notificaciones en Android 13+.

## Archivos clave

- `app/src/main/java/com/wolfpack/ui/login/LoginActivity.kt`
- `app/src/main/java/com/wolfpack/ui/register/RegisterActivity.kt`
- `app/src/main/java/com/wolfpack/ui/forgotpassword/ForgotPasswordActivity.kt`
- `app/src/main/java/com/wolfpack/ui/home/HomeActivity.kt`
- `app/src/main/java/com/wolfpack/ui/nota/NotaListActivity.kt`
- `app/src/main/java/com/wolfpack/ui/nota/NotaFormActivity.kt`
- `app/src/main/java/com/wolfpack/ui/materia/MateriaListActivity.kt`
- `app/src/main/java/com/wolfpack/ui/materia/MateriaFormActivity.kt`
- `app/src/main/java/com/wolfpack/data/repository/AuthRepository.kt`
- `app/src/main/java/com/wolfpack/data/repository/NotaRepository.kt`
- `app/src/main/java/com/wolfpack/data/repository/MateriaRepository.kt`

## Notas finales

Este proyecto es una buena base para una app de productividad o gestión de estudios, ya que combina autenticación, datos en la nube, categorización y recordatorios.
