import ch.usi.si.seart.treesitter.*;
import ch.usi.si.seart.treesitter.printer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ASTBuilder {

    static {
        LibraryLoader.load();
    }
    public static void main(String[] args) {
        String path = "./code_examples/GCD.java";
        File code_file = new File(path);
        StringBuilder text = new StringBuilder();
        try {
            Scanner sc = new Scanner(code_file);
            while (sc.hasNextLine()) {
                text.append(sc.nextLine());
                text.append("\n");
            }
        } catch (FileNotFoundException e) {}
        System.out.println(text);
        try {
            System.out.println("Aboba");
            Parser parser = Parser.getFor(Language.JAVA);
            System.out.println(parser.getLanguage());
            Tree tree = parser.parse(text.toString());
            TreeCursor cursor = tree.getRootNode().walk();
            SyntaxTreePrinter printer = new SyntaxTreePrinter(cursor);
            String actual = printer.print();
            System.out.println(actual);
//            String expected =
//                    "module [0:0] - [0:11]\n" +
//                            "  expression_statement [0:0] - [0:11]\n" +
//                            "    call [0:0] - [0:11]\n" +
//                            "      function: identifier [0:0] - [0:5]\n" +
//                            "      arguments: argument_list [0:5] - [0:11]\n" +
//                            "        string [0:6] - [0:10]\n";
//            assert expected.equals(actual);
        } catch (Exception ex) {
            // ...
        }
    }
}
