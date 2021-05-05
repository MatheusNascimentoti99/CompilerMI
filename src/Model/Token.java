package Model;

import java.util.Formatter;

public class Token {

    public enum T {
        PRE, IDE, NRO, LOG, ART, REL, DEL, CAD, CoMF, EOF, SIB, OpMF, NMF, CMF, ParserError
    };
    public T type;
    public Object val;
    public int line;
    public int col;

    public Token(T type, int[] position) {
        this.type = type;
        this.line = position[0];
        this.col = position[1];
    }

    public boolean isError() {
        if (type == T.SIB || type == T.OpMF || type == T.NMF || type == T.CMF) {
            return true;
        }
        return false;
    }

    public Token(T type, Object val, int[] position) {
        this.type = type;
        this.val = val;
        this.line = position[0];
        this.col = position[1];
    }
    public Token(T type, Object val, int positionLine) {
        this.type = type;
        this.val = val;
        this.line = positionLine;
        this.col = 0;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%d %s", line, type);
        if (val != null) {
            out.format(" %s", val);
        }
        return out.toString();
    }
}
