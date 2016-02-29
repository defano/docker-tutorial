# An introduction to Docker

Supplemental materials and reference guide for the Introduction to Docker presentation.

## Installing Docker

Mac and Windows users will need to install the Docker Toolbox to run and control containers (download for [Windows](https://docs.docker.com/engine/installation/windows/) or [Mac OS](https://docs.docker.com/engine/installation/mac/)).

Linux users can [install Docker directly](https://docs.docker.com/engine/installation/linux/) on various distributions.

##### A note for Mac and Windows users
While you can't run Docker containers *directly* on your Mac or Windows machine, you can manage and control them as though they were. Docker Toolbox comes with a Mac and Windows native version of the `docker` command that knows how to proxy into the Docker Machine and control containers executing inside that "machine." (This was previously not possible on Windows with boot2docker; Docker Machine fixed this limitation.)

*Why should I care?* This feature has the effect of letting native Mac and Windows processes directly control and manage Docker containers. This is especially useful if you're developing code in which your build scripts or integration tests expect to be able to build, start and run a container. Of course, keep in mind that your container-to-host port forwarding will not expose the container's port to your Mac or Windows, but only to your Docker Machine VM. If you need to access your container's ports directly from Mac OS or Windows, you'll need to forward those same ports from the Docker Machine guest OS to your host. (Sound of head exploding, here.)

## Docker Command Reference

A brief refresher on building images and managing containers.

#### Build a Docker image from a Dockerfile
`docker build -t <tag-name> <directory>` where `<tag-name>` is the name that will be assigned to this image and `<directory>` is the path to the directory containing the Dockerfile.

Docker will download any dependent images and build your image according to the Dockerfile. If all goes well, the last line produced by the command should read `Successfully built 351e2c3e7c5c` (your hash will vary).

#### Listing images stored locally
`docker images` will display all the images built on your machine. It will produce output like:
```
REPOSITORY          TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
my-example-image    latest              0db6c7503383        7 seconds ago       227.6 MB
ubuntu              latest              6cc0fc2a5ee3        3 weeks ago         187.9 MB
```

#### Create a container from an existing image
`docker create -p <host-port>:<container-port> <image-name>` will create a container from the image tagged `<image-name>` where `<container-port` is an exposed port on the container and `<host-port>` is the port on the host machine that it should be mapped to. For example, if our container was running a web server on port 80 and we wanted to access that server through port 8080 on our host, we'd provide the argument `-p 8080:80`.

By default, Docker will assign a nonsensical (sometimes comical) name to your container, like `lactating_monkey`. You'll likely wish to refer to this container by name in the future and you can assign your own name using the `--name=` argument on the command line. For example, `docker create my-example-image --name=webserver`.

#### Listing containers
* `docker ps -a` will show all containers (in any state) on the local machine.
* `docker ps` will show only the running containers.

Both commands produce output like:
```
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                  NAMES
fb6bcff347c8        my-example-image    "nginx"             7 minutes ago       Up 7 minutes        0.0.0.0:8082->80/tcp   webserver
```

#### Starting a container
`docker run <container-name>` will start the container named `<container-name>`.

Had we not used the `--name` argument in the `docker create` command we would have to refer to this container using either a) the nonsensical name auto-assigned to it (`lactating_monkey`) or b) the container ID hash (something like `fb6bcff347c8`).

#### Opening a shell on a running container
`docker exec -it <container-identifier> bash` will open a shell on the running container identified by `<container-identifier>` (a container name or ID).

More generally, this command is executing the `bash` (shell) command on the container; the `-it` switch indicates that the command should be interactive and allocate a pseudo TTY.

#### Stopping a container
`docker stop <container-name>` will stop the running container named `<container-name>`.

As with `docker start` you may substitute the container name with an ID hash prefix if you wish.

#### Helpful cleanup commands
Typically one does not usually clean and rebuild everything, but when experimenting its often helpful to be able to return to a clean state.
* `docker stop $(docker ps -q)` to stop all running containers on your machine.
* `docker rm $(docker ps -aq)` to delete all containers on your machine.
* `docker rmi $(docker images -q)` to blow away all images on your machine (works only if no containers exist that were created from the images; best to execute after blowing away all containers).

#### A note about hashes
Whenever referring to an image or container by its ID hash (i.e., `fb6bcff347c8`), you may supply the shortest prefix that remains unambiguous. For example, rather than `docker stop fb6bcff347c8` you could use `docker stop fb6` provided no other container IDs began with `fb6`. In fact, oftentimes a single letter will suffice: `docker stop f`

## Examples
To run these examples, you'll need a shell inside the Docker Machine and the contents of this repository. Get started by:

1. Start a shell inside your Docker Machine by using the Docker Quickstart Terminal app that was installed on Mac and Windows (Linux users can skip this step). See the "Docker Quickstart Terminal" section of the [Windows](https://docs.docker.com/engine/installation/windows/) and [Mac OS](https://docs.docker.com/engine/installation/mac/) install guide for details.
2. Clone this repository using the git command `git clone http://github.com/TBD`

As you work through these examples be sure to read, understand, and follow all safety rules that came with your power tools. Also, take a look at the example Dockerfiles. They're commented to explain what's happening in each step.

#### Example 1: Static Web Server
In this example, we'll create a Docker container based on the Ubuntu distribution, install the Nginx server on it and have it serve a single HTML document (one that will be "baked" into the Docker image).

1. Enter the example directory: `cd webserver-example`
2. Build a Docker image from the Dockerfile using `docker build -t example/nginx-img .` Upon success, this will have create a new Docker image called `example/nginx-img`.
3. Create a container from the newly built image and forward port 80 of the container to port 8080 of our host: `docker create -p 8080:80 --name=webserver example/nginx-img`
4. Use `docker start webserver` to run the container.

Then...

1. See it in action with `curl localhost:8080` (or enter this URL in a browser). You should see the HTML contents of `index.html`.
2. When you're satisfied with your fine work, stop the container with `docker stop webserver`. If you wish, you can destroy the container using `docker rm webserver` and delete the image used to create it with `docker rmi example/nginx-img`

#### Example 2: Volume Web Server
Example 1 illustrated how we could set up a web server in Docker but this was clearly limited in that it could only serve files that were baked into the container's image. This example solves that limitation by linking a portion of the host's filesystem to the container (called a "volume"). This will enable us to add or modify the contents served by Nginx without having to rebuild (or even stop) the container.

1. Enter the example directory: `cd webserver-volume-example`
2. Build the image using `docker build -t example/nginx-volume-img .`
3. Create a container from this image, `docker create -p 8080:80 --name=volumeserver -v /path/to/example/www:/var/www example/nginx-volume-img` where `path/to/example/www` is the absolute path to the `www` directory inside this example. (Sorry Charlie, you can't use a relative path here.)
4. Start the container `docker start volumeserver`

Then...
1. `curl localhost:8080` to see the HTML contents of `www/index.html` returned.
2. Try modifying the contents of `www/index.html` and making the same `curl`
request (you should see your changes reflected in the output)
3. Try adding new files to the `www/` directory. For example,
`echo '<html>Goodbye Cruel World</html>' > www/goodbye.html` then request `curl localhost:8080/goodbye.html`
4. Stop the container using `docker stop volumeserver` and, if desired, blow away the container and image. You remember how to do that, right? (Hint: `docker rm` and `docker rmi`)

#### Example 3: Spring Boot Server
Creates a Docker container running a trivial Spring Boot web application that responds to simple RESTful requests.

1. Enter the example directory: `cd spring-example`
2. Build the Docker image with `docker build -t example/spring-img .`
3. Create a container from this image using `docker create -p 8080:8080 --name springserver example/spring-img`
4. Start the container: `docker start springserver`

Then...

1. In another terminal window, `curl localhost:8080` to see the Spring web app produce the output "Hello World!".
2. Stop the container using `docker stop springserver`

#### Example 4: Linking Spring Boot to Redis
In this example, the goal is to have our Spring Boot server read and write entries in a Redis cache that's operating in a separate container. Docker provides the concept of *container linking* for this purpose; when two containers are linked, Docker will write a host entry into the `/etc/hosts` file of the target container providing the IP address of the linked container.

1. Enter the Redis example directory: `cd redis-example`
2. Build the Redis image using `docker build -t example/redis-img .`
3. Create a container instance from the newly generated image with `docker create --name redis example/redis-img`
4. Start the container with `docker start redis`. Note that we're not exposing any of the container's ports to the host operating system. Nevertheless, the Redis port (6379) will be accessible from the Spring Boot server which we will link to it.
5. Return to the Spring Boot example: `cd ../spring-example`
6. Start the Spring Boot server and link it to the Redis container (already running) using `docker run --link redis:redishost -p 8080:8080 --name springredis example/spring-img`. (Since the Spring Boot example image has already been created, we don't need to re-run the `build` command.)

At this point, our webserver container should be able to delegate to our Redis container for caching key/value pairs.

Then...

1. Smoke test our web server by fetching the "Hello Work" page again, `curl localhost:8080`.
2. Put a key/value pair into the Redis cache with `curl 'localhost:8080/set?key=mykey&value=myvalue'`. If everything is working, the server should respond with `Set: mykey=myvalue`. <br>**Gotcha:** Note the use of single quotes around the curl command; this is required to escape the `&` from the shell.
3. Verify that Redis has accepted our key/value pair by reading it back, `curl 'localhost:8080/get?key=mykey'`. The server should respond with `Get: mykey=myvalue`
4. If you like, shut down both containers with `docker stop redis springredis`

**What is `--link` actually doing?** Link takes the name of a running container and a hostname and assigns the IP address of the container to the hostname. Docker accomplishes this by writing a record into the linked container's `/etc/hosts`, file like:

```
172.17.0.9     b8a2dc66e465
127.0.0.1      localhost
::1            localhost ip6-localhost ip6-loopback
192.168.1.90   redishost
```

In this case, our Redis container is going to be linked into the `springserver` container using the hostname `redishost`. If you were to open a shell in the web server's container (use `docker exec -it springredis bash`) and examine the `/etc/host` file, you'd file a `redisthost` entry similar to that shown above. With this, our Spring Boot web app container can reach the Redis container by referring to it's hostname `redishost`.

#### Example 5: Linking containers with Docker Compose
Linking containers is pretty cool. That said, it's not hard to imagine how this could become quite unmanageable as the number of containers that need to communicate with one another grow within a distributed system. Docker provides the Compose tool for scripting the creation and linking of many containers (very handy for development and CI environments).

In this example, we'll reproduce the Redis / Spring Web App system we created in the last example, but this time we'll provision it with Docker Compose.
