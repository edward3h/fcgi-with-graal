package gg.jte.generated.tag;
import gg.jte.Content;
import org.ethelred.techtest.test2.*;
public final class JtelayoutGenerated {
	public static final String JTE_NAME = "tag/layout.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,3,3,3,8,8,8,9,9,9,9,9,9,9,10,10,11,11,11,16,16,16,18,18,18,21};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Page page, Content content) {
		jteOutput.writeContent("\n<html>\n<head>\n    ");
		if (page.getDescription() != null) {
			jteOutput.writeContent("\n        <meta name=\"description\"");
			if (gg.jte.runtime.TemplateUtils.isAttributeRendered(page.getDescription())) {
				jteOutput.writeContent(" content=\"");
				jteOutput.setContext("meta", "content");
				jteOutput.writeUserContent(page.getDescription());
				jteOutput.writeContent("\"");
			}
			jteOutput.writeContent(">\n    ");
		}
		jteOutput.writeContent("\n    <title>");
		jteOutput.setContext("title", null);
		jteOutput.writeUserContent(page.getTitle());
		jteOutput.writeContent("</title>\n</head>\n<body>\n<div class=\"navbar\">\n</div>\n<h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(page.getTitle());
		jteOutput.writeContent("</h1>\n<div class=\"content\">\n    ");
		jteOutput.setContext("div", null);
		jteOutput.writeUserContent(content);
		jteOutput.writeContent("\n</div>\n</body>\n</html>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Page page = (Page)params.get("page");
		Content content = (Content)params.get("content");
		render(jteOutput, jteHtmlInterceptor, page, content);
	}
}
