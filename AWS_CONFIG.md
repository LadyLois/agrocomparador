# Configuración para AWS Production
# Usar estas variables en AWS Systems Manager Parameter Store o en Lambda/ECS environment variables

# IMPORTANTE: Para RDS MySQL en AWS
# DB_HOST debe ser el endpoint de RDS, ej: agrocomparador.abc123.us-east-1.rds.amazonaws.com
# DB_PORT por defecto 3306
# DB_NAME: agrocomparador
# DB_USER: admin (o el usuario que creaste)
# DB_PASSWORD: Usar AWS Secrets Manager o Systems Manager Parameter Store (NUNCA en el código)

# Ejemplo de variable en AWS:
# DB_HOST=agrocomparador-prod.c9akciq32.us-east-1.rds.amazonaws.com
# DB_PORT=3306
# DB_NAME=agrocomparador
# DB_USER=admin
# DB_PASSWORD=${RETRIEVAR_DE_AWS_SECRETS_MANAGER}
# PORT=8080 (o usar ALB/ELB)
