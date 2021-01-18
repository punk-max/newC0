package miniplc0java.Analyser;

public enum IdentType {
    INT,
    DOUBLE,
    VOID,
    STRING;

    public String toString(){
        switch (this){
            case INT:
            case STRING:
                return "int";
            case DOUBLE:
                return "double";
            default:
                return null;
        }
    }
}
