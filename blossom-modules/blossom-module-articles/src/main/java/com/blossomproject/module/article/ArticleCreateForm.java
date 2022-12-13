package com.blossomproject.module.article;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArticleCreateForm {

    @NotBlank(message = "{articles.article.validation.name.NotBlank.message}")
    @Size(max=100,message="{articles.article.validation.name.Size.message}")
    private String name = "";

    private String summary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
