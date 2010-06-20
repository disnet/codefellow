#! /bin/bash
java -cp \
    ../project/boot/scala-2.8.0.RC5/lib/scala-library.jar:../project/boot/scala-2.8.0.RC5/lib/scala-compiler.jar:../codefellow-core/lib_managed/scala_2.8.0.RC5/compile/bcel-5.2.jar:../codefellow-core/target/scala_2.8.0.RC5/classes \
    de.tuxed.codefellow.Client \
    "/home/roman/Dateien/Projekte/workspace/codefellow/testproject/project2/src/main/scala/Project2.scala" \
    CompleteScope \
    "/home/roman/Dateien/Projekte/workspace/codefellow/testproject/project2/src/main/scala/Project2.scala" \
    141 \
    ""

