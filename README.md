# An introduction to Docker

Supplemental materials and lab instructions for the Chicago Coders Conference Introduction to Docker presentation.

All materials referenced here (except for the installation materials provided on the thumb drive) are available online at [http://github.com/defano/docker-tutorial](http://github.com/defano/docker-tutorial).

## A note for Mac and Windows users
While you can't run Docker containers *directly* on your Mac or Windows machine (yet... there's a Windows beta in the works), you can manage and control them as though they were. Docker Toolbox comes with a Mac and Windows native version of the `docker` command that knows how to proxy into a Docker Machine and control containers executing inside that "machine" just as though they were executing on your host.

This feature has the effect of letting native Mac and Windows processes directly control and manage Docker containers. This is especially useful if you're developing code in which your build scripts or integration tests expect to be able to build, start and run a container.

#### Beware the Port Spaghetti

Keep in mind that when you run containers in the Docker Machine your container-to-host port publishing will not expose the container's port to your Mac or Windows, but only to your VM. That is, a container which exposes port 8080 will be accessible from port 8080 on the Docker Machine, *not port 8080 on the Mac or Windows machine*. (Sound of head exploding, here.)

To access your container's ports from Mac OS or Windows, you'll need to either:

* Direct requests to the IP address of your Docker Machine and not to `localhost` (use `docker-machine ip` to determine the Docker Machine address), or
* Forward the same ports that your container exposes from the Docker Machine to your host OS (using a configuration setting in VirtualBox, typically).

For example, lets say you're a Mac user with Docker Toolbox installed and you've just started a container running a web server on port 8080. To reach the web server from a Mac OS terminal, you'd need to first determine the IP address of the Docker Machine:

```
$ docker-machine ip
192.168.99.104
```

Then make your web request to this IP address:

```
$ curl 192.168.99.104:8080
Hello World!
```

## Part 1: Get started with Docker Toolbox

Start by installing Docker Toolbox on Mac and Windows machines. Linux users should install Docker directly ([see instructions](https://docs.docker.com/engine/installation/linux/)) and then proceed to Part 2.

1. Locate and execute the Docker Toolbox installer in the materials provided on the thumb drive.
2. Upon completion, launch the newly installed "Docker Quickstart Terminal" application. The first time you launch this it will create and configure a Docker Machine (VM) for you. You should see something like:
```
Running pre-create checks...
Creating machine...
(default) Copying /Users/matt/.docker/machine/cache/boot2docker.iso to /Users/matt/.docker/machine/machines/default/boot2docker.iso...
(default) Creating VirtualBox VM...
(default) Creating SSH key...
(default) Starting the VM...
(default) Check network to re-create if needed...
(default) Waiting for an IP...
Waiting for machine to be running, this may take a few minutes...
Detecting operating system of created instance...
Waiting for SSH to be available...
Detecting the provisioner...
Provisioning with boot2docker...
Copying certs to the local machine directory...
Copying certs to the remote machine...
Setting Docker configuration on the remote daemon...
Checking connection to Docker...
Docker is up and running!
```

Windows users may need to enable virtualization in their BIOS if Docker complains of `This computer doesn't have VT-X/AMD-v enabled. Enabling it in the BIOS is mandatory.`

Before doing anything else, enter `docker ps` on the command line (which should print the headings for an otherwise empty table).

There's something wrong your setup if the command appears to timeout or if you receive an error about SSL certificates or something like `Cannot connect to the Docker daemon. Is the docker daemon running on this host?`. If so, double-check that you're working from the terminal created by the Docker Quickstart Terminal. Don't bother proceeding until you have this working.

#### So what's this Docker Quickstart Terminal doing?
Short answer: Setting everything up for you. Long answer:

1. It creates a Docker Machine VM (called `default`) if one does not already exist.
2. It starts the default Docker Machine VM if it is not already running.
3. It configures the shell to let your native `docker` command work by tunneling into the VM auto-magically (allowing you to execute the `docker` command on your Mac or Windows machine as though Docker was running natively on your machine).

#### What if I don't want to use a Quickstart Terminal?
 Piece of cake. Follow these instructions:

1. If you don't already have a Docker Machine VM, create one with `docker-machine create --driver virtualbox default` (where `default` is the name of the VM).
2. If you haven't already started the VM, start it with `docker-machine start default`
3. Configure your shell using `eval $(docker-machine env)`

## Part 3: Install the base images

**Following along at home?** Skip this section and proceed to Part 3. Docker will automatically download dependent images from the Docker Repository for you.

In order to prevent everyone from attempting to download (fairly large) Docker images over the Chicago Coder Conference's (fairly slow) internet connection, we're going to pre-load them from the materials provided on the thumb drive.

Note that this is a rather unusual thing to do and most Docker users will never encounter a need for this in the wild. (Were it not for our limited bandwidth we'd simply let Docker automatically populate our image cache from the Docker Repository online.)

In the terminal created by the `Docker Quickstart Terminal` application:

1. Locate the `docker-images.tar` file in the provided materials.
2. Load these images into your local Docker image cache using `docker load -i docker-images.tar` This will take a few moments and produce output that looks like:
```
917c0fc99b35: Loading layer [==================================================>] 130.9 MB/130.9 MB
5f70bf18a086: Loading layer [==================================================>] 1.024 kB/1.024 kB
bd750002938c: Loading layer [==================================================>] 45.19 MB/45.19 MB
...
e9f4c7b44e04: Loading layer [==================================================>] 400.1 MB/400.1 MB
```

Once this process has completed, verify everything was loaded correctly. Execute `docker images`. You should see:

```
REPOSITORY                  TAG                 IMAGE ID            CREATED             SIZE
example/spring-img          latest              63751c485b33        20 hours ago        864 MB
example/nginx-volume-img    latest              f0aaa65a8453        20 hours ago        216.3 MB
example/nginx-img           latest              a149b9ef9c74        20 hours ago        216.3 MB
example/redis-img           latest              2ab3acbc7e98        20 hours ago        161.2 MB
ubuntu                      latest              2fa927b5cdd3        9 days ago          122 MB
java                        8                   713a9aa340eb        12 days ago         642.9 MB
defano/desert-chicago-njs   latest              13d84dd66ee1        12 weeks ago        1.042 GB
```

**For the interested student:** Curious how we archived these images? Piece of cake: `docker save -o docker-images.tar $(docker images --format "{{.Repository}}:{{.Tag}}")` will write all image layers stored in your local cache to the specified tarball.

## Examples

As you work through these examples be sure to read, understand, and follow all safety rules that came with your power tools. But mostly take a look at the example Dockerfiles. They're documented to explain what's happening at each step.

#### Example 0: Warmup Exercise
Docker can be an amazingly useful tool without ever authoring a single Dockerfile. Just as one can find pre-built virtual machines online, one can use the Docker Hub (repository) to find and experiment with existing images.

In this warmup exercise, we'll run a copy of the [Oasis Chicago project](https://github.com/defano/oasis-chicago-njs). Oasis Chicago is an open-source visualization of business deserts in Chicago (created by me and my colleague [Ben Galewsky](https://github.com/BenGalewsky)).

Typically we'd use the `docker search` command or the search feature on the [Docker Hub website](https://hub.docker.com) to [find this](https://hub.docker.com/r/defano/desert-chicago-njs/) and other available images. Then we'd pull the image with `docker pull defano/desert-chicago-njs` to load it onto our host.

**Do not pull** that image from the Chicago Coder Conference; it's already been loaded for you (and it's big).

In the terminal created by the Docker Quickstart Terminal tool:

1. Execute `docker run -d -p 5000:5000 defano/desert-chicago-njs` to create and run an instance of this image (as a container), exposing port 5000 to the host.

This web app runs on port 5000, which we're mapping to port 5000 on the host. The `-d` flag tells Docker to run the container "detached" (like a background process).

View the app on a browser on your host operating system:

* **For Mac and Windows users:** Type `docker-machine ip` to determine the IP address of your Docker Machine VM and take note of this address (we'll use it throughout the examples). Then, open a browser and navigate to that IP address, port 5000 (e.g., `http://192.168.99.104:5000`). You should see the Oasis Chicago app appear in the browser.

* **For Linux users:** Since the container is running directly on your machine (and not inside of a VM), simply open a browser and go to `localhost:5000`. You should see Oasis Chicago app appear in the browser.

If you've run into trouble getting this example to work, you'll want to work through those issues before continuing as they likely indicate an installation or setup problem.

#### Example 1: Static Web Server
In this example, we'll create a Docker container from the Ubuntu distribution, install the Nginx server on it, and have it serve a single HTML document (a document that will be "baked into" the Docker image).

1. Enter the example directory: `cd webserver-example`
2. Build a Docker image from the provided Dockerfile using `docker build -t example/nginx-img .` Upon success, this will have created a new Docker image called `example/nginx-img`.
3. Create a container from the newly built image and forward port 80 of the container to port 8080 of our host: `docker create -p 8080:80 --name=nginx example/nginx-img`
4. Use `docker start nginx` to run the container.

Then...

1. See it in action with ``curl `docker-machine ip`:8080``. You should see the HTML contents of `index.html`.
2. When you're satisfied with your fine work so far, stop the container with `docker stop nginx`. If you wish, you can destroy the container using `docker rm nginx`. You could also--*but don't!*--delete the image used to create it with `docker rmi example/nginx-img` (if you delete the image you'll need to re-install it from the tarball or let Docker download it from the web).

#### Example 2: Volume Web Server
Example 1 illustrated how we could set up a web server in Docker but this was clearly limited in that it could only serve files that were baked into the container's image. This example solves that limitation by linking a portion of the host's filesystem to the container (called a _volume_). This will enable us to add or modify the contents served by Nginx without having to rebuild (or even stop) the container.

1. Enter the example directory: `cd webserver-volume-example`
2. Build the image using `docker build -t example/nginx-volume-img .`
3. Create a container from this image, `docker create -p 8080:80 --name=nginx-volume -v /path/to/example/www:/var/www example/nginx-volume-img` where `path/to/example/www` is the absolute path to the `www` directory inside this example. (Sorry Charlie, you can't use a relative path here.)
4. Start the container with `docker start nginx-volume`

Then...

1. ``curl `docker-machine ip`:8080`` to see the HTML contents of `www/index.html` returned.
2. Try modifying the contents of `www/index.html` and making the same `curl` request (you should see your changes reflected in the output)
3. Try adding new files to the `www/` directory. For example,
`echo '<html>Goodbye Cruel World</html>' > www/goodbye.html` then request ``curl `docker-machine ip`:8080/goodbye.html``
4. Stop the container using `docker stop nginx-volume` and, if desired, blow away the container and image. You do remember how to do that, right? (Hint: `docker rm` and `docker rmi`)

#### Example 3: Spring Boot Server
Creates a Docker container serving a trivial Spring Boot web application that responds to simple RESTful requests.

1. Enter the example directory: `cd spring-example`
2. Build the Docker image with `docker build -t example/spring-img .`
3. Create a container from this image using `docker create -p 8080:8080 --name spring example/spring-img`
4. Start the container: `docker start spring`

Then...

1. In another terminal window, ``curl `docker-machine ip`:8080`` to see the Spring web app produce the output "Hello World!".
2. Stop the container using `docker stop spring`

#### Example 4: Linking Spring Boot to Redis
In this example, the goal is to have our Spring Boot server read and write entries in a Redis cache that's operating in a separate container. Docker provides the concept of *container linking* for this purpose. When two containers are linked, Docker will write a host entry into the `/etc/hosts` file of the target container providing the IP address of the linked container.

1. Enter the Redis example directory: `cd redis-example`
2. Build the Redis image using `docker build -t example/redis-img .`
3. Create a container instance from the newly generated image with `docker create --name redis example/redis-img`
4. Start the container with `docker start redis`. Note that we're not exposing any of the container's ports to the host operating system. Nevertheless, the Redis port (6379) will be accessible from the Spring Boot server which we will link to it.
5. Return to the Spring Boot example: `cd ../spring-example`
6. Start the Spring Boot server and link it to the Redis container (already running) using `docker run --link redis:redishost -d -p 8080:8080 --name spring-redis example/spring-img`. (Since the Spring Boot example image has already been created, we don't need to re-run the `build` command.)

At this point, our web container should be able to delegate to our Redis container for caching key/value pairs.

Then...

1. Smoke test our web server by fetching the "Hello Work" page just as we did in the last exercise, ``curl `docker-machine ip`:8080``.
2. Put a key/value pair into the Redis cache with ``curl "`docker-machine ip`:8080/set?key=mykey&value=myvalue"``. If everything is working, the server should respond with `Set: mykey=myvalue`. <br>_**Gotcha:**_ Note the use of quotes around the curl command; this is required to escape `&` from the shell.
3. Verify that Redis has accepted our key/value pair by reading it back, ``curl `docker-machine ip`:8080/get?key=mykey'``. The server should respond with `Get: mykey=myvalue`
4. If you like, shut down both containers with `docker stop redis spring-redis`

**What is `--link` actually doing?** Link takes the name of a running container and a hostname and assigns the IP address of the container to that hostname. Docker accomplishes this by writing a record into the linked container's `/etc/hosts`, file like:

```
172.17.0.9     b8a2dc66e465
127.0.0.1      localhost
::1            localhost ip6-localhost ip6-loopback
192.168.1.90   redishost
```

In this case, our Redis container is going to be linked into the `spring` container using the hostname `redishost`. If you were to open a shell in the web server's container (use `docker exec -it spring bash` to do so) and examine the `/etc/host` file, you'd find a `redisthost` entry similar to that shown in the last line above. With this host entry in place, our Spring Boot web app container can reach the Redis container by referring to it's hostname `redishost`. Neat-o!

#### Example 5: Orchestrating containers with Docker Compose
Linking containers is pretty cool. That said, it's not hard to imagine how this could become quite unmanageable as the number of containers that need to communicate with one another grow within a distributed system. Docker provides the Compose tool for scripting the creation and linking of containers (very handy for development and CI environments).

Lets reproduce the Redis / Spring Web App system we created in the last example, but provision it with Docker Compose.

1. Enter the `compose-example` directory, `cd compose-example`.
2. Examine the contents of the `docker-compose.yml` file in this directory; this file scripts how to build and link the Spring Boot and Redis containers.
3. Fire up the system with `docker-compose up`. Provided all goes according to plan, you'll see both containers start up in "attached" mode (that is, their output is written to your terminal). Passing `-d` will run them in "detached" mode (as we've been doing when starting these containers manually).

Then...

1. Verify that both containers are running and linked just as you did in the last example: ``curl "`docker-machine ip`:8080/set?key=k&value=v"`` followed by ``curl `docker-machine ip`:8080/get?key=k'``

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
* `docker ps -a` will show all containers (in any execution state) on the local machine.
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
