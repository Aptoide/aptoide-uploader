/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;
import com.octo.android.robospice.SpiceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pt.caixamagica.aptoide.uploader.dialog.ConfirmationDialog;
import pt.caixamagica.aptoide.uploader.uploadService.MyBinder;
import pt.caixamagica.aptoide.uploader.uploadService.UploadService;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 02-02-2015.
 */
public class FragmentAppView extends Fragment {

    protected SpiceManager spiceManager;

    protected View rootView;

    protected UserCredentialsJson userCredentialsJson;

    int checked = 0;

    GridView gridview;

    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    private ManelAdapter adapter;
    private boolean mBound = false;
    private UploadService mService;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyBinder binder = (MyBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private MenuItem.OnMenuItemClickListener logoutListener() {

        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                ConfirmationDialog confirmationDialog = new ConfirmationDialog();
                confirmationDialog.setTargetFragment(FragmentAppView.this, 0);
                confirmationDialog.show(getFragmentManager(), "confirmDialog");

                return false;
            }
        };
    }

    private MenuItem.OnMenuItemClickListener sortByFirstInstallListener() {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {

                Toast.makeText(getActivity(), "Sorting...", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!item.isChecked()) {
                            Collections.sort(adapter.mDataset, newFirstInstallComparator());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    item.setChecked(true);
                                }
                            });
                            checked = item.getItemId();
                        }
                    }
                }).start();

                return false;
            }
        };
    }

    private void sortByFirstInstall() {
        Toast.makeText(getActivity(), "Sorting by Date", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(adapter.mDataset, newFirstInstallComparator());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private void sortByLastInstall() {
        Toast.makeText(getActivity(), "Sorting by Date", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Collections.sort(adapter.mDataset, newLastInstallComparator());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    private MenuItem.OnMenuItemClickListener sortByNameListener() {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {

                Toast.makeText(getActivity(), "Sorting...", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!item.isChecked()) {
                            // Carrega as labels necessárias
                            PackageManager packageManager = getActivity().getPackageManager();
                            for (SelectablePackageInfo selectablePackageInfo : adapter.mDataset)
                                selectablePackageInfo.getLabel();

                            Collections.sort(adapter.mDataset, newNameComparator());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                    item.setChecked(true);
                                }
                            });
                            checked = item.getItemId();
                        }
                    }
                }).start();

                return false;
            }
        };
    }

    private void sortByName() {
        Toast.makeText(getActivity(), "Sorting by Name", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Carrega as labels necessárias
                PackageManager packageManager = getActivity().getPackageManager();
                for (SelectablePackageInfo selectablePackageInfo : adapter.mDataset)
                    selectablePackageInfo.getLabel();

                Collections.sort(adapter.mDataset, newNameComparator());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public Comparator<SelectablePackageInfo> newNameComparator() {
        return new Comparator<SelectablePackageInfo>() {
            @Override
            public int compare(SelectablePackageInfo lhs, SelectablePackageInfo rhs) {
                return lhs.getLabel().compareTo(rhs.getLabel());
            }
        };
    }

    public Comparator<SelectablePackageInfo> newFirstInstallComparator() {
        return new Comparator<SelectablePackageInfo>() {
            @Override
            public int compare(SelectablePackageInfo lhs, SelectablePackageInfo rhs) {
                return (int) (rhs.firstInstallTime / 1000 - lhs.firstInstallTime / 1000);
            }
        };
    }

    public Comparator<SelectablePackageInfo> newLastInstallComparator() {
        return new Comparator<SelectablePackageInfo>() {
            @Override
            public int compare(SelectablePackageInfo lhs, SelectablePackageInfo rhs) {
                return (int) (getLastInstallDate(rhs) / 1000 - getLastInstallDate(lhs) / 1000);
            }
        };
    }

    private long getLastInstallDate(PackageInfo packageInfo) {
        PackageManager pm = getContext().getPackageManager();
        String appFile = packageInfo.applicationInfo.sourceDir;
        return new File(appFile).lastModified(); //Epoch Time
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            checked = savedInstanceState.getInt("sortCheckable");
        }

        setHasOptionsMenu(true);

        userCredentialsJson = (UserCredentialsJson) getArguments().get("userCredentialsJson");
        setAdapter(savedInstanceState, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_app_view, container, false);

        final Spinner sp = (Spinner) rootView.findViewById(R.id.sort_spinner);

        prepareSpinner(R.id.sort_spinner, R.array.sort_spinner_array);

        sp.post(new Runnable() {
            public void run() {
                sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                        if (arg2 == 0) {
                            sortByLastInstall();
                        }

                        if (arg2 == 1) {
                            sortByName();
                        }

                        adapter.uncheckAll();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }
        });

        TextView tv = (TextView) rootView.findViewById(R.id.select_apps_hint);
        tv.setText("Store   " + userCredentialsJson.getRepo());
        tv.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                PackageInfo pInfo = null;
                try {
                    pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                    String version = pInfo.versionName;
                    int versionCode = pInfo.versionCode;
                    String appName = pInfo.packageName;

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("App : " + appName + "\n" + "Version : " + version + "\n" + "Version Code : " + versionCode);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            System.out.println("Pressed OK in the error of the app version");
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                return false;
            }


        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
        int i = (int) (metrics.widthPixels / px);
        gridview = (GridView) rootView.findViewById(R.id.grid_view);
        gridview.setNumColumns(i);
        adapter.setAdapterView(gridview);

        setUploadButtonListener();
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), UploadService.class);

        getActivity().startService(intent);
        intent = new Intent(getActivity(), UploadService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getActivity().getApplicationInfo().labelRes);

        adapter.setListener(new ManelAdapter.ManelAdapterShowListener() {

            final int timeMillis = 100;

            @Override
            public void show() {
                TranslateAnimation anim = new TranslateAnimation(0, 0, 0, rootView.findViewById(R.id.select_apps_hint).getHeight());
                anim.setDuration(timeMillis);
                anim.setFillAfter(true);

                anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ViewCompat.setTranslationY(rootView.findViewById(R.id.grid_view_and_hint), 0);
                        rootView.findViewById(R.id.grid_view_and_hint).clearAnimation();
                    }
                });
                rootView.findViewById(R.id.grid_view_and_hint).startAnimation(anim);
                rootView.findViewById(R.id.submitAppsButton).setVisibility(View.GONE);
            }

            @Override
            public void hide() {

                TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -rootView.findViewById(R.id.select_apps_hint).getHeight());
                anim.setDuration(timeMillis);
                anim.setFillAfter(true);

                anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ViewCompat.setTranslationY(rootView.findViewById(R.id.grid_view_and_hint), -rootView.findViewById(R.id.select_apps_hint).getHeight());
                        rootView.findViewById(R.id.grid_view_and_hint).clearAnimation();
                    }
                });
                rootView.findViewById(R.id.grid_view_and_hint).startAnimation(anim);
                rootView.findViewById(R.id.submitAppsButton).setVisibility(View.VISIBLE);
            }
        }, rootView);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("userCredentialsJson", userCredentialsJson);
        outState.putInt("sortCheckable", checked);

        adapter.save(outState);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.app_grid_menu, menu);

        menu.findItem(R.id.logout_button).setOnMenuItemClickListener(logoutListener());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    private void prepareSpinner(int viewId, int arrayId) {
        Spinner spinner = (Spinner) rootView.findViewById(viewId);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), arrayId, R.layout.spinner_item);

        String[] stringArray = getResources().getStringArray(arrayId);

        ArrayAdapter<CharSequence> a = new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_item, stringArray) {
            @Override
            public int getCount() {
                int count = super.getCount();
                return count > 0 ? count - 1 : count;
            }
        };

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private void setAdapter(Bundle savedInstanceState, final View view) {

        adapter = new ManelAdapter(savedInstanceState, view, this, nonSystemPackages(true), userCredentialsJson);

        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ((MultiChoiceAdapter) parent.getAdapter()).setItemChecked(id, true);
            }
        });
    }

    private void setUploadButtonListener() {
        rootView.findViewById(R.id.submitAppsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<SelectablePackageInfo> selectablePackageInfos = new ArrayList<>(adapter.getCheckedItemCount());
                for (Long aLong : adapter.getCheckedItems()) {
                    selectablePackageInfos.add(adapter.getItem(aLong.intValue()));
                }

                for (SelectablePackageInfo selectablePackageInfo : selectablePackageInfos) {
                    mService.prepareUploadAndSend(userCredentialsJson, selectablePackageInfo);
                }

                adapter.uncheckAll();
                Toast.makeText(getActivity(), "Sending app in the background", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeToSubmitAppFragment(UserCredentialsJson userCredentialsJson, SelectablePackageInfo selectablePackageInfo) {
        Fragment submitAppFragment = new SubmitAppFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("userCredentialsJson", userCredentialsJson);

        ArrayList<SelectablePackageInfo> selectablePackageInfos = new ArrayList<>(1);
        selectablePackageInfos.add(selectablePackageInfo);

        bundle.putParcelableArrayList("selectableAppNames", selectablePackageInfos);
        submitAppFragment.setArguments(bundle);
        adapter.ffinishActionMode();

        getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, submitAppFragment).commit();
    }

    private List<SelectablePackageInfo> nonSystemPackages(boolean ordered) {
        List<PackageInfo> packs = getActivity().getPackageManager().getInstalledPackages(0);

        Iterator<PackageInfo> infoIterator = packs.iterator();

        LinkedList<PackageInfo> packageInfos = new LinkedList<>();
        while (infoIterator.hasNext()) {
            PackageInfo next = infoIterator.next();
            if (isSystemUpdatedPackage(next) || !isSystemPackage(next)) packageInfos.add(next);
        }

        List<SelectablePackageInfo> selectablePackageInfos = new ArrayList<>();

        selectablePackageInfos.clear();
        for (PackageInfo p : packageInfos) {
            selectablePackageInfos.add(new SelectablePackageInfo(p, getActivity().getPackageManager()));
        }

        if (ordered) Collections.sort(selectablePackageInfos, newLastInstallComparator());

        return selectablePackageInfos;
    }

    private boolean isSystemPackage(PackageInfo packageInfo) {
        return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private boolean isSystemUpdatedPackage(PackageInfo packageInfo) {
        int maskUpdade = ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (packageInfo.applicationInfo.flags & maskUpdade) != 0;
    }
}
