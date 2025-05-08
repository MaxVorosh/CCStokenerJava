import java.util.Vector;

public class Runner {
    public static void main(String[] args) {
        FileWorker worker = new FileWorker();
        worker.writeTokensDir(args[0]);
        Processor processor = new Processor(7, 0.1f, 0.5f, 0.4f, 0.65f, "./index"); // Didn't find k value in paper

        Vector<CodeBlock> blocks = worker.parseDir("./tokens");
        Vector<ClonePair> clones = processor.getClonePairs(blocks);
        worker.writeReport(clones, "./report");
    }
}
