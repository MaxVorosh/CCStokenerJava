public class Runner {
    public static void main(String[] args) {
        String indexPath = "./index";
        String tokenPath = "./tokens";
        String[] parsedArgs = args[0].split("\s");
        int nproc = 25;

        boolean commonMode = !(parsedArgs.length >= 2 && parsedArgs[1].equals("--bcb"));
	float beta = 0.5f;
	float theta = 0.4f;
	float eta = 0.65f;
	System.out.println(parsedArgs.length);
	if (parsedArgs.length == 5) {
		beta = Float.parseFloat(parsedArgs[2]);
		theta = Float.parseFloat(parsedArgs[3]);
		eta = Float.parseFloat(parsedArgs[4]);
	}
	System.out.println(beta);
	System.out.println(theta);
	System.out.println(eta);
        long startTime = System.currentTimeMillis();
        FileWorker worker = new FileWorker(commonMode, beta, theta, eta);
        worker.processAll(parsedArgs[0], indexPath, tokenPath, nproc);
        long endTime = System.currentTimeMillis();
        System.out.println(String.format("%dms", endTime - startTime));
    }
}
