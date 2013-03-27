# cljskel

A Leiningen template that creates a new Clojure web app implementing /1.x/ping and /1.x/status.

## Usage

```
git clone ssh://snc@source.nokia.com/Entertainment-Tooling/git/cljskel
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

Copyright © 2012 Nokia

