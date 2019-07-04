package com.socialedapp.fragment.locationtalk;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialedapp.R;
import com.socialedapp.activity.ActLocationTalkComment;
import com.socialedapp.adapter.AdapterLocationTalkChat;
import com.socialedapp.adapter.adapteritemclickinterface.OnItemClickListener;
import com.socialedapp.fragment.FragBase;
import com.socialedapp.framework.feednotification.FeedNotificationResponse;
import com.socialedapp.framework.locationtalk.LocationTalkResponse;
import com.socialedapp.framework.signup.RegisterResponse;
import com.socialedapp.helper.Constant;
import com.socialedapp.helper.Debug;
import com.socialedapp.helper.FireBaseHandler.FireBaseHelper;
import com.socialedapp.helper.PreferenceField;
import com.socialedapp.helper.UriHelper;
import com.socialedapp.helper.Utils;
import com.socialedapp.helper.permissionutils.PermissionHandler;
import com.socialedapp.helper.permissionutils.PermissionInterface;
import com.socialedapp.pushnotification.SendPushNotificationClass;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import static com.socialedapp.helper.Constant.ChatImages;
import static com.socialedapp.helper.Constant.LOCATION_TALK;
import static com.socialedapp.helper.Constant.LOCATION_TALK_DISTANCE;
import static com.socialedapp.helper.Constant.USERS;

/**
 * Created by Keyur on 15-May-17.
 */

public class FragLocationTalk extends FragBase {

    private PermissionHandler permissionHandler;
    private RecyclerView rcvLocationTalk;
    private AdapterLocationTalkChat adapterLocationTalkChat;
    private EditText edtMessage;
    private RegisterResponse myUserInfo = new RegisterResponse();
    private double latitude = 0.0f;
    private double longitude = 0.0f;
    private ImageView imgPhoto, imgSendMessage;
    private TextView txtDistance;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_locationtalk, container, false);

        setupView(view);

        return view;

    }

    public enum LocationTalkFilter {
        RECENT_POST, MOST_LIKE, CLOSET_POST, MY_POST_LIKE, MY_OWN_POST
    }

    public void setFilter(LocationTalkFilter filter) {

        if (filter == LocationTalkFilter.RECENT_POST) {

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {
                    return Long.parseLong(o1.messageDate) >= Long.parseLong(o2.messageDate) ? 1 : -1;
                }
            });

        } else if (filter == LocationTalkFilter.MOST_LIKE) {

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {
                    if (o1.likeUserIdList.size() == o2.likeUserIdList.size()) {
                        return 0;
                    }
                    return o1.likeUserIdList.size() > o2.likeUserIdList.size() ? 1 : -1;
                }
            });

        } else if (filter == LocationTalkFilter.CLOSET_POST) {

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {
                    Float distanceo1 = Utils.calculateDistanceLocationTalk(Float.parseFloat(o1.curre_lat + "")
                            , Float.parseFloat(o1.curr_long + "")
                            , Float.parseFloat(o1.latitude)
                            , Float.parseFloat(o1.longitude));

                    Float distanceo2 = Utils.calculateDistanceLocationTalk(Float.parseFloat(o2.curre_lat + "")
                            , Float.parseFloat(o2.curr_long + "")
                            , Float.parseFloat(o2.latitude)
                            , Float.parseFloat(o2.longitude));

                    return distanceo2 > distanceo1 ? 1 : -1;
                }
            });

        } else if (filter == LocationTalkFilter.MY_POST_LIKE) {

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {
                    return Long.parseLong(o1.messageDate) >= Long.parseLong(o2.messageDate) ? 1 : -1;
                }
            });

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {

                    String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    if (o1.likeUserIdList.contains(myUserId) ||
                            o2.likeUserIdList.contains(myUserId)) {

                        if (o1.likeUserIdList.contains(myUserId)) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else {
                        return 0;
                    }

                }
            });

        } else if (filter == LocationTalkFilter.MY_OWN_POST) {

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {
                    return Long.parseLong(o1.messageDate) >= Long.parseLong(o2.messageDate) ? 1 : -1;
                }
            });

            final String myUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Collections.sort(adapterLocationTalkChat.getAdapterData(), new Comparator<LocationTalkResponse>() {
                @Override
                public int compare(LocationTalkResponse o1, LocationTalkResponse o2) {

                    if (o1.userId.equalsIgnoreCase(myUserId) ||
                            o2.userId.equalsIgnoreCase(myUserId)) {

                        if (o2.userId.equalsIgnoreCase(myUserId)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return 0;
                    }

                }
            });

        }

        adapterLocationTalkChat.notifyDataSetChanged();
        if (adapterLocationTalkChat.getItemCount() > 0) {
            rcvLocationTalk.scrollToPosition(adapterLocationTalkChat.getItemCount() - 1);
        }

    }


    private void setupView(View view) {

        permissionHandler = PermissionHandler.getInstance(activity);

        Utils.setPref(activity, PreferenceField.OPEN_LOCATION_TALK_SCREEN, true);

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setMenuTitle(getString(R.string.location_talk));
        setFilterVisible(false);
        setFilterVisibleForLocationTalk(true);
        activity.findViewById(R.id.lnrBottom).setVisibility(View.GONE);
        activity.findViewById(R.id.lnrMainLayout).setBackgroundColor(Color.parseColor("#FFFFFF"));
        ((ImageView) activity.findViewById(R.id.imgmenu)).getDrawable().setColorFilter(activity.getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
        ((TextView) activity.findViewById(R.id.txttitle)).setTextColor(Color.parseColor("#3ECAFC"));

        txtDistance = (TextView) view.findViewById(R.id.txtDistance);
        txtDistance.setText(LOCATION_TALK_DISTANCE + getString(R.string.Km));

        adapterLocationTalkChat = new AdapterLocationTalkChat(activity);
        rcvLocationTalk = (RecyclerView) view.findViewById(R.id.rcvLocationTalkChat);
        rcvLocationTalk.setItemAnimator(new DefaultItemAnimator());
        adapterLocationTalkChat.setLikeClickListener(likeClickListener);
        adapterLocationTalkChat.setProfileClick(profileClick);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setStackFromEnd(true);
        rcvLocationTalk.setLayoutManager(linearLayoutManager);
        rcvLocationTalk.getItemAnimator().setChangeDuration(0);
        rcvLocationTalk.setAdapter(adapterLocationTalkChat);
        adapterLocationTalkChat.setOnItemClickListener(itemClick);

        imgSendMessage = (ImageView) view.findViewById(R.id.imgSendMessage);
        imgSendMessage.setOnClickListener(sendMessageClick);

        imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);
        imgPhoto.setOnClickListener(photoClick);

        edtMessage = (EditText) view.findViewById(R.id.edtMessage);

        if (permissionHandler.checkPermission(PermissionHandler.ACCESS_FINE_LOCATION)) {
            getLocationTalkMessages();
        }

    }

    View.OnClickListener sendMessageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendMessage();
        }
    };

    View.OnClickListener profileClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getTag() != null) {
                int position = (int) v.getTag();
                if (!adapterLocationTalkChat.getAdapterData().get(position).userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    gotoOtherUserProfile(adapterLocationTalkChat.getAdapterData().get(position).userId);
                }
            }
        }
    };

    View.OnClickListener likeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getTag() != null) {

                int position = (int) v.getTag();

                LocationTalkResponse locationTalkResponse = adapterLocationTalkChat.getAdapterData().get(position);

                if (locationTalkResponse != null) {

                    if (!locationTalkResponse.likeUserIdList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        updateLikeMemberList(true, locationTalkResponse);
                    } else {
                        updateLikeMemberList(false, locationTalkResponse);
                    }
                }
            }
        }
    };

    private void updateLikeMemberList(final boolean IsLike, LocationTalkResponse locationTalkResponse) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(LOCATION_TALK)
                .child(locationTalkResponse.locationTalkId)
                .child("likeUserIdList");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                if (IsLike) {
                    userListIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }

                if (mutableData.getValue() == null) {
                    mutableData.setValue(userListIds);
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (IsLike) {
                            userListIds.add(useridAdd);
                        } else {
                            if (!useridAdd.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                userListIds.add(useridAdd);
                            }
                        }
                    }
                    mutableData.setValue(userListIds);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Debug.e("onComplete", "onComplete " + databaseError);
            }
        });

    }

    View.OnClickListener photoClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isLocationEnable()) {

                uri_capture = null;
                fileProfilePic = null;

                new MaterialDialog.Builder(activity).items(R.array.profile).title(R.string.profile).itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (permissionHandler.checkPermission(PermissionHandler.READ_EXTERNAL_STORAGE)) {
                            if (position == 0) {
                                chooseGallery();
                            } else {
                                if (permissionHandler.checkPermission(PermissionHandler.CAMERA)) {
                                    chooseCamera();
                                }
                            }
                        }

                    }
                }).show();
            }
        }
    };

    private Uri uri_capture = null;
    private File fileProfilePic = null;

    private void chooseGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constant.PICK_GALLERY);

    }

    private void chooseCamera() {

        uri_capture = Utils.getOutputMediaFileUri(activity);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri_capture);
        startActivityForResult(intent, Constant.PICK_CAMERA);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == activity.RESULT_OK) {

            if (requestCode == Constant.PICK_GALLERY) {
                if (data != null) {
                    String image_path = UriHelper.getPath(activity, data.getData());
                    if (image_path != null && image_path.length() > 0) {
                        fileProfilePic = new File(image_path);
                        UploadPhoto();
                    }
                }
            } else if (requestCode == Constant.PICK_CAMERA) {
                if (uri_capture != null && uri_capture.getPath() != null) {
                    UploadPhoto();
                }
            }
        }
    }

    private UploadTask uploadTask;
    private FirebaseStorage firebaseStorage;

    private void UploadPhoto() {

        showIroidLoader();

        firebaseStorage = FirebaseStorage.getInstance();

        Uri file;

        if (fileProfilePic != null) {
            file = Uri.fromFile(fileProfilePic);
        } else {
            file = uri_capture;
        }

        StorageReference storageReference = firebaseStorage.getReference().child(ChatImages + "/" + file.getLastPathSegment());
        uploadTask = storageReference.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                closeIroidLoader();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference();
                String key = databaseReference.push().getKey();

                LocationTalkResponse locationTalkResponse = new LocationTalkResponse();
                locationTalkResponse.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
                locationTalkResponse.message = "";
                locationTalkResponse.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                locationTalkResponse.firstName = myUserInfo.firstName;
                locationTalkResponse.lastName = myUserInfo.lastName;
                locationTalkResponse.profileImage = myUserInfo.profileImage;
                locationTalkResponse.locationTalkId = key;
                locationTalkResponse.hasImage = Constant.YES;
                locationTalkResponse.longitude = longitude + "";
                locationTalkResponse.latitude = latitude + "";
                locationTalkResponse.locationTalkImage = downloadUrl.toString();
                locationTalkResponse.lastUserLoginTime = myUserInfo.lastUserLoginTime;

                databaseReference.child(LOCATION_TALK).child(key)
                        .setValue(locationTalkResponse);

                sendPushNotificationToUser(locationTalkResponse);

                closeIroidLoader();

            }
        });
    }

    private void sendMessage() {

        final String message = edtMessage.getText().toString().trim();

        if (message.length() > 0 && isLocationEnable()) {

            edtMessage.setText("");

            final DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference();
            String key = databaseReference.push().getKey();

            final LocationTalkResponse locationTalkResponse = new LocationTalkResponse();
            locationTalkResponse.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
            locationTalkResponse.message = message;
            locationTalkResponse.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            locationTalkResponse.firstName = myUserInfo.firstName;
            locationTalkResponse.lastName = myUserInfo.lastName;
            locationTalkResponse.profileImage = myUserInfo.profileImage;
            locationTalkResponse.locationTalkId = key;
            locationTalkResponse.hasImage = Constant.NO;
            locationTalkResponse.locationTalkImage = "";
            locationTalkResponse.latitude = latitude + "";
            locationTalkResponse.longitude = longitude + "";
            locationTalkResponse.lastUserLoginTime = myUserInfo.lastUserLoginTime;

            databaseReference.child(LOCATION_TALK).child(key)
                    .setValue(locationTalkResponse);

            Utils.hideKeyboard(activity, edtMessage.getWindowToken());

            sendPushNotificationToUser(locationTalkResponse);

        }
    }

    private void sendPushNotificationToUser(final LocationTalkResponse locationTalkResponse) {

        FireBaseHelper.getInstance().getDatabaseReference().child(USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            RegisterResponse registerResponse = snapshot.getValue(RegisterResponse.class);

                            if (registerResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                continue;
                            }

                            FeedNotificationResponse feedNotificationClass = new FeedNotificationResponse();
                            feedNotificationClass.firstName = myUserInfo.firstName;
                            feedNotificationClass.lastName = myUserInfo.lastName;
                            feedNotificationClass.userId = myUserInfo.userId;
                            feedNotificationClass.profileImage = myUserInfo.profileImage;
                            feedNotificationClass.nType = Constant.TYPE_LOCATION_TALK;
                            feedNotificationClass.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
                            feedNotificationClass.receiverUserId = registerResponse.userId;
                            feedNotificationClass.hasImage = locationTalkResponse.hasImage;
                            feedNotificationClass.latitude = locationTalkResponse.latitude;
                            feedNotificationClass.longitude = locationTalkResponse.longitude;

                            SendPushNotificationClass.getInstance().sendPush(registerResponse.deviceToken
                                    , feedNotificationClass.getObject());

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    OnItemClickListener itemClick = new OnItemClickListener() {
        @Override
        public void onItemClickListener(int position) {
            Intent intent = new Intent(activity, ActLocationTalkComment.class);
            intent.putExtra("locationTalk", adapterLocationTalkChat.getAdapterData().get(position));
            startActivity(intent);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionInterface() {
            @Override
            public void permissionGranted(String permission) {
            }

            @Override
            public void permissionDenied(String permission) {

            }
        });
    }

    private void getLocationTalkMessages() {

        if (isLocationEnable()) {

            SmartLocation.with(activity).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            SmartLocation.with(activity).location().stop();
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            getMessages();
                        }
                    });

        }

    }

    private boolean isLocationEnable() {

        if (SmartLocation.with(activity).location().state().locationServicesEnabled()) {
            SmartLocation.with(activity).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            SmartLocation.with(activity).location().stop();
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    });
            return true;
        } else {
            new MaterialDialog.Builder(activity)
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel)
                    .title(R.string.location_service)
                    .content(R.string.enable_location_service)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                            Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(viewIntent);
                        }
                    }).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.dismiss();
                }
            }).show();
        }
        return false;
    }

    private DatabaseReference databaseReference;

    private void getMessages() {

        showIroidLoader();

        FireBaseHelper.getInstance().getDatabaseReference().child(Constant.USERS)
                .orderByChild("userId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            myUserInfo = snapshot.getValue(RegisterResponse.class);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        databaseReference = FireBaseHelper.getInstance().getDatabaseReference().child(LOCATION_TALK);
        databaseReference.addChildEventListener(messageChildValueListener);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                closeIroidLoader();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                closeIroidLoader();
            }
        });

    }

    ChildEventListener messageChildValueListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            LocationTalkResponse locationTalkResponse = dataSnapshot.getValue(LocationTalkResponse.class);

            if (locationTalkResponse.latitude.length() > 0 && locationTalkResponse.longitude.length() > 0) {

                Float distance = Utils.calculateDistanceLocationTalk(Float.parseFloat(latitude + "")
                        , Float.parseFloat(longitude + "")
                        , Float.parseFloat(locationTalkResponse.latitude)
                        , Float.parseFloat(locationTalkResponse.longitude));

                if (distance <= LOCATION_TALK_DISTANCE) {

                    locationTalkResponse.curre_lat = "" + latitude;
                    locationTalkResponse.curr_long = "" + longitude;
                    adapterLocationTalkChat.add(locationTalkResponse);
                    if (adapterLocationTalkChat.getItemCount() > 0) {
                        rcvLocationTalk.scrollToPosition(adapterLocationTalkChat.getItemCount() - 1);
                    }
                }

            }


        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            LocationTalkResponse locationTalkResponse = dataSnapshot.getValue(LocationTalkResponse.class);

            for (int i = 0; i < adapterLocationTalkChat.getAdapterData().size(); i++) {
                if (adapterLocationTalkChat.getAdapterData().get(i).locationTalkId.equalsIgnoreCase(locationTalkResponse.locationTalkId)) {
                    locationTalkResponse.curre_lat = "" + latitude;
                    locationTalkResponse.curr_long = "" + longitude;
                    adapterLocationTalkChat.getAdapterData().set(i, locationTalkResponse);
                    adapterLocationTalkChat.notifyItemChanged(i);
                    break;
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            LocationTalkResponse locationTalkResponse = dataSnapshot.getValue(LocationTalkResponse.class);

            for (int i = 0; i < adapterLocationTalkChat.getAdapterData().size(); i++) {
                if (adapterLocationTalkChat.getAdapterData().get(i).locationTalkId.equalsIgnoreCase(locationTalkResponse.locationTalkId)) {
                    adapterLocationTalkChat.removeItem(i);
                    break;
                }
            }
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (databaseReference != null) {
            databaseReference.removeEventListener(messageChildValueListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.setPref(activity, PreferenceField.OPEN_LOCATION_TALK_SCREEN, false);
        setFilterVisibleForLocationTalk(false);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        activity.findViewById(R.id.lnrBottom).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.lnrMainLayout).setBackgroundResource(R.drawable.bg_app_rest);
        ((ImageView) activity.findViewById(R.id.imgmenu)).getDrawable().setColorFilter(activity.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        ((TextView) activity.findViewById(R.id.txttitle)).setTextColor(Color.parseColor("#FFFFFF"));
    }
}
