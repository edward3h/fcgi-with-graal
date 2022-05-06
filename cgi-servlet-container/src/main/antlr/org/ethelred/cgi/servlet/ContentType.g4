grammar ContentType;
import HttpLexer, HttpCommon;

content_type: OWS* mimetype (OWS* ';' OWS* content_type_parameter)*;

mimetype : type '/' subtype ;

content_type_parameter: content_type_parameter_name Equals parameter_value;

subtype: token;

type: token;

content_type_parameter_name: 'charset' | 'boundary';


