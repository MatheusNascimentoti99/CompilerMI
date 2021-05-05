/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matheus Nascimento
 */
public class Parser {

    private final LinkedList<Token> tokens;
    private Token correntToken;
    private Token nextToken;
    private int erros;
    private final LinkedList<TokenAfterParse> result;

    public Parser(LinkedList<Token> tokens) {
        erros = 0;
        this.tokens = tokens;
        correntToken = tokens.pollFirst();
        nextToken = tokens.pollFirst();
        result = new LinkedList<>();
    }

    public int erros() {
        return erros;
    }

    public boolean hasErros() {
        return erros > 0;
    }

    public LinkedList<Token> getTokens() {
        return tokens;
    }

    public LinkedList<TokenAfterParse> getResult() {
        return result;
    }

    public Token seeNextToken() {
        return nextToken;
    }

    public Token getCorrentToken() {
        try {
            if (correntToken == null) {
                throw new NullPointerException();
            } else {
                return correntToken;
            }
        } catch (NullPointerException e) {
            int[] position = {-1, -1};
            return new Token(Token.T.EOF, "", position);
        }
    }

    public boolean equalsValue(String expected) {
        return correntToken.val.equals(expected);
    }

    public Token nextToken() {
        do {
            result.addLast(new TokenAfterParse(correntToken));
            correntToken = nextToken;
            nextToken = tokens.pollFirst();
        } while (correntToken != null && correntToken.isError());
        return correntToken;
    }

    public Token nextToken(boolean include) {
        do {
            if (include) {
                result.addLast(new TokenAfterParse(correntToken));
            }
            correntToken = nextToken;
            nextToken = tokens.pollFirst();
        } while (correntToken != null && correntToken.isError());

        return correntToken;
    }

    public void includeError(String expected) {
        erros++;
        String[] expecteds = expected.split(",");

        try {
            result.addLast(new TokenAfterParse(correntToken.val, correntToken.line, Arrays.toString(expecteds)));

        } catch (NullPointerException e) {
            result.addLast(new TokenAfterParse(correntToken.val, -1, Arrays.toString(expecteds)));
        }
//goNextToken(false);
    }

    public void includeError(String expected, List follows) {
        String[] expecteds = expected.split(",");
        result.addLast(new TokenAfterParse(correntToken.val, correntToken.line, Arrays.toString(expecteds)));
        erros++;
        while (tokens.size() > 0) {
            try {
                if (follows.contains(correntToken.val) || (follows.contains("IDE") && correntToken.type == Token.T.IDE)) {
                    break;
                } else {
                    result.addLast(new TokenAfterParse(correntToken.val, correntToken.line, Arrays.toString(expecteds), true));

                }
                nextToken(false);
            } catch (NullPointerException e) {
                Token errorParseEOF = new Token(Token.T.EOF, "", -1);
                result.addLast(new TokenAfterParse(errorParseEOF.val, -1, Arrays.toString(expecteds)));
            }
        }
//goNextToken(false);
    }

    public Token.T typeNextToken() {
        return nextToken.type;
    }
}
