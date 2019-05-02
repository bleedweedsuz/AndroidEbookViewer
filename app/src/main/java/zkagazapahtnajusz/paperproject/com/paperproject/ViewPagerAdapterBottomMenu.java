package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class ViewPagerAdapterBottomMenu  extends FragmentPagerAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();

    public ViewPagerAdapterBottomMenu(FragmentManager fm) {
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

    public void addFragment(Fragment fragment){

        fragmentList.add(fragment);

    }
}
