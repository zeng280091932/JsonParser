package com.beauney.jsonparser;

import java.util.List;

/**
 * @author zengjiantao
 * @since 2020-08-18
 */
public class News {
    private int id;
    private String title;
    private String content;
    private User author;
    private List<User> reader;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<User> getReader() {
        return reader;
    }

    public void setReader(List<User> reader) {
        this.reader = reader;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", author=" + author +
                ", reader=" + reader +
                '}';
    }
}
