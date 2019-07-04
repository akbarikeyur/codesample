package com.socialedapp.fragment.createvent;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.socialedapp.R;
import com.socialedapp.customview.multitagview.MultiTagView;
import com.socialedapp.fragment.FragBase;
import com.socialedapp.framework.eventresponse.CreatedByEvent;
import com.socialedapp.framework.eventresponse.EventResponse;
import com.socialedapp.framework.signup.RegisterResponse;
import com.socialedapp.helper.Constant;
import com.socialedapp.helper.FireBaseHandler.FireBaseHelper;
import com.socialedapp.helper.Utils;

import static com.socialedapp.helper.Constant.GENDER_ALL;
import static com.socialedapp.helper.Constant.GENDER_FEMALE;
import static com.socialedapp.helper.Constant.GENDER_MALE;
import static com.socialedapp.helper.Constant.GENDER_OTHER;
import static com.socialedapp.helper.Constant.MYEVENT;

/**
 * Created by Keyur on 11-Apr-17.
 */
public class FragCreatePublicEventSummary extends FragBase implements OnMapReadyCallback {

    private String TAG = FragCreatePublicEventSummary.class.getName();
    private LinearLayout lnrPostNow;
    private GoogleMap mMap;
    public MultiTagView tagView;
    private TextView txtEditTitle, txtEditAddress, txtEditDesc, txtEditDateTime, txtEditInterest, txtEditFilter;
    private TextView txtEventDateTime, txtEventTitle, txtEventAddress, txtEventDescription, txtAge, txtGender, txtMemberSize;
    private NestedScrollView nestedScrollView;
    private FrameLayout frmMap;
    private ImageView imgEvent;
    private Fragment fragment;

    public FragCreatePublicEventSummary getInstance(Fragment fragment) {
        this.fragment = fragment;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_create_public_event_summary, null);

        setupView(view);

        return view;
    }

    private void setupView(View view) {

        frmMap = (FrameLayout) view.findViewById(R.id.frmMap);

        imgEvent = (ImageView) view.findViewById(R.id.imgEvent);

        nestedScrollView = (NestedScrollView) view.findViewById(R.id.nestedScrollView);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                frmMap.setTranslationY(scrollY * .5f);
            }
        });

        txtEventDateTime = (TextView) view.findViewById(R.id.txtEventDateTime);

        txtEventTitle = (TextView) view.findViewById(R.id.txtEventTitle);

        txtEventAddress = (TextView) view.findViewById(R.id.txtEventAddress);

        txtEventDescription = (TextView) view.findViewById(R.id.txtEventDescription);

        txtEditTitle = (TextView) view.findViewById(R.id.txtEditTitle);
        txtEditTitle.setOnClickListener(editTitleClick);

        txtEditAddress = (TextView) view.findViewById(R.id.txtEditAddress);
        txtEditAddress.setOnClickListener(editAddressClick);

        txtEditDesc = (TextView) view.findViewById(R.id.txtEditDesc);
        txtEditDesc.setOnClickListener(edtDescClick);

        txtEditDateTime = (TextView) view.findViewById(R.id.txtEditDateTime);
        txtEditDateTime.setOnClickListener(editAddressClick);

        txtEditInterest = (TextView) view.findViewById(R.id.txtEditInterest);
        txtEditInterest.setOnClickListener(edtInterestClick);

        txtEditFilter = (TextView) view.findViewById(R.id.txtEditFilter);
        txtEditFilter.setOnClickListener(edtFilterClick);

        lnrPostNow = (LinearLayout) view.findViewById(R.id.lnrPostNow);
        lnrPostNow.setOnClickListener(postClick);

        tagView = (MultiTagView) view.findViewById(R.id.tag_view);

        txtAge = (TextView) view.findViewById(R.id.txtAge);
        txtAge.setText(getString(R.string.age_) + " 13-60");

        txtGender = (TextView) view.findViewById(R.id.txtGender);
        txtGender.setText(getString(R.string.gender_) + " " + getString(R.string.all));

        txtMemberSize = (TextView) view.findViewById(R.id.txtMemberSize);
        txtMemberSize.setText(getString(R.string.age_) + " 2-50");

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    View.OnClickListener editTitleClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fragment instanceof FragPublicEvent) {
                ((FragPublicEvent) fragment).gotoTitle();
            }

        }
    };

    View.OnClickListener editAddressClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fragment instanceof FragPublicEvent) {
                ((FragPublicEvent) fragment).gotoLocation();
            }
        }
    };

    View.OnClickListener edtDescClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fragment instanceof FragPublicEvent) {
                ((FragPublicEvent) fragment).gotoDesc();
            }
        }
    };

    View.OnClickListener edtInterestClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragChooseCategory fragment = new FragChooseCategory().getInstance(FragCreatePublicEventSummary.this, tagView.getTags());
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
            ft.addToBackStack(FragChooseCategory.class.getName());
            ft.add(R.id.fragContainer, fragment, FragChooseCategory.class.getName());
            ft.commit();
        }
    };

    View.OnClickListener edtFilterClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (fragment instanceof FragPublicEvent) {
                ((FragPublicEvent) fragment).gotoFilter();
            }
        }
    };

    View.OnClickListener postClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            showIroidLoader();

            final EventResponse myPostResponse = new EventResponse();
            myPostResponse.eventTitle = txtEventTitle.getText().toString().trim();
            myPostResponse.eventDescription = txtEventDescription.getText().toString().trim();
            myPostResponse.eventImage = fragCreateEventTitle.getEventImageName();
            myPostResponse.eventAddress = txtEventAddress.getText().toString().trim();
            myPostResponse.eventDateTime = txtEventDateTime.getText().toString().trim();
            myPostResponse.latitude = fragCreateEventLocation.getLatitude() + "";
            myPostResponse.longitude = fragCreateEventLocation.getLongitude() + "";
            myPostResponse.eventinterestList = tagView.getTags();
            myPostResponse.minAge = fragCreateEventFilter.getAgeSeekbar().getSelectedMinValue() + "";
            myPostResponse.maxAge = fragCreateEventFilter.getAgeSeekbar().getSelectedMaxValue() + "";

            if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_MALE)) {
                myPostResponse.gender = GENDER_MALE;
            } else if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_FEMALE)) {
                myPostResponse.gender = GENDER_FEMALE;
            } else if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_OTHER)) {
                myPostResponse.gender = GENDER_OTHER;
            } else if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_ALL)) {
                myPostResponse.gender = GENDER_ALL;
            }

            myPostResponse.memberSize = fragCreateEventFilter.getMemberSizeSeekbar().getSelectedMaxValue() + "";
            myPostResponse.acceptMemberBeforeJoinEvent = fragCreateEventFilter.getMemberBeforeJoinEvent();
            myPostResponse.shareEventOnFB = fragCreateEventFilter.getShareEventOnFB();
            myPostResponse.eventType = Constant.EVENT_NORMAL;
            myPostResponse.isEventCreatedByCompany = Constant.NO;
            myPostResponse.eventCreatedDate = "" + Utils.parseTimeDefaulttoUTC("dd/MM/yyyy HH:mm:ss").getTime();

            final DatabaseReference databaseReference = FireBaseHelper.getInstance().getDatabaseReference().child(MYEVENT);
            final String key = databaseReference.push().getKey();
            myPostResponse.eventId = key;

            FireBaseHelper.getInstance().getUserTable().orderByChild("userId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                RegisterResponse registerResponse = dataSnapshot1.getValue(RegisterResponse.class);

                                CreatedByEvent createdByEvent = new CreatedByEvent();
                                createdByEvent.userId = registerResponse.userId;
                                createdByEvent.firstName = registerResponse.firstName;
                                createdByEvent.lastName = registerResponse.lastName;
                                createdByEvent.profileImage = registerResponse.profileImage;
                                myPostResponse.createdByEvent = createdByEvent;
                                myPostResponse.userId = registerResponse.userId;

                                databaseReference.child(key).setValue(myPostResponse).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        eventPostFinish(myPostResponse);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }
    };

    private void eventPostFinish(EventResponse myPostResponse) {
        Intent intent = new Intent(Constant.CREATE_EVENT_RESULT);
        intent.putExtra("eventData", myPostResponse);
        activity.sendBroadcast(intent);
        activity.finish();

    }

    private FragCreateEventTitle fragCreateEventTitle;
    private FragCreateEventDescription fragCreateEventDescription;
    private FragCreateEventLocation fragCreateEventLocation;
    private FragCreateEventFilter fragCreateEventFilter;

    public void loadData() {

        if (fragment instanceof FragPublicEvent) {
            fragCreateEventTitle = (FragCreateEventTitle) ((FragPublicEvent) fragment).getFragmentForPosition(0);
        }


        if (fragCreateEventTitle != null) {
            txtEventTitle.setText(fragCreateEventTitle.getEventTitle());
            imgEvent.setImageResource(Utils.getEmojiResourceFromName(activity, fragCreateEventTitle.getEventImageName()));
        }

        if (fragment instanceof FragPublicEvent) {
            fragCreateEventDescription = (FragCreateEventDescription) ((FragPublicEvent) fragment).getFragmentForPosition(1);
        }

        if (fragCreateEventDescription != null) {
            txtEventDescription.setText(fragCreateEventDescription.getEventDescription());
        }
        if (fragment instanceof FragPublicEvent) {
            fragCreateEventLocation = (FragCreateEventLocation) ((FragPublicEvent) fragment).getFragmentForPosition(2);
        }

        if (fragCreateEventLocation != null) {

            txtEventDateTime.setText(fragCreateEventLocation.getDateTime());

            txtEventAddress.setText(fragCreateEventLocation.getAddress());

            loadMap(fragCreateEventLocation.getLatitude(), fragCreateEventLocation.getLongitude());
        }

        FragChooseCategory fragChooseCategory = (FragChooseCategory) activity.getSupportFragmentManager().findFragmentByTag(FragChooseCategory.class.getName());

        if (fragChooseCategory != null) {
            tagView.updateTags(fragChooseCategory.getInterestList());
        }
        if (fragment instanceof FragPublicEvent) {
            fragCreateEventFilter = (FragCreateEventFilter) ((FragPublicEvent) fragment).getFragmentForPosition(3);
        }

        if (fragCreateEventFilter != null) {

            txtAge.setText(getString(R.string.age_) + " " + fragCreateEventFilter.getAgeSeekbar().getSelectedMinValue() + " - " +
                    fragCreateEventFilter.getAgeSeekbar().getSelectedMaxValue() + "");

            if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_MALE)) {
                txtGender.setText(getString(R.string.gender_) + " " + getString(R.string.man));
            } else if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_FEMALE)) {
                txtGender.setText(getString(R.string.gender_) + " " + getString(R.string.female));
            } else if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_OTHER)) {
                txtGender.setText(getString(R.string.gender_) + " " + getString(R.string.other));
            } else if (fragCreateEventFilter.getGender().equalsIgnoreCase(GENDER_ALL)) {
                txtGender.setText(getString(R.string.gender_) + " " + getString(R.string.all));
            }

            txtMemberSize.setText(getString(R.string.membersize_) + " " + fragCreateEventFilter.getMemberSizeSeekbar().getSelectedMaxValue() + "");

        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void loadMap(double latitude, double longitude) {

        if (activity == null) {
            return;
        }

        mMap.clear();

        final LatLng latLng = new LatLng(latitude, longitude);

        Bitmap bitmap = Utils.getBitmapFromEventImageName(activity, fragCreateEventTitle.getEventImageName());

        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(Utils.getMarkerBitmapFromView(activity, bitmap, Constant.MarkerColor.BLUE))));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude)).zoom(15).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }


}
