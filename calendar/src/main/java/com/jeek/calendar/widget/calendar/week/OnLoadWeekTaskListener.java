package com.jeek.calendar.widget.calendar.week;

import java.util.List;

/**
 * Created by wangshouheng on 2017/10/11.*/
public interface OnLoadWeekTaskListener {

    /**
     * 加载星期数据的监听
     *
     * @param sYear 星期开始的年
     * @param sMonth 月
     * @param sDay 日
     * @param eYear 星期结束的年
     * @param eMonth 月
     * @param eDay 日
     * @return 提示数据信息 */
    List<Integer> onLoadWekTasks(int sYear, int sMonth, int sDay, int eYear, int eMonth, int eDay);
}
