package com.nnsookwon.workout_log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by nnsoo on 8/18/2016.
 */
public class Exercise {
    private ArrayList<double[]> sets; //one set is {weight, reps}
    private String date, dateSort, exerciseName, category;

    public Exercise() {
        date = "";
        dateSort = "";
        exerciseName = "";
        category = "";
        sets = new ArrayList<double[]>();
    }

    public Exercise(String nDate, String nDateSort, String nExerciseName, String nCategory) {
        date = nDate;
        dateSort = nDateSort;
        exerciseName = nExerciseName;
        category = nCategory;
        sets = new ArrayList<double[]>();
    }

    public void setExerciseName(String nExerciseName) {
        exerciseName = nExerciseName;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setCatgory(String nCategory) {
        category = nCategory;
    }

    public String getCategory() {
        return category;
    }

    public void addNewSet(double weight, double reps) {
        if (reps > 0) {
            double[] arr = {weight, reps};
            sets.add(arr);
        }
    }

    public void updateSet(int pos, double weight, double reps) {
        if (reps < 1) {
            removeSet(pos);
        } else {
            sets.get(pos)[0] = weight;
            sets.get(pos)[1] = reps;
        }
    }

    public void removeSet(int pos) {
        sets.remove(pos);
    }

    public double[] getSet(int pos) {
        return sets.get(pos);
    }

    public void setDate(String d) {
        date = d;
    }

    public String getDate() {
        return date;
    }

    public void setDateSort(String d){
        dateSort = d;
    }

    public String getDateSort(){
        return dateSort;
    }

    public ArrayList<double[]> getSetList() {
        return sets;
    }

    public static ArrayList<double[]> jsonStringToArrayList(String json) {
        //Converts Json String from SQLite into a ArrayList<double[]>
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<double[]>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public String arrayListToJsonString() {
        //convert ArrayList<double[]>sets into a Json String
        Gson gson = new Gson();
        return gson.toJson(sets);
    }

    public String getSetInfo() {
        String result = "";
        int i = 1;
        for (double[] set : sets) {
            result += set[0] + "\tlb.\t\tX\t\t" + (int) set[1] + "\t\t";
            if (set[1] > 1)
                result += "reps";
            else
                result += "rep  ";
            //extra space to light up with "reps"
            if (i++ < sets.size())
                result += "\n";
        }
        return result;
    }
}
