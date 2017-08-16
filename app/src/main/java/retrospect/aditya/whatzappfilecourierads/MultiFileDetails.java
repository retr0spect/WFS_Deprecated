package retrospect.aditya.whatzappfilecourierads;

/**
 * Created by Aditya on 13-02-2015.
 */
public class MultiFileDetails {

    String fileName;
    long fileSize;

    public MultiFileDetails(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

}
