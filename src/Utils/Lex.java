/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Model.Token;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;

/**
 *
 * @author Matheus Nascimento
 */
public class Lex {

    Reader input;
    LinkedList<Token> tokens;
    int charactere = 0;
    int[] position = {0, -1};
    String buffer = new String();

    public Lex(Reader input) throws IOException {
        this.input = input;
    }

    private int read() throws IOException {
        charactere = input.read();
        if (charactere == 10) {
            position[0] = position[0] + 1;
            position[1] = 0;
        } else {
            position[1] = position[1] + 1;
        }
        return charactere;
    }

    //Q0
    public LinkedList<Token> lda() throws IOException {
        tokens = new <Token>LinkedList();
        buffer = new String();
        charactere = read();
        while (charactere >= 0) {
            switch (charactere) {
                case 33:
                    Q18();
                    break;
                case 38:
                    Q21();
                    break;
                case 43:
                    Q7();
                    break;
                case 45:
                    Q9();
                    break;
                case 47:
                    Q11();
                    break;
                case 42:
                    Q6();
                    break;
                case 49:
                case 50:
                case 51:
                case 52:
                case 53:
                case 54:
                case 55:
                case 56:
                case 57:
                    Q4();
                    break;
                case 124:
                    Q19();
                    break;
                case 123:
                case 125:
                case 91:
                case 93:
                case 59:
                case 46:
                case 44:
                case 40:
                case 41:
                    Q1();
                    break;
                case -1:
                    break;
                case 10:
                case 8:
                    charactere = read();
                    break;
                default: {
                    if (isAlpha(charactere)) {
                        Q3();
                        break;
                    }
                    charactere = read();
                    System.out.println((char) charactere);
                }
            }
        }
        return tokens;
    }

    private void Q1() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        tokens.add(new Token(Token.T.DEL, buffer, position));
        buffer = new String();

    }

    private void Q3() throws IOException {
        while (isAlpha(charactere) || isNumber(charactere) || charactere == 95) {
            buffer = buffer + (char) charactere;
            read();
        }
        tokens.add(new Token(Token.T.IDE, buffer, position));
        buffer = new String();

    }

    private void Q4() throws IOException {
        while (isNumber(charactere)) {
            buffer = buffer + (char) charactere;
            charactere = read();
        }
        if (charactere != 46) {
            tokens.add(new Token(Token.T.NRO, buffer, position));
            buffer = new String();
        } else {
            Q5();
            buffer = new String();
        }
    }

    private void Q5() throws IOException {
        buffer = buffer + ((char) charactere);
        charactere = read();
        if (isNumber(charactere)) {
            Q17();
            buffer = new String();
        } else {
            tokens.add(new Token(Token.T.NMF, buffer, position));
            buffer = new String();
        }
        buffer = new String();
    }

    private void Q6() throws IOException {
        buffer = buffer + ((char) charactere);
        read();
        tokens.add(new Token(Token.T.ART, (char) charactere, position));
        buffer = new String();

    }

    private void Q7() throws IOException {
        buffer = buffer + (char) charactere;
        charactere = read();
        if (charactere == 43) {
            Q8();
        } else {
            tokens.add(new Token(Token.T.ART, buffer, position));
            buffer = new String();

        }
    }

    private void Q8() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.ART, buffer, position));
        buffer = new String();
        read();
    }

    private void Q9() throws IOException {
        buffer = buffer + (char) charactere;
        charactere = read();
        if (charactere == 45) {
            Q10();
        } else {
            tokens.add(new Token(Token.T.ART, buffer, position));
            buffer = new String();
        }
    }

    private void Q10() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.ART, buffer, position));
        buffer = new String();
        read();
    }

    private void Q11() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        switch (charactere) {
            case 42:
                Q14();
                break;
            case 47:
                Q12();
                break;
            default:
                System.out.println(buffer);
                tokens.add(new Token(Token.T.ART, buffer, position));
                break;
        }
        buffer = new String();

    }

    private void Q12() throws IOException {
        while (charactere != 10 && charactere > 0) {
            read();
        }
    }

    private void Q14() throws IOException {
        read();
        //Q15 charactere == 42
        while (charactere != 42 && charactere > 0) {
            System.out.println("dentro: " + charactere);

            read();
        }
        System.out.println("fora:" + charactere);

        //Q16 charactere == 47
        if ((charactere < 0)) {
            tokens.add(new Token(Token.T.CoMF, position));
        } else {
            Q15();
        }

    }

    private void Q15() throws IOException {
        read();
        if (charactere != 47) {
            Q14();
        } else {
            read();
        }
    }

    private void Q18() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 61) {
            Q25();
        } else {
            tokens.add(new Token(Token.T.LOG, buffer, position));
        }
        buffer = new String();
        read();

    }

    private void Q19() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 124) {
            Q20();
        } else {
            tokens.add(new Token(Token.T.OpMF, buffer, position));
        }
        buffer = new String();
        read();

    }

    private void Q20() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.LOG, buffer, position));
    }

    private void Q21() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 38) {
            Q22();
        } else {
            tokens.add(new Token(Token.T.OpMF, buffer, position));
        }
        buffer = new String();
        read();

    }

    private void Q22() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.LOG, buffer, position));
    }

    private void Q25() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.REL, buffer, position));
    }

    private boolean isNumber(int charactere) {
        return (charactere >= 49 && charactere <= 57);
    }

    private boolean isAlpha(int charactere) {
        return (charactere >= 65 && charactere <= 90) || (charactere >= 97 && charactere <= 122);
    }

    private void Q17() throws IOException {
        boolean hasNumber = false;
        while (charactere >= 0 && isNumber(charactere)) {
            buffer = buffer + ((char) charactere);
            hasNumber = true;
            charactere = read();
        }
        if (hasNumber) {
            tokens.add(new Token(Token.T.NRO, buffer, position));
        } else {
            tokens.add(new Token(Token.T.NMF, buffer, position));
        }

    }
}
