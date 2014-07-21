# cljskel

A Leiningen template that creates a new Clojure web app implementing /ping and /healthcheck.

## Usage

```
git clone git@github.brislabs.com:platform/cljskel.git
cd  cljskel
lein install
cd ..
lein new cljskel <your project name>
```

Before trying to build your new project you will also need to ensure that leiningen does not use the proxy for pulling dependencies from Nexus:

```
export http_no_proxy=localhost|*.brislabs.com
```

## License

Copyright © 2012-2013 Nokia

