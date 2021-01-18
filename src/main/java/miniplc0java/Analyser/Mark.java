package miniplc0java.Analyser;

public class Mark {

    int instructionId;

    boolean isBreak;

    public Mark(int instructionId, boolean isBreak){
        this.instructionId = instructionId;
        this.isBreak=isBreak;
    }

    public Mark() {
    }

    public int getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(int instructionId) {
        this.instructionId = instructionId;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }
}
