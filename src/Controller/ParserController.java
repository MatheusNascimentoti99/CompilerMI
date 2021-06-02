/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Parser;
import Model.Token;
import Utils.VarTable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matheus Nascimento
 */
public class ParserController {

    private static final String ERROR_ESCOPE = "já declarado no escopo";

    private final List types = Arrays.asList("int", "real", "boolean", "string", "struct");
    private final Parser parse;
    List firstFunc_stm = Arrays.asList("if", "while", "{", "return", ";", "local", "global", "print", "read");
    List first_var_stm = Arrays.asList("local", "global", "print", "read");
    private List follow;
    VarTable simbolTable;
    TableController tableController;

    public ParserController(LinkedList<Token> tokens) {
        parse = new Parser(tokens);
        simbolTable = new VarTable();
        tableController = new TableController();
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
        decls();
    }

    private void start_block() {

        if (parse.getCurrentToken().val.equals("start")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            if (!parse.getCurrentToken().val.equals(")")) {
                parse.includeError(")");
            } else {
                parse.nextToken();
            }
            func_block();
        } else {
            follow = Arrays.asList("function", "procedure");
            parse.includeError("start", follow);
        }
    }

    private void decls() {
        if (parse.getCurrentToken().val.equals("function")
                || parse.getCurrentToken().val.equals("procedure")) {
            decl();
            decls();
        }
    }

    private void decl() {
        func_decl();
        proc_decl();
    }

    private void func_decl() {
        if (parse.getCurrentToken().val.equals("function")) {
            parse.nextToken();
            param_type();
            if (parse.getCurrentToken().type == Token.T.IDE) {
                parse.nextToken();
                if (!parse.getCurrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                params();
                if (parse.getCurrentToken().val.equals(")")) {
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
        tableController.newEscape();
        if (parse.getCurrentToken().val.equals("procedure")) {
            parse.nextToken();
            if (parse.getCurrentToken().type == Token.T.IDE && !parse.getCurrentToken().val.equals("start")) {
                parse.nextToken();
                if (!parse.getCurrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                params();
                if (parse.getCurrentToken().val.equals(")")) {
                    parse.nextToken();
                    func_block();
                } else {
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError(")", follow);
                }
            } else if (parse.getCurrentToken().val.equals("start")) {
                start_block();
                if (parse.getCurrentToken().type != Token.T.EOF) {
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError("EOF", follow);

                }
            } else {
                follow = Arrays.asList("procedure", "function");
                parse.includeError("IDE", follow);
            }
        }
        tableController.removeEscape();
    }

    private void param_type() {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
        } else if (types.contains(parse.getCurrentToken().val)) {
            type();
        }
    }

    private void params() {
        if (parse.getCurrentToken().val.equals("int")
                || parse.getCurrentToken().val.equals("real")
                || parse.getCurrentToken().val.equals("boolean")
                || parse.getCurrentToken().val.equals("string")
                || parse.getCurrentToken().val.equals("struct")
                || parse.getCurrentToken().type == Token.T.IDE) {
            param();
            params_list();
        }
    }

    private void params_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            param();
            params_list();
        }

    }

    private void param() {
        param_type();
        if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
            param_arrays();
        } else {
            follow = Arrays.asList(",", ")");
            parse.includeError(", , )", follow);
        }
    }

    private void param_arrays() {
        if (parse.getCurrentToken().val.equals("[")) {
            parse.nextToken();
            if (parse.getCurrentToken().val.equals("]")) {
                parse.nextToken();
                param_mult_arrays();
            } else {
                follow = Arrays.asList(",", ")");
                parse.includeError("]", follow);
            }
        }
    }

    private void param_mult_arrays() {
        if (parse.getCurrentToken().val.equals("[")) {
            parse.nextToken();
            if (parse.getCurrentToken().type == Token.T.INT || parse.getCurrentToken().type == Token.T.REAL) {
                parse.nextToken();
                if (parse.getCurrentToken().val.equals("]")) {
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
        if (parse.getCurrentToken().val.equals("struct")) {
            struct_block();
            structs();
        }
    }

    private void struct_block() {
        if (parse.getCurrentToken().val.equals("struct")) {
            if (!tableController.addStructScape(parse.nextToken())) {
                parse.includeError("já declarado", Token.T.SEMANTIC);
            }
            if (parse.getCurrentToken().type == Token.T.IDE) {
                parse.nextToken();
                extends_();
                if (!parse.getCurrentToken().val.equals("{")) {
                    parse.includeError("{");
                } else {
                    parse.nextToken();
                }
                const_block();
                var_block();
                if (!parse.getCurrentToken().val.equals("}")) {
                    follow = Arrays.asList("const", "procedure", "struct", "var");
                    parse.includeError("}", follow);
                } else {
                    parse.nextToken();
                }
            } else {
                follow = Arrays.asList("const", "procedure", "struct", "var");
                parse.includeError("IDE", follow);
            }
            tableController.removeStructScape();
        }
    }

    private void extends_() {
        if (parse.getCurrentToken().val.equals("extends")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("struct")) {
                follow = Arrays.asList("{");
                parse.includeError("struct", follow);
            } else {
                parse.nextToken();
                if (parse.getCurrentToken().type == Token.T.IDE) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("{");
                    parse.includeError("IDE", follow);
                }
            }
        }
    }

    private void const_block() {
        if (parse.getCurrentToken().val.equals("const")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("{")) {
                parse.includeError("{");
            } else {
                parse.nextToken();
            }
            const_decls();
            if (!parse.getCurrentToken().val.equals("}")) {
                follow = Arrays.asList("procedure", "var");
                parse.includeError("}", follow);
            } else {
                parse.nextToken();
            }

        }

    }

    private void var_block() {
        if (parse.getCurrentToken().val.equals("var")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("{")) {
                parse.includeError("{");
            } else {
                parse.nextToken();
            }
            var_decls();
            if (!parse.getCurrentToken().val.equals("}")) {
                follow = Arrays.asList("while", "read", "{", "local", "procedure", "return", "global", "print", "if", "IDE");
                parse.includeError("}", follow);

            } else {
                parse.nextToken();
            }
        }

    }

    private void const_decls() {
        if (parse.getCurrentToken().type == Token.T.IDE
                || types.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().val.equals("typedef")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            const_decl();
            const_decls();
        }
    }

    private void var_decls() {
        if (parse.getCurrentToken().type == Token.T.IDE || types.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().val.equals("typedef")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            var_decl();
            var_decls();
        }
    }

    private void const_decl() {
        if (types.contains(parse.getCurrentToken().val)) {
            type();
            const_();
            const_list();
        } else if (parse.getCurrentToken().val.equals("typedef")) {
            typedef();
        } else if (parse.getCurrentToken().val.equals("local") || parse.getCurrentToken().val.equals("global")) {
            stm_scope();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
            const_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("string, typedef, local, boolean, int, struct, real, global, IDE", follow);
        }
    }

    private void stm_scope() {
        if (parse.getCurrentToken().val.equals("local") || parse.getCurrentToken().val.equals("global")) {
            parse.nextToken();
            access();
            accesses();
            assign();
        }
    }

    private void var_decl() {
        if (types.contains(parse.getCurrentToken().val)) {
            type();
            var_();
            var_list();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";, ,", follow);
            }
        } else if (parse.getCurrentToken().val.equals("typedef")) {
            typedef();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
            var_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("int, real, boolean, string, struct", follow);
        }
    }

    private void const_id() {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            const_();
            const_list();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else if (parse.getCurrentToken().val.equals("--")
                || parse.getCurrentToken().val.equals("(")
                || parse.getCurrentToken().val.equals(".")
                || parse.getCurrentToken().val.equals("=")
                || parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("[")) {
            stm_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("--, (, ., =, ++, [, IDE", follow);
        }
    }

    private void var_id() {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            var_();
            var_list();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else if (parse.getCurrentToken().val.equals("--")
                || parse.getCurrentToken().val.equals("(")
                || parse.getCurrentToken().val.equals(".")
                || parse.getCurrentToken().val.equals("=")
                || parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("[")) {
            stm_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("--, (, ., =, ++, [, IDE", follow);
        }
    }

    private void stm_id() {
        if (parse.getCurrentToken().val.equals("=")
                || parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("--")) {
            assign();
        } else if (parse.getCurrentToken().val.equals("[")) {
            array();
            arrays();
            accesses();
            assign();
        } else if (parse.getCurrentToken().val.equals(".")) {
            access();
            accesses();
            assign();
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            args();
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
                if (parse.getCurrentToken().val.equals(";")) {
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
        if (parse.getCurrentToken().val.equals("=")) {
            parse.nextToken();
            expr();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
                parse.includeError(";", follow);
            }
        } else if (parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("--")) {
            parse.nextToken();
            if (parse.getCurrentToken().val.equals(";")) {
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
        if (types.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().type == Token.T.IDE) {
            if (parse.getCurrentToken().val.equals("struct")) {
                parse.nextToken();
                if (parse.getCurrentToken().type != Token.T.IDE) {
                    follow = Arrays.asList("IDE");
                    parse.includeError("IDE", follow);
                } else {
                    parse.nextToken();
                }
            } else {
                tableController.addType(parse.getCurrentToken());
                parse.nextToken();
            }
        }
    }

    private void typedef() {
        if (parse.getCurrentToken().val.equals("typedef")) {
            parse.nextToken();
            type();
            if (parse.getCurrentToken().type == Token.T.IDE) {
                parse.nextToken();
                if (parse.getCurrentToken().val.equals(";")) {
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
        if (parse.getCurrentToken().type != Token.T.IDE) {
            follow = Arrays.asList(";", ",");
            parse.includeError("IDE", follow);
        } else {
            if (tableController.addVar(parse.getCurrentToken())) {
                parse.includeError(ERROR_ESCOPE, Token.T.SEMANTIC);
            }
            parse.nextToken();
            arrays();
        }
    }

    private void var_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            var_();
            var_list();
        }
    }

    private void const_() {
        if (parse.getCurrentToken().type != Token.T.IDE) {
            follow = Arrays.asList("=", ",", ";");
            parse.includeError("IDE", follow);
        } else {
            if (tableController.addConst(parse.getCurrentToken())) {
                parse.includeError(ERROR_ESCOPE, Token.T.SEMANTIC);
            }
            parse.nextToken();
            arrays();
        }
    }

    private void const_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            const_();
            const_list();
        } else if (parse.getCurrentToken().val.equals("=")) {
            parse.nextToken();
            decl_atribute();
            if (parse.getCurrentToken().val.equals(";")) {
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
        tableController.newEscape();
        if (!parse.getCurrentToken().val.equals("{")) {
            parse.includeError("{");
        } else {
            parse.nextToken();
        }
        var_block();
        func_stms();
        if (parse.getCurrentToken().val.equals("}")) {
            parse.nextToken();
        } else {
            follow = Arrays.asList("function", "procedure");
            parse.includeError("}", follow);
        }
        tableController.removeEscape();
    }

    private void func_stms() {
        if (firstFunc_stm.contains(parse.getCurrentToken().val) || parse.getCurrentToken().type == Token.T.IDE) {
            func_stm();
            func_stms();
        }
    }

    private void func_stm() {
        follow = Arrays.asList("local", "}", "else", "return", "while", "id", "read", "global", "print", "{", "if");
        if (parse.getCurrentToken().val.equals("if")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            log_expr();
            if (!parse.getCurrentToken().val.equals(")")) {
                parse.includeError(")");
            } else {
                parse.nextToken();
            }
            if (!parse.getCurrentToken().val.equals("then")) {
                parse.includeError("then");
            } else {
                parse.nextToken();
            }
            func_stm();
            else_stm();
            func_stm();
        } else if (parse.getCurrentToken().val.equals("while")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            log_expr();
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
                func_stm();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError("), IDE, NRO, LOG, ART, REL, DEL, CAD", follow);
            }
        } else if (parse.getCurrentToken().val.equals("{")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")
                || parse.getCurrentToken().val.equals("print")
                || parse.getCurrentToken().val.equals("read")
                || parse.getCurrentToken().val.equals("return")
                || parse.getCurrentToken().val.equals(";")
                || parse.getCurrentToken().type == Token.T.IDE) {
            func_normal_stm();
        }
    }

    private void func_normal_stm() {

        if (parse.getCurrentToken().val.equals("{")) {
            parse.nextToken();
            func_stms();
            if (parse.getCurrentToken().val.equals("}")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError("}", follow);
            }
        } else if (first_var_stm.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().type == Token.T.IDE) {
            var_stm();

        } else if (parse.getCurrentToken().val.equals(";")) {
            parse.nextToken();
        } else if (parse.getCurrentToken().val.equals("return")) {
            parse.nextToken();
            expr();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError(";", follow);
            }
        }
    }

    private void else_stm() {
        if (parse.getCurrentToken().val.equals("else")) {
            parse.nextToken();
        }
    }

    private void var_stm() {
        if (parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            stm_scope();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
            stm_id();
        } else if (parse.getCurrentToken().val.equals("read")
                || parse.getCurrentToken().val.equals("print")) {
            parse.nextToken();
            if (parse.getCurrentToken().val.equals("(")) {
                parse.nextToken();
                args();
                follow = Arrays.asList(";");
                if (parse.getCurrentToken().val.equals(")")) {
                    parse.nextToken();
                    if (parse.getCurrentToken().val.equals(";")) {
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
        if (parse.getCurrentToken().val.equals("{")) {
            array_decl();
        } else if (parse.getCurrentToken().type == Token.T.LOG
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.ART
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.equals("!")
                || parse.getCurrentToken().val.equals("(")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            expr();
        } else {
            follow = Arrays.asList(";");
            parse.includeError("{, (, NRO, IDE, CAD", follow);
        }

    }

    private void arrays() {
        if (parse.getCurrentToken().val.equals("[")) {
            array();
            arrays();
        }
    }

    private void array() {
        if (parse.getCurrentToken().val.equals("[")) {
            parse.nextToken();
            index();
            if (parse.getCurrentToken().val.equals("]")) {
                parse.nextToken();
            }
        }
    }
//incompleta

    private void index() {
        if (parse.getCurrentToken().type == Token.T.LOG
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.ART
                || parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.equals("!")
                || parse.getCurrentToken().val.equals("(")) {
            expr();
        }
    }

    private void array_decl() {
        if (parse.getCurrentToken().val.equals("{")) {
            parse.nextToken();
            array_def();
            if (parse.getCurrentToken().val.equals("}")) {
                parse.nextToken();
                array_vector();
            } else {
                follow = Arrays.asList(";");
                parse.includeError("}, ,", follow);
            }
        }
    }

    private void array_vector() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            array_decl();
        }
    }

    private void array_def() {
        expr();
        array_expr();
    }

    private void array_expr() {
        if (parse.getCurrentToken().val.equals(",")) {
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
        if (parse.getCurrentToken().val.equals("||")) {
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
        if (parse.getCurrentToken().val.equals("&&")) {
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
        if (parse.getCurrentToken().val.equals("==") || parse.getCurrentToken().val.equals("!=")) {
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
        if (parse.getCurrentToken().type == Token.T.REL) {
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
        if (parse.getCurrentToken().val.equals("+")
                || parse.getCurrentToken().val.equals("-")) {
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
        if (parse.getCurrentToken().val.equals("*") || parse.getCurrentToken().val.equals("/")) {
            parse.nextToken();
            unary();
            mult_();
        }
    }

    private void unary() {
        if (parse.getCurrentToken().val.equals("!")) {
            parse.nextToken();
            unary();
        } else {
            value();
        }
    }

    private void value() {
        if (parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().type == Token.T.LOG) {
            parse.nextToken();
        } else if (parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            parse.nextToken();
            access();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
            id_value();
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            expr();
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
                parse.includeError(")", follow);
            }
        } else if (parse.getCurrentToken().val.equals("true") || parse.getCurrentToken().val.equals("false")) {
            parse.nextToken();
        } else {
            follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
            parse.includeError("LOG, CAD, NRO, 'local, 'global', IDE, (", follow);
        }
    }

    private void id_value() {
        if (parse.getCurrentToken().val.equals("[")
                || parse.getCurrentToken().val.equals(".")) {
            arrays();
            accesses();
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            args();
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
                parse.includeError(")", follow);
            }
        }
    }

    private void args() {
        if (parse.getCurrentToken().type == Token.T.LOG
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().type == Token.T.ART
                || parse.getCurrentToken().val.equals("!")
                || parse.getCurrentToken().val.equals("(")) {
            expr();
            args_list();
        }
    }

    private void args_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            expr();
            args_list();
        }
    }

    private void access() {
        if (parse.getCurrentToken().val.equals(".")) {
            parse.nextToken();
            if (parse.getCurrentToken().type == Token.T.IDE) {
                parse.nextToken();
                arrays();
            }
        } else {
            follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
            parse.includeError(".", follow);
        }
    }

    private void accesses() {
        if (parse.getCurrentToken().val.equals(".")) {
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
        if (parse.getCurrentToken().val.equals("||")) {
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
        if (parse.getCurrentToken().val.equals("&&")) {
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
        if (parse.getCurrentToken().val.equals("==")
                || parse.getCurrentToken().val.equals("!=")) {
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
        if (parse.getCurrentToken().val.equals("<")
                || parse.getCurrentToken().val.equals(">")
                || parse.getCurrentToken().val.equals(">=")
                || parse.getCurrentToken().val.equals("<=")) {
            parse.nextToken();
            log_unary();
            log_compare_();
        }
    }

    private void log_unary() {
        if (parse.getCurrentToken().val.equals("!")) {
            parse.nextToken();
            log_unary();
        } else if (parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.equals("false")
                || parse.getCurrentToken().val.equals("true")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")
                || parse.getCurrentToken().val.equals("(")) {
            log_value();
        }
    }

    private void log_value() {
        if (parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().val.equals("false")
                || parse.getCurrentToken().val.equals("true")
                || parse.getCurrentToken().type == Token.T.CAD) {
            parse.nextToken();
        } else if (parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            parse.nextToken();
            access();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            parse.nextToken();
            id_value();
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            log_expr();
            if (parse.getCurrentToken().val.equals(")")) {
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
