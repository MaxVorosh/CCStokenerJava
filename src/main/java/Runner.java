import java.util.Vector;

public class Runner {
    public static void main(String[] args) {
        String indexPath = "./index";
        String indexForSmallPath = "./indexForSmall";
        String tokenPath = "./tokens";

        long startTime = System.currentTimeMillis();
        FileWorker worker = new FileWorker();
        worker.writeTokensDir(args[0], "");
        System.out.println("Tokens ready");
        Processor processor = new Processor(0.1f, 0.5f, 0.4f, 0.65f);

        Index ind = new Index(indexPath, indexForSmallPath, 10); // Didn't find k value in paper
        worker.parseDir(tokenPath, ind);
        System.out.println("Index ready");
        worker.processDir(tokenPath, processor, ind);
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("%dms", endTime - startTime));
    }
}
