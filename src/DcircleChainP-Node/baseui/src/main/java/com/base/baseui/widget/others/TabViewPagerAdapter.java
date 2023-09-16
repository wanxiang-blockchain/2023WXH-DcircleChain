package com.base.baseui.widget.others;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/**
 * @author yangfei
 * @time 2023/3/17
 * @desc
 */
public class TabViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> listFragment;
    private List<String> mPageTitleList;//tab的标题

    private final FragmentManager fragmentManager;

    public TabViewPagerAdapter(FragmentManager fm, List<Fragment> listFragment) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentManager = fm;
        this.listFragment = listFragment;
    }

    public TabViewPagerAdapter(FragmentManager fm, List<Fragment> listFragment, List<String> mPageTitleList) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentManager = fm;
        this.listFragment = listFragment;
        this.mPageTitleList = mPageTitleList;
    }

    public void setNewData(List<Fragment> listFragment) {
        this.listFragment = listFragment;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int pos) {
        return listFragment.get(pos);
    }

    @Override
    public int getCount() {
        return listFragment.size();
    }

    /**
     * 供 tablayout使用 其他可以不管
     *
     * @param position
     * @return
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (null == mPageTitleList || mPageTitleList.isEmpty()) {
            return "";
        }
        return mPageTitleList.get(position);
    }
}
