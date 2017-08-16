package retrospect.aditya.whatzappfilecourierads;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aditya on 13-02-2015.
 */
public class MultiSendFragment extends Fragment {

    Button btnAdd, btnSend;
    TextView tvFileInfo, tvTotalFiles;
    ListView lvAddedFiles;
    ArrayList<MultiFileDetails> fileDetailsArray = new ArrayList<>();
    ArrayList<String> filePaths = new ArrayList<>();
    String outOfMB = " MB | 160 MB";
    String outOfTotalFiles = " File | 10 File";

    float totalMB = 0;
    int totalFiles = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_multisend, container, false);

        /////////// Test Starts ////////////



        /////////// Test Ends //////////////

        tvFileInfo = (TextView) v.findViewById(R.id.textView_multi_fileInfo);
        tvFileInfo.setText(totalMB + outOfMB);

        tvTotalFiles = (TextView) v.findViewById(R.id.textView_multi_totalFiles);
        tvTotalFiles.setText(totalFiles + outOfTotalFiles);

        tvFileInfo.setTextColor(0xFF00ff00);
        tvTotalFiles.setTextColor(0xFF00ff00);

        btnAdd = (Button) v.findViewById(R.id.button_multi_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (filePaths.size() >= 10) {
                    btnAdd.setClickable(false);
                    Toast.makeText(getActivity(), "Only 10 files can be sent at a time!", Toast.LENGTH_SHORT).show();
                } else {
                    btnAdd.setClickable(true);
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file*//*");
                    startActivityForResult(intent, 1);
                }

            }
        });

        btnSend = (Button) v.findViewById(R.id.button_multi_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (fileDetailsArray.isEmpty()) {
                    btnSend.setClickable(false);
                    Toast.makeText(getActivity(), "First add some file(s)!", Toast.LENGTH_SHORT).show();
                } else {
                    new MyMultiSendAsyncTask().execute(filePaths);
                }
            }
        });

        lvAddedFiles = (ListView) v.findViewById(R.id.multi_listview);
        //CustomMultiAdapter adapter = new CustomMultiAdapter(getActivity(), fileDetailsArray);
        //lvAddedFiles.setAdapter(adapter);

        return v;
    }


    ////////////////////////////// ======== FileExplorer Open Here ========== ////////////////////////////////
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1 && data != null) {
            String path = null;
            switch (requestCode) {
                case 1:
                    if (resultCode == -1) {
                        Uri uri = data.getData();
                        try {
                            path = getPath(getActivity(), uri);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            if (path != null) {
                File file = new File(path);
                fileDetailsArray.add(new MultiFileDetails(file.getName(), file.length()));
                filePaths.add(path);

                totalMB += ((float) file.length()) / 1024 / 1024;
                tvFileInfo.setText(String.format("%.2f", (totalMB)) + outOfMB);

                totalFiles += 1;
                tvTotalFiles.setText(totalFiles + outOfTotalFiles);

                if (totalFiles >= 10) {
                    tvTotalFiles.setTextColor(0xFFff0000);
                }

                if (totalMB > 159) {
                    btnSend.setClickable(false);
                    tvFileInfo.setTextColor(0xFFff0000);
                }

                CustomMultiAdapter adapter = new CustomMultiAdapter(getActivity(), fileDetailsArray);
                lvAddedFiles.setAdapter(adapter);

            }
        }
    }

    public static String getPath(Context context, Uri uri)
            throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
                cursor.close();
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    class CustomMultiAdapter extends ArrayAdapter<MultiFileDetails> {
        public CustomMultiAdapter(Context context, List<MultiFileDetails> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.multisend_row_item, parent, false);
            }

            MultiFileDetails mfd = getItem(position);

            TextView fileNameTV = (TextView) convertView.findViewById(R.id.item_multi_FileNameTV);
            TextView fileSizeTV = (TextView) convertView.findViewById(R.id.item_multi_FileSizeTV);
            Button deleteButton = (Button) convertView.findViewById(R.id.item_multi_deleteBtn);

            String fileName = mfd.getFileName();
            fileNameTV.setText(fileName);

            long fileSize = mfd.getFileSize();
            int fileSizeKB = ((int) fileSize) / 1024;
            final float FileSizeMB = ((float) fileSize) / 1024 / 1024;
            final float FileSizeMBFormatted = Float.parseFloat(String.format("%.2f", ((float) fileSize) / 1024 / 1024));
            if (fileSizeKB < 1000) {
                fileSizeTV.setText("Size: " + fileSizeKB + " KB");
            } else {
                fileSizeTV.setText("Size: " + FileSizeMBFormatted + " MB");
            }

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileDetailsArray.remove(position);
                    filePaths.remove(position);

                    totalMB -= FileSizeMB;
                    tvFileInfo.setText(String.format("%.2f", (totalMB)) + outOfMB);

                    totalFiles -= 1;
                    tvTotalFiles.setText(totalFiles + outOfTotalFiles);

                    tvFileInfo.setTextColor(0xFF00ff00);
                    tvTotalFiles.setTextColor(0xFF00ff00);

                    if (filePaths.size() <= 9) {
                        btnAdd.setClickable(true);
                    }
                    if (totalMB < 159) {
                        btnSend.setClickable(true);
                    }
                    if (totalMB == -0.0) {
                        totalMB = 0;
                    }
                    CustomMultiAdapter adapter = new CustomMultiAdapter(getActivity(), fileDetailsArray);
                    lvAddedFiles.setAdapter(adapter);
                }
            });

            return convertView;
        }
    }


    class MyMultiSendAsyncTask extends AsyncTask<ArrayList<String>, Void, ArrayList<String>> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Preparing your file!");
            progressDialog.setTitle("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected ArrayList<String> doInBackground(ArrayList<String>... params) {
            ArrayList<String> preparedFilepaths = new ArrayList<>();

            for (String currentFilePath : params[0]) {
                File file = new File(currentFilePath);
                if (file.length() > 1024 * 1024 * 15.8) {
                    ArrayList<String> splitPreparedFiles = multiPrepareForCourier(currentFilePath);
                    for (String eachSplitFile : splitPreparedFiles) {
                        preparedFilepaths.add(eachSplitFile);
                    }
                } else {
                    preparedFilepaths.add(prepareForCourier(currentFilePath).get(0));
                }
            }
            return preparedFilepaths;
        }

        @Override
        protected void onPostExecute(ArrayList<String> resultPath) {
            super.onPostExecute(resultPath);
            progressDialog.dismiss();
            if (resultPath.size() > 1) {
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("audio/mp3");
                ArrayList<Uri> files = new ArrayList<>();
                for (String path : resultPath) {
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                }
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, files);
                shareIntent.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(shareIntent, "Aditya"));
            } else {
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("audio/mp3");
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(resultPath.get(0)));
                shareIntent.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(shareIntent, "Aditya"));
            }

        }

    }

    //////////////// Returns: String... Mp3 file embedded with data to be sent /////////////////
    public ArrayList<String> prepareForCourier(String fileToSendPath) {

        final String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WSF/.Stub/asset.mp3";
        final String stubPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WSF/.Stub/";

        boolean check = new File(assetMp3).exists();
        if (!check) {
            try {
                new File(stubPath).mkdirs();
                copyFileFromAssets(getActivity(), "asset.mp3", assetMp3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return MultiSplitUtils.makeSingleDataPlusMP3(fileToSendPath);

    }


    public ArrayList<String> multiPrepareForCourier(String fileToSendPath) {

        final String stubPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/";
        final String fileReadyToCourier = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Temp/Splits/";
        final String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3";

        ArrayList<String> listReadyFiles = new ArrayList<>();

        boolean check = new File(assetMp3).exists();
        if (!check) {
            try {
                new File(stubPath).mkdirs();
                copyFileFromAssets(getActivity(), "asset.mp3", assetMp3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            listReadyFiles = MultiSplitUtils.split(fileToSendPath, fileReadyToCourier);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listReadyFiles;

    }


////////////// Conversion to OBjectOutputStream Starts, from 'to be sent' MP3 file ////////////////


    static public void copyFileFromAssets(Context context, String file, String dest) throws Exception {
        InputStream in = null;
        OutputStream fout = null;
        int count;

        try {
            in = context.getAssets().open(file);
            fout = new FileOutputStream(new File(dest));

            byte data[] = new byte[1024];
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

}
