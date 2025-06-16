if [  -f ~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/javac ] ; then 
   export JAVAC=~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/javac
fi

${JAVAC} \
   --add-modules jdk.internal.le \
   --add-exports jdk.internal.le/jdk.internal.org.jline.terminal=ALL-UNNAMED \
   --add-exports jdk.internal.le/jdk.internal.org.jline.utils=ALL-UNNAMED \
   -d out/production/text-editor-tutorial \
   -g \
   --source-path src/java/main \
   --class-path thirdparty/jna-5.17.0.jar\
   src/java/main/editor/Editor.java 
