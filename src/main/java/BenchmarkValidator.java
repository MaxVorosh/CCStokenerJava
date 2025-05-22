import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.HashSet;

public class BenchmarkValidator {

    HashSet<String> used;

    BenchmarkValidator() {
        used = new HashSet<>();
        File file = new File("UsedFiles");
        try {
            Scanner sc = new Scanner(file);
            String last = "";
            while (sc.hasNextLine()) {
                last = sc.nextLine();
                used.add(last);
            }
            sc.close();
            FileWriter fw = new FileWriter("badFiles", true);
            fw.write(last + "\n");
            fw.close();
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }
    
    void runBenchmarkDir(String path, String pref) {
        File dir = new File(path);
        File[] listOfFiles = dir.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    runBenchmark(file.getPath());
                }
                else if (file.isDirectory()) {
                    runBenchmarkDir(file.getPath(), pref + file.getName() + "-");
                }
            }
        }
    }

    void runBenchmark(String name) {
        if (used.contains(name)) {
            return;
        }
        try {
            FileWriter fw = new FileWriter("UsedFiles", true);
            fw.write(name + "\n");
            fw.close();
        }
        catch (Exception e) {
            System.err.println(e);
        }
        try {
            ASTBuilder astb = new ASTBuilder();
            astb.buildAsts(name, "java");
        }
        catch (Exception e) {
            System.err.println(e);
        }
    }
}
