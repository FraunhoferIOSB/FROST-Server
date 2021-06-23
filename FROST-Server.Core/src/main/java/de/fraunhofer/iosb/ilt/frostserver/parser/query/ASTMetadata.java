package de.fraunhofer.iosb.ilt.frostserver.parser.query;

public class ASTMetadata extends SimpleNode {

    public ASTMetadata(int id) {
        super(id);
    }

    public ASTMetadata(Parser p, int id) {
        super(p, id);
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return (String) value;
    }

    @Override
    public String toString() {
        return "Metadata: " + getValue();
    }

}
