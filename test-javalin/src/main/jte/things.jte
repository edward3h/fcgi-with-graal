@import org.ethelred.techtest.test2.*
@import java.util.List

@param Page page
@param List<Thing> things

@tag.layout(page = page, content = @`
    <table>
        <thead>
            <tr><th>ID</th><th>Name</th><th>Colour</th></tr>
        </thead>
        <tbody>
    @for(var thing: things)
        <tr><td>${thing.id()}</td><td>${thing.name()}</td><td>${thing.colour()}</td></tr>
    @endfor
        </tbody>
    </table>
`)