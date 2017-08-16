package retrospect.aditya.whatzappfilecourierads;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beaglebuddy.mp3.MP3;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static retrospect.aditya.whatzappfilecourierads.SendFragment.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

/**
 * Created by Aditya on 06-02-2015.
 */
public class ReceiveFragment extends Fragment {

    private ArrayList<FileDetails> item;
    private List<String> path = null;
    private ListView listView;
    private ProgressDialog pd;

    String extractionPath = Environment.getExternalStorageDirectory() + "/Download/WFS/";
    String receivedAudioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Audio/";

    boolean buttonClicked = false;
    TextView exTractionPathTV;
    CustomAdapter adapter;
    Activity parentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_receive, container, false);

        FloatingActionButton refreshButton = (FloatingActionButton) v.findViewById(R.id.fab_refresh_receive);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                if (item.isEmpty()) {
                    Toast.makeText(parentActivity, R.string.no_files_received, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(parentActivity, R.string.refreshing, Toast.LENGTH_SHORT).show();
                }
                new PopulateListviewAsync().execute();
            }
        });

        listView = (ListView) v.findViewById(R.id.listView2);
        //String sdRootDir = Environment.getExternalStorageDirectory().toString();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(parentActivity).setTitle(R.string.select_action).setPositiveButton(R.string.extract_file, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new ExtractAsyncTask().execute(path.get(position), String.valueOf(position));
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(parentActivity).setTitle(R.string.delete_file).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new DeleteUnextractedAsync().execute(path.get(position), String.valueOf(position));
                        Toast.makeText(parentActivity, R.string.file_deleted, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return true;
            }
        });

        requestPermission();

        return v;
    }



    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ReceiveFragment.this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            new PopulateListviewAsync().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new PopulateListviewAsync().execute();
                } else {
                    Toast.makeText(getActivity(), "Storage permission required for WFS to work!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }





    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    private void getDir(String dirPath) {
        item = new ArrayList<>();
        path = new ArrayList<>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if(files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    if (FilenameUtils.getExtension(file.toString()).equals("mp3")) {
                        try {
                            MP3 mp3 = new MP3(file);
                            int partNumb = mp3.getRating(); // Gets each files' part number.
                            if (partNumb == 101) {
                                String fileName = mp3.getBand();
                                int fileSize = Integer.parseInt(mp3.getMusicBy());
                                String fileExtension = mp3.getLyricsBy();
                                int totalParts = Integer.parseInt(mp3.getPublisher());
                                String splitFileSize = mp3.getComments();

                                item.add(new FileDetails(fileName, splitFileSize, String.valueOf(fileSize), fileExtension, totalParts, partNumb));
                                path.add(file.getPath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else {
            //Toast.makeText(parentActivity, "No received files!", Toast.LENGTH_SHORT).show();
        }

    }

    class CustomAdapter extends ArrayAdapter<FileDetails> {

        public CustomAdapter(Context context, List<FileDetails> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(parentActivity).inflate(R.layout.receive_row_item, parent, false);
            }

            FileDetails fd = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.row_send_fileNameTV);
            TextView size = (TextView) convertView.findViewById(R.id.row_send_fileSizeTV);
            TextView numParts = (TextView) convertView.findViewById(R.id.row_send_numPartsTV);
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_send_imageView);

            name.setText(fd.getFileName());
            size.setText(getResources().getString(R.string.size) + " " + (int) Float.parseFloat(fd.getTotalFileSize()) / 1024 + " KB");
            numParts.setText(getResources().getString(R.string.total_parts) + " " + fd.getTotalParts());
            String extension = fd.getExtension();

            if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac") || extension.equalsIgnoreCase("wav")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.audio));
            } else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("flv") || extension.equalsIgnoreCase("mpg")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.video));
            } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.photo));
            } else if (extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("ppt") || extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("txt")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.doc));
            } else if (extension.equalsIgnoreCase("pdf")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.pdf));
            } else if (extension.equalsIgnoreCase("rar")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.rar));
            } else if (extension.equalsIgnoreCase("zip")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.zip));
            } else if (extension.equalsIgnoreCase("apk")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.android));
            } else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.file));
            }

            return convertView;
        }
    }

    class PopulateListviewAsync extends AsyncTask<Void, Void, Void> {

        public PopulateListviewAsync() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //Toast.makeText(parentActivity, "Loading list of received files! Please wait!", Toast.LENGTH_LONG).show();

            /*if (buttonClicked) {
                pd = new ProgressDialog(parentActivity);
                pd.setMessage("Loading list of files!");
                pd.setTitle("Please Wait...");
                pd.setCancelable(false);
                pd.show();
            }*/
        }

        @Override
        protected Void doInBackground(Void... params) {
            getDir(receivedAudioPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void resultPath) {
            super.onPostExecute(resultPath);
            if (item != null) {
                if (path != null) {
                    adapter = new CustomAdapter(parentActivity, item);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    listView.invalidateViews();
                    listView.refreshDrawableState();
                }
            }

            //Toast.makeText(parentActivity, "Loading completed! ", Toast.LENGTH_LONG).show();

            if (buttonClicked) {
                /*if (pd != null) {
                    pd.dismiss();
                    buttonClicked = false;
                }*/
            }
        }
    }

    class ExtractAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(parentActivity);
            pd.setMessage(getResources().getString(R.string.extracting_file));
            pd.setTitle(R.string.please_wait);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            RandomAccessFile raf = null;

            try {
                TreeMap<Integer, String> treeMap = new TreeMap<>();
                ArrayList<String> filePathsWillBeDeleted = new ArrayList<>();

                File clickedFile = new File(params[0]);
                MP3 mp3 = new MP3(clickedFile);
                String clickedFilename = mp3.getBand();

                // Checking if the file which is being extracted already exists
                // in the extracted file directory. If it does then delete it, to avoid merging.
                File[] extractedDirFiles = new File(extractionPath).listFiles();
                for (File currentExtractedFile : extractedDirFiles) {
                    if (currentExtractedFile.getName().equalsIgnoreCase(clickedFilename)) {
                        currentExtractedFile.delete();
                    }
                }

                File audioPathFile = new File(receivedAudioPath);
                File[] audioFolder = audioPathFile.listFiles();

                for (File currentFile : audioFolder) {
                    if (currentFile.isFile()) {
                        if (FilenameUtils.getExtension(currentFile.toString()).equals("mp3")) {
                            MP3 mp31 = new MP3(currentFile);
                            String loopFilename = mp31.getBand();
                            if (loopFilename != null) {
                                if (loopFilename.equals(clickedFilename)) { //Null Pointer exception here.
                                    MP3 mp3Loop = new MP3(currentFile);
                                    treeMap.put(mp3Loop.getRating(), currentFile.getAbsolutePath());
                                    filePathsWillBeDeleted.add(currentFile.getAbsolutePath());
                                }
                            }
                        }
                    }
                }

                if (treeMap.size() == Integer.parseInt(mp3.getPublisher())) {
                    boolean firstIteration = true;
                    for (Map.Entry<Integer, String> entry : treeMap.entrySet()) {
                        String accessMode = "rw";
                        if (firstIteration) {
                            accessMode = "r";
                        }
                        String loopFilePath = entry.getValue();
                        MP3 loopMp3 = new MP3(loopFilePath);
                        File loopFile = new File(loopFilePath);
                        int splitFileSize = Integer.parseInt(loopMp3.getComments()); // Each split's file size
                        int totalMP3PlusDataFileSize = (int) loopFile.length(); // Full file size
                        int startingByte = totalMP3PlusDataFileSize - splitFileSize; // Bytes to seek
                        File file1 = new File(extractionPath + loopMp3.getBand());
                        raf = new RandomAccessFile(loopFilePath, accessMode);
                        raf.seek(startingByte);
                        byte[] buffer = new byte[8192];
                        while ((raf.read(buffer, 0, buffer.length)) != -1) {
                            FileUtils.writeByteArrayToFile(file1, buffer, true);
                        }
                        firstIteration = false;
                    }

                    for (String s : filePathsWillBeDeleted) {
                        new File(s).delete();
                    }
                    path.remove(params[0]);
                    item.remove(Integer.parseInt(params[1]));

                    parentActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText(parentActivity, R.string.file_finished_extracting, Toast.LENGTH_LONG);
                            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                            if (v != null) v.setGravity(Gravity.CENTER);
                            toast.show();
                        }
                    });

                } else {
                    final int partsTotal = Integer.parseInt(mp3.getPublisher());
                    final int partNotAvail = partsTotal - treeMap.size();
                    parentActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast toast = Toast.makeText(parentActivity, partNotAvail + " / " + partsTotal + getResources().getString(R.string.parts_not_available) + getResources().getString(R.string.parts_not_available_dl_again), Toast.LENGTH_LONG);
                            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                            if (v != null) v.setGravity(Gravity.CENTER);
                            toast.show();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new CustomAdapter(parentActivity, item);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
            if (pd != null) {
                pd.dismiss();
            }
            //new PopulateListviewAsync().execute();
        }
    }

    class DeleteUnextractedAsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            MP3 mp3;
            try {
                File file = new File(params[0]);
                File audioPathFile = new File(receivedAudioPath);
                mp3 = new MP3(file);
                String clickedFilename = mp3.getBand();
                File[] audioFolder = audioPathFile.listFiles();
                for (File currentFile : audioFolder) {
                    if (currentFile.isFile()) {
                        if (FilenameUtils.getExtension(currentFile.toString()).equals("mp3")) {
                            MP3 mp31 = new MP3(currentFile);
                            String loopFilename = mp31.getBand();
                            if (loopFilename.equals(clickedFilename)) {
                                currentFile.delete();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            item.remove(Integer.parseInt(params[1]));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter = new CustomAdapter(parentActivity, item);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
            Toast.makeText(parentActivity, R.string.file_deleted, Toast.LENGTH_SHORT).show();
        }
    }


}
