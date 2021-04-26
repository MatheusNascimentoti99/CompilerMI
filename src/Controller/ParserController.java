/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Parser;
import Model.Token;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 *
 * @author Matheus Nascimento
 */
public class ParserController {

    private final List types;
    private final Parser parse;
    List firstFunc_stm = Arrays.asList("if", "while", "{", "return", ";", "local", "global", "print", "read");
    List first_var_stm = Arrays.asList("local", "global", "print", "read");

    public ParserController(LinkedList<Token> tokens) {
        types = Arrays.asList("int", "real", "boolean", "string", "struct");
        parse = new Parser(tokens);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        parse.getResult().forEach((info) -> {
            result.append(info).append("\n");
        });
        result.append("\n\n-------------------------\n\n");

        return result.toString();
    }

    public void startSymbol() {
        try {
            program();
        } catch (NullPointerException e) {
            System.out.println("Faltando dados");
        }
    }

    private void program() {
        if (parse.getCorrentToken().val.equals("struct")) {
            parse.goNextToken();
            structs();
        }
        if (parse.getCorrentToken().val.equals("const")) {
            const_block();
        }
        if (parse.getCorrentToken().val.equals("var")) {
            var_block();
        }
        if (parse.getCorrentToken().val.equals("procedure")) {
            start_block();
        } else {
            parse.includeError("struct, const, var, procedure, function");
        }
    }

    private void start_block() {
        if (parse.getCorrentToken().val.equals("procedure")) {
            parse.goNextToken();
            if (parse.getCorrentToken().val.equals("start")) {
                parse.goNextToken();
                if (parse.getCorrentToken().val.equals("(")) {
                    parse.goNextToken();
                    if (parse.getCorrentToken().val.equals(")")) {
                        parse.goNextToken();
                        func_block();
                    } else {
                        parse.includeError(")");
                    }
                } else {
                    parse.includeError("(");
                }
            } else {
                parse.includeError("start");
            }
        }
    }

    private void structs() {
        struct_block();
        if (parse.getCorrentToken().val.equals("struct")) {
            structs();
        }
    }

    private void struct_block() {
        if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.goNextToken();
            extends_();
            if (parse.getCorrentToken().val.equals("{")) {
                parse.goNextToken();
                const_block();
                var_block();
                if (!parse.getCorrentToken().val.equals("}")) {
                    parse.includeError("}");
                } else {
                    parse.goNextToken();
                }
            } else {
                parse.includeError("{");
            }
        }
    }

    private void extends_() {
        if (parse.getCorrentToken().val.equals("extends")) {
            parse.goNextToken();
            if (!parse.getCorrentToken().val.equals("struct")) {
                parse.includeError("struct");

            } else {
                parse.goNextToken();
                if (parse.getCorrentToken().type == Token.T.IDE) {
                    parse.goNextToken();
                } else {
                    parse.includeError("IDE");
                }
            }
        }
    }

    private void const_block() {
        if (parse.getCorrentToken().val.equals("const")) {
            parse.goNextToken();
            if (parse.getCorrentToken().val.equals("{")) {
                parse.goNextToken();
                const_decls();
                if (!parse.getCorrentToken().val.equals("}")) {
                    parse.includeError("}");
                } else {
                    parse.goNextToken();
                }
            } else {
                parse.includeError("{");
            }
        }

    }

    private void var_block() {
        if (parse.getCorrentToken().val.equals("var")) {
            parse.goNextToken();
            if (parse.getCorrentToken().val.equals("{")) {
                parse.goNextToken();
                var_decls();
                if (!parse.getCorrentToken().val.equals("}")) {
                    parse.includeError("}");

                } else {
                    parse.goNextToken();
                }
            } else {
                parse.includeError("{");
            }
        }

    }

    private void const_decls() {
        if (parse.getCorrentToken().type == Token.T.IDE
                || types.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().val.equals("typedef")
                || parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            const_decl();
            const_decls();
        }
    }

    private void var_decls() {
        if (parse.getCorrentToken().type == Token.T.IDE || types.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().val.equals("typedef")
                || parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            var_decl();
            var_decls();
        }
    }

    private void const_decl() {
        if (types.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().type == Token.T.IDE) {
            type();
            const_();
            const_list();
        } else if (parse.getCorrentToken().val.equals("typedef")) {
            typedef();
        }
    }

    private void var_decl() {
        if (types.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().type == Token.T.IDE) {
            type();
            var_();
            var_list();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.goNextToken();
            } else {
                parse.includeError(";");

            }
        } else if (parse.getCorrentToken().val.equals("typedef")) {
            typedef();
        }
    }

    private void type() {
        if (types.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().type == Token.T.IDE) {
            if (parse.getCorrentToken().val.equals("struct")) {
                parse.goNextToken();
                if (parse.getCorrentToken().type != Token.T.IDE) {
                    parse.includeError("IDE");
                } else {
                    parse.goNextToken();
                }
            } else {
                parse.goNextToken();
            }
        }
    }

    private void typedef() {
        if (parse.getCorrentToken().val.equals("typedef")) {
            parse.goNextToken();
            type();
            if (parse.getCorrentToken().type == Token.T.IDE) {
                parse.goNextToken();
                if (parse.getCorrentToken().val.equals(";")) {
                    parse.goNextToken();
                } else {
                    parse.includeError(";");
                }
            } else {
                parse.includeError("IDE");
            }
        }
    }

    private void var_() {
        if (parse.getCorrentToken().type != Token.T.IDE) {
            parse.includeError("IDE");
        } else {
            parse.goNextToken();
            arrays();
        }
    }

    private void var_list() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.goNextToken();
            var_();
            var_list();
        }
    }

    private void const_() {
        if (parse.getCorrentToken().type != Token.T.IDE) {
            parse.includeError("IDE");
        } else {
            parse.goNextToken();
            arrays();
        }
    }

    private void const_list() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.goNextToken();
            const_();
            const_list();
        } else if (parse.getCorrentToken().val.equals("=")) {
            parse.goNextToken();
            decl_atribute();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.goNextToken();
            } else {
                parse.includeError(";");
            }
        } else {
            parse.includeError(", , =");
        }
    }

    private void func_block() {
        if (parse.getCorrentToken().val.equals("{")) {
            parse.goNextToken();
            var_block();
            func_stms();
            if (parse.getCorrentToken().val.equals("}")) {
                parse.goNextToken();
            } else {
                parse.includeError("}");
            }

        } else {
            parse.includeError("{");
        }
    }

    private void func_stms() {
        if (firstFunc_stm.contains(parse.getCorrentToken().val) || parse.getCorrentToken().type == Token.T.IDE) {
            func_stm();
            func_stms();
        }
    }

    private void func_stm() {
        if (parse.getCorrentToken().val.equals("if")) {
            parse.goNextToken();
            if (parse.getCorrentToken().val.equals("(")) {
                parse.goNextToken();
                //log_expr();
                if (parse.getCorrentToken().val.equals(")")) {
                    parse.goNextToken();
                    if (parse.getCorrentToken().val.equals("then")) {
                        parse.goNextToken();
                        func_if_stm();
                    } else {
                        parse.includeError("then");
                    }
                } else {
                    parse.includeError(")");
                }
            } else {
                parse.includeError("(");
            }
        } else if (parse.getCorrentToken().val.equals("while")) {
            parse.goNextToken();
            if (parse.getCorrentToken().val.equals("(")) {
                parse.goNextToken();
                //log_expr();
                if (parse.getCorrentToken().val.equals(")")) {
                    parse.goNextToken();
                    func_stm();
                }
            }
        } else if (parse.getCorrentToken().val.equals("{")
                || parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")
                || parse.getCorrentToken().val.equals("print")
                || parse.getCorrentToken().val.equals("read")
                || parse.getCorrentToken().val.equals("return")
                || parse.getCorrentToken().val.equals(";")
                || parse.getCorrentToken().type == Token.T.IDE) {
            func_normal_stm();
        } else {
            parse.includeError("if, while, '{', IDE, local, global, print, read, ';', return");
        }

    }

    private void func_normal_stm() {

        if (parse.getCorrentToken().val.equals("{")) {
            parse.goNextToken();
            func_stms();
            if (parse.getCorrentToken().val.equals("}")) {
                parse.goNextToken();
            } else {
                parse.includeError("}");
            }
        } else if (first_var_stm.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().type == Token.T.IDE) {
            //var_stm();
        } else if (parse.getCorrentToken().val.equals(";")) {
            parse.goNextToken();
        } else if (parse.getCorrentToken().val.equals("return")) {
            parse.goNextToken();
            expr();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.goNextToken();
            } else {
                parse.includeError(";");
            }
        }
    }

    private void func_if_stm() {
        if (parse.getCorrentToken().val.equals("if")
                || parse.getCorrentToken().val.equals("while")
                || parse.getCorrentToken().val.equals("{")
                || parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")
                || parse.getCorrentToken().val.equals("print")
                || parse.getCorrentToken().val.equals("read")
                || parse.getCorrentToken().val.equals(";")
                || parse.getCorrentToken().val.equals("return")
                || parse.getCorrentToken().type == Token.T.IDE) {
            parse.goNextToken();
            func_stms();
            else_if();
        }
    }

    private void else_if() {
        if (parse.getCorrentToken().val.equals("else")) {
            parse.goNextToken();
        }
    }

    private void decl_atribute() {
        if (parse.getCorrentToken().val.equals("{")) {
            array_decl();
        } else if (parse.getCorrentToken().type == Token.T.LOG
                || parse.getCorrentToken().type == Token.T.IDE
                || parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().type == Token.T.ART
                || parse.getCorrentToken().type == Token.T.CAD
                || parse.getCorrentToken().val.equals("!")
                || parse.getCorrentToken().val.equals("(")) {
            expr();
        } else {
            parse.includeError("{, (, NRO, IDE, CAD");
        }

    }

    private void arrays() {
        if (parse.getCorrentToken().val.equals("[")) {
            array();
            arrays();
        }
    }

    private void array() {
        if (parse.getCorrentToken().val.equals("[")) {
            parse.goNextToken();
            index();
            if (parse.getCorrentToken().val.equals("]")) {
                parse.goNextToken();
            }
        }
    }
//incompleta

    private void index() {
        if (parse.getCorrentToken().type == Token.T.LOG
                || parse.getCorrentToken().type == Token.T.IDE
                || parse.getCorrentToken().type == Token.T.ART
                || parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().type == Token.T.CAD
                || parse.getCorrentToken().val.equals("!")
                || parse.getCorrentToken().val.equals("(")) {
            expr();
        }
    }

    private void array_decl() {
        if (parse.getCorrentToken().val.equals("{")) {
            parse.goNextToken();
            array_def();
            if (parse.getCorrentToken().val.equals("}")) {
                parse.goNextToken();
                array_vector();
            } else {
                parse.includeError("}, ,");
            }
        }
    }

    private void array_vector() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.goNextToken();
            array_decl();
        }
    }

    private void array_def() {
        expr();
        array_expr();
    }

    private void array_expr() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.goNextToken();
            array_def();
        }
    }

    private void expr() {
        or();
    }

    private void or() {
        and();
        or_();
    }

    private void or_() {
        if (parse.getCorrentToken().val.equals("||")) {
            parse.goNextToken();
            and();
            or_();
        }
    }

    private void and() {
        equate();
        and_();
    }

    private void and_() {
        if (parse.getCorrentToken().val.equals("&&")) {
            parse.goNextToken();
            equate();
            and_();
        }
    }

    private void equate() {
        compare();
        equate_();
    }

    private void equate_() {
        if (parse.getCorrentToken().val.equals("==") || parse.getCorrentToken().val.equals("!=")) {
            parse.goNextToken();
            compare();
            equate_();
        }
    }

    private void compare() {
        add();
        compare_();
    }

    private void compare_() {
        if (parse.getCorrentToken().type == Token.T.REL) {
            parse.goNextToken();
            add();
            compare_();
        }
    }

    private void add() {
        mult();
        add_();
    }

    private void add_() {
        if (parse.getCorrentToken().val.equals("+")
                || parse.getCorrentToken().val.equals("-")) {
            parse.goNextToken();
            mult();
            add_();
        }
    }

    private void mult() {
        unary();
        mult_();
    }

    private void mult_() {
        if (parse.getCorrentToken().val.equals("*") || parse.getCorrentToken().val.equals("/")) {
            parse.goNextToken();
            unary();
            mult_();
        }
    }

    private void unary() {
        if (parse.getCorrentToken().val.equals("!")) {
            parse.goNextToken();
            unary();
        } else {
            value();
        }
    }

    private void value() {
        if (parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().type == Token.T.CAD
                || parse.getCorrentToken().type == Token.T.LOG) {
            parse.goNextToken();
        } else if (parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            parse.goNextToken();
            access();
        } else if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.goNextToken();
            id_value();
        } else if (parse.getCorrentToken().val.equals("(")) {
            parse.goNextToken();
            expr();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.goNextToken();
            } else {
                parse.includeError(")");
            }
        } else {
            parse.includeError("LOG, CAD, NRO, 'local, 'global', IDE, (");
        }
    }

    private void id_value() {
        if (parse.getCorrentToken().val.equals("[")
                || parse.getCorrentToken().val.equals(".")) {
            arrays();
            accesses();
        } else if (parse.getCorrentToken().val.equals("(")) {
            parse.goNextToken();
            args();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.goNextToken();
            } else {
                parse.includeError(")");
            }
        }
    }

    private void args() {
        if (parse.getCorrentToken().type == Token.T.LOG
                || parse.getCorrentToken().type == Token.T.IDE
                || parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().type == Token.T.CAD
                || parse.getCorrentToken().type == Token.T.ART
                || parse.getCorrentToken().val.equals("!")
                || parse.getCorrentToken().val.equals("(")) {
            expr();
            args_list();
        }
    }

    private void args_list() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.goNextToken();
            expr();
            args_list();
        }
    }

    private void access() {
        if (parse.getCorrentToken().val.equals(".")) {
            parse.goNextToken();
            if (parse.getCorrentToken().type == Token.T.IDE) {
                parse.goNextToken();
                arrays();
            }
        } else {
            parse.includeError(".");
        }
    }

    private void accesses() {
        if (parse.getCorrentToken().val.equals(".")) {
            access();
            accesses();
        }
    }
}
