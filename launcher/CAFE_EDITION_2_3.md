# CleanYourTV Café Edition 2.3 · Galería de fondos

Actualización visual sobre Café Edition 2.2. Mantiene 16 accesos configurables (8 × 2), memoria interna, RAM, redes, meditación, herramientas y tarjetas de experiencias.

## Galería desde el control remoto

En el menú izquierdo abre **Ambientes**, navega con las flechas entre las miniaturas y pulsa **OK** sobre un fondo. El fondo elegido queda guardado en las preferencias de la aplicación. El menú puede volver a abrirse para seleccionar otro.

Hay **6 fondos nuevos** en calidad alta (cafetales, café de especialidad y meditación), más Café Edition original, Jedi y Noche serena. Las miniaturas están optimizadas para equipos con RAM limitada y los fondos son JPEG sin texto ni controles incrustados.

## Validación / instalación

La compilación de GitHub Actions usa `-PlauncherApplicationId=com.gathin.tvhome.cafe.v23`, una identidad independiente de Cafe 2.2. Ejecuta el artefacto `CleanYourTV-Cafe-2_3` y copia su APK a Termux HOME.

`adb -s 127.0.0.1:5555 install -r "$HOME/Cafe23.apk" && adb -s 127.0.0.1:5555 shell am start -n com.gathin.tvhome.cafe.v23/com.gathin.tvhome.CafeHomeActivity`

No cambia HOME, no desinstala el launcher antiguo, ni modifica aplicaciones del sistema.

En 2.3 la capa oscura del inicio es menos opaca para apreciar las fotografías sin perder contraste de los botones.
