package np.com.softwarica.uride.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.databinding.ActivityAddInsuranceDetailsBinding;
import np.com.softwarica.uride.utils.ImageUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.SharedPref;
import np.com.softwarica.uride.utils.ToastUtils;

public class AddInsuranceDetailsActivity extends AppCompatActivity {

    private ActivityAddInsuranceDetailsBinding b;
    private FirebaseUser user;
    private ProgressDialog dialog;
    private DatabaseReference database;
    private StorageReference storage;
    private Object insuranceUri, registrationUri;

    private final int GALLERY_REQUEST_INSURANCE = 001;
    private final int CAMERA_REQUEST_INSURANCE = 002;
    private final int GALLERY_REQUEST_REGISTRATION = 003;
    private final int CAMERA_REQUEST_REGISTRATION = 004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_add_insurance_details);

        b.toolbar.setTitle("Add Documents");

        user = FirebaseAuth.getInstance().getCurrentUser();
        dialog = new ProgressDialog(this);
        storage = FirebaseStorage.getInstance().getReference().child("drivers");
        database = FirebaseDatabase.getInstance().getReference().child("drivers");

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void goBack(View view) {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public void choosePhotoOption(View view) {
        if (view.getId() == R.id.imgVehicleInsurance) {
            selectImage(GALLERY_REQUEST_INSURANCE, CAMERA_REQUEST_INSURANCE);
        } else {
            selectImage(GALLERY_REQUEST_REGISTRATION, CAMERA_REQUEST_REGISTRATION);
        }
    }

    public void selectImage(final int requestCodeGallery, final int requestCodeCamera) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                (arg0, arg1) -> {
                    Intent pictureActionIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pictureActionIntent, requestCodeGallery);
                });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> {
                    if (ContextCompat.checkSelfPermission(AddInsuranceDetailsActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(AddInsuranceDetailsActivity.this, new String[]{Manifest.permission.CAMERA}, GALLERY_REQUEST_REGISTRATION);
                        return;
                    }
                    openCamera(requestCodeCamera);

                });
        myAlertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == GALLERY_REQUEST_INSURANCE) {
                insuranceUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    b.imgVehicleInsurance.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST_INSURANCE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                insuranceUri = ImageUtils.convertBitmapToByte(this, photo);
                b.imgVehicleInsurance.setImageBitmap(photo);
            } else if (requestCode == GALLERY_REQUEST_REGISTRATION) {
                registrationUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    b.imgVehicleRegistration.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA_REQUEST_REGISTRATION) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                registrationUri = ImageUtils.convertBitmapToByte(this, photo);
                b.imgVehicleRegistration.setImageBitmap(photo);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_REQUEST_INSURANCE) {
                openCamera(CAMERA_REQUEST_INSURANCE);
            } else {
                openCamera(CAMERA_REQUEST_REGISTRATION);
            }
        } else {
            ToastUtils.showErrorToast(this, "Camera Permission Denied");
        }
    }

    void openCamera(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, requestCode);
    }

    public void uploadFinalDocuments(View view) {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        if (insuranceUri == null || registrationUri == null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please select document image.");
            dialog.setPositiveButton("OK", (d, i) -> d.dismiss());
            dialog.setCancelable(false);
            dialog.show();
            return;
        }

        dialog.setMessage("Uploading final document...");
        dialog.setCancelable(false);
        dialog.show();

        if (insuranceUri instanceof Uri) {
            storage.child(user.getUid() + "/insurance.jpg").putFile((Uri) insuranceUri).addOnSuccessListener(taskSnapshot -> {
                final String url1 = storage.getDownloadUrl().toString();
                if (registrationUri instanceof Uri) {
                    storage.child(user.getUid() + "registration.jpg").putFile((Uri) registrationUri).addOnSuccessListener(taskSnapshot12 -> addDocument(url1, storage.getDownloadUrl().toString()));
                } else {
                    storage.child(user.getUid() + "registration.jpg").putBytes((byte[]) registrationUri).addOnSuccessListener(taskSnapshot1 -> addDocument(url1, storage.getDownloadUrl().toString()));
                }
            });
        } else {
            storage.child(user.getUid() + "/insurance.jpg").putBytes((byte[]) insuranceUri).addOnSuccessListener(taskSnapshot -> {
                final String url1 = storage.getDownloadUrl().toString();
                if (registrationUri instanceof Uri) {
                    storage.child(user.getUid() + "registration.jpg").putFile((Uri) registrationUri).addOnSuccessListener(taskSnapshot13 -> addDocument(url1, storage.getDownloadUrl().toString()));
                } else {
                    storage.child(user.getUid() + "registration.jpg").putBytes((byte[]) registrationUri).addOnSuccessListener(taskSnapshot14 -> addDocument(url1, storage.getDownloadUrl().toString()));
                }
            });
        }
    }

    public void addDocument(String url1, String url2) {
        dialog.dismiss();
        HashMap<String, String> documentData = new HashMap<>();
        documentData.put("insuranceImageUrl", url1);
        documentData.put("registrationImageUrl", url2);
        database.child(user.getUid()).child("documentUploadLevel").setValue("3");
        database.child(user.getUid()).child("documentInfo").setValue(documentData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SharedPref.setString(AddInsuranceDetailsActivity.this, "isDriver", "true");
                finishAffinity();
                startActivity(new Intent(AddInsuranceDetailsActivity.this, SignupCompleteActivity.class));
            }
        });
    }
}
