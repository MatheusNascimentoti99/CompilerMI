/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Model.Token;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 *
 * @author Matheus Nascimento
 */
public class Lex {

    Reader input;
    LinkedList<Token> tokens;
    int charactere = 0;
    int[] position = {1, 0}; //In the future can have col
    String buffer = new String();
    String[] PLE = {"var", "const", "typedef", "struct", "extends", "procedure", "function", "start", "return", "if", "else", "then", "while", "read", "print", "int", "real", "boolean", "string", "true", "false", "global", "local"};
    HashMap<Integer, String> listPLE;

    public Lex(Reader input) throws IOException {
        this.input = input;
        listPLE = new HashMap<Integer, String>();
        for (String PLE1 : PLE) {
            listPLE.put(PLE1.hashCode(), PLE1);
        }

    }

    private int read() throws IOException {
        if (charactere == 10) {
            position[0] = position[0] + 1;
            position[1] = 0;
        }
        charactere = input.read();
        return charactere;
    }

    //Q0
    public void lda() throws IOException {
        tokens = new <Token>LinkedList();
        buffer = new String();
        charactere = read();
        while (charactere >= 0) {
            switch (charactere) {
                //Caracteres ignorados
                case 8:
                case 9:
                case 10:
                case 11:
                case 13:
                case 32:
                    charactere = read();
                    break;
                case 33:
                    Q18();
                    break;
                case 34:
                    Q30();
                    break;
                case 38:
                    Q21();
                    break;
                case 42:
                    Q6();
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

                //Números
                case 48:
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
                case 60:
                    Q28_Q26();
                    break;
                case 61:
                    Q23();
                    break;
                case 62:
                    Q28_Q26();
                    break;
                case 124:
                    Q19();
                    break;

                //Delimitadores
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
                default: {
                    if (isAlpha(charactere)) {
                        Q3();
                    } else {
                        tokens.add(new Token(Token.T.SIB, (char) charactere, position));
                        charactere = read();
                    }
                }
            }
        }
    }

    //Estádo final para Delimitadores
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
        if (listPLE.containsKey(buffer.toLowerCase().hashCode())) {
            tokens.add(new Token(Token.T.PRE, buffer.toLowerCase(), position));

        } else {
            tokens.add(new Token(Token.T.IDE, buffer, position));

        }
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
                tokens.add(new Token(Token.T.ART, buffer, position));
                break;
        }
        buffer = new String();

    }

    private void Q12() throws IOException {
        while (charactere != 10 && charactere > 0) {
            buffer = buffer + (char) charactere;
            read();
        }
    }

    private void Q14() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        //Q15 charactere == 42
        while (charactere != 42 && charactere > 0) {
            buffer = buffer + (char) charactere;
            read();
        }
        //Q16 charactere == 47
        if ((charactere < 0)) {
            tokens.add(new Token(Token.T.CoMF, buffer, position));
            buffer = new String();
        } else {
            buffer = buffer + (char) charactere;
            Q15();
        }

    }

    private void Q15() throws IOException {
        read();
        if (charactere != 47) {
            Q14();
        } else {
            read();
            buffer = new String();
        }
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

    private void Q18() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 61) {
            Q29_Q27_Q25_Q24();
        } else {
            tokens.add(new Token(Token.T.LOG, buffer, position));
        }
        buffer = new String();

    }

    private void Q19() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 124) {
            Q20_Q22();
        } else {
            tokens.add(new Token(Token.T.OpMF, buffer, position));
        }
        buffer = new String();

    }

    private void Q20_Q22() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.LOG, buffer, position));
        read();

    }

    private void Q21() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 38) {
            Q20_Q22();
        } else {
            tokens.add(new Token(Token.T.OpMF, buffer, position));
        }
        buffer = new String();

    }

    private void Q23() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 61) {
            Q29_Q27_Q25_Q24();
        } else {
            tokens.add(new Token(Token.T.REL, buffer, position));
        }
        buffer = new String();

    }

    //Q24
    private void Q29_Q27_Q25_Q24() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.REL, buffer, position));
        read();
    }

    private void Q28_Q26() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 61) {
            Q29_Q27_Q25_Q24();
        } else {
            tokens.add(new Token(Token.T.REL, buffer, position));
        }
        buffer = new String();
        read();
    }

    private void Q30() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        if (charactere == 92) {
            Q31();
        } else if (isNumber(charactere) || isAlpha(charactere) || isSimbol(charactere)) {
            Q32();
        } else if (charactere == 34) {
            Q34();
        } else {
            CMF();
        }

    }

    private void Q31() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        if (charactere == 34) {
            Q33();
        } else if (isNumber(charactere) || isAlpha(charactere) || isSimbol(charactere)) {
            Q32();
        }

    }

    private void Q32() throws IOException {
        while (isNumber(charactere) || isAlpha(charactere) || isSimbol(charactere) && charactere != 92) {
            buffer = buffer + (char) charactere;
            read();
        }
        switch (charactere) {
            case 92:
                Q31();
                break;
            case 34:
                Q34();
                break;
            default:
                CMF();
                break;
        }

    }

    private void Q33() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        if (isNumber(charactere) || isAlpha(charactere) || isSimbol(charactere)) {
            Q32();
        } else if (charactere == 34) {
            Q34();
        } else {
            CMF();
        }
    }

    private void Q34() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.CAD, buffer, position));
        buffer = new String();
        read();
    }

    private void CMF() throws IOException {
        while (charactere != 34 && charactere != 10 && charactere > 0) {
            if (charactere == 92) {
                read();
                buffer = buffer + (char) charactere;
            }
            read();
            if (charactere != 10) {
                buffer = buffer + (char) charactere;
            }

        }
        tokens.add(new Token(Token.T.CMF, buffer, position));
        buffer = new String();
        read();
    }

    private boolean isNumber(int charactere) {
        return (charactere >= 48 && charactere <= 57);
    }

    private boolean isAlpha(int charactere) {
        return (charactere >= 65 && charactere <= 90) || (charactere >= 97 && charactere <= 122);
    }

    private boolean isSimbol(int charactere) {
        return (charactere >= 32 && charactere <= 126 && charactere != 34);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        tokens.forEach((token) -> {
            result.append(token.toString()).append("\n");
        });
        return result.toString();
    }

}
