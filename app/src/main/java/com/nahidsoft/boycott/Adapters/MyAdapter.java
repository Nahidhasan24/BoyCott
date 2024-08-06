package com.nahidsoft.boycott.Adapters;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.nahidsoft.boycott.Fragments.BoyCottFragment;
import com.nahidsoft.boycott.Fragments.ListFragment;
import com.nahidsoft.boycott.Fragments.MyListFragment;

public class MyAdapter extends FragmentPagerAdapter {

    private Context myContext;
    int totalTabs;

    public MyAdapter(Context context, FragmentManager fm, int totalTabs) {
        super(fm);
        myContext = context;
        this.totalTabs = totalTabs;
    }

    // this is for fragment tabs
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:

                return new ListFragment();
            case 1:
                return new BoyCottFragment();
            case 2:

                return new MyListFragment();
            default:
                return null;
        }
    }
    // this counts total number of tabs
    @Override
    public int getCount() {
        return totalTabs;
    }
}
