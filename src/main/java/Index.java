import java.util.*;

public class Index {
    Set<Integer> hashes;
    int k; // Parameter for active token n-grams

    Index(int k) {
        hashes = new HashSet<>();
        this.k = k;
        FileWorker fw = new FileWorker();
        fw.updateDir("./index");
    }

    void addBlock(CodeBlock block) {
        FileWorker fw = new FileWorker();
        Vector<String> tokens = block.getActiveTokens();
        if (tokens.size() < k) {
            return;
        }
        tokens.sort(null);
        Vector<String> window = new Vector<>();
        for (int i = 0; i < k; ++i) {
            window.add(tokens.get(i));
        }
        int h = String.join(" ", window).hashCode();
        hashes.add(h);
        fw.addBlockDirect(h, block);
        for (int i = k; i < tokens.size(); ++i) {
            window.remove(0);
            window.add(tokens.get(i));
            h = String.join(" ", window).hashCode();
            fw.addBlockDirect(h, block);
            hashes.add(h);
        }
    }

    Vector<CodeBlock> getBlocks(Vector<String> tokens) {
        FileWorker fw = new FileWorker();
        Set<Integer> usedBlocks = new HashSet<>();
        Vector<CodeBlock> v = new Vector<>();
        if (tokens.size() < k) {
            return v;
        }
        tokens.sort(null);
        Vector<String> window = new Vector<>();
        for (int i = 0; i < k; ++i) {
            window.add(tokens.get(i));
        }
        int h = String.join(" ", window).hashCode();
        if (hashes.contains(h)) {
            Vector<CodeBlock> blocks = fw.readBlocks(h);
            for (CodeBlock block : blocks) {
                if (!usedBlocks.contains(block.hashCode())) {
                    usedBlocks.add(block.hashCode());
                    v.add(block);
                }
            }
        }
        for (int i = k; i < tokens.size(); ++i) {
            window.remove(0);
            window.add(tokens.get(i));
            h = String.join(" ", window).hashCode();
            if (hashes.contains(h)) {
                Vector<CodeBlock> blocks = fw.readBlocks(h);
                for (CodeBlock block : blocks) {
                    if (!usedBlocks.contains(block.hashCode())) {
                        usedBlocks.add(block.hashCode());
                        v.add(block);
                    }
                }
            }
        }
        return v;
    }
}
