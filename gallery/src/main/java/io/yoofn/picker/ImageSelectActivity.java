package io.yoofn.picker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.autolayout.AutoLayoutActivity;

import java.util.ArrayList;
import java.util.List;

import io.yoofn.picker.control.AlbumCollection;
import io.yoofn.picker.control.PictureCollection;
import io.yoofn.picker.control.SelectedUriCollection;
import io.yoofn.picker.model.Album;
import io.yoofn.picker.model.SelectionSpec;
import io.yoofn.picker.utils.BundleUtils;
import io.yoofn.picker.utils.MediaStoreCompat;
import pub.devrel.easypermissions.EasyPermissions;


public class ImageSelectActivity extends AutoLayoutActivity implements AlbumCollection.OnDirectorySelectListener, EasyPermissions.PermissionCallbacks {

    public static final String EXTRA_RESULT_SELECTION = BundleUtils.buildKey(ImageSelectActivity.class, "EXTRA_RESULT_SELECTION");
    public static final String EXTRA_SELECTION_SPEC = BundleUtils.buildKey(ImageSelectActivity.class, "EXTRA_SELECTION_SPEC");
    public static final String EXTRA_RESUME_LIST = BundleUtils.buildKey(ImageSelectActivity.class, "EXTRA_RESUME_LIST");
    //    public static final String EXTRA_BUNDLE = BundleUtils.buildKey(ImageSelectActivity.class, "EXTRA_BUNDLE");
    public static final String EXTRA_BUNDLE = "EXTRA_BUNDLE";
    //    public static final String EXTRA_ENGINE = BundleUtils.buildKey(ImageSelectActivity.class, "EXTRA_ENGINE");

    public static final String STATE_CAPTURE_PHOTO_URI = BundleUtils.buildKey(ImageSelectActivity.class, "STATE_CAPTURE_PHOTO_URI");

    private RelativeLayout rlTop;
    private TextView mFoldName;
    private View mListViewGroup;
    private ListView mListView;
    private GridView mGridView;
    public static final int REQUEST_CODE_CAPTURE = 3;
    private MediaStoreCompat mMediaStoreCompat;
    private Button commit;
    private ImageView galleryTip;
    private SelectionSpec selectionSpec;
    private ImageView btnBack;
    private AlbumCollection albumCollection = new AlbumCollection();
    private final PictureCollection mPhotoCollection = new PictureCollection();
    private final SelectedUriCollection mCollection = new SelectedUriCollection(this);
    private String mCapturePhotoUriHolder;
    public static final int REQUEST_CODE_PERMS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        requestPermissions(savedInstanceState);

    }


    public void setResult() {
        Intent intent = getIntent();
        intent.putParcelableArrayListExtra(ImageSelectActivity.EXTRA_RESULT_SELECTION,
                (ArrayList<? extends Parcelable>) mCollection.asList());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    View.OnClickListener mOnClickFoldName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListViewGroup.getVisibility() == View.VISIBLE) {
                hideFolderList();
            } else {
                showFolderList();
            }
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mCollection.onSaveInstanceState(outState);
        albumCollection.onSaveInstanceState(outState);
        outState.putString(STATE_CAPTURE_PHOTO_URI, mCapturePhotoUriHolder);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        albumCollection.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Uri captured = mMediaStoreCompat.getCapturedPhotoUri(data, mCapturePhotoUriHolder);
            if (captured != null) {
                mCollection.add(captured);
                mMediaStoreCompat.cleanUp(mCapturePhotoUriHolder);
                if (mCollection.isSingleChoose()) {
                    setResult();
                }
            }
        }
        if (requestCode == 21) {
            Uri uri = data.getParcelableExtra("cc");
            boolean isSelected = data.getBooleanExtra("dd", false);
            if (isSelected) {
                mCollection.add(uri);
            } else {
                mCollection.remove(uri);
            }
            ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
            //mGridView.postInvalidate();
        }

    }

    private void requestPermissions(Bundle savedInstanceState) {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (EasyPermissions.hasPermissions(this, perms)) {
            pass(savedInstanceState);
        } else {
            EasyPermissions.requestPermissions(this, "程序需要读取你的照片哦！", REQUEST_CODE_PERMS, perms);
        }
    }

    private void pass(Bundle savedInstanceState) {
        mCapturePhotoUriHolder = savedInstanceState != null ? savedInstanceState.getString(STATE_CAPTURE_PHOTO_URI) : "";
        selectionSpec = getIntent().getParcelableExtra(ImageSelectActivity.EXTRA_SELECTION_SPEC);
        mMediaStoreCompat = new MediaStoreCompat(this, new Handler(Looper.getMainLooper()));

        mCollection.onCreate(savedInstanceState);
        mCollection.prepareSelectionSpec(selectionSpec);
        mCollection.setDefaultSelection(getIntent().<Uri>getParcelableArrayListExtra(EXTRA_RESUME_LIST));
        mCollection.setOnSelectionChange((maxCount, selectCount) -> commit.setText("确定(" + selectCount + "/" + maxCount + ")"));

        mGridView = (GridView) findViewById(R.id.gridView);

        mListView = (ListView) findViewById(R.id.listView);
        btnBack = (ImageView) findViewById(R.id.btn_back);
        mListViewGroup = findViewById(R.id.listViewParent);
        mListViewGroup.setOnClickListener(mOnClickFoldName);
        mFoldName = (TextView) findViewById(R.id.foldName);
        galleryTip = (ImageView) findViewById(R.id.gallery_tip);
        LinearLayout selectFold = (LinearLayout) findViewById(R.id.selectFold);
        commit = (Button) findViewById(R.id.commit);
        commit.setText("确定(0/" + selectionSpec.getMaxSelectable() + ")");
        if (selectionSpec.isSingleChoose()) {
            commit.setVisibility(View.GONE);
        }
        mFoldName.setText("最近图片");
        selectFold.setOnClickListener(mOnClickFoldName);

        albumCollection.onCreate(ImageSelectActivity.this, this, selectionSpec, mListView);
        albumCollection.loadAlbums();
        mPhotoCollection.onCreate(ImageSelectActivity.this, mGridView, mCollection, selectionSpec);
        mPhotoCollection.loadAllPhoto();

        commit.setOnClickListener(v -> {
            if (mCollection.isEmpty()) {
                Toast.makeText(getApplicationContext(), "未选择图片", Toast.LENGTH_LONG).show();
            } else {
                setResult();
            }
        });
        btnBack.setOnClickListener(v -> finish());

        if (selectionSpec.willStartCamera())
            showCameraAction();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        pass(null);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Toast.makeText(this, "你需要手动开启权限，否则无法读取手机相册", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    public void prepareCapture(String uri) {
        mCapturePhotoUriHolder = uri;
    }

    private void showFolderList() {
        galleryTip.setImageResource(R.drawable.gallery_up);
        mListViewGroup.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.listview_up);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.listview_fade_in);

        mListView.startAnimation(animation);
        mListViewGroup.startAnimation(fadeIn);
        //mListViewGroup.setVisibility(View.VISIBLE);
    }

    private void hideFolderList() {
        galleryTip.setImageResource(R.drawable.gallery_down);
        Animation animation = AnimationUtils.loadAnimation(ImageSelectActivity.this, R.anim.listview_down);
        Animation fadeOut = AnimationUtils.loadAnimation(ImageSelectActivity.this, R.anim.listview_fade_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mListViewGroup.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mListView.startAnimation(animation);
        mListViewGroup.startAnimation(fadeOut);
    }

    public MediaStoreCompat getMediaStoreCompat() {
        return mMediaStoreCompat;
    }

    @Override
    protected void onDestroy() {
        mMediaStoreCompat.destroy();
        albumCollection.onDestroy();
        mPhotoCollection.onDestroy();
        super.onDestroy();
    }

    /**
     * 选择相机
     */
    public void showCameraAction() {
        mCapturePhotoUriHolder = mMediaStoreCompat.invokeCameraCapture(this, ImageSelectActivity.REQUEST_CODE_CAPTURE);
    }

    @Override
    public void onSelect(Album album) {
        hideFolderList();
        mFoldName.setText(album.getDisplayName(this));
        mPhotoCollection.resetLoad(album);
    }

    @Override
    public void onReset(Album album) {
        mPhotoCollection.load(album);
    }

}
