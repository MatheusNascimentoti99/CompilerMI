/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

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
    private final LinkedList<TokenAfterParse> result;

    public Parser(LinkedList<Token> tokens) {
        this.tokens = tokens;
        correntToken = tokens.pollFirst();
        nextToken = tokens.pollFirst();
        result = new LinkedList<>();
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
        result.push(new TokenAfterParse(correntToken));
        correntToken = nextToken;
        nextToken = tokens.pollFirst();
        return correntToken;
    }

    public Token nextToken(boolean include) {
        if (include) {
            result.push(new TokenAfterParse(correntToken));
        }
        correntToken = nextToken;
        nextToken = tokens.pollFirst();

        return correntToken;
    }

    public void includeError(String expected) {
        try {
            result.push(new TokenAfterParse(correntToken.val, correntToken.line, expected));

        } catch (NullPointerException e) {
            result.push(new TokenAfterParse(correntToken.val, -1, expected));
        }
//goNextToken(false);
    }

    public void includeError(String expected, List follows) {
        result.push(new TokenAfterParse(correntToken.val, correntToken.line, expected));

        while (tokens.size() > 0) {
            try {
                if (follows.contains(correntToken.val) || (follows.contains("IDE") && correntToken.type == Token.T.IDE)) {
                    break;
                } else {
                    result.push(new TokenAfterParse(correntToken.val, correntToken.line, expected, true));

                }
                nextToken(false);
            } catch (NullPointerException e) {
                Token errorParseEOF = new Token(Token.T.EOF, "", -1);
                result.push(new TokenAfterParse(errorParseEOF.val, -1, expected));
            }
        }
//goNextToken(false);
    }

    public Token.T typeNextToken() {
        return nextToken.type;
    }
}
