# ------------------------------------------------------------------------------
# Specify the image from which we inherit: an Ubuntu distribution in this case.
# Note that every Dockerfile _must_ begin with a 'FROM' command.
#
# What are my choices? Search the Docker Hub for available base images:
# https://hub.docker.com
# ------------------------------------------------------------------------------
FROM ubuntu:latest


# ------------------------------------------------------------------------------
# Whose fault is this? Assigning blame is very important. :)
# ------------------------------------------------------------------------------
MAINTAINER matt@defano.com


# ------------------------------------------------------------------------------
# Install the Redis server software onto the image
# ------------------------------------------------------------------------------
RUN apt-get update
RUN	apt-get install -y redis-server


# ------------------------------------------------------------------------------
# Expose the port that Redis runs on
# ------------------------------------------------------------------------------
EXPOSE      6379


# ------------------------------------------------------------------------------
# Start the Redis server in the container
# ------------------------------------------------------------------------------
CMD  ["/usr/bin/redis-server"]
