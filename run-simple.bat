@echo off
set DB_HOST=localhost
set DB_USER=admin
set DB_PASSWORD=AgroComparador2026!
set DB_NAME=comparador
set PORT=8080
java -cp ".;mysql-connector-java-9.0.0.jar" agrocomparador
pause
