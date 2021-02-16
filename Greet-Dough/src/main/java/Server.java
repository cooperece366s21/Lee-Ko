import com.fasterxml.jackson.databind.ObjectMapper;

import static spark.Spark.*;
import java.io.*;
import java.util.HashMap;

public class Server {

    ////////////////// File Paths //////////////////
    private static final String PATH_TO_USER = "/users/";
    private static final String PATH_TO_USER_ID = PATH_TO_USER + ":id";
//    private static final String PATH_TO_POST = "/posts/";
//    private static final String PATH_TO_POST_ID = PATH_TO_POST + ":id";

    ////////////////// Members //////////////////
    private static ObjectMapper mapper = new ObjectMapper();
    private static UtilityID recordID = new UtilityID();
    private static HashMap<Integer, User> userHash = new HashMap<>();
    private static HashMap<Integer, Post> postHash = new HashMap<>();

    ////////////////// Functions //////////////////
    public static User getUser( int ID ) {
        return userHash.get(ID);
    }

    public static Post getPost( int ID ) {
        return postHash.get(ID);
    }

    public static void addUser( User newUser ) { userHash.put( newUser.getID(), newUser ); }

    public static void addPost( Post newPost ) { postHash.put( newPost.getID(), newPost ); }

    // Removes a given ID from a given map
    public static <T> boolean removeFromMap( HashMap<Integer, T> map, int ID ) {

        if ( map.containsKey(ID) ) {

            map.remove(ID);
            return true;

        } else {
            return false;
        }

    }

    // Returns true if successful;
    //         false otherwise.
    public static boolean removeUser( int ID ) {

        if ( Server.removeFromMap( Server.userHash, ID ) ) {

            Server.addUnusedUserID(ID); // This user's ID is now unused, so add to stack
            return true;

        } else {
            return false;
        }

    }

    // Returns true if successful;
    //         false otherwise.
    public static boolean removePost( int ID ) {

        if ( Server.removeFromMap( Server.postHash, ID ) ) {

            Server.addUnusedPostID(ID); // This post's ID is now unused, so add to stack
            return true;

        } else {
            return false;
        }

    }

    public static int getUnusedUserID() {
        return recordID.getUnusedUserID();
    }

    public static int getUnusedPostID() {
        return recordID.getUnusedPostID();
    }

    public static int getUnusedImageID() { return recordID.getUnusedImageID(); }

    public static void addUnusedUserID( int ID ) {
        recordID.addUnusedUserID(ID);
    }

    public static void addUnusedPostID( int ID ) {
        recordID.addUnusedPostID(ID);
    }

    public static void addUnusedImageID( int ID ) { recordID.addUnusedImageID(ID); }

    /////////// NEED TO SAVE STACKS BEFORE SERVER SHUTDOWN
    public static void main(String[] args) {

        initExceptionHandler((e) -> {
            System.out.println("Could not start server on port 9999");
            System.exit(100);
        });
        port(9999);
        init();

        // Load the saved stack and users
        recordID = (UtilityID) Server.loadObject( "data/stack.txt");
        userHash = (HashMap<Integer, User>) Server.loadObject( "data/users.txt" );

        // you can send requests with curls.
        // curl -X POST localhost:9999/users/*id*

        // USER ROUTES
        // Returns user given an id
        get( Server.PATH_TO_USER_ID, (req, res) -> {

            int id = Integer.parseInt( req.params(":id") );
            return userHash.get(id);

        });

        // Creates a new user into database or wherever
        post( Server.PATH_TO_USER, (req, res) -> {

            // curl -d "name=Tony Belladonna" -X post localhost:9999/users/

            String name = req.queryParams("name");
            User tempUser = new User(name);
            System.out.println( "Creating a user: " + tempUser.getName() + ", " + tempUser.getID() );

            // Save target user to server
            Server.addUser( tempUser );
            saveObject(userHash, "data/users.txt");
            saveObject(recordID, "data/stack.txt");

            return Server.mapper.writeValueAsString(userHash);

        });

        // Update the user. Needs a lot of options.
        put( Server.PATH_TO_USER_ID, (req,res) -> {

            int id = Integer.parseInt( req.params(":id") );
            User tempUser = Server.getUser(id);
            System.out.println( tempUser.getFollowers().size() );
            tempUser.subscribe(0);
            System.out.println( tempUser.getFollowers().size() );

            return "Updating a user: " + id;

        });

        // Deletes user
        delete( Server.PATH_TO_USER_ID, (req,res) -> {

            int ID = Integer.parseInt( req.params(":id") );

            // Check if the ID is valid
            assert ID >= 0: "Invalid ID.";
            assert userHash.containsKey(ID): "User does not exist.";

            // Delete target user dependencies
            User targetUser = userHash.get(ID);
            targetUser.deleteUser();

            // Remove target user from server
            Server.removeUser(ID);
            saveObject(userHash, "data/users.txt");
            saveObject(recordID, "data/stack.txt");

            System.out.println( "Deleted a user: " + targetUser.getName() + ", " + targetUser.getID() );

            return Server.mapper.writeValueAsString(userHash);

        });

        // POST ROUTES

        //  Creates a new post. Data query must include the ID of the user who is posting.
        //  Updates the user's feed to include the postID.

//        post(Server.PATH_TO_POST, (req, res) -> {
//            int userID = Integer.parseInt( req.queryParams("userID") );
//            Post tempPost = new Post( req.queryParams("contents") );
//            User tempUser = userHash.get( userID );
//
//            postHash.put(tempPost.getID(), tempPost);
//            // ** update tempUser to contain new post in feed here.
//            // Need to change the feed data structure s.t it is a list of post ID's rather than content
//            userHash.put( userID, tempUser );
//
//
//            save(userHash, "data/users.txt");
//            save(recordID, "data/stack.txt");
//            return Server.mapper.writeValueAsString(tempPost);
//
//
//        });

    }

    // IO Helper Functions
    private static Integer saveObject( Object objToSave, String fileName ){

        try {

            FileOutputStream fo = new FileOutputStream(new File(fileName), false);
            ObjectOutputStream oo = new ObjectOutputStream(fo);

            oo.writeObject( objToSave );
            oo.flush();
            oo.close();
            fo.close();

        } catch ( Exception ex ) {
            ex.printStackTrace();
            return -1;
        }

        return 0;

    }

    // Generalized Load Function. Requires casting on call.
    private static Object loadObject( String fileName ){

        Object objToLoad = new Object();

        try {

            FileInputStream fi = new FileInputStream( new File( fileName ) );
            ObjectInputStream oi = new ObjectInputStream(fi);

            if( fi.available() != 0 ) {
                objToLoad = oi.readObject();
                oi.close();
                fi.close();
            }

        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        return objToLoad;
    }

}