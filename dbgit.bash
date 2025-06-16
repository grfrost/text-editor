if [  -f ~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/java ] ; then 
   export JAVA=~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/java
fi
${JAVA} \
   --add-modules jdk.internal.le \
   --add-exports jdk.internal.le/jdk.internal.org.jline.terminal=ALL-UNNAMED \
   --add-exports jdk.internal.le/jdk.internal.org.jline.utils=ALL-UNNAMED \
   -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
   --enable-native-access=ALL-UNNAMED \
   -cp thirdparty/jna-5.17.0.jar:out/production/text-editor-tutorial \
    editor.Editor $1
