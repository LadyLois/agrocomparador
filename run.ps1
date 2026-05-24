$env:DB_HOST        = "localhost"
$env:DB_USER        = "root"
$env:DB_PASSWORD    = "agrocomparador"
$env:DB_NAME        = "agrocomparador"
$env:DB_PORT        = "3306"
$env:PORT           = "8080"
$env:ADMIN_PASSWORD = "Isi_2026"

java -cp ".;jsoup-1.15.3.jar;mysql-connector-java-9.0.0.jar" agrocomparador
