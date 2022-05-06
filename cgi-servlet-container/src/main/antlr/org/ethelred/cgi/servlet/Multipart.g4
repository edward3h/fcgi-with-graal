grammar Multipart;
import HttpLexer, HttpCommon, ContentType;

@lexer::members {
    private String boundary;

    public void setBoundary(String text) {
        boundary = text;
    }

    public boolean isBoundary(String text) {
        if (boundary.equals(text)) {
            System.err.println("boundary match");
            return true;
        }
        return false;
    }
}

multipart: part* endBoundary;

part: boundary header* CRLF data CRLF;

header: header_name Colon OWS* header_value OWS* CRLF ;

header_name: token;

header_value: .*?;

boundary: '--' BOUNDARY CRLF;

endBoundary: '--' BOUNDARY '--' CRLF?;

BOUNDARY: (ALPHA | DIGIT | Minus)+? {isBoundary(getText())}?;

data: .*?;

CRLF: '\r'? '\n';
