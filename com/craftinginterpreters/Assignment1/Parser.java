package com.craftinginterpreters.Assignment1;

import static com.craftinginterpreters.Assignment1.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignment();
    }

    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration();

            if (match(RIVER))
                return riverDeclaration();

            if (match(DAM))
                return damDeclaration();

            if (match(TIMEUNITS)) {
                return timeUnitDeclaration();
            }

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(PRINT))
            return printStatement();

        if (match(LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt riverDeclaration() {
        Token name = consume(IDENTIFIER, "Expect river name.");
        consume(EQUAL, "Expect '=' after river name.");
        Expr initalizer = expression();
        consume(SEMICOLON, "Expect ';' after river declaration.");
        return new Stmt.Var(name, initalizer);
    }

    private Stmt timeUnitDeclaration() {
        Token name = consume(IDENTIFIER, "Expect time units name.");
        consume(EQUAL, "Expect '=' after time unit name.");
        Expr initalizer = expression();
        consume(SEMICOLON, "Expect ';' after time unit declaration.");
        return new Stmt.Var(name, initalizer);

    }

    private Stmt damDeclaration() {
        Token name = consume(IDENTIFIER, "Expect dam name.");
        consume(EQUAL, "Expect '=' after river name.");
        Expr initalizer = expression();
        consume(SEMICOLON, "Expect ';' after river declaration.");
        return new Stmt.Var(name, initalizer);

    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr assignment() {
        Expr expr = connection();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(NEW)) {
            if (match(RIVER)) {
                consume(LEFT_PAREN,
                        "Expect '(' after 'Dam'. | Format: River('name', 'volume', flowRate, 'riverType', {'POI[n]'', 'POI[n+1]'...})");
                Expr name = expression();
                consume(COMMA, "Expect ',' between name and volume.");
                Expr volume = expression();
                consume(COMMA, "Expect ',' between volume and riverType.");
                Expr flowRate = expression();
                consume(COMMA, "Expect ',' between flowRate and riverType.");
                Expr riverType = expression();
                consume(COMMA, "Expect ',' between riverType and Point Of Interests.");

                // Point Of Interests as an array of strings
                List<String> associateRivers = new ArrayList<>();
                consume(LEFT_BRACE, "Expect '{' before Point Of Interests array.");
                if (!check(RIGHT_BRACE)) {
                    do {
                        Expr riverName = expression();
                        associateRivers.add(getLiteralValues(riverName));
                    } while (match(COMMA));
                }
                consume(RIGHT_BRACE, "Expect '}' after associateRivers array.");
                consume(RIGHT_PAREN, "Expect ')' after River arguments.");

                // get values from expressions and convert to strings
                return new Expr.River(getLiteralValues(name), getLiteralValues(volume),
                        Double.valueOf(getLiteralValues(flowRate)),
                        getLiteralValues(riverType),
                        associateRivers.toArray(new String[0]));
            }
            if (match(DAM)) {
                consume(LEFT_PAREN,
                        "Expect '(' after 'Dam'. | Format: Dam('name', 'capacity', 'parentRiver', 'destinationRiver')");
                Expr name = expression();
                consume(COMMA, "Expect ',' between name and capacity.");
                Expr capacity = expression();
                consume(COMMA, "Expect ',' between capacity and River reference.");
                Expr parentRiver = expression();
                consume(COMMA, "Expect ',' between parentRiver and destinationRiver.");
                Expr destinationRiver = expression();
                consume(RIGHT_PAREN, "Expect ')' after Dam arguments.");

                return new Expr.Dam(getLiteralValues(name), getLiteralValues(capacity), getLiteralValues(parentRiver),
                        getLiteralValues(destinationRiver));
            }

            if (match(TIMEUNITS)) {
                consume(LEFT_PAREN, "Expect '(' after 'TimeUnits'. | Format: TimeUnits('unit/period', numDays)");
                Expr unit = expression();
                consume(COMMA, "Expect ',' between unit and numDays.");
                Expr numDays = expression();
                consume(RIGHT_PAREN, "Expect ')' after TimeUnits arguments.");

                return new Expr.TimeUnits(getLiteralValues(unit), Double.valueOf(getLiteralValues(numDays)));
            }
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private String getLiteralValues(Expr expr) {
        if (expr instanceof Expr.Literal) {
            Object value = ((Expr.Literal) expr).value;
            if (value != null) {
                return value.toString();
            }

            if (expr instanceof Expr.Variable) {
                return ((Expr.Variable) expr).name.lexeme;

            }
        }
        throw new RuntimeException("Expected a literal expression with a non-null value.");
    }

    // Connections:
    private Expr connection() {
        Expr expr = equality();
        while (match(CONNECTS)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}