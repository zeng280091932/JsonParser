package com.beauney.jsonparser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.beauney.library.jsonparser.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void parse(View view) {
        News news = new News();
        news.setId(12);
        news.setTitle("新年放假通知");
        news.setContent("从今天开始放假啦。");
        news.setAuthor(new User("Beckham", "asdfdfd", true));
        List<User> users = new ArrayList<>();
        users.add(new User("张三", "123456", true));
        users.add(new User("李四", "654321", false));
        users.add(new User("王五", "567890", true));
        news.setReader(users);
        String json = JsonParser.toJson(news);
        Log.d("Debug", json);

        News newNews = (News) JsonParser.parseObject(json, News.class);
        Log.d("Debug", "newNews------>" + newNews);
    }
}
