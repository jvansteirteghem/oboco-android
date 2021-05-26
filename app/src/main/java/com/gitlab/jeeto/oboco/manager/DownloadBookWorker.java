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
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.BookDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DownloadBookWorker extends Worker {
    private NotificationManager notificationManager;

    public DownloadBookWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        notificationManager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public Result doWork() {
        Data data = getInputData();

        Long bookId = data.getLong("bookId", 0);

        if(bookId == 0) {
            return Result.failure();
        }

        try {
            download(bookId);
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

    private void download(Long bookId) throws Exception {
        // Downloads a file and updates bytes read
        // Calls setForegroundAsync(createForegroundInfo(myProgress))
        // periodically when it needs to update the ongoing Notification.
        Context context = getApplicationContext();

        ApplicationService applicationService = createApplicationService(context);

        BookDto book = applicationService.getBook(bookId, "(bookCollection)").blockingGet();

        setForegroundAsync(createForegroundInfo(book.getName()));

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getBookCollection().getName() + File.separator  + book.getName() +  ".cbz");

        if(file.isFile() == false) {
            ResponseBody responseBody = applicationService.downloadBook(bookId).blockingGet();

            File parentFile = file.getParentFile();
            if (parentFile.isDirectory() == false) {
                parentFile.mkdirs();
            }

            File downloadFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), book.getBookCollection().getName() + File.separator  + "DOWNLOAD_" + book.getName() +  ".cbz");

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = responseBody.byteStream();
                outputStream = new FileOutputStream(downloadFile, false);

                byte[] buffer = new byte[8 * 1024];
                int bufferSize;
                while ((bufferSize = inputStream.read(buffer)) != -1) {
                    if(isStopped()) {
                        break;
                    }

                    outputStream.write(buffer, 0, bufferSize);
                }
                outputStream.flush();
            } finally {
                if(outputStream != null) {
                    try {
                        outputStream.close();
                    } catch(Exception e) {
                        // pass
                    }
                }
                if(inputStream != null) {
                    try {
                        inputStream.close();
                    } catch(Exception e) {
                        // pass
                    }
                }
            }

            if(isStopped()) {
                downloadFile.delete();
            } else {
                downloadFile.renameTo(file);
            }
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(@NonNull String info) {
        Context context = getApplicationContext();

        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "workDownload")
                .setTicker("Downloading book")
                .setContentTitle("Downloading book")
                .setContentText(info)
                .setSmallIcon(R.drawable.notification_action_background)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(android.R.drawable.ic_delete, "Cancel", intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(notification, "workDownload");
        }

        return new ForegroundInfo(1, notification.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(NotificationCompat.Builder notificationBuilder, String id) {
        Context context = getApplicationContext();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        NotificationChannel channel = new NotificationChannel(id, "WorkManagerApp", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("WorkManagerApp Notifications");

        notificationManager.createNotificationChannel(channel);
    }

}

