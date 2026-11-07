# CleanYourTV Jedi Edition 2.0 — rama `jedi-v2-ui`

Prototipo Android TV compilado por GitHub Actions. Se instala como una app diferente: `com.gathin.tvhome.jedi.v2`.
NO reemplaza `com.gathin.tvhome.jedi`, ni el launcher anterior, ni altera Google TV Launcher.

## Integrado

- Pantalla con menú lateral, imagen Jedi, reloj/fecha local y saludo según la hora.
- Estado de red y espacio libre real en `/data`.
- Cuatro aplicaciones reales (YouTube, Netflix TV, Prime Video, Play Store).
- Navegación explícita con D-pad entre menú, tarjetas y fila inferior, con foco visible.
- Meditación: temporizador local seleccionable de 5, 10, 20 o 30 minutos, con pausa/finalizar.
- Ambientes: 2 fondos empaquetados (Atardecer Jedi y Noche serena), preferencia persistente.
- Configuración: abre ajustes existentes del launcher.
- Mantenimiento: consulta el almacenamiento real y abre administrador de aplicaciones Android.
- Un botón "Clima" sin temperatura simulada (su integración real queda pendiente).

## Pendiente

- Clima en tiempo real, integración multimedia avanzada, widgets de consumo y perfección pixel a pixel del arte de referencia.
- Verificación visual en un Challenger R3 real. Validar HOME antes de establecerlo como predeterminado.

## Instalación de prueba

1. GitHub Actions > Build CleanYourTV Jedi 2.0 > Artifacts > CleanYourTV-Jedi-2-test.
2. Coloca `app-debug.apk` en `HOME` de Termux con el nombre `Jedi2.apk`.
3. Ejecuta en una sola línea: `adb -s 127.0.0.1:5555 install -r "$HOME/Jedi2.apk" && adb -s 127.0.0.1:5555 shell am start -n com.gathin.tvhome.jedi.v2/com.gathin.tvhome.JediHomeActivity`.
4. NO cambies HOME hasta comprobar pantalla, mando y apps. Conserva la versión 1.0 como respaldo.
