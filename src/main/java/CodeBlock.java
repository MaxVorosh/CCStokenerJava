import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class CodeBlock {

    private TokenCollection vars;
    private TokenCollection ops;
    private TokenCollection callees;

    private HashMap<String, Integer> activeTokens;
    private int activeTokensCnt;
    private int id;

    private int tokensNum;

    private CodeBlockInfo info;

    CodeBlock(int id) {
        activeTokens = new HashMap<>();
        vars = new TokenCollection();
        ops = new TokenCollection();
        callees = new TokenCollection();
        this.id = id;
        activeTokensCnt = 0;
    }

    void addActiveToken(String token) {
        activeTokensCnt += 1;
        if (!activeTokens.containsKey(token)) {
            activeTokens.put(token, 1);
        }
        else {
            Integer currentValue = activeTokens.get(token);
            activeTokens.put(token, currentValue + 1);
        }
    }

    void setCollection(TokenCollection collection, CollectionType type) {
        if (type == CollectionType.VAR) {
            vars = collection;
        }
        else if (type == CollectionType.OPERATION) {
            ops = collection;
        }
        else if (type == CollectionType.CALLEE) {
            callees = collection;
        }
    }

    Token getToken(int i, CollectionType type) {
        if (type == CollectionType.VAR) {
            return vars.get(i);
        }
        if (type == CollectionType.OPERATION) {
            return ops.get(i);
        }
        if (type == CollectionType.CALLEE) {
            return callees.get(i);
        }
        return null;
    }

    int collectionSize(CollectionType type) {
        if (type == CollectionType.VAR) {
            return vars.size();
        }
        if (type == CollectionType.OPERATION) {
            return ops.size();
        }
        if (type == CollectionType.CALLEE) {
            return callees.size();
        }
        return 0;
    }

    HashMap<String, Integer> getActiveTokens() {
        return activeTokens;
    }

    @Override
    public int hashCode() {
        return id;
    }

    int activeTokensOverlap(CodeBlock other) {
        int cnt = 0;
        for (String token : other.activeTokens.keySet()) {
            if (activeTokens.containsKey(token)) {
                cnt += Math.min(activeTokens.get(token), other.activeTokens.get(token));
            }
        }
        return cnt;
    }

    boolean shouldBeFiltered(CodeBlock other, float beta, float theta) {
        int targetTokens = tokensNum;
        int candidateTokens = other.tokensNum;
        float tokenRatio = min(targetTokens, candidateTokens) / (float) max(targetTokens, candidateTokens);
        if (tokenRatio <= theta) {
            return true;
        }
        int overlap = activeTokensOverlap(other);
        float overlapRatio = overlap / (float) min(activeTokensCnt, other.activeTokensCnt);

        return overlapRatio <= beta;
    }


    void setInfo(CodeBlockInfo info) {
        this.info = info;
    }

    CodeBlockInfo getInfo() {
        return info;
    }

    void setTokensNum(int tokens) {
        tokensNum = tokens;
    }

    int getTokensNum() {
        return tokensNum;
    }

    float[] getCollectionAvg(CollectionType type) {
        if (type == CollectionType.VAR) {
            return vars.getAvg();
        }
        if (type == CollectionType.OPERATION) {
            return ops.getAvg();
        }
        if (type == CollectionType.CALLEE) {
            return callees.getAvg();
        }
        return new float[25];
    }
}
