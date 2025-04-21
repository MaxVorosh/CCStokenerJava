import ch.usi.si.seart.treesitter.*;
import ch.usi.si.seart.treesitter.printer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.Map;

public class ASTBuilder {

    Map<String, Language> langDict;

    static {
        LibraryLoader.load();
    }

    ASTBuilder() {
        langDict = new HashMap<>();
        langDict.put("java", Language.JAVA);
    }

    public ASTNode buildAsts(String path, String langName) {
        Language lang = langDict.get(langName);
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
        String textStr = String.join("\n", text);
        // System.out.println(textStr);
        String ast;
        try {
            Parser parser = Parser.getFor(lang);
            Tree tree = parser.parse(textStr);
            TreeCursor cursor = tree.getRootNode().walk();
            SyntaxTreePrinter printer = new SyntaxTreePrinter(cursor);
            ast = printer.print();
            System.out.println(ast);
        } catch (Exception ex) {
            return null;
        }
        ASTNode startNode = null;
        ASTNode curNode = null;
        String[] astLines = ast.split("\n");
        int level = -1;
        for (String line : astLines) {
            int currentLevel = getLevel(line);
            // System.out.println(currentLevel);
            // System.out.println(level);
            // System.out.println("");
            while (currentLevel - 1 != level) {
                curNode = curNode.parent;
                level--;
            }
            String lastInfo = "";
            if (curNode != null) {
                lastInfo = curNode.getMetaInfo();
            }
            ASTNode newNode = parseLine(lang, text, line, lastInfo);
            if (startNode == null) {
                startNode = newNode;
                curNode = startNode;
                level = currentLevel;
            }
            else {
                curNode.children.add(newNode);
                newNode.parent = curNode;
                curNode = newNode;
                level = currentLevel;
            }
        }
        return startNode;
    }

    private ASTNode parseLine(Language lang, Vector<String> text, String line, String prevType) {
        if (lang == Language.JAVA) {
            return parseJavaLine(text, line, prevType);
        }
        return null;
    }

    private ASTNode parseJavaLine(Vector<String> text, String line, String prevType) {
        String[] args = line.split(" ");
        String type = args[0];
        if (type == "left:" || type == "right:") {
            args = Arrays.copyOfRange(args, 1, args.length);
            type = args[0];
        }
        switch (type) {
            case "method_declaration":
                return new InnerNode(NodeType.METHOD_DEF);
            case "assignment_expression":
                return new InnerNode(NodeType.ASSIGN_EXPR);
            case "identifier":
                String[] pos1 = args[1].substring(1, args[1].length() - 1).split(":");
                String[] pos2 = args[3].substring(1, args[3].length() - 1).split(":");
                int lineNumber = Integer.parseInt(pos1[0]);
                int start = Integer.parseInt(pos1[1]);
                int end = Integer.parseInt(pos2[1]);
                String name = text.get(lineNumber).substring(start, end);
                return new IdentifierNode(name);
            case "program":
                return new InnerNode(NodeType.ENTRY);
            case "condition:":
                switch (prevType) {
                    case "if_statement":
                        return new InnerNode(NodeType.IF_COND);
                    case "while_statement":
                        return new InnerNode(NodeType.LOOP_COND);
                }
                return new UnknownNode(args[1]);
            case "body:":
                return new InnerNode(NodeType.LOOP_BODY);
            case "consequence:":
                return new InnerNode(NodeType.IF_ELSE_BODY);
            case "alternative:":
                return new InnerNode(NodeType.IF_ELSE_BODY);
        }
        return new UnknownNode(type);
    }

    private int getLevel(String line) {
        int c = 0;
        while (c < line.length() && line.charAt(c) == ' ') {
            c++;
        }
        return c / 2;
    }
}
