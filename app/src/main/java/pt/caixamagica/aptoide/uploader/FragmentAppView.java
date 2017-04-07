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
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.vending.licensing.ValidationException;
import com.manuelpeinado.multichoiceadapter.MultiChoiceAdapter;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import pt.caixamagica.aptoide.uploader.dialog.ConfirmationDialog;
import pt.caixamagica.aptoide.uploader.retrofit.RetrofitSpiceServiceUploaderSecondary;
import pt.caixamagica.aptoide.uploader.retrofit.request.GetProposedRequest;
import pt.caixamagica.aptoide.uploader.uploadService.MyBinder;
import pt.caixamagica.aptoide.uploader.uploadService.UploadService;
import pt.caixamagica.aptoide.uploader.util.Utils;
import pt.caixamagica.aptoide.uploader.webservices.json.GetProposedResponse;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by neuro on 02-02-2015.
 */
public class FragmentAppView extends Fragment {

  protected SpiceManager spiceManager;

  protected View rootView;

  protected UserCredentialsJson userCredentialsJson;
  protected SpiceManager spiceManagerSecondary =
      new SpiceManager(RetrofitSpiceServiceUploaderSecondary.class);
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

    @Override public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      MyBinder binder = (MyBinder) service;
      mService = binder.getService();
      mBound = true;
    }

    @Override public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };

  private MenuItem.OnMenuItemClickListener sortByFirstInstallListener() {
    return new MenuItem.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(final MenuItem item) {

        Toast.makeText(getActivity(), "Sorting...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
          @Override public void run() {
            if (!item.isChecked()) {
              Collections.sort(adapter.mDataset, newFirstInstallComparator());
              getActivity().runOnUiThread(new Runnable() {
                @Override public void run() {
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

  public Comparator<SelectablePackageInfo> newFirstInstallComparator() {
    return new Comparator<SelectablePackageInfo>() {
      @Override public int compare(SelectablePackageInfo lhs, SelectablePackageInfo rhs) {
        return (int) (rhs.firstInstallTime / 1000 - lhs.firstInstallTime / 1000);
      }
    };
  }

  private void sortByFirstInstall() {
    Toast.makeText(getActivity(), "Sorting by Date", Toast.LENGTH_SHORT).show();

    new Thread(new Runnable() {
      @Override public void run() {
        Collections.sort(adapter.mDataset, newFirstInstallComparator());
        getActivity().runOnUiThread(new Runnable() {
          @Override public void run() {
            adapter.notifyDataSetChanged();
          }
        });
      }
    }).start();
  }

  private MenuItem.OnMenuItemClickListener sortByNameListener() {
    return new MenuItem.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(final MenuItem item) {

        Toast.makeText(getActivity(), "Sorting...", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
          @Override public void run() {
            if (!item.isChecked()) {
              // Carrega as labels necessárias
              PackageManager packageManager = getActivity().getPackageManager();
              for (SelectablePackageInfo selectablePackageInfo : adapter.mDataset)
                selectablePackageInfo.getLabel();

              Collections.sort(adapter.mDataset, newNameComparator());
              getActivity().runOnUiThread(new Runnable() {
                @Override public void run() {
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

  public Comparator<SelectablePackageInfo> newNameComparator() {
    return new Comparator<SelectablePackageInfo>() {
      @Override public int compare(SelectablePackageInfo lhs, SelectablePackageInfo rhs) {
        return lhs.getLabel().compareTo(rhs.getLabel());
      }
    };
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      checked = savedInstanceState.getInt("sortCheckable");
    }
    spiceManagerSecondary.start(getContext());
    setHasOptionsMenu(true);

    userCredentialsJson = (UserCredentialsJson) getArguments().get("userCredentialsJson");
    setAdapter(savedInstanceState, null);
  }

  @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
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

          @Override public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
      }
    });
    LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.store_info);
    TextView textView = (TextView) rootView.findViewById(R.id.store_name);
    textView.setText(" " + userCredentialsJson.getRepo());
    linearLayout.setOnLongClickListener(new View.OnLongClickListener() {

      @Override public boolean onLongClick(View v) {
        PackageInfo pInfo = null;
        try {
          pInfo =
              getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
          String version = pInfo.versionName;
          int versionCode = pInfo.versionCode;
          String appName = pInfo.packageName;

          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
          builder.setMessage("App : "
              + appName
              + "\n"
              + "Version : "
              + version
              + "\n"
              + "Version Code : "
              + versionCode);

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

  @Override public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    DisplayMetrics metrics = getResources().getDisplayMetrics();
    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120,
        getResources().getDisplayMetrics());
    int i = (int) (metrics.widthPixels / px);
    gridview = (GridView) rootView.findViewById(R.id.grid_view);
    gridview.setNumColumns(i);
    adapter.setAdapterView(gridview);
    final SwipeRefreshLayout swipeRefreshLayout =
        (SwipeRefreshLayout) rootView.findViewById(R.id.swipe);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        //call method(s) responsible for creating the list.
        //notifyDataSetChanged on adapter
        adapter.mDataset = nonSystemPackages(true);
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
      }
    });
    setUploadButtonListener();
  }

  @Override public void onStart() {
    super.onStart();

    Intent intent = new Intent(getActivity(), UploadService.class);

    getActivity().startService(intent);
    intent = new Intent(getActivity(), UploadService.class);
    getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override public void onResume() {
    super.onResume();
    getActivity().setTitle(getActivity().getApplicationInfo().labelRes);

    adapter.setListener(new ManelAdapter.ManelAdapterShowListener() {

      final int timeMillis = 100;

      @Override public void show() {
        TranslateAnimation anim =
            new TranslateAnimation(0, 0, 0, rootView.findViewById(R.id.store_icon).getHeight());
        anim.setDuration(timeMillis);
        anim.setFillAfter(true);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

          @Override public void onAnimationStart(Animation animation) {
          }

          @Override public void onAnimationRepeat(Animation animation) {
          }

          @Override public void onAnimationEnd(Animation animation) {
            ViewCompat.setTranslationY(rootView.findViewById(R.id.grid_view_and_hint), 0);
            rootView.findViewById(R.id.grid_view_and_hint).clearAnimation();
          }
        });
        rootView.findViewById(R.id.grid_view_and_hint).startAnimation(anim);
        rootView.findViewById(R.id.submitAppsButton).setVisibility(View.GONE);
      }

      @Override public void hide() {

        TranslateAnimation anim =
            new TranslateAnimation(0, 0, 0, -rootView.findViewById(R.id.store_icon).getHeight());
        anim.setDuration(timeMillis);
        anim.setFillAfter(true);

        anim.setAnimationListener(new TranslateAnimation.AnimationListener() {

          @Override public void onAnimationStart(Animation animation) {
          }

          @Override public void onAnimationRepeat(Animation animation) {
          }

          @Override public void onAnimationEnd(Animation animation) {
            ViewCompat.setTranslationY(rootView.findViewById(R.id.grid_view_and_hint),
                -rootView.findViewById(R.id.store_icon).getHeight());
            rootView.findViewById(R.id.grid_view_and_hint).clearAnimation();
          }
        });
        rootView.findViewById(R.id.grid_view_and_hint).startAnimation(anim);
        rootView.findViewById(R.id.submitAppsButton).setVisibility(View.VISIBLE);
      }
    }, rootView);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putSerializable("userCredentialsJson", userCredentialsJson);
    outState.putInt("sortCheckable", checked);

    adapter.save(outState);
  }

  @Override public void onStop() {
    super.onStop();

    if (mBound) {
      getActivity().unbindService(mConnection);
      mBound = false;
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    spiceManagerSecondary.shouldStop();
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);

    inflater.inflate(R.menu.app_grid_menu, menu);

    menu.findItem(R.id.logout_button).setOnMenuItemClickListener(logoutListener());
  }

  private MenuItem.OnMenuItemClickListener logoutListener() {

    return new MenuItem.OnMenuItemClickListener() {
      @Override public boolean onMenuItemClick(MenuItem item) {

        ConfirmationDialog confirmationDialog = new ConfirmationDialog();
        confirmationDialog.setTargetFragment(FragmentAppView.this, 0);
        confirmationDialog.show(getFragmentManager(), "confirmDialog");

        return false;
      }
    };
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
  }

  private void setUploadButtonListener() {
    rootView.findViewById(R.id.submitAppsButton).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        ArrayList<SelectablePackageInfo> selectablePackageInfos =
            new ArrayList<>(adapter.getCheckedItemCount());
        for (Long aLong : adapter.getCheckedItems()) {
          selectablePackageInfos.add(adapter.getItem(aLong.intValue()));
        }

        for (final SelectablePackageInfo selectablePackageInfo : selectablePackageInfos) {
          GetProposedRequest getProposedRequest =
              new GetProposedRequest(Utils.getLanguage(), selectablePackageInfo.packageName);
          spiceManagerSecondary.execute(getProposedRequest,
              new RequestListener<GetProposedResponse>() {
                @Override public void onRequestFailure(SpiceException spiceException) {
                  //TODO: what to do?
                }

                @Override public void onRequestSuccess(GetProposedResponse getProposedResponse) {
                  //Show submitappfragment with form with content that came from getProposed
                  String language = Utils.getLanguage();
                  List<GetProposedResponse.Data> dataList = getProposedResponse.data;

                  if (!dataList.isEmpty()) {
                    //check if present language exists, if not, check default (en) in response
                    //compare local language with languages received from webservice and send correct strings???
                    Fragment fragment = SubmitAppFragment.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("userCredentialsJson", userCredentialsJson);

                    ArrayList<SelectablePackageInfo> selectablePackageInfos = new ArrayList<>(1);
                    selectablePackageInfos.add(selectablePackageInfo);
                    bundle.putParcelableArrayList("selectableAppNames", selectablePackageInfos);
                    bundle.putString("title", dataList.get(0).getTitle());
                    bundle.putString("description", dataList.get(0).getDescription());
                    bundle.putBoolean("from_appview", true);
                    fragment.setArguments(bundle);
                    getChildFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.app_view_fragment, fragment)
                        .commit();
                  } else {
                    //No proposed translations available call upload
                    try {
                      mService.prepareUploadAndSend(userCredentialsJson, selectablePackageInfo);
                    } catch (ValidationException e) {
                      e.printStackTrace();
                    }
                  }
                }
              });
        }
        adapter.uncheckAll();
        Toast.makeText(getActivity(), "Sending app in the background", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void prepareSpinner(int viewId, int arrayId) {
    Spinner spinner = (Spinner) rootView.findViewById(viewId);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getActivity(), arrayId, R.layout.spinner_item);

    String[] stringArray = getResources().getStringArray(arrayId);

    ArrayAdapter<CharSequence> a =
        new ArrayAdapter<CharSequence>(getActivity(), R.layout.spinner_item, stringArray) {
          @Override public int getCount() {
            int count = super.getCount();
            return count > 0 ? count - 1 : count;
          }
        };

    // Specify the layout to use when the list of choices appears
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
    spinner.setAdapter(adapter);
  }

  private void sortByLastInstall() {
    Toast.makeText(getActivity(), "Sorting by Date", Toast.LENGTH_SHORT).show();

    new Thread(new Runnable() {
      @Override public void run() {
        Collections.sort(adapter.mDataset, newLastInstallComparator());
        getActivity().runOnUiThread(new Runnable() {
          @Override public void run() {
            adapter.notifyDataSetChanged();
          }
        });
      }
    }).start();
  }

  private void sortByName() {
    Toast.makeText(getActivity(), "Sorting by Name", Toast.LENGTH_SHORT).show();

    new Thread(new Runnable() {
      @Override public void run() {
        // Carrega as labels necessárias
        PackageManager packageManager = getActivity().getPackageManager();
        for (SelectablePackageInfo selectablePackageInfo : adapter.mDataset)
          selectablePackageInfo.getLabel();

        Collections.sort(adapter.mDataset, newNameComparator());
        getActivity().runOnUiThread(new Runnable() {
          @Override public void run() {
            adapter.notifyDataSetChanged();
          }
        });
      }
    }).start();
  }

  private void setAdapter(Bundle savedInstanceState, final View view) {

    adapter = new ManelAdapter(savedInstanceState, view, this, nonSystemPackages(true),
        userCredentialsJson);

    adapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        ((MultiChoiceAdapter) parent.getAdapter()).setItemChecked(id, true);
      }
    });
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

  private boolean isSystemUpdatedPackage(PackageInfo packageInfo) {
    int maskUpdade = ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
    return (packageInfo.applicationInfo.flags & maskUpdade) != 0;
  }

  private boolean isSystemPackage(PackageInfo packageInfo) {
    return ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
  }

  public Comparator<SelectablePackageInfo> newLastInstallComparator() {
    return new Comparator<SelectablePackageInfo>() {
      @Override public int compare(SelectablePackageInfo lhs, SelectablePackageInfo rhs) {
        return (int) (getLastInstallDate(rhs) / 1000 - getLastInstallDate(lhs) / 1000);
      }
    };
  }

  private long getLastInstallDate(PackageInfo packageInfo) {
    PackageManager pm = getContext().getPackageManager();
    String appFile = packageInfo.applicationInfo.sourceDir;
    return new File(appFile).lastModified(); //Epoch Time
  }
}
