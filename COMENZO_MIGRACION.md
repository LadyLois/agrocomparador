# 📌 RESUMEN: ARCHIVOS DE MIGRACIÓN CREADOS

## 📂 Archivos Creados en tu Proyecto

### 📘 **DOCUMENTACIÓN PRINCIPAL**

#### 1. 🚀 [QUICK_START_MIGRACION.md](QUICK_START_MIGRACION.md)
   - **Para:** Usuarios con prisa (5 min)
   - **Contiene:** Los comandos más esenciales sin explicación
   - **Ideal si:** Solo quieres copiar-pegar y listo

#### 2. 📖 [MIGRACION_PASO_A_PASO.md](MIGRACION_PASO_A_PASO.md)
   - **Para:** Mayoría de usuarios (20-30 min)
   - **Contiene:** Pasos visuales, screenshots de prompts, explicaciones
   - **Ideal si:** Quieres entender qué está pasando

#### 3. 📚 [MIGRAR_A_AWS.md](MIGRAR_A_AWS.md)
   - **Para:** Usuarios que quieren documentación completa
   - **Contiene:** 3 métodos, troubleshooting extenso, múltiples opciones
   - **Ideal si:** Necesitas referencia detallada

#### 4. 🎯 [COMANDOS_RAPIDOS_MIGRACION.md](COMANDOS_RAPIDOS_MIGRACION.md)
   - **Para:** Usuarios técnicos que necesitan referencia rápida
   - **Contiene:** Todos los comandos posibles, opciones avanzadas
   - **Ideal si:** Ya conoces el proceso

#### 5. 📚 [README_MIGRACION.md](README_MIGRACION.md)
   - **Para:** Navegar entre documentos
   - **Contiene:** Índice, flujos de decisión, búsqueda rápida
   - **Ideal si:** No sabes cuál documento leer

---

### 🔧 **SCRIPTS POWERTOOLS (Windows PowerShell)**

#### 1. ⚡ [MIGRACION_RAPIDA.ps1](MIGRACION_RAPIDA.ps1)
   - **No. de Pasos:** 1 solo comando
   - **Tiempo:** 15-20 min
   - **¿Qué hace?**
     - Exporta BD local automáticamente
     - Transfiere a EC2 con SCP
     - Importa en EC2 remotamente
     - Verifica los datos
   - **Cómo usar:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File MIGRACION_RAPIDA.ps1
   ```

#### 2. 📦 [1_EXPORTAR_BD.ps1](1_EXPORTAR_BD.ps1)
   - **Paso:** 1 de 3 (exportación local)
   - **¿Qué hace?** Exporta BD con mysqldump
   - **Cómo usar:**
   ```powershell
   .\1_EXPORTAR_BD.ps1
   ```

#### 3. 📤 [2_TRANSFERIR_A_AWS.ps1](2_TRANSFERIR_A_AWS.ps1)
   - **Paso:** 2 de 3 (transferencia a EC2)
   - **¿Qué hace?** Copia archivo a EC2 con SCP
   - **Cómo usar:**
   ```powershell
   .\2_TRANSFERIR_A_AWS.ps1
   ```

---

### 🖥️ **SCRIPTS BASH (Para ejecutar en EC2 Linux)**

#### 1. 📥 [3_IMPORTAR_EN_EC2.sh](3_IMPORTAR_EN_EC2.sh)
   - **Paso:** 3 de 3 (importación remota)
   - **¿Qué hace?** Importa datos en EC2, descomprime, verifica
   - **Cómo usar:**
   ```bash
   ssh -i tu-key.pem ec2-user@tu-ec2-ip
   scp -i tu-key.pem 3_IMPORTAR_EN_EC2.sh ec2-user@tu-ec2-ip:/tmp/
   chmod +x /tmp/3_IMPORTAR_EN_EC2.sh
   /tmp/3_IMPORTAR_EN_EC2.sh
   ```

#### 2. 🔧 [INSTALAR_DEPENDENCIAS_EC2.sh](INSTALAR_DEPENDENCIAS_EC2.sh)
   - **Cuándo:** Primera vez que usas EC2
   - **¿Qué hace?** Instala Java, MySQL, configura auto-inicio
   - **Cómo usar:**
   ```bash
   ssh -i tu-key.pem ec2-user@tu-ec2-ip
   chmod +x INSTALAR_DEPENDENCIAS_EC2.sh
   ./INSTALAR_DEPENDENCIAS_EC2.sh
   ```

---

## 🎯 CÓMO ELEGIR QUÉ USAR

```
¿TIEMPO DISPONIBLE?
├─ 5 min    → QUICK_START_MIGRACION.md
├─ 15-20 min → MIGRACION_RAPIDA.ps1
├─ 30 min    → MIGRACION_PASO_A_PASO.md
├─ 1+ hora   → MIGRAR_A_AWS.md (lectura completa)
└─ Referencia → COMANDOS_RAPIDOS_MIGRACION.md

¿EXPERIENCIA?
├─ Principiante    → MIGRACION_PASO_A_PASO.md
├─ Intermedio       → MIGRACION_RAPIDA.ps1
├─ Avanzado/SysAdmin → COMANDOS_RAPIDOS_MIGRACION.md
└─ Confundido       → README_MIGRACION.md
```

---

## 📋 FLUJO DE TRABAJO RECOMENDADO

### **Plan A: Automatizado (Recomendado)**
```
1. Abre PowerShell
2. Ejecuta: MIGRACION_RAPIDA.ps1
3. Sigue prompts
4. ✅ Listo
```
**Tiempo total:** 20 min

---

### **Plan B: Paso a Paso (Más control)**
```
1. Lee: MIGRACION_PASO_A_PASO.md (10 min)
2. Ejecuta: 1_EXPORTAR_BD.ps1
3. Ejecuta: 2_TRANSFERIR_A_AWS.ps1
4. SSH a EC2
5. Ejecuta: 3_IMPORTAR_EN_EC2.sh
6. ✅ Listo
```
**Tiempo total:** 30 min

---

### **Plan C: Manual (Máximo control)**
```
1. Lee: COMANDOS_RAPIDOS_MIGRACION.md
2. Copia cada comando manualmente
3. Ejecuta en PowerShell/SSH
4. Verifica después de cada paso
5. ✅ Listo
```
**Tiempo total:** 40 min

---

## 🚀 INICIO RÁPIDO

### Si tienes prisa:
```powershell
# 1. Abre PowerShell
# 2. Navega a tu proyecto
cd c:\Java\agroagrocomparador

# 3. Ejecuta el script rápido
powershell -ExecutionPolicy Bypass -File MIGRACION_RAPIDA.ps1

# 4. Sigue los prompts
# ✅ BD migrada en 20 min
```

---

### Si prefieres hacerlo paso a paso:
```powershell
# 1. Exportar
.\1_EXPORTAR_BD.ps1

# 2. Transferir
.\2_TRANSFERIR_A_AWS.ps1

# 3. SSH a EC2 y ejecutar
ssh -i tu-key.pem ec2-user@tu-ec2-ip
/tmp/3_IMPORTAR_EN_EC2.sh
```

---

## 📊 ESTRUCTURA DE ARCHIVOS CREADOS

```
c:\Java\agroagrocomparador\
│
├── 📘 DOCUMENTACIÓN (Lee primero)
│   ├── QUICK_START_MIGRACION.md           (⚡ 5 min)
│   ├── README_MIGRACION.md                (📚 Índice)
│   ├── MIGRACION_PASO_A_PASO.md           (📖 30 min)
│   ├── MIGRAR_A_AWS.md                    (📕 Completa)
│   └── COMANDOS_RAPIDOS_MIGRACION.md      (🎯 Referencia)
│
├── 🔧 SCRIPTS WINDOWS (Ejecuta en tu PC)
│   ├── MIGRACION_RAPIDA.ps1               (⚡ Todo en 1)
│   ├── 1_EXPORTAR_BD.ps1                  (📦 Paso 1)
│   ├── 2_TRANSFERIR_A_AWS.ps1             (📤 Paso 2)
│
├── 🖥️ SCRIPTS LINUX (Ejecuta en EC2)
│   ├── 3_IMPORTAR_EN_EC2.sh               (📥 Paso 3)
│   ├── INSTALAR_DEPENDENCIAS_EC2.sh       (🔧 Setup)
│
└── 📁 backups/                            (Tus backups aquí)
    └── backup_agrocomparador_TIMESTAMP.sql
```

---

## ✅ CHECKLIST ANTES DE EMPEZAR

- [ ] EC2 instancia creada y corriendo
- [ ] MySQL instalado en EC2 (o corre INSTALAR_DEPENDENCIAS_EC2.sh)
- [ ] Archivo .pem descargado y guardado
- [ ] Tienes acceso SSH a EC2 (prueba: `ssh -i tu-key.pem ec2-user@tu-ec2-ip`)
- [ ] BD local 'agrocomparador' tiene datos
- [ ] mysqldump y scp disponibles en tu PC (Windows 10+)
- [ ] PowerShell 5.1+ o posterior

---

## 🚨 PROBLEMAS COMUNES

**"Script no se ejecuta en PowerShell"**
→ Ejecuta como administrador y prueba:
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser
```

**"mysql command not found"**
→ En EC2, instala:
```bash
sudo yum install mysql -y
```

**"SSH connection refused"**
→ Verifica:
- EC2 está corriendo
- Security Group permite puerto 22
- Archivo .pem tiene permisos (no lo abras en editor)
- Usas el usuario correcto (ec2-user o ubuntu)

---

## 📞 ¿QUÉ HAGO SI...?

| Situación | Solución |
|-----------|----------|
| "No sé qué documento leer" | Lee [README_MIGRACION.md](README_MIGRACION.md) |
| "Solo tengo 5 minutos" | Usa [QUICK_START_MIGRACION.md](QUICK_START_MIGRACION.md) |
| "Quiero hacerlo todo automático" | Ejecuta [MIGRACION_RAPIDA.ps1](MIGRACION_RAPIDA.ps1) |
| "Prefiero hacerlo paso a paso" | Lee [MIGRACION_PASO_A_PASO.md](MIGRACION_PASO_A_PASO.md) |
| "Necesito cada comando posible" | Ve a [COMANDOS_RAPIDOS_MIGRACION.md](COMANDOS_RAPIDOS_MIGRACION.md) |
| "Algo no funciona" | Busca en troubleshooting de MIGRACION_PASO_A_PASO.md |

---

## 🎉 RESULTADO ESPERADO

Después de la migración:
```bash
# Conectarse a BD remota funciona:
mysql -h localhost -u admin -p agrocomparador -e "SELECT COUNT(*) FROM productos;"
# ✓ Resultado: número de productos

# Aplicación Java se conecta sin errores de BD
java -cp ".:mysql-connector-java-9.0.0.jar" agroagrocomparador
# ✓ Conexión establecida correctamente
```

---

## 🔐 NOTA IMPORTANTE DE SEGURIDAD

⚠️ **NUNCA** hagas esto:
- ❌ Hardcodear credencials en el código
- ❌ Compartir archivo .pem
- ❌ Hacer MySQL públicamente accesible
- ❌ Usar contraseña débil

✅ **SIEMPRE**:
- ✓ Usar variables de entorno para credenciales
- ✓ Guardar .pem en lugar seguro
- ✓ Permitir MySQL solo desde EC2 (restricción IP)
- ✓ Usar SSH key en lugar de password

---

**Creado:** Abril 2026
**Para:** Agroagrocomparador v1.0
**Última revisión:** ${fecha}

¡Listo para empezar? 🚀 Elige tu método y comienza!
