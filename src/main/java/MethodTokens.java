import java.util.HashSet;
import java.util.Vector;

public class MethodTokens {
    HashSet<String> actionTokens;
    StructNode root;
    Vector<int[]> varVarTokens;
    Vector<int[]> varOpTokens;
    Vector<int[]> varCalleeTokens;
    String path;
    int startLine, endLine;
    int tokensCnt;
    int n; // for n-grams

    MethodTokens(String path, int startLine, int endLine, StructNode root, int n) {
        actionTokens = new HashSet<>();
        varVarTokens = new Vector<>();
        varOpTokens = new Vector<>();
        varCalleeTokens = new Vector<>();
        tokensCnt = 0;
        this.startLine = startLine;
        this.endLine = endLine;
        this.root = root;
        this.n = n;
    }

    void computeSemanticTokens() {
        computeVarSemanticTokens(root);
        computeSemanticTokensTree(root);
    }

    private void computeVarSemanticTokens(StructNode rootTree) {
        if (rootTree.type == StructNodeType.IDENTIFIER) {
            int[] groupToken = getNGram(rootTree, n);
            for (int i = 0; i < groupToken.length; ++i) {
                rootTree.semToken[i] = groupToken[i];
            }
            varVarTokens.add(groupToken);
        }
        for (StructNode ch : rootTree.childs) {
            computeVarSemanticTokens(ch);
        }
    }

    private void computeSemanticTokensTree(StructNode rootTree) {
        if (rootTree.type == StructNodeType.METHOD || rootTree.type == StructNodeType.OPERATION) {
            int[] otherToken = getSemSum(rootTree);
            for (int i = 0; i < 25; ++i) {
                rootTree.semToken[i] = rootTree.token[i] + otherToken[i];
            }
        }
        for (StructNode ch : rootTree.childs) {
            computeSemanticTokensTree(ch);
        }
    }

    private int[] getSemSum(StructNode rootTree) {
        int[] token = new int[25];
        for (int i = 0; i < 25; ++i) {
            token[i] = 0;
        }
        for (StructNode ch : rootTree.childs) {
            int[] otherToken = ch.semToken;
            if (ch.type == StructNodeType.UNDEFINED) {
                otherToken = getSemSum(ch);
            }
            for (int i = 0; i < token.length; ++i) {
                token[i] += otherToken[i];
            }
        }
        return token;
    }

    private int[] getNGram(StructNode rootTree, int n) {
        if (n == 0 || rootTree.pr == null) {
            return sumTokens(rootTree, this.n);
        }
        return getNGram(rootTree.pr, n - 1);
    }

    private int[] sumTokens(StructNode rootTree, int n) {
        int[] token = new int[25];
        for (int i = 0; i < token.length; ++i) {
            token[i] = rootTree.token[i];
        }
        if (n == 0) {
            return token;
        }
        for (StructNode ch : rootTree.childs) {
            int[] otherToken = sumTokens(ch, n - 1);
            for (int i = 0; i < token.length; ++i) {
                token[i] += otherToken[i];
            }
        }
        return token;
    }
}
