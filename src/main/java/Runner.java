import java.util.Vector;

public class Runner {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        FileWorker worker = new FileWorker();
        worker.writeTokensDir(args[0], "");
        System.out.println("Tokens ready");
        Processor processor = new Processor(10, 0.1f, 0.5f, 0.4f, 0.65f, "./index", "./indexForSmall"); // Didn't find k value in paper

        Vector<CodeBlock> blocks = worker.parseDir("./tokens");
        Vector<ClonePair> clones = processor.getClonePairs(blocks);
        worker.writeReport(clones, "./clonepairs.txt");
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("%dms", endTime - startTime));
        // BenchmarkValidator bv = new BenchmarkValidator();
        // bv.runBenchmarkDir(args[0], "");
    }
}
