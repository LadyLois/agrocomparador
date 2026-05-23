#!/bin/bash
# Script de compilación para Linux/Mac con Web Scraping

cd "$(dirname "$0")/../../"

echo ""
echo "============================================"
echo "🔨 Compilando agrocomparador con Scraper"
echo "============================================"
echo ""

# Verificar si los JARs existen
if [ ! -f "jsoup-1.15.3.jar" ]; then
    echo "❌ ERROR: jsoup-1.15.3.jar no encontrado"
    echo "   Descargalo desde: https://jsoup.org/download"
    echo "   Ubicacion requerida: jsoup-1.15.3.jar"
    exit 1
fi

if [ ! -f "mysql-connector-java-9.0.0.jar" ]; then
    echo "❌ ERROR: mysql-connector-java-9.0.0.jar no encontrado"
    exit 1
fi

echo "✓ Configuración correcta"
echo ""
echo "Compilando archivos Java..."
echo ""

# Compilar con classpath
javac -cp "jsoup-1.15.3.jar:mysql-connector-java-9.0.0.jar" -d . \
    agrocomparador.java \
    agrocomparador/data/*.java \
    agrocomparador/business/*.java \
    agrocomparador/ui/*.java \
    agrocomparador/scraper/*.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Compilación exitosa!"
    echo ""
    echo "Para ejecutar:"
    echo "  java -cp \"jsoup-1.15.3.jar:mysql-connector-java-9.0.0.jar:.\" agrocomparador"
    echo ""
else
    echo ""
    echo "❌ Error durante la compilación"
    echo ""
    exit 1
fi
