java \
   -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 \
   --enable-native-access=ALL-UNNAMED \
   -cp out/production/text-editor-tutorial \
    editor.Editor $1
