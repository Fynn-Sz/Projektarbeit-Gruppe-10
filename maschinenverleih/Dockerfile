# =====================================================================
#  Docker-Image für den Maschinenverleih (zweistufig)
#  Stufe 1 baut die JAR mit dem vollen JDK, Stufe 2 enthält nur noch die
#  Laufzeit (JRE) + JAR → deutlich kleineres und sichereres Image.
# =====================================================================

# ---------- Stufe 1: Bauen ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Erst nur die Maven-Dateien kopieren und Abhängigkeiten laden.
# Docker merkt sich diese Schicht – solange sich pom.xml nicht ändert,
# müssen die Abhängigkeiten beim nächsten Bau nicht neu geladen werden.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Dann den Quellcode kopieren und die JAR bauen.
# Tests laufen hier NICHT (keine Datenbank im Build) – das erledigt vorher test.yml.
COPY src src
RUN ./mvnw -B -q package -DskipTests

# ---------- Stufe 2: Ausführen ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Nicht als root laufen lassen
RUN useradd --system --uid 1001 --no-create-home app
USER app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# Datenbank-Zugang kommt zur Laufzeit über Umgebungsvariablen
# (DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD) – nie ins Image schreiben!
ENTRYPOINT ["java", "-jar", "app.jar"]
