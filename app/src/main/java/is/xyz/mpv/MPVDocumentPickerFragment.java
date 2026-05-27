package is.xyz.mpv;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import is.xyz.filepicker.DocumentPickerFragment;

public class MPVDocumentPickerFragment extends DocumentPickerFragment {
    private VideoThumbnailLoader thumbnailLoader = null;

    public MPVDocumentPickerFragment(@NonNull Uri root) {
        super(root);
    }

    @Override
    public void onBindViewHolder(@NonNull DirViewHolder vh, int position, @NonNull Uri data) {
        super.onBindViewHolder(vh, position, data);

        if (thumbnailLoader == null)
            thumbnailLoader = new VideoThumbnailLoader(requireContext());

        ImageView icon = (ImageView) vh.icon;
        if (isDir(data))
            thumbnailLoader.bindDirectoryIcon(icon);
        else
            thumbnailLoader.bindDocument(icon, data, getName(data));
    }

    @Override
    public void onClickCheckable(@NonNull View view, @NonNull FileViewHolder vh) {
        mListener.onDocumentPicked(vh.file, false);
    }

    @Override
    public boolean onLongClickCheckable(@NonNull View view, @NonNull DirViewHolder vh) {
        mListener.onDocumentPicked(vh.file, true);
        return true;
    }

    public boolean isBackTop() {
        return mCurrentPath.equals(getRoot());
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
