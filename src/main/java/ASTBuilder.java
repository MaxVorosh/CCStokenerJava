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
    Map<String, NodeType> opTypes;

    static {
        LibraryLoader.load();
    }

    ASTBuilder() {
        langDict = new HashMap<>();
        langDict.put("java", Language.JAVA);

        String[] logical = new String[]{">", "<", ">=", "<=", "==", "!="};
        String[] conditional = new String[]{"|", "||", "&", "&&", "^", "!"};
        opTypes = new HashMap<>();
        for (String log : logical) {
            opTypes.put(log, NodeType.LOGICAL_EXPR);
        }
        for (String cond : conditional) {
            opTypes.put(cond, NodeType.CONDITION_EXPR);
        }
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
            ASTNode newNode = parseLine(lang, text, line, curNode);
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
        updateAST(startNode, lang);
        return startNode;
    }

    private void updateAST(ASTNode root, Language lang) {
        if (lang == Language.JAVA) {
            updateASTJava(root, 0);
            return;
        }
    }

    private void updateASTJava(ASTNode root, int index) {
        String info = root.getMetaInfo();
        if (info.startsWith("expr")) {
            String op = info.split(" ")[1];
            NodeType type = NodeType.NUMERIC_EXPR;
            if (opTypes.containsKey(op)) {
                type = opTypes.get(op);
            }
            ASTNode node = new InnerNode(type);
            node.parent = root.parent;
            node.parent.children.set(index, node);
            for (ASTNode child : root.children) {
                node.children.add(child);
            }
            root = node;
        }
        for (int i = 0; i < root.children.size(); ++i) {
            updateASTJava(root.children.get(i), i);
        }
    }

    private ASTNode parseLine(Language lang, Vector<String> text, String line, ASTNode prevNode) {
        if (lang == Language.JAVA) {
            return parseJavaLine(text, line, prevNode);
        }
        return null;
    }

    private ASTNode parseJavaLine(Vector<String> text, String line, ASTNode prevNode) {
        String[] args = line.split(" ");
        String type = args[0];
        if (type == "left:" || type == "right:") {
            String from = args[args.length - 3];
            String to = args[args.length - 1];
            String[] prevArgs = prevNode.getMetaInfo().split(" ");
            if (type == "left:") {
                prevArgs[prevArgs.length - 3] = to;
                prevNode.setMetaInfo(String.join(" ", prevArgs));
            }
            else {
                int[] r = getRange(prevArgs[prevArgs.length - 3], from);
                prevNode.setMetaInfo("expr " + text.get(r[0]).substring(r[1] + 1, r[2] - 1));
            }
            args = Arrays.copyOfRange(args, 1, args.length);
            type = args[0];
        }
        switch (type) {
            case "method_declaration":
                return new InnerNode(NodeType.METHOD_DEF);
            case "assignment_expression":
                return new InnerNode(NodeType.ASSIGN_EXPR);
            case "identifier":
                int[] r = getRange(args[1], args[3]);
                String name = text.get(r[0]).substring(r[1], r[2]);
                return new IdentifierNode(name);
            case "program":
                return new InnerNode(NodeType.ENTRY);
            case "condition:":
                switch (prevNode.getMetaInfo()) {
                    case "if_statement":
                        return new InnerNode(NodeType.IF_COND);
                    case "while_statement":
                        return new InnerNode(NodeType.LOOP_COND);
                    case "switch_expression":
                        return new InnerNode(NodeType.SWITCH_CONDITION);
                }
                return new UnknownNode(args[1]);
            case "body:":
                if (prevNode instanceof InnerNode) {
                    switch (((InnerNode)prevNode).type) {
                        case LOOP_COND:
                            return new InnerNode(NodeType.LOOP_BODY);
                        default:
                            return new UnknownNode(type);
                    }
                }
                if (prevNode.getMetaInfo() == "switch_expression") {
                    return new InnerNode(NodeType.SWITCH_BODY);
                }
                return new UnknownNode(type);
            case "consequence:":
                return new InnerNode(NodeType.IF_ELSE_BODY);
            case "alternative:":
                return new InnerNode(NodeType.IF_ELSE_BODY);
            case "local_variable_declaration":
                return new InnerNode(NodeType.VAR_DECL);
            case "binary_expression":
                return new UnknownNode(line);
            case "array_access":
                return new InnerNode(NodeType.ARRAY_SELECTOR);
            case "method_invocation":
                return new InnerNode(NodeType.METHOD_INVOC);
            case "return_statement":
                return new InnerNode(NodeType.RETURN_STMT);
            case "switch_block_statement_group":
                return new InnerNode(NodeType.CASE_BODY);
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

    private int[] getRange(String s1, String s2) {
        String[] pos1 = s1.substring(1, s1.length() - 1).split(":");
        String[] pos2 = s2.substring(1, s2.length() - 1).split(":");
        int lineNumber = Integer.parseInt(pos1[0]);
        int start = Integer.parseInt(pos1[1]);
        int end = Integer.parseInt(pos2[1]);
        return new int[]{lineNumber, start, end};
    }
}
