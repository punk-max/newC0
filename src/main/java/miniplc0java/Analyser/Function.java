package miniplc0java.Analyser;

import java.util.ArrayList;

public class Function {

    String name;

    int IDInGlobal=-1;

    int returnSlot=0;

    int paramSlot=0;

    int locSlot=0;

    int instructionNum=0;

    IdentType identType;

    ArrayList<ArrayList<Byte>>instructions=new ArrayList<>();

    ArrayList<String>instructionsString=new ArrayList<>();

    ArrayList<Ident> params=new ArrayList<>();

    ArrayList<Ident> locals=new ArrayList<>();

    public Function(String name){
        this.name=name;
    }

    public Function() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIDInGlobal() {
        return IDInGlobal;
    }

    public void setIDInGlobal(int IDInGlobal) {
        this.IDInGlobal = IDInGlobal;
    }

    public int getReturnSlot() {
        return returnSlot;
    }

    public void setReturnSlot(int returnSlot) {
        this.returnSlot = returnSlot;
    }

    public int getParamSlot() {
        return paramSlot;
    }

    public void setParamSlot(int paramSlot) {
        this.paramSlot = paramSlot;
    }

    public int getLocSlot() {
        return locSlot;
    }

    public void setLocSlot(int locSlot) {
        this.locSlot = locSlot;
    }

    public int getInstructionNum() {
        return instructionNum;
    }

    public void setInstructionNum(int instructionNum) {
        this.instructionNum = instructionNum;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public void setIdentType(IdentType identType) {
        this.identType = identType;
    }

    public void setInstructions(ArrayList<ArrayList<Byte>> instructions) {
        this.instructions = instructions;
    }

    public void setInstructionsString(ArrayList<String> instructionsString) {
        this.instructionsString = instructionsString;
    }

    public ArrayList<Ident> getParams() {
        return params;
    }

    public void setParams(ArrayList<Ident> params) {
        this.params = params;
    }

    public ArrayList<Ident> getLocals() {
        return locals;
    }

    public void setLocals(ArrayList<Ident> locals) {
        this.locals = locals;
    }

    public String getInstructionsString(){
        StringBuilder stringBuilder=new StringBuilder();
        for(int i=0;i<instructionsString.size();i++){
            stringBuilder.append(i+":\t");
            stringBuilder.append(instructionsString.get(i)+"\n");
        }
        return stringBuilder.toString();
    }

    public ArrayList<Byte> getInstructions() {
        ArrayList<Byte>bytes=new ArrayList<>();
        for(ArrayList<Byte>instructions:this.instructions)bytes.addAll(instructions);
        return bytes;
    }


}
