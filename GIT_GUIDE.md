# 📋 Archivos para Git - Guía Definitiva

## ✅ ARCHIVOS QUE VAN A GIT (SEGUROS)

### Código Fuente
```
✅ agrocomparador/**/
   ├── data/DatabaseConnection.java
   ├── data/ProductoDAO.java
   ├── business/ProductoService.java
   ├── ui/WebServer.java
   ├── ui/HTMLBuilder.java
   └── agrocomparador.java

✅ criar_base_datos.sql
   (Script SQL sin datos sensibles)
```

### Documentación
```
✅ README.md
✅ SECURITY.md
✅ QUICK_START.md
✅ DEPLOYMENT_AWS.md
✅ AWS_READY.md
✅ AWS_CONFIG.md
✅ PRODUCTION_READY.md
✅ ARQUITECTURA_VISUAL.md
✅ ESTRUCTURA.md
✅ CHECKLIST.md
✅ CONFIGURAR_MYSQL.md
```

### Configuración Pública (SIN VALORES REALES)
```
✅ .env.example
   (Plantilla, sin contraseñas reales)

✅ .env.production.example
   (Plantilla, sin contraseñas reales)

✅ .gitignore
   (Protege archivos sensibles)
```

### Scripts Públicos
```
✅ setup-env.bat
   (Script seguro - pide contraseña en runtime)

✅ setup-env.ps1
   (Script seguro - pide contraseña en runtime)

✅ run_production.sh
   (Script seguro - Lee de AWS)

✅ run_aws.bat
   (Script seguro - pide contraseña en runtime)
```

### Dependencias
```
✅ mysql-connector-java-9.0.0.jar
   (JAR público, necesario para conectar a MySQL)
```

---

## ❌ ARCHIVOS QUE NO VAN A GIT (PROTEGIDOS)

### Archivos de Ambiente (CON VALORES REALES)
```
❌ .env
❌ .env.local
❌ .env.production
❌ .env.*.local
❌ .env.*.prod

⚠️ RAZÓN: Contienen DB_PASSWORD, DB_HOST, etc.
```

### Archivos Compilados
```
❌ *.class
❌ /target/
❌ /bin/
❌ /build/
❌ /out/

⚠️ RAZÓN: Se generan automáticamente al compilar
```

### Logs y Output
```
❌ *.log
❌ output.log
❌ nohup.out
❌ /logs/
❌ /log/

⚠️ RAZÓN: Pueden contener información sensible
```

### IDE y Configuración Local
```
❌ .idea/
❌ .vscode/
❌ *.iml
❌ *.code-workspace
❌ .classpath
❌ .project

⚠️ RAZÓN: Configuración personal, no necesaria en repo
```

### Secretos
```
❌ *.secret
❌ *.key
❌ *.pem
❌ credentials.json
❌ aws-credentials
❌ ~/.aws/

⚠️ RAZÓN: Exposición de credenciales
```

---

## 📊 Estructura del Repositorio Final

```
agrocomparador/
├── .gitignore                 ✅ INCLUIR (protección)
├── .env.example              ✅ INCLUIR (plantilla sin valores)
├── .env.production.example   ✅ INCLUIR (plantilla sin valores)
├── README.md                 ✅ INCLUIR
├── SECURITY.md              ✅ INCLUIR
├── QUICK_START.md           ✅ INCLUIR
├── PRODUCTION_READY.md      ✅ INCLUIR
├── DEPLOYMENT_AWS.md        ✅ INCLUIR
├── AWS_READY.md             ✅ INCLUIR
├── AWS_CONFIG.md            ✅ INCLUIR
├── ARQUITECTURA_VISUAL.md   ✅ INCLUIR
├── ESTRUCTURA.md            ✅ INCLUIR
├── CHECKLIST.md             ✅ INCLUIR
├── CONFIGURAR_MYSQL.md      ✅ INCLUIR
├── crear_base_datos.sql     ✅ INCLUIR
│
├── mysql-connector-java-9.0.0.jar  ✅ INCLUIR
├── setup-env.bat            ✅ INCLUIR
├── setup-env.ps1            ✅ INCLUIR
├── run_production.sh        ✅ INCLUIR
├── run_aws.bat              ✅ INCLUIR
│
├── agrocomparador/          ✅ INCLUIR
│   ├── data/
│   │   ├── DatabaseConnection.java
│   │   └── ProductoDAO.java
│   ├── business/
│   │   └── ProductoService.java
│   └── ui/
│       ├── WebServer.java
│       └── HTMLBuilder.java
├── agrocomparador.java      ✅ INCLUIR
│
├── .env                     ❌ IGNORAR (credenciales locales)
├── .env.local              ❌ IGNORAR (credenciales locales)
├── .env.production         ❌ IGNORAR (credenciales produccción)
├── *.class                 ❌ IGNORAR (compilados)
├── .vscode/                ❌ IGNORAR (IDE local)
├── .idea/                  ❌ IGNORAR (IDE local)
├── *.log                   ❌ IGNORAR (logs)
└── target/                 ❌ IGNORAR (compilación)
```

---

## 🚀 Pasos Finales Antes de Hacer Push

### 1. Verificar status
```bash
git status
# Debería mostrar archivos .java, .md, .sql, etc.
# NO debería mostrar .env, *.class, *.log
```

### 2. Ver diferencias
```bash
git diff
# Verificar que no hay contraseñas en los cambios
```

### 3. Revisar .gitignore
```bash
cat .gitignore | head -20
# Debe tener .env, .env.local, *.log, etc.
```

### 4. Crear .gitattributes (Opcional pero recomendado)
```bash
# Asegurar que los archivos de fin de línea sean consistentes
* text=auto
*.java text eol=lf
*.sh text eol=lf
*.bat text eol=crlf
*.sql text eol=lf
*.md text eol=lf
```

### 5. Comitear
```bash
git add .
git commit -m "Initial commit: AgroComparador - Secure, AWS-ready Java app"
git push origin main
```

---

## ⚠️ CHECKLIST SEGURIDAD ANTES DE PUSH

- [ ] `.gitignore` incluido y correcto
- [ ] NO hay archivos `.env` (solo `.env.example`)
- [ ] NO hay credenciales en archivos `.md`
- [ ] NO hay contraseñas en comentarios del código
- [ ] NO hay archivos `.class` compilados
- [ ] `mysql-connector-java-9.0.0.jar` está en `.gitignore` EXCEPTO
- [ ] Documentación `SECURITY.md` incluida
- [ ] `PRODUCTION_READY.md` incluida

---

## 🔐 Verificación Final (Comandos)

```bash
# 1. Buscar credenciales antes de comitear
git diff --cached | grep -i "password\|secret\|key"
# Debería NO encontrar nada

# 2. Verificar que .gitignore es efectivo
git check-ignore -v .env
git check-ignore -v *.class
# Debería mostrar que está ignorado

# 3. Ver qué va a subirse
git ls-files
# NO debería incluir .env, *.log, *.class, .vscode/

# 4. Simulación de clone (verificar integridad)
git archive --format=tar HEAD | tar -O | head -50
# Verifica estructura sin credenciales
```

---

## ✅ LISTO PARA GIT Y PRODUCCIÓN

Con este `.gitignore` actualizado, tu repositorio está 100% protegido:

- ✅ Sin credenciales
- ✅ Sin archivos compilados
- ✅ Sin IDE local
- ✅ Sin logs
- ✅ Documentación completa
- ✅ Seguro para clonar y ejecutar en AWS

**¡Ahora sí está COMPLETAMENTE LISTO! 🚀**
