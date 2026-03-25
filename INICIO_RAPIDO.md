# ⚡ INICIO RÁPIDO

## 1️⃣ Compilar (Windows)

En PowerShell o CMD, desde la carpeta del proyecto:

```bash
javac -d . agrocomparador.java agrocomparador/data/*.java agrocomparador/business/*.java agrocomparador/ui/*.java
```

**Debería mostrar:** Sin errores ✅

---

## 2️⃣ Ejecutar (Windows)

```bash
java agrocomparador
```

**Debería mostrar:**
```
🚀 Servidor iniciado en puerto 80
📍 Accede a: http://localhost/
```

> ⚠️ **Nota:** El puerto 80 puede requerir permisos de administrador en Windows. Si falla:
> - Cambiar en `agrocomparador/ui/WebServer.java` línea: `private static final int PUERTO = 8080;`
> - Recompilar: `javac -d . agrocomparador.java agrocomparador/data/*.java agrocomparador/business/*.java agrocomparador/ui/*.java`
> - Luego acceder a: `http://localhost:8080/`

---

## 3️⃣ Probar en Navegador

### Ver todos los productos
Abre: **http://localhost/**

Deberías ver:
- ✅ Título "🌾 Comparador de Precios Agrícolas"
- ✅ Tabla con columnas: Producto | Variedad | Fuente | Precio
- ✅ Formulario de búsqueda
- ✅ Cantidad de registros

### Buscar por producto
Abre: **http://localhost/?producto=Tomate**

Deberías ver:
- ✅ Solo productos que contengan "Tomate"
- ✅ Formulario con "Tomate" prerellenado
- ✅ Botón "Limpiar" visible

---

## ⚠️ Si no funciona...

### Error: "Address already in use"
**Problema:** Puerto 80 en uso
**Solución:** Cambiar a puerto 8080 (ver arriba)

### Error: "JDBC Driver not found"
**Problema:** Driver MySQL no está en classpath
**Solución:** Descargar `mysql-connector-java-*.jar` y:
```bash
javac -cp "mysql-connector-java-*.jar" -d . agrocomparador.java agrocomparador/data/*.java agrocomparador/business/*.java agrocomparador/ui/*.java
java -cp ".;mysql-connector-java-*.jar" agrocomparador
```

### Error: "Connection refused"
**Problema:** MySQL no está corriendo o credenciales incorrectas
**Solución:**
1. Verificar MySQL esté corriendo
2. Editar `agrocomparador/data/DatabaseConnection.java`
3. Verificar credenciales y base de datos "comparador" existe

### Tabla vacía en navegador
**Solución:** Insertar datos de ejemplo (ver README.md)

---

## 📁 Estructura Creada

```
✅ agrocomparador.java
✅ agrocomparador/
   ✅ data/
      ✅ DatabaseConnection.java
      ✅ ProductoDAO.java
   ✅ business/
      ✅ ProductoService.java
   ✅ ui/
      ✅ WebServer.java
      ✅ HTMLBuilder.java
✅ README.md
✅ ESTRUCTURA.md
✅ EJEMPLOS_USO.java
✅ CHECKLIST.md
✅ RESUMEN.md
```

---

## 📖 Documentación

- **README.md** → Instrucciones completas
- **ESTRUCTURA.md** → Explicación de arquitectura
- **EJEMPLOS_USO.java** → Código de ejemplo
- **CHECKLIST.md** → Verificación de requisitos
- **RESUMEN.md** → Resumen ejecutivo

---

## 🎯 Lo que hace

1. **Lee** datos de MySQL (tabla precios, productos, fuentes)
2. **Filtra** por nombre de producto si se especifica en URL
3. **Genera** tabla HTML moderna
4. **Devuelve** página web formateada

---

## 🚀 Próximos Pasos

1. ✅ Compilar
2. ✅ Ejecutar
3. ✅ Probar en navegador
4. ⏳ Extender con más funcionalidades

¡Diviértete! 🎉

---

**¿Preguntas?** Consulta los archivos de documentación incluidos.
