FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -Dmaven.resolver.transport=wagon \
    -Dmaven.wagon.http.retryHandler.count=5 dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests -Dmaven.resolver.transport=wagon \
    -Dmaven.wagon.http.retryHandler.count=5 package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/servicio-usuarios-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
