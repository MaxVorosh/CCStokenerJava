import ch.usi.si.seart.treesitter.*;
import ch.usi.si.seart.treesitter.printer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

public class ASTBuilder {

    static {
        LibraryLoader.load();
    }
    public static void main(String[] args) {
        // String path = "./code_examples/GCD.java";
        // buildAsts(path, Language.JAVA);
    }

    public ASTNode buildAsts(String path, Language lang) {
        File code_file = new File(path);
        Vector<String> text = new Vector<>();
        try {
            Scanner sc = new Scanner(code_file);
            while (sc.hasNextLine()) {
                text.add(sc.nextLine());
            }
            sc.close();
        } catch (FileNotFoundException e) {
            return null;
        }

        String ast;
        try {
            Parser parser = Parser.getFor(lang);
            Tree tree = parser.parse(text.toString());
            TreeCursor cursor = tree.getRootNode().walk();
            SyntaxTreePrinter printer = new SyntaxTreePrinter(cursor);
            ast = printer.print();
        } catch (Exception ex) {
            return null;
        }
        ASTNode startNode = null;
        ASTNode curNode = null;
        String[] astLines = ast.split("\n");
        int level = 0;
        for (String line : astLines) {
            int currentLevel = getLevel(line);
            ASTNode newNode = parseLine(lang, text, line);
            if (newNode == null) {
                continue;
            }
            if (startNode == null) {
                startNode = newNode;
                curNode = startNode;
                level = currentLevel;
            }
            else {
                while (currentLevel - 1 != level) {
                    curNode = curNode.parent;
                }
                curNode.children.add(newNode);
                curNode = newNode;
            }
        }
        return startNode;
    }

    private ASTNode parseLine(Language lang, Vector<String> text, String line) {
        if (lang == Language.JAVA) {
            return parseJavaLine(text, line);
        }
        return null;
    }

    private ASTNode parseJavaLine(Vector<String> text, String line) {
        return null;
    }

    private int getLevel(String line) {
        int c = 0;
        while (c < line.length() && line.charAt(c) == ' ') {
            c++;
        }
        return c / 2;
    }
}
