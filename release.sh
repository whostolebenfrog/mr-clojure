#!/bin/sh
# Making this script because we can't accept a password during `lein release` and don't want to add keys IDs to project.clj

lein vcs assert-committed
lein change version leiningen.release/bump-version release
lein vcs commit
git tag `cat project.clj | grep defproject | cut -d" " -f 3 | tr -d "\""` # I'm actually pretty happy with this
lein deploy clojars
lein change version leiningen.release/bump-version
lein vcs commit
lein vcs push
