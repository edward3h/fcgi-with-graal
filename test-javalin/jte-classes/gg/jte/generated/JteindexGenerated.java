package gg.jte.generated;
import org.ethelred.techtest.test2.*;
public final class JteindexGenerated {
	public static final String JTE_NAME = "index.jte";
	public static final int[] JTE_LINE_INFO = {0,0,2,2,2,4,4,4,4,5,5,5,6,6,6};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, Page page) {
		jteOutput.writeContent("\n");
		gg.jte.generated.tag.JtelayoutGenerated.render(jteOutput, jteHtmlInterceptor, page, new gg.jte.html.HtmlContent() {
			public void writeTo(gg.jte.html.HtmlTemplateOutput jteOutput) {
				jteOutput.writeContent("\n    <p>");
				jteOutput.setContext("p", null);
				jteOutput.writeUserContent(page.getDescription());
				jteOutput.writeContent("</p>\n");
			}
		});
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		Page page = (Page)params.get("page");
		render(jteOutput, jteHtmlInterceptor, page);
	}
}
