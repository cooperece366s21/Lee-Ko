package store.model;

import model.Comment;
import model.Image;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentStoreImpl extends Store<Comment> {

    public CommentStoreImpl() {
        super();
    }

    public CommentStoreImpl( int start ) {
        super(start);
    }

    public Comment getPostID( int ID ) {
        return super.get(ID);
    }

    public void addComment(String commentContent, int currentUser, Comment newComment) {
        newComment.commentTime = LocalDateTime.now();
        List<String> value = new ArrayList<String>();
        value.add(Integer.toString(currentUser));
        value.add(commentContent);
        newComment.comment.put(newComment.commentTime, value);
    }

}