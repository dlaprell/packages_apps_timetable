package eu.laprell.timetable.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

/**
 * Created by david on 12.11.14.
 */
public class AnimUtils {

    public static int aScale() {
        return A_SCALE;
    }

    private static int A_SCALE;
    public static void setGlobalAnimatorScale(int scale) {
        A_SCALE = Math.max(1, scale);
    }

    public static com.nineoldandroids.animation.Animator withLayer(final View target,
            com.nineoldandroids.animation.Animator a) {
        AnimatorListenerAdapter an = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
                target.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }

            @Override
            public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                target.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        };
        a.addListener(an);

        return a;
    }

    public static void animateProgressExit(final View v) {
        ViewPropertyAnimator a = v.animate().alpha(0f).translationY(-300).setDuration(1000);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            a.withEndAction(new Runnable() {
                @Override
                public void run() {
                    v.setVisibility(View.GONE);
                }
            });
        } else {
            a.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    v.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        a.start();
    }

    public static void animateProgressEnter(final View v) {
        v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);
        v.setTranslationY(-300);
        ViewPropertyAnimator a = v.animate().alpha(1f).translationY(0).setDuration(1000);
        a.start();
    }

    public static void animateViewAddingInLayout(final View v, final ViewGroup g, int position) {
        v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int height = v.getMeasuredHeight();

        v.setAlpha(0);
        g.addView(v, position);

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.setDuration(150);

        for(int i = 0;i < g.getChildCount();i++) {
            ObjectAnimator a = ObjectAnimator.ofFloat(g.getChildAt(i), "translationY", -height, 0);
            g.getChildAt(i).setLayerType(View.LAYER_TYPE_HARDWARE, null);
            set.playTogether(a);
        }

        set.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                for(int i = 0;i < g.getChildCount();i++) {
                    g.getChildAt(i).setTranslationY(0);
                    g.getChildAt(i).setLayerType(View.LAYER_TYPE_NONE, null);
                }

                ViewPropertyAnimator a = v.animate().alpha(1).setDuration(200);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    a.withLayer();
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        set.start();
    }

    public static void animateViewDeletingInLayout(View v, ViewGroup g) {
        animateViewDeletingInLayout(v, g, null);
    }

    public static void animateViewDeletingInLayout(final View v, final ViewGroup g, final Runnable r) {
        v.setPivotX(v.getWidth() / 2);
        v.setPivotY(0);

        int i = 0;
        boolean skip = false;
        for(;!skip && i < g.getChildCount();i++) {
            if(g.getChildAt(i) == v)
                skip = true;
        }

        final int height = v.getHeight();

        AnimatorSet set = new AnimatorSet();
        set.setDuration(150);
        set.setInterpolator(new AccelerateDecelerateInterpolator());

        for(;i < g.getChildCount();i++) {
            ObjectAnimator a = ObjectAnimator.ofFloat(g.getChildAt(i),
                    "translationY", 0f, -height);
            set.playTogether(a);
            g.getChildAt(i).setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        ObjectAnimator a = ObjectAnimator.ofFloat(v, "rotationX", 0f, -90f);

        set.playTogether(a);

        set.start();
        set.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                g.removeView(v);

                for(int i = 0;i < g.getChildCount();i++) {
                    g.getChildAt(i).setTranslationY(0);
                    g.getChildAt(i).setLayerType(View.LAYER_TYPE_NONE, null);
                }

                if(r != null) r.run();
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    public static void animateViewDeletingInLayoutAlpha(final View v, final ViewGroup g) {
        int i = 0;
        boolean skip = false;
        for(;!skip && i < g.getChildCount();i++) {
            if(g.getChildAt(i) == v)
                skip = true;
        }

        final int height = v.getHeight();

        ViewPropertyAnimator a = v.animate().alpha(0).setDuration(200);

        final int ii = i;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            a.withEndAction(new Runnable() {
                @Override
                public void run() {
                    moveAllUp(ii, g, height, v);
                }
            });
        } else {
            a.setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    moveAllUp(ii, g, height, v);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
    }

    private static void moveAllUp(int i, final ViewGroup g, final int h, final View v) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(150);
        set.setInterpolator(new DecelerateInterpolator());

        for(;i < g.getChildCount();i++) {
            ObjectAnimator a = ObjectAnimator.ofFloat(g.getChildAt(i),
                    "translationY", 0f, -h);
            set.playTogether(a);
            g.getChildAt(i).setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        set.start();
        set.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                g.removeView(v);

                for(int i = 0;i < g.getChildCount();i++) {
                    g.getChildAt(i).setTranslationY(0);
                    g.getChildAt(i).setLayerType(View.LAYER_TYPE_NONE, null);
                }
            }
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    public static Animator withEndAction(Animator a, final Runnable r) {
        a.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                r.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return a;
    }

    public static void afterPreDraw(@NonNull final View target, @NonNull final Runnable r) {
        target.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                target.getViewTreeObserver().removeOnPreDrawListener(this);
                r.run();
                return true;
            }
        });
    }

    public static class LayerAdapter extends AnimatorListenerAdapter {
        private View mTargetView;

        public LayerAdapter(View v) {
            mTargetView = v;
        }

        @Override
        public void onAnimationStart(com.nineoldandroids.animation.Animator animation) {
            mTargetView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        @Override
        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
            mTargetView.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    public static int[] getViewLoc(View v) {
        return getViewLoc(v, new int[2]);
    }

    public static int[] getViewLoc(View v, int[]loc) {
        float pivX = v.getPivotX();
        float pivY = v.getPivotY();

        v.setPivotX(0);
        v.setPivotY(0);

        v.getLocationOnScreen(loc);

        v.setPivotX(pivX);
        v.setPivotY(pivY);

        return loc;
    }

    public static void animateMaterialDialogIn(final View anchor, final MaterialDialog dialog) {
        final View decorView = dialog.getWindow().getDecorView();
        final View content = (View)dialog.getTitleFrame().getParent().getParent();

        final int[] anchorLoc = getViewLoc(anchor);
        anchorLoc[0] += anchor.getWidth() / 2;
        anchorLoc[1] += anchor.getHeight() / 2;

        AnimUtils.afterPreDraw(decorView, new Runnable() {
            @Override
            public void run() {
                int[] decorLoc = getViewLoc(decorView);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                    int centerX = Math.min(anchorLoc[0] - decorLoc[0], decorView.getWidth());
                    centerX = Math.max(centerX, 0);

                    int centerY = Math.min(anchorLoc[1] - decorLoc[1], decorView.getHeight());
                    centerY = Math.max(centerY, 0);

                    float startRadius = Math.min(anchor.getWidth(), anchor.getHeight());
                    float endRadius = Math.max(decorView.getWidth(), decorView.getHeight()) * 1.2f;

                    Animator a = ViewAnimationUtils.createCircularReveal(
                            decorView,
                            centerX,
                            centerY,
                            startRadius,
                            endRadius
                    );

                    final int revDur = 320;
                    final int fadeDur = 150;

                    a.setDuration(revDur);
                    a.start();

                    content.setAlpha(0f);
                    com.nineoldandroids.view.ViewPropertyAnimator.animate(content)
                            .alpha(1f)
                            .setStartDelay(revDur - fadeDur)
                            .setDuration(fadeDur)
                            .setListener(new LayerAdapter(content));
                } else {
                    decorView.setScaleX(0);
                    decorView.setScaleY(0);

                    com.nineoldandroids.view.ViewPropertyAnimator.animate(decorView)
                            .scaleY(1f).scaleX(1f).setDuration(5000).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                            content.setVisibility(View.VISIBLE);
                            content.setAlpha(0f);
                            com.nineoldandroids.view.ViewPropertyAnimator.animate(content).alpha(1f)
                                    .setDuration(2000).setListener(new LayerAdapter(content));
                        }
                    });
                }
            }
        });
    }

    public static void animateListPopupWindowIn(ListPopupWindow popUp) {
        final ListView list = popUp.getListView();
        final View parent = (View) list.getParent();
        final View anchor = popUp.getAnchorView();

        parent.setPivotX(0);
        parent.setPivotY(0);

        AnimUtils.afterPreDraw(list, new Runnable() {
            @Override
            public void run() {
                list.setVisibility(View.INVISIBLE);

                float iniScaleX = anchor.getWidth() / (float)parent.getWidth();
                float iniScaleY = anchor.getHeight() / (float)parent.getHeight();

                parent.setScaleX(iniScaleX);
                parent.setScaleY(iniScaleY);

                int[] aLoc = getViewLoc(anchor);
                int[] popLoc = getViewLoc(parent);

                float pivX = aLoc[0] - popLoc[0] + anchor.getWidth()/* / 2*/;
                float pivY = aLoc[1] - popLoc[1] /*+ mData.more.getHeight() / 2*/;

                Log.d("Timetable", "aLoc[" + aLoc[0] + "," + aLoc[1] + "]" +
                        "popLoc[" + popLoc[0] + "," + popLoc[1] + "]" +
                        " pivX=" + pivX + " pivY=" + pivY);

                parent.setPivotX(pivX);
                parent.setPivotY(pivY);

                com.nineoldandroids.view.ViewPropertyAnimator.animate(parent)
                        .scaleY(1f).scaleX(1f).setDuration(170).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(com.nineoldandroids.animation.Animator animation) {
                        list.setVisibility(View.VISIBLE);
                        list.setAlpha(0f);
                        com.nineoldandroids.view.ViewPropertyAnimator.animate(list).alpha(1f)
                                .setDuration(150).setListener(new LayerAdapter(list));
                    }
                });
            }
        });
    }
}
