package retrospect.aditya.whatzappfilecourierads;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.appnext.ads.interstitial.Interstitial;
import com.appnext.core.callbacks.OnAdClosed;
import com.astuetz.PagerSlidingTabStrip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

/*//////////////--------------Comments Comments Comments Comments-----////////////////////////
1. whatsAppExists() methods checks if WA is installed or not.
2. Directories are created inside whatsAppExists() methods by createDirectories();
3.
*///////////////--------------Comments Comments Comments Comments-----////////////////////////

public class MainActivity extends ActionBarActivity {

    private PagerSlidingTabStrip tabs;
    ViewPager pager;
    MyPagerAdapter adapter;
    SharedPreferences prefs = null;
    MenuItem languageMenu;
    Dialog dialog;

    int showAdOnExit;
    Interstitial interstitial_Ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDirectories();

        showAdOnExit = 0;

        prefs = getSharedPreferences("retrospect.aditya.whatzappfilecourier", MODE_PRIVATE);

        interstitial_Ad = new Interstitial(this, "a1880581-8a05-4205-b70c-f187d1020ba9");
        interstitial_Ad.loadAd();
        interstitial_Ad.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                MainActivity.this.finish();
                System.exit(0);
            }
        });

        /////// ------ Set Language ----- ///////
        setLocale(prefs.getString("lang", "en"));


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.ic_action_wa);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" WFS: Whats App File Sender");
        getSupportActionBar().setElevation(30);

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        tabs.setShouldExpand(true);
        tabs.setIndicatorColor(0xFF34af23);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width = displaymetrics.widthPixels;
        if (width <= 480) {
            tabs.setIndicatorHeight(8);
        }
        if (width > 480 && width <= 800) {
            tabs.setIndicatorHeight(12);
        }
        if (width > 800) {
            tabs.setIndicatorHeight(20);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tabs.setElevation(20);
        }

        adapter = new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        pager.setPageMargin(pageMargin);
        tabs.setViewPager(pager);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {
            new AlertDialog.Builder(this).setTitle(R.string.title_first_run).setMessage(R.string.message_first_run).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(), HelpActivity.class));
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Are you sure you want to exit WFS?");
        builder1.setTitle("Exit App!");
        builder1.setCancelable(true);

        builder1.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (interstitial_Ad.isAdLoaded()) {
                            interstitial_Ad.showAd();
                        } else {
                            MainActivity.super.onBackPressed();
                        }
                    }
                });

        builder1.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }


    public void setLocale(String input) {
        Locale locale = new Locale(input);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        languageMenu = menu.findItem(R.id.language_locale);
        updateLanguageIcon();
        return true;
    }

    private void updateLanguageIcon() {
        String currentLanguage = prefs.getString("lang", "en");
        switch (currentLanguage) {
            case "pt":
                languageMenu.setIcon(R.drawable.pt);
                break;
            case "es":
                languageMenu.setIcon(R.drawable.es);
                break;
            case "it":
                languageMenu.setIcon(R.drawable.it);
                break;
            case "de":
                languageMenu.setIcon(R.drawable.ger);
                break;
            default:
                languageMenu.setIcon(R.drawable.en);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.language_locale:
                langDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void langDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.lang_dialog_layout);
        dialog.setTitle("Change Language");
        dialog.show();

        Button btnEn = (Button) dialog.findViewById(R.id.btn_lang_en);
        btnEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                prefs.edit().putString("lang", "en").commit();
                setLocale("en");
                restartActivityAfterLangChange();
                Toast.makeText(MainActivity.this, "Language changed to English!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnPt = (Button) dialog.findViewById(R.id.btn_lang_pt);
        btnPt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                prefs.edit().putString("lang", "pt").commit();
                restartActivityAfterLangChange();
                setLocale("pt");
                Toast.makeText(MainActivity.this, "Language changed to Portuguese!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnEs = (Button) dialog.findViewById(R.id.btn_lang_es);
        btnEs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                prefs.edit().putString("lang", "es").commit();
                restartActivityAfterLangChange();
                setLocale("es");
                Toast.makeText(MainActivity.this, "Language changed to Spanish!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnIt = (Button) dialog.findViewById(R.id.btn_lang_it);
        btnIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                prefs.edit().putString("lang", "it").commit();
                restartActivityAfterLangChange();
                setLocale("it");
                Toast.makeText(MainActivity.this, "Language changed to Italian!", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnGer = (Button) dialog.findViewById(R.id.btn_lang_german);
        btnGer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                prefs.edit().putString("lang", "de").commit();
                restartActivityAfterLangChange();
                setLocale("de");
                Toast.makeText(MainActivity.this, "Language changed to German!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void restartActivityAfterLangChange() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {getResources().getString(R.string.send_file_tab), getResources().getString(R.string.received_tab), getResources().getString(R.string.extracted_tab)};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new SendFragment();
            } else if (position == 1) {
                return new ReceiveFragment();
            } else {
                return new ExtractedFragment();
            }
        }
    }

    private void createDirectories() {
        String extracted = Environment.getExternalStorageDirectory() + "/Download/WFS/";
        String stub = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/";
        String sent = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Sent/";
        String temp = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Temp/";
        String courier = Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/Temp/Splits/";
        String audio = Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Audio/";

        ArrayList<String> allDirs = new ArrayList<>();
        allDirs.add(extracted);
        allDirs.add(stub);
        allDirs.add(sent);
        allDirs.add(temp);
        allDirs.add(courier);
        allDirs.add(audio);

        ckeckExistanceAndCreate(allDirs);
    }

    private void ckeckExistanceAndCreate(ArrayList<String> dirPaths) {
        for (String path : dirPaths) {
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        }

        boolean check = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3").exists();
        if (!check) {
            try {
                AssetsCopy.copyFileFromAssets(MainActivity.this, "asset.mp3", Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/.Stub/asset.mp3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final String NOMEDIA_FILE = ".nomedia";
        File path = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/WFS/");
        path.mkdirs();
        File file = new File(path, NOMEDIA_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }


}