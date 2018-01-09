package pt.caixamagica.aptoide.uploader;

import android.content.Context;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import pt.caixamagica.aptoide.uploader.retrofit.request.UploadedAppsRequest;
import pt.caixamagica.aptoide.uploader.util.AppsInStorePersister;
import pt.caixamagica.aptoide.uploader.util.InstalledUtils;
import pt.caixamagica.aptoide.uploader.util.Md5AsyncUtils;
import pt.caixamagica.aptoide.uploader.util.StoredCredentialsManager;
import pt.caixamagica.aptoide.uploader.webservices.json.UploadedAppsJson;
import pt.caixamagica.aptoide.uploader.webservices.json.UserCredentialsJson;

/**
 * Created by filipe on 15-11-2017.
 */

public class AppsInStoreController {

  private SpiceManager spiceManager;
  private AppsInStorePersister appsInStorePersister;
  private InstalledUtils installedUtils;
  private Md5AsyncUtils md5AsyncUtils;
  private Context applicationContext;
  private double BUFFER_SIZE = 5;
  private ExecutorService executorService;

  public AppsInStoreController(SpiceManager spiceManager, AppsInStorePersister appsInStorePersister,
      InstalledUtils installedUtils, Md5AsyncUtils md5AsyncUtils, Context applicationContext) {
    this.spiceManager = spiceManager;
    this.appsInStorePersister = appsInStorePersister;
    this.installedUtils = installedUtils;
    this.md5AsyncUtils = md5AsyncUtils;
    this.applicationContext = applicationContext;
  }

  public void start() {
    spiceManager.start(applicationContext);

    executorService = Executors.newSingleThreadExecutor();
    executorService.submit(new Thread(new Runnable() {
      @Override public void run() {
        refreshInstalledAppsMd5List();
      }
    }));
  }

  private void refreshInstalledAppsMd5List() {
    List<SelectablePackageInfo> selectablePackageInfos =
        installedUtils.getNonUploadedNonSystemPackages(false);

    Md5AsyncUtils.OnNewUploadedApps newUploadedAppsListener =
        createMd5Listener(selectablePackageInfos.size());

    md5AsyncUtils.computeMd5(selectablePackageInfos, newUploadedAppsListener);
  }

  private Md5AsyncUtils.OnNewUploadedApps createMd5Listener(final int size) {
    return new Md5AsyncUtils.OnNewUploadedApps() {
      private AtomicInteger atomicInteger = new AtomicInteger(0);
      private ConcurrentLinkedQueue<Md5AsyncUtils.Model> concurrentLinkedQueue =
          new ConcurrentLinkedQueue<>();

      @Override public void onNewUploadedApps(Md5AsyncUtils.Model model) {
        concurrentLinkedQueue.add(model);
        int count = atomicInteger.incrementAndGet();
        if (count % BUFFER_SIZE == 0 || count == size) {
          List<Md5AsyncUtils.Model> modelList = createModelList();
          sendUploadedAppsRequest(modelList);
        }
      }

      private List<Md5AsyncUtils.Model> createModelList() {
        List<Md5AsyncUtils.Model> list = new LinkedList<>();

        double queueSize = Math.min(BUFFER_SIZE, concurrentLinkedQueue.size());
        for (int i = 0; i < queueSize; i++) {
          if (!concurrentLinkedQueue.isEmpty()) {
            list.add(concurrentLinkedQueue.poll());
          }
        }

        return list;
      }

      private void sendUploadedAppsRequest(List<Md5AsyncUtils.Model> modelList) {
        UserCredentialsJson storedUserCredentials =
            new StoredCredentialsManager(applicationContext).getStoredUserCredentials();

        String token = storedUserCredentials.getToken();
        String storeName = storedUserCredentials.getRepo();

        spiceManager.execute(
            new UploadedAppsRequest(token, storeName, createMd5StringList(modelList)),
            new RequestListener<UploadedAppsJson>() {
              @Override public void onRequestFailure(SpiceException spiceException) {
                spiceException.printStackTrace();
              }

              @Override public void onRequestSuccess(UploadedAppsJson uploadedAppsJson) {
                System.out.println("request was successful");

                List<UploadedAppsJson.DataList.App> remoteAppsList =
                    uploadedAppsJson.datalist.getList();

                List<UploadedApp> listOfAppsInStore = new ArrayList<>();
                for (int i = 0; i < uploadedAppsJson.datalist.getList()
                    .size(); i++) {
                  listOfAppsInStore.add(new UploadedApp(remoteAppsList.get(i)
                      .getFile()
                      .getPackageName()
                      .getName(), remoteAppsList.get(i)
                      .getFile()
                      .getVercode()));
                }
                updateStoredUploadedApps(listOfAppsInStore);
              }
            });
      }
    };
  }

  private void updateStoredUploadedApps(List<UploadedApp> listOfAppsInStore) {
    appsInStorePersister.saveUploadedApps(listOfAppsInStore);
  }

  private List<String> createMd5StringList(List<Md5AsyncUtils.Model> modelList) {
    String str = "[";

    for (Md5AsyncUtils.Model model : modelList) {
      str += model.getMd5sum() + ",";
    }

    char[] chars = str.toCharArray();
    chars[str.length() - 1] = ']';

    List<String> strings = new LinkedList<>();
    for (Md5AsyncUtils.Model model : modelList) {
      strings.add(model.getMd5sum());
    }

    return strings;
  }

  public void stop() {
    if (spiceManager != null && spiceManager.isStarted()) {
      spiceManager.shouldStop();
    }
    if (executorService != null) {
      executorService.shutdown();
    }
  }
}
