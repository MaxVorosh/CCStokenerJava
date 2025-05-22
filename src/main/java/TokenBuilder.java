import java.util.HashSet;
import java.util.Vector;

public class TokenBuilder {

    int n; // variable for n-grams
    Vector<MethodTokens> methods;
    HashSet<String> banned;

    TokenBuilder(int n) {
        this.n = n;
        methods = new Vector<>();
        banned = new HashSet<>();
        banned.add("103321.java");
        banned.add("47742.java");
        banned.add("123414.java");
    }

    void buildTokens(String path, String lang) {
        String[] parts = path.split("/");
        if (banned.contains(parts[parts.length - 1])) {
            return;
        }
        ASTBuilder astb = new ASTBuilder();
        ASTNode root;
        try {
            root = astb.buildAsts(path, lang);
        }
        catch (Exception e) {
            System.err.println(e);
            return;
        }
        if (root == null) {
            return;
        }
        parseMethods(root, path);
        for (MethodTokens method : methods) {
            method.computeSemanticTokens();
        }
    }

    void parseMethods(ASTNode root, String path) {
        if (root instanceof InnerNode) {
            if (((InnerNode)root).type == NodeType.METHOD_DEF) {
                int[] tokenArr = new int[25];
                for (int i = 0; i < 25; ++i) {
                    tokenArr[i] = 0;
                }
                StructNode rootStruct = new StructNode(StructNodeType.UNDEFINED);
                MethodTokens meth = new MethodTokens(path, ((InnerNode)root).startLine, ((InnerNode)root).endLine, rootStruct, n);
                HashSet<String> methodActionTokens = new HashSet<>();
                // System.out.println("================");
                int tokens = parseMethod(root, tokenArr, rootStruct, methodActionTokens);
                meth.root = rootStruct;
                meth.tokensCnt = tokens;
                meth.actionTokens = methodActionTokens;
                methods.add(meth);
            }
        }
        for (ASTNode ch : root.children) {
            parseMethods(ch, path);
        }
    }

    int parseMethod(ASTNode root, int[] tokenArr, StructNode structNode, HashSet<String> actionTokens) {
        // System.out.println("In");
        if (root instanceof IdentifierNode) {
            // System.out.println(((IdentifierNode)root).name);
            structNode.type = StructNodeType.IDENTIFIER;
            for (int i = 0; i < tokenArr.length; ++i) {
                structNode.token[i] += tokenArr[i];
                // System.out.print(structNode.token[i]);
                // System.out.print(' ');
            }
            // System.out.println("");
        }
        if (root instanceof InnerNode) {
            InnerNode node = (InnerNode)root;
            tokenArr[node.type.ordinal()] += 1;
            if (node.type == NodeType.METHOD_INVOC) {
                // System.out.println("Meth");
                structNode.type = StructNodeType.METHOD;
                for (int i = 0; i < tokenArr.length; ++i) {
                    structNode.token[i] += tokenArr[i];
                    // System.out.print(structNode.token[i]);
                    // System.out.print(' ');
                }   
                // System.out.println("");
            }
            if (node.type == NodeType.LOGICAL_EXPR || node.type == NodeType.NUMERIC_EXPR || node.type == NodeType.CONDITION_EXPR) {
                structNode.type = StructNodeType.OPERATION;
            }
        }
        if (root instanceof ActionTokenNode) {
            actionTokens.add(((ActionTokenNode)root).getMetaInfo());
        }
        int tokens = 1;
        for (ASTNode ch : root.children) {
            StructNode child = new StructNode(StructNodeType.UNDEFINED);
            child.pr = structNode;
            structNode.childs.add(child);
            tokens += parseMethod(ch, tokenArr, child, actionTokens);
        }
        if (root instanceof InnerNode) {
            InnerNode node = (InnerNode)root;
            tokenArr[node.type.ordinal()] -= 1;
        }
        // System.out.println("Out");
        return tokens;
    }
}
