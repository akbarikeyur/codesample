package com.socialedapp.fragment.photosview.galleryprofile;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.socialedapp.R;
import com.socialedapp.activity.ActLikedPhotoUserList;
import com.socialedapp.fragment.FragBase;
import com.socialedapp.framework.chatmessages.ChatMessagesResponse;
import com.socialedapp.framework.feednotification.FeedNotificationResponse;
import com.socialedapp.framework.groupchat.GroupChatMessageGalleryResponse;
import com.socialedapp.framework.groupchatevent.EventChatMessageGalleryResponse;
import com.socialedapp.framework.locationtalk.LocationTalkResponse;
import com.socialedapp.framework.signup.RegisterResponse;
import com.socialedapp.framework.uploadgalleryresponse.UploadGalleryResponse;
import com.socialedapp.framework.userprofilelike.PhotoLikeResponse;
import com.socialedapp.helper.Debug;
import com.socialedapp.helper.FireBaseHandler.FireBaseHelper;
import com.socialedapp.helper.permissionutils.PermissionHandler;
import com.socialedapp.helper.permissionutils.PermissionInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.socialedapp.helper.Constant.GROUP_CHAT;
import static com.socialedapp.helper.Constant.GROUP_CHAT_EVENT;
import static com.socialedapp.helper.Constant.GROUP_CHAT_GALLERY;
import static com.socialedapp.helper.Constant.GROUP_CHAT_MESSAGE;
import static com.socialedapp.helper.Constant.PHOTO_LIKE;
import static com.socialedapp.helper.Constant.USERS;
import static com.socialedapp.helper.Constant.USER_GALLERY;

/**
 * Created by Keyur on 10-May-17.
 */

public class FragProfilePhotoViewProfile extends FragBase {

    private ImageView imgPhoto, imgHeartLike, imgClose, imgSetting;
    private LinearLayout lnrLike, lnrLikeClick;
    private RegisterResponse myUserInfo;
    private TextView txtLikeCount;
    private Object object;
    private PermissionHandler permissionHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_profile_photo, container, false);

        setupView(view);

        return view;

    }

    public void setProfileData(Object object) {
        this.object = object;
    }

    private void setupView(View view) {

        permissionHandler = PermissionHandler.getInstance(activity);

        imgClose = (ImageView) view.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(closeClick);
        imgClose.getDrawable().setColorFilter(getResources().getColor(R.color.white, null), PorterDuff.Mode.SRC_IN);

        imgSetting = (ImageView) view.findViewById(R.id.imgPhotoSetting);
        imgSetting.setVisibility(View.GONE);
        imgSetting.setOnClickListener(settingClick);

        lnrLikeClick = (LinearLayout) view.findViewById(R.id.lnrLikeClick);
        lnrLikeClick.setOnClickListener(likeClick);

        lnrLike = (LinearLayout) view.findViewById(R.id.lnrLike);
        lnrLike.setVisibility(View.GONE);

        txtLikeCount = (TextView) view.findViewById(R.id.txtLikeCount);

        imgHeartLike = (ImageView) view.findViewById(R.id.imgHeartLike);

        imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);

        FireBaseHelper.getInstance().getDatabaseReference().child(USERS)
                .orderByChild("userId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            RegisterResponse registerResponse = snapshot.getValue(RegisterResponse.class);
                            myUserInfo = registerResponse;
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        bindData();

    }

    View.OnClickListener settingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (permissionHandler.checkPermission(PermissionHandler.READ_EXTERNAL_STORAGE)) {
                if (object instanceof EventChatMessageGalleryResponse) {
                    showPhotoSettingDialog(((EventChatMessageGalleryResponse) object).groupChatImage);
                } else if (object instanceof GroupChatMessageGalleryResponse) {
                    showPhotoSettingDialog(((GroupChatMessageGalleryResponse) object).groupChatImage);
                }
            }
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

    View.OnClickListener closeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activity.finish();
        }
    };


    private void bindData() {

        if (object instanceof UploadGalleryResponse) {

            UploadGalleryResponse uploadGalleryResponse = (UploadGalleryResponse) object;

            if (uploadGalleryResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                imgHeartLike.setImageResource(R.drawable.ic_white_like);
            } else {
                if (uploadGalleryResponse.likeUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    imgHeartLike.setImageResource(R.drawable.like_me);
                } else {
                    imgHeartLike.setImageResource(R.drawable.like_me_not);
                }

            }

            if (uploadGalleryResponse.imagePath.length() > 0) {
                Picasso.with(activity).load(uploadGalleryResponse.imagePath).into(imgPhoto);
            }

            lnrLike.setVisibility(View.VISIBLE);

            if (uploadGalleryResponse.likeUserList.size() == 0) {
                txtLikeCount.setText(uploadGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
            } else if (uploadGalleryResponse.likeUserList.size() == 1) {
                txtLikeCount.setText(uploadGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
            } else {
                txtLikeCount.setText(uploadGalleryResponse.likeUserList.size() + " " + getString(R.string.likes));
            }

        } else if (object instanceof LocationTalkResponse) {

            if (((LocationTalkResponse) object).locationTalkImage.length() > 0) {
                Picasso.with(activity).load(((LocationTalkResponse) object).locationTalkImage).into(imgPhoto);
            }

        } else if (object instanceof ChatMessagesResponse) {
            if (((ChatMessagesResponse) object).chatImage.length() > 0) {
                Picasso.with(activity).load(((ChatMessagesResponse) object).chatImage).into(imgPhoto);
            }

        } else if (object instanceof EventChatMessageGalleryResponse) {

            EventChatMessageGalleryResponse eventChatMessageGalleryResponse = (EventChatMessageGalleryResponse) object;

            if (eventChatMessageGalleryResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                imgHeartLike.setImageResource(R.drawable.ic_white_like);
            } else {
                if (eventChatMessageGalleryResponse.likeUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    imgHeartLike.setImageResource(R.drawable.like_me);
                } else {
                    imgHeartLike.setImageResource(R.drawable.like_me_not);
                }

            }

            if (eventChatMessageGalleryResponse.groupChatImage.length() > 0) {
                Picasso.with(activity).load(eventChatMessageGalleryResponse.groupChatImage).into(imgPhoto);
            }

            lnrLike.setVisibility(View.VISIBLE);
            imgSetting.setVisibility(View.VISIBLE);

            if (eventChatMessageGalleryResponse.likeUserList.size() == 0) {
                txtLikeCount.setText(eventChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
            } else if (eventChatMessageGalleryResponse.likeUserList.size() == 1) {
                txtLikeCount.setText(eventChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
            } else {
                txtLikeCount.setText(eventChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.likes));
            }

        } else if (object instanceof GroupChatMessageGalleryResponse) {

            GroupChatMessageGalleryResponse groupChatMessageGalleryResponse = (GroupChatMessageGalleryResponse) object;

            if (groupChatMessageGalleryResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                imgHeartLike.setImageResource(R.drawable.ic_white_like);
            } else {
                if (groupChatMessageGalleryResponse.likeUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    imgHeartLike.setImageResource(R.drawable.like_me);
                } else {
                    imgHeartLike.setImageResource(R.drawable.like_me_not);
                }

            }
            if (groupChatMessageGalleryResponse.groupChatImage.length() > 0) {
                Picasso.with(activity).load(groupChatMessageGalleryResponse.groupChatImage).into(imgPhoto);
            }
            lnrLike.setVisibility(View.VISIBLE);

            imgSetting.setVisibility(View.VISIBLE);

            if (groupChatMessageGalleryResponse.likeUserList.size() == 0) {
                txtLikeCount.setText(groupChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
            } else if (groupChatMessageGalleryResponse.likeUserList.size() == 1) {
                txtLikeCount.setText(groupChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
            } else {
                txtLikeCount.setText(groupChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.likes));
            }


        } else if (object instanceof FeedNotificationResponse) {
            if (((FeedNotificationResponse) object).uploadedPhotoUrl.length() > 0) {
                Picasso.with(activity).load(((FeedNotificationResponse) object).uploadedPhotoUrl).into(imgPhoto);
            }
        }
    }

    View.OnClickListener likeClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (myUserInfo != null) {

                if (object instanceof UploadGalleryResponse) {
                    UploadGalleryResponse uploadGalleryResponse = (UploadGalleryResponse) object;
                    setGalleryLikeCall(uploadGalleryResponse);
                } else if (object instanceof EventChatMessageGalleryResponse) {
                    EventChatMessageGalleryResponse eventChatMessageGalleryResponse = (EventChatMessageGalleryResponse) object;
                    setEventPhotoLikeCall(eventChatMessageGalleryResponse);
                } else if (object instanceof GroupChatMessageGalleryResponse) {
                    GroupChatMessageGalleryResponse groupChatMessageGalleryResponse = (GroupChatMessageGalleryResponse) object;
                    setGroupPhotoLikeCall(groupChatMessageGalleryResponse);
                }
            }
        }
    };

    private void setGroupPhotoLikeCall(GroupChatMessageGalleryResponse groupChatMessageGalleryResponse) {

        if (!groupChatMessageGalleryResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            lnrLike.setEnabled(false);

            if (!groupChatMessageGalleryResponse.likeUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                tapHeartZoomInOutAnimation(imgHeartLike);

                updateTransactionForEventPhotoLike(groupChatMessageGalleryResponse.groupChatId, GROUP_CHAT);

                ((GroupChatMessageGalleryResponse) object).likeUserList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if (groupChatMessageGalleryResponse.likeUserList.size() == 0) {
                    txtLikeCount.setText(groupChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
                } else if (groupChatMessageGalleryResponse.likeUserList.size() == 1) {
                    txtLikeCount.setText(groupChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
                } else {
                    txtLikeCount.setText(groupChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.likes));
                }

                imgHeartLike.setImageResource(R.drawable.like_me);
            }

        } else {
            if (groupChatMessageGalleryResponse.likeUserList.size() > 0) {
                openWhoLikedForEventGroupPhoto(groupChatMessageGalleryResponse.groupChatId);
            }
        }

    }

    private void setEventPhotoLikeCall(EventChatMessageGalleryResponse eventChatMessageGalleryResponse) {

        if (!eventChatMessageGalleryResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            lnrLike.setEnabled(false);

            if (!eventChatMessageGalleryResponse.likeUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                tapHeartZoomInOutAnimation(imgHeartLike);

                updateTransactionForEventPhotoLike(eventChatMessageGalleryResponse.groupChatId, GROUP_CHAT_EVENT);

                ((EventChatMessageGalleryResponse) object).likeUserList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

                if (eventChatMessageGalleryResponse.likeUserList.size() == 0) {
                    txtLikeCount.setText(eventChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
                } else if (eventChatMessageGalleryResponse.likeUserList.size() == 1) {
                    txtLikeCount.setText(eventChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
                } else {
                    txtLikeCount.setText(eventChatMessageGalleryResponse.likeUserList.size() + " " + getString(R.string.likes));
                }

                imgHeartLike.setImageResource(R.drawable.like_me);
            }

        } else {
            if (eventChatMessageGalleryResponse.likeUserList.size() > 0) {
                openWhoLikedForEventGroupPhoto(eventChatMessageGalleryResponse.groupChatId);
            }
        }

    }

    private void setGalleryLikeCall(UploadGalleryResponse uploadGalleryResponse) {

        if (!uploadGalleryResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

            lnrLike.setEnabled(false);

            if (!uploadGalleryResponse.likeUserList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                tapHeartZoomInOutAnimation(imgHeartLike);
                updateTransactionForLike(uploadGalleryResponse);
                ((UploadGalleryResponse) object).likeUserList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                if (uploadGalleryResponse.likeUserList.size() == 0) {
                    txtLikeCount.setText(uploadGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
                } else if (uploadGalleryResponse.likeUserList.size() == 1) {
                    txtLikeCount.setText(uploadGalleryResponse.likeUserList.size() + " " + getString(R.string.like));
                } else {
                    txtLikeCount.setText(uploadGalleryResponse.likeUserList.size() + " " + getString(R.string.likes));
                }
                imgHeartLike.setImageResource(R.drawable.like_me);
            }

        } else {
            if (uploadGalleryResponse.likeUserList.size() > 0) {
                openWhoLikedProfilePicture(uploadGalleryResponse.id);
            }
        }


    }

    private void tapHeartZoomInOutAnimation(final ImageView imageView) {

        float zoomOutValue = 1f;

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(imageView, View.SCALE_X, 0, zoomOutValue);
        objectAnimator3.setInterpolator(new BounceInterpolator());
        objectAnimator3.setDuration(700);

        ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(imageView, View.SCALE_Y, 0, zoomOutValue);
        objectAnimator4.setInterpolator(new BounceInterpolator());
        objectAnimator4.setDuration(700);

        animatorSet.playTogether(objectAnimator3, objectAnimator4);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

    }

    private void openWhoLikedForEventGroupPhoto(String groupChatId) {
        Intent intent = new Intent(activity, ActLikedPhotoUserList.class);
        intent.putExtra("groupChatId", "" + groupChatId);
        activity.startActivity(intent);
    }

    private void openWhoLikedProfilePicture(String galleryId) {
        Intent intent = new Intent(activity, ActLikedPhotoUserList.class);
        intent.putExtra("galleryId", "" + galleryId);
        startActivity(intent);
    }

    private void updateTransactionForLike(final UploadGalleryResponse uploadGalleryResponse) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(USER_GALLERY)
                .child(uploadGalleryResponse.id)
                .child("likeUserList");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                boolean isUserFound = false;

                if (mutableData.getValue() == null) {
                    mutableData.setValue(userListIds);
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (!useridAdd.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            userListIds.add(useridAdd);
                        } else {
                            isUserFound = true;
                        }
                    }
                }

                if (!isUserFound) {
                    userListIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    mutableData.setValue(userListIds);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean isSuccess, DataSnapshot dataSnapshot) {
                Debug.e("onComplete", "onComplete " + databaseError);

                if (isSuccess) {

                    DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference();
                    String key = databaseReference.push().getKey();

                    PhotoLikeResponse userProfilePhotoLikeResponse = new PhotoLikeResponse();
                    userProfilePhotoLikeResponse.userId = myUserInfo.userId;
                    userProfilePhotoLikeResponse.firstName = myUserInfo.firstName;
                    userProfilePhotoLikeResponse.lastName = myUserInfo.lastName;
                    userProfilePhotoLikeResponse.profileImage = myUserInfo.profileImage;
                    userProfilePhotoLikeResponse.userStatus = myUserInfo.userStatus;
                    userProfilePhotoLikeResponse.galleryId = uploadGalleryResponse.id;
                    userProfilePhotoLikeResponse.lastUserLoginTime = myUserInfo.lastUserLoginTime;
                    userProfilePhotoLikeResponse.likeId = key;

                    FireBaseHelper.getInstance().getDatabaseReference().child(PHOTO_LIKE).child(key).setValue(userProfilePhotoLikeResponse);

                }

            }
        });

    }

    private void updateTransactionForEventPhotoLike(final String groupChatId, final String childName) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(childName)
                .child(GROUP_CHAT_GALLERY)
                .child(groupChatId)
                .child("likeUserList");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                boolean isUserFound = false;

                if (mutableData.getValue() == null) {
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (!useridAdd.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            userListIds.add(useridAdd);
                        } else {
                            isUserFound = true;
                        }

                    }
                }

                if (!isUserFound) {
                    userListIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    mutableData.setValue(userListIds);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean isSuccess, DataSnapshot dataSnapshot) {
                Debug.e("onComplete", "onComplete " + databaseError);

                if (isSuccess) {
                    updateTransactionForEventPhotoLikeInMessage(groupChatId, childName);
                }

            }
        });

    }

    private void updateTransactionForEventPhotoLikeInMessage(final String groupChatId, final String childName) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(childName)
                .child(GROUP_CHAT_MESSAGE)
                .child(groupChatId)
                .child("likeUserList");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                boolean isUserFound = false;

                if (mutableData.getValue() == null) {
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (!useridAdd.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            userListIds.add(useridAdd);
                        } else {
                            isUserFound = true;
                        }
                    }
                }

                if (!isUserFound) {
                    userListIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    mutableData.setValue(userListIds);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }

            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean isSuccess, DataSnapshot dataSnapshot) {
                Debug.e("onComplete", "onComplete " + databaseError);

                if (isSuccess) {

                    DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference();
                    String key = databaseReference.push().getKey();

                    PhotoLikeResponse userProfilePhotoLikeResponse = new PhotoLikeResponse();
                    userProfilePhotoLikeResponse.userId = myUserInfo.userId;
                    userProfilePhotoLikeResponse.firstName = myUserInfo.firstName;
                    userProfilePhotoLikeResponse.lastName = myUserInfo.lastName;
                    userProfilePhotoLikeResponse.profileImage = myUserInfo.profileImage;
                    userProfilePhotoLikeResponse.userStatus = myUserInfo.userStatus;
                    userProfilePhotoLikeResponse.groupChatId = groupChatId;
                    userProfilePhotoLikeResponse.lastUserLoginTime = myUserInfo.lastUserLoginTime;
                    userProfilePhotoLikeResponse.likeId = key;

                    FireBaseHelper.getInstance().getDatabaseReference().child(PHOTO_LIKE).child(key).setValue(userProfilePhotoLikeResponse);

                }

            }
        });

    }

}
