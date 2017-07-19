package io.yoofn.picker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhy.autolayout.AutoLayoutActivity;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by kalshen on 17/5/4.
 *
 * @描述 &{TODO}
 */

public class PictureDetailActivity extends AutoLayoutActivity {

    private ImageView mIv_select;
    private PhotoView mLargePicture;
    private Uri uri;
    private boolean isSelected;
    private boolean isCountOver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_detail);
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("aa");
        isSelected = intent.getBooleanExtra("bb", false);
        isCountOver = intent.getBooleanExtra("ee", false);
        mLargePicture = (PhotoView) findViewById(R.id.picture_detail);
        mIv_select = (ImageView) findViewById(R.id.iv_select);

        mIv_select.setImageResource(isSelected ? R.drawable.pick_photo_checkbox_check : R.drawable.pick_photo_checkbox_normal);
        mLargePicture.setImageURI(uri);
        mIv_select.setOnClickListener(v -> {
            if (!isSelected && isCountOver) {
                Toast.makeText(this, "超过数量限制", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                mIv_select.setImageResource(isSelected ? R.drawable.pick_photo_checkbox_normal : R.drawable.pick_photo_checkbox_check);
                Intent data = new Intent();
                data.putExtra("cc", uri);
                data.putExtra("dd", isSelected=!isSelected);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("cc", uri);
        data.putExtra("dd", isSelected);
        setResult(RESULT_OK, data);
        finish();
    }
}
