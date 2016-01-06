package com.mycompany.app;

import static spark.Spark.*;

import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import com.mongodb.util.JSON;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;

public class Main {
    public static void main(String[] args) {
        MongoClientURI uri = new MongoClientURI("mongodb://example:example@candidate.54.mongolayer.com:10775,candidate.57.mongolayer.com:10128/Todo?replicaSet=set-5647f7c9cd9e2855e00007fb");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase db = mongoClient.getDatabase("Todo");
        MongoCollection<Document> collection = db.getCollection("todos");

        String ENV_PORT = System.getenv().get("PORT");
        port( ENV_PORT == null ? 4567 : Integer.parseInt(ENV_PORT) );

        get("/todos", (request, response) -> {
            List<DBObject> todos = new ArrayList<DBObject>();
            FindIterable<Document> iterable = collection.find();
            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    todos.add((DBObject) JSON.parse(document.toJson()));
                }
            });
            return todos;
        });

        get("/todos/:id", (request, response) -> {
            String id = request.params(":id");
            FindIterable<Document> iterable = collection.find(new Document("_id", new ObjectId(id)));
            Document todo = iterable.first();
            if (todo == null) {
                response.status(404);
                return "Resource not found";
            } else {
                return JSON.parse(todo.toJson());
            }
        });

        post("/todos", (request, response) -> {
            DBObject body = (DBObject) JSON.parse(request.body());
            Object title = body.get("title");
            Object completed = body.get("completed");
            Document newTodo = new Document()
                .append("title", title)
                .append("completed", completed == null ? false : completed);

            collection.insertOne(newTodo);
            return newTodo;
        });

        put("/todos/:id", (request, response) -> {
            String id = request.params(":id");
            DBObject body = (DBObject) JSON.parse(request.body());
            Object title = body.get("title");
            Object completed = body.get("completed");
            Document update = new Document();

            if (title != null) {
                update.append("title", title);
            }

            if (completed != null) {
                update.append("completed", completed);
            }

            return collection.updateOne(
                new Document("_id", new ObjectId(id)),
                new Document("$set", update)
            );
        });

        delete("/todos/:id", (request, response) -> {
            String id = request.params(":id");
            return collection.deleteOne(new Document("_id", new ObjectId(id)));
        });
    }
}
