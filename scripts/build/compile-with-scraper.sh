#!/bin/bash
# Script de compilación para Linux/Mac con Web Scraping

cd "$(dirname "$0")/../../"

echo ""
echo "============================================"
echo "🔨 Compilando agrocomparador con Scraper"
echo "============================================"
echo ""

# Verificar si los JARs existen
if [ ! -f "lib/jsoup-1.15.3.jar" ]; then
    echo "❌ ERROR: jsoup-1.15.3.jar no encontrado"
    echo "   Descargalo desde: https://jsoup.org/download"
    echo "   Ubicacion requerida: lib/jsoup-1.15.3.jar"
    exit 1
fi

echo "✓ Configuración correcta"
echo ""
echo "Compilando archivos Java..."
echo ""

# Compilar con classpath
javac -cp "lib/jsoup-1.15.3.jar:lib/mysql-connector-java-8.0.33.jar" -d . \
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
    echo "  java -cp \"lib/jsoup-1.15.3.jar:lib/mysql-connector-java-8.0.33.jar:.\" agrocomparador"
    echo ""
else
    echo ""
    echo "❌ Error durante la compilación"
    echo ""
    exit 1
fi
