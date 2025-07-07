import java.io.File;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Tests {

    private boolean compareFile(String firstPath, String secondPath) {
        File firstFile = new File(firstPath);
        File secondFile = new File(secondPath);
        try {
            Scanner firstScanner = new Scanner(firstFile);
            Scanner secondScanner = new Scanner(secondFile);
            while (firstScanner.hasNextLine() && secondScanner.hasNextLine()) {
                String firstLine = firstScanner.nextLine();
                String secondLine = secondScanner.nextLine();
                if (!firstLine.equals(secondLine)) {
                    firstScanner.close();
                    secondScanner.close();
                    return false;
                }
            }
            boolean result = ((!firstScanner.hasNextLine()) && (!secondScanner.hasNextLine()));
            firstScanner.close();
            secondScanner.close();
            System.out.println(result);
            return result;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Test
    public void OverallTest() {
        String[] args = new String[]{"./src/test/testData/programs"};
        Runner.main(args);
        Assertions.assertTrue(compareFile("./src/test/testResults/891.txt", "./tokens/891.txt"));
        Assertions.assertTrue(compareFile("./src/test/testResults/clonepairs.txt", "./clonepairs.txt"));
    }
}