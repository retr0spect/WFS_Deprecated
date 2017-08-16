package retrospect.aditya.whatzappfilecourierads;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Aditya on 06-02-2015.
 */
public class SendFragment extends Fragment {

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

    ProgressDialog pd;

    private List<String> path = null;
    private TextView myPath;
    private ListView listView;

    FloatingActionButton fabCleanCache, fabHelp, fabShare, fabRateUs, fabWFC, fabQuiz, fabNoAds;
    FloatingActionsMenu fam;

    View view;

    Activity parentActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_send, container, false);

////////////////////////// ======== File Shared by file explorers ========== /////////////////////////
        Intent intent = parentActivity.getIntent();
        /*Uri uri = intent.getData();
        if (uri != null) {
            final File file = new File(uri.toString().substring(6));
            dialogSendConfirmation(file);
        } else {
            //Toast.makeText(parentActivity, "Invalid File! Try Again!", Toast.LENGTH_SHORT).show();
        }*/


        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.v2_dialog);
        Button dialogButton = (Button) dialog.findViewById(R.id.btn_v2_get);
        // if button is clicked, close the custom dialog
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Uri uri = Uri.parse("market://details?id=com.retroid.wfs.whatsapp.file.sender");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.retroid.wfs.whatsapp.file.sender")));
                }
            }
        });

        dialog.show();

        if (intent.getAction().equals(Intent.ACTION_SEND)) {
            Bundle bundle = intent.getExtras();
            Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
            if (uri != null) {
                String filePath = GetPath.getFilePath(parentActivity, uri);
                File file = new File(filePath);
                dialogSendConfirmation(file);
            } else {
                Toast.makeText(parentActivity, R.string.invalid_file, Toast.LENGTH_SHORT).show();
            }
        }


////////////////////////////============= Button Click Handles ================////////////////////////////
        fam = (FloatingActionsMenu) view.findViewById(R.id.fam);

        CardView cv = (CardView) view.findViewById(R.id.card_view);
        CardView cvLolli = (CardView) view.findViewById(R.id.card_view_Lolli);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cv.setVisibility(View.GONE);
            TextView cardButtonLolli = (TextView) view.findViewById(R.id.select_file_cardbuttonLolli);
            cardButtonLolli.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file*//**//*");
                    final PackageManager packageManager = parentActivity.getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
                    if (list.size() > 0) {
                        startActivityForResult(intent, 1);
                    } else {
                        Toast toast = Toast.makeText(parentActivity, R.string.no_file_explorer_file_chooser, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        if (tv != null) tv.setGravity(Gravity.CENTER);
                        toast.show();
                    }

                }
            });

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            cvLolli.setVisibility(View.GONE);
            TextView cardButton = (TextView) view.findViewById(R.id.select_file_cardbutton);
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file*//**//*");
                    final PackageManager packageManager = parentActivity.getPackageManager();
                    List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
                    if (list.size() > 0) {
                        startActivityForResult(intent, 1);
                    } else {
                        Toast toast = Toast.makeText(parentActivity, R.string.no_file_explorer_blue_button, Toast.LENGTH_LONG);
                        TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
                        if (tv != null) tv.setGravity(Gravity.CENTER);
                        toast.show();
                    }
                }
            });
        }

        fabQuiz = (FloatingActionButton) view.findViewById(R.id.fab_quiz);
        fabQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parentActivity, R.string.quiz_time, Toast.LENGTH_LONG).show();
                Uri uri = Uri.parse("market://details?id=com.retroid.quiz.time.ultimate.trivia");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.retroid.quiz.time.ultimate.trivia")));
                }
            }
        });

        /*fabWFC = (FloatingActionButton) view.findViewById(R.id.fab_wfc);
        fabWFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(parentActivity, R.string.wclean, Toast.LENGTH_LONG).show();
                Uri uri = Uri.parse("market://details?id=com.retrospectivecreations.wfc");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.retrospectivecreations.wfc")));
                }
            }
        });*/


        fabNoAds = (FloatingActionButton) view.findViewById(R.id.fab_button_noAds);
        fabNoAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*new AlertDialog.Builder(parentActivity).setTitle(R.string.adfree_title).setMessage(R.string.adfree_message).setPositiveButton(R.string.yes_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Uri uri = Uri.parse("market://details?id=com.retroid.whatsapp.file.sender.wfs");
                        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        try {
                            startActivity(goToMarket);
                        } catch (ActivityNotFoundException e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.retroid.whatsapp.file.sender.wfs")));
                        }
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();*/
                Uri uri = Uri.parse("market://details?id=com.retroid.wfs.whatsapp.file.sender");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.retroid.wfs.whatsapp.file.sender")));
                }

            }
        });


        fabRateUs = (FloatingActionButton) view.findViewById(R.id.fab_rate_us);
        fabRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(parentActivity).setTitle(R.string.rate_app_title)
                        .setMessage(R.string.rate_app_message)
                        .setPositiveButton(R.string.yes_sure, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri uri = Uri.parse("market://details?id=" + parentActivity.getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + parentActivity.getPackageName())));
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

            }
        });

        fabCleanCache = (FloatingActionButton) view.findViewById(R.id.fab_cleancache);
        fabCleanCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cacheSentPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Sent/";
                File file = new File(cacheSentPath);
                try {
                    String size = String.valueOf((int) FileUtils.sizeOfDirectory(file) / 1024 / 1024).trim();
                    FileUtils.cleanDirectory(file);
                    Toast.makeText(parentActivity, size + " " + getResources().getString(R.string.mb_cleaned), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        fabHelp = (FloatingActionButton) view.findViewById(R.id.fab_button_help);
        fabHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(parentActivity, HelpActivity.class));
            }
        });

        fabShare = (FloatingActionButton) view.findViewById(R.id.fab_button_share);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Download WFS: WhatsApp File Sender https://play.google.com/store/apps/details?id=retrospect.aditya.whatzappfilecourierads");
                try {
                    parentActivity.startActivity(Intent.createChooser(whatsappIntent, "Share 'WFS: WhatsApp File Sender' using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(parentActivity, R.string.no_msg_app, Toast.LENGTH_SHORT).show();
                }
            }
        });
////////////////////=============== Button Click Handles End ===============////////////////////////

        myPath = (TextView) view.findViewById(R.id.editTextCurrentPath);
        listView = (ListView) view.findViewById(R.id.listView);

        requestPermission();

        return view;
    }



    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            SendFragment.this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            String sdRootDir = Environment.getExternalStorageDirectory().toString();
            getDir(sdRootDir);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String sdRootDir = Environment.getExternalStorageDirectory().toString();
                    getDir(sdRootDir);
                } else {
                    Toast.makeText(getActivity(), "Storage permission required for WFS to work!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


///////////////////////=============== OnCreate End ===============/////////////////////////////////

    /////////////////////============== FileExplorer Open Here =================////////////////////////
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1 && data != null) {
            String path = null;
            switch (requestCode) {
                case 1:
                    Uri uri = data.getData();
                    try {
                        path = getPath(parentActivity, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    break;
            }

            if (path != null) {
                File file = new File(path);
                long lengthMB = file.length() / 1024 / 1024;
                if (file.length() > 159.8 * 1024 * 1024) {
                    Toast toast = Toast.makeText(parentActivity, R.string.selected_file_size + lengthMB + " MB\n\n" + R.string.sending_limit, Toast.LENGTH_LONG);
                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                    if (v != null) v.setGravity(Gravity.CENTER);
                    toast.show();
                    //Toast.makeText(parentActivity), "    Selected file size: " + lengthMB + "MB\n\n" + "File sending limit: 160 MB!", Toast.LENGTH_LONG).show();
                } else {
                    new MyAsyncTask().execute(file.getAbsolutePath());
                }
            } else {
                Toast.makeText(parentActivity, R.string.invalid_file, Toast.LENGTH_SHORT).show();
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

    ///////////////////////////------- Get Directory------ /////////////////////////////
    private void getDir(String dirPath) {

        myPath.setText("Current Path: " + dirPath);
        List<FileProperties> item = new ArrayList<>();
        path = new ArrayList<>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        String rootPath = "/";
        if (!dirPath.equals(rootPath)) {
            //item.add(new FileProperties("./", "", "", false));
            //path.add(rootPath);
            item.add(new FileProperties("../", "", "", false, 0, "01-01-2000"));
            path.add(f.getParent());
        }

        for (File file : files) {
            path.add(file.getPath());
            file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            String lasModified = sdf.format(file.lastModified());

            if (file.isDirectory()) {
                //item.add(file.getName() + "/");
                File[] filesList = file.listFiles();

                if (filesList != null && filesList.length > 0) {
                    item.add(new FileProperties(file.getName() + "/", "", String.valueOf(file.length()), false, filesList.length, lasModified));
                } else {
                    item.add(new FileProperties(file.getName() + "/", "", String.valueOf(file.length()), false, 0, lasModified));
                }
            } else
                //item.add(file.getName());
                item.add(new FileProperties(file.getName(), FilenameUtils.getExtension(file.toString()), String.valueOf(file.length()), true, 0, lasModified));
        }


        Collections.sort(path, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        CustomAdapter adapter = new CustomAdapter(parentActivity, item);
        adapter.sort(new Comparator<FileProperties>() {
            @Override
            public int compare(FileProperties lhs, FileProperties rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        listView.setAdapter(adapter);

        //listView.setAdapter(new ArrayAdapter<>(this, R.layout.row, item));
        final File[] file = new File[1];
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file[0] = new File(path.get(position));
                if (file[0].isDirectory()) {
                    if (file[0].canRead())
                        getDir(path.get(position));
                    else {
                        new AlertDialog.Builder(parentActivity)
                                .setTitle("[" + file[0].getName() + "] " + R.string.folder_not_read)
                                .setPositiveButton(R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                    }

                } else {
                    dialogSendConfirmation(file[0]);
                }
            }
        });

    }

    class CustomAdapter extends ArrayAdapter<FileProperties> {

        public CustomAdapter(Context context, List<FileProperties> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(parentActivity).inflate(R.layout.send_row_item, parent, false);
            }

            FileProperties fd = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.row_fileNameTV);
            TextView lastMod = (TextView) convertView.findViewById(R.id.row_LastModTV);
            TextView numFiles = (TextView) convertView.findViewById(R.id.row_numFilesSizeFileTV);
            ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);

            name.setText(fd.getName());
            lastMod.setText(getResources().getString(R.string.last_modified) + " " + fd.getLastModified());
            String extension = fd.getExtension();

            if (fd.getIsFile()) {
                numFiles.setText(getResources().getString(R.string.size) + " " + String.valueOf(Long.parseLong(fd.getSize()) / 1024) + " KB");
                if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac") || extension.equalsIgnoreCase("wav")) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.audio));
                } else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("flv") || extension.equalsIgnoreCase("mpg")) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.video));
                } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                } else if (extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("ppt") || extension.equalsIgnoreCase("txt") || extension.equalsIgnoreCase("docx")) {
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

            } else {
                numFiles.setText(getResources().getString(R.string.contains) + " " + fd.getnumFilesInFolder() + " files");
                icon.setImageDrawable(getResources().getDrawable(R.drawable.folder));
            }

            if (fd.getLastModified().equals("01-01-2000")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.back));
            }

            return convertView;
        }
    }

    class MyAsyncTask extends AsyncTask<String, String, ArrayList<String>> {

        public MyAsyncTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(parentActivity);
            pd.setMessage(getResources().getString(R.string.preparing));
            pd.setTitle(R.string.please_wait);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected ArrayList<String> doInBackground(String... fileToSendPath) {
            File file = new File(fileToSendPath[0]);
            if (file.length() > 1024 * 1024 * 15.9) {
                return multiPrepareForCourier(fileToSendPath[0]);
            } else {
                return prepareForCourier(fileToSendPath[0]);
            }
        }


        @Override
        protected void onPostExecute(ArrayList<String> resultPath) {
            super.onPostExecute(resultPath);
            pd.dismiss();
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
            Toast.makeText(parentActivity, getResources().getString(R.string.select_person), Toast.LENGTH_LONG).show();
        }
    }


    //////////////======= Returns: String... Mp3 file embedded with data to be sent ======/////////////////
    public ArrayList<String> prepareForCourier(String fileToSendPath) {

        final String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3";
        final String stubPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/";

        boolean check = new File(assetMp3).exists();
        if (!check) {
            try {
                new File(stubPath).mkdirs();
                AssetsCopy.copyFileFromAssets(parentActivity, "asset.mp3", assetMp3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return MultiSplitUtils.makeSingleDataPlusMP3(fileToSendPath);

    }


    public ArrayList<String> multiPrepareForCourier(String fileToSendPath) {
        final String stubPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/";
        final String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3";
        final String fileReadyToCourier = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Temp/Splits/";
        ArrayList<String> listReadyFiles = new ArrayList<>();
        boolean check = new File(assetMp3).exists();
        if (!check) {
            try {
                new File(stubPath).mkdirs();
                AssetsCopy.copyFileFromAssets(parentActivity, "asset.mp3", assetMp3);
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


    private void dialogSendConfirmation(final File file) {
        new AlertDialog.Builder(parentActivity)
                .setTitle(R.string.now_sending_title)
                .setMessage(getResources().getString(R.string.now_sending_message_one) + " \"" + file.getName() + "\"" + "?")
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                long lengthMB = file.length() / 1024 / 1024;
                                if (file.length() > 159.2 * 1024 * 1024) {
                                    Toast toast = Toast.makeText(parentActivity, getResources().getString(R.string.selected_file_size) + lengthMB + "MB\n\n" + getResources().getString(R.string.sending_limit), Toast.LENGTH_LONG);
                                    TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                                    if (v != null) v.setGravity(Gravity.CENTER);
                                    toast.show();
                                } else {
                                    int parts = (int) (file.length() / 1024 / 1024 / 15.8) + 1;
                                    String message;
                                    if (parts == 1) {
                                        message = getResources().getString(R.string.selected_file_will_hidden);
                                    } else {
                                        message = getResources().getString(R.string.selected_file_will_divided) + " " + parts + " audio files. \n\n" + getResources().getString(R.string.selected_file_will_be_sure);
                                    }
                                    new AlertDialog.Builder(parentActivity).setTitle(R.string.attention)
                                            .setMessage(message)
                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new MyAsyncTask().execute(file.getAbsolutePath());
                                                    dialog.dismiss();
                                                }
                                            }).setCancelable(false).show();
                                }
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(parentActivity, R.string.canceled, Toast.LENGTH_SHORT).show();
            }
        }).show();
    }






}