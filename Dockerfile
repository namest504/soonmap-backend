FROM adoptopenjdk/openjdk11
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
#ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]
ARG PROFILE
ENV PROFILE_VALUE $PROFILE
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=${PROFILE_VALUE}","/app.jar"]