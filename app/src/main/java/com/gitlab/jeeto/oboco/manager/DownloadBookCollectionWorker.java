package com.gitlab.jeeto.oboco.manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.BookCollectionDto;
import com.gitlab.jeeto.oboco.client.BookDto;
import com.gitlab.jeeto.oboco.client.PageableListDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import okhttp3.ResponseBody;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DownloadBookCollectionWorker extends Worker {
    private final static String CHANNEL_ID = "download";
    private final static String CHANNEL_NAME = "download";
    private final static String CHANNEL_DESCRIPTION = "download";
    private final static int NOTIFICATION_ID = (int) System.currentTimeMillis();
    private NotificationManager notificationManager;

    public static WorkRequest createDownloadWorkRequest(Long downloadId, String downloadName) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build();

        WorkRequest downloadWorkRequest =
                new OneTimeWorkRequest.Builder(DownloadBookCollectionWorker.class)
                        .setConstraints(constraints)
                        .addTag("download")
                        .addTag("type:" + DownloadWorkType.BOOK_COLLECTION.name())
                        .addTag("createDate:" + new Date().getTime())
                        .addTag("downloadId:" + downloadId)
                        .addTag("downloadName:" + downloadName)
                        .setInputData(
                                new Data.Builder()
                                        .putLong("bookCollectionId",  downloadId)
                                        .build()
                        )
                        .build();
        return downloadWorkRequest;
    }

    public DownloadBookCollectionWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public Result doWork() {
        Data data = getInputData();

        Long bookCollectionId = data.getLong("bookCollectionId", 0);

        if(bookCollectionId == 0) {
            return Result.failure();
        }

        try {
            download(bookCollectionId);
        } catch(Exception e) {
            //e.printStackTrace();

            return Result.failure();
        }

        return Result.success();
    }

    private ApplicationService createApplicationService(Context context) {
        SharedPreferences sp = context.getSharedPreferences("application", Context.MODE_PRIVATE);
        String baseUrl = sp.getString("baseUrl", "");

        AuthenticationManager authenticationManager = new AuthenticationManager(context);

        ApplicationService applicationService = new ApplicationService(context, baseUrl, authenticationManager);

        return applicationService;
    }

    private void download(Long bookCollectionId) throws Exception {
        Context context = getApplicationContext();

        long now = new Date().getTime();
        for(File file: context.getExternalCacheDir().listFiles()) {
            if(file.isFile() && file.getName().endsWith(".download")) {
                // 1 day = 24 * 60 * 60 * 1000
                if((now - file.lastModified()) > 86400000) {
                    file.delete();
                }
            }
        }

        ApplicationService applicationService = createApplicationService(context);

        BookCollectionDto bookCollection = applicationService.getBookCollection(bookCollectionId, "()").blockingGet();

        Integer page = 1;
        Integer pageSize = 100;

        do {
            if(isStopped()) {
                break;
            }

            PageableListDto<BookDto> bookPageableList = applicationService.getBooksByBookCollection(bookCollectionId, page, pageSize, "(bookMark)").blockingGet();
            for(BookDto book: bookPageableList.getElements()) {
                if(isStopped()) {
                    break;
                }

                setForegroundAsync(createForegroundInfo(book.getName()));

                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), bookCollection.getName() + File.separator  + book.getName() +  ".cbz");

                if(file.isFile() == false) {
                    ResponseBody responseBody = applicationService.downloadBook(book.getId()).blockingGet();

                    File downloadFile = new File(context.getExternalCacheDir(), book.getName() +  ".cbz."  + now + ".download");

                    try {
                        InputStream inputStream = null;
                        OutputStream outputStream = null;
                        try {
                            inputStream = responseBody.byteStream();
                            outputStream = new FileOutputStream(downloadFile, false);

                            byte[] buffer = new byte[8 * 1024];
                            int bufferSize;
                            while ((bufferSize = inputStream.read(buffer)) != -1) {
                                if (isStopped()) {
                                    break;
                                }

                                outputStream.write(buffer, 0, bufferSize);
                            }
                            outputStream.flush();
                        } finally {
                            if (outputStream != null) {
                                try {
                                    outputStream.close();
                                } catch (Exception e) {
                                    // pass
                                }
                            }
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (Exception e) {
                                    // pass
                                }
                            }
                        }
                    } catch(Exception e) {
                        downloadFile.delete();

                        throw e;
                    }

                    if(isStopped()) {
                        downloadFile.delete();
                    } else {
                        File parentFile = file.getParentFile();
                        if (parentFile.isDirectory() == false) {
                            parentFile.mkdirs();
                        }
                        downloadFile.renameTo(file);
                    }
                }
            }

            page = bookPageableList.getNextPage();
        } while(page != null);
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String info) {
        Context context = getApplicationContext();

        String title = context.getResources().getString(R.string.download_book_collection_worker_download);
        String actionTitle = context.getResources().getString(R.string.download_book_collection_worker_download_stop);

        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setTicker(title)
                .setContentTitle(title)
                .setContentText(info)
                .setSmallIcon(R.drawable.notification_action_background)
                .setOngoing(true)
                .setSilent(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(R.drawable.outline_remove_black_24, actionTitle, intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notificationBuilder, CHANNEL_ID);
        }

        return new ForegroundInfo(NOTIFICATION_ID, notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(NotificationCompat.Builder notificationBuilder, String id) {
        Context context = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);

        NotificationChannel channel = new NotificationChannel(id, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(CHANNEL_DESCRIPTION);

        notificationManager.createNotificationChannel(channel);
    }

}

