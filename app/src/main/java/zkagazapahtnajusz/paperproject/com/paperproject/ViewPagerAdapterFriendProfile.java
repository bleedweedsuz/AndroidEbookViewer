package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class ViewPagerAdapterFriendProfile extends FragmentPagerAdapter{

    private final List<Fragment> fragmentList = new ArrayList<>();

    private String tabTitles[] = new String[] { "POST","BOOKS", "INFO"};

    public ViewPagerAdapterFriendProfile(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    public void addFragmentUserProfile(Fragment fragment){
        fragmentList.add(fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
