/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador.View;

import Model.Token;
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

            Path outputPath = Paths.get(new File("output/").getAbsolutePath());
            Files.createDirectories(outputPath);
            // create new file
            fileInput = new File("input/").getAbsoluteFile();
            // returns pathnames for files and directory
            paths = fileInput.listFiles();

            // for each pathname in pathname array
            for (File path : paths) {

                // prints file and directory paths
                if (path.getName().matches(patternFile)) {
                    try {

                        Reader input = new FileReader(path);
                        Lex lex = new Lex(input);

                        Matcher numberEndFile = patternEndFile.matcher(path.getName());
                        numberEndFile.find();
                        try (FileWriter arqOutput = new FileWriter(outputPath.toString() + "/saida" + numberEndFile.group() + ".txt")) {
                            PrintWriter gravarArq = new PrintWriter(arqOutput);
                            lex.lda();
                            gravarArq.print(lex.toString());
                            System.out.println("Created outputFile: saida" + numberEndFile.group() + ".txt");
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

        } catch (SecurityException e) {
            System.out.println("Não foi permitido ler a lista de arquivos");
            // if any error occurs
        } catch (IOException e) {
            System.out.println("Não foi possível criar o diretório de output");

        }

    }

}
