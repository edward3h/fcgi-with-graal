grammar HttpCommon;
import HttpLexer;

parameter_value: token | quoted_string;

quoted_string: '"' (ALPHA | DIGIT | QDTEXT | quoted_pair)* '"';

quoted_pair: '\\' (SP | HTAB | VCHAR | OBS_TEXT);

token: tchar+;

tchar:
	  ExclamationMark
	| DollarSign
	| Hashtag
	| Percent
	| Ampersand
	| SQuote
	| Star
	| Plus
    | Minus
	| Dot
	| Caret
    | Underscore
	| BackQuote
	| VBar
	| Tilde
	| DIGIT
	| ALPHA;