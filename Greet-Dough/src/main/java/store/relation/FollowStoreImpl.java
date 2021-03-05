package store.relation;

import java.util.ArrayList;

public class FollowStoreImpl extends Relation {

    public FollowStoreImpl() {
        super();
    }

    public ArrayList<Integer> getFollowers( int ID ) {
        return super.get(ID);
    }

    public void addFollower( int curUser, int targetUser ) {
        super.add( curUser, targetUser );
    }

    public void removeFollower( int curUser, int targetUser ) {
        super.remove( curUser, targetUser );
    }

    // Deletes user along with all followers
    public boolean deleteUser( int ID ) {
        return super.delete(ID);
    }

}