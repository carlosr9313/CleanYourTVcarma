# CleanYourTV Café Edition 2.1 — Vista previa para Challenger R3

Esta versión es **una aplicación de prueba separada**, con ID `com.gathin.tvhome.cafe.preview`.
No reemplaza a Jedi Edition 1.0 ni 2.0, y NO cambia la preferencia HOME por sí sola.

## Funciones reales

- **16 tarjetas en 8 columnas × 2 filas.** Las primeras cuatro son YouTube, Netflix, Prime Video y Play Store; las restantes son aplicaciones de TV instaladas y accesos funcionales cuando hay menos aplicaciones disponibles. No se muestran servicios como Disney+, Max o DGO como si estuvieran instalados cuando no lo están.
- **Personalización desde el TV.** Pulsa el botón «Personalizar» o mantén OK sobre una tarjeta para reasignarla a otra aplicación instalada o a una herramienta. El orden se guarda en preferencias locales; existe una opción de restauración con confirmación.
- **Barra superior de almacenamiento libre:** datos reales de la partición interna (`StatFs /data`). Al pulsar muestra libre y total.
- **Barra superior de RAM disponible:** lectura real de `ActivityManager.MemoryInfo`; se distingue de almacenamiento.
- **Redes:** indicador de transporte activo (Wi-Fi/Ethernet/sin red) y menú para abrir ajustes Wi-Fi, Bluetooth y conexión. No necesita permisos de ubicación ni inventa SSID ni temperatura.
- **Meditación:** inicia el temporizador nativo existente con opciones de 5, 10, 20, 30 minutos.
- **Ambientes:** cambia entre café, Jedi y noche serena; guarda la preferencia.
- **Café y origen:** diálogo de información con acceso a búsqueda educativa en YouTube.
- **Mantenimiento:** consulta almacenamiento/RAM y abre el administrador de aplicaciones de Android (no desinstala automáticamente nada ni inhabilita servicios).
- **Configuración y Todas las apps:** controles reales con manejo de fallos cuando un ajuste o aplicación no está disponible.
- **Navegación D-pad:** navegación entre tarjetas y menú; resaltado de enfoque, entrada mediante OK.

## Compilar en GitHub

El workflow `.github/workflows/android.yml` se ejecuta cuando se hace `push` a `jedi-v2-ui`, y produce el artifact `CleanYourTV-Cafe-Preview`.
El APK queda firmado por el debug key del runner. Usa un ID de paquete nuevo para que sea instalable sin sustituir una compilación anterior con otra firma.

## Instalar y abrir (desde Termux en el Challenger)

Guarda el APK de la ejecución nueva con nombre `Cafe.apk` en `$HOME`:

`adb -s 127.0.0.1:5555 install -r "$HOME/Cafe.apk" && adb -s 127.0.0.1:5555 shell am start -n com.gathin.tvhome.cafe.preview/com.gathin.tvhome.CafeHomeActivity`

No configures esta vista previa como HOME sin verificar primero los 16 accesos, la memoria, el menú redes y la navegación física.

## Recuperación

El launcher Jedi instalado previamente y Google TV Launcher no se eliminan; esta instalación no ejecuta ADB para cambiar HOME. Para borrar solo la vista previa:

`adb -s 127.0.0.1:5555 uninstall com.gathin.tvhome.cafe.preview`

## Límites

Las aplicaciones adicionales dependen de las que realmente estén instaladas y de la visibilidad que permita Android 11. Los controles de Wi-Fi y Bluetooth abren los ajustes del sistema; no fuerzan cambios privilegiados sin permiso. La cifra de almacenamiento corresponde a `/data`, no a una tarjeta externa. Esta entrega fue validada a nivel de código, recursos y manifiesto; debe compilarse y probarse con el Challenger físico.
