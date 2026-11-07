# CleanYourTV — Jedi Edition (prototipo funcional)

La pantalla elegida se implementa como una **actividad nativa Android TV**, no como una imagen estática ni como una modificación del firmware. Mantiene el launcher original `MainActivity` dentro del proyecto y conserva su código.

## En el diseño

- Fondo Jedi cinematográfico integrado en recursos, sin recurrir a `/sdcard/Android/data`.
- Menú lateral con Inicio, Apps (interfaz clásica), Ajustes, Mantenimiento e Información.
- Hora y fecha reales, saludo según la hora, conectividad real y almacenamiento interno disponible real.
- Fila principal exclusivamente con YouTube, Netflix, Prime Video y Play Store.
- Explora: Meditación (búsqueda en YouTube), Ambientes (Ajustes de pantalla), Configuración (CleanYourTV) y Mantenimiento (Administrar aplicaciones).
- Controles compatibles con mando/DPAD y realce de enfoque, con un diseño de vidrio oscuro.

## Limitaciones del prototipo

- No incluye datos reales de clima (se evita mostrar una temperatura ficticia).
- Las tarjetas de Explora abren funciones existentes: no hay todavía reproductor de meditación ni motor de fondos.
- No desinstala ni deshabilita aplicaciones de Google automáticamente.
- El fondo es una adaptación artística inspirada en la composición elegida, no una extracción exacta de su maqueta que contiene elementos de interfaz.
- La compilación debe validarse con GitHub Actions; aquí no hay SDK Android completo.

## Instalación sin reemplazar el launcher que ya funciona

La Action de la raíz compila el APK con `-PlauncherApplicationId=com.gathin.tvhome.jedi` y deja intacta la versión `com.gathin.tvhome` ya instalada.
Una vez descargado y transferido `app-debug.apk` a HOME de Termux:

`adb -s 127.0.0.1:5555 install -r "$HOME/app-debug.apk"`

`adb -s 127.0.0.1:5555 shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -p com.gathin.tvhome.jedi`

Si no te gusta el resultado, abre de nuevo la versión anterior:

`adb -s 127.0.0.1:5555 shell am start -a android.intent.action.MAIN -c android.intent.category.HOME -p com.gathin.tvhome`

No elijas launcher predeterminado hasta validar el mando y todas las apps.
