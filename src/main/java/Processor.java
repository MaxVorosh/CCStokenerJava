import java.util.Vector;

public class Processor {

    float phi; // Block similarity step
    float beta; // Same active token ratio threshold
    float theta; // Total token ratio threshold
    float eta; // Block similarity threshold
    int k; // Index n-gram size
    String indexDir;
    Processor(int k, float phi, float beta, float theta, float eta, String indexDir) {
        this.k = k;
        this.phi = phi;
        this.beta = beta;
        this.theta = theta;
        this.eta = eta;
        this.indexDir = indexDir;
    }

    float getSimilarity(CodeBlock first, CodeBlock second, CollectionType type) {
        float res = 0;
        float tr = 1;
        float maxSize = Math.max(first.collectionSize(type), second.collectionSize(type));
        if (maxSize == 0) {
            return 1;
        }
        while (tr > 0) {
            for (int i = 0; i < first.collectionSize(type); ++i) {
                if (first.isMarked(i, type)) {
                    continue;
                }
                for (int j = 0; j < second.collectionSize(type); ++j) {
                    if (second.isMarked(j, type)) {
                        continue;
                    }
                    Token firstToken = first.getToken(i, type);
                    Token secondToken = second.getToken(j, type);
                    float sim = firstToken.sim(secondToken);
                    if (sim > tr) {
                        res += sim;
                        first.markToken(i, type);
                        second.markToken(j, type);
                        break;
                    }
                }
            }
            tr -= phi;
        }
        first.reset(type);
        second.reset(type);
        return res / maxSize;
    }

    Vector<ClonePair> getClonePairs(Vector<CodeBlock> blocks) {
        Index ind = new Index(indexDir, k);
        for (CodeBlock block : blocks) {
            ind.addBlock(block);
        }
        Vector<ClonePair> pairs = new Vector<>();
        for (CodeBlock block : blocks) {
            Vector<CodeBlock> candidates = ind.getBlocks(block.getActiveTokens());
            for (CodeBlock otherBlock : candidates) {
                if (otherBlock.hashCode() >= block.hashCode()) {
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
        }
        return pairs;
    }
}
