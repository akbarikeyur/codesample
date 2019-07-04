package com.socialedapp.fragment.events;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.socialedapp.R;
import com.socialedapp.activity.ActBase;
import com.socialedapp.activity.MyApplication;
import com.socialedapp.adapter.PagerAdapterMapList;
import com.socialedapp.customview.SlideDisableViewpager;
import com.socialedapp.fragment.FragBase;
import com.socialedapp.framework.eventresponse.EventResponse;
import com.socialedapp.framework.signup.RegisterResponse;
import com.socialedapp.framework.signup.UserFilter;
import com.socialedapp.helper.Constant;
import com.socialedapp.helper.Debug;
import com.socialedapp.helper.FireBaseHandler.FireBaseHelper;
import com.socialedapp.helper.PreferenceField;
import com.socialedapp.helper.Utils;
import com.socialedapp.helper.permissionutils.PermissionHandler;
import com.socialedapp.helper.permissionutils.PermissionInterface;
import com.socialedapp.receiver.LocationUpdateReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

import static com.socialedapp.helper.Constant.MYEVENT;
import static com.socialedapp.helper.Constant.USERS;

/**
 * Created by Keyur on 10-Apr-17.
 */
public class FragAllEventMain extends FragBase {

    public ImageView imgEventListMap;
    private PermissionHandler permissionHandler;
    private PagerAdapterMapList pagerAdaterMapList;
    private SlideDisableViewpager viewPager;
    private UserFilter userFilter = new UserFilter();
    private FrameLayout frmFiltered, frmInvited, frmMarked, frmJoined, frmCreated;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_all_event_main, null);

        setupView(view);

        return view;
    }

    public void setUserFilterData(UserFilter userFilter) {
        showIroidLoader();
        this.userFilter = userFilter;
        filterEventData();
    }

    public void setUserFilterFromRecommanded(UserFilter userFilter) {
        this.userFilter = userFilter;
    }

    private void setupView(View view) {

        setMenuTitle(getString(R.string.app_name));
        setFilterVisible(true);
        setAllEventSelected(true);

        pagerAdaterMapList = new PagerAdapterMapList(getChildFragmentManager(), this);
        viewPager = (SlideDisableViewpager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdaterMapList);
        viewPager.setOffscreenPageLimit(pagerAdaterMapList.getCount());
        viewPager.setPagingEnabled(false);
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (viewPager.getCurrentItem() == 1) {
                    imgEventListMap.setSelected(true);
                } else {
                    imgEventListMap.setSelected(false);
                }
            }
        });

        imgEventListMap = (ImageView) view.findViewById(R.id.imgEventListMap);

        activity.findViewById(R.id.lnrBottom).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                activity.findViewById(R.id.lnrBottom).getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.bottomMargin = activity.findViewById(R.id.lnrBottom).getHeight();
                layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.map_float_right_margin);

                imgEventListMap.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imgEventListMap.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        imgEventListMap.setLayoutParams(layoutParams);
                    }
                });

            }
        });


        imgEventListMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startAnimation();
                if (imgEventListMap.isSelected()) {
                    imgEventListMap.setSelected(false);
                    viewPager.setCurrentItem(0, false);
                } else {
                    viewPager.setCurrentItem(1, false);
                    imgEventListMap.setSelected(true);
                }
            }
        });

        frmFiltered = (FrameLayout) view.findViewById(R.id.frmFiltered);
        frmFiltered.setOnClickListener(filteredClick);

        frmInvited = (FrameLayout) view.findViewById(R.id.frmInvited);
        frmInvited.setOnClickListener(invitedEventClick);

        frmMarked = (FrameLayout) view.findViewById(R.id.frmMarked);
        frmMarked.setOnClickListener(markedEventClick);

        frmJoined = (FrameLayout) view.findViewById(R.id.frmJoined);
        frmJoined.setOnClickListener(joinedEventClick);

        frmCreated = (FrameLayout) view.findViewById(R.id.frmCreated);
        frmCreated.setOnClickListener(createdEventClick);

        permissionHandler = PermissionHandler.getInstance(activity);

        if (permissionHandler.checkPermission(PermissionHandler.ACCESS_FINE_LOCATION)) {
            loadFirstTimeData();
        }

    }

    public void updateEventFilter(String name, String value) {

        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put(name, value);

        FireBaseHelper.getInstance().getDatabaseReference().child(USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("userFilter")
                .updateChildren(hashMap);

        filterEventData();

    }

    View.OnClickListener filteredClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.getPref(activity, PreferenceField.IGNORE_FILTERED_EVENT, Constant.NO).equalsIgnoreCase(Constant.YES)) {
                Utils.setPref(activity, PreferenceField.IGNORE_FILTERED_EVENT, Constant.NO);
            } else {
                Utils.setPref(activity, PreferenceField.IGNORE_FILTERED_EVENT, Constant.YES);
            }
            bindEventFilterData();
            filterEventData();
        }
    };

    View.OnClickListener createdEventClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (userFilter.ignoreMyEvent.equalsIgnoreCase(Constant.YES)) {
                userFilter.ignoreMyEvent = Constant.NO;
                Utils.setPref(activity, PreferenceField.IGNORE_MY_EVENT, Constant.NO);
                updateEventFilter("ignoreMyEvent", "" + Constant.NO);
            } else {
                userFilter.ignoreMyEvent = Constant.YES;
                Utils.setPref(activity, PreferenceField.IGNORE_MY_EVENT, Constant.YES);
                updateEventFilter("ignoreMyEvent", "" + Constant.YES);
            }
            bindEventFilterData();
        }
    };

    View.OnClickListener joinedEventClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (userFilter.ignoreJoinedEvent.equalsIgnoreCase(Constant.YES)) {
                userFilter.ignoreJoinedEvent = Constant.NO;
                Utils.setPref(activity, PreferenceField.IGNORE_JOINED_EVENT, Constant.NO);
                updateEventFilter("ignoreJoinedEvent", "" + Constant.NO);
            } else {
                userFilter.ignoreJoinedEvent = Constant.YES;
                Utils.setPref(activity, PreferenceField.IGNORE_JOINED_EVENT, Constant.YES);
                updateEventFilter("ignoreJoinedEvent", "" + Constant.YES);
            }
            bindEventFilterData();
        }
    };

    View.OnClickListener markedEventClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (userFilter.ignoreMarkedEvent.equalsIgnoreCase(Constant.YES)) {
                userFilter.ignoreMarkedEvent = Constant.NO;
                Utils.setPref(activity, PreferenceField.IGNORE_MARKED_EVENT, Constant.NO);
                updateEventFilter("ignoreMarkedEvent", "" + Constant.NO);
            } else {
                userFilter.ignoreMarkedEvent = Constant.YES;
                Utils.setPref(activity, PreferenceField.IGNORE_MARKED_EVENT, Constant.YES);
                updateEventFilter("ignoreMarkedEvent", "" + Constant.YES);
            }
            bindEventFilterData();
        }
    };

    View.OnClickListener invitedEventClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (userFilter.ignoreInvitedEvent.equalsIgnoreCase(Constant.YES)) {
                userFilter.ignoreInvitedEvent = Constant.NO;
                Utils.setPref(activity, PreferenceField.IGNORE_INVITED_EVENT, Constant.NO);
                updateEventFilter("ignoreInvitedEvent", "" + Constant.NO);
            } else {
                userFilter.ignoreInvitedEvent = Constant.YES;
                Utils.setPref(activity, PreferenceField.IGNORE_INVITED_EVENT, Constant.YES);
                updateEventFilter("ignoreInvitedEvent", "" + Constant.YES);
            }
            bindEventFilterData();
        }
    };

    private DatabaseReference databaseReference;
    private ArrayList<EventResponse> eventResponsesList = new ArrayList<>();
    private ArrayList<EventResponse> tempEventResponsesList = new ArrayList<>();
    private RegisterResponse registerResponse;

    private void getAllEventData(final Location location) {

        FireBaseHelper.getInstance().getUserTable().orderByChild("userId")
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                            registerResponse = snapshot.getValue(RegisterResponse.class);

                            if (registerResponse != null && activity != null) {

                                if (Utils.getPref(activity, PreferenceField.IS_APP_LAUNCH, true)) {

                                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                    hashMap.put("latitude", "" + location.getLatitude());
                                    hashMap.put("longitude", "" + location.getLongitude());

                                    snapshot.getRef().child("userFilter")
                                            .child("filterRegisterUserLocation")
                                            .updateChildren(hashMap);

                                    hashMap.put("lastUserLoginTime", Constant.DOT_GREEN + "");

                                    snapshot.getRef().updateChildren(hashMap);

                                    Utils.canceAlarm(activity, LocationUpdateReceiver.LOCATION_UPDATE_RECEIVER_INTENT);

                                    if (registerResponse.isRadar.equalsIgnoreCase(Constant.YES)) {
                                        Utils.setAlarm(activity, LocationUpdateReceiver.LOCATION_UPDATE_RECEIVER_INTENT, Constant.ALARM_TIME_INTERVAL);
                                    }

                                    Utils.setPref(activity, PreferenceField.LATITUDE, location.getLatitude() + "");

                                    Utils.setPref(activity, PreferenceField.LONGITUDE, location.getLongitude() + "");

                                    Utils.setPref(activity, PreferenceField.IS_APP_LAUNCH, false);

                                }

                                userFilter = registerResponse.userFilter;

                                Utils.setPref(activity, PreferenceField.IGNORE_MY_EVENT, userFilter.ignoreMyEvent);
                                Utils.setPref(activity, PreferenceField.IGNORE_JOINED_EVENT, userFilter.ignoreJoinedEvent);
                                Utils.setPref(activity, PreferenceField.IGNORE_MARKED_EVENT, userFilter.ignoreMarkedEvent);
                                Utils.setPref(activity, PreferenceField.IGNORE_INVITED_EVENT, userFilter.ignoreInvitedEvent);

                                databaseReference = FireBaseHelper.getInstance().getDatabaseReference().child(MYEVENT);
                                databaseReference.addListenerForSingleValueEvent(valueEventListener);
                                databaseReference.addChildEventListener(chilValueListener);

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

    private ChildEventListener chilValueListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            EventResponse eventResponse = dataSnapshot.getValue(EventResponse.class);

            for (int i = 0; i < eventResponsesList.size(); i++) {

                if (eventResponsesList.get(i).eventId.equalsIgnoreCase(eventResponse.eventId)) {
                    eventResponsesList.set(i, eventResponse);
                    filterEventData();
                    break;
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            EventResponse eventResponse = dataSnapshot.getValue(EventResponse.class);

            for (int i = 0; i < eventResponsesList.size(); i++) {

                if (eventResponsesList.get(i).eventId.equalsIgnoreCase(eventResponse.eventId)) {
                    eventResponsesList.remove(eventResponsesList.get(i));
                    filterEventData();
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

    private ValueEventListener valueEventListener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (dataSnapshot.getValue() != null) {

                eventResponsesList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventResponse eventResponse = snapshot.getValue(EventResponse.class);
                    if (eventResponse != null && (eventResponse.eventType.equalsIgnoreCase(Constant.EVENT_NORMAL))) {
                        eventResponsesList.add(eventResponse);
                    }
                }

            }

            filterEventData();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            closeIroidLoader();
        }
    };

    private void filterEventData() {

        tempEventResponsesList = new ArrayList<>();

        for (int i = 0; i < eventResponsesList.size(); i++) {

            EventResponse eventResponse = eventResponsesList.get(i);

            if (Utils.isEventExpired(eventResponse.eventDateTime)) {
                continue;
            }

            Debug.e("eventResponse", "eventResponse " + eventResponse.eventTitle);

            if (eventResponse.rememberEventUserId.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                if (!((ActBase) activity).isIgnoreMarkedEvent()) {
                    tempEventResponsesList.add(eventResponsesList.get(i));
                }
            } else if (eventResponse.userId.equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                if (!((ActBase) activity).isIgnoreMyEvent()) {
                    if (((MyApplication) activity.getApplicationContext()).getRestFilterToAddEvent(activity, eventResponse, userFilter)) {
                        tempEventResponsesList.add(eventResponsesList.get(i));
                    }
                }
            } else if (eventResponse.joinMembersList.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                if (!((ActBase) activity).isIgnoreJoinEvent()) {
                    if (((MyApplication) activity.getApplicationContext()).getRestFilterToAddEvent(activity, eventResponse, userFilter)) {
                        tempEventResponsesList.add(eventResponsesList.get(i));
                    }
                }
            } else if (eventResponse.pendingInvitedEventUserId.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                if (!((ActBase) activity).isIgnoreInvitedEvent()) {
                    if (((MyApplication) activity.getApplicationContext()).getRestFilterToAddEvent(activity, eventResponse, userFilter)) {
                        tempEventResponsesList.add(eventResponsesList.get(i));
                    }
                }
            } else if (!((ActBase) activity).isIgnoreFilteredEvent()) {
                if (((MyApplication) activity.getApplicationContext()).getRestFilterToAddEvent(activity, eventResponse, userFilter)) {
                    tempEventResponsesList.add(eventResponsesList.get(i));
                }
            }

        }

        bindMapListData(tempEventResponsesList);

        bindEventFilterData();

        closeIroidLoader();

    }

    private void bindEventFilterData() {

        if (userFilter.ignoreMyEvent.equalsIgnoreCase(Constant.YES)) {
            frmCreated.setForeground(getResources().getDrawable(R.color.white_transparent, null));
        } else {
            frmCreated.setForeground(getResources().getDrawable(android.R.color.transparent, null));
        }

        if (userFilter.ignoreJoinedEvent.equalsIgnoreCase(Constant.YES)) {
            frmJoined.setForeground(getResources().getDrawable(R.color.white_transparent, null));
        } else {
            frmJoined.setForeground(getResources().getDrawable(android.R.color.transparent, null));
        }

        if (userFilter.ignoreMarkedEvent.equalsIgnoreCase(Constant.YES)) {
            frmMarked.setForeground(getResources().getDrawable(R.color.white_transparent, null));
        } else {
            frmMarked.setForeground(getResources().getDrawable(android.R.color.transparent, null));
        }

        if (userFilter.ignoreInvitedEvent.equalsIgnoreCase(Constant.YES)) {
            frmInvited.setForeground(getResources().getDrawable(R.color.white_transparent, null));
        } else {
            frmInvited.setForeground(getResources().getDrawable(android.R.color.transparent, null));
        }

        if (Utils.getPref(activity, PreferenceField.IGNORE_FILTERED_EVENT, Constant.NO).equalsIgnoreCase(Constant.YES)) {
            frmFiltered.setForeground(getResources().getDrawable(R.color.white_transparent, null));
        } else {
            frmFiltered.setForeground(getResources().getDrawable(android.R.color.transparent, null));
        }

    }

    private void bindMapListData(ArrayList<EventResponse> tempEventResponsesList) {

        FragAllEventMap fragAllEventMap = (FragAllEventMap) getFragmentForPosition(0);

        if (fragAllEventMap != null) {
            fragAllEventMap.setMyUserInfo(registerResponse);
            fragAllEventMap.setEventData(tempEventResponsesList);
//            fragAllEventMap.updateMapLocation();
        }

        FragAllEventList fragAllEventList = (FragAllEventList) getFragmentForPosition(1);

        if (fragAllEventList != null) {
            fragAllEventList.setEventData(tempEventResponsesList);
            fragAllEventList.setEventMainClassReference(this);
        }
    }

    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(5);

    private void startAnimation() {

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(imgEventListMap, View.SCALE_X, 0.6f, 1f);
        bounceAnimX.setDuration(200);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(imgEventListMap, View.SCALE_Y, 0.6f, 1f);
        bounceAnimY.setDuration(200);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }

    private void loadFirstTimeData() {

        if (SmartLocation.with(activity).location().state().locationServicesEnabled()) {

            showIroidLoader();

            SmartLocation.with(activity).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {

                            if (activity != null) {

                                SmartLocation.with(activity).location().stop();

                                new UpdateUserCity(location).execute();

                                getAllEventData(location);

                            } else {
                                closeIroidLoader();
                            }

                        }
                    });

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
    }

    private class UpdateUserCity extends AsyncTask<String, String, String> {

        private Location location;

        UpdateUserCity(Location location) {
            this.location = location;
        }

        @Override
        protected String doInBackground(String... params) {
            updateUserCity(location);
            return null;
        }
    }

    private void updateUserCity(Location location) {

        Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
        List<Address> addresses;
        try {

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses != null && addresses.size() > 0) {

                final String cityName = addresses.get(0).getLocality();

                if (cityName != null) {

                    FireBaseHelper.getInstance().getDatabaseReference().child(USERS)
                            .orderByChild("userId")
                            .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                        hashMap.put("location", "" + cityName);
                                        snapshot.getRef().updateChildren(hashMap);

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults, new PermissionInterface() {
            @Override
            public void permissionGranted(String permission) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadFirstTimeData();
                    }
                }, 500);
            }

            @Override
            public void permissionDenied(String permission) {

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setAllEventSelected(false);
        if (databaseReference != null) {
            databaseReference.removeEventListener(valueEventListener);
            databaseReference.removeEventListener(chilValueListener);
        }
    }

    public static String makeFragmentName(int containerViewId, long position) {
        return "android:switcher:" + containerViewId + ":" + position;
    }

    public Fragment getFragmentForPosition(int position) {
        String tag = makeFragmentName(viewPager.getId(), position);
        return getChildFragmentManager().findFragmentByTag(tag);
    }
}
