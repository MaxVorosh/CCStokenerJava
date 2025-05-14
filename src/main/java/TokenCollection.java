import java.util.Vector;

public class TokenCollection {
    private Vector<Token> collection;
    private Vector<Boolean> marked;

    TokenCollection() {
        collection = new Vector<>();
        marked = new Vector<>();
    }

    void add(Token t) {
        collection.add(t);
        marked.add(false);
    }

    Token get(int i) {
        return collection.get(i);
    }

    void mark(int i) {
        marked.set(i, true);
    }

    boolean isMarked(int i) {
        return marked.get(i);
    }

    int size() {
        return collection.size();
    }

    void reset() {
        for (int i = 0; i < marked.size(); ++i) {
            marked.set(i, false);
        }
    }
}
