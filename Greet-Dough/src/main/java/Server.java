import com.google.gson.Gson;
import store.impl.LikeStoreImpl;
import store.impl.PostStoreImpl;
import store.impl.UserStoreImpl;
import store.impl.ImageStoreImpl;
import store.relation.FollowStoreImpl;
import store.relation.PostCommentStoreImpl;
import store.relation.SubStoreImpl;
import store.impl.CommentStoreImpl;
import store.model.*;
import utility.IOservice;
import database.Handler;

import static spark.Spark.*;

public class Server {

    ////////////////// Members //////////////////
    private static UserStore userStore = new UserStoreImpl();
    private static PostStore postStore = new PostStoreImpl();
    private static ImageStore imageStore = new ImageStoreImpl();
    private static LikeStore likeStore = new LikeStoreImpl();
    private static CommentStore commentStore = new CommentStoreImpl();
    private static SubStore subStore = new SubStoreImpl();
    private static FollowStore followStore = new FollowStoreImpl();
    private static PostCommentStore postCommentStore = new PostCommentStoreImpl();

    private static Gson gson = new Gson();

    public static void main(String[] args) {

        initExceptionHandler((e) -> {
            System.out.println("Could not start server on port 9999");
            System.exit(100);
        });
        port(9999);
        init();

        try {
            Server.userStore = (UserStoreImpl) IOservice.loadObject("data/users.txt");
        } catch (ClassCastException e) {
            System.out.println("(User load) Empty file or wrong class cast");
        }

        try {
            Server.postStore = (PostStoreImpl) IOservice.loadObject("data/posts.txt");
        } catch (ClassCastException e) {
            System.out.println("(Post load) Empty file or wrong class cast");
        }

        try {
            Server.subStore = (SubStore) IOservice.loadObject("data/subs.txt");
        } catch (ClassCastException e) {
            System.out.println("(Sub load) Empty file or wrong class cast");
        }
        try {
            Server.followStore = (FollowStore) IOservice.loadObject("data/follows.txt");
        } catch (ClassCastException e) {
            System.out.println("(Follow load) Empty file or wrong class cast");
        }
        try {
            Server.commentStore = (CommentStore) IOservice.loadObject(("data/comment.txt");
        } catch (ClassCastException e) {
            System.out.println("(Follow load) Empty file or wrong class cast");
        }

        Handler handler = new Handler(
                Server.userStore,
                Server.postStore,
                Server.imageStore,
                Server.likeStore,
                Server.commentStore,
                Server.subStore,
                Server.followStore,
                Server.postCommentStore );

        // USER ROUTES
        /////////////////

        // Returns user given an id
        get("/users/:id/", (req, res) -> handler.getUser(req), gson::toJson);

        // Creates a new user
        // curl -d "name=Tony" -X post localhost:9999/users/
        post("/users/", (req, res) -> handler.createUser(req) );

        // Update the user. Needs a lot of options.
        put("/users/:id/", (req, res) -> {

            int id = Integer.parseInt(req.params(":id"));
            return "Updating a user: " + id;

        });

        // Deletes user given UserID
        delete("/users/:id/", (req, res) -> handler.deleteUser(req));
        
        // USER RELATION ROUTES
        ///////////////////////

            // curl -d "curUser=2" -X post localhost:9999/users/0/subscribe/
            post( "/users/:id/subscribe/", (req,res) -> handler.subscribe(req), gson::toJson );

            // curl -d "curUser=2" -X post localhost:9999/users/0/unsubscribe/
            post( "/users/:id/unsubscribe/", (req,res) -> handler.unsubscribe(req), gson::toJson );

            // curl -d "curUser=2" -X post localhost:9999/users/0/follow/
            post( "/users/:id/follow/", (req,res) -> handler.follow(req), gson::toJson );

            // curl -d "curUser=2" -X post localhost:9999/users/0/unfollow/
            post( "/users/:id/unfollow/", (req,res) -> handler.unfollow(req), gson::toJson );

        // POST ROUTES
        ////////////////////

        //  Returns post object
        // curl localhost:9999/posts/0/
        get("/posts/:id/", (req, res) -> handler.getPost(req), gson::toJson);

        //  Creates a new post
        // curl -d "userID=0&contents=hello world" -X post localhost:9999/posts/
        post("/posts/", (req, res) -> handler.createPost(req));

        //  Deletes post given postID
        // curl -X delete localhost:9999/posts/0
        delete("/posts/:id/", (req, res) -> handler.deletePost(req));

        // returns feed if user is subscribed.
        // curl -d "curUser=2" -X post localhost:9999/users/0/feed/
        post( "/users/:id/feed/", (req,res) -> handler.getFeed(req), gson::toJson );

        // Comment, post for now, put request since we are updating something about the post??
        // curl -d "curUser=0&postID=0&commentContent=chata" -X post localhost:9999/posts/
        post("/posts/:id/", (req, res) -> handler.createComment(req), gson::toJson);

        // Like, put request
        // curl -d "postID=0&curUser=0" -X put localhost:9999/posts/
        put("/posts/:id/", (req, res) -> handler.tryLike(req), gson::toJson);

        // Upload Image, which is createPost but imageID exists
        // curl -d "userID=0&contents=hello world&imageID=0" -X post localhost:9999/posts/
        // uploadImage() will prompt user for a path

    }
}