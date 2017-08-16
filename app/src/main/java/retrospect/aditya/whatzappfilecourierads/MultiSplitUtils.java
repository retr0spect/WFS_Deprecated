package retrospect.aditya.whatzappfilecourierads;

import android.os.Environment;
import android.util.Log;

import com.beaglebuddy.mp3.MP3;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by Aditya on 09-02-2015.
 */
public class MultiSplitUtils {

    public static ArrayList<String> split(String inputFilePath, String outputFolderPath) throws IOException {

        ArrayList<String> filePathList = new ArrayList<>();
        RandomAccessFile raf = new RandomAccessFile(new File(inputFilePath), "r");
        long totalFileLength = raf.length();
        long partSize = 1024 * 1024 * (long)15.8;
        long numSplits = totalFileLength / partSize;
        long remainingBytes = totalFileLength % partSize;

        int maxReadBufferSize = 8 * 1024; // 8KB
        int destIx;
        for (destIx = 1; destIx <= numSplits; destIx++) {
            String filePath = outputFolderPath + (destIx + 100);
            filePathList.add(filePath);

            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(filePath));
            if (partSize > maxReadBufferSize) {
                long numReads = partSize / maxReadBufferSize;
                long numRemainingRead = partSize % maxReadBufferSize;
                for (int i = 0; i < numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, partSize);
            }
            bw.close();
        }
        if (remainingBytes > 0) {
            String filePath = outputFolderPath + (destIx + 100);
            filePathList.add(filePath);
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(filePath));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();

        return joinSplitsAndMp3(filePathList, inputFilePath);
    }

    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }


    static ArrayList<String> joinSplitsAndMp3(ArrayList<String> splitFileList, String originalFilePath) {

        ArrayList<String> mp3PlusDataPaths = new ArrayList<>();

        String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3";
        String assetMp3Copy = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Temp/tempAssetCopy.mp3";
        final String fileReadyToCourier = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Sent/";

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        dateFormat.format(date);

        File originalFile = new File(originalFilePath);
        String fileName = originalFile.getName();
        String fileSize = String.valueOf(originalFile.length());
        String lastModified = String.valueOf(originalFile.lastModified());
        String fileExtension = FilenameUtils.getExtension(originalFilePath);

        try {
            FileUtils.copyFile(new File(assetMp3), new File(assetMp3Copy));

            MP3 mp3 = new MP3(assetMp3Copy);
            mp3.setBand(fileName);
            mp3.setLyrics(lastModified);
            mp3.setLyricsBy(fileExtension);
            mp3.setMusicBy(String.valueOf(fileSize));
            mp3.setPublisher(String.valueOf(((int) originalFile.length()) / (15 * 1024 * 1024) + 1)); // Set total number of parts
            mp3.save();

            Random random = new Random();

            int counter = 0;
            for (String eachFile : splitFileList) {
                File loopFile = new File(eachFile);
                mp3.setRating(Integer.parseInt(loopFile.getName())); // Set each Split files' current part number
                mp3.setComments(String.valueOf(loopFile.length())); // Set each Split files' size/length
                mp3.save();

                String currentFile = fileReadyToCourier + dateFormat.format(date) + random.nextInt(1000) + counter + ".mp3";
                mp3PlusDataPaths.add(currentFile);
                File file = new File(currentFile);
                IOCopier.joinFiles(file, new File[]{new File(assetMp3Copy), new File(eachFile)});
                counter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Cleanup temporary split data files
        for(String s : splitFileList) {
            new File(s).delete();
        }

        return mp3PlusDataPaths;

    }

    static ArrayList<String> makeSingleDataPlusMP3(String originalFilePath) {

        ArrayList<String> mp3PlusDataPath = new ArrayList<>();

        String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3";
        String assetMp3Copy = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Temp/tempAssetCopy.mp3";
        final String fileReadyToCourier = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Sent/";

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS");
        Date date = new Date();
        dateFormat.format(date);

        File originalFile = new File(originalFilePath);
        String fileName = originalFile.getName();
        String fileSize = String.valueOf(originalFile.length());
        String lastModified = String.valueOf(originalFile.lastModified());
        String fileExtension = FilenameUtils.getExtension(originalFilePath);
        if(fileExtension == null || fileExtension.isEmpty() || fileExtension.equals("")) {
            fileExtension = "empty";
        }
        try {
            FileUtils.copyFile(new File(assetMp3), new File(assetMp3Copy));
            MP3 mp3 = new MP3(assetMp3Copy);
            mp3.setBand(fileName);
            mp3.setLyrics(lastModified);
            mp3.setLyricsBy(fileExtension);
            mp3.setMusicBy(String.valueOf(fileSize));
            mp3.setPublisher(String.valueOf(1)); // Set total number of parts
            mp3.setRating(101); // Set each Split files' current part number
            mp3.setComments(fileSize); // Set each Split files' size/length
            mp3.save();

            Random random = new Random();

            String currentFile = fileReadyToCourier + dateFormat.format(date) + random.nextInt(1000) + ".mp3";
            mp3PlusDataPath.add(currentFile);
            File file = new File(currentFile);
            IOCopier.joinFiles(file, new File[]{new File(assetMp3Copy), originalFile});

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mp3PlusDataPath;
    }


}
