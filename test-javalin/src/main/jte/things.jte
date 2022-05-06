@import org.ethelred.techtest.test2.*
@import java.util.List

@param Page page
@param List<Thing> things

@template.tag.layout(page = page, content = @`
<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Colour</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    @for(var thing: things)
        <tr>
            <td>${thing.id()}</td>
            <td><a href="/things/${thing.id()}">${thing.name()}</a></td>
            <td style="background-color: ${thing.colour()}">${thing.colour()}</td>
            <td>
                <button onclick="deleteThing(${thing.id()});">Delete</button>
            </td>
        </tr>
    @endfor
    <tr>
        <form action="/things" method="post">
            <td></td>
            <td><input type="text" name="name"></td>
            <td><input type="color" name="colour"></td>
            <td><input type="submit" value="Create"></td>
        </form>
    </tr>
    </tbody>
</table>
`)