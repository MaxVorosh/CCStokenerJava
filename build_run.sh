javac -cp ./src/main/java -d ./build ./src/main/java/Runner.java
cd build
java Runner ./../small_tokens ./../small_report.txt
