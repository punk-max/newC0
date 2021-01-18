package miniplc0java.Analyser;

import java.util.ArrayList;
import java.util.Arrays;

public class VmStack {
    ArrayList<StackEnum>stackEnums=new ArrayList<>();
    public void push(StackEnum...stackEnums){
        this.stackEnums.addAll(Arrays.asList(stackEnums));
    }
    public void push(IdentType... identType){
        for (IdentType type : identType) {
            this.stackEnums.add(change(type));
        }
    }
    public void pop(StackEnum...stackEnums){
        for(int i=stackEnums.length-1;i>=0;i--){
            if(stackEnums[i]==top()){
                this.stackEnums.remove(this.stackEnums.size()-1);
            }else
                throw new Error(Analyser.getPos());
        }
        return;
    }
    public void pop(IdentType... identTypes){
        for(int i = identTypes.length-1; i>=0; i--){
            if(change(identTypes[i])==this.stackEnums.get(this.stackEnums.size()-1)){
                this.stackEnums.remove(top());
            }else
                throw new Error(Analyser.getPos());
        }
        return;
    }
    public StackEnum preTop() {
        return stackEnums.get(stackEnums.size()-2);
    }
    public StackEnum change(IdentType identType){
        switch (identType) {
            case INT:
                return StackEnum.INT;
            case DOUBLE:
                return StackEnum.DOUBLE;
            default:
                return null;
        }
    }
    public StackEnum top(){
        return stackEnums.get(stackEnums.size()-1);
    }

    public VmStack() {
    }

    public ArrayList<StackEnum> getStackEnums() {
        return stackEnums;
    }

    public void setStackEnums(ArrayList<StackEnum> stackEnums) {
        this.stackEnums = stackEnums;
    }
}
