/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package petcare.com.mypetcare.Activity.SearchFragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import petcare.com.mypetcare.Activity.AdoptDetailActivity;
import petcare.com.mypetcare.Activity.AnnounceActivity;
import petcare.com.mypetcare.Activity.BaseActivity;
import petcare.com.mypetcare.Activity.HospitalDetailActivity;
import petcare.com.mypetcare.Activity.MatingDetailActivity;
import petcare.com.mypetcare.Activity.MatingRequestActivity;
import petcare.com.mypetcare.Activity.MissingDetailActivity;
import petcare.com.mypetcare.Activity.ReportWriteActivity;
import petcare.com.mypetcare.Adapter.AdoptGridViewAdapter;
import petcare.com.mypetcare.Adapter.AnnounceGridViewAdapter;
import petcare.com.mypetcare.Adapter.HospitalListViewAdapter;
import petcare.com.mypetcare.Adapter.MissingGridViewAdapter;
import petcare.com.mypetcare.Model.AnnounceInfoListVO;
import petcare.com.mypetcare.Model.GeoRegionVO;
import petcare.com.mypetcare.Model.GeoStateVO;
import petcare.com.mypetcare.Model.MatingListVO;
import petcare.com.mypetcare.Model.ReportListVO;
import petcare.com.mypetcare.R;
import petcare.com.mypetcare.Util.GeneralApi;
import petcare.com.mypetcare.Util.GsonUtil;

public class SlidingTabsBasicFragment extends Fragment {
    private int hospitalCurrentPosition = 1;
    private static final int SEARCH_COUNT = 20;

    static final String LOG_TAG = "SlidingTabsBasicFragment";
    private static ActionBar actionBar;
    private static TextView tvTitle;
    private static ImageView ivWrite;
    private static ImageButton ibBack;
    private static SamplePagerAdapter adapter;
    private static List<GeoStateVO.GeoStateObject> stateList;
    private static List<GeoRegionVO.GeoRegionObject> regionList;
    private static String selectedState = null;
    private static String selectedRegion = null;
    private static final String[] titleArr = {"병원", "미용" ,"호텔", "용품", "카페", "장례", "분양", "신고", "공고"};
    private static AnnounceGridViewAdapter adapterAnnounce;
    private static AdoptGridViewAdapter adapterMating;
    private static MissingGridViewAdapter adapterMissing;
    private static List<Integer> pagingCount;
    private static final int NUM_HOSPITAL = 0;
    private static final int NUM_BEAUTY = 1;
    private static final int NUM_HOTEL = 2;
    private static final int NUM_TOOL = 3;
    private static final int NUM_CAFE = 4;
    private static final int NUM_FUNERAL = 5;
    private static final int NUM_ADOPT = 6;
    private static final int NUM_REPORT = 7;
    private static final int NUM_NOTI = 8;
    private static boolean isLoading = false;
    private static long currentTime = 0L;
    private static int maxCount = 0;
    private static final int PAGE_OFFSET = 15;
    private static List<Boolean> pagingLastCheck;
    private static boolean isLoaded = false;

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sample, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.tb_slide);
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);

        actionBar = ((BaseActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        View actionBarView = inflater.inflate(R.layout.actionbar_main2, null);
        tvTitle = (TextView) actionBarView.findViewById(R.id.tv_main2_title);
        ibBack = (ImageButton) actionBarView.findViewById(R.id.ib_main2_back);
        ivWrite = (ImageView) actionBarView.findViewById(R.id.iv_main2_write);

        ActionBar.LayoutParams lp1 = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        actionBar.setCustomView(actionBarView, lp1);

//        Toolbar parent = (Toolbar) actionBarView.getParent();
        toolbar.setContentInsetsAbsolute(0, 0);

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && mViewPager.getCurrentItem() == NUM_ADOPT && adapter.getAdoptPageState() != 0) {
                    adapter.setAdoptPageState(0);
                    ivWrite.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    tvTitle.setText(titleArr[NUM_ADOPT]);
                    return true;
                }
                return false;
            }
        });

        pagingCount = new ArrayList<>();
        pagingLastCheck = new ArrayList<>();
        for (int i = 0; i < titleArr.length; i++) {
            pagingCount.add(1);
            pagingLastCheck.add(false);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                isLoaded = true;
            }
        }, 1000);

        return view;
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of {@link SamplePagerAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set its PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        adapter = new SamplePagerAdapter();
        mViewPager.setAdapter(adapter);
        Bundle bundle = this.getArguments();
        int startPage = bundle.getInt("startPage");
        mViewPager.setCurrentItem(startPage);

        if (startPage == NUM_REPORT) {
            ivWrite.setVisibility(View.VISIBLE);
            ivWrite.setOnClickListener(null);
            ivWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ReportWriteActivity.class);
                    startActivity(intent);
                }
            });
        }

        tvTitle.setText(titleArr[startPage]);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvTitle.setText(titleArr[position]);
                if (position == NUM_ADOPT && adapter.getAdoptPageState() == 2) {
                    tvTitle.setText("교배");
                    ivWrite.setVisibility(View.VISIBLE);
                    ivWrite.setOnClickListener(null);
                    ivWrite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), MatingRequestActivity.class);
                            startActivity(intent);
                        }
                    });
                } else if (position == NUM_REPORT) {
                    ivWrite.setVisibility(View.VISIBLE);
                    ivWrite.setOnClickListener(null);
                    ivWrite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ReportWriteActivity.class);
                            startActivity(intent);
                        }
                    });
                } else {
                    ivWrite.setVisibility(View.GONE);
                    ivWrite.setOnClickListener(null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // its PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);
//        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.indicatorSelectedFont));
        mSlidingTabLayout.setDividerColors(Color.parseColor("#00000000"));
        mSlidingTabLayout.setSelectedIndicatorColors(Color.parseColor("#FFFFFF"));
        mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
    class SamplePagerAdapter extends PagerAdapter {
        private int adoptPageState = 0;
        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 9;
        }

        public int getAdoptPageState() {
            return adoptPageState;
        }

        public void setAdoptPageState(int state) {
            adoptPageState = state;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return titleArr[position];
        }

        // END_INCLUDE (pageradapter_getpagetitle)

        public class HospitalApi extends GeneralApi {
            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            // Inflate a new layout from our resources
            View view = null;

            switch (position) {
//                case 0:
//                    break;
//                case 1:
//                    break;
                case NUM_ADOPT:
                    switch (adoptPageState) {
                        case 1:
                            view = getActivity().getLayoutInflater().inflate(R.layout.fragment_adopt_list, container, false);
                            container.addView(view);

                            GridView gvAdopt = (GridView) view.findViewById(R.id.gv_adopt_list);
                            AdoptGridViewAdapter adapterAdopt = new AdoptGridViewAdapter(getContext(), R.layout.gridview_adopt_list);
                            gvAdopt.setAdapter(adapterAdopt);
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/SEBjThb.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/SEBjThb.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/SEBjThb.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/SEBjThb.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            adapterAdopt.addItem("http://i.imgur.com/3jXjgTT.jpg");
                            gvAdopt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(getActivity(), AdoptDetailActivity.class);
                                    intent.putExtra("id", id);

                                    startActivity(intent);
                                }
                            });

                            break;
                        case 2:
                            view = getActivity().getLayoutInflater().inflate(R.layout.fragment_mating_list, container, false);
                            container.addView(view);
                            tvTitle.setText("교배");

                            ivWrite.setVisibility(View.VISIBLE);

                            GridView gvMating = (GridView) view.findViewById(R.id.gv_mating_list);
                            adapterMating = new AdoptGridViewAdapter(getContext(), R.layout.gridview_adopt_list);
                            gvMating.setAdapter(adapterMating);
                            callMatingListCallApi();
                            gvMating.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Intent intent = new Intent(getActivity(), MatingDetailActivity.class);
                                    intent.putExtra("id", id);

                                    startActivity(intent);
                                }
                            });

                            break;
                        default:
                            view = getActivity().getLayoutInflater().inflate(R.layout.fragment_adopt, container, false);
                            container.addView(view);
                            ImageView ivAdopt = (ImageView) view.findViewById(R.id.iv_adopt);
                            ivAdopt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    adoptPageState = 1;
                                    notifyDataSetChanged();
                                }
                            });
                            ImageView ivMating = (ImageView) view.findViewById(R.id.iv_mating);
                            ivMating.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    adoptPageState = 2;
                                    ivWrite.setOnClickListener(null);
                                    ivWrite.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(getActivity(), MatingRequestActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                                    notifyDataSetChanged();
                                }
                            });
                            break;
                    }
                    break;
//                case 3:
//                    break;
                case NUM_NOTI:
                    pagingCount.set(NUM_NOTI, 1);
                    pagingLastCheck.set(NUM_NOTI, false);
                    view = getActivity().getLayoutInflater().inflate(R.layout.fragment_announce_list, container, false);
                    container.addView(view);

                    GridView gvAnnounce = (GridView) view.findViewById(R.id.gv_announce_list);
                    adapterAnnounce = new AnnounceGridViewAdapter(getContext(), R.layout.gridview_announce_list);
                    gvAnnounce.setAdapter(adapterAnnounce);
                    gvAnnounce.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), AnnounceActivity.class);
                            intent.putExtra("id", id);

                            startActivity(intent);
                        }
                    });

                    gvAnnounce.setOnScrollListener(new AbsListView.OnScrollListener() {
                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            if (firstVisibleItem + visibleItemCount >= totalItemCount && isLoaded) {
                                Log.d("gridview", "reached at bottom");
                                callAnnouncePetCallApi();
                            }
                        }

                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {

                        }
                    });

                    Spinner spState = (Spinner) view.findViewById(R.id.sp_announce_state);
                    final Spinner spRegion = (Spinner) view.findViewById(R.id.sp_announce_region);

                    spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (CollectionUtils.isNotEmpty(stateList)) {
                                GeoStateVO.GeoStateObject stateObject = stateList.get(position);
                                selectedState = stateObject.getCode();
                                callRegionApi(spRegion);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    spRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (CollectionUtils.isNotEmpty(regionList)) {
                                GeoRegionVO.GeoRegionObject regionObject = regionList.get(position);
                                selectedRegion = regionObject.getRegionCode();
                                adapterAnnounce.removeAllItems();
                                callAnnouncePetCallApi();
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    GeoStateApi geoStateApi = new GeoStateApi(spState, spRegion);
                    Map headerGeo = new HashMap<>();
                    String urlGeo = "http://220.73.175.100:8080/MPMS/mob/mobile.service";
                    String serviceIdGeo = "MPMS_12002";
                    headerGeo.put("url", urlGeo);
                    headerGeo.put("serviceName", serviceIdGeo);

                    Map bodyGeo = new HashMap<>();

                    geoStateApi.execute(headerGeo, bodyGeo);
                    break;
                case NUM_HOSPITAL:
                    pagingCount.set(NUM_HOSPITAL, 1);
                    view = getActivity().getLayoutInflater().inflate(R.layout.fragment_hospital, container, false);
                    container.addView(view);
                    ListView lvHospitalList = (ListView) view.findViewById(R.id.lv_hospital_list);
                    HospitalListViewAdapter adapter = new HospitalListViewAdapter(view.getContext());
                    HospitalApi hospitalApi = new HospitalApi();

                    Map headers = new HashMap<>();
                    String url = "http://220.73.175.100:8080/MPMS/mob/mobile.service";
                    String serviceId = "MPMS_02001";
                    String contentType = "application/json";
                    headers.put("url", url);
                    headers.put("serviceName", serviceId);

                    Map params = new HashMap<>();
//                    params.put("USER_EMAIL", global.get("email"));
                    params.put("SEARCH_COUNT", SEARCH_COUNT);
                    params.put("SEARCH_PAGE", hospitalCurrentPosition++);

//                    hospitalApi.execute(headers, params);

                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    adapter.addItem("test", "testdesc", "testdesc", R.drawable.ic_menu_camera, Arrays.asList(new String[]{"d", "b"}));
                    lvHospitalList.setAdapter(adapter);
                    lvHospitalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), HospitalDetailActivity.class);
                            intent.putExtra("id", id);

                            startActivity(intent);
                        }
                    });
                    break;
//                case 6:
//                    break;
                case NUM_REPORT:
                    pagingCount.set(NUM_REPORT, 1);
                    view = getActivity().getLayoutInflater().inflate(R.layout.fragment_report, container, false);
                    container.addView(view);

                    TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tl_report);
                    tabLayout.addTab(tabLayout.newTab().setText("실종"));
                    tabLayout.addTab(tabLayout.newTab().setText("보호중"));

                    tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            switch (tab.getPosition()) {
                                case 0:
                                    Fragment fragment1 = MissingFragment.newInstance(0);
                                    getFragmentManager()
                                            .beginTransaction().replace(R.id.containerLayout, fragment1).commit();
                                    break;
                                default:
                                    Fragment fragment2 = MissingFragment.newInstance(1);
                                    getFragmentManager()
                                            .beginTransaction().replace(R.id.containerLayout, fragment2).commit();
                                    break;
                            }
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });

                    Fragment fragment1 = MissingFragment.newInstance(0);
                    getFragmentManager().beginTransaction().replace(R.id.containerLayout, fragment1).commit();

                    break;
                default:
                    view = getActivity().getLayoutInflater().inflate(R.layout.pager_item, container, false);
                    // Add the newly created View to the ViewPager
                    container.addView(view);

                    // Retrieve a TextView from the inflated View, and update its text
                    TextView title = (TextView) view.findViewById(R.id.item_title);
                    title.setText(String.valueOf(position));
                    break;
            }

//            if (position == 7) {
//                v = getActivity().getLayoutInflater().inflate(R.layout.fragment_hospital, container, false);
//                container.addView(v);
//            } else {
//                v = getActivity().getLayoutInflater().inflate(R.layout.pager_item,
//                        container, false);
//                // Add the newly created View to the ViewPager
//                container.addView(v);
//
//                // Retrieve a TextView from the inflated View, and update its text
//                TextView title = (TextView) v.findViewById(R.id.item_title);
//                title.setText(String.valueOf(position + 1));
//            }

            // Return the View
            return view;
        }

        /**
         * Destroy the item from the {@link ViewPager}. In our case this is simply removing the
         * {@link View}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
//            Log.i(LOG_TAG, "destroyItem() [position: " + position + "]");
        }
    }

    public static class MissingFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public MissingFragment() {
        }

        public static MissingFragment newInstance(int sectionNumber) {
            MissingFragment fragment = new MissingFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            int num = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;

            switch (num) {
                case 0:
                    rootView = inflater.inflate(R.layout.fragment_missing_list, container, false);
                    GridView gvMissing = (GridView) rootView.findViewById(R.id.gv_missing_list);
                    adapterMissing = new MissingGridViewAdapter(getContext(), R.layout.gridview_missing_list);
                    gvMissing.setAdapter(adapterMissing);

                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", true, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", true, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");
                    adapterMissing.addItem("http://i.imgur.com/3jXjgTT.jpg", false, "0.5km");

                    MissingListApi missingListApi = new MissingListApi();
                    Map header = new HashMap<>();
                    String url = "http://220.73.175.100:8080/MPMS/mob/mobile.service";
                    String serviceId = "MPMS_11001";
                    header.put("url", url);
                    header.put("serviceName", serviceId);

                    Map body = new HashMap<>();
                    body.put("SEARCH_COUNT", PAGE_OFFSET);
                    int currentPage = pagingCount.get(NUM_REPORT);
                    body.put("SEARCH_PAGE", currentPage);
                    pagingCount.set(NUM_REPORT, currentPage + 1);

//                    missingListApi.execute(header, body);

                    gvMissing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getActivity(), MissingDetailActivity.class);
                            intent.putExtra("id", id);

                            startActivity(intent);
                        }
                    });
                    break;
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_missing_list, container, false);
                    break;
            }
            return rootView;
        }
    }

    private static class MissingListApi extends GeneralApi {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ReportListVO reportListVO = GsonUtil.fromJson(result, ReportListVO.class);
            if (reportListVO.getResultCode() != 0) {
                return;
            }

            List<ReportListVO.ReportPetObject> dataList = reportListVO.getData();
            for (ReportListVO.ReportPetObject data : dataList) {
                adapterMissing.addItem(data.getThumbImgUrl(), data.isFound(), data.getLocation());
            }

            adapterMissing.notifyDataSetChanged();
        }
    }

    private class MatingListApi extends GeneralApi {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            MatingListVO matingListVO = GsonUtil.fromJson(result, MatingListVO.class);
            if (matingListVO.getResultCode() != 0) {
                return;
            }

            List<MatingListVO.MatingObject> dataList = matingListVO.getData();
            for (MatingListVO.MatingObject data : dataList) {
                adapterMating.addItem(data.getThumbImgUrl());
            }

            adapterMating.notifyDataSetChanged();
        }
    }

    private void callMatingListCallApi() {
        MatingListApi matingListApi = new MatingListApi();
        Map header = new HashMap<>();
        String url = "http://220.73.175.100:8080/MPMS/mob/mobile.service";
        String serviceId = "MPMS_10001";
        header.put("url", url);
        header.put("serviceName", serviceId);

        Map body = new HashMap<>();
        body.put("SEARCH_COUNT", PAGE_OFFSET);
        int currentPage = pagingCount.get(NUM_ADOPT);
        body.put("SEARCH_PAGE", currentPage);
        pagingCount.set(NUM_ADOPT, currentPage + 1);

        matingListApi.execute(header, body);
    }

    private class GeoStateApi extends GeneralApi {
        Spinner spinnerState;
        Spinner spinnerRegion;

        public GeoStateApi(Spinner spinnerState, Spinner spinnerRegion) {
            this.spinnerState = spinnerState;
            this.spinnerRegion = spinnerRegion;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            GeoStateVO geoStateVO = GsonUtil.fromJson(result, GeoStateVO.class);
            if (geoStateVO.getResultCode() != 0) {
                return;
            }

            stateList = geoStateVO.getData();
            List<String> arr = new ArrayList<>();

            for (GeoStateVO.GeoStateObject geo : stateList) {
                 arr.add(geo.getName());
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_spinner_item, arr)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    return setCentered(super.getView(position, convertView, parent));
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    return setCentered(super.getDropDownView(position, convertView, parent));
                }

                private View setCentered(View view)
                {
                    TextView textView = (TextView)view.findViewById(android.R.id.text1);
                    textView.setGravity(Gravity.CENTER);
                    return view;
                }
            };
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerState.setAdapter(spinnerArrayAdapter);

            if (stateList.size() > 0) {
                pagingLastCheck.set(NUM_NOTI, false);
                selectedState = stateList.get(0).getCode();
                callRegionApi(spinnerRegion);
            }
        }
    }

    private void callRegionApi(Spinner spinnerRegion) {
        try {
            GeoRegionApi geoRegionApi = new GeoRegionApi(spinnerRegion);
            Map headerGeo = new HashMap<>();
            String urlGeo = "http://220.73.175.100:8080/MPMS/mob/mobile.service";
            String serviceIdGeo = "MPMS_12003";
            headerGeo.put("url", urlGeo);
            headerGeo.put("serviceName", serviceIdGeo);

            Map bodyGeo = new HashMap<>();
            bodyGeo.put("UPR_CD", selectedState);

            geoRegionApi.execute(headerGeo, bodyGeo);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "정보를 조회하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private class GeoRegionApi extends GeneralApi {
        Spinner spinner;

        public GeoRegionApi(Spinner spinner) {
            this.spinner = spinner;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            GeoRegionVO geoRegionVO = GsonUtil.fromJson(result, GeoRegionVO.class);
            if (geoRegionVO.getResultCode() != 0) {
                Toast.makeText(getActivity(), "데이터를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                spinner.setAdapter(null);
                return;
            }

            regionList = geoRegionVO.getData();
            List<String> arr = new ArrayList<>();

            for (GeoRegionVO.GeoRegionObject geo : regionList) {
                arr.add(geo.getRegionName());
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String> (getActivity(), android.R.layout.simple_spinner_item, arr)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    return setCentered(super.getView(position, convertView, parent));
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent)
                {
                    return setCentered(super.getDropDownView(position, convertView, parent));
                }

                private View setCentered(View view)
                {
                    TextView textView = (TextView)view.findViewById(android.R.id.text1);
                    textView.setGravity(Gravity.CENTER);
                    return view;
                }
            };
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(null);
            spinner.setAdapter(spinnerArrayAdapter);

            if (arr.size() > 0) {
                pagingLastCheck.set(NUM_NOTI, false);
                selectedRegion = regionList.get(0).getRegionCode();
            }
        }
    }

    private void callAnnouncePetCallApi() {
        if (pagingLastCheck.get(NUM_NOTI)) {
            return ;
        }

        AnnouncePetCallApi announcePetCallApi = new AnnouncePetCallApi();
        Map headerGeo = new HashMap<>();
        String urlGeo = "http://220.73.175.100:8080/MPMS/mob/mobile.service";
        String serviceIdGeo = "MPMS_12001";
        headerGeo.put("url", urlGeo);
        headerGeo.put("serviceName", serviceIdGeo);

        Map bodyGeo = new HashMap<>();
        bodyGeo.put("UPR_CD", selectedState);
        bodyGeo.put("ORG_CD", selectedRegion);

        bodyGeo.put("SEARCH_COUNT", PAGE_OFFSET);
        int currentPage = pagingCount.get(NUM_NOTI);
        bodyGeo.put("SEARCH_PAGE", currentPage);
        pagingCount.set(NUM_NOTI, currentPage + 1);
        bodyGeo.put("BGN_DE", "20170101");
        bodyGeo.put("END_DE", "20170331");

        announcePetCallApi.execute(headerGeo, bodyGeo);
    }

    private class AnnouncePetCallApi extends GeneralApi {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (pagingLastCheck.get(NUM_NOTI)) {
                return ;
            }

            AnnounceInfoListVO announceInfoListVO = GsonUtil.fromJson(result, AnnounceInfoListVO.class);
            if (announceInfoListVO.getResultCode() != 0) {
                return;
            }

            List<AnnounceInfoListVO.AnnounceInfoObject> dataList = announceInfoListVO.getData();

            if (dataList.size() == 0 ) {
                pagingLastCheck.set(NUM_NOTI, true);
                return ;
            }

            for (AnnounceInfoListVO.AnnounceInfoObject data : dataList) {
                adapterAnnounce.addItem(data.getThumbUrl());
            }

            adapterAnnounce.notifyDataSetChanged();

            if (dataList.size() < PAGE_OFFSET) {
                pagingLastCheck.set(NUM_NOTI, true);
            }
        }
    }
}
