package is.xyz.mpv;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;

import is.xyz.filepicker.FilePickerFragment;

import java.io.File;

public class MPVFilePickerFragment extends FilePickerFragment {

    private File rootPath = new File("/");
    private VideoThumbnailLoader thumbnailLoader = null;

    @Override
    public void onBindViewHolder(@NonNull DirViewHolder vh, int position, @NonNull File data) {
        super.onBindViewHolder(vh, position, data);

        if (thumbnailLoader == null)
            thumbnailLoader = new VideoThumbnailLoader(requireContext());

        ImageView icon = (ImageView) vh.icon;
        if (isDir(data))
            thumbnailLoader.bindDirectoryIcon(icon);
        else
            thumbnailLoader.bindFile(icon, data);
    }

    @Override
    public void onClickCheckable(@NonNull View v, @NonNull FileViewHolder vh) {
        mListener.onFilePicked(vh.file);
    }

    @Override
    public boolean onLongClickCheckable(@NonNull View v, @NonNull DirViewHolder vh) {
        mListener.onDirPicked(vh.file);
        return true;
    }

    @NonNull
    @Override
    public File getRoot() {
        return rootPath;
    }

    public void setRoot(@NonNull File path) {
        rootPath = path;
    }

    public boolean isBackTop() {
        return mCurrentPath.equals(getRoot());
    }

    private @NonNull String makeRelative(@NonNull String path) {
        String head = getRoot().toString();
        if (path.equals(head))
            return "";
        if (!head.endsWith("/"))
            head += "/";
        return path.startsWith(head) ? path.substring(head.length()) : path;
    }

    @Override
    public void onChangePath(File file) {
        ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (file != null && bar != null)
            bar.setSubtitle(makeRelative(file.getPath()));
    }

    @Override
    public void onDestroy() {
        if (thumbnailLoader != null) {
            thumbnailLoader.shutdown();
            thumbnailLoader = null;
        }
        super.onDestroy();
    }
}
