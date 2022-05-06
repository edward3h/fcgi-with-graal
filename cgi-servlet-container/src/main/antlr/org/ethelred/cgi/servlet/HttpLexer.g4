lexer grammar HttpLexer;

OBS_TEXT: '\u0080' ..'\u00ff';

OWS: SP | HTAB;

SP: ' ';

HTAB: '\t';

ALPHA: [A-Za-z];

DIGIT: [0-9];
Minus :'-';
Dot   : '.';
Underscore: '_';
Tilde : '~';
QuestionMark :'?';
Slash :'/';
ExclamationMark: '!';
Colon:':';
At: '@';
DollarSign:'$';
Hashtag:'#';
Ampersand:'&';
Percent:'%';
SQuote:'\'';
Star:'*';
Plus:'+';
Caret:'^';
BackQuote:'`';
VBar:'|';

LColumn:'(';
RColumn:')';
SemiColon:';';
Equals:'=';
Period:',';

VCHAR:
	ExclamationMark
	| '"'
	| Hashtag
	| DollarSign
	| Percent
	| Ampersand
	| SQuote
	| LColumn
	| RColumn
	| Star
	| Plus
	| Period
	| Minus
	| Dot
	| Slash
	| Colon
	| SemiColon
	| '<'
	| Equals
	| '>'
	| QuestionMark
	| At
	| '['
	| '\\'
	| Caret
	| Underscore
	| ']'
	| BackQuote
	| '{'
	| '}'
	| VBar
	| Tilde;

QDTEXT: OWS | OBS_TEXT | '\u0021' | '\u0023' ..'\u005B' | '\u005D' ..'\u007E';
