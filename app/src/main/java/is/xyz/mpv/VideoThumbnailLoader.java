package is.xyz.mpv;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.LruCache;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class VideoThumbnailLoader {
    private static final int THUMBNAIL_SIZE_DP = 56;
    private static final int MAX_CACHE_SIZE_KB = 8 * 1024;
    private static final Set<String> VIDEO_EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "264", "265", "266", "3g2", "3gp", "3gp2", "3gpp", "3gpp2", "asf", "asx",
            "av1", "avc", "avf", "avi", "bdm", "bdmv", "clpi", "cpi", "divx", "dv",
            "evo", "evob", "f4v", "flc", "fli", "flic", "flv", "gxf", "h264", "h265",
            "h266", "hdmov", "hdv", "hevc", "lrv", "m1u", "m1v", "m2t", "m2ts",
            "m2v", "m4u", "m4v", "mk3d", "mkv", "mj2", "mov", "mp2v", "mp4",
            "mp4v", "mpe", "mpeg", "mpeg2", "mpeg4", "mpg", "mpg4", "mpv", "mpv2",
            "mts", "mtv", "mxf", "mxu", "nsv", "nut", "ogm", "ogv", "ogx", "qt",
            "qtvr", "rm", "rmj", "rmm", "rms", "rmvb", "rmx", "rv", "rvx", "sdp",
            "tod", "trp", "ts", "tsa", "tsv", "tts", "vc1", "vfw", "vob", "vro",
            "vvc", "webm", "wm", "wmv", "wmx", "x264", "x265", "xvid", "y4m", "yuv"
    )));

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final LruCache<String, Bitmap> cache;
    private final Object pendingLock = new Object();
    private final Map<String, List<WeakReference<ImageView>>> pending = new HashMap<>();
    private final Set<String> failed = Collections.synchronizedSet(new HashSet<>());
    private final int thumbnailSizePx;
    private final @Nullable ColorStateList iconTint;

    VideoThumbnailLoader(@NonNull Context context) {
        appContext = context.getApplicationContext();
        thumbnailSizePx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                THUMBNAIL_SIZE_DP,
                context.getResources().getDisplayMetrics()
        );
        iconTint = resolveIconTint(context);

        int memoryKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSizeKb = Math.max(1024, Math.min(memoryKb / 16, MAX_CACHE_SIZE_KB));
        cache = new LruCache<String, Bitmap>(cacheSizeKb) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
                return Math.max(1, value.getByteCount() / 1024);
            }
        };
    }

    void bindDirectoryIcon(@NonNull ImageView imageView) {
        imageView.setTag(null);
        imageView.setVisibility(View.VISIBLE);
        imageView.setBackground(null);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageTintList(iconTint);
        imageView.setImageResource(R.drawable.nnf_ic_folder_black_48dp);
    }

    void bindFile(@NonNull ImageView imageView, @NonNull File file) {
        if (!isVideoName(file.getName())) {
            hideIcon(imageView);
            return;
        }

        String key = file.getAbsolutePath();
        bindVideo(imageView, key, () -> loadFileThumbnail(file));
    }

    void bindDocument(@NonNull ImageView imageView, @NonNull Uri uri, @NonNull String displayName) {
        if (!isVideoName(displayName)) {
            hideIcon(imageView);
            return;
        }

        bindVideo(imageView, uri.toString(), () -> loadDocumentThumbnail(uri));
    }

    void shutdown() {
        executor.shutdownNow();
        cache.evictAll();
        synchronized (pendingLock) {
            pending.clear();
        }
        failed.clear();
    }

    private void bindVideo(@NonNull ImageView imageView, @NonNull String key, @NonNull ThumbnailTask task) {
        imageView.setTag(key);
        imageView.setVisibility(View.VISIBLE);
        imageView.setBackgroundColor(ContextCompat.getColor(appContext, R.color.alpha12pct));

        Bitmap cached = cache.get(key);
        if (cached != null) {
            showThumbnail(imageView, cached);
            return;
        }

        showVideoPlaceholder(imageView);
        if (failed.contains(key)) {
            return;
        }

        WeakReference<ImageView> imageViewRef = new WeakReference<>(imageView);
        synchronized (pendingLock) {
            List<WeakReference<ImageView>> targets = pending.get(key);
            if (targets != null) {
                targets.add(imageViewRef);
                return;
            }

            targets = new ArrayList<>();
            targets.add(imageViewRef);
            pending.put(key, targets);
        }

        executor.execute(() -> {
            Bitmap bitmap = task.load();
            if (bitmap != null) {
                cache.put(key, bitmap);
            } else {
                failed.add(key);
            }

            List<WeakReference<ImageView>> targets;
            synchronized (pendingLock) {
                targets = pending.remove(key);
            }

            final List<WeakReference<ImageView>> finalTargets = targets;
            mainHandler.post(() -> {
                if (finalTargets == null) {
                    return;
                }
                for (WeakReference<ImageView> ref : finalTargets) {
                    ImageView target = ref.get();
                    if (target == null || !key.equals(target.getTag())) {
                        continue;
                    }
                    if (bitmap != null) {
                        showThumbnail(target, bitmap);
                    } else {
                        showVideoPlaceholder(target);
                    }
                }
            });
        });
    }

    private void showVideoPlaceholder(@NonNull ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageTintList(iconTint);
        imageView.setImageResource(R.drawable.round_play_arrow_24);
    }

    private void showThumbnail(@NonNull ImageView imageView, @NonNull Bitmap bitmap) {
        imageView.setImageTintList(null);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
    }

    private void hideIcon(@NonNull ImageView imageView) {
        imageView.setTag(null);
        imageView.setVisibility(View.GONE);
        imageView.setBackground(null);
        imageView.setImageTintList(null);
        imageView.setImageDrawable(null);
    }

    @SuppressWarnings("deprecation")
    private @Nullable Bitmap loadFileThumbnail(@NonNull File file) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return ThumbnailUtils.createVideoThumbnail(
                        file,
                        new Size(thumbnailSizePx, thumbnailSizePx),
                        null
                );
            }
            return ThumbnailUtils.createVideoThumbnail(
                    file.getAbsolutePath(),
                    MediaStore.Video.Thumbnails.MICRO_KIND
            );
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private @Nullable Bitmap loadDocumentThumbnail(@NonNull Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return appContext.getContentResolver().loadThumbnail(
                        uri,
                        new Size(thumbnailSizePx, thumbnailSizePx),
                        (CancellationSignal) null
                );
            }
        } catch (IOException | RuntimeException e) {
            // Fall through to MediaMetadataRetriever for providers without thumbnail support.
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(appContext, uri);
            return scaleThumbnail(retriever.getFrameAtTime(0));
        } catch (RuntimeException e) {
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException | RuntimeException ignored) {
            }
        }
    }

    private @Nullable Bitmap scaleThumbnail(@Nullable Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        return ThumbnailUtils.extractThumbnail(bitmap, thumbnailSizePx, thumbnailSizePx);
    }

    private static boolean isVideoName(@NonNull String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return false;
        }
        String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        return VIDEO_EXTENSIONS.contains(extension);
    }

    private static @Nullable ColorStateList resolveIconTint(@NonNull Context context) {
        TypedValue value = new TypedValue();
        if (!context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, value, true)) {
            return null;
        }
        if (value.resourceId != 0) {
            return ContextCompat.getColorStateList(context, value.resourceId);
        }
        return ColorStateList.valueOf(value.data);
    }

    private interface ThumbnailTask {
        @Nullable Bitmap load();
    }
}
