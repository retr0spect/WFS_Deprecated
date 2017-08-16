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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static retrospect.aditya.whatzappfilecourierads.SendFragment.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

/**
 * Created by Aditya on 06-02-2015.
 */
public class ExtractedFragment extends Fragment {

    private ListView extractedListView;
    private String extractedPath = Environment.getExternalStorageDirectory() + "/Download/WFS/";
    private ArrayList<File> listFile;
    private ProgressDialog pd;
    FloatingActionButton btnFabRefresh;
    ExtractedArrayAdapter adapter;
    boolean buttonClicked = false;
    private TextView pathTV;

    Activity parentActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_extracted, container, false);

        pathTV = (TextView) v.findViewById(R.id.editTextPath);
        pathTV.setText(" File Path: " + extractedPath + " ");

        extractedListView = (ListView) v.findViewById(R.id.listViewExtracted);

        btnFabRefresh = (FloatingActionButton) v.findViewById(R.id.fab_refresh_extracted);
        btnFabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parentActivity, R.string.refreshing, Toast.LENGTH_SHORT).show();
                buttonClicked = true;
                requestPermission();
            }
        });

        requestPermission();

        extractedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(parentActivity).setTitle(R.string.open_file).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            OpenFileByIntent.openFile(parentActivity, new File(listFile.get(position).getAbsolutePath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(parentActivity, "Canceled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        extractedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(parentActivity).setTitle(R.string.delete_file).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(String.valueOf(listFile.get(position)));
                        file.delete();
                        requestPermission();
                        Toast.makeText(parentActivity, R.string.file_deleted, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ///Toast.makeText(parentActivity, "Canceled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).show();

                return true;
            }
        });
        return v;
    }



    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ExtractedFragment.this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            new MyExtractedAsyncTask().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new MyExtractedAsyncTask().execute();
                } else {
                    Toast.makeText(getActivity(), "Storage permission required for WFS to work!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        requestPermission();
    }

    class ExtractedArrayAdapter extends ArrayAdapter<File> {

        public ExtractedArrayAdapter(Context context, List<File> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parentActivity).inflate(R.layout.extracted_row_item, parent, false);
            }

            File file = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.textView_Row_Extracted_FileName);
            TextView size = (TextView) convertView.findViewById(R.id.textView_Row_Extracted_fileSizeTV);
            TextView modified = (TextView) convertView.findViewById(R.id.textView_Row_Extracted_LastModified);
            ImageView icon = (ImageView) convertView.findViewById(R.id.imageView_Row_Extracted_Icon);

            name.setText(file.getName());
            size.setText( + (int) (file.length()) / 1024 + " KB");

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            modified.setText(getResources().getString(R.string.last_modified) + " " + sdf.format(file.lastModified()));

            String extension = FilenameUtils.getExtension(file.toString());

            if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac") || extension.equalsIgnoreCase("wav")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.audio));
            } else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("flv") || extension.equalsIgnoreCase("mpg")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.video));
            } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif") || extension.equalsIgnoreCase("bmp")) {
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


    class MyExtractedAsyncTask extends AsyncTask<Void, Void, Void> {

        public MyExtractedAsyncTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (buttonClicked) {
                pd = new ProgressDialog(parentActivity);
                pd.setMessage(getResources().getString(R.string.loading_list_file));
                pd.setTitle(R.string.please_wait);
                pd.setCancelable(false);
                pd.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            File file = new File(extractedPath);
            File[] files = file.listFiles();

            listFile = new ArrayList<>();
            Collections.addAll(listFile, files);

            return null;
        }

        @Override
        protected void onPostExecute(Void resultPath) {
            super.onPostExecute(resultPath);
            if (listFile.isEmpty()) {
                if (buttonClicked) {
                    Toast.makeText(parentActivity, R.string.no_new_ext_files, Toast.LENGTH_SHORT).show();
                }
            }
            adapter = new ExtractedArrayAdapter(parentActivity, listFile);
            extractedListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            extractedListView.invalidateViews();
            extractedListView.refreshDrawableState();
            if (buttonClicked) {
                if (pd != null) {
                    pd.dismiss();
                    buttonClicked = false;
                }
            }
        }
    }


}
