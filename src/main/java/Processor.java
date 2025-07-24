import java.util.Vector;

public class Processor {

    float phi; // Block similarity step
    float beta; // Same active token ratio threshold
    float theta; // Total token ratio threshold
    float eta; // Block similarity threshold
    int k; // Index n-gram size
    String indexDir;
    String smallIndexDir;
    Processor(float phi, float beta, float theta, float eta) {
        this.phi = phi;
        this.beta = beta;
        this.theta = theta;
        this.eta = eta;
    }

    float getSimilarity(CodeBlock first, CodeBlock second, CollectionType type) {
        if (first.collectionSize(type) == 0 && second.collectionSize(type) == 0) {
            return 1;
        }
        float[] firstAvg = first.getCollectionAvg(type);
        float[] secondAvg = second.getCollectionAvg(type);
        float result = 0;
        float firstLen = 0;
        float secondLen = 0;
        for (int i = 0; i < firstAvg.length; ++i) {
            firstLen += firstAvg[i] * firstAvg[i];
            secondLen += secondAvg[i] * secondAvg[i];
            result += firstAvg[i] * secondAvg[i];
        }
        return (float)(result / Math.sqrt(firstLen) / Math.sqrt(secondLen));
    }

    Vector<ClonePair> getClonePairs(CodeBlock block, Index ind) {
        Vector<ClonePair> pairs = new Vector<>();
        Vector<CodeBlock> candidates = ind.getBlocks(block);
        for (CodeBlock otherBlock : candidates) {
            if (otherBlock.getTokensNum() == block.getTokensNum() && 
                otherBlock.hashCode() >= block.hashCode()) {
                continue;
            }
            boolean shouldFilter = block.shouldBeFiltered(otherBlock, beta, theta);
            if (shouldFilter) {
                continue;
            }

            float varSim = getSimilarity(block, otherBlock, CollectionType.VAR);
            float operationSim = getSimilarity(block, otherBlock, CollectionType.OPERATION);
            float calleeSim = getSimilarity(block, otherBlock, CollectionType.CALLEE);
            float sim = (varSim + operationSim + calleeSim) / 3;
            if (sim > eta) {
                pairs.add(new ClonePair(block.getInfo(), otherBlock.getInfo()));
            }
        }
        return pairs;
    }
}
