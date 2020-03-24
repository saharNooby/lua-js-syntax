grammar LuaJSSyntax;

program
    : statement* EOF
    ;

block
    : '{' statement* '}'
    ;

statement
    : ';'                                                                               # Semicolon
    | block                                                                             # BlockStatement
    | 'let' namelist ('=' explist)?                                                     # LocalVariableDeclaration
    | varlist '=' explist                                                               # GlobalVariableDeclaration
    | var assignmentOperator exp                                                        # AssginmentOperator
    | var nameAndArgs                                                                   # FunctionCall
    | NAME ':'                                                                          # LabelDeclaration
    | 'break'                                                                           # Break
    | 'continue'                                                                        # Continue
    | 'goto' NAME                                                                       # Goto
    | 'return' explist?                                                                 # Return
    | 'if' '(' exp ')' statement ('else' statement)?                                    # If
    | 'while' '(' exp ')' statement                                                     # While
    | 'do' statement 'while' '(' exp ')'                                                # DoWhile
    | 'for' '(' init=statement? ';' exp? ';' after=statement? ')' body=statement        # For
    | 'for' '(' namelist 'in' exp ')' statement                                         # ForIn
    | 'for' '(' NAME (',' NAME)? 'of' exp ')' statement                                 # ForOf
    | 'function' funcname '(' namelist? ')' block                                       # FunctionDeclaration
    | 'try' block 'catch' '(' NAME ')' block                                            # TryCatch
    | 'throw' exp                                                                       # Throw
    | var '++'                                                                          # Increment
    | var '--'                                                                          # Decrement
    ;

exp
    : '(' exp ')'                                                                       # ParenthesisExpression
    | ('nil' | 'true' | 'false')                                                        # Literal
    | number                                                                            # NumberLiteral
    | string                                                                            # StringLiteral
    | var                                                                               # VarExpression
    | var nameAndArgs                                                                   # FunctionCallExpression
    | table                                                                             # TableExpression
    | list                                                                              # ListExpression
    | 'function' '(' namelist? ')' block                                                # FunctionLiteral
    | ('(' namelist? ')' | NAME) '=>' (exp | block)                                     # ArrowFunctionLiteral
    | op=('!' | '-' | '~' | '#') exp                                                    # UnaryOperator
    | <assoc=right> exp op='**' exp                                                     # PowerOperator
    | exp op=('*' | '/' | '%') exp                                                      # MulDivModOperator
    | exp op=('+' | '-') exp                                                            # AddSubOperator
    | exp op=('<<' | '>>') exp                                                          # BitwiseShift
    | exp op='&' exp                                                                    # BitwiseAnd
    | exp op='^' exp                                                                    # BitwiseXor
    | exp op='|' exp                                                                    # BitwiseOr
    | <assoc=right> exp op='..' exp                                                     # ConcatOperator
    | exp op=('<' | '>' | '<=' | '>=' | '!=' | '==') exp                                # ComparisonOperator
    | exp op='&&' exp                                                                   # AndOperator
    | exp op='||' exp                                                                   # OrOperator
    | <assoc=right> exp '?' exp ':' exp                                                 # TernaryOperator
    ;

table
    : '{' entries? '}'
    ;

list
    : '[' elements? ']'
    ;

assignmentOperator
    : '*='
    | '/='
    | '%='
    | '+='
    | '-='
    | '&='
    | '|='
    | '^='
    | '<<='
    | '>>='
    | '..='
    | '**='
    ;

funcname
    : (NAME '::')? NAME
    ;

namelist
    : NAME (',' NAME)*
    ;

explist
    : exp (',' exp)*
    ;

entries
    : entry (',' entry)* ','?
    ;

entry
    : (NAME | key_expr) ':' exp
    ;

key_expr
    : '[' exp ']'
    ;

elements
    : exp (',' exp)* ','?
    ;

var
    : (NAME | '(' exp ')' varSuffix) varSuffix*
    ;

varSuffix
    : nameAndArgs* ('[' exp ']' | '.' NAME)
    ;

nameAndArgs
    : ('::' NAME)? args
    ;

args
    : '(' explist? ')'
    ;

varlist
    : var (',' var)*
    ;

number
    : INT | HEX | FLOAT | HEX_FLOAT
    ;

string
    : NORMALSTRING | CHARSTRING
    ;

NAME
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;

INT
    : Digit+
    ;

HEX
    : '0' [xX] HexDigit+
    ;

FLOAT
    : Digit+ '.' Digit* ExponentPart?
    | '.' Digit+ ExponentPart?
    | Digit+ ExponentPart
    ;

HEX_FLOAT
    : '0' [xX] HexDigit+ '.' HexDigit* HexExponentPart?
    | '0' [xX] '.' HexDigit+ HexExponentPart?
    | '0' [xX] HexDigit+ HexExponentPart
    ;

fragment
Digit
    : [0-9]
    ;

fragment
HexDigit
    : [0-9a-fA-F]
    ;

fragment
ExponentPart
    : [eE] [+-]? Digit+
    ;

fragment
HexExponentPart
    : [pP] [+-]? Digit+
    ;

NORMALSTRING
    : '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;

CHARSTRING
    : '\'' ( EscapeSequence | ~('\''|'\\') )* '\''
    ;

fragment
EscapeSequence
    : '\\' [abfnrtvz"'\\]
    | '\\' '\r'? '\n'
    | DecimalEscape
    | HexEscape
    | UtfEscape
    ;

fragment
DecimalEscape
    : '\\' Digit
    | '\\' Digit Digit
    | '\\' [0-2] Digit Digit
    ;

fragment
HexEscape
    : '\\' 'x' HexDigit HexDigit
    ;

fragment
UtfEscape
    : '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN)
    ;

LINE_COMMENT
    : '//'
    (
    | ~('\r'|'\n') ~('\r'|'\n')*
    ) ('\r\n'|'\r'|'\n'|EOF)
    -> channel(HIDDEN)
    ;

WS
    : [ \t\u000C\r\n]+ -> skip
    ;