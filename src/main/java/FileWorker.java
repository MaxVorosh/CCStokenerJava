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
            sc.close();
        } catch (FileNotFoundException e) {
            System.err.println("File " + file.toString() + " not found");
        }
    }

    void writeReport(Vector<ClonePair> clones, String path) {
        try {
            FileWriter fw = new FileWriter(path);
            for (ClonePair pair : clones) {
                CodeBlockInfo p1 = pair.first;
                CodeBlockInfo p2 = pair.second;
                fw.write(p1.filename + "," + (p1.startLine + 1) + "," + (p1.endLine + 1) + "," +
                        p2.filename + "," + (p2.startLine + 1) + "," + (p2.endLine + 1) + "\n");
            }
            fw.flush();
            fw.close();
        } catch (IOException e) {
            System.err.println("Can't create report file");
        }
    }

    void addBlockDirect(String pathDir, int key, CodeBlock value) {
        String path = String.format("%s/%d", pathDir, key);
        try {
            FileWriter fw = new FileWriter(path, true);
            CodeBlockInfo info = value.getInfo();
            fw.write(String.format("New %d %d %s %d %d\n", value.hashCode(), value.getTokensNum(), info.filename, info.startLine, info.endLine));
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
            fw.close();
        }
        catch (IOException e) {
            System.err.println("Exception occured");
        }
    }

    Vector<CodeBlock> readBlocks(String pathDir, int key) {
        String path = String.format("%s/%d", pathDir, key);
        File file = new File(path);
        CodeBlock currentBlock = new CodeBlock(-1);
        TokenCollection collection = new TokenCollection();
        Vector<CodeBlock> blocks = new Vector<>();
        int cnt = 0;
        ParseStage stage = ParseStage.CALLEE;
        try {
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                line = line.strip();
                if (line.startsWith("New") && stage == ParseStage.CALLEE) {
                    String[] data = line.split(" ");
                    int id = Integer.parseInt(data[1]);
                    int tokensNum = Integer.parseInt(data[2]);
                    String blockPath = data[3];
                    int startLine = Integer.parseInt(data[4]);
                    int endLine = Integer.parseInt(data[5]);
                    currentBlock = new CodeBlock(id);
                    currentBlock.setInfo(new CodeBlockInfo(blockPath, startLine, endLine));
                    currentBlock.setTokensNum(tokensNum);
                    stage = ParseStage.ACTIVE;
                }
                else if (line.startsWith("Vars")) {
                    collection = new TokenCollection();
                    stage = ParseStage.VAR;
                }
                else if (line.startsWith("Operations")) {
                    collection = new TokenCollection();
                    stage = ParseStage.OPERATION;
                }
                else if (line.startsWith("Callees")) {
                    collection = new TokenCollection();
                    stage = ParseStage.CALLEE;
                }
                else if (stage == ParseStage.ACTIVE) {
                    String[] active = line.split(" ");
                    for (String token : active) {
                        currentBlock.addActiveToken(token);
                    }
                }
                else if (cnt == 0) {
                    cnt = Integer.parseInt(line);
                    if (cnt == 0 && stage == ParseStage.CALLEE) {
                        blocks.add(currentBlock);
                    }
                }
                else {
                    cnt -= 1;
                    int[] tokenArray = Stream.of(line.split(" ")).mapToInt(Integer::parseInt).toArray();
                    collection.add(new Token(tokenArray));
                    if (cnt == 0) {
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
            sc.close();
            return blocks;
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            return new Vector<>();
        }
    }

    void updateDir(String path) {
        File dir = new File(path);
        File[] listOfFiles = dir.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                file.delete();
            }
        }
    }

    void writeTokensDir(String path, String pref) {
        File dir = new File(path);
        File[] listOfFiles = dir.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    writeTokensFile(file, pref);
                }
                else if (file.isDirectory()) {
                    writeTokensDir(file.getPath(), pref + file.getName() + "-");
                }
            }
        }
    }

    void writeTokensFile(File file, String pref) {
        TokenBuilder tb = new TokenBuilder(3);
        String name = file.getName();
        String[] nameParts = name.split("\\.");
        name = pref + String.join(".", Arrays.copyOfRange(nameParts, 0, nameParts.length - 1)) + ".txt";
        String type = nameParts[nameParts.length - 1];
        tb.buildTokens(file.getPath(), type);
        if (tb.methods.size() == 0) {
            return;
        }
        try {
            FileWriter fw = new FileWriter("./tokens/" + name, false);
            for (MethodTokens meth : tb.methods) {
                writeTokensMethod(fw, meth, file.getPath());
            }
            fw.close();
        }
        catch (Exception e) {System.err.println(e);}
    }

    void writeTokensMethod(FileWriter fw, MethodTokens meth, String path) {
        try {
            fw.write(String.format("<block filePath:%s, startline:%d, endline:%d, validTokenNum:%d, totalTokenNum: %d>\n", path, meth.startLine, meth.endLine, meth.tokensCnt, meth.tokensCnt));
            fw.write("<type>\n");
            for (String actionToken : meth.actionTokens) {
                fw.write(String.format("%s,1: [", actionToken));
                for (int i = 0; i < 24; ++i) {
                    fw.write("0, ");
                }
                fw.write("0]\n");
            }
            fw.write("</type>\n");
            fw.write("<variable group>\n");
            for (int[] varVarToken : meth.varVarTokens) {
                fw.write("_,1: [");
                for (int i = 0; i < 24; ++i) {
                    fw.write(String.format("%d, ", varVarToken[i]));
                }
                fw.write(String.format("%d]\n", varVarToken[24]));
            }
            fw.write("</variable group>\n");
            fw.write("<method group>\n");
            for (int[] varCalleeToken : meth.varCalleeTokens) {
                fw.write("_,1: [");
                for (int i = 0; i < 24; ++i) {
                    fw.write(String.format("%d, ", varCalleeToken[i]));
                }
                fw.write(String.format("%d]\n", varCalleeToken[24]));
            }
            fw.write("</method group>\n");
            fw.write("<relation>\n");
            for (int[] varOpToken : meth.varOpTokens) {
                fw.write("_,1: [");
                for (int i = 0; i < 24; ++i) {
                    fw.write(String.format("%d, ", varOpToken[i]));
                }
                fw.write(String.format("%d]\n", varOpToken[24]));
            }
            fw.write("</relation>\n");
            fw.write("</block>\n");
        }
        catch (Exception e) {}
    }
}
