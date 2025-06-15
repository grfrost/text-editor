if [  -f ~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/java ] ; then 
   export JAVA=~/github/babylon-grfrost-fork/build/linux-x86_64-server-release/jdk/bin/java
fi
   
${JAVA} \
   --enable-native-access=ALL-UNNAMED \
   -cp thirdparty/jna-5.17.0.jar:out/production/text-editor-tutorial \
    editor.Editor $1
