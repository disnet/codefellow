#! /bin/bash

sbt publish-local && cd testproject && rm project/plugins/project/build.properties && sbt codefellow && cd ..

