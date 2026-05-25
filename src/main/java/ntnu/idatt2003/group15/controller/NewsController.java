package ntnu.idatt2003.group15.controller;

import java.util.Objects;
import javafx.collections.ObservableList;
import ntnu.idatt2003.group15.model.*;
import ntnu.idatt2003.group15.model.news.NewsArchive;
import ntnu.idatt2003.group15.model.news.NewsItem;

public class NewsController {

    NewsArchive newsArchive;

    public NewsController(NewsArchive newsArchive) {

        this.newsArchive = Objects.requireNonNull(newsArchive);
    }

    public ObservableList<NewsItem> getNewsObservable() {
        return newsArchive.getActiveNewsItems();
    }

    public void advanceWeek() {
        newsArchive.advance();
    }

    public void publish(double maxEventChance) {
        newsArchive.publishNews(maxEventChance);
    }

    public void reset() {
        newsArchive.reset();
    }
}