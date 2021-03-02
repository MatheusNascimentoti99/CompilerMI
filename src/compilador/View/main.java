/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador.View;

import Utils.Lex;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 * @author Matheus Nascimento
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                Reader input = args.length > 0
                        ? new FileReader(args[0])
                        : new InputStreamReader(System.in);
                Lex lex = new Lex(input);
                System.out.println("fim " +lex.lda().toString());
                input.close();
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

}
