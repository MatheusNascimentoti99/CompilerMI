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
    private Token currentToken;
    private Token nextToken;
    private int erros;
    private int errosSem;
    private final LinkedList<TokenAfterParse> result;

    public Parser(LinkedList<Token> tokens) {
        erros = 0;
        errosSem = 0;
        this.tokens = tokens;
        currentToken = tokens.pollFirst();
        nextToken = tokens.pollFirst();
        result = new LinkedList<>();
    }

    public int erros() {
        return erros;
    }
    
    public int errosSem() {
        return errosSem;
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

    public Token getCurrentToken() {
        try {
            if (currentToken == null) {
                throw new NullPointerException();
            } else {
                return currentToken;
            }
        } catch (NullPointerException e) {
            int[] position = {-1, -1};
            return new Token(Token.T.EOF, "", position);
        }
    }

    public boolean equalsValue(String expected) {
        return currentToken.val.equals(expected);
    }

    public Token nextToken() {
        do {
            result.addLast(new TokenAfterParse(currentToken));
            currentToken = nextToken;
            nextToken = tokens.pollFirst();
        } while (currentToken != null && currentToken.isError());
        return currentToken;
    }

    public Token nextToken(boolean include) {
        do {
            if (include) {
                result.addLast(new TokenAfterParse(currentToken));
            }
            currentToken = nextToken;
            nextToken = tokens.pollFirst();
        } while (currentToken != null && currentToken.isError());

        return currentToken;
    }

    public void includeError(String expected) {
        erros++;
        String[] expecteds = expected.split(",");
        try {
            result.addLast(new TokenAfterParse(currentToken.val, currentToken.line, Arrays.toString(expecteds)));

        } catch (NullPointerException e) {
            Token errorParseEOF = new Token(Token.T.EOF, "EOF", -1);
            result.addLast(new TokenAfterParse(errorParseEOF.val, -1, Arrays.toString(expecteds)));
        }
    }

    public void includeError(String expected, Token.T type) {
        if (type != Token.T.SEMANTIC) {
            erros++;
        } else {
            errosSem++;
        }
        result.addLast(new TokenAfterParse(currentToken, expected, type));
    }

    public void includeError(String expected, Token token, Token.T type) {
        if (type != Token.T.SEMANTIC) {
            erros++;
        } else {
            errosSem++;
        }
        result.addLast(new TokenAfterParse(token, expected, type));
    }

    public void includeError(String expected, List follows) {
        String[] expecteds = expected.split(",");
        try {
            erros++;
            result.addLast(new TokenAfterParse(currentToken.val, currentToken.line, Arrays.toString(expecteds)));
            while (tokens.size() > 0) {
                try {
                    if (follows.contains(currentToken.val) || (follows.contains("IDE") && currentToken.type == Token.T.IDE)) {
                        break;
                    } else {
                        result.addLast(new TokenAfterParse(currentToken.val, currentToken.line, Arrays.toString(expecteds), true));

                    }
                    nextToken(false);
                } catch (NullPointerException e) {
                    Token errorParseEOF = new Token(Token.T.EOF, "EOF", -1);
                    result.addLast(new TokenAfterParse(errorParseEOF.val, -1, Arrays.toString(expecteds)));
                }
            }
        } catch (NullPointerException e) {
            Token errorParseEOF = new Token(Token.T.EOF, "EOF", -1);
            result.addLast(new TokenAfterParse(errorParseEOF.val, -1, Arrays.toString(expecteds)));
        }
//goNextToken(false);
    }

    public Token.T typeNextToken() {
        return nextToken.type;
    }
}
