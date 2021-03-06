package com.boxfishedu.fishcard.timer.enums;

/**
 * Created by hucl on 16/4/15.
 */
public enum QueueTypeEnum {

    ORDER2SERVICE(1),TEACHING_ONLINE(2),TEACHING_SERVICE(3),NOTIFY_ORDER(4),NOTIFY_TIMER(5), ASSIGN_TEACHER(6), ASSIGN_TEACHER_REPLY(7),CREATE_GROUP(8),ASSIGN_TEACHER_TIMER_REPLY(9),TEACHER_OUT_NUMBER_TIMER(10),TEACHER_OUT_NUMBER_TIMER_REPLY(11);

    private int code;

    private QueueTypeEnum(int code) {
        this.code = code;
    }

    public int value() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}