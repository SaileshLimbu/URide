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
import java.util.ArrayList;
import java.util.HashMap;

import np.com.softwarica.uride.R;
import np.com.softwarica.uride.adapters.SpinnerCarAdapter;
import np.com.softwarica.uride.databinding.ActivityAddVehicleDetailsBinding;
import np.com.softwarica.uride.models.CarItem;
import np.com.softwarica.uride.utils.ImageUtils;
import np.com.softwarica.uride.utils.NetworkUtils;
import np.com.softwarica.uride.utils.RequestCodeUtils;
import np.com.softwarica.uride.utils.ToastUtils;

public class AddVehicleDetailsActivity extends AppCompatActivity {

    private ActivityAddVehicleDetailsBinding b;
    private Context context;
    private ProgressDialog dialog;
    private FirebaseUser user;
    private StorageReference storageRef;
    private DatabaseReference database;
    private ArrayList<CarItem> carListItem;
    private Object vehicleUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_add_vehicle_details);

        context = this;
        dialog = new ProgressDialog(this);
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child("drivers/" + user.getUid() + "/vehicle.jpg");
        database = FirebaseDatabase.getInstance().getReference().child("drivers");

        initSpinner();
        b.toolbar.setTitle("Add Vehicle Details");

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

    public void initSpinner() {
        carListItem = new ArrayList<>();
        carListItem.add(new CarItem(R.drawable.vehicle_1, "Sedan", "3 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_2, "Plus", "7 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_3, "Regular", "3 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_4, "SUV", "6 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_5, "Luxury", "4 seats"));
        carListItem.add(new CarItem(R.drawable.vehicle_6, "Motor Bike", "2 seats"));

        SpinnerCarAdapter adapter = new SpinnerCarAdapter(this, carListItem);
        b.spinnerVehicle.setAdapter(adapter);
    }

    public void choosePhotoOption(View view) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Upload Pictures Option");
        myAlertDialog.setMessage("How do you want to set your picture?");

        myAlertDialog.setPositiveButton("Gallery",
                (arg0, arg1) -> {
                    Intent pictureActionIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pictureActionIntent, RequestCodeUtils.GALLERY_REQUEST_CODE);
                });

        myAlertDialog.setNegativeButton("Camera",
                (arg0, arg1) -> {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(AddVehicleDetailsActivity.this, new String[]{Manifest.permission.CAMERA}, RequestCodeUtils.CAMERA_REQUEST_CODE);
                        return;
                    }
                    openCamera();
                });
        myAlertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == RequestCodeUtils.GALLERY_REQUEST_CODE) {
                vehicleUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    b.imageVehicle.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == RequestCodeUtils.CAMERA_REQUEST_CODE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                vehicleUri = ImageUtils.convertBitmapToByte(getApplicationContext(), photo);
                b.imageVehicle.setImageBitmap(photo);
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

    public void uploadVehicleDetails(View view) {
        if (!NetworkUtils.isConnected(this)) {
            ToastUtils.showErrorToast(this, getResources().getString(R.string.str_en_no_internet));
            return;
        }
        String vehicleBrand = b.txtBrand.getText().toString().trim();
        String vehicleModal = b.txtModal.getText().toString().trim();
        String year = b.txtYear.getText().toString().trim();
        String vehicleColor = b.txtColor.getText().toString().trim();

        if (vehicleBrand.isEmpty()) {
            b.txtBrand.setError("Please enter vehicle brand.");
            b.txtBrand.setFocusable(true);
            return;
        }
        if (vehicleModal.isEmpty()) {
            b.txtModal.setError("Please enter vehicle modal.");
            b.txtModal.setFocusable(true);
            return;
        }
        if (year.isEmpty()) {
            b.txtYear.setError("Please enter vehicle year.");
            b.txtYear.setFocusable(true);
            return;
        }
        if (vehicleColor.isEmpty()) {
            b.txtColor.setError("Please enter vehicle color.");
            b.txtColor.setFocusable(true);
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
        dialog.setMessage("Uploading Document...");
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
        HashMap<String, String> vehicleData = new HashMap<>();
        vehicleData.put("vehicleType", ((CarItem) b.spinnerVehicle.getSelectedItem()).getCarName());
        vehicleData.put("vehicleBrand", b.txtBrand.getText().toString().trim());
        vehicleData.put("vehicleModal", b.txtModal.getText().toString().trim());
        vehicleData.put("vehicleYear", b.txtYear.getText().toString().trim());
        vehicleData.put("vehicleColor", b.txtColor.getText().toString().trim());
        vehicleData.put("imageUrl", url);

        database.child(user.getUid()).child("vehicleInfo").setValue(vehicleData).addOnCompleteListener(task -> {
            dialog.dismiss();
            database.child(user.getUid()).child("documentUploadLevel").setValue("1");
            startActivity(new Intent(context, AddDriverLicenseActivity.class));
            finish();
        });
    }

    void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RequestCodeUtils.CAMERA_REQUEST_CODE);
    }
}
