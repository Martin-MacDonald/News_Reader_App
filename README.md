# News_Reader_App

Android app built on Android Studio that brings in news from hacker news and allows you to browse each article.

How the app works:
- Once opened the app calls https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty
- The JSON data is parsed to create a list of article names and associated url's
- A listview is created with the top 50 articles
- It also saves them to the SQLite database which and instead of making the call every time it opens it saves the 50 articles
- When you click on the article it opens up a webview useing the article url

I will improve by adding a refresh option which will clear the SQL database and bring in new articles.
An indication that the data is older than n number of days could be good too.


