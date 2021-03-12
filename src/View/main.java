/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Utils.Lex;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Matheus Nascimento
 */
public class main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File fileInput;
        File[] paths;
        String patternFile = "entrada\\d+\\.txt";
        Pattern patternEndFile = Pattern.compile("\\d+");

        try {
            File fileOutput = new File("../output");
            if (fileOutput.exists()) {
                File[] listFiles = fileOutput.listFiles();
                for (File file : listFiles) {
                    file.delete();
                }
            }
            
            //Verificação da localização do diretório de execução
            String pathAbsolute = new File( "." ).getCanonicalPath();
            boolean endsWith = pathAbsolute.endsWith("dist");
            Path outputPath = Paths.get(endsWith? "../output": "output/");
            Files.createDirectories(outputPath);
            fileInput = new File(endsWith ? "../input": "input");
            
            
            // Retorna a lista de arquivos do diretório de inputs
            paths = fileInput.listFiles();
            
            if (paths == null) {
                System.out.println("Arquivo de input não encontrado");
            } else {
                // for each pathname in pathname array
                for (File path : paths) {

                    // Verifica se o arquivo segue o padrão
                    if (path.getName().matches(patternFile)) {
                        try {
                            //Prepara o primeiro arquivo para leitura
                            Reader input = new FileReader(path);
                            Lex lex = new Lex(input);

                            Matcher numberEndFile = patternEndFile.matcher(path.getName());
                            numberEndFile.find();
                            try (FileWriter arqOutput = new FileWriter(outputPath.toString() + "/saida" + numberEndFile.group() + ".txt")) {
                                PrintWriter gravarArq = new PrintWriter(arqOutput);
                                lex.createListTokens();
                                gravarArq.print(lex.toString());
                                System.out.println("Criado outputFile: saida" + numberEndFile.group() + ".txt");
                                System.out.println(lex.hasErros() ? "Arquivo possue erros\n" : "Arquivo analisado sem erros\n");
                            } catch (IOException e) {
                                System.err.println("Não foi possível criar o arquivo " + outputPath.toString() + "/saida" + numberEndFile.group() + ".txt");
                            }

                        } catch (IOException e) {
                            System.err.println("Erro inesperado ao ler o arquivo " + path.getName());
                        }
                    } else {
                        System.out.println("O arquivo " + path.getName() + " não segue o padrão \"entradaX.txt\"");
                    }
                }
            }
        } catch (SecurityException e) {
            System.out.println("Não foi permitido ler a lista de arquivos");
            // if any error occurs
        } catch (IOException e) {
            System.out.println("Não foi possível criar o diretório de output");

        }

    }

}
