import java.util.Vector;

public class Runner {
    public static void main(String[] args) {
        System.out.println("Started");
        FileWorker worker = new FileWorker();
        Processor processor = new Processor(3, 0.1f, 0.5f, 0.4f, 0.65f, args[2]); // Didn't find k value in paper

        Vector<CodeBlock> blocks = worker.parseDir(args[0]);
        Vector<ClonePair> clones = processor.getClonePairs(blocks);
        worker.writeReport(clones, args[1]);
    }
}
