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
import java.util.LinkedList;

/**
 *
 * @author Matheus Nascimento
 */
public final class Lex {

    int errors;
    Reader input;
    LinkedList<Token> tokens;
    int charactere = 0;
    int[] position = {1, 0}; //In the future can have col
    String buffer = new String();
    String[] PLE = {"var", "const", "typedef", "struct", "extends", "procedure", "function", "start", "return", "if", "else", "then", "while", "read", "print", "int", "real", "boolean", "string", "true", "false", "global", "local"};
    HashMap<Integer, String> listPLE;

    public Lex(Reader input) throws IOException {
        errors = 0;
        this.input = input;
        listPLE = new HashMap<>();
        for (String PLE1 : PLE) {
            listPLE.put(PLE1.hashCode(), PLE1);
        }
        createListTokens();
    }

    //Função para ler próximo caractere e contar linha e coluna
    private int read() throws IOException {
        if (charactere == 10) {
            position[0] = position[0] + 1;
            position[1] = 0;
        }
        charactere = input.read();
        return charactere;
    }

    //Q0
    private void createListTokens() throws IOException {
        tokens = new <Token>LinkedList();
        buffer = new String();
        charactere = read();

        //Estado inicial do Autômato finito
        while (charactere >= 0) {
            switch (charactere) {
                /*
                    ***********Caracteres ignorados********
                 */
                case 8:
                case 9:
                case 10:
                case 11:
                case 13:
                case 32:
                    charactere = read();
                    break;

                case 33: //Entrada !
                    Q18();
                    break;

                case 34: //Entrada "
                    Q30();
                    break;

                case 38: //Entrada &
                    Q21();
                    break;

                case 42: //Entrada *
                    Q6();
                    break;

                case 43: //Entrada +
                    Q7();
                    break;

                case 45: //Entrada -
                    Q9();
                    break;

                case 47: //Entrada de /
                    Q11();
                    break;

                /*
                    ***********Entrada para digitos********
                 */
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
                case 62: //Entrada > ou <
                    Q28_Q26();
                    break;

                case 61: //Entrada =
                    Q23();
                    break;

                case 124: //Entrada |
                    Q19();
                    break;

                /*
                    ***********Delimitadores********
                 */
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
                        Q3(); // Entrada de Letras
                    } else {
                        // Valores não especificados 
                        errors++;
                        tokens.add(new Token(Token.T.SIB, (char) charactere, position));
                        charactere = read();
                    }
                }
            }
        }
    }

    //Estado final para Delimitadores
    private void Q1() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        tokens.add(new Token(Token.T.DEL, buffer, position));
        buffer = new String();

    }

    //Estado incial para Identificadores
    private void Q3() throws IOException {
        //Enquanto for um valor aceito como delimitador continua a formação
        while (isAlpha(charactere) || isNumber(charactere) || charactere == 95) {
            buffer = buffer + (char) charactere;
            read();
        }
        //Verifica se o identificador formado é uma palavra reservada
        if (listPLE.containsKey(buffer.toLowerCase().hashCode())) {
            tokens.add(new Token(Token.T.PRE, buffer.toLowerCase(), position));

        } else {
            tokens.add(new Token(Token.T.IDE, buffer, position));

        }
        buffer = new String();

    }

    //Estado incial para Digitos
    private void Q4() throws IOException {
        while (isNumber(charactere)) {
            buffer = buffer + (char) charactere;
            charactere = read();
        }
        //Se depois do número não for um ponto, então finaliza a formação
        if (charactere != 46) {
            tokens.add(new Token(Token.T.NRO, buffer, position));
            buffer = new String();
        } else {
            Q5(); //Verifica o resto da formação se ouver ponto depois do número
            buffer = new String();
        }
    }

    private void Q5() throws IOException {
        //inclue o ponto na formação e verificar se há números depois
        buffer = buffer + ((char) charactere);
        charactere = read();
        if (isNumber(charactere)) {
            Q17();
            buffer = new String();
        } else {
            errors++;
            tokens.add(new Token(Token.T.NMF, buffer, position));
            buffer = new String();
        }
        buffer = new String();
    }

    //Forma o operador *
    private void Q6() throws IOException {
        buffer = buffer + ((char) charactere);
        read();
        tokens.add(new Token(Token.T.ART, buffer, position));
        buffer = new String();

    }

    //Verifica se a entrada forma apenas um operador + ou um ++
    private void Q7() throws IOException {
        buffer = buffer + (char) charactere;
        charactere = read();
        if (charactere == 43) {
            Q8_Q10(); //Se ouver outro +, então passa para o próximo estado aceitavel
        } else {
            tokens.add(new Token(Token.T.ART, buffer, position));
            buffer = new String();

        }
    }

    //Estado final para ++
    private void Q8_Q10() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.ART, buffer, position));
        buffer = new String();
        read();
    }

    //Estado inicial para -
    private void Q9() throws IOException {
        buffer = buffer + (char) charactere;
        charactere = read();
        if (charactere == 45) {
            Q8_Q10();
        } else {
            tokens.add(new Token(Token.T.ART, buffer, position));
            buffer = new String();
        }
    }


    //Estado inicial para /     Verifica se é um operador aritmético ou o inicio de um comentário de linha ou bloco
    private void Q11() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        switch (charactere) {
            case 42:    //Se tiver *, então incia a formação do comentário de bloco
                Q14();
                break;
            case 47: // Se for outro /, então incia a formação do comentário de linha 
                Q12();
                break;
            default: //Se não, então é apenas um operador aritmético
                tokens.add(new Token(Token.T.ART, buffer, position));
                break;
        }
        buffer = new String();

    }

    //Forma o comentário de linha
    private void Q12() throws IOException {
        while (charactere != 10 && charactere > 0) {
            buffer = buffer + (char) charactere;
            read();
        }
    }

    //Formação de comentário de bloco
    private void Q14() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        //Q15 charactere == 42
        while (charactere != 42 && charactere > 0) { //Enquanto não encontrar um * ou chegar no final do arquivo
            if (charactere != 10) {
                buffer = buffer + (char) charactere;
            }
            read();
        }
        //Q16 charactere == 47
        if ((charactere < 0)) {                 //Se chegar no final do arquivo, então o comentário nunca foi fechado
            errors++;
            tokens.add(new Token(Token.T.CoMF, buffer, position));
            buffer = new String();
        } else {            //Implica que teve um * como entrada, então passa para o próximo estado
            buffer = buffer + (char) charactere;
            Q15();
        }

    }

    //Verifica se há um / finalizando o comentário de bloco
    private void Q15() throws IOException {
        read();
        if (charactere != 47) { //Se não houver um / em seguida, então retorna para o estado anterior
            Q14();
        } else { //Conclui a formação de um comentário de bloco
            read();
            buffer = new String();
        }
    }

    //Verifica os número que há depois do ponto 
    private void Q17() throws IOException {
        boolean hasNumber = false;
        while (charactere >= 0 && isNumber(charactere)) { //Enquanto houver número ou não finalizar o arquivo
            buffer = buffer + ((char) charactere);
            hasNumber = true;
            charactere = read();
        }
        if (hasNumber) { //Se  já houve um número após o ponto, então finaliza em um estado de aceitação
            tokens.add(new Token(Token.T.NRO, buffer, position));
        } else {
            errors++;
            tokens.add(new Token(Token.T.NMF, buffer, position));
        }

    }

    //Verifica se há a formação de um ! ou !=
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

    //Verifica se a formação de ||
    private void Q19() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 124) {
            Q20_Q22();
        } else { //Se não houver outro | em seguida, então finaliza em um estado não final
            errors++;
            tokens.add(new Token(Token.T.OpMF, buffer, position));
        }
        buffer = new String();

    }

    //Estado final para && e ||
    private void Q20_Q22() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.LOG, buffer, position));
        read();

    }

    // Verifica a formação de &
    private void Q21() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 38) {
            Q20_Q22();
        } else {
            errors++;
            tokens.add(new Token(Token.T.OpMF, buffer, position));
        }
        buffer = new String();

    }

    //Verifica a formação do = ou ==
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

    //Estado para entrada de = após outro operador relacional 
    private void Q29_Q27_Q25_Q24() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.REL, buffer, position));
        read();
    }

    //Verifica a formação do > ou < ou <= ou >=
    private void Q28_Q26() throws IOException {
        buffer = buffer + (char) charactere;

        read();
        if (charactere == 61) {
            Q29_Q27_Q25_Q24();
        } else {
            tokens.add(new Token(Token.T.REL, buffer, position));
            buffer = new String();

        }
    }

    //Estado inicial da cadeia de caracteres
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

    //Verifica se há um \
    private void Q31() throws IOException {
        buffer = buffer + (char) charactere;
        read();
        if (charactere == 34) { //Se for um " após \
            Q33();
        } else if (isNumber(charactere) || isAlpha(charactere) || isSimbol(charactere)) { //Se for outro valor
            Q32();
        }

    }

    private void Q32() throws IOException {
        //Loop para formação de (numero|letra|simbolo)* e muda de estado para o simbolo especial \
        while (isNumber(charactere) || isAlpha(charactere) || isSimbol(charactere) && charactere != 92) {
            buffer = buffer + (char) charactere;
            read();
        }
        switch (charactere) {
            case 92: // Se for um \
                Q31();
                break;
            case 34: //Se for um " finalizando a cadeia
                Q34();
                break;
            default: //Implica em uma cadeia má formada
                CMF();
                break;
        }

    }

    //Estado para a possibilidade de haver \"
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

    //Estado final da cadeia de caracteres
    private void Q34() throws IOException {
        buffer = buffer + (char) charactere;
        tokens.add(new Token(Token.T.CAD, buffer, position));
        buffer = new String();
        read();
    }

    // Loop para permanecer lendo cadeia má formada
    private void CMF() throws IOException {
        while (charactere != 34 && charactere != 10 && charactere > 0) {
            if (charactere == 92) {
                buffer = buffer + (char) charactere;
            } else { 
                if (charactere != 10) {
                    buffer = buffer + (char) charactere;
                }
            }
            read();

        }
        errors++;
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

    public boolean hasErros() {
        return errors > 0;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        tokens.forEach((token) -> {
            result.append(token.toString()).append("\n");
        });
        result.append("\n\n-------------------------\n\n");
        result.append(errors > 0 ? "Arquivo contém " + errors + " erros léxicos" : "Arquivo analisado com sucesso!");

        return result.toString();
    }
    
    public LinkedList<Token> getTokens(){
        return tokens;
    }

}
