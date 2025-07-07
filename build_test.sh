rm -rf index
rm -rf indexForSmall
rm -rf tokens
rm -rf clonepairs.txt
mkdir index
mkdir indexForSmall
mkdir tokens
sh gradlew test
rm -rf index
rm -rf indexForSmall
rm -rf tokens