FROM gradle:6-jdk8 AS GRADLE

COPY . /app
WORKDIR /app
RUN gradle build
RUN gradle shadowJar

FROM amazoncorretto:8-alpine3.14
COPY --from=GRADLE /app /app
WORKDIR /app

CMD java -jar build/libs/AbstractTransformation-all.jar