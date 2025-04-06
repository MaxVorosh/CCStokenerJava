import java.util.*;

public class Index {
    Set<Integer> hashes;
    int k; // Parameter for active token n-grams

    Index(int k) {
        hashes = new HashSet<>();
        this.k = k;
    }

    void addBlock(CodeBlock block) {
        System.out.println("OK");
        FileWorker fw = new FileWorker();
        Vector<String> tokens = block.getActiveTokens();
        System.out.println(tokens.size());
        if (tokens.size() < k) {
            return;
        }
        tokens.sort(null);
        Deque<String> window = new ArrayDeque<>();
        for (int i = 0; i < k; ++i) {
            window.push(tokens.get(i));
        }
        int h = window.hashCode();
        fw.addBlockDirect(h, block);
        for (int i = k; i < tokens.size(); ++i) {
            window.pollFirst();
            window.push(tokens.get(i));
            h = window.hashCode();
            fw.addBlockDirect(h, block);
        }
    }

    Vector<CodeBlock> getBlocks(Vector<String> tokens) {
        FileWorker fw = new FileWorker();
        Set<CodeBlock> usedBlocks = new HashSet<>();
        Vector<CodeBlock> v = new Vector<>();
        if (tokens.size() < k) {
            return v;
        }
        tokens.sort(null);
        Deque<String> window = new ArrayDeque<>();
        for (int i = 0; i < k; ++i) {
            window.push(tokens.get(i));
        }
        int h = window.hashCode();
        if (hashes.contains(h)) {
            Vector<CodeBlock> blocks = fw.readBlocks(h);
            for (CodeBlock block : blocks) {
                if (!usedBlocks.contains(block)) {
                    usedBlocks.add(block);
                    v.add(block);
                }
            }
        }
        return v;
    }
}
