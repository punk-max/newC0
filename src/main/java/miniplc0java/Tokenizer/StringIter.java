package miniplc0java.Tokenizer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StringIter {
    private Pos pos=new Pos();
    private FileReader fileReader;
    private char thisChar,nextChar;
    public StringIter(File file) throws IOException {
        fileReader=new FileReader(file);
        nextChar=(char)fileReader.read();
    }
    public char peekChar(){
            return nextChar;
    }
    public char getChar(){
        return thisChar;
    }
    public char nextChar() throws IOException {
        if(thisChar=='\n')
            pos.nextRow();
        else
            pos.nextCol();
        thisChar=nextChar;
        nextChar=(char)fileReader.read();
        return thisChar;
    }
    public char expectChar(char a) throws IOException {
        if(!ifNextChar(a))
            throw new Error("row: "+pos.getRow()+" col: "+pos.getCol());
        return thisChar;
    }
    public char expectChar(char a,char b) throws IOException {
        if(!ifNextChar(a,b))
            throw new Error("row: "+pos.getRow()+" col: "+pos.getCol());
        return thisChar;
    }
    public boolean ifNextChar(char a) throws IOException {
        if(nextChar==a){
            nextChar();
            return true;
        }else
            return false;
    }
    public boolean ifNextChar(char a,char b) throws IOException {
        if(a<=nextChar&&nextChar<=b){
            nextChar();
            return true;
        }else
            return false;
    }
    public Pos getPos(){
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }

    public FileReader getFileReader() {
        return fileReader;
    }

    public void setFileReader(FileReader fileReader) {
        this.fileReader = fileReader;
    }

    public char getThisChar() {
        return thisChar;
    }

    public void setThisChar(char thisChar) {
        this.thisChar = thisChar;
    }

    public char getNextChar() {
        return nextChar;
    }

    public void setNextChar(char nextChar) {
        this.nextChar = nextChar;
    }
}
