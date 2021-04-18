package store.model;

import model.Post;

import java.util.List;

public interface PostStore {

    Post getPost( int pid );

    /**
     * @return a list composed of every post made by a user
     */
    List<Post> makeFeed( int uid );

    boolean hasPost( int pid );

    Post addPost( String title, String contents, int uid );

    Post addPost( String title, String contents, int uid, Integer iid );

    void deletePost( int pid );

    void changeTitle( int pid, String newTitle );

    void changeContents( int pid, String newContents );

}
