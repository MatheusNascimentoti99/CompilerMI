/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.LinkedList;

/**
 *
 * @author Matheus Nascimento
 */
public class Parser {

    private final LinkedList<Token> tokens;
    private Token correntToken;
    private Token nextToken;
    private LinkedList<String> result;

    public Parser(LinkedList<Token> tokens) {
        this.tokens = tokens;
        correntToken = tokens.pollFirst();
        nextToken = tokens.pollFirst();
        result = new LinkedList<>();
    }

    public LinkedList<String> getResult() {
        return result;
    }

    public Token seeNextToken() {
        return nextToken;
    }

    public Token getCorrentToken() {
        return correntToken;
    }

    public Token goNextToken() {
        if (!correntToken.isError()) {
            result.push(correntToken.toString());
        }
        correntToken = nextToken;
        nextToken = tokens.pollFirst();
        return correntToken;
    }


    public void includeError(String expected) {
        result.push("\n" + correntToken.line + "  Token recebido: '" + this.correntToken.val.toString() + "' . Tokens esperados: '" + expected + "'");
        goNextToken();
    }

    public Token.T typeNextToken() {
        return nextToken.type;
    }
}
