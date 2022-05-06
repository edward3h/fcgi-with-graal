@import org.ethelred.techtest.test2.*
@import java.util.List

@param Page page
@param Thing thing

@template.tag.layout(page = page, content = @`
<dl id="view" style="background-color: ${thing.colour()};">
    <dt>ID</dt>
    <dd>${thing.id()}</dd>
    <dt>Name</dt>
    <dd>${thing.name()}</dd>
    <dd><button onclick="showForm();">Edit</button></dd>
</dl>
<dl id="edit" style="display: none;">
    <form id="editform" method="put" action="/things/${thing.id()}" onsubmit="updateThing(event); return false;">
        <dt>ID</dt>
        <dd>${thing.id()}</dd>
        <dt>Name</dt>
        <dd><input type="text" name="name" value="${thing.name()}"></dd>
        <dt>Colour</dt>
        <dd><input type="color" name="colour" value="${thing.colour()}"></dd>
        <dd><input type="submit" value="Save"></dd>
    </form>
</dl>
`)