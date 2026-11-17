# Mi tiempo de estudio — Laboratorio 6

Aplicación Android Java para registrar horas de estudio. Código PUCP: 20233564.

## Funciones

- Acceso con **FirebaseUI** y diseño propio en español, con logo. Correo/contraseña, registro, recuperación de contraseña y Google. Facebook se incorpora al configurar sus valores públicos de cliente.
- La sesión de Firebase se conserva entre aperturas; cierre de sesión desde la lista.
- Cuatro vistas: acceso, lista de sesiones, agregar/editar y gráficos.
- Cada sesión contiene `course` (curso), `date` (ISO `AAAA-MM-DD`), `hours` (horas decimales, > 0 y <= 24) y `activity` (Teoría, Práctica o Laboratorio).
- CRUD en `users/{uid}/sessions/{id}` con listener de Firestore en tiempo real.
- Barras: **suma de horas por año y mes**, sin mezclar años. Torta: proporción de **horas**, no cantidad de sesiones, por actividad. Un filtro de curso actualiza ambos gráficos. Los meses se pueden desplazar horizontalmente y los valores también aparecen como texto.
- Validación local y reglas de seguridad que impiden acceder a otros usuarios.

## Abrir y compilar

1. Abrir esta carpeta en Android Studio con JDK 21 o posterior, Android SDK 37 y acceso a Internet.
2. Completar [CONFIGURAR_FIREBASE.md](CONFIGURAR_FIREBASE.md). El ID de aplicación se conserva como `pe.lab6.ecohuerto` para mantener compatibilidad con registros Firebase anteriores; el nombre visible es **Mi tiempo de estudio**.
3. Ejecutar `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` (Windows) o `./gradlew` con los mismos argumentos (Linux/macOS).
4. Instalar `app/build/outputs/apk/debug/app-debug.apk`.

El proyecto compila sin `google-services.json` para facilitar la revisión del código, pero **no permite iniciar sesión ni simula Firebase** sin esa configuración. No contiene cuentas, contraseñas ni datos ficticios precargados. El APK de EcoHuerto anterior no corresponde a esta solución y se retiró.

## Validación

Pruebas unitarias: sumas por mes, separación de años, filtro por curso en los dos gráficos, datos vacíos y validación de sesiones. Ver [PRUEBAS_MANUALES.md](PRUEBAS_MANUALES.md) para la comprobación en dos cuentas y dispositivos.

La rúbrica menciona Google y Facebook, aunque el enunciado solo exige Google. Se deja preparada la integración de Facebook, pero requiere una aplicación Meta y habilitar el proveedor en Firebase. No puede darse por probada antes de configurarla.

## Estado de la entrega

Código corregido; la conexión al proyecto `lab6-20233564` requiere descargar su archivo Android y habilitar sus proveedores. La compilación por sí sola no demuestra autenticación, persistencia remota ni permisos desplegados. No se ha concedido acceso a terceros ni enviado correos desde este trabajo.

Comprobación local realizada: `:app:assembleDebug`, `:app:testDebugUnitTest` (4 pruebas, 0 fallos) y `:app:lintDebug` completados correctamente. Lint conserva advertencias de mantenimiento (versiones, textos fijos y APIs deprecadas), sin errores. Autenticación y operaciones contra el proyecto remoto pendientes de configuración y prueba real.
