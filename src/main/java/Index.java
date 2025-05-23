import java.util.*;

public class Index {
    Set<Integer> hashes;
    int k; // Parameter for active token n-grams
    String pathDir;
    String smallPathDir;

    Index(String pathDir, String smallPathDir, int k) {
        hashes = new HashSet<>();
        this.k = k;
        FileWorker fw = new FileWorker();
        fw.updateDir(pathDir);
        this.pathDir = pathDir;
        this.smallPathDir = smallPathDir;
    }

    void addBlock(CodeBlock block) {
        FileWorker fw = new FileWorker();
        Vector<String> tokens = block.getActiveTokens();
        if (tokens.size() < k) {
            int id = block.getTokensNum() / 100 + block.getActiveTokens().size();
            if (id < 100) {
                return;
            }
            fw.addBlockDirect(smallPathDir, id, block);
            return;
        }
        tokens.sort(null);
        Vector<String> window = new Vector<>();
        for (int i = 0; i < k; ++i) {
            window.add(tokens.get(i));
        }
        int h = String.join(" ", window).hashCode();
        hashes.add(h);
        fw.addBlockDirect(pathDir, h, block);
        for (int i = k; i < tokens.size(); ++i) {
            window.remove(0);
            window.add(tokens.get(i));
            h = String.join(" ", window).hashCode();
            fw.addBlockDirect(pathDir, h, block);
            hashes.add(h);
        }
    }

    Vector<CodeBlock> getBlocks(CodeBlock startBlock) {
        Vector<String> tokens = startBlock.getActiveTokens();
        FileWorker fw = new FileWorker();
        Set<Integer> usedBlocks = new HashSet<>();
        Vector<CodeBlock> v = new Vector<>();
        if (tokens.size() < k) {
            // return new Vector<>();
            int id = startBlock.getTokensNum() / 100 + tokens.size();
            if (id < 100) {
                return new Vector<>();
            }
            return fw.readBlocks(smallPathDir, id);
        }
        tokens.sort(null);
        Vector<String> window = new Vector<>();
        for (int i = 0; i < k; ++i) {
            window.add(tokens.get(i));
        }
        int h = String.join(" ", window).hashCode();
        if (hashes.contains(h)) {
            Vector<CodeBlock> blocks = fw.readBlocks(pathDir, h);
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
                Vector<CodeBlock> blocks = fw.readBlocks(pathDir, h);
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
