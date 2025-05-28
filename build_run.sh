rm -rf index
rm -rf indexForSmall
rm -rf tokens
mkdir index
mkdir indexForSmall
mkdir tokens
sh gradlew run --args=\'$1\' --no-daemon -Dorg.gradle.jvmargs="-Xms1024m -Xmx2048m"
rm -rf index
rm -rf indexForSmall
rm -rf tokens