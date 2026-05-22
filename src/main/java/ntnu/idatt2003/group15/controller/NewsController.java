package ntnu.idatt2003.group15.controller;

import java.util.Objects;
import java.util.Random;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.model.news.NewsArchive;
import ntnu.idatt2003.group15.model.news.NewsItem;

public class NewsController {

    NewsArchive newsArchive;
    private final Random random = new Random();

    public NewsController(StackPane root, GameSettings settings, NewsArchive newsArchive) {

        this.newsArchive = Objects.requireNonNull(newsArchive);
    }

    public void addNewsItem(NewsItem item) {
        newsArchive.addNewItem(item);
    }


    public ObservableList<NewsItem> getNewsObservable() {
        return newsArchive.getActiveNewsItems();
    }

    public void advanceWeek() {
        newsArchive.advance();
    }

    public void publish() {
        newsArchive.publishNews();
    }
}