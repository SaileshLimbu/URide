package np.com.softwarica.uride.activities.drivers;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import np.com.softwarica.uride.databinding.ActivityAddDriverLicenseBinding;
import np.com.softwarica.uride.utils.ImageUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.RequestCodeUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class AddDriverLicenseActivity extends AppCompatActivity {


    private ActivityAddDriverLicenseBinding b;
    private Context context;
    private FirebaseUser user;
    private ProgressDialog dialog;
    private DatabaseReference database;
    private StorageReference storageRef;
    private Object vehicleUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_add_driver_license);

        context = this;
        user = FirebaseAuth.getInstance().getCurrentUser();
        dialog = new ProgressDialog(this);
        storageRef = FirebaseStorage.getInstance().getReference().child("drivers/" + user.getUid() + "/license.jpg");
        database = FirebaseDatabase.getInstance().getReference().child("drivers");

        b.toolbar.setTitle("Add Driver's License");

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
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery", (arg0, arg1) -> {
            Intent pictureActionIntent = null;
            pictureActionIntent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(
                    pictureActionIntent,
                    RequestCodeUtils.GALLERY_REQUEST_CODE);
        });

        myAlertDialog.setNegativeButton("Camera", (arg0, arg1) -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(AddDriverLicenseActivity.this, new String[]{Manifest.permission.CAMERA}, RequestCodeUtils.CAMERA_REQUEST_CODE);
                return;
            }
            openCamera();

        });
        myAlertDialog.show();
    }

    void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RequestCodeUtils.CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == RequestCodeUtils.GALLERY_REQUEST_CODE) {
                vehicleUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    b.imageLicense.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == RequestCodeUtils.CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                vehicleUri = ImageUtils.convertBitmapToByte(this, photo);
                b.imageLicense.setImageBitmap(photo);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ToastUtils.showErrorToast(this, "Camera Permission Denied");
        }
    }


    public void uploadLicense(View view) {
        if(!NetworkUtils.isConnected(this)){
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        String licenseNumber = b.txtLicenseNumber.getText().toString().trim();
        String issuedDate = b.txtIssuedDate.getText().toString().trim();
        String expiryDate = b.txtExpiryDate.getText().toString().trim();

        if (licenseNumber.isEmpty()) {
            b.txtLicenseNumber.setError("Please enter your license number.");
            b.txtLicenseNumber.setFocusable(true);
            return;
        }
        if (issuedDate.isEmpty()) {
            b.txtIssuedDate.setError("Please enter issued date.");
            b.txtIssuedDate.setFocusable(true);
            return;
        }
        if (expiryDate.isEmpty()) {
            b.txtExpiryDate.setError("Please enter expiry date.");
            b.txtExpiryDate.setFocusable(true);
            return;
        }

        if (vehicleUri == null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please select vehicle image.");
            dialog.setPositiveButton("OK", (d, i) -> d.dismiss());
            dialog.setCancelable(false);
            dialog.show();
            return;
        }

        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, "No internet connection!!!");
            return;
        }
        dialog.setMessage("Uploading License...");
        dialog.setCancelable(false);
        dialog.show();

        if (vehicleUri instanceof Uri) {
            storageRef.putFile((Uri) vehicleUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    ToastUtils.showErrorToast(context, task.getException().getMessage());
                    dialog.dismiss();
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    addDocumentData(downloadUri.toString());
                }
            });
        } else {
            storageRef.putBytes((byte[]) vehicleUri).continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    ToastUtils.showErrorToast(context, task.getException().getMessage());
                    dialog.dismiss();
                    throw task.getException();
                }
                return storageRef.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    addDocumentData(downloadUri.toString());
                }
            });
        }
    }

    private void addDocumentData(String url) {
        HashMap<String, String> licenseData = new HashMap<>();
        licenseData.put("licenseNumber", b.txtLicenseNumber.getText().toString().trim());
        licenseData.put("licenseType", b.spinnerVehicleType.getSelectedItem().toString());
        licenseData.put("issuedDate", b.txtIssuedDate.getText().toString().trim());
        licenseData.put("expiryDate", b.txtExpiryDate.getText().toString().trim());
        licenseData.put("imageUrl", url);

        database.child(user.getUid()).child("licenseInfo").setValue(licenseData).addOnCompleteListener(task -> {
            dialog.dismiss();
            database.child(user.getUid()).child("documentUploadLevel").setValue("2");
            startActivity(new Intent(context, AddInsuranceDetailsActivity.class));
            finish();
        });
    }
}
