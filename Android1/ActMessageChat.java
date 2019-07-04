package com.socialedapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.socialedapp.R;
import com.socialedapp.adapter.AdapterMessagesChat;
import com.socialedapp.adapter.adapteritemclickinterface.OnItemClickListener;
import com.socialedapp.commoninterface.EmojiItemClick;
import com.socialedapp.customview.CustomEmojiView;
import com.socialedapp.framework.blockuser.BlockUserResponse;
import com.socialedapp.framework.chatmessages.ChatMessageListResponse;
import com.socialedapp.framework.chatmessages.ChatMessagesResponse;
import com.socialedapp.framework.feednotification.FeedNotificationResponse;
import com.socialedapp.framework.friendlist.FriendsListResponse;
import com.socialedapp.framework.signup.RegisterResponse;
import com.socialedapp.framework.userprivacysetting.UserPrivacySettingResponse;
import com.socialedapp.helper.Constant;
import com.socialedapp.helper.FireBaseHandler.FireBaseHelper;
import com.socialedapp.helper.PreferenceField;
import com.socialedapp.helper.UriHelper;
import com.socialedapp.helper.Utils;
import com.socialedapp.helper.permissionutils.PermissionHandler;
import com.socialedapp.helper.permissionutils.PermissionInterface;
import com.socialedapp.pushnotification.SendPushNotificationClass;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;

import static com.socialedapp.activity.ActShowPhotos.PROFILE_PHOTO;
import static com.socialedapp.helper.Constant.CHAT_MESSAGES;
import static com.socialedapp.helper.Constant.CHAT_MESSAGE_LIST;
import static com.socialedapp.helper.Constant.ChatImages;
import static com.socialedapp.helper.Constant.FRIENDS_LIST;
import static com.socialedapp.helper.Constant.USER_PRIVACY_SETTING;

/**
 * Created by Keyur on 10-May-17.
 */

public class ActMessageChat extends ActBase {

    private TextView txtUserName;
    private ImageView imgBack;
    private RecyclerView rcvGroupChat;
    private AdapterMessagesChat adapterGroupChat;
    private EditText edtMessage;
    private Query query;
    private Query queryRemoveChatId;
    private ImageView imgPhoto, imgProfile, imgEmoji, imgSendMessage;
    private ChatMessageListResponse friendsListResponse;
    private RegisterResponse frdRegisterResponse;
    private RegisterResponse myUserInfo;
    private CustomEmojiView emojiView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_message_chat);

        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        ((MyApplication) getApplicationContext()).setChatUserId(friendsListResponse.frd_userId);
        Utils.setPref(getActivity(), PreferenceField.OPEN_NEW_CHAT_MESSAGE_SCREEN, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((MyApplication) getApplicationContext()).setChatUserId("");
        Utils.setPref(getActivity(), PreferenceField.OPEN_NEW_CHAT_MESSAGE_SCREEN, false);
    }

    private void initView() {

        friendsListResponse = (ChatMessageListResponse) getIntent().getSerializableExtra("chat");

        if (friendsListResponse != null) {

            ((MyApplication) getApplicationContext()).setChatUserId(friendsListResponse.frd_userId);
            Utils.setPref(getActivity(), PreferenceField.OPEN_NEW_CHAT_MESSAGE_SCREEN, true);

            permissionHandler = PermissionHandler.getInstance(getActivity());

            txtUserName = (TextView) findViewById(R.id.txtUserName);
            txtUserName.setOnClickListener(profileClick);

            imgSendMessage = (ImageView) findViewById(R.id.imgSendMessage);
            imgSendMessage.setOnClickListener(sendMessageClick);

            imgProfile = (ImageView) findViewById(R.id.imgProfile);
            imgProfile.setOnClickListener(profileClick);

            imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
            imgPhoto.setOnClickListener(photoClick);

            imgBack = (ImageView) findViewById(R.id.imgBack);
            imgBack.setOnClickListener(backClick);

            edtMessage = (EditText) findViewById(R.id.edtMessage);

            adapterGroupChat = new AdapterMessagesChat(getActivity());
            rcvGroupChat = (RecyclerView) findViewById(R.id.rcvGroupChat);
            rcvGroupChat.setItemAnimator(new DefaultItemAnimator());

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            linearLayoutManager.setStackFromEnd(true);
            rcvGroupChat.setLayoutManager(linearLayoutManager);
            rcvGroupChat.getItemAnimator().setChangeDuration(0);
            rcvGroupChat.setAdapter(adapterGroupChat);
            adapterGroupChat.setOnItemClickListener(itemClick);
            adapterGroupChat.setonProfileClick(profileClick);

            StickyHeaderDecoration decor = new StickyHeaderDecoration(adapterGroupChat);
            rcvGroupChat.addItemDecoration(decor);

            query = FireBaseHelper.getInstance().getDatabaseReference().child(CHAT_MESSAGES);
            query = query.orderByChild("chatId").equalTo(friendsListResponse.chatId);

            queryRemoveChatId = FireBaseHelper.getInstance().getDatabaseReference().child(CHAT_MESSAGE_LIST);
            queryRemoveChatId = queryRemoveChatId.orderByChild("chatId").equalTo(friendsListResponse.chatId);
            queryRemoveChatId.addChildEventListener(removeChatIdListener);

            bindData();

            queryblockUser = FireBaseHelper.getInstance().getDatabaseReference().child(Constant.BLOCK_USER);
            queryblockUser = queryblockUser.orderByChild("blockToUser")
                    .equalTo(friendsListResponse.frd_userId + "_" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            queryblockUser.addChildEventListener(childEventListener);


            edtMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeEmojiView();
                }
            });

            edtMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        closeEmojiView();
                    }
                }
            });

            imgEmoji = (ImageView) findViewById(R.id.imgEmoji);
            imgEmoji.setOnClickListener(emojiClick);

            emojiView = (CustomEmojiView) findViewById(R.id.emojiView);
            emojiView.bindEmojiData(getSupportFragmentManager());
            emojiView.setVisibility(View.GONE);
            emojiView.emojiClick(new EmojiItemClick() {
                @Override
                public void emojiItemClick(String name) {
                    closeEmojiView();
                    sendEmojiToDatabase(name);
                }
            });

        } else {
            finish();
        }

    }

    View.OnClickListener sendMessageClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendMessage();
        }
    };

    private void sendEmojiToDatabase(String emojiImage) {

        DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference();
        String chatMessageId = databaseReference.push().getKey();

        ChatMessagesResponse chatMessagesResponse = new ChatMessagesResponse();
        chatMessagesResponse.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
        chatMessagesResponse.message = getString(R.string.emoji);
        chatMessagesResponse.chatId = friendsListResponse.chatId;
        chatMessagesResponse.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatMessagesResponse.profileImage = myUserInfo.profileImage;
        chatMessagesResponse.chatMessageId = chatMessageId;
        chatMessagesResponse.hasImage = Constant.YES;
        chatMessagesResponse.chatImage = "";
        chatMessagesResponse.emojiName = emojiImage;

        databaseReference.child(CHAT_MESSAGES).child(chatMessageId)
                .setValue(chatMessagesResponse);

        updateLastMessageWithUnreadCount(chatMessagesResponse);

    }

    View.OnClickListener emojiClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (emojiView.getVisibility() != View.VISIBLE) {
                Utils.hideKeyboard(getActivity(), edtMessage.getWindowToken());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        emojiView.setVisibility(View.VISIBLE);
                        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
                        emojiView.startAnimation(animation);

                    }
                }, 150);
            }
        }
    };

    private void closeEmojiView() {
        if (emojiView.getVisibility() == View.VISIBLE) {
            emojiView.setVisibility(View.GONE);
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
            emojiView.startAnimation(animation);
        }
    }

    ChildEventListener removeChatIdListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            finish();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private View.OnClickListener profileClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getTag() != null && frdRegisterResponse != null) {

                int position = (int) v.getTag();

                if (!adapterGroupChat.getAdapterData().get(position).userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    gotoOtherUserProfile(adapterGroupChat.getAdapterData().get(position).userId);
                }

            } else {
                gotoOtherUserProfile(frdRegisterResponse.userId);
            }
        }
    };


    private Query queryblockUser;

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            BlockUserResponse blockUserResponse = dataSnapshot.getValue(BlockUserResponse.class);

            if (blockUserResponse.blockToUser.equalsIgnoreCase(friendsListResponse.frd_userId + "_" + FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                finish();
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        queryRemoveChatId.removeEventListener(removeChatIdListener);
        queryblockUser.removeEventListener(childEventListener);
        Utils.setPref(getActivity(), PreferenceField.OPEN_NEW_CHAT_MESSAGE_SCREEN, false);
        ((MyApplication) getApplicationContext()).setChatUserId("");
        query.removeEventListener(messageChildValueListener);
        FireBaseHelper.getInstance().getDatabaseReference().child(CHAT_MESSAGE_LIST)
                .orderByChild("chatId")
                .equalTo(friendsListResponse.chatId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            FriendsListResponse friendsListResponse1 = snapshot.getValue(FriendsListResponse.class);

                            if (friendsListResponse1.frd_userId.equalsIgnoreCase(friendsListResponse.frd_userId)) {
                                HashMap<String, Object> stringStringHashMap = new HashMap<String, Object>();
                                stringStringHashMap.put("unreadMessageCount", "0");
                                snapshot.getRef().updateChildren(stringStringHashMap);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    ChildEventListener messageChildValueListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            ChatMessagesResponse chatMessagesResponse = dataSnapshot.getValue(ChatMessagesResponse.class);
            adapterGroupChat.add(chatMessagesResponse);
            if (adapterGroupChat.getItemCount() > 0) {
                rcvGroupChat.scrollToPosition(adapterGroupChat.getItemCount() - 1);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private PermissionHandler permissionHandler;

    View.OnClickListener photoClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!checkUserSendMessage()) {
                return;
            }

            uri_capture = null;
            fileProfilePic = null;
            closeEmojiView();
            new MaterialDialog.Builder(getActivity()).items(R.array.profile).title(R.string.profile).itemsCallback(new MaterialDialog.ListCallback() {
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
    };

    private Uri uri_capture = null;
    private File fileProfilePic = null;

    private void chooseGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, Constant.PICK_GALLERY);

    }

    private void chooseCamera() {

        uri_capture = Utils.getOutputMediaFileUri(getActivity());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri_capture);
        startActivityForResult(intent, Constant.PICK_CAMERA);

    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == getActivity().RESULT_OK) {

            if (requestCode == Constant.PICK_GALLERY) {
                if (data != null) {
                    String image_path = UriHelper.getPath(getActivity(), data.getData());
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
                String chatMessageId = databaseReference.push().getKey();

                ChatMessagesResponse chatMessagesResponse = new ChatMessagesResponse();
                chatMessagesResponse.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
                chatMessagesResponse.message = getString(R.string.Image);
                chatMessagesResponse.chatId = friendsListResponse.chatId;
                chatMessagesResponse.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatMessagesResponse.profileImage = myUserInfo.profileImage;
                chatMessagesResponse.chatMessageId = chatMessageId;
                chatMessagesResponse.hasImage = Constant.YES;
                chatMessagesResponse.chatImage = downloadUrl.toString();
                chatMessagesResponse.emojiName = "";

                databaseReference.child(CHAT_MESSAGES).child(chatMessageId)
                        .setValue(chatMessagesResponse);

                closeIroidLoader();

                updateLastMessageWithUnreadCount(chatMessagesResponse);

            }
        });
    }

    private void updateLastMessageWithUnreadCount(final ChatMessagesResponse chatMessagesResponse) {

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(CHAT_MESSAGE_LIST)
                .orderByChild("chatId")
                .equalTo(friendsListResponse.chatId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            FriendsListResponse friendsListResponse = snapshot.getValue(FriendsListResponse.class);

                            HashMap<String, Object> lastMessageHashMap = new HashMap<String, Object>();
                            lastMessageHashMap.put("lastMessage", "" + chatMessagesResponse.message);
                            lastMessageHashMap.put("lastMessageDateTime", "" + chatMessagesResponse.messageDate);

                            if (!chatMessagesResponse.emojiName.equalsIgnoreCase("")) {
                                lastMessageHashMap.put("emojiName", "" + chatMessagesResponse.emojiName);
                            } else {
                                lastMessageHashMap.put("emojiName", "");
                            }

                            if (!friendsListResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                int unreadMessageCount = Integer.parseInt(friendsListResponse.unreadMessageCount);
                                unreadMessageCount = unreadMessageCount + 1;
                                lastMessageHashMap.put("unreadMessageCount", "" + unreadMessageCount);
                            }

                            snapshot.getRef().updateChildren(lastMessageHashMap);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

        if (frdRegisterResponse != null && myUserInfo != null) {

            FeedNotificationResponse feedNotificationClass = new FeedNotificationResponse();
            feedNotificationClass.firstName = myUserInfo.firstName;
            feedNotificationClass.lastName = myUserInfo.lastName;
            feedNotificationClass.userId = myUserInfo.userId;
            feedNotificationClass.profileImage = myUserInfo.profileImage;
            feedNotificationClass.nType = Constant.TYPE_NEW_CHAT_MESSAGE;
            feedNotificationClass.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
            feedNotificationClass.receiverUserId = frdRegisterResponse.userId;

            SendPushNotificationClass.getInstance().sendPush(frdRegisterResponse.deviceToken
                    , feedNotificationClass.getObject());

        }

    }

    private boolean checkUserSendMessage() {

        if (otherUserPrivacySettingResponse.messageOnlyByFriends.equalsIgnoreCase(Constant.YES)) {
            if (!IsFriend) {
                Utils.ShowToast(getActivity(), R.string.you_can_not_send_msg_this_user);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private void sendMessage() {

        if (!checkUserSendMessage()) {
            return;
        }

        final String message = edtMessage.getText().toString().trim();

        if (message.length() > 0) {

            edtMessage.setText("");

            final DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference();
            String chatMessageId = databaseReference.push().getKey();

            final ChatMessagesResponse chatMessagesResponse = new ChatMessagesResponse();
            chatMessagesResponse.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
            chatMessagesResponse.message = message;
            chatMessagesResponse.chatId = friendsListResponse.chatId;
            chatMessagesResponse.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            chatMessagesResponse.profileImage = myUserInfo.profileImage;
            chatMessagesResponse.chatMessageId = chatMessageId;
            chatMessagesResponse.hasImage = Constant.NO;
            chatMessagesResponse.chatImage = "";


            Utils.hideKeyboard(getActivity(), edtMessage.getWindowToken());

            databaseReference.child(CHAT_MESSAGES).child(chatMessageId)
                    .setValue(chatMessagesResponse);

            updateLastMessageWithUnreadCount(chatMessagesResponse);

        }
    }

    OnItemClickListener itemClick = new OnItemClickListener() {
        @Override
        public void onItemClickListener(int position) {

            if (adapterGroupChat.getAdapterData().get(position).hasImage.equalsIgnoreCase(Constant.YES)) {

                String groupChatImage = adapterGroupChat.getAdapterData().get(position).chatImage;

                if (groupChatImage != null && groupChatImage.length() > 0) {
                    Intent intent = new Intent(getActivity(), ActShowPhotos.class);
                    intent.putExtra("data", adapterGroupChat.getAdapterData().get(position));
                    intent.putExtra(PROFILE_PHOTO, PROFILE_PHOTO);
                    startActivity(intent);
                }
            }
        }
    };

    View.OnClickListener backClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    private void bindData() {

        txtUserName.setText(friendsListResponse.frd_firstName + " " + friendsListResponse.frd_lastName);

        if (friendsListResponse.frd_profileImage.length() > 0) {
            Picasso.with(getActivity()).load(friendsListResponse.frd_profileImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgProfile);
        }


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

        FireBaseHelper.getInstance().getDatabaseReference().child(Constant.USERS)
                .orderByChild("userId")
                .equalTo(friendsListResponse.frd_userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            frdRegisterResponse = snapshot.getValue(RegisterResponse.class);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(CHAT_MESSAGE_LIST)
                .orderByChild("chatId")
                .equalTo(friendsListResponse.chatId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            FriendsListResponse friendsListResponse = snapshot.getValue(FriendsListResponse.class);
                            if (friendsListResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                snapshot.getRef().child("unreadMessageCount").setValue("0");
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        showIroidLoader();

        query.addChildEventListener(messageChildValueListener);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (adapterGroupChat.getItemCount() > 0) {
                    rcvGroupChat.scrollToPosition(adapterGroupChat.getItemCount() - 1);
                }
                checkIsAbleToSendMessage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                closeIroidLoader();
            }
        });

    }


    private boolean IsFriend = false;

    private void checkIsAbleToSendMessage() {

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(FRIENDS_LIST)
                .orderByChild("userId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            FriendsListResponse friendsListResponse = snapshot.getValue(FriendsListResponse.class);

                            if (friendsListResponse.frd_userId.equalsIgnoreCase(ActMessageChat.this.friendsListResponse.frd_userId)) {
                                IsFriend = true;
                                break;
                            }
                        }

                        getOtherUserPrivacySetting();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        closeIroidLoader();
                    }
                });

    }

    private UserPrivacySettingResponse otherUserPrivacySettingResponse;

    private void getOtherUserPrivacySetting() {

        FireBaseHelper.getInstance().getDatabaseReference().child(USER_PRIVACY_SETTING)
                .orderByChild("userId")
                .equalTo(friendsListResponse.frd_userId)
                .addValueEventListener(valueEventListenerPrivacySetting);

    }

    private ValueEventListener valueEventListenerPrivacySetting = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                otherUserPrivacySettingResponse = snapshot.getValue(UserPrivacySettingResponse.class);
            }
            closeIroidLoader();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            closeIroidLoader();
        }
    };

}
