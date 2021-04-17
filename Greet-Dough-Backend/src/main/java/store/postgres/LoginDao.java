package store.postgres;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface LoginDao {

    @SqlUpdate("DROP TABLE IF EXISTS login;")
    void deleteTable();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS login( " +
            "user_token TEXT " +        "NOT NULL, " +
            "user_id INT " +            "NOT NULL, " +
            "timestamp timestamp " +    "NOT NULL " + "DEFAULT NOW(), " +
            "PRIMARY KEY(user_token), " +
            "CONSTRAINT fk_user " + "FOREIGN KEY(user_id) " +
                "REFERENCES users(user_id) " + "ON DELETE CASCADE " +
            ");")
    void createTable();

    @SqlUpdate("INSERT INTO login (user_token, user_id) " +
            "VALUES (MD5(random()::text), :user_id);")
    @GetGeneratedKeys("user_token")
    String insertSession(@Bind("user_id") int user_id);

    @SqlUpdate("DELETE FROM login " +
            "WHERE user_token = (:user_token);")
    void deleteSession(@Bind("user_token") String user_token);

    @SqlQuery("SELECT user_id FROM login " +
            "WHERE user_token = (:user_token) AND " +
                    "timestamp > NOW() - INTERVAL '1 hour';")
    Optional<Integer> getUserID(@Bind("user_token") String user_token );

    // Invalidates tokens after a specified amount of time
    //      Takes place before an INSERT
    // From https://www.the-art-of-web.com/sql/trigger-delete-old/
    @SqlUpdate( "CREATE OR REPLACE FUNCTION delete_old_rows() RETURNS trigger " +
                    "LANGUAGE plpgsql " +
                    "AS $$ " +
                "DECLARE " +
                    "row_count int; " +
                "BEGIN " +
                    "DELETE FROM login WHERE timestamp < NOW() - INTERVAL '1 hour'; " +
                    "IF found THEN " +
                        "GET DIAGNOSTICS row_count = ROW_COUNT; " +
                    "RAISE NOTICE 'DELETE % row(s) FROM login', row_count; " +
                    "END IF; " +
                    "RETURN NULL; " +
                "END; " +
                "$$; ")
    void createTrigger();

    @SqlUpdate("CREATE TRIGGER trigger_delete_old_rows " +
                    "BEFORE INSERT ON login " +
                    "EXECUTE PROCEDURE delete_old_rows();")
    void setTrigger();



}
