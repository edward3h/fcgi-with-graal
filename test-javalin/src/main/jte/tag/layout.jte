@import gg.jte.Content
@import org.ethelred.techtest.test2.*

@param Page page
@param Content content

<html>
<head>
    @if(page.getDescription() != null)
        <meta name="description" content="${page.getDescription()}">
    @endif
    <link rel="stylesheet" href="/styles.css">
    <title>${page.getTitle()}</title>
</head>
<body>
<div class="navbar">
</div>
<h1>${page.getTitle()}</h1>
<div class="content">
    ${content}
</div>
<script src="/support.js"></script>
</body>
</html>