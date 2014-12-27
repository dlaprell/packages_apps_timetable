package eu.laprell.timetable.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.ListPopupWindow;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.adnansm.timelytextview.TimelyView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;

import eu.laprell.timetable.BackgroundService;
import eu.laprell.timetable.R;
import eu.laprell.timetable.animation.WaveAnimator;
import eu.laprell.timetable.database.DbAccess;
import eu.laprell.timetable.database.TimeUnit;
import eu.laprell.timetable.database.TimetableDatabase;
import eu.laprell.timetable.utils.AnimUtils;
import eu.laprell.timetable.utils.MetricsUtils;
import eu.laprell.timetable.widgets.FloatingActionButton;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by david on 07.11.14.
 */
public class TimeGridFragment extends Fragment {

    private FloatingActionButton mFab;
    private LinearLayout mTimeContainer;
    private Thread mLoadingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler();
            Looper.loop();
        }
    });
    private Handler mHandler;

    private ArrayList<Data> mList;
    private CircularProgressBar mProgress;
    private SavingAsyncTask mTask;

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.add) {
                addNewTime();
            }
        }
    };

    public TimeGridFragment() {
        mList = new ArrayList<Data>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLoadingThread.start();

        mTask = new SavingAsyncTask();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup v = (ViewGroup)inflater.inflate(
                R.layout.fragment_timegrid, container, false);

        mTimeContainer = (LinearLayout)v.findViewById(R.id.time_container);

        mProgress = (CircularProgressBar)v.findViewById(R.id.circular_loading);

        v.findViewById(R.id.add).setOnClickListener(mClickListener);

        /*mFab = (FloatingActionButton)inflater.inflate(R.layout.fab_add,
                v, false);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTime();
            }
        });

        v.addView(mFab);*/

        loadTable();

        return v;
    }

    private void addNewTime() {
        Data d = new Data();
        Data pre = mList.get(mList.size() - 1);

        TimeUnit t = new TimeUnit(-1);

        if(pre != null) {
            int time = pre.time.getStartTime() < pre.time.getEndTime()
                    ? pre.time.getEndTime() : pre.time.getStartTime();

            t.setStartTime(time);
            t.setEndTime(time + 1);
            d.num = pre.num + 1;
        } else {
            d.num = 0;
        }

        d.time = t;

        mList.add(d);

        displayNewItem(mList.size() - 1);

        mTask.doExecute(d);
    }

    private void loadTable() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DbAccess access = new DbAccess(mProgress.getContext());
                TimetableDatabase db = access.get();

                long[] tids = db.getDatabaseEntries(TimetableDatabase.TYPE_TIMEUNIT);

                int num = 1;
                for(int i = 0;i < tids.length;i++) {
                    long tid = tids[i];

                    Data d = new Data();
                    d.time = (TimeUnit) db.getDatabaseEntryById(TimetableDatabase.TYPE_TIMEUNIT, tid);

                    if(!d.time.isBreak()) {
                        d.num = num;
                        num++;
                    }

                    mList.add(d);

                    //Log.d("Timetable", d.time.makeTimeString("s - e") + " -> " + i);

                    access.close();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                AnimUtils.animateProgressExit(mProgress);

                if(!isAdded()) {
                    return;
                }

                for (int i = 0;i < mList.size();i++)
                    displayNewItem(i);
            }
        }.execute();
    }

    private void displayNewItem(int pos) {
        Data d = mList.get(pos);

        d.view = LayoutInflater.from(mTimeContainer.getContext()).inflate(
                R.layout.list_item_time_grid, mTimeContainer, false);
        d.lis = new ButtonClickListener(d);

        updateView(d);

        AnimUtils.animateViewAddingInLayout(d.view, mTimeContainer, pos);
    }

    private void updateView(Data d) {
        ObjectAnimator a = animateNumView(d);
        if(a != null)a.start();

        d.startButton.setText(d.time.makeTimeString("s"));
        d.endButton.setText(d.time.makeTimeString("e"));
    }

    private ObjectAnimator animateNumView(Data d) {
        ObjectAnimator a;

        if(!d.time.isBreak()) {
            a = d.numView.animate(getIntTag(d.numView), d.num).setDuration(200);
            d.numView.setTag(d.num);
        } else {
            a = d.numView.animate(getIntTag(d.numView), -1).setDuration(200);
            d.numView.setTag(-1);
        }

        return a;
    }

    /**
     * Gets the tag of a {@link android.view.View} through the getTag() method and
     * converts it to an int
     * @param v the view, who's tag should be converted
     * @return the int value of the tag
     */
    private int getIntTag(View v) {
        return v.getTag() == null ? -1 : ((Integer)v.getTag());
    }

    public class Data {
        TimeUnit time;
        int num;
        ButtonClickListener lis;
        View view;
        TimelyView numView;
        Button startButton, endButton;
    }

    private class ButtonClickListener implements View.OnClickListener {

        private Data mData;

        public ButtonClickListener(Data d) {
            mData = d;

            d.startButton = (Button)d.view.findViewById(R.id.btn_start_time);
            d.endButton = (Button)d.view.findViewById(R.id.btn_end_time);
            d.numView = (TimelyView)d.view.findViewById(R.id.btn_num);

            d.startButton.setOnClickListener(this);
            d.endButton.setOnClickListener(this);
            d.numView.setOnClickListener(this);

            d.numView.setTextColor(d.numView.getResources().getColor(R.color.accent_));
            d.numView.setTextStroke((int)MetricsUtils.convertDpToPixel(2));
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btn_start_time) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                                mData.time.setStartTime((i * 60) + i2);

                                mTask.doExecute(mData);

                                updateView(mData);
                            }
                        }, mData.time.getStartTime() / 60 , mData.time.getStartTime() % 60, false, false);

                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
            } else if(v.getId() == R.id.btn_end_time) {
                final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2) {
                                mData.time.setEndTime((i * 60) + i2);

                                mTask.doExecute(mData);

                                updateView(mData);
                            }
                        }, mData.time.getEndTime() / 60 , mData.time.getEndTime() % 60, false, false);

                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
            } else if(v.getId() == R.id.btn_num) {
                final ListPopupWindow popUp = new ListPopupWindow(getActivity());
                popUp.setAnchorView(v);
                popUp.setWidth((int) MetricsUtils.convertDpToPixel(120));
                popUp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            if (mData.time.isBreak()) {
                                updateNumForAllAfter(mList.indexOf(mData), true);
                            }
                        } else {
                            if (!mData.time.isBreak()) {
                                updateNumForAllAfter(mList.indexOf(mData), false);
                            }
                        }
                        popUp.dismiss();
                    }
                });

                popUp.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.adapter_list_popup_item,
                        android.R.id.text1, getResources().getStringArray(R.array.array_list_popup_time_type)));

                popUp.show();
            }
        }
    }

    private void updateNumForAllAfter(int pos, boolean newLesson) {
        int curNum = 0;
        int i = pos - 1;

        if(pos > 0) {
            do {
                if(!mList.get(i).time.isBreak())
                    curNum = mList.get(i).num;
                i--;
            } while (i >= 0 && curNum == 0);
        }

        Data d = mList.get(pos);
        d.num = newLesson ? ++curNum : 0;
        d.time.setBreak(!newLesson);

        mTask.doExecute(d);

        updateView(d);

        WaveAnimator waveAnimator = new WaveAnimator(new WaveAnimator.WaveAnimationApplier<Data>() {
            @Override
            public Animator makeAnimationForView(View v, Data d) {
                return animateNumView(d);
            }
        }).setSpeed(MetricsUtils.convertDpToPixel(300)).setStartAnchorView(d.numView, true);

        for(i = pos + 1;i < mList.size();i++) {
            d = mList.get(i);

            d.num = d.time.isBreak() ? 0 : ++curNum;

            waveAnimator.addTarget(d.numView, d);
        }

        waveAnimator.start();
    }

    public FragmentManager getSupportFragmentManager() {
        return getActivity().getSupportFragmentManager();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mLoadingThread.interrupt();
    }

    public class SavingAsyncTask extends AsyncTask<Data, Void, Void> {
        @Override
        protected Void doInBackground(Data... params) {
            DbAccess access = new DbAccess(getActivity());
            TimetableDatabase db = access.get();

            Data d = params[0];

            if(d.time.getId() != -1) {
                db.updateDatabaseEntry(d.time);
            } else {
                d.time = (TimeUnit) db.insertDatabaseEntryForId(d.time);
            }

            BackgroundService.get().getLessonNotifier().checkForNewNotifications();

            access.close();

            return null;
        }

        public void doExecute(Data d) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                executeOnExecutor(THREAD_POOL_EXECUTOR, d);
            } else {
                execute(d);
            }

            mTask = new SavingAsyncTask();
        }
    }
}
