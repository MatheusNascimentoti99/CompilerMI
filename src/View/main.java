/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package View;

import Controller.ParserController;
import Controller.LexController;
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
            //Verificação da localização do diretório de execução
            boolean endsWith = new File(".").getCanonicalPath().endsWith("dist");
            String pathRelative = endsWith ? "..\\" : "";
            File fileOutput = new File(pathRelative + "output");
            if (fileOutput.exists()) {
                File[] listFiles = fileOutput.listFiles();
                for (File file : listFiles) {
                    file.delete();
                }
            }

            Path outputPath = Paths.get(pathRelative + "output");
            Files.createDirectories(outputPath);
            fileInput = new File(pathRelative + "input");

            System.out.println("Entradas em: " + fileInput.getAbsolutePath());

            System.out.println("Resultados em: " + outputPath.toAbsolutePath() + "\n");

            // Retorna a lista de arquivos do diretório de inputs
            paths = fileInput.listFiles();

            if (paths == null) {
                System.out.println("Arquivo de input não encontrado");
                System.out.println("Crie o diretório e insira os arquivos em: " + fileInput.getAbsolutePath());
            } else {
                // for each pathname in pathname array
                for (File path : paths) {

                    // Verifica se o arquivo segue o padrão
                    if (path.getName().matches(patternFile)) {
                        try {
                            //Prepara o primeiro arquivo para leitura
                            Reader input = new FileReader(path);
                            //Cria a analise léxica para o arquivo atual
                            LexController lex = new LexController(input);

                            //Verificar número do arquivo
                            Matcher numberEndFile = patternEndFile.matcher(path.getName());
                            numberEndFile.find();
                            try (FileWriter arqOutput = new FileWriter(outputPath.toString() + "\\saida" + numberEndFile.group() + ".txt")) {
                                PrintWriter gravarArq = new PrintWriter(arqOutput);
                                System.out.println("Arquivo analisado: " + path.getName());
                                ParserController parser = new ParserController(lex.getTokens());
                                parser.startSymbol();
                                StringBuilder result = new StringBuilder();
                                result.append(parser.toString());
                                result.append(parser.getParse().errosSem()>0? "\nArquivo contém " + parser.getParse().errosSem() + " erros semânticos": "\nSem erros semânticos");
                                result.append(parser.getParse().erros()>0? "\nArquivo contém " + parser.getParse().erros()+ " erros sintaticos": "\nSem erros sintaticos");
                                result.append(lex.hasErros() ? "\nArquivo contém " + lex.erros + " erros léxicos" : "\nArquivo sem erros léxicos");
                                gravarArq.print(result.toString());
                                System.out.println("Output criado: saida" + numberEndFile.group() + ".txt");
                                System.out.println(parser.getParse().hasErros() ? "Arquivo contém " + parser.getParse().erros() + " erros sintáticos" : "Sucesso! Arquivo não contém erros sintáticos");
                                System.out.println(parser.getParse().errosSem()>0? "Arquivo contém " + parser.getParse().errosSem()+ " erros semanticos\n" : "Sucesso! Arquivo não contém erros semanticos\n");

                            } catch (IOException e) {
                                System.err.println("Não foi possível criar o arquivo " + outputPath.toString() + "\\saida" + numberEndFile.group() + ".txt");
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
