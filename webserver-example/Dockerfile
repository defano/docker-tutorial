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
# These 'RUN' commands execute when the image is built and each modify the
# resulting image's filesystem. As a matter of fact, each RUN command creates
# it's own "intermediate" image. This has the effect of letting Docker cache
# each result for future reuse. For example, if I had another Dockerfile that
# called for ubuntu:latest and then performed an apt-get update, Docker would
# simply grab the image created by (and cached) as a result of this Dockerfile.
#
# Note that these DO NOT execute when the container starts. See the 'CMD'
# instruction for executing commands inside the container at runtime (think of
# RUN like 'compile time' and CMD as 'run time').
# ------------------------------------------------------------------------------

# Install Nginx
RUN apt-get update
RUN apt-get -y install nginx

# Don't run Nginx as a daemon (if you do, the container will stop immediately)
RUN echo "daemon off;" >> /etc/nginx/nginx.conf

# Create SSL and document root directories
RUN mkdir -p /etc/nginx/ssl
RUN mkdir -p /var/www


# ------------------------------------------------------------------------------
# Copy the 'default' and 'index.html' files from the local filesystem into the
# image. These files provide server configuration ('default') and a "hello
# world" web page ('index.html').
# ------------------------------------------------------------------------------
ADD default /etc/nginx/sites-available/default
ADD index.html /var/www/index.html


# ------------------------------------------------------------------------------
# Allow access to port 80
# ------------------------------------------------------------------------------
EXPOSE 80


# ------------------------------------------------------------------------------
# This command executes when a container created from this image starts. (The
# 'nginx' command starts the web server.)
#
# Note that as soon as this command completes, the container stops (thus the
# reason we don't want the server to run as a daemon).
# ------------------------------------------------------------------------------
CMD ["nginx"]
