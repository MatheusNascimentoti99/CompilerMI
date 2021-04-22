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

    private LinkedList<Token> tokens;
    private Token correntToken;
    private Token nextToken;

    public Parser(LinkedList<Token> tokens) {
        this.tokens = tokens;
        correntToken = tokens.pollFirst();
        nextToken = tokens.pollFirst();
    }

    public Token seeNextToken() {
        return nextToken;
    }

    public Token getCorrentToken() {
        return correntToken;
    }

    public Token goNextToken() {
        correntToken = nextToken;
        nextToken = tokens.pollFirst();
        return correntToken;
    }

    public Token.T typeNextToken() {
        return nextToken.type;
    }
}
