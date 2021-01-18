package miniplc0java.Analyser;

public class Ident {
    String name;
    boolean isConst;
    boolean isGlobal;
    boolean isParam;
    boolean isLocal;
    int offset;
    IdentType identType;
    String value;

    public Ident(String name, boolean isConst, IdentType identType, boolean isGlobal, boolean isParam, boolean isLocal, String value){
        this.name=name;
        this.isConst=isConst;
        this.identType = identType;
        this.isGlobal=isGlobal;
        this.isParam=isParam;
        this.isLocal=isLocal;
        this.value=value;
    }

    public Ident() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean param) {
        isParam = param;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public void setIdentType(IdentType identType) {
        this.identType = identType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
