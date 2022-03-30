@import org.ethelred.techtest.test2.*

@param Page page

@template.tag.layout(page = page, content = @`
    <p>${page.getDescription()}</p>
`)