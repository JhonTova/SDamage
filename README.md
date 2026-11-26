<div align="center">

# SDamage

### Una suite de PvP modular. Enciende solo lo que necesitas.

Ajusta daños, cooldowns y mecánicas de combate por módulos, sin plugins pesados de más.

[![build](https://github.com/JhonTova/SDamage/actions/workflows/build.yml/badge.svg)](https://github.com/JhonTova/SDamage/actions/workflows/build.yml) [![release](https://img.shields.io/github/v/release/JhonTova/SDamage?label=release)](https://github.com/JhonTova/SDamage/releases/latest) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62B47A) ![Java](https://img.shields.io/badge/Java-21-E76F00)

</div>

---

## Características

- **Diseño modular** — activa o desactiva cada módulo por separado.
- **Reglas de daño** personalizadas por arma o situación.
- **Cooldowns** de ítems (perlas de ender, totems, lo que definas).
- **Ajustes de comida** y regeneración.
- **Control de totems** de la inmortalidad.
- **Por mundo** — aplica el PvP solo donde toca.
- Compatible con WorldGuard para respetar regiones.

## Comandos

| Comando | Acción |
|---|---|
| `/spvp info` | Estado del plugin. |
| `/spvp modules` | Lista los módulos activos. |
| `/spvp reload` | Recarga la configuración y los módulos. |

Permiso: `spvp.admin`

## Requisitos

Requiere **[SiriusCore](https://github.com/JhonTova/SiriusCore)**. WorldGuard es opcional.

## Instalación

`SiriusCore.jar` + `SDamage.jar` en `plugins/`. Paper / Purpur 1.21.11+.

## Descarga

El `.jar` de cada versión está en [Releases](https://github.com/JhonTova/SDamage/releases/latest).

## Compilar desde el código

Requiere Java 21 y Maven. SiriusCore no está en un repositorio Maven público, así que
primero se instala en local la versión que declara el `pom.xml`:

```bash
git clone --branch v1.1.1 https://github.com/JhonTova/SiriusCore.git
mvn -f SiriusCore/pom.xml install -DskipTests
mvn package        # en este repositorio; el .jar queda en target/
```

Cada push y pull request se compila en GitHub Actions. Al subir una tag `v*`, el `.jar`
se adjunta solo a la release.

## Licencia

[MIT](LICENSE): puedes usarlo, modificarlo y redistribuirlo manteniendo el aviso de copyright.
Requiere SiriusCore, que tiene su propia licencia.
