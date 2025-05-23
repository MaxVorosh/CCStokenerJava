import java.util.HashSet;
import java.util.Vector;
import java.util.HashMap;

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
    HashMap<String, Variable> variables;

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
        variables = new HashMap<>();
    }

    void computeSemanticTokens() {
        int[] token = new int[25];
        for (int i = 0; i < 25; ++i) {
            token[i] = 0;
        }
        computeSemanticTokensTree(root);
    }

    private void computeSemanticTokensTree(StructNode rootTree) {
        if (rootTree.type == StructNodeType.IDENTIFIER) {
            Variable v = new Variable(n);
            v.setToken(rootTree.token);
            variables.put(rootTree.mainIdentifier, v);
        }
        else if (rootTree.type == StructNodeType.METHOD || rootTree.type == StructNodeType.OPERATION) {
            HashSet<String> args = new HashSet<>();
            getIdentifiers(rootTree, args);
            HashSet<String> related = getRelatedIdentifiers(args, n);
            // System.out.println(String.format("%d %d", args.size(), related.size()));
            // for (String var : args) {
            //     System.out.println(var);
            // }
            int[] token = new int[25];
            for (int i = 0; i < 25; ++i) {
                token[i] = rootTree.token[i];
            }
            for (String rel : related) {
                for (int i = 0; i < 25; ++i) {
                    token[i] += variables.get(rel).token[i];
                }
            }
            if (rootTree.type == StructNodeType.METHOD) {
                varCalleeTokens.add(token);
            }
            else {
                varOpTokens.add(token);
            }
        }
        else {
            if (rootTree.mainIdentifier == "" && rootTree.identifiers.size() != 0) {
                // Method definition
                for (String var : rootTree.identifiers) {
                    int[] token = new int[25];
                    for (int i = 0; i < 25; ++i) {
                        token[i] = rootTree.token[i];
                    }
                    varVarTokens.add(token);
                    Variable v = new Variable(n);
                    v.setToken(token);
                    variables.put(var, v);
                }
            }
            else if (rootTree.mainIdentifier != "") {
                int[] token = new int[25];
                for (int i = 0; i < 25; ++i) {
                    token[i] = rootTree.token[i];
                }
                HashSet<String> related = getRelatedIdentifiers(new HashSet<>(rootTree.identifiers), n);
                for (String rel : related) {
                    for (int i = 0; i < 25; ++i) {
                        token[i] += variables.get(rel).token[i];
                    }
                }
                if (related.size() != 0) {
                    varVarTokens.add(token);
                }
                if (variables.containsKey(rootTree.mainIdentifier)) {
                    variables.get(rootTree.mainIdentifier).setToken(token);
                    variables.get(rootTree.mainIdentifier).addVariables(related);;
                }
                else {
                    Variable v = new Variable(n);
                    v.setToken(token);
                    v.addVariables(related);
                    variables.put(root.mainIdentifier, v);
                }
            }
        }
        for (StructNode ch : rootTree.childs) {
            computeSemanticTokensTree(ch);
        }
    }

    private void getIdentifiers(StructNode rootTree, HashSet<String> args) {
        if (rootTree.type == StructNodeType.IDENTIFIER) {
            args.add(rootTree.mainIdentifier);
        }
        for (StructNode ch : rootTree.childs) {
            getIdentifiers(ch, args);
        }
    }

    private HashSet<String> getRelatedIdentifiers(HashSet<String> vars, int depth) {
        HashSet<String> hs = new HashSet<>(vars);
        if (depth == 0) {
            return hs;
        }
        for (String var : vars) {
            HashSet<String> relatedVariables = getRelatedIdentifiers(variables.get(var).getRelatedVariables(), depth - 1);
            hs.addAll(relatedVariables);
        }
        return hs;
    }
}