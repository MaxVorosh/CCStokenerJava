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
    Map<String, NodeType> nodeTypes;

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

        nodeTypes = new HashMap<>();
        fillTypes();
    }

    private void fillTypes() {
        nodeTypes.put("method_declaration", NodeType.METHOD_DEF);
        nodeTypes.put("constructor_declaration", NodeType.METHOD_DEF);
        nodeTypes.put("assignment_expression", NodeType.ASSIGN_EXPR);
        nodeTypes.put("consequence:", NodeType.IF_ELSE_BODY);
        nodeTypes.put("alternative:", NodeType.IF_ELSE_BODY); //TODO in original instrument this does not exist
        nodeTypes.put("local_variable_declaration", NodeType.VAR_DECL);
        nodeTypes.put("array_access", NodeType.ARRAY_SELECTOR);
        nodeTypes.put("method_invocation", NodeType.METHOD_INVOC);
        nodeTypes.put("return_statement", NodeType.RETURN_STMT);
        nodeTypes.put("switch_block_statement_group", NodeType.CASE_BODY);
        nodeTypes.put("throw_statement", NodeType.THROW_BODY);
        nodeTypes.put("catch_close", NodeType.CATCH_BODY);
        nodeTypes.put("finally_close", NodeType.FINALLY_BODY);
        nodeTypes.put("array_creation_expression", NodeType.CLASS_ARRAY_CREATOR);
        nodeTypes.put("object_creation_expression", NodeType.CLASS_ARRAY_CREATOR);
        nodeTypes.put("lambda_expression", NodeType.LAMBDA_EXPR);
    }

    public ASTNode buildAsts(String path, String langName) {
        // System.out.println(path);
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
        String ast;
        try {
            Parser parser = Parser.getFor(lang);
            Tree tree = parser.parse(textStr);
            TreeCursor cursor = tree.getRootNode().walk();
            SyntaxTreePrinter printer = new SyntaxTreePrinter(cursor);
            ast = printer.print();
            // System.out.println(ast);
        } catch (Exception ex) {
            return null;
        }
        ASTNode startNode = null;
        ASTNode curNode = null;
        String[] astLines = ast.split("\n");
        int level = -1;
        for (String line : astLines) {
            int currentLevel = getLevel(line);
            line = line.strip();
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
        if (info.startsWith("expr ")) {
            String op = info.split(" ")[1].strip();
            NodeType type = NodeType.NUMERIC_EXPR;
            if (opTypes.containsKey(op)) {
                type = opTypes.get(op);
            }
            ASTNode node = new InnerNode(type, -1, -1);
            node.parent = root.parent;
            node.parent.children.set(index, node);
            for (ASTNode child : root.children) {
                node.children.add(child);
            }
            root = node;
        }
        if (root.parent != null && root.parent.getMetaInfo() == "assert_statement") {
            NodeType type = NodeType.ASSERT_BODY;
            if (index == 0) {
                type = NodeType.ASSERT_COND;
            }
            ASTNode betweenNode = new InnerNode(type, -1, -1);
            betweenNode.children.add(root);
            root.parent.children.set(index, betweenNode);
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
        if (type.equals("left:") || type.equals("right:")) {
            if (prevNode instanceof UnknownNode) {
                String from = args[args.length - 3];
                String to = args[args.length - 1];
                String[] prevArgs = prevNode.getMetaInfo().split(" ");
                if (type.equals("left:")) {
                    prevArgs[prevArgs.length - 3] = to;
                    prevNode.setMetaInfo(String.join(" ", prevArgs));
                }
                else {
                    int[] r = getRange(prevArgs[prevArgs.length - 3], from);
                    if (r[1] >= r[2]) {
                        // System.err.println("Parsing error");
                    }
                    else {
                        prevNode.setMetaInfo("expr " + text.get(r[0]).substring(r[1], r[2]));
                    }
                }
            }
            args = Arrays.copyOfRange(args, 1, args.length);
            line = String.join(" ", args);
            type = args[0];
        }
        if (type.equals("type:") || (type.equals("name:") && prevNode instanceof InnerNode && 
            (((InnerNode)prevNode).type == NodeType.METHOD_DEF || ((InnerNode)prevNode).type == NodeType.METHOD_INVOC))) {
            String from = args[args.length - 3];
            String to = args[args.length - 1];
            int[] r = getRange(from, to);
            String name = text.get(r[0]).substring(r[1], r[2]);
            return new ActionTokenNode(name);
        }
        if (type.equals("value:") || type.equals("name:") || type.equals("array:")) {
            args = Arrays.copyOfRange(args, 1, args.length);
            line = String.join(" ", args);
            type = args[0];
        }
        int[] lines = getRangeLines(args[args.length - 3], args[args.length - 1]);
        if (nodeTypes.containsKey(type)) {
            return new InnerNode(nodeTypes.get(type), lines[0], lines[1]);
        }
        switch (type) {
            case "identifier":
                int[] r = getRange(args[1], args[3]);
                String name = text.get(r[0]).substring(r[1], r[2]);
                return new IdentifierNode(name);
            case "condition:":
                switch (prevNode.getMetaInfo()) {
                    case "if_statement":
                        return new InnerNode(NodeType.IF_COND, lines[0], lines[1]);
                    case "while_statement":
                        return new InnerNode(NodeType.LOOP_COND, lines[0], lines[1]);
                    case "switch_expression":
                        return new InnerNode(NodeType.SWITCH_CONDITION, lines[0], lines[1]);
                }
                return new UnknownNode(line);
            case "body:":
                if (prevNode instanceof InnerNode) {
                    switch (((InnerNode)prevNode).type) {
                        case LOOP_COND:
                            return new InnerNode(NodeType.LOOP_BODY, lines[0], lines[1]);
                        default:
                            return new UnknownNode(line);
                    }
                }
                switch (prevNode.getMetaInfo()) {
                    case "switch_expression":
                       return new InnerNode(NodeType.SWITCH_BODY, lines[0], lines[1]);
                    case "try_statement":
                        return new InnerNode(NodeType.TRY_BODY, lines[0], lines[1]);
                }
                return new UnknownNode(line);
            case "binary_expression":
                return new UnknownNode(line);
        }
        return new UnknownNode(line);
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

    private int[] getRangeLines(String s1, String s2) {
        String[] pos1 = s1.substring(1, s1.length() - 1).split(":");
        String[] pos2 = s2.substring(1, s2.length() - 1).split(":");
        int lineNumber1 = Integer.parseInt(pos1[0]);
        int lineNumber2 = Integer.parseInt(pos2[0]);
        return new int[]{lineNumber1, lineNumber2};
    }
}
