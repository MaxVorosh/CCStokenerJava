import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
import java.util.stream.Stream;


public class FileWorker {
    private enum ParseStage {
        SKIP,
        ACTIVE,
        VAR,
        OPERATION,
        CALLEE
    }

    Vector<CodeBlock> parseDir(String path) {
        Vector<CodeBlock> blocks = new Vector<>();
        File dir = new File(path);
        parseDirVector(dir, blocks);
        return blocks;
    }

    private void parseDirVector(File dir, Vector<CodeBlock> blocks) {
        File[] listOfFiles = dir.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                parseFile(file, blocks);
            }
            else if (file.isDirectory()) {
                parseDirVector(file, blocks);
            }
        }
    }

    void parseFile(File file, Vector<CodeBlock> blocks) {
        CodeBlock currentBlock = new CodeBlock(blocks.size());
        ParseStage stage = ParseStage.SKIP;
        TokenCollection collection = new TokenCollection();
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("<block")) {
                    line = line.substring(1, line.length() - 1);
                    String[] parts = line.split(",");
                    String filename = parts[0].split(":")[1].strip();
                    int startLine = Integer.parseInt(parts[1].split(":")[1].strip());
                    int endLine = Integer.parseInt(parts[2].split(":")[1].strip());
                    int totalTokens = Integer.parseInt(parts[4].split(":")[1].strip());
                    currentBlock.setTokensNum(totalTokens);
                    currentBlock.setInfo(new CodeBlockInfo(filename, startLine, endLine));
                }
                else if (line.startsWith("</block>")) {
                    blocks.add(currentBlock);
                    currentBlock = new CodeBlock(blocks.size());
                }
                else if (line.startsWith("<type>") || line.startsWith("<basic type") || line.startsWith("<method>")) {
                    stage = ParseStage.ACTIVE;
                }
                else if (line.startsWith("<variable group>")) {
                    stage = ParseStage.VAR;
                }
                else if (line.startsWith("<method group>")) {
                    stage = ParseStage.CALLEE;
                }
                else if (line.startsWith("<relation>")) {
                    stage = ParseStage.OPERATION;
                }
                else if (line.startsWith("</")) {
                    if (stage == ParseStage.VAR) {
                        currentBlock.setCollection(collection, CollectionType.VAR);
                    }
                    else if (stage == ParseStage.OPERATION) {
                        currentBlock.setCollection(collection, CollectionType.OPERATION);
                    }
                    else if (stage == ParseStage.CALLEE) {
                        currentBlock.setCollection(collection, CollectionType.CALLEE);
                    }
                    collection = new TokenCollection();
                }
                else if (line.startsWith("<")) {
                    stage = ParseStage.SKIP;
                }
                else {
                    String[] parts = line.split(":");
                    String[] header = parts[0].split(",");
                    String name = header[0];
                    parts[1] = parts[1].strip();
                    String[] vec = parts[1].substring(1, parts[1].length() - 1).split(",");
                    Vector<Integer> tokenVec = new Vector<Integer>();
                    for (String val : vec) {
                        tokenVec.add(Integer.parseInt(val.strip()));
                    }
                    Integer[] token = new Integer[tokenVec.size()];
                    token = tokenVec.toArray(token);
                    if (stage == ParseStage.SKIP) {}
                    else if (stage == ParseStage.ACTIVE) {
                        currentBlock.addActiveToken(name);
                    }
                    else {

                        collection.add(new Token(Arrays.stream(token).mapToInt(Integer::intValue).toArray()));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + file.toString() + " not found");
        }
    }

    void writeReport(Vector<ClonePair> clones, String path) {
        try {
            FileWriter fw = new FileWriter(path);
            for (ClonePair pair : clones) {
                CodeBlockInfo p1 = pair.first.getInfo();
                CodeBlockInfo p2 = pair.second.getInfo();
                fw.write(p1.filename + " " + p1.startLine + " " + p1.endLine + " " +
                        p2.filename + " " + p2.startLine + " " + p2.endLine + "\n");
            }
            fw.flush();
        } catch (IOException e) {
            System.err.println("Can't create report file");
        }
    }

    void addBlockDirect(int key, CodeBlock value) {
        String path = String.format("./index/%d", key);
        try {
            FileWriter fw = new FileWriter(path);
            fw.write(String.format("New %d\n", value.hashCode()));
            for (String token : value.getActiveTokens()) {
                fw.write(String.format("%s ", token));
            }
            fw.write("\nVars\n");
            fw.write(value.collectionSize(CollectionType.VAR) + "\n");
            for (int i = 0; i < value.collectionSize(CollectionType.VAR); ++i) {
                String tokenStr = value.getToken(i, CollectionType.VAR).toString();
                fw.write(String.format("%s\n", tokenStr));
            }
            fw.write("Operations\n");
            fw.write(value.collectionSize(CollectionType.OPERATION) + "\n");
            for (int i = 0; i < value.collectionSize(CollectionType.OPERATION); ++i) {
                String tokenStr = value.getToken(i, CollectionType.OPERATION).toString();
                fw.write(String.format("%s\n", tokenStr));
            }
            fw.write("Callees\n");
            fw.write(value.collectionSize(CollectionType.CALLEE) + "\n");
            for (int i = 0; i < value.collectionSize(CollectionType.CALLEE); ++i) {
                String tokenStr = value.getToken(i, CollectionType.CALLEE).toString();
                fw.write(String.format("%s\n", tokenStr));
            }
            fw.flush();
        }
        catch (IOException e) {
            System.err.println("Exception occured");
        }
    }

    Vector<CodeBlock> readBlocks(int key) {
        String path = String.format("./index/%d", key);
        File file = new File(path);
        CodeBlock currentBlock = new CodeBlock(-1);
        TokenCollection collection = new TokenCollection();
        Vector<CodeBlock> blocks = new Vector<>();
        int cnt = -1;
        ParseStage stage = ParseStage.CALLEE;
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                line = line.strip();
                if (line.startsWith("New")) {
                    int id = Integer.parseInt(line.split(" ")[1]);
                    currentBlock = new CodeBlock(id);
                    stage = ParseStage.ACTIVE;
                }
                else if (line.startsWith("Vars")) {
                    stage = ParseStage.VAR;
                }
                else if (line.startsWith("Operations")) {
                    stage = ParseStage.OPERATION;
                }
                else if (line.startsWith("Callees")) {
                    stage = ParseStage.CALLEE;
                }
                else if (stage == ParseStage.ACTIVE) {
                    String[] active = line.split(" ");
                    for (String token : active) {
                        currentBlock.addActiveToken(token);
                    }
                }
                else if (cnt == -1) {
                    cnt = Integer.parseInt(line);
                }
                else {
                    cnt -= 1;
                    int[] tokenArray = Stream.of(line.split(" ")).mapToInt(Integer::parseInt).toArray();
                    collection.add(new Token(tokenArray));
                    if (cnt == 0) {
                        cnt = -1;
                        if (stage == ParseStage.VAR) {
                            currentBlock.setCollection(collection, CollectionType.VAR);
                        }
                        else if (stage == ParseStage.OPERATION) {
                            currentBlock.setCollection(collection, CollectionType.OPERATION);
                        }
                        else {
                            currentBlock.setCollection(collection, CollectionType.CALLEE);
                            blocks.add(currentBlock);
                        }
                    }
                }
            }
            return blocks;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return new Vector<>();
        }
    }
}
