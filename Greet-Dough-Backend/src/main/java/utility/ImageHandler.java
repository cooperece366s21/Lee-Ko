package utility;

import org.jdbi.v3.core.Jdbi;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Random;

public class ImageHandler {

    private final Path imageDir;
    private final Random filenameGen;
    private static final int MAX_FILENAME_SIZE = 10;
    private static final FileSystem fileSys = FileSystems.getDefault();

    public ImageHandler() {

        this.imageDir = setImageDir();
        this.filenameGen = new Random();

    }

    public Path setImageDir() {

        Path tempPath = fileSys.getPath( System.getProperty("user.dir") );

        // Stores in Greet-Dough-Backend/data/images
        Path newPath = fileSys.getPath( tempPath.toString() + File.separator + "data" + File.separator + "images" );

        return newPath;

    }

    // From https://stackoverflow.com/a/21974043
    public String getFileExtension( String filename ) {

        String extension = "";

        int i = filename.lastIndexOf('.');
        int p = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));

        if (i > p) {
            extension = filename.substring(i);
        }

        return extension;

    }

    public String copyImage( String path ) {

        Path srcPath = fileSys.getPath(path);
        String extension = getFileExtension(path);

        // Generate a random alphanumeric filename
        //      Characters from '0' to 'z'
        // From https://www.baeldung.com/java-random-string
        String filename = filenameGen.ints(48,122+1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit( MAX_FILENAME_SIZE )
                .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                .toString();

        // Writes to imageDir/RANDOM_NAME
        Path destPath = fileSys.getPath( imageDir.toString() + File.separator + filename + extension );

        // Attempt to save the image
        try {

            // Creates the file to write to
            new File( destPath.toString() ).createNewFile();

            // Copies the file
            Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return destPath.toString();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return destPath.toString();

    }

}
