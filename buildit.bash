if [  -f ~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/javac ] ; then 
   export JAVAC=~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/javac
fi

${JAVAC} \
   -d out/production/text-editor-tutorial \
   -g \
   --source-path src/java/main \
   --class-path thirdparty/jna-5.17.0.jar\
   src/java/main/editor/Editor.java 
