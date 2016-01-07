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
        MongoClientURI uri = new MongoClientURI("mongodb://example:example@candidate.54.mongolayer.com:10775,candidate.57.mongolayer.com:10128/spark-server-with-mongo?replicaSet=set-5647f7c9cd9e2855e00007fb");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase db = mongoClient.getDatabase("spark-server-with-mongo");
        MongoCollection<Document> collection = db.getCollection("todos");

        String ENV_PORT = System.getenv().get("PORT");
        port( ENV_PORT == null ? 4567 : Integer.parseInt(ENV_PORT) );

        /*
         * enable CORS in our Spark server. CORS is the acronym for “Cross-origin resource sharing”: a mechanism that allows to access REST resources outside the original domain of the request.
         * http://www.mastertheboss.com/cool-stuff/create-a-rest-services-layer-with-spark
         */
        options("/*", (request,response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if(accessControlRequestMethod != null){
            response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request,response) -> {
            response.header("Access-Control-Allow-Origin", "*");
        });

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

        put("/todos/toggleAll", (request, response) -> {
            return "Should be implemented";
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

        delete("/todos/clearCompleted", (request, response) -> {
            return "Should be implemented";
        });

        delete("/todos/:id", (request, response) -> {
            String id = request.params(":id");
            return collection.deleteOne(new Document("_id", new ObjectId(id)));
        });

    }
}
