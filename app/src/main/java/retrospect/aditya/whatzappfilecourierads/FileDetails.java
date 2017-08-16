package retrospect.aditya.whatzappfilecourierads;

import java.io.Serializable;

class FileDetails implements Serializable {

    static final long serialVersionUID =-6328540275909407684L;

    private String fileName;
    private String fileSize;
    private String totalFileSize;
    private String extension;
    private int totalParts;
    private int currentPart;


    public FileDetails(String fileName, String fileSize, String totalFileSize, String extension, int totalParts, int currentPart) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.totalFileSize = totalFileSize;
        this.extension = extension;
        this.totalParts = totalParts;
        this.currentPart = currentPart;


    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getTotalFileSize() {
        return totalFileSize;
    }

    public String getExtension() {
        return extension;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public int getCurrentPart() {
        return currentPart;
    }

}