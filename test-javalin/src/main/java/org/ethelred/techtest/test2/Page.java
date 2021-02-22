package org.ethelred.techtest.test2;

/**
 * TODO
 *
 * @author eharman
 * @since 2021-02-10
 */
public class Page
{
    private final String title;
    private String description;

    public Page(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
