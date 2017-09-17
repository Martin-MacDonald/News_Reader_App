package com.example.martin.newsreader;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final String NEW_NEWS_API = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
    final int NO_OF_ARTICLES = 50;
    ArrayList<Integer> newsIDList = new ArrayList<>();
    ArrayList<String> newsTitleList = new ArrayList<>();
    ArrayList<String> newsURLList = new ArrayList<>();
    ArrayAdapter<String> newsTitleAdapter;
    ListView articleListView;
    SQLiteDatabase newsDB;

    private class GetNewsArticle extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1){

                    char content = (char) data;
                    result += content;
                    data = reader.read();

                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject newsArticle = new JSONObject(s);
                String article = newsArticle.getString("title");
                String url = newsArticle.getString("url");
                newsTitleList.add(article);
                newsURLList.add(url);
                newsTitleAdapter.notifyDataSetChanged();
                SQLiteStatement stmt = newsDB.compileStatement("INSERT INTO news (title, url) VALUES ( ?, ?)");
                stmt.bindString(1, article);
                stmt.bindString(2, url);
                stmt.execute();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private class GetNewNews extends AsyncTask<String, Void, String>{


        @Override
        protected String doInBackground(String... strings) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1){

                    char content = (char) data;
                    result += content;
                    data = reader.read();

                }

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONArray newsID = new JSONArray(s);
                for (int i = 0; i < NO_OF_ARTICLES; i++){
                    newsIDList.add(newsID.getInt(i));
                }

                for (int i = 0; i < newsIDList.size(); i++){
                    GetNewsArticle findNewsArticle = new GetNewsArticle();
                    findNewsArticle.execute("https://hacker-news.firebaseio.com/v0/item/" + Integer.toString(newsIDList.get(i)) + ".json?print=pretty");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            newsDB = this.openOrCreateDatabase("News", MODE_PRIVATE, null);
            newsDB.execSQL("CREATE TABLE IF NOT EXISTS news (title VARCHAR, url VARCHAR)");

            Cursor c = newsDB.rawQuery("SELECT * FROM news", null);

            if (c.getCount() > 0){
                int titleIndex = c.getColumnIndex("title");
                int urlIndex = c.getColumnIndex("url");

                c.moveToFirst();

                while (c != null) {
                    newsTitleList.add(c.getString(titleIndex));
                    newsURLList.add(c.getString(urlIndex));
                    c.moveToNext();
                }
            } else {
                GetNewNews findNewsID = new GetNewNews();
                findNewsID.execute(NEW_NEWS_API);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        articleListView = (ListView) findViewById(R.id.articleListView);
        newsTitleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, newsTitleList);
        articleListView.setAdapter(newsTitleAdapter);

        articleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent showWebPage = new Intent(getApplicationContext(), WebViewActivity.class);
                showWebPage.putExtra("URL", newsURLList.get(i));
                startActivity(showWebPage);
            }
        });

    }
}
