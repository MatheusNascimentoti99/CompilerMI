/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Arrays;
import java.util.Formatter;

/**
 *
 * @author Matheus Nascimento
 */
public class TokenAfterParse extends Token {

    private String expected;
    private boolean panic;

    public TokenAfterParse(Token token) {
        super(token.type, token.val, token.line);
    }

    public TokenAfterParse(Object val, int positionLine, String expected) {
        super(Token.T.Parser, val, positionLine);
        this.expected = expected;
    }

    public TokenAfterParse(Object val, int positionLine, String expected, boolean panicMode) {
        super(Token.T.Parser, val, positionLine);
        this.expected = expected;
        this.panic = panicMode;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("%d %s", super.line, super.type);
        if (val != null) {
            out.format(" %s", super.val);
        }
        if (super.type == Token.T.Parser) {
            out.format("    Esperado: %s", expected);
        }
        if (panic) {
            out.format("    - Em modo pânico");

        }

        return out.toString();
    }
}
