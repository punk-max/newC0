package miniplc0java.Analyser;

import miniplc0java.Tokenizer.*;

import java.io.*;
import java.util.ArrayList;

public class Analyser {
    static Tokenizer t;

    DataOutputStream output;

    FunctionList functionList=new FunctionList();

    Scope scope=new Scope();

    boolean Functing =false;

    ArrayList<Boolean>br=new ArrayList<>();

    ArrayList<Mark> Marks =new ArrayList<>();

    VmStack vmStack =new VmStack();

    int loopLevel=0;

    ReturnCheck returnCheck=new ReturnCheck();

    static String[] lib ={"getint","getdouble","getchar","putint","putdouble","putchar","putstr","putln"};

    private void unexpected(boolean flag){
        if(!flag)
            throw new Error(getPos());
    }

    private void AnalyseProgram() throws IOException {
        while(declStmt());
        while(t.peekToken()!=null)
            unexpected(function());

        Integer entry = null;
        for(int i=0;i<functionList.functions.size();i++){
            if(functionList.functions.get(i).name.equals("main")){
                entry = i;
                break;
            }
        }
        if(entry==null)
            throw new Error(getPos());
        if(functionList.functions.get(entry).identType != IdentType.VOID){
            functionList.addInstruction("stackalloc "+1,toByteInt(0x1a,1));
        }
        functionList.addInitialInstruction("call "+entry,toByteInt(0x48,entry));
        functionList.addVariable(new Ident("_start",true, IdentType.STRING,true,false,false,"_start"));
        functionList.functions.get(0).IDInGlobal=functionList.functions.get(0).params.size()-1;
    }

    private boolean declStmt() {
        boolean isConst;
        if(t.ifNextToken(TokenType.LET_KW))
            isConst=false;
        else if(t.ifNextToken(TokenType.CONST_KW))
            isConst=true;
        else
            return false;
        String name=t.expectToken(TokenType.IDENT).getValue().toString();
        IdentType identType =getVariableType();
        Ident ident =new Ident(name,isConst, identType,functionList.functions.size()==1,false,functionList.functions.size()!=1,null);
        functionList.addVariable(ident);
        scope.addVariable(ident);
        if(t.ifNextToken(TokenType.ASSIGN)){
            pushVariableAddress(name);
            unexpected(expr(false));
            functionList.addInstruction("store.64",(byte)0x17);
            vmStack.pop(identType);
            vmStack.pop(StackEnum.ADDR);
        }else if(isConst){
            throw new Error(getPos());
        }
        t.expectToken(TokenType.SEMICOLON);

        return true;
    }

    private boolean function() throws IOException {
        if(t.ifNextToken(TokenType.FN_KW)){
            String name=t.expectToken(TokenType.IDENT).getValue().toString();

            Function function=new Function(name);

            functionList.add(function);

            functionList.addVariable(new Ident(name,true, IdentType.STRING,true,false,false,name));

            function.IDInGlobal=functionList.functions.get(0).params.size()-1;

            scope.addScope();

            t.expectToken(TokenType.L_PAREN);
            functionParamList();
            t.expectToken(TokenType.R_PAREN);
            t.expectToken(TokenType.ARROW);
            Token type=t.expectToken(TokenType.TYPE_KW);

            if(type.getValue().equals("int")){
                functionList.setVariableType(IdentType.INT);
                functionList.topFunction.params.add(0,new Ident(null,false, IdentType.INT,false,true,false,null));
                for(int i=1;i<functionList.topFunction.params.size();i++)
                    functionList.topFunction.params.get(i).offset++;
                functionList.setReturnSlot();
            }
            else if(type.getValue().equals("double")){
                functionList.setVariableType(IdentType.DOUBLE);
                functionList.topFunction.params.add(0,new Ident(null,false, IdentType.DOUBLE,false,true,false,null));
                for(int i=1;i<functionList.topFunction.params.size();i++)
                    functionList.topFunction.params.get(i).offset++;
                functionList.setReturnSlot();
            }
            else if(type.getValue().equals("void")){
                functionList.setVariableType(IdentType.VOID);
//                functions.setReturnSlot(0);
            }
            else
                throw new Error(getPos());
            Functing =true;
            returnCheck.returnPoints.add(new ReturnPoint(ReturnType.FUNCTION,false));
            unexpected(blockStmt());
            returnCheck.getResult();
            if(returnCheck.returnPoints.size()!=1)
                throw new Error(getPos());
            else if(function.identType != IdentType.VOID&&!returnCheck.returnPoints.get(0).isReturn)
                throw new Error(getPos());
            returnCheck.returnPoints.remove(0);
            if(function.identType == IdentType.VOID&&(function.instructionNum==0||!function.instructionsString.get(function.instructionNum-1).equals("ret"))){
                functionList.addInstruction("ret",(byte)0x49);
            }
        }else
            return false;
        return true;
    }
    private boolean functionParamList() {
        if(t.peekToken().getTokenType()!=TokenType.R_PAREN){
            unexpected(functionParam());
            while(t.ifNextToken(TokenType.COMMA)){
                unexpected(functionParam());
            }
        }else
            return false;
        return true;
    }
    private boolean functionParam() {
        boolean isConst=false;
        if(t.ifNextToken(TokenType.CONST_KW))
            isConst=true;
        String name=t.expectToken(TokenType.IDENT).getValue().toString();
        IdentType identType =getVariableType();
        Ident ident =new Ident(name,isConst, identType,false,true,false,null);
        scope.addVariable(ident);
        functionList.addVariable(ident);
        return true;
    }
    private boolean blockStmt() throws IOException {
        if(t.ifNextToken(TokenType.L_BRACE)){
            if(!Functing)
                scope.addScope();
            Functing =false;
            while(!t.ifNextToken(TokenType.R_BRACE)){
                unexpected(stmt());
            }
            scope.removeScope();
        }else
            return false;
        return true;
    }
    private boolean stmt() throws IOException {
        if(declStmt()){
        }else if(t.peekToken().getTokenType()==TokenType.IF_KW){
            if(loopLevel==0)
                returnCheck.returnPoints.add(new ReturnPoint(ReturnType.IF,false));
            ifStmt(true);
        }else if(whileStmt()){
        }else if(returnStmt()){
        }else if(blockStmt()){
        }else if(t.ifNextToken(TokenType.SEMICOLON)){
        }else if(breakContinueStmt()){
        }else if(expr(false)){
            t.expectToken(TokenType.SEMICOLON);
        }else
            return false;
        return true;
    }
    private void ifStmt(boolean isFirst) throws IOException {
        if(t.ifNextToken(TokenType.IF_KW)){
            unexpected(expr(true));
            vmStack.pop(StackEnum.BOOL);
            int brStart=functionList.getInstructionNum();
            functionList.addInstruction("null",(byte)0);
            unexpected(blockStmt());
            int brNum=functionList.getInstructionNum()-brStart-1;
            if(t.ifNextToken(TokenType.ELSE_KW)){
                int skipStart=functionList.getInstructionNum();
                brNum++;
                functionList.addInstruction("null",(byte)0);
                if(t.peekToken().getTokenType()==TokenType.L_BRACE){
                    if(loopLevel==0)
                        returnCheck.returnPoints.add(new ReturnPoint(ReturnType.ELSE,false));
                    blockStmt();
                }else if(t.peekToken().getTokenType()==TokenType.IF_KW){
                    if(loopLevel==0)
                        returnCheck.returnPoints.add(new ReturnPoint(ReturnType.ELSE_IF,false));
                    ifStmt(false);
                }else
                    throw new Error(getPos());
                int skipNum=functionList.getInstructionNum()-skipStart-1;
//                replaceBrInstruction(skipStart,skipNum);
                functionList.replaceInstruction(skipStart,"br "+skipNum,toByteInt(0x41,skipNum));
            }
            if(isFirst&&loopLevel==0)
                returnCheck.getResult();
            replaceBrInstruction(brStart,brNum);
        }
    }
    private boolean whileStmt()throws IOException {
        if(t.ifNextToken(TokenType.WHILE_KW)){
            int returnPos=functionList.getInstructionNum();
            int existedBreakPoint= Marks.size();
            unexpected(expr(true));
            vmStack.pop(StackEnum.BOOL);
            int brStart=functionList.getInstructionNum();
            functionList.addInstruction("null",(byte)0);
            loopLevel++;
            unexpected(blockStmt());
            loopLevel--;
            for(int i = Marks.size()-1; Marks.size()!=existedBreakPoint; i--){
                Mark mark = Marks.get(i);
                if(mark.isBreak){
                    int brNum=functionList.getInstructionNum()- mark.instructionId;
                    functionList.replaceInstruction(mark.instructionId,"br "+brNum,toByteInt(0x41,brNum));
                }else{
                    int returnNum=returnPos- mark.instructionId -1;
                    functionList.replaceInstruction(mark.instructionId,"br "+returnNum,toByteInt(0x41,returnNum));
                }
                Marks.remove(i);
            }
            int brNum=functionList.getInstructionNum()-brStart;
            replaceBrInstruction(brStart,brNum);
            int returnNum=returnPos-functionList.getInstructionNum()-1;
            functionList.addInstruction("br "+returnNum,toByteInt(0x41,returnNum));
            return true;
        }
        return false;
    }

    private boolean breakContinueStmt(){
        if(t.ifNextToken(TokenType.BREAK_KW))
        {
            if(loopLevel==0)
                throw new Error("token "+t.getThisToken().getValue().toString()+"cannot be here "+getPos());
            else{
                Marks.add(new Mark(functionList.getInstructionNum(),true));
                functionList.addInstruction("null",(byte)0);
            }
        }

        else if(t.ifNextToken(TokenType.CONTINUE_KW))
        {
            if(loopLevel==0)
                throw new Error("token "+t.getThisToken().getValue().toString()+"cannot be here "+getPos());
            else{
                Marks.add(new Mark(functionList.getInstructionNum(),false));
                functionList.addInstruction("null",(byte)0);
            }
        }
        else
            return false;
        t.expectToken(TokenType.SEMICOLON);
        return true;
    }

    private boolean returnStmt() {
        if(t.ifNextToken(TokenType.RETURN_KW)){
            if(functionList.topFunction.identType != IdentType.VOID){
                functionList.addInstruction("arga 0",toByteInt(0x0b,0));
                vmStack.push(StackEnum.ADDR);
                unexpected(expr(false));
                functionList.addInstruction("store.64",(byte)0x17);
                if(functionList.topFunction.identType == IdentType.INT)
                    vmStack.pop(StackEnum.INT);
                else
                    vmStack.pop(StackEnum.DOUBLE);
                vmStack.pop(StackEnum.ADDR);
            }
            t.expectToken(TokenType.SEMICOLON);
            if(loopLevel==0)
                returnCheck.top().isReturn =true;
            functionList.addInstruction("ret",(byte)0x49);
        }else
            return false;
        return true;
    }
    private boolean expr(boolean isBool){
        t.savePoint();
        if(t.ifNextToken(TokenType.IDENT)&&t.ifNextToken(TokenType.ASSIGN)){
            t.loadPoint();
            t.removePoint();
            String name=t.expectToken(TokenType.IDENT).getValue().toString();
            Ident ident =pushVariableAddress(name);
            if(ident.isConst){
                throw new Error(getPos());
            }
            t.expectToken(TokenType.ASSIGN);
            unexpected(assignExpr(isBool));
            functionList.addInstruction("store.64", (byte)0x17);
            vmStack.pop(ident.identType);
        }else{
            t.loadPoint();
            t.removePoint();
            assignExpr(isBool);
        }
        return true;
    }
    private boolean assignExpr(boolean isBool){
        if(compareExpr()){
            while(true){
                String s;
                if(t.ifNextToken(TokenType.GE)){
                    unexpected(compareExpr());
                    s="GE";
                }
                else if(t.ifNextToken(TokenType.LE)){
                    unexpected(compareExpr());
                    s="LE";
                }
                else if(t.ifNextToken(TokenType.GT)){
                    unexpected(compareExpr());
                    s="GT";
                }
                else if(t.ifNextToken(TokenType.LT)){
                    unexpected(compareExpr());
                    s="LT";
                }
                else if(t.ifNextToken(TokenType.EQ)){
                    unexpected(compareExpr());
                    s="EQ";
                }
                else if(t.ifNextToken(TokenType.NEQ)){
                    unexpected(compareExpr());
                    s="NEQ";
                }
                else{
                    if(isBool && vmStack.top() == StackEnum.INT){
                        br.add(false);
                        vmStack.pop(StackEnum.INT);
                        vmStack.push(StackEnum.BOOL);
                    }else if(isBool&& vmStack.top()==StackEnum.DOUBLE){
                        br.add(false);
                        vmStack.pop(StackEnum.DOUBLE);
                        vmStack.push(StackEnum.BOOL);
                    }
                    break;
                }

                compare();

                if(s.equals("GE")){
                    functionList.addInstruction("set.lt",(byte)0x39);
                    br.add(true);
                }else if(s.equals("LE")){
                    functionList.addInstruction("set.gt",(byte)0x3a);
                    br.add(true);
                }else if(s.equals("GT")){
                    functionList.addInstruction("set.gt",(byte)0x3a);
                    br.add(false);
                }else if(s.equals("LT")){
                    functionList.addInstruction("set.lt",(byte)0x39);
                    br.add(false);
                }else if(s.equals("EQ")){
                    br.add(true);
                }else{
                    br.add(false);
                }
            }
        }else
            return false;
        return true;
    }
    private boolean compareExpr(){
        if(plusMinusExpr()){
            while(true){
                if(t.ifNextToken(TokenType.PlUS)){
                    unexpected(plusMinusExpr());
                    if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.INT){
                        functionList.addInstruction("add.i",(byte)0x20);
                        vmStack.pop(StackEnum.INT);
                    }else if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.DOUBLE) {
                        functionList.addInstruction("add.f", (byte)0x24);
                        vmStack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(getPos());
                }else if(t.ifNextToken(TokenType.MINUS)){
                    unexpected(plusMinusExpr());
                    if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.INT){
                        functionList.addInstruction("sub.i",(byte)0x21);
                        vmStack.pop(StackEnum.INT);
                    }else if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.DOUBLE){
                        functionList.addInstruction("sub.f",(byte)0x25);
                        vmStack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(getPos());
                }else
                    break;
            }
        }else
            return false;
        return true;
    }
    private boolean plusMinusExpr(){
        if(mulDivExpr()){
            while(true){
                if(t.ifNextToken(TokenType.MUL)){
                    unexpected(mulDivExpr());
                    if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.INT){
                        functionList.addInstruction("mul.i",(byte)0x22);
                        vmStack.pop(StackEnum.INT);
                    }else if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.DOUBLE){
                        functionList.addInstruction("mul.f",(byte)0x26);
                        vmStack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(getPos());
                }else if(t.ifNextToken(TokenType.DIV)){
                    unexpected(mulDivExpr());
                    if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.INT){
                        functionList.addInstruction("div.i",(byte)0x23);
                        vmStack.pop(StackEnum.INT);
                    }else if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.DOUBLE){
                        functionList.addInstruction("div.f",(byte)0x27);
                        vmStack.pop(StackEnum.DOUBLE);
                    }else
                        throw new Error(getPos());
                }else
                    break;
            }
        }else
            return false;
        return true;
    }
    private boolean mulDivExpr(){
        if(factor()){
            if(vmStack.stackEnums.size()==0)
                return true;
            String name= vmStack.top().toString();
            while(t.ifNextToken(TokenType.AS_KW)){
                name=t.expectToken(TokenType.TYPE_KW).getValue().toString();
                if(!name.equals("int")&&!name.equals("double"))
                    throw new Error(getPos());
            }
            if(vmStack.top().toString().equals(name)){
            }else if(vmStack.top().toString().equals("int")&&name.equals("double")){
                functionList.addInstruction("int to float",(byte)0x36);
                vmStack.pop(StackEnum.INT);
                vmStack.push(StackEnum.DOUBLE);
            }else if(vmStack.top().toString().equals("double")&&name.equals("int")){
                functionList.addInstruction("float to int",(byte)0x37);
                vmStack.pop(StackEnum.DOUBLE);
                vmStack.push(StackEnum.INT);
            }
        }else
            return false;
        return true;
    }
    private boolean factor(){
        boolean neg= negateFactor();
        t.savePoint();
        if(groupExpr()){

        }else if(callExpr()){

        }else if(identExpr()){

        }else if(t.ifNextToken(TokenType.INT)){
            functionList.addInstruction("push "+(long)t.getThisToken().getValue(),toByteLong(0x01, (Long) t.getThisToken().getValue()));
            vmStack.push(StackEnum.INT);
        }else if(t.ifNextToken(TokenType.DOUBLE)){
            functionList.addInstruction("push "+(double)t.getThisToken().getValue(),toByteDouble(0x01,(Double) t.getThisToken().getValue()));
            vmStack.push(StackEnum.DOUBLE);
        }else if(t.ifNextToken(TokenType.STRING)){
            String s=t.getThisToken().getValue().toString();
            functionList.addVariable(new Ident(null,true, IdentType.STRING,true,false,false,s));
            scope.addGlobalVariable(new Ident(null,true, IdentType.STRING,true,false,false,s));
            long ID= (long) functionList.functions.get(0).params.size()-1;
            functionList.addInstruction("push "+ID,toByteLong(0x01,(Long)ID));
            vmStack.push(StackEnum.INT);
        }else if(t.ifNextToken(TokenType.CHAR)){
            functionList.addInstruction("push "+ (long)(char)t.getThisToken().getValue(),toByteLong(0x01, (long)(char)t.getThisToken().getValue()));
            vmStack.push(StackEnum.INT);
        }else
            return false;
        t.removePoint();
        if(!neg&&(vmStack.top()==StackEnum.INT))
            functionList.addInstruction("neg.i",(byte)0x34);
        else if(!neg&&(vmStack.top()==StackEnum.DOUBLE))
            functionList.addInstruction("neg.f",(byte)0x35);
        else if(!neg)
            throw new Error(getPos());
        return true;
    }
    private boolean negateFactor(){
        boolean b=true;
        while(t.ifNextToken(TokenType.MINUS))b=!b;
        return b;
    }
    private boolean groupExpr(){
        if(t.ifNextToken(TokenType.L_PAREN)){
            unexpected(expr(false));
            t.expectToken(TokenType.R_PAREN);
        }else
            return false;
        return true;
    }
    private boolean callExpr(){
        if(t.ifNextToken(TokenType.IDENT)&&t.ifNextToken(TokenType.L_PAREN)){
            t.loadPoint();
            String name=t.expectToken(TokenType.IDENT).getValue().toString();
            Integer functionID=functionList.searchFunction(name);
            Function function;
            if(functionID!=null)
                function=functionList.functions.get(functionID);
            else
                function=null;
            int expectedParamNum=0,actualParamNum=0;
            if(function!=null){
                expectedParamNum=function.paramSlot;
                if(function.identType == IdentType.INT){
                    functionList.addInstruction("stackalloc "+1,toByteInt(0x1a,1));
                    vmStack.push(StackEnum.INT);
                }else if(function.identType == IdentType.DOUBLE){
                    functionList.addInstruction("stackalloc "+1,toByteInt(0x1a,1));
                    vmStack.push(StackEnum.DOUBLE);
                }
            }
            t.expectToken(TokenType.L_PAREN);
            if(!t.ifNextToken(TokenType.R_PAREN)){
                unexpected(false);
                actualParamNum++;
                while (!t.ifNextToken(TokenType.R_PAREN)){
                    t.expectToken(TokenType.COMMA);
                    unexpected(expr(false));
                    actualParamNum++;
                }
            }
            if(function==null){
                if((name.equals(lib[3])||name.equals(lib[4])||name.equals(lib[5])||name.equals(lib[6]))&&actualParamNum!=1)
                    throw new Error(getPos());
                else if((name.equals(lib[0])||name.equals(lib[1])||name.equals(lib[2])||name.equals(lib[7]))&&actualParamNum!=0)
                    throw new Error(getPos());
                addStdioFunctionInstruction(name);
            }else{
                if(expectedParamNum!=actualParamNum)
                    throw new Error(getPos());
                for(int i=function.params.size()-1;i>0;i--){
                    if(function.params.get(i).identType.toString().equals(vmStack.top().toString())){
                        vmStack.pop(vmStack.top());
                    }else
                        throw new Error(getPos());
                }
                functionList.addInstruction("call "+functionID,toByteInt(0x48,functionID));
            }
        }else{
            t.loadPoint();
            return false;
        }
        return true;
    }
    private boolean identExpr(){
        if(t.ifNextToken(TokenType.IDENT)){
            String name=t.getThisToken().getValue().toString();
            Ident ident =pushVariableAddress(name);
            functionList.addInstruction("load.64",(byte)0x13);
            vmStack.pop(StackEnum.ADDR);
            vmStack.push(ident.identType);
        }else
            return false;
        return true;
    }

    private void output() throws IOException {
        output.writeInt(0x72303b3e);

        output.writeInt(0x00000001);

        int globalVariableNum=functionList.functions.get(0).params.size();
        output.writeInt(globalVariableNum);
        for(int i=0;i<globalVariableNum;i++){
            Ident ident =functionList.functions.get(0).params.get(i);
            output.writeByte(ident.isConst?1:0);
            if(ident.identType != IdentType.STRING){
                output.writeInt(8);
                output.writeLong(0);
            }
            else{
                output.writeInt(ident.value.length());
                for(int j = 0; j< ident.value.length(); j++){
                    output.writeByte(ident.value.toCharArray()[j]);
                }
            }
        }
        output.writeInt(functionList.functions.size());
        for(int i=0;i<functionList.functions.size();i++){
            Function function=functionList.functions.get(i);
            output.writeInt(function.IDInGlobal);
            output.writeInt(function.returnSlot);
            output.writeInt(function.paramSlot);
            output.writeInt(function.locSlot);
            output.writeInt(function.instructionNum);
            ArrayList<Byte>instructions=function.getInstructions();
            for (Byte instruction : instructions) {
                output.write(instruction);
            }
        }
        output.flush();
        output.close();
    }

    private byte[] toByteLong(int instruction,Long num){
        byte[]bytes=new byte[8+1];
        bytes[0]=(byte)instruction;
        for(int i=8-1;i>=0;i--){
            bytes[8-i]=(byte)(num>>(i*8));
        }
        return bytes;
    }
    private byte[] toByteInt(int instruction,Integer num){
        byte[]bytes=new byte[4+1];
        bytes[0]=(byte)instruction;
        for(int i=4-1;i>=0;i--){
            bytes[4-i]=(byte)(num>>(i*8));
        }
        return bytes;
    }

    private byte[] toByteDouble(int instruction,double num){
        return toByteLong(instruction,Double.doubleToLongBits(num));
    }

    private Ident pushVariableAddress(String name){
        Ident ident =scope.getVariable(name);
        if(ident.isGlobal) {
            functionList.addInstruction("global "+ ident.offset, toByteInt(0x0c, ident.offset));
            vmStack.push(StackEnum.ADDR);
        }else if(ident.isParam){
            functionList.addInstruction("arga "+ ident.offset, toByteInt(0x0b, ident.offset));
            vmStack.push(StackEnum.ADDR);
        }else if(ident.isLocal){
            functionList.addInstruction("local "+ ident.offset, toByteInt(0x0a, ident.offset));
            vmStack.push(StackEnum.ADDR);
        }
        return ident;
    }
    private IdentType getVariableType(){
        IdentType identType;
        t.expectToken(TokenType.COLON);
        Token type=t.expectToken(TokenType.TYPE_KW);
        if(type.getValue().equals("void"))
            throw new Error(getPos());
        else if(type.getValue().equals("int"))
            identType = IdentType.INT;
        else if(type.getValue().equals("double"))
            identType = IdentType.DOUBLE;
        else
            throw new Error(getPos());
        return identType;
    }
    private void compare(){
        if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.INT){
            functionList.addInstruction("cmp.i",(byte)0x30);
            vmStack.pop(StackEnum.INT,StackEnum.INT);
            vmStack.push(StackEnum.BOOL);
        }else if(vmStack.top()== vmStack.preTop()&& vmStack.preTop()==StackEnum.DOUBLE){
            functionList.addInstruction("cmp.f",(byte)0x32);
            vmStack.pop(StackEnum.DOUBLE,StackEnum.DOUBLE);
            vmStack.push(StackEnum.BOOL);
        }else
            throw new Error(getPos());
    }

    private void replaceBrInstruction(int start,int num){
        boolean b=br.get(br.size()-1);
        br.remove(br.size()-1);
        if(b)
            functionList.replaceInstruction(start,"notZero "+num,toByteInt(0x43,num));
        else
            functionList.replaceInstruction(start,"isZero "+num,toByteInt(0x42,num));
    }

    public void addStdioFunctionInstruction(String name){
        if(name.equals(lib[0])){
            functionList.addInstruction("scan.i",(byte)0x50);
            vmStack.push(StackEnum.INT);

        }else if(name.equals(lib[1])){
            functionList.addInstruction("scan.f",(byte)0x52);
            vmStack.push(StackEnum.DOUBLE);

        }else if(name.equals(lib[2])){
            functionList.addInstruction("scan.c",(byte)0x51);
            vmStack.push(StackEnum.INT);

        }else if(name.equals(lib[3])){
            functionList.addInstruction("print.i",(byte)0x54);
            vmStack.pop(StackEnum.INT);

        }else if(name.equals(lib[4])){
            functionList.addInstruction("print.f",(byte)0x56);
            vmStack.pop(StackEnum.DOUBLE);

        }else if(name.equals(lib[5])){
            functionList.addInstruction("print.c",(byte)0x55);
            vmStack.pop(StackEnum.INT);

        }else if(name.equals(lib[6])){
            functionList.addInstruction("print.s",(byte)0x57);
            vmStack.pop(StackEnum.INT);

        }else if(name.equals(lib[7])){
            functionList.addInstruction("println",(byte)0x58);
        }
    }
    public static String getPos(){
        return "at: row"+t.getThisToken().getStartPos().getRow()+" col"+t.getThisToken().getStartPos().getCol();
    }

    public Analyser(File file,File file2) throws IOException {
        t=new Tokenizer(file);
        output=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file2.getAbsoluteFile())));
        functionList.add(new Function("_start"));
        scope.addScope();
        AnalyseProgram();
        output();
    }

    public Analyser() {
    }

    public static Tokenizer getT() {
        return t;
    }

    public static void setT(Tokenizer t) {
        Analyser.t = t;
    }

    public DataOutputStream getOutput() {
        return output;
    }

    public void setOutput(DataOutputStream output) {
        this.output = output;
    }

    public FunctionList getFunctionList() {
        return functionList;
    }

    public void setFunctionList(FunctionList functionList) {
        this.functionList = functionList;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public boolean isFuncting() {
        return Functing;
    }

    public void setFuncting(boolean functing) {
        Functing = functing;
    }

    public VmStack getVmStack() {
        return vmStack;
    }

    public void setVmStack(VmStack vmStack) {
        this.vmStack = vmStack;
    }

    public ArrayList<Boolean> getBr() {
        return br;
    }

    public void setBr(ArrayList<Boolean> br) {
        this.br = br;
    }

    public ArrayList<Mark> getMarks() {
        return Marks;
    }

    public void setMarks(ArrayList<Mark> Marks) {
        this.Marks = Marks;
    }

    public int getLoopLevel() {
        return loopLevel;
    }

    public void setLoopLevel(int loopLevel) {
        this.loopLevel = loopLevel;
    }

    public ReturnCheck getReturnCheck() {
        return returnCheck;
    }

    public void setReturnCheck(ReturnCheck returnCheck) {
        this.returnCheck = returnCheck;
    }

    public static String[] getLib() {
        return lib;
    }

    public static void setLib(String[] lib) {
        Analyser.lib = lib;
    }
}
