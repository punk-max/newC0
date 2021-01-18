package miniplc0java.Analyser;

import java.util.ArrayList;

public class FunctionList {

    ArrayList<Function>functions=new ArrayList<>();

    Function topFunction=null;

    public void add(Function function){
        for(int i=1;i<functions.size();i++){
            if(functions.get(i).name.equals(function.name))
                throw new Error(Analyser.getPos());
        }

        topFunction=function;

        functions.add(function);
    }
    public void addVariable(Ident ident){

        if(ident.isGlobal){
            ident.offset=functions.get(0).params.size();
            functions.get(0).params.add(ident);
        }
        else if(ident.isLocal){
            ident.offset=topFunction.locals.size();
            topFunction.locals.add(ident);
            topFunction.locSlot++;
        }
        else if(ident.isParam){
            ident.offset=topFunction.params.size();
            topFunction.params.add(ident);
            topFunction.paramSlot++;
        }
        else
            throw new Error(Analyser.getPos());
    }

    public void setVariableType(IdentType identType){
        topFunction.identType = identType;
    }

    public void setReturnSlot(){
        topFunction.returnSlot=1;
    }

    public void addInstruction(String instructionString,byte...instruction){
        addInstruction(topFunction,instructionString,instruction);
    }

    public void addInitialInstruction(String instructionString,byte...instruction){
        addInstruction(functions.get(0),instructionString,instruction);
    }

    public void addInstruction(Function function,String instructionString,byte...instruction){
        function.instructionsString.add(instructionString);
        function.instructions.add(new ArrayList<>());
        int num=function.instructions.size()-1;
        for(byte b:instruction)function.instructions.get(num).add(b);
        function.instructionNum++;
    }

    public void replaceInstruction(int pos,String instructionString,byte...instruction){
        topFunction.instructionsString.remove(pos);
        topFunction.instructionsString.add(pos,instructionString);
        topFunction.instructions.get(pos).clear();
        for(byte b:instruction)topFunction.instructions.get(pos).add(b);
    }

    public int getInstructionNum(){return topFunction.instructionNum;}

    public Integer searchFunction(String name){
        for(String s:Analyser.lib){
            if(s.equals(name))
                return null;
        }
        for(int i=1;i<functions.size();i++){
            if(functions.get(i).name.equals(name))
                return i;
        }
        throw new Error(Analyser.getPos());
    }

    public FunctionList() {
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void setFunctions(ArrayList<Function> functions) {
        this.functions = functions;
    }

    public Function getTopFunction() {
        return topFunction;
    }

    public void setTopFunction(Function topFunction) {
        this.topFunction = topFunction;
    }
}
