# ------------------------------------------------------------------------------
# In this example, we're starting with the Java:8 base image. Of course, we
# could start with a basic Linux distro like Ubuntu and run commands to install
# a JRE, but this simplifies our life.
#
# Search https://hub.docker.com for alternatives.
# ------------------------------------------------------------------------------
FROM java:8


# ------------------------------------------------------------------------------
# Base image comes with Java, but we'll need Maven to compile our app.
# ------------------------------------------------------------------------------
RUN apt-get update
RUN apt-get install -y maven


# ------------------------------------------------------------------------------
# Specify the current working directory (like cd in a shell). All commands
# executed subsequent to this will execute from within the /code directory.
#
# Not strictly necessary as we could always specify absolute paths instead.
# ------------------------------------------------------------------------------
WORKDIR /code


# ------------------------------------------------------------------------------
# For simplicity, we're adding the source code of our webapp project into the
# image and compiling it with Maven. While perfectly legal, a better approach
# might be to pre-compile the app (perhaps with a CI system) and simply ADD the
# compiled JAR, WAR or EAR to your image.
#
# Running 'mvn package' here creates a target/ subdirectory and places the
# compiled JAR (spring-example-1.0.0.jar) into it.
# ------------------------------------------------------------------------------
ADD spring-app /code
RUN mvn package -DskipTests=true


# ------------------------------------------------------------------------------
# Expose the server's port (8080)
# ------------------------------------------------------------------------------
EXPOSE 8080


# ------------------------------------------------------------------------------
# Start the server when the container starts
# ------------------------------------------------------------------------------
CMD /usr/lib/jvm/java-8-openjdk-amd64/bin/java -jar target/spring-example-1.0.0.jar
