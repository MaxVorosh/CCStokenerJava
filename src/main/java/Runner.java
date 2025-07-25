import java.util.Vector;

public class Runner {
    public static void main(String[] args) {
        String indexPath = "./index";
        String tokenPath = "./tokens";
        String[] parsedArgs = args[0].split("\s");

        boolean commonMode = !(parsedArgs.length >= 2 && parsedArgs[1].equals("--bcb"));
        long startTime = System.currentTimeMillis();
        FileWorker worker = new FileWorker(commonMode);
        worker.writeTokensDir(parsedArgs[0], "");
        System.out.println("Tokens ready");
        Processor processor = new Processor(0.1f, 0.5f, 0.4f, 0.65f);

        Index ind = new Index(indexPath, 50, commonMode);
        worker.parseDir(tokenPath, ind);
        System.out.println("Index ready");
        worker.processDir(tokenPath, processor, ind);
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("%dms", endTime - startTime));
    }
}
