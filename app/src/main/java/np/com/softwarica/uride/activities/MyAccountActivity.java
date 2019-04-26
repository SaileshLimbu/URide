package np.com.softwarica.uride.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.callbacks.UValueEventListener;
import np.com.softwarica.uride.databinding.ActivityMyAccountBinding;
import np.com.softwarica.uride.utils.DriverUtils;
import np.com.softwarica.uride.utils.FirebaseUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class MyAccountActivity extends AppCompatActivity {
    private final int REQ_CODE_VEHICLE_GALLERY = 001;
    private final int REQ_CODE_VEHICLE_CAMERA = 002;
    private final int REQ_CODE_PROFILE_GALLERY = 003;
    private final int REQ_CODE_PROFILE_CAMERA = 004;
    private ActivityMyAccountBinding binding;
    private Context context;
    private StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_account);

        binding.toolbar.setTitle("My Account");

        context = this;
        storage = FirebaseStorage.getInstance().getReference().child("drivers");

        setupClickEvents();
        getDriverProfile();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    private void setupClickEvents() {
        binding.txtUpdatePhoto.setOnClickListener(v -> choosePhotoOption(false));
        binding.imgEditProPic.setOnClickListener(v -> choosePhotoOption(true));
    }

    public void choosePhotoOption(boolean isProfile) {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Upload Pictures Option");
        dialog.setMessage("How do you want to set your picture?");

        dialog.setPositiveButton("Gallery", (arg0, arg1) -> {
            Intent pictureActionIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pictureActionIntent, isProfile ? REQ_CODE_PROFILE_GALLERY : REQ_CODE_VEHICLE_GALLERY);
        });

        dialog.setNegativeButton("Camera",
                (arg0, arg1) -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(MyAccountActivity.this, new String[]{Manifest.permission.CAMERA}, REQ_CODE_VEHICLE_CAMERA);
                        return;
                    }
                    openCamera(isProfile ? REQ_CODE_PROFILE_CAMERA : REQ_CODE_VEHICLE_CAMERA);
                });
        dialog.show();
    }

    private void getDriverProfile() {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId).addListenerForSingleValueEvent(new UValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!DriverUtils.isValidDriverNode(dataSnapshot)) return;
                HashMap<String, Object> response = (HashMap<String, Object>) dataSnapshot.getValue();

                String fullName = (String) response.get("fullName");
                String profilePic = (String) response.get("profilePic");
                String vehicleImage = (String) dataSnapshot.child("vehicleInfo/imageUrl").getValue();

                binding.txtUsername.setText(fullName);
                binding.imageCover.setImageURI(Uri.parse(vehicleImage));

                if (!profilePic.isEmpty()) {
                    binding.imgProfilePic.setImageURI(Uri.parse(profilePic));
                } else {
                    binding.imgProfilePic.setImageURI(Uri.parse((String) dataSnapshot.child("vehicleInfo/imageUrl").getValue()));
                }

            }
        });
    }

    public void goBack(View view) {
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    void openCamera(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQ_CODE_VEHICLE_GALLERY) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    binding.imageCover.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateVehicleImage();
            } else if (requestCode == REQ_CODE_VEHICLE_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                binding.imageCover.setImageBitmap(photo);
                updateVehicleImage();
            } else if (requestCode == REQ_CODE_PROFILE_CAMERA) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                binding.imgProfilePic.setImageBitmap(photo);
                updateProfilePic();
            } else if (requestCode == REQ_CODE_PROFILE_GALLERY) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    binding.imgProfilePic.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateProfilePic();
            }
        }
    }

    private void updateVehicleImage() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Vehicle Image...");
        dialog.setCancelable(false);
        dialog.show();

        binding.imageCover.setDrawingCacheEnabled(true);
        binding.imageCover.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) binding.imageCover.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storage.child(FirebaseUtils.userId + "/vehicle.jpg").putBytes(data).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId + "/vehicleInfo/imageUrl").setValue(task.getResult().getDownloadUrl().toString());
            } else {
                ToastUtils.showErrorToast(context, task.getException().getLocalizedMessage());
            }
        });
    }

    private void updateProfilePic() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Profile Picture...");
        dialog.setCancelable(false);
        dialog.show();

        binding.imgProfilePic.setDrawingCacheEnabled(true);
        binding.imgProfilePic.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) binding.imgProfilePic.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        FirebaseUtils.storage.child("profile_picture/" + FirebaseUtils.userId + ".jpg").putBytes(data).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                FirebaseUtils.database.child("drivers/" + FirebaseUtils.userId + "/profilePic").setValue(task.getResult().getDownloadUrl().toString());
            } else {
                Toast.makeText(context, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera(REQ_CODE_VEHICLE_CAMERA);
        } else {
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show();
        }
    }
}