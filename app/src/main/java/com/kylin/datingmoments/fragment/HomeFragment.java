package com.kylin.datingmoments.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kylin.datingmoments.R;
import com.kylin.datingmoments.activity.VideoPlayerActivity;
import com.kylin.datingmoments.adapter.EndlessRecyclerOnScrollListener;
import com.kylin.datingmoments.adapter.VideoAdapter;
import com.kylin.datingmoments.dao.DAO;
import com.kylin.datingmoments.dao.DAOFactory;
import com.kylin.datingmoments.entity.VideoInfo;
import com.kylin.datingmoments.util.Constant;
import com.kylin.datingmoments.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kylin on 16-5-22.
 */
public class HomeFragment extends LazyFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int ITEM_NUM_PER_PAGE =  20;

    private boolean mIsRefreshing;

    private int mPageNum;

    private List<VideoInfo> mListVideo;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private VideoAdapter mAdapterRecycler;

    private DAO mDAO = DAOFactory.getDAO();

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_home);
        initData();
        initView();
    }

    private void initData() {
        mIsRefreshing = false;
        mPageNum = 0;
        mListVideo = new ArrayList<VideoInfo>();
        mAdapterRecycler = new VideoAdapter(getApplicationContext(), mListVideo, new VideoAdapter.OnCoverClickListener() {
            @Override
            public void onClick(int position) {
                VideoInfo info = mListVideo.get(position);
                Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                intent.putExtra(Constant.RECORD_VIDEO_PATH, info.getVideoPath());
                intent.putExtra(Constant.RECORD_VIDEO_CAPTURE, info.getCoverPath());
                intent.putExtra(Constant.RECORD_VIDEO_ID, info.getObjectId());
                startActivity(intent);
            }
        });
        onRefresh();
    }

    private void initView() {
        TextView title = (TextView) findViewById(R.id.com_actionbar_tv_title);
        title.setText(R.string.tab_home);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.fra_home_srl);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setRefreshing(false);
        //设置样式
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        mRecyclerView = (RecyclerView) findViewById(R.id.fra_home_rv);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL);
        mRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelOffset(R.dimen.video_cover_padding)));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                loadMoreDate();
            }
        });
        mRecyclerView.setAdapter(mAdapterRecycler);
    }

    private void loadMoreDate() {
        Logger.i("加载更多！");
        mPageNum ++;
        mDAO.getVideoList(mPageNum,ITEM_NUM_PER_PAGE,new DAO.GetVideoCallback() {
            @Override
            public void onSuccess(List<VideoInfo> result) {
                mListVideo.addAll(result);
                mAdapterRecycler.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                Logger.e("加载数据完成！");
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void onRefresh() {
        mPageNum = 0;
        Logger.e("开始加载数据！");
        mDAO.getVideoList(mPageNum,ITEM_NUM_PER_PAGE,new DAO.GetVideoCallback() {
            @Override
            public void onSuccess(List<VideoInfo> result) {
                mListVideo.clear();
                mListVideo.addAll(result);
                mAdapterRecycler.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
                Logger.e("加载数据完成！");
            }

            @Override
            public void onError() {

            }
        });
    }

    public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.top = space * 2;
            outRect.right = space;
        }
    }



}
