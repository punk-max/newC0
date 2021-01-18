package miniplc0java.Analyser;

import java.util.ArrayList;

public class Scope {

    public ArrayList<ArrayList<Ident>>variableLists=new ArrayList<>();

    public void addScope(){
        variableLists.add(new ArrayList<>());
    }

    public void addVariable(Ident ident){
        ArrayList<Ident> idents =variableLists.get(variableLists.size()-1);
        for(int i = 0; i< idents.size(); i++){
            if(idents.get(i).name.equals(ident.name))
                throw new Error(Analyser.getPos());
        }
        variableLists.get(variableLists.size()-1).add(ident);
    }
    public void addGlobalVariable(Ident ident){
        variableLists.get(0).add(ident);
    }
    public void removeScope(){
        variableLists.remove(variableLists.size()-1);
    }
    public Ident getVariable(String name){
        for(int i=variableLists.size()-1;i>=0;i--){
            ArrayList<Ident> idents =variableLists.get(i);
            for(int j = idents.size()-1; j>=0; j--){
                if(name.equals(idents.get(j).name))
                    return idents.get(j);
            }
        }
        throw new Error(Analyser.getPos());
    }

    public Scope() {
    }

    public ArrayList<ArrayList<Ident>> getVariableLists() {
        return variableLists;
    }

    public void setVariableLists(ArrayList<ArrayList<Ident>> variableLists) {
        this.variableLists = variableLists;
    }
}
