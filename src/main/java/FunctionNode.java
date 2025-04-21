public class FunctionNode extends InnerNode {
    boolean isOperation;

    FunctionNode(NodeType t, boolean isOp) {
        super(t);
        isOperation = isOp;
    }
}
