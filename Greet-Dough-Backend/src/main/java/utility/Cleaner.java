package utility;

import org.jdbi.v3.core.Jdbi;
import store.postgres.ImageStorePostgres;
import store.postgres.LoginStorePostgres;

/**
 * The class is used to periodically clean up the database.
 */
public class Cleaner implements Runnable {

    private final ImageStorePostgres imageStorePostgres;
    private final LoginStorePostgres loginStorePostgres;

    public Cleaner() {

        Jdbi jdbi = GreetDoughJdbi.create("jdbc:postgresql://localhost:4321/greetdough");
        imageStorePostgres = new ImageStorePostgres(jdbi);
        loginStorePostgres = new LoginStorePostgres(jdbi);

    }

    @Override
    public void run() {

        imageStorePostgres.clearDeleted();
        loginStorePostgres.clearDeleted();

    }

}
