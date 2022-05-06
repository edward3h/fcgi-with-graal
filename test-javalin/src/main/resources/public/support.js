function deleteThing(id) {
    fetch(`/things/${id}`, {
        method: 'DELETE'
    })
    .then(() => window.location.href = '/things');
}

function updateThing(event) {
    event.preventDefault();
    const form = event.currentTarget;
    fetch(form.action, {
        method: 'PUT',
        body: new FormData(form)
    })
    .then(() => window.location.href = '/things');
}

function showForm() {
    const view = document.getElementById("view");
    view.style.setProperty("display", "none");
    const edit = document.getElementById("edit");
    edit.style.removeProperty("display");
}