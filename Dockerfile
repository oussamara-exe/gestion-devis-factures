# Étape 1 : Build - Compilation de l'application Spring Boot avec Maven
FROM maven:3-eclipse-temurin-26 AS builder

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier pom.xml et télécharger les dépendances
# Cette étape permet de mettre en cache les dépendances Maven si pom.xml ne change pas
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Construire l'application et créer le JAR
# -B : mode batch (pas de prompts interactifs)
# -DskipTests : sauter les tests pendant le build Docker (ils seront exécutés dans CI/CD)
RUN mvn clean package -B -DskipTests

# Vérifier que le JAR a bien été créé
RUN find /app/target -name "*.jar" -type f

# Étape 2 : Runtime - Image optimisée pour exécuter le JAR
FROM eclipse-temurin:17-jre

# Installer wget pour les healthchecks
RUN apt-get update && apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

# Créer un utilisateur non-root pour la sécurité
RUN groupadd -r spring && useradd -r -g spring spring

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR depuis l'étape builder
# Utiliser un pattern wildcard pour être flexible sur le nom du JAR
COPY --from=builder /app/target/*.jar app.jar

# Changer le propriétaire du fichier JAR
RUN chown spring:spring app.jar

# Passer à l'utilisateur non-root
USER spring:spring

# Exposer le port 8080 (port par défaut de Spring Boot)
EXPOSE 8080

# Variables d'environnement pour Java optimisé pour les conteneurs
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Commande pour démarrer l'application
# -Djava.security.egd=file:/dev/./urandom : accélère le démarrage en utilisant un nonce non-bloquant
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

