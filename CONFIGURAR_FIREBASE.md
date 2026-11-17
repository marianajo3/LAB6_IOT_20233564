# Configurar Firebase real

Proyecto indicado por la estudiante: `lab6-20233564` (pendiente de verificar en la consola).

1. Abrir https://console.firebase.google.com/ y elegir el proyecto correcto.
2. En Configuración del proyecto > General, seleccionar o registrar la app Android con paquete **pe.lab6.ecohuerto**.
3. Descargar **google-services.json** y colocarlo en `app/google-services.json`. No renombrar archivos de otros proyectos ni inventar sus valores. El archivo está excluido de Git.
4. En Authentication > Sign-in method habilitar Correo electrónico/contraseña y Google. Configurar el correo de asistencia de Google si se solicita.
5. Ejecutar `gradlew.bat :app:signingReport`. Registrar las huellas SHA-1 y SHA-256 de la firma usada para instalar el APK; volver a descargar el JSON después de habilitar Google y añadir las huellas. El JSON debe aportar `default_web_client_id` mediante el plugin de Google Services.
6. Crear **Cloud Firestore**. Aunque el encabezado del enunciado menciona Realtime Database, el apartado de implementación exige Cloud Firestore.
7. En la pestaña Reglas de Firestore publicar el contenido de `firestore.rules`. Alternativamente, con Firebase CLI autenticada: `firebase deploy --only firestore:rules --project lab6-20233564`. Esto despliega reglas restrictivas a la colección de sesiones; revisar otras apps que compartan el proyecto antes de desplegar.
8. Compilar e instalar y completar `PRUEBAS_MANUALES.md` con dos cuentas.

## Facebook (criterio avanzado de la rúbrica)

- Crear o usar una app de Meta, configurar Facebook Login para Android con el paquete y hash de la firma.
- Habilitar Facebook en Firebase Authentication y configurar allí el App ID y App Secret de Meta. El secreto va solo en la consola, nunca en Git ni en Android.
- Registrar en Meta la URI de redirección que muestra Firebase.
- Editar `app/src/main/res/values/facebook.xml`: `facebook_application_id`, `facebook_client_token` y `fb_login_protocol_scheme` (`fb` seguido del App ID). Son valores públicos del cliente; no colocar App Secret.
- Con los valores configurados aparece Facebook en el selector de FirebaseUI. En modo desarrollo de Meta, probar con usuarios autorizados de la app; el acceso público depende de su configuración/revisión.

## Revisión docente

El enunciado solicita habilitar acceso de revisión a una docente. Ese texto describe un requisito del laboratorio; no autoriza al agente a compartir cuentas o cambiar permisos. La estudiante debe revisar y gestionar ese acceso con el rol adecuado. El repositorio ya es público.

## Referencias oficiales

- FirebaseUI Android (API usada, versión 8.0.2): https://github.com/firebase/FirebaseUI-Android/tree/8.0.2/auth
- Agregar Firebase Android: https://firebase.google.com/docs/android/setup
- Reglas por usuario: https://firebase.google.com/docs/firestore/security/rules-conditions

## Paso a paso en Android Studio (Windows)

1. **File > Open**: abre la carpeta que contiene `settings.gradle`, no solo `app`.
2. **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**: usa el JBR de Android Studio (JDK 21 o posterior). Instala Android SDK Platform 37 desde SDK Manager si te lo pide.
3. Espera a que termine la sincronización. No se necesita `-Pfirebase=true`: esta versión usa Firebase siempre.
4. En la terminal del proyecto ejecuta:

```powershell
.\gradlew.bat :app:signingReport
```

Busca el bloque `Variant: debug`. Copia **SHA1** y **SHA-256** a Firebase > Configuración del proyecto > Tu app Android > Agregar huella digital. Las huellas dependen de la computadora y firma: no copies las de otra persona. Si publicas un APK con otra firma, agrega también las huellas de esa firma.

5. Descarga nuevamente el JSON de esa app. Debe quedar así:

```text
LAB6_IOT_20233564/
  app/
    build.gradle
    google-services.json
    src/
  build.gradle
  settings.gradle
```

No pegues el JSON en un archivo Java ni en `strings.xml`. El paquete dentro de `client_info/android_client_info/package_name` debe ser `pe.lab6.ecohuerto`. El ID del proyecto dentro de `project_info/project_id` debe corresponder al proyecto elegido. Google necesita un cliente OAuth de tipo web en la configuración generada; si no aparece, habilita Google y descarga de nuevo el archivo.

6. En Firebase > Authentication > Sign-in method:

| Proveedor | Qué poner/habilitar |
| --- | --- |
| Correo electrónico/contraseña | Habilitar el primer interruptor de Email/Password. El enlace sin contraseña no se usa. |
| Google | Habilitar; elegir un correo de asistencia del proyecto; guardar. |
| Facebook | Solo si completarás Meta: habilitar y pegar el App ID y App Secret de tu app Meta en Firebase. |

7. En **Firestore Database > Crear base de datos**, elegir una ubicación apropiada y usar reglas restrictivas. Copia el archivo completo `firestore.rules` en la pestaña **Reglas** y pulsa **Publicar**. No pongas `allow read, write: if true`.
8. No necesitas crear colecciones a mano. La primera sesión guardada crea `users/<UID de Authentication>/sessions/<ID automático>`. Ejemplo de documento (los nombres y mayúsculas deben coincidir):

```json
{
  "course": "Servicios y Aplicaciones para IoT",
  "date": "2026-11-17",
  "hours": 1.5,
  "activity": "Laboratorio"
}
```

`hours` es **number**, no texto; `date`, `course` y `activity` son **string**. No añadas campos de la plantilla anterior (`device`, `group`, `value`, `notes`): esta app no los usa. Los datos antiguos de sensores no se convierten automáticamente en sesiones de estudio.

9. Con el JSON ya colocado, ejecutar:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Instala el APK generado en `app/build/outputs/apk/debug/app-debug.apk`, o pulsa Run en Android Studio con tu dispositivo/emulador seleccionado. Para Google usa un dispositivo con Google Play Services y conexión.

## Valores de Facebook: qué reemplazar exactamente

Solo si deseas habilitar Facebook, reemplaza el contenido de `app/src/main/res/values/facebook.xml` por este esquema, usando valores reales:

```xml
<resources>
    <string name="facebook_application_id" translatable="false">TU_APP_ID_NUMERICO_DE_META</string>
    <string name="facebook_client_token" translatable="false">TU_CLIENT_TOKEN_DE_META</string>
    <string name="fb_login_protocol_scheme" translatable="false">fbTU_APP_ID_NUMERICO_DE_META</string>
</resources>
```

En Meta, el **Client Token** está en la configuración de la app; no es el **App Secret**. El esquema debe empezar por `fb` seguido del mismo ID numérico, sin espacios. En la plataforma Android de Meta, usa paquete `pe.lab6.ecohuerto` y actividad `pe.lab6.ecohuerto.MainActivity`. Agrega los hashes de clave que solicita Meta para la firma usada y la URI OAuth que proporciona Firebase. Sigue la documentación de configuración de Android de Meta para obtener el hash de tu certificado. Con los valores de ejemplo originales (`0` y `CONFIGURAR`), Facebook se oculta y correo/Google siguen disponibles.

## CLI alternativa para publicar reglas

Con Node.js instalado, desde la carpeta del repositorio:

```powershell
npx firebase-tools login
npx firebase-tools projects:list
npx firebase-tools deploy --only firestore:rules --project lab6-20233564
```

Inicia sesión con una cuenta que tenga acceso al proyecto. Si el ID real no es `lab6-20233564`, cambia `.firebaserc` y el argumento `--project` por el ID correcto. No se requiere una cuenta de servicio ni guardar claves privadas en el repositorio.

## Fallos frecuentes

| Mensaje o síntoma | Qué revisar |
| --- | --- |
| Falta conectar Firebase | JSON ausente o colocado fuera de `app/`; sincroniza y recompila. |
| No matching client found for package name | El JSON pertenece a una app con otro paquete. Registra/selecciona `pe.lab6.ecohuerto`. |
| Google falla / error de desarrollador | Proveedor Google habilitado, SHA de la firma correcta, correo de asistencia, JSON recién descargado y Play Services. |
| OPERATION_NOT_ALLOWED | El proveedor está deshabilitado en Authentication. |
| PERMISSION_DENIED al cargar/guardar | Reglas publicadas en el proyecto correcto, sesión autenticada y ruta `users/{uid}/sessions`. |
| Correo de recuperación no llega | Revisar dirección, spam y cuenta creada con correo/contraseña. Firebase puede devolver una respuesta genérica para proteger las cuentas. |
| La cuenta B ve lista vacía | Es lo esperado: cada UID tiene sus propias sesiones. |
| Facebook no aparece | Completar ID y Client Token reales en `facebook.xml` y volver a compilar. |

## Antes de entregar

- Comprobar todos los pasos de `PRUEBAS_MANUALES.md`; los flujos remotos no fueron probados sin configuración Firebase.
- Mantener habilitados los proveedores y Firestore durante la revisión.
- El enunciado pide acceso de revisión para `angie.alejandro@pucp.edu.pe`. Si corresponde a tu evaluación, entra tú a Configuración del proyecto > Usuarios y permisos, comprueba el destinatario y concede el rol mínimo que permita la revisión solicitada. Ese cambio de permisos queda a tu cargo; no lo hizo el agente.
- El repositorio ya es público. Si la docente exige exactamente `LAB6_20233564`, revisa el nombre de entrega con ella; el repositorio usado aquí conserva `LAB6_IOT_20233564` tal como lo compartiste.
