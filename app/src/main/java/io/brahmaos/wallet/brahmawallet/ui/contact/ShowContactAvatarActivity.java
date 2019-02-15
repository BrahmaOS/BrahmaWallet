package io.brahmaos.wallet.brahmawallet.ui.contact;


import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.ImageUtil;

/**
 * Show contact avatar.
 */
public class ShowContactAvatarActivity extends BaseActivity {

    @Override
    protected String tag() {
        return ShowContactAvatarActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        setContentView(R.layout.activity_show_contact_avatar);
        showNavBackBtn();

        int contactId = getIntent().getIntExtra(IntentParam.PARAM_CONTACT_ID, 0);
        if (0 == contactId) {
            BLog.w(tag(), "can not get <contact-id>");
            finish();
            return;
        }

        ContactViewModel mViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        ImageView ivContactAvatar = findViewById(R.id.contact_avatar_iv);
        mViewModel.getContactById(contactId).observe(this, (ContactEntity contactEntity) -> {
            if (contactEntity != null) {
                if (contactEntity.getAvatar() != null && contactEntity.getAvatar().length() > 0 && !contactEntity.getAvatar().equals("null")) {
                    Uri uriAvatar = Uri.parse(contactEntity.getAvatar());
                    try {
                        Bitmap bmpAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(), uriAvatar);
                        ivContactAvatar.setImageBitmap(bmpAvatar);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "show contact photo failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
