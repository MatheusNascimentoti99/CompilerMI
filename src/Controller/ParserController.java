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

    private final List types = Arrays.asList("int", "real", "boolean", "string", "struct");
    private final Parser parse;
    List firstFunc_stm = Arrays.asList("if", "while", "{", "return", ";", "local", "global", "print", "read");
    List first_var_stm = Arrays.asList("local", "global", "print", "read");
    private List follow;

    public ParserController(LinkedList<Token> tokens) {
        parse = new Parser(tokens);
    }

    public Parser getParse() {
        return parse;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        parse.getResult().forEach((info) -> {
            result.append(info).append("\n");
        });
        result.append("\n\n-------------------------\n\n");
        result.append(parse.hasErros() ? "Arquivo contém " + parse.erros() + " erros sintáticos (excluindo Modo Pânico)" : "Arquivo analisado com sucesso!");

        return result.toString();
    }

    public void startSymbol() {
        program();

    }

    private void program() {
        structs();
        const_block();
        var_block();
        start_block();
        decls();
    }

    private void start_block() {
        if (parse.getCorrentToken().val.equals("procedure")) {
            parse.nextToken();
            if (parse.getCorrentToken().val.equals("start")) {
                parse.nextToken();
                if (!parse.getCorrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                if (!parse.getCorrentToken().val.equals(")")) {
                    parse.includeError(")");
                } else {
                    parse.nextToken();
                }
                func_block();
            } else {
                follow = Arrays.asList("function", "procedure");
                parse.includeError("start", follow);
            }
        } else {
            follow = Arrays.asList("function", "procedure");
            parse.includeError("procedure start", follow);
        }
    }

    private void decls() {
        if (parse.getCorrentToken().val.equals("function")
                || parse.getCorrentToken().val.equals("procedure")) {
            decl();
            decls();
        }
    }

    private void decl() {
        func_decl();
        proc_decl();
    }

    private void func_decl() {
        if (parse.getCorrentToken().val.equals("function")) {
            parse.nextToken();
            param_type();
            if (parse.getCorrentToken().type == Token.T.IDE) {
                parse.nextToken();
                if (!parse.getCorrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                params();
                if (parse.getCorrentToken().val.equals(")")) {
                    parse.nextToken();
                    func_block();
                } else {
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError(")", follow);
                }
            } else {
                follow = Arrays.asList("procedure", "function");
                parse.includeError("IDE", follow);
            }
        }
    }

    private void proc_decl() {
        if (parse.getCorrentToken().val.equals("procedure")) {
            parse.nextToken();
            if (parse.getCorrentToken().type == Token.T.IDE && !parse.getCorrentToken().val.equals("start")) {
                parse.nextToken();
                if (!parse.getCorrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                params();
                if (parse.getCorrentToken().val.equals(")")) {
                    parse.nextToken();
                    func_block();
                } else {
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError(")", follow);
                }
            } else {
                follow = Arrays.asList("procedure", "function");
                parse.includeError("IDE", follow);
            }
        }
    }

    private void param_type() {
        if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
        } else if (types.contains(parse.getCorrentToken().val)) {
            type();
        }
    }

    private void params() {
        if (parse.getCorrentToken().val.equals("int")
                || parse.getCorrentToken().val.equals("real")
                || parse.getCorrentToken().val.equals("boolean")
                || parse.getCorrentToken().val.equals("string")
                || parse.getCorrentToken().val.equals("struct")
                || parse.getCorrentToken().type == Token.T.IDE) {
            param();
            params_list();
        }
    }

    private void params_list() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.nextToken();
            param();
            params_list();
        }

    }

    private void param() {
        param_type();
        if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
            param_arrays();
        } else {
            follow = Arrays.asList(",", ")");
            parse.includeError(", , )", follow);
        }
    }

    private void param_arrays() {
        if (parse.getCorrentToken().val.equals("[")) {
            parse.nextToken();
            if (parse.getCorrentToken().val.equals("]")) {
                parse.nextToken();
                param_mult_arrays();
            } else {
                follow = Arrays.asList(",", ")");
                parse.includeError("]", follow);
            }
        }
    }

    private void param_mult_arrays() {
        if (parse.getCorrentToken().val.equals("[")) {
            parse.nextToken();
            if (parse.getCorrentToken().type == Token.T.NRO) {
                parse.nextToken();
                if (parse.getCorrentToken().val.equals("]")) {
                    parse.nextToken();
                    param_mult_arrays();
                } else {
                    follow = Arrays.asList(",", ")");
                    parse.includeError("]", follow);
                }
            } else {
                follow = Arrays.asList(",", ")");
                parse.includeError("NMO", follow);
            }
        }
    }

    private void structs() {
        if (parse.getCorrentToken().val.equals("struct")) {
            struct_block();
            structs();
        }
    }

    private void struct_block() {
        if (parse.getCorrentToken().val.equals("struct")) {
            parse.nextToken();
            if (parse.getCorrentToken().type == Token.T.IDE) {
                parse.nextToken();
                extends_();
                if (!parse.getCorrentToken().val.equals("{")) {
                    parse.includeError("{");
                } else {
                    parse.nextToken();
                }
                const_block();
                var_block();
                if (!parse.getCorrentToken().val.equals("}")) {
                    follow = Arrays.asList("const", "procedure", "struct", "var");
                    parse.includeError("}", follow);
                } else {
                    parse.nextToken();
                }
            } else {
                follow = Arrays.asList("const", "procedure", "struct", "var");
                parse.includeError("IDE", follow);
            }
        }
    }

    private void extends_() {
        if (parse.getCorrentToken().val.equals("extends")) {
            parse.nextToken();
            if (!parse.getCorrentToken().val.equals("struct")) {
                follow = Arrays.asList("{");
                parse.includeError("struct", follow);
            } else {
                parse.nextToken();
                if (parse.getCorrentToken().type == Token.T.IDE) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("{");
                    parse.includeError("IDE", follow);
                }
            }
        }
    }

    private void const_block() {
        if (parse.getCorrentToken().val.equals("const")) {
            parse.nextToken();
            if (!parse.getCorrentToken().val.equals("{")) {
                parse.includeError("{");
            } else {
                parse.nextToken();
            }
            const_decls();
            if (!parse.getCorrentToken().val.equals("}")) {
                follow = Arrays.asList("procedure", "var");
                parse.includeError("}", follow);
            } else {
                parse.nextToken();
            }

        }

    }

    private void var_block() {
        if (parse.getCorrentToken().val.equals("var")) {
            parse.nextToken();
            if (!parse.getCorrentToken().val.equals("{")) {
                parse.includeError("{");
            } else {
                parse.nextToken();
            }
            var_decls();
            if (!parse.getCorrentToken().val.equals("}")) {
                follow = Arrays.asList("while", "read", "{", "local", "procedure", "return", "global", "print", "if", "IDE");
                parse.includeError("}", follow);

            } else {
                parse.nextToken();
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
        if (types.contains(parse.getCorrentToken().val)) {
            type();
            const_();
            const_list();
        } else if (parse.getCorrentToken().val.equals("typedef")) {
            typedef();
        } else if (parse.getCorrentToken().val.equals("local") || parse.getCorrentToken().val.equals("global")) {
            stm_scope();
        } else if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
            const_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("string, typedef, local, boolean, int, struct, real, global, IDE", follow);
        }
    }

    private void stm_scope() {
        if (parse.getCorrentToken().val.equals("local") || parse.getCorrentToken().val.equals("global")) {
            parse.nextToken();
            access();
            accesses();
            assign();
        }
    }

    private void var_decl() {
        if (types.contains(parse.getCorrentToken().val)) {
            type();
            var_();
            var_list();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";, ,", follow);
            }
        } else if (parse.getCorrentToken().val.equals("typedef")) {
            typedef();
        } else if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
            var_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("int, real, boolean, string, struct", follow);
        }
    }

    private void const_id() {
        if (parse.getCorrentToken().type == Token.T.IDE) {
            const_();
            const_list();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else if (parse.getCorrentToken().val.equals("--")
                || parse.getCorrentToken().val.equals("(")
                || parse.getCorrentToken().val.equals(".")
                || parse.getCorrentToken().val.equals("=")
                || parse.getCorrentToken().val.equals("++")
                || parse.getCorrentToken().val.equals("[")) {
            stm_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("--, (, ., =, ++, [, IDE", follow);
        }
    }

    private void var_id() {
        if (parse.getCorrentToken().type == Token.T.IDE) {
            var_();
            var_list();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else if (parse.getCorrentToken().val.equals("--")
                || parse.getCorrentToken().val.equals("(")
                || parse.getCorrentToken().val.equals(".")
                || parse.getCorrentToken().val.equals("=")
                || parse.getCorrentToken().val.equals("++")
                || parse.getCorrentToken().val.equals("[")) {
            stm_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("--, (, ., =, ++, [, IDE", follow);
        }
    }

    private void stm_id() {
        if (parse.getCorrentToken().val.equals("=")
                || parse.getCorrentToken().val.equals("++")
                || parse.getCorrentToken().val.equals("--")) {
            assign();
        } else if (parse.getCorrentToken().val.equals("[")) {
            array();
            arrays();
            accesses();
            assign();
        } else if (parse.getCorrentToken().val.equals(".")) {
            access();
            accesses();
            assign();
        } else if (parse.getCorrentToken().val.equals("(")) {
            parse.nextToken();
            args();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.nextToken();
                if (parse.getCorrentToken().val.equals(";")) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", ";", "struct", "global", "print", "if", "{");
                    parse.includeError(";", follow);
                }
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", ";", "struct", "global", "print", "if", "{");
                parse.includeError(")", follow);
            }
        } else {
            follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", ";", "struct", "global", "print", "if", "{");
            parse.includeError("=, ++, --, [, . , (", follow);
        }
    }

    private void assign() {
        if (parse.getCorrentToken().val.equals("=")) {
            parse.nextToken();
            expr();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
                parse.includeError(";", follow);
            }
        } else if (parse.getCorrentToken().val.equals("++")
                || parse.getCorrentToken().val.equals("--")) {
            parse.nextToken();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
                parse.includeError(";", follow);
            }
        } else {
            follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
            parse.includeError("=, ++, --", follow);

        }
    }

    private void type() {
        if (types.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().type == Token.T.IDE) {
            if (parse.getCorrentToken().val.equals("struct")) {
                parse.nextToken();
                if (parse.getCorrentToken().type != Token.T.IDE) {
                    follow = Arrays.asList("IDE");
                    parse.includeError("IDE", follow);
                } else {
                    parse.nextToken();
                }
            } else {
                parse.nextToken();
            }
        }
    }

    private void typedef() {
        if (parse.getCorrentToken().val.equals("typedef")) {
            parse.nextToken();
            type();
            if (parse.getCorrentToken().type == Token.T.IDE) {
                parse.nextToken();
                if (parse.getCorrentToken().val.equals(";")) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("local", "}", "int", "real", "IDE", "string", "typedef", "boolean", "struct", "global");
                    parse.includeError(";", follow);
                }
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "IDE", "string", "typedef", "boolean", "struct", "global");
                parse.includeError("IDE", follow);
            }
        }
    }

    private void var_() {
        if (parse.getCorrentToken().type != Token.T.IDE) {
            follow = Arrays.asList(";", ",");
            parse.includeError("IDE", follow);
        } else {
            parse.nextToken();
            arrays();
        }
    }

    private void var_list() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.nextToken();
            var_();
            var_list();
        }
    }

    private void const_() {
        if (parse.getCorrentToken().type != Token.T.IDE) {
            follow = Arrays.asList("=", ",", ";");
            parse.includeError("IDE", follow);
        } else {
            parse.nextToken();
            arrays();
        }
    }

    private void const_list() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.nextToken();
            const_();
            const_list();
        } else if (parse.getCorrentToken().val.equals("=")) {
            parse.nextToken();
            decl_atribute();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else {
            follow = Arrays.asList("string", "typedef", "local", "}", "boolean", "int", "struct", "real", "global", "IDE");
            parse.includeError(", , =", follow);
        }
    }

    private void func_block() {
        if (!parse.getCorrentToken().val.equals("{")) {
            parse.includeError("{");
        } else {
            parse.nextToken();
        }
        var_block();
        func_stms();
        if (parse.getCorrentToken().val.equals("}")) {
            parse.nextToken();
        } else {
            follow = Arrays.asList("function", "procedure");
            parse.includeError("}", follow);
        }
    }

    private void func_stms() {
        if (firstFunc_stm.contains(parse.getCorrentToken().val) || parse.getCorrentToken().type == Token.T.IDE) {
            func_stm();
            func_stms();
        }
    }

    private void func_stm() {
        follow = Arrays.asList("local", "}", "else", "return", "while", "id", "read", "global", "print", "{", "if");
        if (parse.getCorrentToken().val.equals("if")) {
            parse.nextToken();
            if (!parse.getCorrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            log_expr();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.nextToken();
                if (!parse.getCorrentToken().val.equals("then")) {
                    parse.includeError("then");
                } else {
                    parse.nextToken();
                }
                func_stm();
                else_stm();
                func_stm();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ';', "global", "print", "{", "if");
                parse.includeError(")", follow);
            }
        } else if (parse.getCorrentToken().val.equals("while")) {
            parse.nextToken();
            if (!parse.getCorrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            log_expr();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.nextToken();
                func_stm();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError("), IDE, NRO, LOG, ART, REL, DEL, CAD", follow);
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
        }
    }

    private void func_normal_stm() {

        if (parse.getCorrentToken().val.equals("{")) {
            parse.nextToken();
            func_stms();
            if (parse.getCorrentToken().val.equals("}")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError("}", follow);
            }
        } else if (first_var_stm.contains(parse.getCorrentToken().val)
                || parse.getCorrentToken().type == Token.T.IDE) {
            var_stm();

        } else if (parse.getCorrentToken().val.equals(";")) {
            parse.nextToken();
        } else if (parse.getCorrentToken().val.equals("return")) {
            parse.nextToken();
            expr();
            if (parse.getCorrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError(";", follow);
            }
        }
    }

    private void else_stm() {
        if (parse.getCorrentToken().val.equals("else")) {
            parse.nextToken();
        }
    }

    private void var_stm() {
        if (parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            stm_scope();
        } else if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
            stm_id();
        } else if (parse.getCorrentToken().val.equals("read")
                || parse.getCorrentToken().val.equals("print")) {
            parse.nextToken();
            if (parse.getCorrentToken().val.equals("(")) {
                parse.nextToken();
                args();
                follow = Arrays.asList(";");
                if (parse.getCorrentToken().val.equals(")")) {
                    parse.nextToken();
                    if (parse.getCorrentToken().val.equals(";")) {
                        parse.nextToken();
                    } else {
                        follow = Arrays.asList(";", "return", "else", "IDE", "global",
                                "local", "print", "if", "read", "}", "while", "{");
                        parse.includeError(";", follow);
                    }
                } else {
                    follow = Arrays.asList(";", "return", "else", "IDE", "global",
                            "local", "print", "if", "read", "}", "while", "{");
                    parse.includeError(")", follow);
                }
            } else {
                follow = Arrays.asList(";", "return", "else", "IDE", "global",
                        "local", "print", "if", "read", "}", "while", "{");
                parse.includeError("(", follow);
            }
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
                || parse.getCorrentToken().val.equals("(")
                || parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            expr();
        } else {
            follow = Arrays.asList(";");
            parse.includeError("{, (, NRO, IDE, CAD", follow);
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
            parse.nextToken();
            index();
            if (parse.getCorrentToken().val.equals("]")) {
                parse.nextToken();
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
            parse.nextToken();
            array_def();
            if (parse.getCorrentToken().val.equals("}")) {
                parse.nextToken();
                array_vector();
            } else {
                follow = Arrays.asList(";");
                parse.includeError("}, ,", follow);
            }
        }
    }

    private void array_vector() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.nextToken();
            array_decl();
        }
    }

    private void array_def() {
        expr();
        array_expr();
    }

    private void array_expr() {
        if (parse.getCorrentToken().val.equals(",")) {
            parse.nextToken();
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
            parse.nextToken();
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
            parse.nextToken();
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
            parse.nextToken();
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
            parse.nextToken();
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
            parse.nextToken();
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
            parse.nextToken();
            unary();
            mult_();
        }
    }

    private void unary() {
        if (parse.getCorrentToken().val.equals("!")) {
            parse.nextToken();
            unary();
        } else {
            value();
        }
    }

    private void value() {
        if (parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().type == Token.T.CAD
                || parse.getCorrentToken().type == Token.T.LOG) {
            parse.nextToken();
        } else if (parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            parse.nextToken();
            access();
        } else if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
            id_value();
        } else if (parse.getCorrentToken().val.equals("(")) {
            parse.nextToken();
            expr();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
                parse.includeError(")", follow);
            }
        } else if (parse.getCorrentToken().val.equals("true") || parse.getCorrentToken().val.equals("false")) {
            parse.nextToken();
        } else {
            follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
            parse.includeError("LOG, CAD, NRO, 'local, 'global', IDE, (", follow);
        }
    }

    private void id_value() {
        if (parse.getCorrentToken().val.equals("[")
                || parse.getCorrentToken().val.equals(".")) {
            arrays();
            accesses();
        } else if (parse.getCorrentToken().val.equals("(")) {
            parse.nextToken();
            args();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
                parse.includeError(")", follow);
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
            parse.nextToken();
            expr();
            args_list();
        }
    }

    private void access() {
        if (parse.getCorrentToken().val.equals(".")) {
            parse.nextToken();
            if (parse.getCorrentToken().type == Token.T.IDE) {
                parse.nextToken();
                arrays();
            }
        } else {
            follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
            parse.includeError(".", follow);
        }
    }

    private void accesses() {
        if (parse.getCorrentToken().val.equals(".")) {
            access();
            accesses();
        }
    }

    private void log_expr() {
        log_or();
    }

    private void log_or() {
        log_and();
        log_or_();
    }

    private void log_or_() {
        if (parse.getCorrentToken().val.equals("||")) {
            parse.nextToken();
            log_and();
            log_or_();
        }
    }

    private void log_and() {
        log_equate();
        log_and_();
    }

    private void log_and_() {
        if (parse.getCorrentToken().val.equals("&&")) {
            parse.nextToken();
            log_equate();
            log_and_();
        }
    }

    private void log_equate() {
        log_compare();
        log_equate_();
    }

    private void log_equate_() {
        if (parse.getCorrentToken().val.equals("==")
                || parse.getCorrentToken().val.equals("!=")) {
            parse.nextToken();
            log_compare();
            log_equate_();
        }
    }

    private void log_compare() {
        log_unary();
        log_compare_();
    }

    private void log_compare_() {
        if (parse.getCorrentToken().val.equals("<")
                || parse.getCorrentToken().val.equals(">")
                || parse.getCorrentToken().val.equals(">=")
                || parse.getCorrentToken().val.equals("<=")) {
            parse.nextToken();
            log_unary();
            log_compare_();
        }
    }

    private void log_unary() {
        if (parse.getCorrentToken().val.equals("!")) {
            parse.nextToken();
            log_unary();
        } else if (parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().type == Token.T.IDE
                || parse.getCorrentToken().type == Token.T.CAD
                || parse.getCorrentToken().val.equals("false")
                || parse.getCorrentToken().val.equals("true")
                || parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")
                || parse.getCorrentToken().val.equals("(")) {
            log_value();
        }
    }

    private void log_value() {
        if (parse.getCorrentToken().type == Token.T.NRO
                || parse.getCorrentToken().val.equals("false")
                || parse.getCorrentToken().val.equals("true")
                || parse.getCorrentToken().type == Token.T.CAD) {
            parse.nextToken();
        } else if (parse.getCorrentToken().val.equals("local")
                || parse.getCorrentToken().val.equals("global")) {
            parse.nextToken();
            access();
        } else if (parse.getCorrentToken().type == Token.T.IDE) {
            parse.nextToken();
            id_value();
        } else if (parse.getCorrentToken().val.equals("(")) {
            parse.nextToken();
            log_expr();
            if (parse.getCorrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList(")");
                parse.includeError(")", follow);
            }
        } else {
            follow = Arrays.asList(")");
            parse.includeError("false, local, str, true, IDE, NMO, global", follow);
        }
    }
}
