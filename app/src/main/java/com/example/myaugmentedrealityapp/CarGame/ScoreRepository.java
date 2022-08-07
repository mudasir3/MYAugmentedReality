package com.example.myaugmentedrealityapp.CarGame;


import android.content.Context;
import android.os.AsyncTask;


import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;

public class ScoreRepository {
    private ScoreDatabase scoreDatabase;
    public ScoreRepository(Context context){
        scoreDatabase = Room.databaseBuilder(context,
                ScoreDatabase.class,"db_score").build();
    }
    public void insertScore(int score){
        HighScore newScore = new HighScore(score,0);
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids){
                scoreDatabase.highScoreDao().insert(newScore);
                return null;
            } }.execute();
        }

    public LiveData<List<HighScore>> getScore(){
        return scoreDatabase.highScoreDao().getHighScore();
    }

    public void deleteScore(HighScore highScore){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids){
                scoreDatabase.highScoreDao().delete(highScore);
                return null;
            } }.execute();
    }


}
