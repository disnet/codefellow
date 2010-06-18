#! /bin/bash

#sbt compile && cp -r codefellow-plugin/target/scala_2.7.7/classes/* testproject/project/plugins/target/scala_2.7.7/plugin-classes/ && cd testproject/ && sbt codefellow && cd ..

sbt publish-local && cd testproject && rm project/plugins/project/build.properties && sbt codefellow && cd ..

