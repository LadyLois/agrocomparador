# 📚 ÍNDICE DE GUÍAS DE MIGRACIÓN

## 🎯 Elige tu método según tu experiencia:

### ⚡ **RÁPIDO** - Si confías en scripts automatizados:
📄 [MIGRACION_RAPIDA.ps1](MIGRACION_RAPIDA.ps1)
- 1 solo script que hace todo automáticamente
- Solo necesitas tu EC2 IP y archivo .pem
- Mejor para usuarios con experiencia

---

### 📋 **COMPLETO** - Si prefieres hacerlo paso a paso:
📄 [MIGRACION_PASO_A_PASO.md](MIGRACION_PASO_A_PASO.md)
- Guía visual con instrucciones detalladas
- Pasos claros para Windows PowerShell + Linux SSH
- Incluye troubleshooting

---

### 🔧 **MANUAL** - Si necesitas cada comando:
📄 [COMANDOS_RAPIDOS_MIGRACION.md](COMANDOS_RAPIDOS_MIGRACION.md)
- Todos los comandos posibles de migración
- Opciones avanzadas (RDS, Docker, etc.)
- Referencia rápida

---

### 📖 **DETALLADA** - Si necesitas documentación profunda:
📄 [MIGRAR_A_AWS.md](MIGRAR_A_AWS.md)
- 3 métodos diferentes (archivo SQL, mysqldump, direct)
- Explicaciones de cada paso
- FAQs y troubleshooting

---

## 🚀 SCRIPTS INCLUIDOS

| Script | Ubicación | Función | Para quién |
|--------|-----------|---------|-----------|
| **MIGRACION_RAPIDA.ps1** | Raíz | Una línea, todo automático | Expertos |
| **1_EXPORTAR_BD.ps1** | Raíz | Exporta BD local | Inicio/intermedio |
| **2_TRANSFERIR_A_AWS.ps1** | Raíz | Transfiere a EC2 | Inicio/intermedio |
| **3_IMPORTAR_EN_EC2.sh** | Raíz | Importa en EC2 (bash) | Inicio/intermedio |

---

## 📊 FLUJO DE DECISIÓN

```
¿Tienes experiencia con AWS/Linux?
├─ SÍ → MIGRACION_RAPIDA.ps1
├─ Algo → MIGRACION_PASO_A_PASO.md
└─ NO  → MIGRAR_A_AWS.md (lee primero)
```

---

## ⏱️ TIEMPO ESTIMADO

| Método | Tiempo | Dificultad |
|--------|--------|-----------|
| Script automático | 15-20 min | Baja |
| Paso a paso | 20-30 min | Media |
| Manual/directos | 25-40 min | Media-Alta |
| Leyendo doc completa | 1 hora | Baja |

---

## ✅ CHECKLIST PREVIO A MIGRACIÓN

- [ ] EC2 instancia corriendo
- [ ] MySQL instalado en EC2
- [ ] SSH key (.pem) descargado
- [ ] Archivo .pem tiene permisos correctos (400)
- [ ] BD local 'agrocomparador' existe y tiene datos
- [ ] MySQL client en tu PC está en PATH
- [ ] PowerShell 5.1+ (para scripts)
- [ ] Security Group de EC2 permite puerto 22

---

## 🔍 BÚSQUEDA RÁPIDA

**Si buscas:**

- 🔄 **Exportar BD local** → [MIGRACION_PASO_A_PASO.md#paso-1️⃣-exportar-bd-local](MIGRACION_PASO_A_PASO.md#paso-1️⃣-exportar-bd-local)
- 📤 **Transferir a EC2** → [MIGRACION_PASO_A_PASO.md#paso-2️⃣-transferir-a-ec2](MIGRACION_PASO_A_PASO.md#paso-2️⃣-transferir-a-ec2)
- 📥 **Importar en EC2** → [MIGRACION_PASO_A_PASO.md#paso-4️⃣-importar-bd-en-ec2](MIGRACION_PASO_A_PASO.md#paso-4️⃣-importar-bd-en-ec2)
- ⚙️ **Configurar Java** → [MIGRACION_PASO_A_PASO.md#-configurar-aplicación-java-para-usar-bd-remota](MIGRACION_PASO_A_PASO.md#-configurar-aplicación-java-para-usar-bd-remota)
- 🔐 **Usar RDS** → [COMANDOS_RAPIDOS_MIGRACION.md#-usar-rds-amazon-en-lugar-de-mysql-en-ec2](COMANDOS_RAPIDOS_MIGRACION.md#-usar-rds-amazon-en-lugar-de-mysql-en-ec2)
- 🚨 **Troubleshooting** → [MIGRACION_PASO_A_PASO.md#-troubleshooting](MIGRACION_PASO_A_PASO.md#-troubleshooting)
- ❓ **FAQs** → [MIGRACION_PASO_A_PASO.md#-preguntas-frecuentes](MIGRACION_PASO_A_PASO.md#-preguntas-frecuentes)

---

## 🎓 ORDEN DE LECTURA RECOMENDADO

### Para principiantes:
1. Este archivo (índice) - 2 min
2. MIGRACION_PASO_A_PASO.md - 10 min lectura
3. Ejecutar pasos uno por uno - 30 min

### Para intermedios:
1. MIGRACION_PASO_A_PASO.md - 5 min lectura
2. Ejecutar MIGRACION_RAPIDA.ps1 - 20 min

### Para avanzados:
1. COMANDOS_RAPIDOS_MIGRACION.md - referencia rápida
2. Ejecutar comandos directamente

---

## 📞 RESOLUCIÓN DE PROBLEMAS

**No entiendo cómo usar los scripts:**
→ Lee [MIGRACION_PASO_A_PASO.md](MIGRACION_PASO_A_PASO.md)

**El script falla en algún paso:**
→ Busca en [MIGRAR_A_AWS.md](MIGRAR_A_AWS.md#-troubleshooting) o [MIGRACION_PASO_A_PASO.md#-troubleshooting](MIGRACION_PASO_A_PASO.md#-troubleshooting)

**Necesito hacer migraciones recurrentes:**
→ Usa [MIGRACION_RAPIDA.ps1](MIGRACION_RAPIDA.ps1)

**Quiero entender todo detalle a detalle:**
→ Lee [MIGRAR_A_AWS.md](MIGRAR_A_AWS.md) completamente

---

## 🔗 DOCUMENTACIÓN RELACIONADA

- [DEPLOYMENT_AWS.md](DEPLOYMENT_AWS.md) - Deployment completo de aplicación
- [AWS_CONFIG.md](AWS_CONFIG.md) - Configuración AWS
- [AWS_READY.md](AWS_READY.md) - Checklist de preparación
- [CONFIGURAR_MYSQL.md](CONFIGURAR_MYSQL.md) - Setup inicial MySQL

---

## 💡 TIPS PRÁCTICOS

✅ **Antes de empezar:**
- Haz un backup local de tu BD local primero
- Verifica que SSH funciona a tu EC2
- Prueba MySQL local (conecta y ejecuta un SELECT)

✅ **Durante la migración:**
- Ten ambas ventanas abiertas (PC y EC2 SSH)
- Copia-pega los comandos (no los escribas)
- Si algo falla, siempre puedes reintentar

✅ **Después de la migración:**
- Verifica que los datos se importaron correctamente
- Guarda el backup en carpeta de backups
- Anota en tu documentación la fecha de migración

---

## 🆚 COMPARACIÓN DE MÉTODOS

### Método 1: Script RÁPIDO
```
Ventajas:
✓ Solo 1 comando
✓ Tiempo mínimo
✓ Menos errores manuales

Desventajas:
✗ Necesita actualizar variables
✗ Menos control
```

### Método 2: Paso a Paso
```
Ventajas:
✓ Entiendes cada paso
✓ Fácil de debuggear
✓ Control total

Desventajas:
✗ Más tiempo
✗ Más comandos para escribir
```

### Método 3: Manual (directos)
```
Ventajas:
✓ Máximo control
✓ Puedes automatizar después
✓ Para scripting avanzado

Desventajas:
✗ Tedioso
✗ Requiere experiencia
```

---

## 🎉 ¡LISTO PARA EMPEZAR?

**Haz clic en la guía que elegiste** y sigue los pasos.

Si tienes dudas en el proceso, el troubleshooting está incluido en cada documento.

¡Buena suerte con tu migración! 🚀

---

**Última actualización:** Abril 2026
**Para:** AgroComparador v1.0
