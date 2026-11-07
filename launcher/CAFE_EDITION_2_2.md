# CleanYourTV Café Edition 2.2 — ajuste visual

Basado en la versión Café Edition 2.1 de la rama `jedi-v2-ui`.

Mejoras: interfaz de mayor contraste, 16 accesos reales en cuadrícula 8 × 2,
logos y etiquetas más legibles, tarjetas de experiencias ilustradas, 6 accesos
rápidos funcionales, barra superior separada para almacenamiento libre, RAM
libre, red y hora/fecha reales, y un paisaje cafetalero como fondo.

* No se crean accesos a servicios no instalados; las 16 posiciones son
  configurables con pulsación larga de OK o mediante Personalizar.
* Limpiar TV abre mantenimiento y administración segura de apps; no borra
  automáticamente datos privados ni deshabilita servicios del sistema.
* El clima no se muestra porque este parche no incluye un proveedor de
  meteorología en tiempo real.
* Paquete de prueba independiente: `com.gathin.tvhome.cafe.v22`.
  Se puede instalar al lado de Café 2.1 y Jedi Edition; no cambiar HOME
  hasta confirmar navegación y apariencia real en el televisor.
* Gradle 8.9, Android Gradle Plugin 8.5.2, Java 17 y SDK 34 via workflow.

## Aplicar desde Termux (una sola línea)
`cd "$HOME/cleanyourtv" && test "$(git branch --show-current)" = "jedi-v2-ui" && unzip -oq "$HOME/Cafe22.zip" -d . && git add .github/workflows/android.yml launcher/app/build.gradle launcher/app/src/main launcher/CAFE_EDITION_2_2.md && git diff --cached --check && git -c user.name="Carlos Restrepo" -c user.email="carlosr9313@users.noreply.github.com" commit -m "Refine Cafe Edition 2.2 visual layout" && git push -u origin jedi-v2-ui`

## Compilar
GitHub Actions: **Build CleanYourTV Cafe 2.2 Visual**.
Artifact: **CleanYourTV-Cafe-2_2**.

## Instalar desde HOME de Termux
`adb -s 127.0.0.1:5555 install -r "$HOME/Cafe22.apk" && adb -s 127.0.0.1:5555 shell am start -n com.gathin.tvhome.cafe.v22/com.gathin.tvhome.CafeHomeActivity`

Esta es una iteración de diseño, no una reproducción pixel por pixel de
una imagen estática. Debe compilarse y evaluarse sobre la resolución real
antes de establecerse como pantalla HOME.
