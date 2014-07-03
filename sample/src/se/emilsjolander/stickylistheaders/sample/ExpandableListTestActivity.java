package se.emilsjolander.stickylistheaders.sample;

/**
 * Created by panwenye on 14-6-14.
 */
public class ExpandableListTestActivity {
    //        final ViewGroup.LayoutParams lp = target.getLayoutParams();
//
//        float animStartY = type == ANIMATION_EXPAND ? 0f : viewHeight;
//        float animEndY = type == ANIMATION_EXPAND ? viewHeight : 0f;
//        ValueAnimator animator = ValueAnimator.ofFloat(animStartY, animEndY);
//        animator.setDuration(200);
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//                if (type == ExpandCollapseAnimation.EXPAND) {
//                    target.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                if (type == ExpandCollapseAnimation.EXPAND) {
//                    target.setVisibility(View.VISIBLE);
//                } else {
//                    target.setVisibility(View.GONE);
//                }
//                target.getLayoutParams().height = viewHeight;
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                lp.height = ((Float) valueAnimator.getAnimatedValue()).intValue();
//                target.setLayoutParams(lp);
//                target.requestLayout();
//            }
//        });
//        animator.start();
}
