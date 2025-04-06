import java.util.StringJoiner;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

public class Token {
    private int[] token;

    Token(int[] token) {
        this.token = token;
    }

    float sim(Token other) {
        float res = 0;
        for (int i = 0; i < token.length; ++i) {
            res += token[i] * other.token[i];
        }
        return res / vectorSize() / other.vectorSize();
    }

    private float vectorSize() {
        float res = 0;
        for (int coord : token) {
            res += coord * coord;
        }
        return (float) sqrt(res);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        IntStream.of(token).forEach(x -> sj.add(String.valueOf(x)));
        return sj.toString();
    }
}
