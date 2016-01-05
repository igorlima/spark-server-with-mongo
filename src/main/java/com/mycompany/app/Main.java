package com.mycompany.app;

import static spark.Spark.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        MongoClientURI uri = new MongoClientURI("mongodb://example:example@candidate.54.mongolayer.com:10775,candidate.57.mongolayer.com:10128/Todo?replicaSet=set-5647f7c9cd9e2855e00007fb");
        MongoClient mongoClient = new MongoClient(uri);
        DB db = mongoClient.getDB( "Todo" );
        DBCollection collection = db.getCollection("todos");

        get("/hello", (req, res) -> {
            List<DBObject> todos = new ArrayList<DBObject>();
            try (DBCursor cursor = collection.find()) {
                while(cursor.hasNext()) {
                  todos.add(cursor.next());
                }
            }
            return todos;
        });

        get("/hi", (req, res) -> {
            DBObject myDoc = collection.findOne();
            return myDoc;
        })
    }
}
