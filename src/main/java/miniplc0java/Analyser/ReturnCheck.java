package miniplc0java.Analyser;

import java.util.ArrayList;

public class ReturnCheck {
    ArrayList<ReturnPoint>returnPoints=new ArrayList<>();
    public ReturnPoint top(){
        return returnPoints.get(returnPoints.size()-1);
    }
    public ReturnPoint preTop(){
        return returnPoints.get(returnPoints.size()-2);
    }
    public void getResult(){
        while (true){
            if(top().returnType == ReturnType.FUNCTION)break;
            else if(top().returnType == ReturnType.IF){
                returnPoints.remove(returnPoints.size()-1);
                break;
            }else if(top().returnType == ReturnType.ELSE&&preTop().returnType == ReturnType.ELSE_IF){
                if(!top().isReturn ||!preTop().isReturn)
                    top().isReturn =false;
                returnPoints.remove(returnPoints.size()-2);
            }else if(top().returnType == ReturnType.ELSE&&preTop().returnType == ReturnType.IF){
                if(top().isReturn &&preTop().isReturn)
                    returnPoints.get(returnPoints.size()-3).isReturn =true;
                returnPoints.remove(returnPoints.size()-1);
                returnPoints.remove(returnPoints.size()-1);
                break;
            }else if(top().returnType == ReturnType.ELSE_IF&&preTop().returnType == ReturnType.IF){
                returnPoints.remove(returnPoints.size()-1);
                returnPoints.remove(returnPoints.size()-1);
                break;
            }else if(top().returnType == ReturnType.ELSE_IF&&preTop().returnType == ReturnType.ELSE_IF){
                returnPoints.remove(returnPoints.size()-1);
                returnPoints.remove(returnPoints.size()-1);
            }else if(top().returnType == ReturnType.IF&&preTop().returnType == ReturnType.FUNCTION){
                returnPoints.remove(returnPoints.size()-1);
            }
        }
    }

    public ReturnCheck() {
    }

    public ArrayList<ReturnPoint> getReturnPoints() {
        return returnPoints;
    }

    public void setReturnPoints(ArrayList<ReturnPoint> returnPoints) {
        this.returnPoints = returnPoints;
    }
}
