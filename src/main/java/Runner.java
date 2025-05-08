import java.util.Vector;

public class Runner {
    public static void main(String[] args) {
        // ASTBuilder astb = new ASTBuilder();
        // astb.buildAsts("./code_examples/GCD.java", "java");
        TokenBuilder tb = new TokenBuilder(5); // Didn't find n value in paper
        tb.buildTokens("./code_examples/GCD.java", "java");
        // for (MethodTokens m : tb.methods) {
        //     System.err.println(m.tokensCnt);
        //     System.err.println(m.actionTokens.size());
        //     for (String tok : m.actionTokens) {
        //         System.err.println(tok);
        //     }
        // }
        // System.out.println("Started");
        // FileWorker worker = new FileWorker();
        // Processor processor = new Processor(7, 0.1f, 0.5f, 0.4f, 0.65f, args[2]); // Didn't find k value in paper

        // Vector<CodeBlock> blocks = worker.parseDir(args[0]);
        // Vector<ClonePair> clones = processor.getClonePairs(blocks);
        // worker.writeReport(clones, args[1]);
    }
}
