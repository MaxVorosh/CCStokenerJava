mkdir index
mkdir tokens
sh gradlew run --args=\'$1\'
rm -rf index
rm -rf tokens