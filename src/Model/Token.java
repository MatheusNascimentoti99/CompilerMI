package Model;

import java.util.Formatter;

public class Token {

    public enum T {
        PRE, IDE, NRO, LOG, ART, REL, DEL, CAD, CoMF, EOF, SIB, OpMF, NMF, CMF
    };
    public T type;
    public Object val;
    public int line;
    public int col;
    public boolean isError;

    public Token(T type, int[] position) {
        this.type = type;
        if (type == T.CoMF || type == T.SIB || type == T.OpMF || type == T.NMF || type == T.CMF) {
            isError = true;
        }
        this.line = position[0];
        this.col = position[1];
    }

    public Token(T type, Object val, int[] position) {
        this.type = type;
        this.val = val;
        this.line = position[0];
        this.col = position[1];
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
