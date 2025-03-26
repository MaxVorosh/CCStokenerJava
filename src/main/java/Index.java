import java.util.*;

public class Index {
    private Map<Integer, Vector<CodeBlock>> index;
    int k; // Parameter for active token n-grams

    Index(int k) {
        index = new HashMap<>();
        this.k = k;
    }

    void addBlock(CodeBlock block) {
        Vector<String> tokens = block.getActiveTokens();
        if (tokens.size() < k) {
            return;
        }
        tokens.sort(null);
        Deque<String> window = new ArrayDeque<>();
        for (int i = 0; i < k; ++i) {
            window.push(tokens.get(i));
        }
        int h = window.hashCode();
        addBlockDirect(h, block);
        for (int i = k; i < tokens.size(); ++i) {
            window.pollFirst();
            window.push(tokens.get(i));
            h = window.hashCode();
            addBlockDirect(h, block);
        }
    }

    Vector<CodeBlock> getBlocks(Vector<String> tokens) {
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
        if (index.containsKey(h)) {
            Vector<CodeBlock> blocks = index.get(h);
            for (CodeBlock block : blocks) {
                if (!usedBlocks.contains(block)) {
                    usedBlocks.add(block);
                    v.add(block);
                }
            }
        }
        return v;
    }

    private void addBlockDirect(int key, CodeBlock value) {
        index.putIfAbsent(key, new Vector<>());
        Vector<CodeBlock> data = index.get(key);
        data.add(value);
        index.put(key, data);
    }
}
