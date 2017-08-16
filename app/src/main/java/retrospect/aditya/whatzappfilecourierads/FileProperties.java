package retrospect.aditya.whatzappfilecourierads;

/**
 * Created by Aditya on 06-02-2015.
 */
public class FileProperties {

    private String name;
    private String extension;
    private String size;
    private boolean isFile;
    private int numFilesInFolder;
    private String lastModified;

    public FileProperties(String name, String extension, String size, boolean isFile, int numFilesInFolder, String lastModified) {
        this.name = name;
        this.size = size;
        this.isFile = isFile;
        this.extension = extension;
        this.numFilesInFolder = numFilesInFolder;
        this.lastModified = lastModified;

    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getSize() {
        return size;
    }

    public boolean getIsFile() {
        return isFile;
    }

    public int getnumFilesInFolder() {
        return numFilesInFolder;
    }

    public String getLastModified() {
        return lastModified;
    }

}
