package com.socialedapp.fragment.events.alleventdetail;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.socialedapp.R;
import com.socialedapp.activity.ActEventCompany;
import com.socialedapp.activity.ActEventMapFull;
import com.socialedapp.activity.ActInviteEventToFriendsGroups;
import com.socialedapp.activity.ActShowPhotos;
import com.socialedapp.adapter.AdapterEventGroupChatLastMessage;
import com.socialedapp.adapter.adapteritemclickinterface.OnItemClickListener;
import com.socialedapp.commoninterface.UpdateCommentInterface;
import com.socialedapp.customview.MoreImageView;
import com.socialedapp.customview.RippleBackground;
import com.socialedapp.customview.WorkaroundMapFragment;
import com.socialedapp.fragment.FragBase;
import com.socialedapp.fragment.events.FragAllEventDetailMain;
import com.socialedapp.fragment.events.FragEventComment;
import com.socialedapp.fragment.member.FragEventMemberList;
import com.socialedapp.framework.company.CompanyResponse;
import com.socialedapp.framework.eventresponse.EventResponse;
import com.socialedapp.framework.feednotification.FeedNotificationResponse;
import com.socialedapp.framework.friendlist.FriendsListResponse;
import com.socialedapp.framework.groupchatevent.EventChatAllGalleryResponse;
import com.socialedapp.framework.groupchatevent.EventChatMessageGalleryResponse;
import com.socialedapp.framework.memberlist.EventMemberListResponse;
import com.socialedapp.framework.pendinginviteeventresponse.PendingInviteEventResponse;
import com.socialedapp.framework.pengingjoineventresponse.PendingJoinEventResponse;
import com.socialedapp.framework.rateevent.RateEventResponse;
import com.socialedapp.framework.signup.RegisterResponse;
import com.socialedapp.framework.userprivacysetting.UserPrivacySettingResponse;
import com.socialedapp.helper.Constant;
import com.socialedapp.helper.Debug;
import com.socialedapp.helper.FacebookHelper;
import com.socialedapp.helper.FireBaseHandler.FireBaseHelper;
import com.socialedapp.helper.Utils;
import com.socialedapp.pushnotification.SendPushNotificationClass;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.socialedapp.activity.ActShowPhotos.GALLERY_PHOTO;
import static com.socialedapp.helper.Constant.COMPANY;
import static com.socialedapp.helper.Constant.EVENT_COMMENT;
import static com.socialedapp.helper.Constant.EVENT_MEMBER_LIST;
import static com.socialedapp.helper.Constant.FRIENDS_LIST;
import static com.socialedapp.helper.Constant.GROUP_CHAT_EVENT;
import static com.socialedapp.helper.Constant.GROUP_CHAT_GALLERY;
import static com.socialedapp.helper.Constant.GROUP_CHAT_LAST_TWO_MESSAGE;
import static com.socialedapp.helper.Constant.MYEVENT;
import static com.socialedapp.helper.Constant.PENDING_INVITE_EVENT;
import static com.socialedapp.helper.Constant.PENDING_JOIN_EVENT;
import static com.socialedapp.helper.Constant.TYPE_USER_JOIN_EVENT;
import static com.socialedapp.helper.Constant.TYPE_USER_JOIN_YOUR_EVENT;
import static com.socialedapp.helper.Constant.USERS;
import static com.socialedapp.helper.Constant.USER_PRIVACY_SETTING;
import static com.socialedapp.helper.Utils.getCurrentLatitude;

/**
 * Created by Keyur on 11-Apr-17.
 */
public class FragAllEventDetailInformation extends FragBase implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView txtJoinMeeting;
    private ImageView imgEventCreatedProfile;
    private FrameLayout frmMap;
    private NestedScrollView nestedScrollView;
    private MoreImageView moreImageView;
    private EventResponse eventResponse;
    private TextView txtEventTitle, txtEventAddress, txtEventDescription, txtDistance, txtEventDateTime, txtCreatedByEventUserName;
    private TextView txtDescReadMore, txtMemberSeeAll, txtInviteFriends;
    private DatabaseReference databaseReference;
    private LinearLayout lnrMemberList;
    private ImageView imgProfileEvent;
    private RegisterResponse eventCreatedUserResponse;
    private TextView txtMemberTotalList;
    private ImageView imgFullMapView;
    private LinearLayout lnrEventCreate;
    private TextView txtDirection;
    private FragAllEventDetailMain fragAllEventDetailMain;
    private ImageView imgPhoto1, imgPhoto2, imgPhoto3, imgPhoto4;
    private LinearLayout lnrPhotos;
    private TextView txtPhotosCount;
    private FrameLayout frmMorePhotos;
    private RecyclerView rcvEventGroupChat;
    private AdapterEventGroupChatLastMessage adapterGroupChat;
    private ImageView imgUploadNewPhoto, imgGroupChat;
    private LinearLayout lnrNestedLayout, lnrBottomDetail;
    private ImageView imgTempEvent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_all_event_detail_information, null);

        setupView(view);

        return view;
    }

    public FragAllEventDetailInformation setEventData(EventResponse eventResponse, FragAllEventDetailMain fragAllEventDetailMain) {
        this.eventResponse = eventResponse;
        this.fragAllEventDetailMain = fragAllEventDetailMain;
        return this;
    }

    View.OnClickListener fullMapViewClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(activity, ActEventMapFull.class);
            intent.putExtra("data", eventResponse);
            startActivity(intent);

        }
    };

    private void setupView(View view) {

        txtDirection = (TextView) view.findViewById(R.id.txtDirection);
        txtDirection.setOnClickListener(directionClick);

        imgTempEvent = (ImageView) view.findViewById(R.id.imgTempEvent);

        imgFullMapView = (ImageView) view.findViewById(R.id.imgFullMapView);
        imgFullMapView.setOnClickListener(fullMapViewClick);

        FacebookHelper.getInstance(getActivity());

        databaseReference = FireBaseHelper.getInstance().getDatabaseReference();

        lnrEventCreate = (LinearLayout) view.findViewById(R.id.lnrEventCreate);
        lnrEventCreate.setOnClickListener(profileClick);

        fragAllEventDetailMain.imgRememberEvent.setOnClickListener(rememberEventClick);

        fragAllEventDetailMain.imgShare.setVisibility(View.INVISIBLE);
        fragAllEventDetailMain.imgShare.setOnClickListener(shareEventClick);

        imgProfileEvent = (ImageView) view.findViewById(R.id.imgProfileEvent);

        txtInviteFriends = (TextView) view.findViewById(R.id.txtInviteFriends);
        txtInviteFriends.setOnClickListener(inviteFriendsClick);


        SupportMapFragment mapFragment = (WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ((WorkaroundMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).setListener(new WorkaroundMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {
                nestedScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        nestedScrollView = (NestedScrollView) view.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                frmMap.setTranslationY(scrollY * 0.5f);
            }
        });

        txtDescReadMore = (TextView) view.findViewById(R.id.txtDescReadMore);
        txtDescReadMore.setOnClickListener(readMoreDescClick);

        moreImageView = (MoreImageView) view.findViewById(R.id.moreImageView);

        frmMap = (FrameLayout) view.findViewById(R.id.frmMap);

        txtJoinMeeting = (TextView) view.findViewById(R.id.txtJoinMeeting);
        txtJoinMeeting.setOnClickListener(joinMeetingClick);


        fragAllEventDetailMain.lnrComment.setOnClickListener(commentClick);

        txtEventTitle = (TextView) view.findViewById(R.id.txtEventTitle);

        txtEventAddress = (TextView) view.findViewById(R.id.txtEventAddress);

        txtEventDescription = (TextView) view.findViewById(R.id.txtEventDescription);

        txtDistance = (TextView) view.findViewById(R.id.txtDistance);

        txtEventDateTime = (TextView) view.findViewById(R.id.txtEventDateTime);

        imgEventCreatedProfile = (ImageView) view.findViewById(R.id.imgEventCreatedProfile);

        txtCreatedByEventUserName = (TextView) view.findViewById(R.id.txtCreatedByEventUserName);

        txtMemberTotalList = (TextView) view.findViewById(R.id.txtMemberTotalList);

        lnrMemberList = (LinearLayout) view.findViewById(R.id.lnrMemberList);
        lnrMemberList.setVisibility(View.GONE);

        txtMemberSeeAll = (TextView) view.findViewById(R.id.txtMemberSeeAll);
        txtMemberSeeAll.setOnClickListener(memberSeeAllClick);

        imgPhoto1 = (ImageView) view.findViewById(R.id.imgPhoto1);
        imgPhoto1.setOnClickListener(photo1Click);

        imgPhoto2 = (ImageView) view.findViewById(R.id.imgPhoto2);
        imgPhoto2.setOnClickListener(photo2Click);

        imgPhoto3 = (ImageView) view.findViewById(R.id.imgPhoto3);
        imgPhoto3.setOnClickListener(photo3Click);

        imgPhoto4 = (ImageView) view.findViewById(R.id.imgPhoto4);
        imgPhoto4.setOnClickListener(photo4Click);

        lnrPhotos = (LinearLayout) view.findViewById(R.id.lnrPhotos);
        lnrPhotos.setVisibility(View.GONE);

        frmMorePhotos = (FrameLayout) view.findViewById(R.id.frmMorePhotos);
        frmMorePhotos.setOnClickListener(morePhotosClick);

        imgGroupChat = (ImageView) view.findViewById(R.id.imgGroupChat);
        imgGroupChat.setOnClickListener(groupChatClick);

        imgUploadNewPhoto = (ImageView) view.findViewById(R.id.imgUploadNewPhoto);
        imgUploadNewPhoto.setOnClickListener(newPhotoUploadClick);

        txtPhotosCount = (TextView) view.findViewById(R.id.txtPhotosCount);

        lnrBottomDetail = (LinearLayout) view.findViewById(R.id.lnrBottomDetail);

        lnrNestedLayout = (LinearLayout) view.findViewById(R.id.lnrNestedLayout);
        lnrBottomDetail.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                lnrBottomDetail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                lnrNestedLayout.setPadding(0, 0, 0, (int) (lnrBottomDetail.getHeight() * (1.5)));
            }
        });

        adapterGroupChat = new AdapterEventGroupChatLastMessage(activity);
        rcvEventGroupChat = (RecyclerView) view.findViewById(R.id.rcvEventGroupChat);
        rcvEventGroupChat.setItemAnimator(new DefaultItemAnimator());

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        rcvEventGroupChat.setLayoutManager(linearLayoutManager);
        rcvEventGroupChat.setNestedScrollingEnabled(false);
        rcvEventGroupChat.setAdapter(adapterGroupChat);
        adapterGroupChat.setOnItemClickListener(itemClick);
        adapterGroupChat.setProfileClick(chatProfileClick);

        getCommentCount();

        bindEventData();

        getGroupChatGallery();

        getLastMessage();

    }

    View.OnClickListener chatProfileClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() != null) {
                int position = (int) v.getTag();
                if (!adapterGroupChat.getAdapterData().get(position).userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    gotoOtherUserProfile(adapterGroupChat.getAdapterData().get(position).userId);
                }
            }
        }
    };

    OnItemClickListener itemClick = new OnItemClickListener() {
        @Override
        public void onItemClickListener(int position) {

        }
    };

    View.OnClickListener photo1Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoGalleryPhotos("" + 0);
        }
    };

    View.OnClickListener photo2Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoGalleryPhotos("" + 1);
        }
    };


    View.OnClickListener photo3Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoGalleryPhotos("" + 2);
        }
    };

    View.OnClickListener photo4Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoGalleryPhotos("" + 3);
        }
    };

    View.OnClickListener morePhotosClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            fragAllEventDetailMain.gotoGalleryView();
        }
    };

    private void gotoGalleryPhotos(String position) {

        Type type = new TypeToken<List<EventChatAllGalleryResponse>>() {
        }.getType();
        String json = new Gson().toJson(galleryList, type);

        Intent intent = new Intent(activity, ActShowPhotos.class);
        intent.putExtra("data", json);
        intent.putExtra("position", position);
        intent.putExtra(GALLERY_PHOTO, GALLERY_PHOTO);
        intent.putExtra(ActShowPhotos.FROM_EVENT, ActShowPhotos.FROM_EVENT);
        startActivity(intent);

    }

    private Query queryLastMessage;

    private void getLastMessage() {

        queryLastMessage = FireBaseHelper.getInstance().getDatabaseReference().child(GROUP_CHAT_EVENT).child(GROUP_CHAT_LAST_TWO_MESSAGE);
        queryLastMessage = queryLastMessage
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId);

        queryLastMessage.addValueEventListener(valueEventListener);

    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            ArrayList<EventChatMessageGalleryResponse> list = new ArrayList<>();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                EventChatMessageGalleryResponse groupChatResponse = snapshot.getValue(EventChatMessageGalleryResponse.class);
                list.add(groupChatResponse);

            }
            adapterGroupChat.addAll(list);

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        galleryQuery.removeEventListener(getValueEventListener);
        queryLastMessage.removeEventListener(valueEventListener);
    }

    private Query galleryQuery;
    private ArrayList<EventChatAllGalleryResponse> galleryList = new ArrayList<>();

    private void getGroupChatGallery() {

        galleryQuery = FireBaseHelper.getInstance().getDatabaseReference().child(GROUP_CHAT_EVENT).child(GROUP_CHAT_GALLERY);
        galleryQuery = galleryQuery.orderByChild("eventId").equalTo(eventResponse.eventId);
        galleryQuery.addValueEventListener(getValueEventListener);

    }

    ValueEventListener getValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            galleryList.clear();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                EventChatAllGalleryResponse eventChatAllGalleryResponse = snapshot.getValue(EventChatAllGalleryResponse.class);
                galleryList.add(eventChatAllGalleryResponse);
            }

            if (activity != null) {
                bindPhotos();
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private void bindPhotos() {

        if (galleryList.size() == 0) {
            lnrPhotos.setVisibility(View.GONE);
            return;
        }

        lnrPhotos.setVisibility(View.VISIBLE);

        imgPhoto1.setVisibility(View.INVISIBLE);
        imgPhoto1.setImageResource(0);

        imgPhoto2.setVisibility(View.INVISIBLE);
        imgPhoto2.setImageResource(0);

        imgPhoto3.setVisibility(View.INVISIBLE);
        imgPhoto3.setImageResource(0);

        imgPhoto4.setVisibility(View.INVISIBLE);
        imgPhoto4.setImageResource(0);

        Collections.reverse(galleryList);

        fragAllEventDetailMain.getFragEventDetailGallery().addAllImages(galleryList);

        frmMorePhotos.setVisibility(View.INVISIBLE);

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        if (galleryList.size() >= 1) {

            imgPhoto1.setVisibility(View.VISIBLE);
            if (galleryList.get(0).groupChatImage.length() > 0) {
                Picasso.with(getActivity()).load(galleryList.get(0).groupChatImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgPhoto1);
            }

        }

        if (galleryList.size() >= 2) {
            imgPhoto2.setVisibility(View.VISIBLE);
            if (galleryList.get(1).groupChatImage.length() > 0) {
                Picasso.with(getActivity()).load(galleryList.get(1).groupChatImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgPhoto2);
            }
        }

        if (galleryList.size() >= 3) {
            imgPhoto3.setVisibility(View.VISIBLE);
            if (galleryList.get(2).groupChatImage.length() > 0) {
                Picasso.with(getActivity()).load(galleryList.get(2).groupChatImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgPhoto3);
            }
        }

        if (galleryList.size() >= 4) {
            imgPhoto4.setVisibility(View.VISIBLE);
            if (galleryList.get(3).groupChatImage.length() > 0) {
                Picasso.with(getActivity()).load(galleryList.get(3).groupChatImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgPhoto4);
            }
        }

        if (galleryList.size() >= 5) {
            frmMorePhotos.setVisibility(View.VISIBLE);
            txtPhotosCount.setVisibility(View.VISIBLE);
            txtPhotosCount.setText("+" + (galleryList.size() - 4));

        } else {
            frmMorePhotos.setVisibility(View.INVISIBLE);
            txtPhotosCount.setVisibility(View.INVISIBLE);
        }


    }

    View.OnClickListener newPhotoUploadClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(imgUploadNewPhoto, View.SCALE_X, 0.6f, 1f);
            bounceAnimX.setDuration(200);
            bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(imgUploadNewPhoto, View.SCALE_Y, 0.6f, 1f);
            bounceAnimY.setDuration(200);
            bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);

            animatorSet.play(bounceAnimX).with(bounceAnimY);
            animatorSet.start();
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fragAllEventDetailMain.getFragEventDetailChat().imgPhoto.performClick();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        }
    };

    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(5);

    View.OnClickListener groupChatClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(imgGroupChat, View.SCALE_X, 0.6f, 1f);
            bounceAnimX.setDuration(200);
            bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

            ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(imgGroupChat, View.SCALE_Y, 0.6f, 1f);
            bounceAnimY.setDuration(200);
            bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);

            animatorSet.play(bounceAnimX).with(bounceAnimY);
            animatorSet.start();
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    fragAllEventDetailMain.gotoChatView();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

        }
    };

    View.OnClickListener directionClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {

                String curr_lat = getCurrentLatitude(activity) + "";
                String curr_lon = Utils.getCurrentLongitude(activity) + "";

                String event_lat = eventResponse.latitude + "";
                String event_lon = eventResponse.longitude + "";

                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + curr_lat + "," + curr_lon + "&daddr=" + event_lat + "," + event_lon + ""));
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    View.OnClickListener profileClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (eventResponse.isEventCreatedByCompany.equalsIgnoreCase(Constant.NO)) {
                if (!eventResponse.createdByEvent.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    gotoOtherUserProfile(eventResponse.createdByEvent.userId);
                }
            } else {
                gotoEventCompanyPage();
            }
        }
    };

    private void gotoEventCompanyPage() {

        FireBaseHelper.getInstance().getDatabaseReference().child(COMPANY)
                .orderByChild("companyId").equalTo(eventResponse.userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            CompanyResponse companyResponse = snapshot.getValue(CompanyResponse.class);

                            if (companyResponse != null) {

                                Intent intent = new Intent(activity, ActEventCompany.class);
                                intent.putExtra("company", companyResponse);
                                intent.putExtra("eventId", eventResponse.eventId + "");
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }

    View.OnClickListener shareEventClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int array = 0;

            if (eventResponse.shareEventOnFB.equalsIgnoreCase(Constant.YES)) {
                array = R.array.share_option;
            } else {
                array = R.array.report_option;
            }

            new MaterialDialog.Builder(activity).items(array)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {

                            if (eventResponse.shareEventOnFB.equalsIgnoreCase(Constant.YES)) {
                                if (position == 0) {
                                    eventShareOnFB();
                                } else if (position == 1) {
                                    eventShareOnWhatsApp();
                                } else {

                                }

                            } else {
                                if (position == 0) {

                                }
                            }


                        }
                    }).show();
        }
    };

    private void eventShareOnWhatsApp() {

        String date = Utils.parseTime(eventResponse.eventDateTime, "dd MMM yyyy 'at' hh:mm a", "dd MMM yyyy");

        String message = eventResponse.createdByEvent.firstName + " " +
                getString(R.string.share_fb_option_msg).replace("@eventname", eventResponse.eventTitle).replace("@date", date);

        PackageManager pm = activity.getPackageManager();

        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = message;

            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, text + " https://startPlaying.google.com/store/apps/details?id=" + activity.getPackageName());
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(activity, R.string.whatsapp_not_install, Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private void eventShareOnFB() {

        String date = Utils.parseTime(eventResponse.eventDateTime, "dd MMM yyyy 'at' hh:mm a", "dd MMM yyyy");

        String message = eventResponse.createdByEvent.firstName + " " +
                getString(R.string.share_fb_option_msg).replace("@eventname", eventResponse.eventTitle).replace("@date", date);

        ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                .setContentTitle(getString(R.string.app_name))
                .setContentDescription(message)
                .setContentUrl(Uri.parse("https://developers.facebook.com"))
                .build();

        ShareDialog.show(activity, shareLinkContent);

    }

    View.OnClickListener rememberEventClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!fragAllEventDetailMain.imgRememberEvent.isSelected()) {
                fragAllEventDetailMain.imgRememberEvent.setSelected(true);
                updateRememberUserId(true);
            } else {
                fragAllEventDetailMain.imgRememberEvent.setSelected(false);
                updateRememberUserId(false);
            }
        }
    };

    private void updateRememberUserId(final boolean IsRemember) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(MYEVENT)
                .child(eventResponse.eventId)
                .child("rememberEventUserId");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                if (IsRemember) {
                    userListIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }

                if (mutableData.getValue() == null) {
                    mutableData.setValue(userListIds);
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (IsRemember) {
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

    private void checkRateEvent() {

        if (Utils.isEventExpired(eventResponse.eventDateTime)) {

            FireBaseHelper.getInstance().getDatabaseReference().child(Constant.RATE_EVENT)
                    .orderByChild("eventIdRateUserId")
                    .equalTo(eventResponse.eventId + "_" + FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getChildrenCount() == 0) {
                                showRateEventDialog();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        }


    }

    private LinearLayout lnrSkip;

    private void showRateEventDialog() {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);

        View rateEventDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_rate_event, null, false);

        final RippleBackground rippleBackground = (RippleBackground) rateEventDialogView.findViewById(R.id.contentProfile);
        rippleBackground.startRippleAnimation();

        final RippleBackground rippleBackground1 = (RippleBackground) rateEventDialogView.findViewById(R.id.contentEventImage);
        rippleBackground1.startRippleAnimation();

        ImageView imgEventCreaterProfile = (ImageView) rateEventDialogView.findViewById(R.id.imgEventCreaterProfile);
        if (eventResponse.createdByEvent.profileImage.length() > 0) {
            Picasso.with(getActivity()).load(eventResponse.createdByEvent.profileImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgEventCreaterProfile);
        }


        ImageView imgEvent = (ImageView) rateEventDialogView.findViewById(R.id.imgEvent);
        imgEvent.setImageResource(Utils.getEmojiResourceFromName(activity, eventResponse.eventImage));

        ImageView imgThumsUp = (ImageView) rateEventDialogView.findViewById(R.id.imgThumsUp);
        imgThumsUp.setOnClickListener(thumsUpClick);

        ImageView imgThumsDown = (ImageView) rateEventDialogView.findViewById(R.id.imgThumsDown);
        imgThumsDown.setOnClickListener(thumsDownClick);

        lnrSkip = (LinearLayout) rateEventDialogView.findViewById(R.id.lnrSkip);
        lnrSkip.setOnClickListener(skipClick);

        TextView txtInvitationMsg = (TextView) rateEventDialogView.findViewById(R.id.txtInvitationMsg);
        txtInvitationMsg.setText(Html.fromHtml(getString(R.string.event_rate_msg)
                .replace("@EventName", "" + eventResponse.eventTitle)));

        materialDialog = builder.customView(rateEventDialogView, false).build();
        Utils.setWindowManagerCustom(materialDialog);
        materialDialog.show();

    }

    View.OnClickListener thumsUpClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rateEvent(true);
        }
    };

    View.OnClickListener thumsDownClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            rateEvent(false);
        }
    };

    private void rateEvent(boolean Thumsup) {

        DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference().child(Constant.RATE_EVENT);

        String rateId = databaseReference.push().getKey();

        RateEventResponse rateEventResponse = new RateEventResponse();
        rateEventResponse.rateId = rateId;
        rateEventResponse.eventId = eventResponse.eventId;
        rateEventResponse.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rateEventResponse.eventIdRateUserId = eventResponse.eventId + "_" + FirebaseAuth.getInstance().getCurrentUser().getUid();
        rateEventResponse.eventCreatedUserId = eventResponse.createdByEvent.userId;

        if (Thumsup) {
            rateEventResponse.rate = Constant.THUMS_UP;
        } else {
            rateEventResponse.rate = Constant.THUMS_DOWN;
        }

        databaseReference.child(rateId).setValue(rateEventResponse);

        lnrSkip.performClick();

    }


    View.OnClickListener skipClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            materialDialog.dismiss();
        }
    };

    View.OnClickListener inviteFriendsClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (!Utils.isEventExpired(eventResponse.eventDateTime)) {

                if (eventResponse != null && txtJoinMeeting.getText().toString().trim().equalsIgnoreCase(getString(R.string.unjoin_meeting))) {
                    if (materialDialog != null) {
                        materialDialog.dismiss();
                        materialDialog = null;
                    }
                    Intent intent = new Intent(activity, ActInviteEventToFriendsGroups.class);
                    intent.putExtra("eventData", eventResponse);
                    startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_in_bottom, 0);
                }
            } else {
                Utils.ShowToast(activity, R.string.event_expired);
            }

        }
    };

    private void getCommentCount() {

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(EVENT_COMMENT)
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fragAllEventDetailMain.txtCommentCount.setText("" + dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    View.OnClickListener memberSeeAllClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (memberList.size() > 0) {

                FragEventMemberList fragment = new FragEventMemberList();
                fragment.setMemberListData(memberList);
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                ft.addToBackStack(FragEventMemberList.class.getName());
                ft.add(R.id.fragContainer, fragment, FragEventMemberList.class.getName());
                ft.commit();

            }
        }
    };

    View.OnClickListener readMoreDescClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (txtDescReadMore.getText().toString().trim().equalsIgnoreCase(getString(R.string.read_less))) {
                txtDescReadMore.setText(getString(R.string.read_more));
                txtEventDescription.setSingleLine(true);
            } else {
                txtDescReadMore.setText(getString(R.string.read_less));
                txtEventDescription.setSingleLine(false);
            }

        }
    };

    private void bindEventData() {

        if (eventResponse != null) {

            if (Utils.isEventExpired(eventResponse.eventDateTime)) {
                txtInviteFriends.setVisibility(View.INVISIBLE);
                txtJoinMeeting.setVisibility(View.INVISIBLE);
            } else {
                if (eventResponse.eventType.equalsIgnoreCase(Constant.EVENT_PRIVATE)
                        && eventResponse.allowFriendsToInvitetheirFriends.equalsIgnoreCase(Constant.NO)) {
                    txtInviteFriends.setVisibility(View.INVISIBLE);
                } else {
                    txtInviteFriends.setVisibility(View.VISIBLE);
                }
                txtJoinMeeting.setVisibility(View.VISIBLE);
            }

            txtEventTitle.setText(eventResponse.eventTitle);

            txtEventDescription.setText(eventResponse.eventDescription);

            txtEventDescription.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    txtEventDescription.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (txtEventDescription.getLineCount() > 1) {
                        txtDescReadMore.setVisibility(View.VISIBLE);
                        txtEventDescription.setSingleLine(true);
                    } else {
                        txtEventDescription.setSingleLine(false);
                        txtDescReadMore.setVisibility(View.INVISIBLE);
                    }
                }
            });

            txtEventAddress.setText(eventResponse.eventAddress);

            fragAllEventDetailMain.imgShare.setVisibility(View.VISIBLE);

            txtEventDateTime.setText(eventResponse.eventDateTime);

            if (eventResponse.latitude.length() > 0 && eventResponse.longitude.length() > 0) {

                txtDistance.setText(
                        Utils.calculateDistanceBetweenLocation(getCurrentLatitude(activity)
                                , Utils.getCurrentLongitude(activity)
                                , Float.parseFloat(eventResponse.latitude)
                                , Float.parseFloat(eventResponse.longitude)) + " " + getString(R.string.Km));
            }

            if (eventResponse.createdByEvent.profileImage.length() > 0) {
                Picasso.with(getActivity()).load(eventResponse.createdByEvent.profileImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgEventCreatedProfile);
            } else {
                imgEventCreatedProfile.setImageResource(R.drawable.avatar);
            }

            txtCreatedByEventUserName.setText(eventResponse.createdByEvent.firstName + " " + eventResponse.createdByEvent.lastName);

            imgProfileEvent.setImageResource(Utils.getEmojiResourceFromName(activity, eventResponse.eventImage));

            if (eventResponse.rememberEventUserId.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                fragAllEventDetailMain.imgRememberEvent.setSelected(true);
            } else {
                fragAllEventDetailMain.imgRememberEvent.setSelected(false);
            }

            fragAllEventDetailMain.imgRememberEvent.setVisibility(View.GONE);

            showIroidLoader();

            FireBaseHelper.getInstance().getDatabaseReference().child(USERS)
                    .orderByChild("userId")
                    .equalTo(eventResponse.userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                eventCreatedUserResponse = snapshot.getValue(RegisterResponse.class);
                            }
                            closeIroidLoader();
                            checkEventToJoinMeeting();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            closeIroidLoader();
                        }
                    });

        }
    }

    private void checkEventToJoinMeeting() {

        showIroidLoader();

        databaseReference
                .child(PENDING_JOIN_EVENT)
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                PendingJoinEventResponse pendingJoinEventResponse = snapshot.getValue(PendingJoinEventResponse.class);

                                if (pendingJoinEventResponse != null &&
                                        pendingJoinEventResponse.pendingUserId
                                                .equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    txtJoinMeeting.setSelected(false);
                                    txtJoinMeeting.setText(getString(R.string.pending_meeting));
                                    txtJoinMeeting.setEnabled(false);
                                    break;
                                }
                            }
                        }
                        getMemberList();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        closeIroidLoader();
                    }
                });

    }

    private ArrayList<EventMemberListResponse> memberList = new ArrayList<>();

    private void getMemberList() {

        databaseReference
                .child(EVENT_MEMBER_LIST)
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        memberList = new ArrayList<EventMemberListResponse>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            EventMemberListResponse memberListResponse = snapshot.getValue(EventMemberListResponse.class);

                            if (memberListResponse != null) {
                                memberList.add(memberListResponse);
                            }
                        }
                        bindMemberData();
                        closeIroidLoader();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        closeIroidLoader();
                    }
                });

    }

    private void bindMemberData() {

        if (isEventJoinbyMe()) {
            txtJoinMeeting.setSelected(true);
            txtJoinMeeting.setText(getString(R.string.unjoin_meeting));
            fragAllEventDetailMain.imgRememberEvent.setVisibility(View.GONE);
            checkRateEvent();
        } else {
            if (!txtJoinMeeting.getText().toString().trim().equalsIgnoreCase(getString(R.string.pending_meeting))) {
                txtJoinMeeting.setSelected(false);
                txtJoinMeeting.setText(getString(R.string.join_meeting));
                if (eventResponse.eventType.equalsIgnoreCase(Constant.EVENT_PRIVATE)) {
                    fragAllEventDetailMain.imgRememberEvent.setVisibility(View.GONE);
                } else {
                    fragAllEventDetailMain.imgRememberEvent.setVisibility(View.VISIBLE);
                }
            } else {
                fragAllEventDetailMain.imgRememberEvent.setVisibility(View.GONE);
            }
        }

        if (memberList.size() > 0) {
            lnrMemberList.setVisibility(View.VISIBLE);
            moreImageView.addUrlList(Utils.getEventMemberProfileUrlList(memberList));
        } else {
            lnrMemberList.setVisibility(View.GONE);
        }

        txtMemberTotalList.setText(memberList.size() + "/" + eventResponse.memberSize + " " + getString(R.string.members));

    }

    private boolean isEventJoinbyMe() {

        for (int i = 0; i < memberList.size(); i++) {
            if (memberList.get(i).userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                return true;
            }
        }
        return false;
    }

    View.OnClickListener commentClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            FragEventComment fragment = new FragEventComment();
            if (txtJoinMeeting.getText().toString().equalsIgnoreCase(getString(R.string.unjoin_meeting))) {
                fragment.setCommentVisible(true);
            } else {
                fragment.setCommentVisible(false);
            }
            fragment.setEventResponse(eventResponse);
            fragment.updateCommentInterface(updateCommentInterface);
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
            ft.addToBackStack(FragEventComment.class.getName());
            ft.add(R.id.fragContainer, fragment, FragEventComment.class.getName());
            ft.commit();
        }
    };

    UpdateCommentInterface updateCommentInterface = new UpdateCommentInterface() {
        @Override
        public void updateComment() {
            getCommentCount();
        }
    };


    View.OnClickListener joinMeetingClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (IsInternetConnected() && !Utils.isEventExpired(eventResponse.eventDateTime)) {
                checkMemberCanJoin();
            } else {
                Utils.ShowToast(activity, R.string.event_expired);
            }
        }
    };

    private void checkMemberCanJoin() {

        showIroidLoader();

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(MYEVENT)
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildrenCount() > 0) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                EventResponse eventResponse = snapshot.getValue(EventResponse.class);

                                if (eventResponse != null) {

                                    if (txtJoinMeeting.isSelected()) {
                                        closeIroidLoader();
                                        callUnjoinMeeting();
                                    } else {
                                        if (eventResponse.eventType.equalsIgnoreCase(Constant.EVENT_NORMAL)) {
                                            int totalMember = eventResponse.joinMembersList.size();
                                            int memberSize = Integer.parseInt(eventResponse.memberSize);
                                            if (totalMember < memberSize) {
                                                closeIroidLoader();
                                                callJoinMeetingEvent();
                                            } else {
                                                Utils.ShowToast(activity, getString(R.string.member_limi_reach));
                                                closeIroidLoader();
                                            }
                                        } else if (eventResponse.eventType.equalsIgnoreCase(Constant.EVENT_PRIVATE)) {
                                            callJoinMeetingEvent();
                                        } else {
                                            closeIroidLoader();
                                        }

                                    }

                                } else {
                                    closeIroidLoader();
                                }
                            }

                        } else {
                            Utils.ShowToast(activity, getString(R.string.delete_event_by_user));
                            closeIroidLoader();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        closeIroidLoader();
                    }
                });

    }


    private void callUnjoinMeeting() {

        showIroidLoader();

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(EVENT_MEMBER_LIST)
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            EventMemberListResponse memberListResponse = snapshot.getValue(EventMemberListResponse.class);

                            if (memberListResponse != null && memberListResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                snapshot.getRef().removeValue();

                                closeIroidLoader();

                                checkEventToJoinMeeting();

                                // TODO UPDATE MEMBER LIST
                                updateJoinMemberList(FirebaseAuth.getInstance().getCurrentUser().getUid(), false);

                                // TODO PENDING EVENT UPDATE JOIN MEMBER COUNT
                                updatePendingjoinEventMemberCount(false);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        closeIroidLoader();
                    }
                });


    }

    private void updatePendingjoinEventMemberCount(final boolean isIncrement) {

        FireBaseHelper.getInstance().getDatabaseReference()
                .child(PENDING_INVITE_EVENT)
                .orderByChild("eventId")
                .equalTo(eventResponse.eventId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            PendingInviteEventResponse inviteEventResponse = snapshot.getValue(PendingInviteEventResponse.class);

                            if (inviteEventResponse != null) {
                                updateTransactionForPendingEvent(isIncrement, inviteEventResponse);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

    private void updateTransactionForPendingEvent(final boolean isIncrement, PendingInviteEventResponse inviteEventResponse) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(PENDING_INVITE_EVENT)
                .child(inviteEventResponse.pendingEventId)
                .child("totalJoinMember");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue("1");
                } else {
                    String count = mutableData.getValue(String.class);
                    int totalCount = 0;
                    if (isIncrement) {
                        totalCount = Integer.parseInt(count) + 1;
                    } else {
                        totalCount = Integer.parseInt(count) - 1;
                    }
                    mutableData.setValue(totalCount + "");
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Debug.e("onComplete", "onComplete " + databaseError);
            }
        });


    }

    private void callJoinMeetingEvent() {

        showIroidLoader();

        databaseReference
                .child(USERS)
                .orderByChild("userId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildrenCount() == 0) {
                            closeIroidLoader();
                            return;
                        }

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            final RegisterResponse myUserInfo = snapshot.getValue(RegisterResponse.class);

                            if (myUserInfo != null) {

                                if (eventResponse.acceptMemberBeforeJoinEvent.equalsIgnoreCase(Constant.YES)) {

                                    String key = databaseReference.push().getKey();

                                    PendingJoinEventResponse pendingJoinEventResponse = new PendingJoinEventResponse();
                                    pendingJoinEventResponse.pendingJoinId = key;
                                    pendingJoinEventResponse.eventId = eventResponse.eventId;
                                    pendingJoinEventResponse.pendingUserId = myUserInfo.userId;
                                    pendingJoinEventResponse.firstName = myUserInfo.firstName;
                                    pendingJoinEventResponse.lastName = myUserInfo.lastName;
                                    pendingJoinEventResponse.profileImage = myUserInfo.profileImage;
                                    pendingJoinEventResponse.deviceToken = myUserInfo.deviceToken;
                                    pendingJoinEventResponse.lastUserLoginTime = myUserInfo.lastUserLoginTime;

                                    sendPushNotificationToEventCreatedUserAndAllFriendListToJoinEvent(myUserInfo, Constant.TYPE_WANT_TO_JOIN_EVENT);

                                    FireBaseHelper.getInstance().getDatabaseReference()
                                            .child(PENDING_JOIN_EVENT)
                                            .child(key)
                                            .setValue(pendingJoinEventResponse)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    txtJoinMeeting.setSelected(false);

                                                    txtJoinMeeting.setText(getString(R.string.pending_meeting));

                                                    txtJoinMeeting.setEnabled(false);

                                                    fragAllEventDetailMain.imgRememberEvent.setVisibility(View.GONE);

                                                    removeRememberUserIdMemberList(eventResponse, FirebaseAuth.getInstance().getCurrentUser().getUid(), false);

                                                    // TODO UPDATE MEMBER LIST
                                                    updatePendingJoinMemberList(myUserInfo.userId, true);

                                                    closeIroidLoader();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    closeIroidLoader();
                                                }
                                            });
                                } else {

                                    String key = databaseReference.push().getKey();

                                    EventMemberListResponse memberListResponse = new EventMemberListResponse();
                                    memberListResponse.memberListId = key;
                                    memberListResponse.eventId = eventResponse.eventId;
                                    memberListResponse.userId = myUserInfo.userId;
                                    memberListResponse.firstName = myUserInfo.firstName;
                                    memberListResponse.lastName = myUserInfo.lastName;
                                    memberListResponse.profileImage = myUserInfo.profileImage;
                                    memberListResponse.deviceToken = myUserInfo.deviceToken;
                                    memberListResponse.lastUserLoginTime = myUserInfo.lastUserLoginTime;
                                    memberListResponse.gender = myUserInfo.gender;

                                    if (eventResponse.isEventCreatedByCompany.equalsIgnoreCase(Constant.YES)) {
                                        memberListResponse.companyId = eventResponse.userId;
                                    }

                                    if (eventResponse.isEventCreatedByCompany.equalsIgnoreCase(Constant.NO)) {
                                        sendPushNotificationToEventCreatedUserAndAllFriendListToJoinEvent(myUserInfo, TYPE_USER_JOIN_EVENT);
                                    }

                                    databaseReference
                                            .child(EVENT_MEMBER_LIST)
                                            .child(key)
                                            .setValue(memberListResponse)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    // TODO UPDATE MEMBER LIST
                                                    updateJoinMemberList(myUserInfo.userId, true);

                                                    // TODO PENDING EVENT UPDATE JOIN MEMBER COUNT
                                                    updatePendingjoinEventMemberCount(true);

                                                    txtJoinMeeting.setSelected(true);
                                                    txtJoinMeeting.setText(getString(R.string.unjoin_meeting));

                                                    showJoinEventDialog();

                                                    getMemberList();

                                                    removeRememberUserIdMemberList(eventResponse, FirebaseAuth.getInstance().getCurrentUser().getUid(), false);

                                                    removeFromMyPendingEvent();

                                                    updateInvitedEventUserIdList(eventResponse.eventId, FirebaseAuth.getInstance().getCurrentUser().getUid(), false);

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    closeIroidLoader();
                                                }
                                            });


                                }

                            } else {
                                closeIroidLoader();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        closeIroidLoader();
                    }
                });
    }

    private void updateInvitedEventUserIdList(String eventId, final String userId, final boolean isAdd) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(MYEVENT)
                .child(eventId)
                .child("pendingInvitedEventUserId");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                if (isAdd) {
                    userListIds.add(userId);
                }

                if (mutableData.getValue() == null) {
                    mutableData.setValue(userListIds);
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (isAdd) {
                            userListIds.add(useridAdd);
                        } else {
                            if (!useridAdd.equalsIgnoreCase(userId)) {
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

    private void removeFromMyPendingEvent() {

        FireBaseHelper.getInstance().getDatabaseReference().child(PENDING_INVITE_EVENT).orderByChild("toUserId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PendingInviteEventResponse inviteEventResponse = snapshot.getValue(PendingInviteEventResponse.class);
                            if (inviteEventResponse != null && inviteEventResponse.eventId.equalsIgnoreCase(eventResponse.eventId)) {
                                snapshot.getRef().removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void removeRememberUserIdMemberList(EventResponse eventResponse, final String myUserId, final boolean isAdd) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(MYEVENT)
                .child(eventResponse.eventId)
                .child("rememberEventUserId");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                if (isAdd) {
                    userListIds.add(myUserId);
                }

                if (mutableData.getValue() == null) {
                    mutableData.setValue(userListIds);
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (isAdd) {
                            userListIds.add(useridAdd);
                        } else {
                            if (!useridAdd.equalsIgnoreCase(myUserId)) {
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

    private MaterialDialog materialDialog;
    private LinearLayout lnrInviteFriendsDialog, lnrCancel;

    private void showJoinEventDialog() {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);

        View createEventDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_join_event, null, false);

        final RippleBackground rippleBackground = (RippleBackground) createEventDialogView.findViewById(R.id.contentProfile);
        rippleBackground.startRippleAnimation();

        final RippleBackground rippleBackground1 = (RippleBackground) createEventDialogView.findViewById(R.id.contentEventImage);
        rippleBackground1.startRippleAnimation();

        ImageView imgEventCreaterProfile = (ImageView) createEventDialogView.findViewById(R.id.imgEventCreaterProfile);
        if (eventResponse.createdByEvent.profileImage.length() > 0) {
            Picasso.with(getActivity()).load(eventResponse.createdByEvent.profileImage).resize(250, 250).centerCrop().onlyScaleDown().into(imgEventCreaterProfile);
        }

        TextView txtTitle = (TextView) createEventDialogView.findViewById(R.id.txtTitle);
        txtTitle.setText(getString(R.string.join_event_title).replace("@eventname", "" + eventResponse.eventTitle));

        TextView txtShareFacebook = (TextView) createEventDialogView.findViewById(R.id.txtShareFacebook);
        if (eventResponse.shareEventOnFB.equalsIgnoreCase(Constant.YES)) {
            txtShareFacebook.setVisibility(View.VISIBLE);
        } else {
            txtShareFacebook.setVisibility(View.INVISIBLE);
        }

        txtShareFacebook.setOnClickListener(shareEventFacebookClick);

        ImageView imgEvent = (ImageView) createEventDialogView.findViewById(R.id.imgEvent);
        imgEvent.setImageResource(Utils.getEmojiResourceFromName(activity, eventResponse.eventImage));

        lnrCancel = (LinearLayout) createEventDialogView.findViewById(R.id.lnrCancel);
        lnrCancel.setOnClickListener(cancelClick);

        lnrInviteFriendsDialog = (LinearLayout) createEventDialogView.findViewById(R.id.lnrInviteFriendsDialog);
        lnrInviteFriendsDialog.setOnClickListener(inviteFriendsClick);

        materialDialog = builder.customView(createEventDialogView, false).build();
        Utils.setWindowManagerCustom(materialDialog);
        materialDialog.show();

    }

    View.OnClickListener shareEventFacebookClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            materialDialog.dismiss();
            materialDialog = null;

            String date = Utils.parseTime(eventResponse.eventDateTime, "dd MMM yyyy 'at' hh:mm a", "dd MMM yyyy");

            String message = eventResponse.createdByEvent.firstName + " " +
                    getString(R.string.share_fb_option_msg_join).replace("@eventname", eventResponse.eventTitle).replace("@date", date);

            ShareLinkContent shareLinkContent = new ShareLinkContent.Builder()
                    .setContentTitle(getString(R.string.app_name))
                    .setContentDescription(message)
                    .setContentUrl(Uri.parse("https://developers.facebook.com"))
                    .build();

            ShareDialog.show(activity, shareLinkContent);

        }
    };

    View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            materialDialog.dismiss();
            materialDialog = null;
        }
    };

    private void updatePendingJoinMemberList(final String myUserId, final boolean isAdd) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(MYEVENT)
                .child(eventResponse.eventId)
                .child("pendingJoinEventUserId");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                if (mutableData.getValue() == null) {

                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (isAdd) {
                            userListIds.add(useridAdd);
                        } else {
                            if (!useridAdd.equalsIgnoreCase(myUserId)) {
                                userListIds.add(useridAdd);
                            }
                        }
                    }

                }

                if (isAdd && !userListIds.contains(myUserId)) {
                    userListIds.add(myUserId);
                }
                mutableData.setValue(userListIds);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Debug.e("onComplete", "onComplete " + databaseError);
            }
        });

    }

    private void updateJoinMemberList(final String myUserId, final boolean isAdd) {

        DatabaseReference firebaseDatabase = (DatabaseReference) FireBaseHelper.getInstance().getDatabaseReference()
                .child(MYEVENT)
                .child(eventResponse.eventId)
                .child("joinMembersList");

        firebaseDatabase.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ArrayList<String> userListIds = new ArrayList<String>();

                if (isAdd) {
                    userListIds.add(myUserId);
                }

                if (mutableData.getValue() == null) {
                    mutableData.setValue(userListIds);
                } else {
                    for (MutableData mutableData1 : mutableData.getChildren()) {
                        String useridAdd = mutableData1.getValue(String.class);
                        if (isAdd) {
                            userListIds.add(useridAdd);
                        } else {
                            if (!useridAdd.equalsIgnoreCase(myUserId)) {
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

    private void sendPushNotificationToEventCreatedUserAndAllFriendListToJoinEvent(final RegisterResponse myUserInfo, final String type) {

        // TODO send Push Notification to Event Created User

        if (eventCreatedUserResponse != null) {

            FeedNotificationResponse feedNotificationClass = new FeedNotificationResponse();
            feedNotificationClass.firstName = myUserInfo.firstName;
            feedNotificationClass.lastName = myUserInfo.lastName;
            feedNotificationClass.userId = myUserInfo.userId;
            feedNotificationClass.profileImage = myUserInfo.profileImage;
            if (type.equals(TYPE_USER_JOIN_EVENT)) {
                feedNotificationClass.nType = TYPE_USER_JOIN_YOUR_EVENT;
            } else {
                feedNotificationClass.nType = type;
            }
            feedNotificationClass.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
            feedNotificationClass.receiverUserId = eventCreatedUserResponse.userId;
            feedNotificationClass.eventId = eventResponse.eventId;
            feedNotificationClass.eventTitle = eventResponse.eventTitle;
            feedNotificationClass.eventCreatedUserId = eventResponse.createdByEvent.userId;

            SendPushNotificationClass.getInstance().sendPush(eventCreatedUserResponse.deviceToken
                    , feedNotificationClass.getObject());

            Utils.saveFeedNotification(feedNotificationClass);

            // TODO User Privacy Setting Attending Feed If Yes

            if (type.equals(TYPE_USER_JOIN_EVENT)) {

                FireBaseHelper.getInstance().getDatabaseReference()
                        .child(USER_PRIVACY_SETTING)
                        .orderByChild("userId")
                        .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                    UserPrivacySettingResponse settingResponse = snapshot.getValue(UserPrivacySettingResponse.class);

                                    if (settingResponse.eventAttendingFeed.equals(Constant.YES)) {

                                        FireBaseHelper.getInstance().getDatabaseReference()
                                                .child(FRIENDS_LIST)
                                                .orderByChild("userId")
                                                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                                            FriendsListResponse friendsListResponse = snapshot.getValue(FriendsListResponse.class);

                                                            if (friendsListResponse.isBlock.equals(Constant.NO)
                                                                    && !friendsListResponse.frd_userId.equalsIgnoreCase(eventCreatedUserResponse.userId)) {

                                                                FeedNotificationResponse feedNotificationClass = new FeedNotificationResponse();
                                                                feedNotificationClass.firstName = myUserInfo.firstName;
                                                                feedNotificationClass.lastName = myUserInfo.lastName;
                                                                feedNotificationClass.userId = myUserInfo.userId;
                                                                feedNotificationClass.profileImage = myUserInfo.profileImage;
                                                                feedNotificationClass.nType = TYPE_USER_JOIN_EVENT;
                                                                feedNotificationClass.messageDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();
                                                                feedNotificationClass.receiverUserId = friendsListResponse.frd_userId;
                                                                feedNotificationClass.eventId = eventResponse.eventId;
                                                                feedNotificationClass.eventTitle = eventResponse.eventTitle;
                                                                feedNotificationClass.eventCreatedUserId = eventResponse.createdByEvent.userId;

                                                                Utils.saveFeedNotification(feedNotificationClass);

                                                            }


                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                    }


                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadMap(Utils.getLatitude(eventResponse.latitude), Utils.getLongitude(eventResponse.longitude));
    }

    private void loadMap(double latitude, double longitude) {

        if (activity == null) {
            return;
        }
        final LatLng latLng = new LatLng(latitude, longitude);


        Picasso.with(activity).load(Utils.getEmojiResourceFromName(activity, eventResponse.eventImage)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (activity != null) {
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(Utils.getMarkerBitmapFromView(activity, bitmap, Constant.MarkerColor.BLUE))));
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(15).build();
        mMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }
}
