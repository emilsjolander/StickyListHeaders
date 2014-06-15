package se.emilsjolander.stickylistheaders;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;
import java.util.WeakHashMap;

/**
 * add expand/collapse functions like ExpandableListView
 * provide customizable expand/collapse animation
 * @author lsjwzh
 */
public class ExpandableStickyListHeadersListView extends StickyListHeadersListView {
    public interface IAnimationExecutor{
        public void executeAnim(View target,int animType,int viewHeight);
    }

    public final static int ANIMATION_COLLAPSE = 1;
    public final static int ANIMATION_EXPAND = 0;

    WeakHashMap<View,Integer> mOriginalViewHeight = new WeakHashMap<View, Integer>();
    ExpandableStickyListHeadersAdapter mExpandableStickyListHeadersAdapter;



    IAnimationExecutor mAnimExecutor = new IAnimationExecutor() {
        @Override
        public void executeAnim(View target, int animType, int viewHeight) {
            if(animType==ANIMATION_EXPAND){
                target.setVisibility(VISIBLE);
            }else if(animType==ANIMATION_COLLAPSE){
                target.setVisibility(GONE);
            }
        }
    };


    public ExpandableStickyListHeadersListView(Context context) {
        super(context);
    }

    public ExpandableStickyListHeadersListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableStickyListHeadersListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(StickyListHeadersAdapter adapter) {
        mExpandableStickyListHeadersAdapter = new ExpandableStickyListHeadersAdapter(adapter);
        super.setAdapter(mExpandableStickyListHeadersAdapter);
    }

    public View findViewByItemId(long itemId){
        return mExpandableStickyListHeadersAdapter.findViewByItemId(itemId);
    }

    public long findItemIdByView(View view){
        return mExpandableStickyListHeadersAdapter.findItemIdByView(view);
    }

    public void expand(long groupId) {
        if(!mExpandableStickyListHeadersAdapter.isGroupCollapsed(groupId)){
            return;
        }
        mExpandableStickyListHeadersAdapter.expand(groupId);
        //find and expand views in group
        List<View> itemViews = mExpandableStickyListHeadersAdapter.getItemViewsByGroup(groupId);
        if(itemViews==null){
            return;
        }
        for (View view : itemViews) {
            animateView(view, ANIMATION_EXPAND);
        }
    }

    public void collapse(long groupId) {
        if(mExpandableStickyListHeadersAdapter.isGroupCollapsed(groupId)){
            return;
        }
        mExpandableStickyListHeadersAdapter.collapse(groupId);
        //find and hide views in group
        List<View> itemViews = mExpandableStickyListHeadersAdapter.getItemViewsByGroup(groupId);
        if(itemViews==null){
            return;
        }
        for (View view : itemViews) {
            animateView(view, ANIMATION_COLLAPSE);
        }
    }

    public boolean isGroupCollapsed(long groupId){
        return  mExpandableStickyListHeadersAdapter.isGroupCollapsed(groupId);
    }

    public void setAnimExecutor(IAnimationExecutor animExecutor) {
        this.mAnimExecutor = animExecutor;
    }

    /**
     * Performs either COLLAPSE or EXPAND animation on the target view
     *
     * @param target the view to animate
     * @param type   the animation type, either ExpandCollapseAnimation.COLLAPSE
     *               or ExpandCollapseAnimation.EXPAND
     */
    private void animateView(final View target, final int type) {
        if(ANIMATION_EXPAND==type&&target.getVisibility()==VISIBLE){
            return;
        }
        if(ANIMATION_COLLAPSE==type&&target.getVisibility()!=VISIBLE){
            return;
        }
        if(mOriginalViewHeight.get(target)==null){
            mOriginalViewHeight.put(target,target.getLayoutParams().height);
        }
        final int viewHeight = mOriginalViewHeight.get(target);
        if(mAnimExecutor!=null){
            mAnimExecutor.executeAnim(target,type,viewHeight);
        }

    }

}
