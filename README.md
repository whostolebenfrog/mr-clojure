# Mr-Clojure

MixRadio's clojure skeleton project.

This generates the base REST service project that we use at MixRadio. In contains various libraries that we find useful and can be considered a condensing of the various pieces of commonality between our many clojure web services. We use this every day at MixRadio to power our music backend.

This project is the underlying Leiningen template, see [mr-clojure-exploded](http://github.com/mixradio/mr-clojure-exploded) for an example of the resulting service.

## Usage

Generate a new project with:

`lein new mr-clojure <your-project-name>`

You now have a web service with a couple of basic "I'm alive" resources and an embedded Jetty server to run it.

### Testing your new service

`cd` into your new project directory and run

`./acceptance wait`

This starts the service in acceptance test mode, but rather than running the tests and reporting the result, it just starts the service and waits. This allows you to manually call it or to run tests against it from the repl.

Let's call the healthcheck resource and see if we get a response:

`curl localhost:8080/healthcheck`

and we should see the result:

`{"name":"i","version":"1.0.0-SNAPSHOT","success":true,"dependencies":[]}`

## Adding in a new resource

Let's just add a quick example of a resource that responds to an input parameter. Leave the server running as we're going to do this live.

Open `src/<your-project-name>/web.clj` and navigate to the route definitions at the bottom of the file. Add a new route on the resource `/hello` and tell it to call a function called greet and take a parameter of `name`. As a rule of thumb we like to keep our route definitions clean from too much code as it makes it very easy to see at a glace what a service does. The main way to do this to immediately defer to a handling function.

Here's how it should look:

```clj
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

```clj
(defn greet
  "Says hello!"
  [nickname]
  {:status 200 :body (format "Hello %s!\n" nickname)})
```

Save the file and give it a test:

`curl "http://localhost:8080/hello?nickname=world"`

Should give us the output:

`Hello world!`

Success!

## Testing

There are a few different types of test defined in our skeleton project: unit, acceptance and integration. These words mean different things to different people and our definition is no exception so I'll define it here.
* Unit - This one is pretty well defined. However unlike a lot of people we often test our web layer heavily using unit like tests where we don't start the server. There are examples of this in `test/unit/web.clj`
* Acceptance - Our definition here is probably one that suits our service oriented architecture. To us acceptance tests mean starting the service and poking it from another process, but only in isolation. This is when we use [rest driver](http://github.com/whostolebenfrog/rest-cljer) to mock out our calls to other web services. Some teams use this heavily where others lean more towards web unit test.
* Integration - Many people refer to this as an acceptance test. This is where the service is started and called with all its dependencies.

Each of these can be run in isolation using:

`lein midje :filter unit`

`./acceptance` or `./acceptance wait` to only start the server and not run the tests

`./integration` or `./integration wait` to only start the server and not run the tests

`./all` to run all the tests

### Running a single test in isolation
If you add metadata to a specific integration/acceptance test (or tests) and then tell midje to run
just those tests with that metadata. For example, consider the following acceptance test:

```clojure
(fact "test something"
    :only
    (test-stuff))
```
you can now run that test in isolation with `./acceptance only`.

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

[mr-clojure is released under the 3-clause license ("New BSD License" or "Modified BSD License")](https://github.com/mixradio/mr-clojure/blob/master/LICENSE)
