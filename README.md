# mr-clojure

MixRadio's clojure skeleton project.

This generates the base REST service project that we use at MixRadio. In contains various libraries that we find useful and can be considered a condensing of the various pieces of commonality between our many clojure web services. We use this every day at MixRadio to power our music backend.

This project is the underlying Leiningen template, see ::TODO - add link to project:: for an example of the generated service.

## Usage

Generate a new project with:

`lein new mr-clojure <your-project-name>`

You now have a web service with a couple of basic "I'm alive" resources and an embedded Jetty server to run it.

### Testing your new service

`cd` into your new project directory and run

`./acceptance wait`

This starts the service in acceptance test mode but rather than running the tests and reporting the result, it just starts the service and waits. This allows you to manually call it or to run tests against it from the repl.

Let's call the healthcheck resource and see if we get a response:

`curl localhost:8080/healthcheck`

and we should see the result:

`{"name":"i","version":"1.0.1-SNAPSHOT","success":true,"dependencies":[]}`

## Adding in a new resource

Let's just add a quick example of a resource that responds to an input parameter. Leave the server running as we're going to do this live.

Open `src/<your-project-name>/web.clj` and navigate to the route definitions at the bottom of the file. Add a new route on the resource `/hello` and tell it to call a function called greet and take a parameter of `name`. As a rule of thumb we like to keep our route definitions clean from too much code as it makes it very easy to see at a glace what a service does. The main way to do this to immediately defer to a handling function.

Here's how it should look:

```
(defroutes routes

  (GET "/healthcheck"
       [] (healthcheck))

  (GET "/ping"
       [] "pong")

  (GET "/hello"
       [nickname] (greet nickname))

  (route/not-found (error-response "Resource not found" 404)))

```

And now let's define our greet function:

```
(defn greet
  "Says hello!"
  [nickname]
  {:status 200 :body (format "Hello %s!\n" nickname)})
```

Save the file and give it a test:

`curl localhost:8080/hello?nickname=world`

Should give us the output:

`Hello world!`

Success!

## Testing

There are a few different types of test defined in our skeleton project: unit, acceptance and integration. These words mean different things to different people and our definition is no exception so I'll define it here.
* Unit - This one is pretty well defined. However unlike a lot of people we often test our web layer heavily using unit like tests where we don't start the server. There are examples of this in `test/unit/web.clj`
* Acceptance - Our definition here is probably one that suits our service oriented architecture. To us acceptance tests mean starting the service and poking it from another process but only in isolation. That it was use [rest driver](http://github.com/whostolebenfrog/rest-cljer) to mock out our calls to other web services. Some teams use this heavily where others lean more towards web unit test.
* Integration - Many people refer to this as an acceptance test. This is where the service is started and called with all its dependencies.

Each of these can be run in isolation using:

`lein midje :filter unit`

`./acceptance` or `./acceptance wait` to only start the server and not run the tests

`./integration` or `./integration wait` to only start the server and not run the tests

`./all` to run all the tests

## Enabling SSL

It's also possible to create a service that accepts SSL connections. There are a few steps that need
to be taken to do this and you'll need to have knowledge of how SSL works and how to set it up. The
example here uses the jetty test keystore and credentials, so don't use these in a production system!

1. Copy the jetty test keystore (from the root of this project) to a location on your machine, 
   for example to the root of the service you are creating.

2. Add the following entries to the properties defined when calling run-jetty in the start-server
   function in the setup.clj file of your new service:

      `:ssl-port 8443
      :keystore "path-to-keystore-file"
      :key-password "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4"
      :key-mgr-password "OBF:1u2u1wml1z7s1z7a1wnl1u2g"
      :truststore "path-to-keystore-file"
      :trust-password "OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4"`

   If you place the keystore file in the root of the service, you can just set path-of-keystore-file
   to be the value `keystore`. If it's in another location, specify the full path to the file.

3. Run `acceptance wait` and you'll see logging that shows both an HTTP and an HTTPS ServerConnector
   being created.

4. `curl http://localhost:8080/healthcheck` and you'll see a response over a non-SSL connection.

5. `curl -v --insecure https://localhost:8443/healthcheck` and you'll see a response over a SSL
   connection.  `--insecure` means don't worry about the test certificate being invalid and `-v` 
   means that you'll see the various details of SSL protocol negotiation.

## Libraries

These are some of the libraries that we use:

* [cheshire](https://github.com/dakrone/cheshire) - excellent and fast JSON parsing and generation
* [clj-http](https://github.com/dakrone/clj-http) - a nice interface for making http calls
* [clj-time](https://github.com/clj-time/clj-time) - date and time library with a great interface
* [clojure](http://clojure.org) - 1.6
* [compojure](https://github.com/weavejester/compojure) - the basis of our web service, used to define resources and middleware
* [environ](https://github.com/weavejester/environ) - reads environment variables and allows development values to be defined in the project.clj
* [jetty](http://www.eclipse.org/jetty/) - a lightweight JVM web server that we embed with our services
* [midje](https://github.com/marick/Midje) - testing and mocking
* [rest-driver](https://github.com/whostolebenfrog/rest-cljer) - http level dependency mocking

## Deployment

`lein release` will update the version number, uberjar and then create an RPM of the service. This RPM can be installed on any compatible server (e.g. Redhat, CentOs, Amazon Linux). You will, however need to ensure that the environment variables defined in the project.clj are available for the RPM to run.

## License

Copyright © 2014 MixRadio

[Radix is released under the 3-clause license ("New BSD License" or "Modified BSD License")](https://github.com/mixradio/radix/blob/master/LICENSE)
