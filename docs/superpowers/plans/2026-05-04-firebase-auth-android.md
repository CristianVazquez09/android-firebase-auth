
# Firebase Authentication Android — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Construir un sistema de autenticación completo en Android con Firebase que soporte registro, login, logout, recuperación de contraseña y Google Sign-In, con sesiones de 3 horas y detección automática de token expirado.

**Architecture:** MVVM (ViewModel + LiveData) con separación estricta en capas: UI (Activities + XML Layouts), ViewModel (lógica de presentación), Repository (Firebase calls), y TokenManager (sesión cifrada). El token de sesión se almacena en EncryptedSharedPreferences con timestamp de expiración a 3 horas; Firebase maneja el hash de contraseñas server-side con bcrypt.

**Tech Stack:** Kotlin, Firebase Authentication, Firebase Firestore, Google Sign-In SDK, EncryptedSharedPreferences (Jetpack Security), XML Layouts, Material Design 3, MVVM + LiveData

---

## Aclaración de responsabilidades: Firebase vs cliente

| Requisito | Quién lo maneja | Cómo |
|-----------|-----------------|------|
| Hash de contraseña | **Firebase (servidor)** | bcrypt automático — nunca viaja en claro |
| Token cifrado | **Cliente (Android)** | EncryptedSharedPreferences con AES-256 |
| Expiración 3 horas | **Cliente (Android)** | Timestamp guardado al login, verificado en cada pantalla |
| Alerta de sesión expirada | **Cliente (Android)** | Interceptor en BaseActivity |

---

## Lo que necesitas crear en Firebase Console ANTES de implementar

### Paso A — Crear el proyecto Firebase
1. Ve a [https://console.firebase.google.com](https://console.firebase.google.com)
2. Clic en **"Agregar proyecto"**
3. Nombre del proyecto: `PracticaFinal` (o el que prefieras)
4. Desactiva Google Analytics (opcional para este proyecto)
5. Clic en **"Crear proyecto"**

### Paso B — Agregar tu app Android
1. En la consola, clic en el ícono de **Android** (`</>`)
2. **Package name:** `com.wolfpack` (anota este — lo usarás en Android Studio)
3. Deja los demás campos vacíos por ahora
4. Clic en **"Registrar app"**
5. **Descarga `google-services.json`** — lo necesitarás en el Task 1

### Paso C — Habilitar Email/Password Authentication
1. En el menú izquierdo: **Authentication → Sign-in method**
2. Clic en **"Correo electrónico/contraseña"**
3. Activa el primer toggle (**Habilitar**)
4. Guarda

### Paso D — Habilitar Google Sign-In
1. En la misma pantalla: clic en **"Google"**
2. Activa el toggle
3. Pon un **nombre de proyecto público** (ej: "PracticaFinal Auth")
4. Selecciona tu correo como correo de soporte
5. Guarda
6. **Copia el "ID de cliente web"** — lo necesitas en el Task 7

### Paso E — Crear base de datos Firestore
1. Menú izquierdo: **Firestore Database**
2. Clic en **"Crear base de datos"**
3. Modo: **"Iniciar en modo de prueba"** (para desarrollo)
4. Región: `us-central1` (o la más cercana a ti)
5. Clic en **"Listo"**

### Paso F — Reglas de Firestore (copiar exactamente)
En Firestore → pestaña **Reglas**, pega:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
Publica las reglas.

**Entregables para la implementación:**
- [ ] `google-services.json` descargado
- [ ] ID de cliente web de Google Sign-In copiado
- [ ] Package name confirmado

---

## Estructura de archivos

```
app/
├── google-services.json                          ← descargado de Firebase
├── build.gradle (app)                            ← dependencias Firebase
├── src/main/
│   ├── java/com/wolfpack/
│   │   ├── data/
│   │   │   ├── model/
│   │   │   │   └── UserProfile.kt               ← data class con campos del usuario
│   │   │   ├── repository/
│   │   │   │   └── AuthRepository.kt            ← todas las llamadas a Firebase Auth + Firestore
│   │   │   └── local/
│   │   │       └── TokenManager.kt              ← EncryptedSharedPreferences + expiración 3h
│   │   ├── ui/
│   │   │   ├── base/
│   │   │   │   └── BaseActivity.kt              ← interceptor de sesión expirada
│   │   │   ├── login/
│   │   │   │   ├── LoginActivity.kt
│   │   │   │   └── LoginViewModel.kt
│   │   │   ├── register/
│   │   │   │   ├── RegisterActivity.kt
│   │   │   │   └── RegisterViewModel.kt
│   │   │   ├── forgotpassword/
│   │   │   │   ├── ForgotPasswordActivity.kt
│   │   │   │   └── ForgotPasswordViewModel.kt
│   │   │   └── home/
│   │   │       ├── HomeActivity.kt
│   │   │       └── HomeViewModel.kt
│   │   └── utils/
│   │       └── Extensions.kt                    ← helpers (validación email, etc.)
│   └── res/
│       ├── layout/
│       │   ├── activity_login.xml
│       │   ├── activity_register.xml
│       │   ├── activity_forgot_password.xml
│       │   └── activity_home.xml
│       ├── values/
│       │   ├── strings.xml
│       │   └── colors.xml
│       └── drawable/
│           └── bg_rounded_input.xml             ← fondo para campos de texto
```

---

## Task 1: Configuración del proyecto Android + Firebase

> ✅ **PARCIALMENTE COMPLETADO** — El proyecto ya existe con package `com.wolfpack`, minSdk 26 (Oreo), Java 11, Kotlin DSL. Los archivos Gradle ya fueron corregidos. Solo falta el Step 2 (google-services.json correcto).

**Files:**
- ✅ `build.gradle.kts` (raíz) — Kotlin plugin agregado
- ✅ `app/build.gradle.kts` — viewBinding, Firebase Auth, Firestore, ViewModel, Security Crypto, Coroutines agregados
- ✅ `gradle/libs.versions.toml` — versiones de Kotlin y dependencias agregadas
- ⚠️ `app/google-services.json` — requiere reemplazo (ver Step 2)

- [x] **Step 1: Proyecto Android creado** — ya existe en `C:\Users\malaf\...\PracticaFinal`

- [ ] **Step 2: Reemplazar google-services.json con el correcto**

  El archivo actual tiene package `com.wolpack` (sin 'f'). El app usa `com.wolfpack`.
  
  En Firebase Console:
  1. Configuración del proyecto → tu app Android → eliminar app `com.wolpack`
  2. Agregar nueva app Android con package `com.wolfpack`
  3. Descargar nuevo `google-services.json`
  4. Reemplazar `app/google-services.json` en el proyecto

- [ ] **Step 3: Habilitar Google Sign-In en Firebase Console**

  Authentication → Sign-in method → Google → Activar → Copiar **ID de cliente web**

- [ ] **Step 4: Sync Gradle en Android Studio**

  Clic en **"Sync Now"** después de cualquier cambio. Debe terminar sin errores.

- [ ] **Step 5: Configurar AndroidManifest.xml**

  Reemplaza el contenido de `app/src/main/AndroidManifest.xml`:

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <manifest xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:tools="http://schemas.android.com/tools">

      <uses-permission android:name="android.permission.INTERNET" />

      <application
          android:allowBackup="true"
          android:dataExtractionRules="@xml/data_extraction_rules"
          android:fullBackupContent="@xml/backup_rules"
          android:icon="@mipmap/ic_launcher"
          android:label="@string/app_name"
          android:roundIcon="@mipmap/ic_launcher_round"
          android:supportsRtl="true"
          android:theme="@style/Theme.PracticaFinal">

          <activity
              android:name=".ui.login.LoginActivity"
              android:exported="true">
              <intent-filter>
                  <action android:name="android.intent.action.MAIN" />
                  <category android:name="android.intent.category.LAUNCHER" />
              </intent-filter>
          </activity>

          <activity android:name=".ui.register.RegisterActivity" />
          <activity android:name=".ui.forgotpassword.ForgotPasswordActivity" />
          <activity android:name=".ui.home.HomeActivity" />

      </application>
  </manifest>
  ```

- [ ] **Step 6: Commit**

  ```bash
  git add .
  git commit -m "chore: setup Firebase dependencies and Gradle configuration"
  ```

---

## Task 2: Modelo de datos y repositorio Firebase

**Files:**
- Create: `app/src/main/java/com/wolfpack/data/model/UserProfile.kt`
- Create: `app/src/main/java/com/wolfpack/data/repository/AuthRepository.kt`
- Create: `app/src/test/java/com/wolfpack/data/repository/AuthRepositoryTest.kt`

- [ ] **Step 1: Escribir el test del modelo**

  Crea `app/src/test/java/com/wolfpack/data/model/UserProfileTest.kt`:

  ```kotlin
  package com.wolfpack.data.model

  import org.junit.Assert.*
  import org.junit.Test

  class UserProfileTest {

      @Test
      fun `UserProfile tiene todos los campos requeridos`() {
          val user = UserProfile(
              uid = "abc123",
              nombre = "Juan",
              apellido = "Pérez",
              telefono = "12345678",
              email = "juan@example.com"
          )
          assertEquals("abc123", user.uid)
          assertEquals("Juan", user.nombre)
          assertEquals("Pérez", user.apellido)
          assertEquals("12345678", user.telefono)
          assertEquals("juan@example.com", user.email)
      }

      @Test
      fun `UserProfile se puede convertir a Map para Firestore`() {
          val user = UserProfile(
              uid = "abc123",
              nombre = "Juan",
              apellido = "Pérez",
              telefono = "12345678",
              email = "juan@example.com"
          )
          val map = user.toMap()
          assertEquals("Juan", map["nombre"])
          assertEquals("Pérez", map["apellido"])
          assertEquals("12345678", map["telefono"])
          assertEquals("juan@example.com", map["email"])
      }
  }
  ```

- [ ] **Step 2: Ejecutar test para verificar que falla**

  En Android Studio: clic derecho sobre `UserProfileTest` → **Run**
  Resultado esperado: **ERROR** — `UserProfile` no existe aún

- [ ] **Step 3: Crear UserProfile**

  Crea `app/src/main/java/com/wolfpack/data/model/UserProfile.kt`:

  ```kotlin
  package com.wolfpack.data.model

  data class UserProfile(
      val uid: String = "",
      val nombre: String = "",
      val apellido: String = "",
      val telefono: String = "",
      val email: String = ""
  ) {
      fun toMap(): Map<String, Any> = mapOf(
          "nombre" to nombre,
          "apellido" to apellido,
          "telefono" to telefono,
          "email" to email
      )

      companion object {
          fun fromMap(uid: String, map: Map<String, Any>): UserProfile = UserProfile(
              uid = uid,
              nombre = map["nombre"] as? String ?: "",
              apellido = map["apellido"] as? String ?: "",
              telefono = map["telefono"] as? String ?: "",
              email = map["email"] as? String ?: ""
          )
      }
  }
  ```

- [ ] **Step 4: Ejecutar test para verificar que pasa**

  Resultado esperado: **PASS** (2 tests verdes)

- [ ] **Step 5: Crear AuthRepository**

  Crea `app/src/main/java/com/wolfpack/data/repository/AuthRepository.kt`:

  ```kotlin
  package com.wolfpack.data.repository

  import com.wolfpack.data.model.UserProfile
  import com.google.firebase.auth.FirebaseAuth
  import com.google.firebase.auth.FirebaseUser
  import com.google.firebase.auth.GoogleAuthProvider
  import com.google.firebase.firestore.FirebaseFirestore
  import kotlinx.coroutines.tasks.await

  class AuthRepository(
      private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
      private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
  ) {

      val currentUser: FirebaseUser? get() = auth.currentUser

      suspend fun registerWithEmail(
          email: String,
          password: String,
          profile: UserProfile
      ): Result<FirebaseUser> = runCatching {
          val result = auth.createUserWithEmailAndPassword(email, password).await()
          val user = result.user!!
          // Guardar perfil extendido en Firestore
          firestore.collection("users")
              .document(user.uid)
              .set(profile.copy(uid = user.uid).toMap())
              .await()
          user
      }

      suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> =
          runCatching {
              val result = auth.signInWithEmailAndPassword(email, password).await()
              result.user!!
          }

      suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> = runCatching {
          val credential = GoogleAuthProvider.getCredential(idToken, null)
          val result = auth.signInWithCredential(credential).await()
          val user = result.user!!
          // Si es nuevo usuario de Google, crear perfil base en Firestore
          if (result.additionalUserInfo?.isNewUser == true) {
              val profile = UserProfile(
                  uid = user.uid,
                  nombre = user.displayName?.split(" ")?.firstOrNull() ?: "",
                  apellido = user.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                  email = user.email ?: "",
                  telefono = user.phoneNumber ?: ""
              )
              firestore.collection("users")
                  .document(user.uid)
                  .set(profile.toMap())
                  .await()
          }
          user
      }

      suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
          auth.sendPasswordResetEmail(email).await()
      }

      fun logout() {
          auth.signOut()
      }

      suspend fun getUserProfile(uid: String): Result<UserProfile> = runCatching {
          val doc = firestore.collection("users").document(uid).get().await()
          UserProfile.fromMap(uid, doc.data ?: emptyMap())
      }
  }
  ```

- [ ] **Step 6: Commit**

  ```bash
  git add .
  git commit -m "feat: add UserProfile model and AuthRepository"
  ```

---

## Task 3: TokenManager — sesión cifrada y expiración de 3 horas

**Files:**
- Create: `app/src/main/java/com/wolfpack/data/local/TokenManager.kt`
- Create: `app/src/test/java/com/wolfpack/data/local/TokenManagerTest.kt`

- [ ] **Step 1: Escribir tests del TokenManager**

  Crea `app/src/test/java/com/wolfpack/data/local/TokenManagerTest.kt`:

  ```kotlin
  package com.wolfpack.data.local

  import org.junit.Assert.*
  import org.junit.Test

  class TokenManagerTest {

      @Test
      fun `sesion no ha expirado si loginTime es reciente`() {
          val loginTime = System.currentTimeMillis() - (1 * 60 * 60 * 1000L) // hace 1 hora
          assertFalse(TokenManager.isSessionExpired(loginTime))
      }

      @Test
      fun `sesion ha expirado si loginTime es hace mas de 3 horas`() {
          val loginTime = System.currentTimeMillis() - (4 * 60 * 60 * 1000L) // hace 4 horas
          assertTrue(TokenManager.isSessionExpired(loginTime))
      }

      @Test
      fun `sesion ha expirado si loginTime es exactamente 3 horas`() {
          val loginTime = System.currentTimeMillis() - (3 * 60 * 60 * 1000L)
          assertTrue(TokenManager.isSessionExpired(loginTime))
      }
  }
  ```

- [ ] **Step 2: Ejecutar tests para verificar que fallan**

  Resultado esperado: **ERROR** — `TokenManager` no existe aún

- [ ] **Step 3: Crear TokenManager**

  Crea `app/src/main/java/com/wolfpack/data/local/TokenManager.kt`:

  ```kotlin
  package com.wolfpack.data.local

  import android.content.Context
  import androidx.security.crypto.EncryptedSharedPreferences
  import androidx.security.crypto.MasterKey

  class TokenManager(context: Context) {

      companion object {
          private const val PREFS_NAME = "auth_secure_prefs"
          private const val KEY_TOKEN = "firebase_token"
          private const val KEY_LOGIN_TIME = "login_timestamp"
          private const val KEY_UID = "user_uid"
          private const val SESSION_DURATION_MS = 3 * 60 * 60 * 1000L // 3 horas

          fun isSessionExpired(loginTimeMs: Long): Boolean {
              return System.currentTimeMillis() - loginTimeMs >= SESSION_DURATION_MS
          }
      }

      private val masterKey = MasterKey.Builder(context)
          .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
          .build()

      private val prefs = EncryptedSharedPreferences.create(
          context,
          PREFS_NAME,
          masterKey,
          EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
          EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
      )

      fun saveSession(token: String, uid: String) {
          prefs.edit()
              .putString(KEY_TOKEN, token)
              .putString(KEY_UID, uid)
              .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
              .apply()
      }

      fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

      fun getUid(): String? = prefs.getString(KEY_UID, null)

      fun getLoginTime(): Long = prefs.getLong(KEY_LOGIN_TIME, 0L)

      fun isSessionValid(): Boolean {
          val token = getToken() ?: return false
          val loginTime = getLoginTime()
          return token.isNotEmpty() && !isSessionExpired(loginTime)
      }

      fun clearSession() {
          prefs.edit().clear().apply()
      }
  }
  ```

- [ ] **Step 4: Ejecutar tests para verificar que pasan**

  Resultado esperado: **PASS** (3 tests verdes)

- [ ] **Step 5: Commit**

  ```bash
  git add .
  git commit -m "feat: add TokenManager with AES-256 encrypted session and 3h expiry"
  ```

---

## Task 4: BaseActivity — interceptor de sesión expirada

**Files:**
- Create: `app/src/main/java/com/wolfpack/ui/base/BaseActivity.kt`

- [ ] **Step 1: Crear BaseActivity**

  Crea `app/src/main/java/com/wolfpack/ui/base/BaseActivity.kt`:

  ```kotlin
  package com.wolfpack.ui.base

  import android.content.Intent
  import android.os.Bundle
  import androidx.appcompat.app.AlertDialog
  import androidx.appcompat.app.AppCompatActivity
  import com.wolfpack.data.local.TokenManager
  import com.wolfpack.ui.login.LoginActivity

  abstract class BaseActivity : AppCompatActivity() {

      protected lateinit var tokenManager: TokenManager

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          tokenManager = TokenManager(this)
      }

      override fun onResume() {
          super.onResume()
          // Verificar expiración cada vez que la actividad vuelve al frente
          if (requiresAuth() && !tokenManager.isSessionValid()) {
              showSessionExpiredDialog()
          }
      }

      // Las Activities que necesitan auth sobreescriben esto devolviendo true
      open fun requiresAuth(): Boolean = true

      private fun showSessionExpiredDialog() {
          AlertDialog.Builder(this)
              .setTitle("Sesión expirada")
              .setMessage("Tu sesión ha expirado después de 3 horas. Por favor inicia sesión nuevamente.")
              .setCancelable(false)
              .setPositiveButton("Iniciar sesión") { _, _ ->
                  tokenManager.clearSession()
                  val intent = Intent(this, LoginActivity::class.java)
                  intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                  startActivity(intent)
                  finish()
              }
              .show()
      }
  }
  ```

- [ ] **Step 2: Commit**

  ```bash
  git add .
  git commit -m "feat: add BaseActivity with session expiry interceptor"
  ```

---

## Task 5: Recursos XML — colores, strings y fondo de inputs

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/drawable/bg_rounded_input.xml`
- Modify: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: Actualizar strings.xml**

  Reemplaza el contenido de `app/src/main/res/values/strings.xml`:

  ```xml
  <resources>
      <string name="app_name">PracticaFinal</string>
      <string name="label_nombre">Nombre</string>
      <string name="label_apellido">Apellido</string>
      <string name="label_telefono">Teléfono</string>
      <string name="label_email">Correo electrónico</string>
      <string name="label_password">Contraseña</string>
      <string name="btn_login">Iniciar sesión</string>
      <string name="btn_register">Crear cuenta</string>
      <string name="btn_google">Continuar con Google</string>
      <string name="btn_forgot">¿Olvidaste tu contraseña?</string>
      <string name="btn_logout">Cerrar sesión</string>
      <string name="link_go_register">¿No tienes cuenta? Regístrate</string>
      <string name="link_go_login">¿Ya tienes cuenta? Inicia sesión</string>
      <string name="msg_reset_sent">Revisa tu correo para restablecer tu contraseña</string>
      <string name="error_empty_fields">Por favor completa todos los campos</string>
      <string name="error_invalid_email">Correo electrónico inválido</string>
      <string name="error_password_short">La contraseña debe tener al menos 6 caracteres</string>
      <string name="error_phone_invalid">Número de teléfono inválido</string>
  </resources>
  ```

- [ ] **Step 2: Actualizar colors.xml**

  Reemplaza el contenido de `app/src/main/res/values/colors.xml`:

  ```xml
  <resources>
      <color name="colorPrimary">#1565C0</color>
      <color name="colorPrimaryDark">#0D47A1</color>
      <color name="colorAccent">#42A5F5</color>
      <color name="colorBackground">#F5F7FA</color>
      <color name="colorSurface">#FFFFFF</color>
      <color name="colorOnPrimary">#FFFFFF</color>
      <color name="colorTextPrimary">#212121</color>
      <color name="colorTextSecondary">#757575</color>
      <color name="colorInputBackground">#F0F4FF</color>
      <color name="colorDivider">#E0E0E0</color>
      <color name="colorError">#D32F2F</color>
      <color name="colorGoogleRed">#EA4335</color>
  </resources>
  ```

- [ ] **Step 3: Crear drawable para inputs redondeados**

  Crea `app/src/main/res/drawable/bg_rounded_input.xml`:

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <shape xmlns:android="http://schemas.android.com/apk/res/android"
      android:shape="rectangle">
      <solid android:color="@color/colorInputBackground" />
      <corners android:radius="12dp" />
      <stroke
          android:width="1dp"
          android:color="@color/colorDivider" />
  </shape>
  ```

- [ ] **Step 4: Actualizar tema en themes.xml**

  Abre `app/src/main/res/values/themes.xml` y reemplaza:

  ```xml
  <resources>
      <style name="Theme.PracticaFinal" parent="Theme.MaterialComponents.Light.NoActionBar">
          <item name="colorPrimary">@color/colorPrimary</item>
          <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
          <item name="colorAccent">@color/colorAccent</item>
          <item name="android:windowBackground">@color/colorBackground</item>
      </style>
  </resources>
  ```

- [ ] **Step 5: Commit**

  ```bash
  git add .
  git commit -m "feat: add XML resources (colors, strings, drawables, theme)"
  ```

---

## Task 6: Login — XML Layout + ViewModel + Activity

**Files:**
- Create: `app/src/main/res/layout/activity_login.xml`
- Create: `app/src/main/java/com/wolfpack/ui/login/LoginViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/login/LoginActivity.kt`
- Create: `app/src/test/java/com/wolfpack/ui/login/LoginViewModelTest.kt`

- [ ] **Step 1: Crear activity_login.xml**

  Crea `app/src/main/res/layout/activity_login.xml`:

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@color/colorBackground"
      android:fillViewport="true">

      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:orientation="vertical"
          android:padding="32dp"
          android:gravity="center">

          <!-- Logo / Título -->
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Bienvenido"
              android:textSize="28sp"
              android:textStyle="bold"
              android:textColor="@color/colorTextPrimary"
              android:layout_marginBottom="8dp" />

          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Inicia sesión para continuar"
              android:textSize="14sp"
              android:textColor="@color/colorTextSecondary"
              android:layout_marginBottom="40dp" />

          <!-- Email -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilEmail"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_email"
              app:boxBackgroundColor="@color/colorInputBackground"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="16dp">

              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etEmail"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="textEmailAddress"
                  android:imeOptions="actionNext" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- Contraseña -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilPassword"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_password"
              app:boxBackgroundColor="@color/colorInputBackground"
              app:passwordToggleEnabled="true"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="8dp">

              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etPassword"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="textPassword"
                  android:imeOptions="actionDone" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- ¿Olvidaste tu contraseña? -->
          <TextView
              android:id="@+id/tvForgotPassword"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="@string/btn_forgot"
              android:textColor="@color/colorPrimary"
              android:textSize="13sp"
              android:layout_gravity="end"
              android:layout_marginBottom="24dp"
              android:padding="4dp" />

          <!-- Botón Iniciar sesión -->
          <com.google.android.material.button.MaterialButton
              android:id="@+id/btnLogin"
              android:layout_width="match_parent"
              android:layout_height="56dp"
              android:text="@string/btn_login"
              android:textSize="16sp"
              app:cornerRadius="12dp"
              android:layout_marginBottom="16dp" />

          <!-- Divisor -->
          <LinearLayout
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:orientation="horizontal"
              android:gravity="center_vertical"
              android:layout_marginBottom="16dp">

              <View
                  android:layout_width="0dp"
                  android:layout_height="1dp"
                  android:layout_weight="1"
                  android:background="@color/colorDivider" />

              <TextView
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content"
                  android:text="  o  "
                  android:textColor="@color/colorTextSecondary" />

              <View
                  android:layout_width="0dp"
                  android:layout_height="1dp"
                  android:layout_weight="1"
                  android:background="@color/colorDivider" />
          </LinearLayout>

          <!-- Botón Google -->
          <com.google.android.material.button.MaterialButton
              android:id="@+id/btnGoogle"
              android:layout_width="match_parent"
              android:layout_height="56dp"
              android:text="@string/btn_google"
              android:textSize="14sp"
              app:cornerRadius="12dp"
              app:strokeWidth="1dp"
              app:strokeColor="@color/colorDivider"
              android:backgroundTint="@color/colorSurface"
              android:textColor="@color/colorTextPrimary"
              style="@style/Widget.MaterialComponents.Button.OutlinedButton"
              android:layout_marginBottom="32dp" />

          <!-- ProgressBar -->
          <ProgressBar
              android:id="@+id/progressBar"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:visibility="gone"
              android:layout_marginBottom="16dp" />

          <!-- Ir a registro -->
          <TextView
              android:id="@+id/tvGoToRegister"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="@string/link_go_register"
              android:textColor="@color/colorPrimary"
              android:textSize="14sp"
              android:padding="8dp" />

      </LinearLayout>
  </ScrollView>
  ```

- [ ] **Step 2: Escribir test del LoginViewModel**

  Crea `app/src/test/java/com/wolfpack/ui/login/LoginViewModelTest.kt`:

  ```kotlin
  package com.wolfpack.ui.login

  import com.wolfpack.data.repository.AuthRepository
  import kotlinx.coroutines.ExperimentalCoroutinesApi
  import kotlinx.coroutines.test.runTest
  import org.junit.Assert.*
  import org.junit.Before
  import org.junit.Test
  import org.mockito.Mockito.*

  @ExperimentalCoroutinesApi
  class LoginViewModelTest {

      private lateinit var mockRepo: AuthRepository
      private lateinit var viewModel: LoginViewModel

      @Before
      fun setup() {
          mockRepo = mock(AuthRepository::class.java)
          viewModel = LoginViewModel(mockRepo)
      }

      @Test
      fun `validateInputs devuelve false si email esta vacio`() {
          val result = viewModel.validateInputs("", "password123")
          assertFalse(result)
      }

      @Test
      fun `validateInputs devuelve false si password esta vacia`() {
          val result = viewModel.validateInputs("test@test.com", "")
          assertFalse(result)
      }

      @Test
      fun `validateInputs devuelve true con email y password validos`() {
          val result = viewModel.validateInputs("test@test.com", "password123")
          assertTrue(result)
      }
  }
  ```

- [ ] **Step 3: Ejecutar tests para verificar que fallan**

  Resultado esperado: **ERROR** — `LoginViewModel` no existe aún

- [ ] **Step 4: Crear LoginViewModel**

  Crea `app/src/main/java/com/wolfpack/ui/login/LoginViewModel.kt`:

  ```kotlin
  package com.wolfpack.ui.login

  import androidx.lifecycle.LiveData
  import androidx.lifecycle.MutableLiveData
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.wolfpack.data.repository.AuthRepository
  import com.google.firebase.auth.FirebaseUser
  import kotlinx.coroutines.launch

  class LoginViewModel(
      private val repo: AuthRepository = AuthRepository()
  ) : ViewModel() {

      private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
      val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

      private val _loading = MutableLiveData<Boolean>()
      val loading: LiveData<Boolean> = _loading

      fun validateInputs(email: String, password: String): Boolean {
          return email.isNotBlank() && password.isNotBlank()
      }

      fun login(email: String, password: String) {
          _loading.value = true
          viewModelScope.launch {
              val result = repo.loginWithEmail(email, password)
              _loginResult.value = result
              _loading.value = false
          }
      }

      fun loginWithGoogle(idToken: String) {
          _loading.value = true
          viewModelScope.launch {
              val result = repo.loginWithGoogle(idToken)
              _loginResult.value = result
              _loading.value = false
          }
      }
  }
  ```

- [ ] **Step 5: Ejecutar tests para verificar que pasan**

  Resultado esperado: **PASS** (3 tests verdes)

- [ ] **Step 6: Crear LoginActivity**

  Crea `app/src/main/java/com/wolfpack/ui/login/LoginActivity.kt`:

  ```kotlin
  package com.wolfpack.ui.login

  import android.content.Intent
  import android.os.Bundle
  import android.view.View
  import android.widget.Toast
  import androidx.activity.result.contract.ActivityResultContracts
  import androidx.activity.viewModels
  import com.wolfpack.R
  import com.wolfpack.databinding.ActivityLoginBinding
  import com.wolfpack.ui.base.BaseActivity
  import com.wolfpack.ui.forgotpassword.ForgotPasswordActivity
  import com.wolfpack.ui.home.HomeActivity
  import com.wolfpack.ui.register.RegisterActivity
  import com.google.android.gms.auth.api.signin.GoogleSignIn
  import com.google.android.gms.auth.api.signin.GoogleSignInOptions
  import com.google.android.gms.common.api.ApiException

  class LoginActivity : BaseActivity() {

      private lateinit var binding: ActivityLoginBinding
      private val viewModel: LoginViewModel by viewModels()

      // REEMPLAZA "TU_WEB_CLIENT_ID" con el ID de cliente web de Firebase Console
      private val WEB_CLIENT_ID = "TU_WEB_CLIENT_ID"

      private val googleSignInLauncher = registerForActivityResult(
          ActivityResultContracts.StartActivityForResult()
      ) { result ->
          val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
          try {
              val account = task.getResult(ApiException::class.java)
              account.idToken?.let { viewModel.loginWithGoogle(it) }
          } catch (e: ApiException) {
              Toast.makeText(this, "Google Sign-In falló: ${e.message}", Toast.LENGTH_SHORT).show()
          }
      }

      override fun requiresAuth(): Boolean = false // Login no necesita sesión activa

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)

          // Si ya hay sesión válida, ir directo a Home
          if (tokenManager.isSessionValid()) {
              goToHome()
              return
          }

          binding = ActivityLoginBinding.inflate(layoutInflater)
          setContentView(binding.root)

          setupObservers()
          setupListeners()
      }

      private fun setupObservers() {
          viewModel.loading.observe(this) { isLoading ->
              binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
              binding.btnLogin.isEnabled = !isLoading
              binding.btnGoogle.isEnabled = !isLoading
          }

          viewModel.loginResult.observe(this) { result ->
              result.onSuccess { user ->
                  user.getIdToken(false).addOnSuccessListener { tokenResult ->
                      tokenResult.token?.let { token ->
                          tokenManager.saveSession(token, user.uid)
                      }
                      goToHome()
                  }
              }
              result.onFailure { error ->
                  Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
              }
          }
      }

      private fun setupListeners() {
          binding.btnLogin.setOnClickListener {
              val email = binding.etEmail.text.toString().trim()
              val password = binding.etPassword.text.toString()
              if (viewModel.validateInputs(email, password)) {
                  viewModel.login(email, password)
              } else {
                  Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
              }
          }

          binding.btnGoogle.setOnClickListener { launchGoogleSignIn() }

          binding.tvForgotPassword.setOnClickListener {
              startActivity(Intent(this, ForgotPasswordActivity::class.java))
          }

          binding.tvGoToRegister.setOnClickListener {
              startActivity(Intent(this, RegisterActivity::class.java))
          }
      }

      private fun launchGoogleSignIn() {
          val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
              .requestIdToken(WEB_CLIENT_ID)
              .requestEmail()
              .build()
          val googleSignInClient = GoogleSignIn.getClient(this, gso)
          googleSignInLauncher.launch(googleSignInClient.signInIntent)
      }

      private fun goToHome() {
          startActivity(Intent(this, HomeActivity::class.java))
          finish()
      }
  }
  ```

- [ ] **Step 7: Commit**

  ```bash
  git add .
  git commit -m "feat: add Login screen with email and Google sign-in"
  ```

---

## Task 7: Registro — XML Layout + ViewModel + Activity

**Files:**
- Create: `app/src/main/res/layout/activity_register.xml`
- Create: `app/src/main/java/com/wolfpack/ui/register/RegisterViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/register/RegisterActivity.kt`
- Create: `app/src/test/java/com/wolfpack/ui/register/RegisterViewModelTest.kt`

- [ ] **Step 1: Crear activity_register.xml**

  Crea `app/src/main/res/layout/activity_register.xml`:

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:background="@color/colorBackground"
      android:fillViewport="true">

      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:orientation="vertical"
          android:padding="32dp">

          <!-- Título -->
          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Crear cuenta"
              android:textSize="26sp"
              android:textStyle="bold"
              android:textColor="@color/colorTextPrimary"
              android:layout_marginBottom="8dp" />

          <TextView
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="Completa tus datos para registrarte"
              android:textSize="14sp"
              android:textColor="@color/colorTextSecondary"
              android:layout_marginBottom="32dp" />

          <!-- Nombre -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilNombre"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_nombre"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="16dp">
              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etNombre"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="textPersonName"
                  android:imeOptions="actionNext" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- Apellido -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilApellido"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_apellido"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="16dp">
              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etApellido"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="textPersonName"
                  android:imeOptions="actionNext" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- Teléfono -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilTelefono"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_telefono"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="16dp">
              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etTelefono"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="phone"
                  android:imeOptions="actionNext" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- Email -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilEmail"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_email"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="16dp">
              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etEmail"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="textEmailAddress"
                  android:imeOptions="actionNext" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- Contraseña -->
          <com.google.android.material.textfield.TextInputLayout
              android:id="@+id/tilPassword"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:hint="@string/label_password"
              app:passwordToggleEnabled="true"
              style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
              android:layout_marginBottom="32dp">
              <com.google.android.material.textfield.TextInputEditText
                  android:id="@+id/etPassword"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:inputType="textPassword"
                  android:imeOptions="actionDone" />
          </com.google.android.material.textfield.TextInputLayout>

          <!-- Botón Registrar -->
          <com.google.android.material.button.MaterialButton
              android:id="@+id/btnRegister"
              android:layout_width="match_parent"
              android:layout_height="56dp"
              android:text="@string/btn_register"
              android:textSize="16sp"
              app:cornerRadius="12dp"
              android:layout_marginBottom="16dp" />

          <!-- ProgressBar -->
          <ProgressBar
              android:id="@+id/progressBar"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:visibility="gone"
              android:layout_gravity="center"
              android:layout_marginBottom="16dp" />

          <!-- Ir a login -->
          <TextView
              android:id="@+id/tvGoToLogin"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="@string/link_go_login"
              android:textColor="@color/colorPrimary"
              android:textSize="14sp"
              android:layout_gravity="center"
              android:padding="8dp" />

      </LinearLayout>
  </ScrollView>
  ```

- [ ] **Step 2: Escribir tests del RegisterViewModel**

  Crea `app/src/test/java/com/wolfpack/ui/register/RegisterViewModelTest.kt`:

  ```kotlin
  package com.wolfpack.ui.register

  import org.junit.Assert.*
  import org.junit.Before
  import org.junit.Test

  class RegisterViewModelTest {

      private lateinit var viewModel: RegisterViewModel

      @Before
      fun setup() {
          viewModel = RegisterViewModel()
      }

      @Test
      fun `validateInputs devuelve false si nombre esta vacio`() {
          val result = viewModel.validateInputs("", "Perez", "12345678", "a@a.com", "pass123")
          assertFalse(result)
      }

      @Test
      fun `validateInputs devuelve false si password tiene menos de 6 caracteres`() {
          val result = viewModel.validateInputs("Juan", "Perez", "12345678", "a@a.com", "12345")
          assertFalse(result)
      }

      @Test
      fun `validateInputs devuelve false si email es invalido`() {
          val result = viewModel.validateInputs("Juan", "Perez", "12345678", "noesemail", "pass123")
          assertFalse(result)
      }

      @Test
      fun `validateInputs devuelve true con todos los campos validos`() {
          val result = viewModel.validateInputs("Juan", "Perez", "12345678", "juan@test.com", "pass123")
          assertTrue(result)
      }
  }
  ```

- [ ] **Step 3: Ejecutar tests para verificar que fallan**

  Resultado esperado: **ERROR** — `RegisterViewModel` no existe aún

- [ ] **Step 4: Crear RegisterViewModel**

  Crea `app/src/main/java/com/wolfpack/ui/register/RegisterViewModel.kt`:

  ```kotlin
  package com.wolfpack.ui.register

  import androidx.lifecycle.LiveData
  import androidx.lifecycle.MutableLiveData
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.wolfpack.data.model.UserProfile
  import com.wolfpack.data.repository.AuthRepository
  import com.google.firebase.auth.FirebaseUser
  import kotlinx.coroutines.launch

  class RegisterViewModel(
      private val repo: AuthRepository = AuthRepository()
  ) : ViewModel() {

      private val _registerResult = MutableLiveData<Result<FirebaseUser>>()
      val registerResult: LiveData<Result<FirebaseUser>> = _registerResult

      private val _loading = MutableLiveData<Boolean>()
      val loading: LiveData<Boolean> = _loading

      fun validateInputs(
          nombre: String,
          apellido: String,
          telefono: String,
          email: String,
          password: String
      ): Boolean {
          if (nombre.isBlank() || apellido.isBlank() || telefono.isBlank()) return false
          if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return false
          if (password.length < 6) return false
          return true
      }

      fun register(nombre: String, apellido: String, telefono: String, email: String, password: String) {
          _loading.value = true
          viewModelScope.launch {
              val profile = UserProfile(
                  nombre = nombre,
                  apellido = apellido,
                  telefono = telefono,
                  email = email
              )
              val result = repo.registerWithEmail(email, password, profile)
              _registerResult.value = result
              _loading.value = false
          }
      }
  }
  ```

- [ ] **Step 5: Ejecutar tests para verificar que pasan**

  Resultado esperado: **PASS** (4 tests verdes)

- [ ] **Step 6: Crear RegisterActivity**

  Crea `app/src/main/java/com/wolfpack/ui/register/RegisterActivity.kt`:

  ```kotlin
  package com.wolfpack.ui.register

  import android.content.Intent
  import android.os.Bundle
  import android.view.View
  import android.widget.Toast
  import androidx.activity.viewModels
  import com.wolfpack.R
  import com.wolfpack.databinding.ActivityRegisterBinding
  import com.wolfpack.ui.base.BaseActivity
  import com.wolfpack.ui.home.HomeActivity

  class RegisterActivity : BaseActivity() {

      private lateinit var binding: ActivityRegisterBinding
      private val viewModel: RegisterViewModel by viewModels()

      override fun requiresAuth(): Boolean = false

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          binding = ActivityRegisterBinding.inflate(layoutInflater)
          setContentView(binding.root)

          setupObservers()
          setupListeners()
      }

      private fun setupObservers() {
          viewModel.loading.observe(this) { isLoading ->
              binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
              binding.btnRegister.isEnabled = !isLoading
          }

          viewModel.registerResult.observe(this) { result ->
              result.onSuccess { user ->
                  user.getIdToken(false).addOnSuccessListener { tokenResult ->
                      tokenResult.token?.let { token ->
                          tokenManager.saveSession(token, user.uid)
                      }
                      goToHome()
                  }
              }
              result.onFailure { error ->
                  Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
              }
          }
      }

      private fun setupListeners() {
          binding.btnRegister.setOnClickListener {
              val nombre = binding.etNombre.text.toString().trim()
              val apellido = binding.etApellido.text.toString().trim()
              val telefono = binding.etTelefono.text.toString().trim()
              val email = binding.etEmail.text.toString().trim()
              val password = binding.etPassword.text.toString()

              when {
                  !viewModel.validateInputs(nombre, apellido, telefono, email, password) -> {
                      Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                  }
                  password.length < 6 -> {
                      binding.tilPassword.error = getString(R.string.error_password_short)
                  }
                  else -> {
                      binding.tilPassword.error = null
                      viewModel.register(nombre, apellido, telefono, email, password)
                  }
              }
          }

          binding.tvGoToLogin.setOnClickListener { finish() }
      }

      private fun goToHome() {
          startActivity(Intent(this, HomeActivity::class.java))
          finishAffinity()
      }
  }
  ```

- [ ] **Step 7: Commit**

  ```bash
  git add .
  git commit -m "feat: add Register screen with full user profile fields"
  ```

---

## Task 8: Recuperación de contraseña — XML Layout + ViewModel + Activity

**Files:**
- Create: `app/src/main/res/layout/activity_forgot_password.xml`
- Create: `app/src/main/java/com/wolfpack/ui/forgotpassword/ForgotPasswordViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/forgotpassword/ForgotPasswordActivity.kt`

- [ ] **Step 1: Crear activity_forgot_password.xml**

  Crea `app/src/main/res/layout/activity_forgot_password.xml`:

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:orientation="vertical"
      android:padding="32dp"
      android:gravity="center"
      android:background="@color/colorBackground">

      <TextView
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:text="Recuperar contraseña"
          android:textSize="24sp"
          android:textStyle="bold"
          android:textColor="@color/colorTextPrimary"
          android:layout_marginBottom="8dp" />

      <TextView
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:text="Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña"
          android:textSize="14sp"
          android:textColor="@color/colorTextSecondary"
          android:gravity="center"
          android:layout_marginBottom="32dp" />

      <com.google.android.material.textfield.TextInputLayout
          android:id="@+id/tilEmail"
          android:layout_width="match_parent"
          android:layout_height="wrap_content"
          android:hint="@string/label_email"
          style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
          android:layout_marginBottom="24dp">
          <com.google.android.material.textfield.TextInputEditText
              android:id="@+id/etEmail"
              android:layout_width="match_parent"
              android:layout_height="wrap_content"
              android:inputType="textEmailAddress"
              android:imeOptions="actionDone" />
      </com.google.android.material.textfield.TextInputLayout>

      <com.google.android.material.button.MaterialButton
          android:id="@+id/btnReset"
          android:layout_width="match_parent"
          android:layout_height="56dp"
          android:text="Enviar enlace"
          android:textSize="16sp"
          app:cornerRadius="12dp"
          android:layout_marginBottom="16dp" />

      <ProgressBar
          android:id="@+id/progressBar"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:visibility="gone"
          android:layout_marginBottom="16dp" />

      <TextView
          android:id="@+id/tvBackToLogin"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:text="Volver al inicio de sesión"
          android:textColor="@color/colorPrimary"
          android:textSize="14sp"
          android:padding="8dp" />

  </LinearLayout>
  ```

- [ ] **Step 2: Crear ForgotPasswordViewModel**

  Crea `app/src/main/java/com/wolfpack/ui/forgotpassword/ForgotPasswordViewModel.kt`:

  ```kotlin
  package com.wolfpack.ui.forgotpassword

  import androidx.lifecycle.LiveData
  import androidx.lifecycle.MutableLiveData
  import androidx.lifecycle.ViewModel
  import androidx.lifecycle.viewModelScope
  import com.wolfpack.data.repository.AuthRepository
  import kotlinx.coroutines.launch

  class ForgotPasswordViewModel(
      private val repo: AuthRepository = AuthRepository()
  ) : ViewModel() {

      private val _resetResult = MutableLiveData<Result<Unit>>()
      val resetResult: LiveData<Result<Unit>> = _resetResult

      private val _loading = MutableLiveData<Boolean>()
      val loading: LiveData<Boolean> = _loading

      fun sendPasswordReset(email: String) {
          if (email.isBlank()) {
              _resetResult.value = Result.failure(Exception("El correo no puede estar vacío"))
              return
          }
          _loading.value = true
          viewModelScope.launch {
              _resetResult.value = repo.sendPasswordReset(email)
              _loading.value = false
          }
      }
  }
  ```

- [ ] **Step 3: Crear ForgotPasswordActivity**

  Crea `app/src/main/java/com/wolfpack/ui/forgotpassword/ForgotPasswordActivity.kt`:

  ```kotlin
  package com.wolfpack.ui.forgotpassword

  import android.os.Bundle
  import android.view.View
  import android.widget.Toast
  import androidx.activity.viewModels
  import com.wolfpack.R
  import com.wolfpack.databinding.ActivityForgotPasswordBinding
  import com.wolfpack.ui.base.BaseActivity

  class ForgotPasswordActivity : BaseActivity() {

      private lateinit var binding: ActivityForgotPasswordBinding
      private val viewModel: ForgotPasswordViewModel by viewModels()

      override fun requiresAuth(): Boolean = false

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
          setContentView(binding.root)

          setupObservers()
          setupListeners()
      }

      private fun setupObservers() {
          viewModel.loading.observe(this) { isLoading ->
              binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
              binding.btnReset.isEnabled = !isLoading
          }

          viewModel.resetResult.observe(this) { result ->
              result.onSuccess {
                  Toast.makeText(this, getString(R.string.msg_reset_sent), Toast.LENGTH_LONG).show()
                  finish()
              }
              result.onFailure { error ->
                  Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
              }
          }
      }

      private fun setupListeners() {
          binding.btnReset.setOnClickListener {
              viewModel.sendPasswordReset(binding.etEmail.text.toString().trim())
          }
          binding.tvBackToLogin.setOnClickListener { finish() }
      }
  }
  ```

- [ ] **Step 4: Commit**

  ```bash
  git add .
  git commit -m "feat: add ForgotPassword screen with Firebase email reset"
  ```

---

## Task 9: Home — XML Layout + ViewModel + Activity con logout

**Files:**
- Create: `app/src/main/res/layout/activity_home.xml`
- Create: `app/src/main/java/com/wolfpack/ui/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/wolfpack/ui/home/HomeActivity.kt`

- [ ] **Step 1: Crear activity_home.xml**

  Crea `app/src/main/res/layout/activity_home.xml`:

  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:app="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:orientation="vertical"
      android:background="@color/colorBackground">

      <!-- Toolbar -->
      <androidx.appcompat.widget.Toolbar
          android:id="@+id/toolbar"
          android:layout_width="match_parent"
          android:layout_height="?attr/actionBarSize"
          android:background="@color/colorPrimary"
          app:title="Inicio"
          app:titleTextColor="@color/colorOnPrimary" />

      <!-- Contenido centrado -->
      <LinearLayout
          android:layout_width="match_parent"
          android:layout_height="0dp"
          android:layout_weight="1"
          android:orientation="vertical"
          android:gravity="center"
          android:padding="32dp">

          <TextView
              android:id="@+id/tvWelcome"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:text="¡Bienvenido!"
              android:textSize="26sp"
              android:textStyle="bold"
              android:textColor="@color/colorTextPrimary"
              android:layout_marginBottom="8dp" />

          <TextView
              android:id="@+id/tvUserEmail"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:textSize="14sp"
              android:textColor="@color/colorTextSecondary"
              android:layout_marginBottom="8dp" />

          <TextView
              android:id="@+id/tvSessionInfo"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:textSize="12sp"
              android:textColor="@color/colorTextSecondary"
              android:layout_marginBottom="48dp" />

          <com.google.android.material.button.MaterialButton
              android:id="@+id/btnLogout"
              android:layout_width="match_parent"
              android:layout_height="56dp"
              android:text="@string/btn_logout"
              android:textSize="16sp"
              app:cornerRadius="12dp"
              app:backgroundTint="@color/colorError" />

      </LinearLayout>
  </LinearLayout>
  ```

- [ ] **Step 2: Crear HomeViewModel**

  Crea `app/src/main/java/com/wolfpack/ui/home/HomeViewModel.kt`:

  ```kotlin
  package com.wolfpack.ui.home

  import androidx.lifecycle.ViewModel
  import com.wolfpack.data.repository.AuthRepository

  class HomeViewModel(
      private val repo: AuthRepository = AuthRepository()
  ) : ViewModel() {

      fun getCurrentUserEmail(): String = repo.currentUser?.email ?: "Usuario"

      fun logout() {
          repo.logout()
      }
  }
  ```

- [ ] **Step 3: Crear HomeActivity**

  Crea `app/src/main/java/com/wolfpack/ui/home/HomeActivity.kt`:

  ```kotlin
  package com.wolfpack.ui.home

  import android.content.Intent
  import android.os.Bundle
  import androidx.activity.viewModels
  import com.wolfpack.databinding.ActivityHomeBinding
  import com.wolfpack.ui.base.BaseActivity
  import com.wolfpack.ui.login.LoginActivity
  import java.text.SimpleDateFormat
  import java.util.*

  class HomeActivity : BaseActivity() {

      private lateinit var binding: ActivityHomeBinding
      private val viewModel: HomeViewModel by viewModels()

      override fun requiresAuth(): Boolean = true

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          binding = ActivityHomeBinding.inflate(layoutInflater)
          setContentView(binding.root)

          setSupportActionBar(binding.toolbar)
          displayUserInfo()
          setupListeners()
      }

      private fun displayUserInfo() {
          binding.tvUserEmail.text = viewModel.getCurrentUserEmail()

          val loginTime = tokenManager.getLoginTime()
          val expiresAt = loginTime + (3 * 60 * 60 * 1000L)
          val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
          binding.tvSessionInfo.text = "Sesión expira a las ${sdf.format(Date(expiresAt))}"
      }

      private fun setupListeners() {
          binding.btnLogout.setOnClickListener {
              viewModel.logout()
              tokenManager.clearSession()
              val intent = Intent(this, LoginActivity::class.java)
              intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
              startActivity(intent)
              finish()
          }
      }
  }
  ```

- [ ] **Step 4: Commit**

  ```bash
  git add .
  git commit -m "feat: add Home screen with user info and logout"
  ```

---

## Task 10: Verificación final e integración

- [ ] **Step 1: Agregar el Web Client ID de Google**

  En `LoginActivity.kt`, reemplaza:
  ```kotlin
  private val WEB_CLIENT_ID = "TU_WEB_CLIENT_ID"
  ```
  con el ID de cliente web que copiaste en el Paso D de la configuración de Firebase Console.

- [ ] **Step 2: Ejecutar todos los tests unitarios**

  En Android Studio: menú **Run → Run All Tests**
  Resultado esperado: todos en verde

  O desde terminal:
  ```bash
  ./gradlew test
  ```
  Resultado esperado: `BUILD SUCCESSFUL` sin errores

- [ ] **Step 3: Ejecutar en emulador o dispositivo físico**

  Probar el flujo completo:
  - [ ] Abrir app → va directo a Login
  - [ ] Registro con todos los campos → va a Home
  - [ ] Logout → vuelve a Login
  - [ ] Login con email/contraseña → va a Home
  - [ ] Login con Google → va a Home
  - [ ] Recuperación de contraseña → recibir email
  - [ ] Simular expiración: cambiar `SESSION_DURATION_MS` a `10000L` (10 segundos), iniciar sesión, esperar → debe aparecer alerta y redirigir a Login
  - [ ] Restaurar `SESSION_DURATION_MS` a `3 * 60 * 60 * 1000L`

- [ ] **Step 4: Commit final**

  ```bash
  git add .
  git commit -m "feat: complete Firebase authentication system with 3h session management"
  ```

---

## Resumen de lo que necesitas de tu parte (Firebase Console)

| Paso | Acción | Qué me entregas |
|------|--------|-----------------|
| A | Crear proyecto Firebase | Nombre del proyecto |
| B | Agregar app Android | Descargar `google-services.json` |
| C | Habilitar Email/Password | Confirmación |
| D | Habilitar Google Sign-In | **ID de cliente web** (lo necesito para `LoginActivity`) |
| E | Crear Firestore Database | Confirmación |
| F | Publicar reglas Firestore | Confirmación |

Una vez tengas estos pasos completados, la implementación puede comenzar desde el Task 1.