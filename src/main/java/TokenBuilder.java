import java.util.Vector;

public class TokenBuilder {

    int n; // variable for n-grams
    Vector<MethodTokens> methods;

    TokenBuilder(int n) {
        this.n = n;
        methods = new Vector<>();
    }

    void buildTokens(String path, String lang) {
        ASTBuilder astb = new ASTBuilder();
        ASTNode root = astb.buildAsts(path, lang);
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
                int tokens = parseMethod(root, tokenArr, rootStruct);
                meth.root = rootStruct;
                meth.tokensCnt = tokens;
                methods.add(meth);
            }
        }
        for (ASTNode ch : root.children) {
            parseMethods(ch, path);
        }
    }

    int parseMethod(ASTNode root, int[] tokenArr, StructNode structNode) {
        if (root instanceof IdentifierNode) {
            structNode.type = StructNodeType.IDENTIFIER;
            for (int i = 0; i < tokenArr.length; ++i) {
                structNode.token[i] += tokenArr[i];
            }
        }
        if (root instanceof InnerNode) {
            InnerNode node = (InnerNode)root;
            if (node.type == NodeType.METHOD_INVOC) {
                structNode.type = StructNodeType.METHOD;
                for (int i = 0; i < tokenArr.length; ++i) {
                    structNode.token[i] += tokenArr[i];
                }   
            }
            tokenArr[node.type.ordinal()] += 1;
        }
        int tokens = 1;
        for (ASTNode ch : root.children) {
            StructNode child = new StructNode(StructNodeType.UNDEFINED);
            child.pr = structNode;
            structNode.childs.add(child);
            tokens += parseMethod(ch, tokenArr, child);
        }
        return tokens;
    }
}
