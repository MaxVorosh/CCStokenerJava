public class UnknownNode extends ASTNode{
    String type;

    UnknownNode(String t) {
        super();
        type = t;
    }

    @Override
    String getMetaInfo() {
        return type;
    }
}
