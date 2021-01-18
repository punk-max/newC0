package miniplc0java.Analyser;

public class ReturnPoint {

    ReturnType returnType;

    boolean isReturn;

    public ReturnPoint(ReturnType returnType, boolean isReturn){
        this.returnType = returnType;
        this.isReturn = isReturn;
    }

    public ReturnPoint() {
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void setReturnType(ReturnType returnType) {
        this.returnType = returnType;
    }

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn(boolean isReturn) {
        this.isReturn = isReturn;
    }
}
