package com.example.myapplication_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.media.SoundPool;
import android.os.Bundle;
import android.media.AudioManager;


import java.util.Random;

public class Game extends View{

    //스크린 폭, 높이 저장 변수
    int scrw, scrh;
    //캐릭터 이미지 기준점 x좌표, y좌표 저장
    float xd, yd;
    //------------------------적 (3마리) 생성--------------------------
    //1. 적 x좌표
    float[] rxd = new float[3];
    //2. 적 y좌표
    float[] ryd = new float[3];
    //3. 적 이동 카운트
    int[] count2 = new int[3];
    //4. 적 생명
    int[] life = new int[3];
    //5. 적군 이동 방향
    private String[] RectDirButton = new String[3];

    //----------------------캐릭터 방향 및 버튼 설정----------------------------------
    //캐릭터 이미지 기준점의 x좌표, y좌표 위치 값 저장
    //int xd, yd;
    //카운트 이용
    int count = 0;
    //카운트 시작 유무
    boolean start = false;
    //클릭 방향 버튼 판별
    private String DirButton;
    private String DirButton2;

    //---------------------------미사일 설정----------------------------------
    int missileCount; // 발사 가능한 최대 미사일 수
    int[] missileNum = new int[10]; //미사일 번호
    float [] mx = new float[10];   // 미사일 x 위치
    float [] my = new float[10];   // 미사일 y 위치
    int [] md = new int[10];   // 미사일 방향
    int MD = 4;     // 미사일 초기 방향 (왼, 오, 위, 아래 > 순서대로)

    //적 랜덤 변수
    Random random = new Random();

    //왼쪽 눌렀는지, 오른쪽 눌렀는지 판별
    int n;
    //페인트 정보 저장장
    Paint p = new Paint();
    private GameThread T;

    MediaPlayer mp;
    SoundPool sound = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
    int soundId;


    public Game(Context con, AttributeSet at) {
        super(con, at);

        mp = MediaPlayer.create(con, R.raw.timeloop);
        mp.start();
        mp.setLooping(true);
    }

    @Override
    //뷰 크기 변경될 때
    protected void onSizeChanged(int sw, int sh, int esw, int esh){
        super.onSizeChanged(sw, sh, esw, esh);
        this.scrw = sw; //스크린 너비
        this.scrh = sh; //스크린 높이

        //적 생명력 저장
        for(int i = 0; i < 3; i++){
            //최대 2번까지 맞기 가능능
           life[i] =2;
        }

        if (T==null){
            T = new GameThread();
            T.start();
        }
    }

    @Override
    //뷰 윈도우에서 분리될때마다 발생
    protected void onDetachedFromWindow(){
        T.run = false;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas){
        //적1 색상 설정 변수 및 그림 생성
        Paint rect = new Paint();
        rect.setColor(Color.BLACK);
        canvas.drawRect(0, 0, scrw, scrh, rect);

        //적2
        Paint rect2 = new Paint();
        rect2.setColor(Color.WHITE);
        canvas.drawRect(scrw%64/2, scrh%32/2, scrw-scrw%64/2, scrh-scrh%32/2, rect2);

        p.setColor(Color.BLACK);
        p.setTextSize(scrh/16);

        //canvas.drawText("sw"+scrw+"sh"+scrh, 0, scrh/16,p);

        Bitmap[] main = new Bitmap[12];
        Bitmap[] missile = new Bitmap[10];
        
        //미사일 수 0으로 설정
        missileCount = 0;

        //----------------------------------misslie-----------------------------------------
        for(int i=0; i<10; i++){
            missile[i] = BitmapFactory.decodeResource(getResources(), R.drawable.missile);  //미사일 파일 이미지 정보
            missile[i] = Bitmap.createScaledBitmap(missile[i], scrw / 16, scrw / 16, true); //미사일 크기
            
            //미사일 비활성시
            if(missileNum[i] == 0){
                missileCount += 1;
            }

            if(missileNum[i] == 1){    //미사일 활성 > 발사
                canvas.drawBitmap(missile[i], mx[i], my[i], null);
                //left
                if(md[i] == 1){
                    mx[i] -= scrw / 64;
                }
                //Right
                if(md[i] == 2){
                    mx[i] += scrw / 64;
                }
                //Up
                if(md[i] == 3){
                    my[i] -= scrh / 32;
                }
                //Down
                if(md[i] == 4){
                    my[i] += scrh/ 32;
                }
            }

            for(int j=0; j<3; j++){
                //생명력 있는 j번째 적 > 미사일 맞았을시 사라짐
                if(life[j] > 0  && mx[i] <= scrw/2 +(scrw-scrw%64) / 8 + rxd[j] && mx[i] >= scrw/2 + rxd[j]){
                    life[j] -= 1;   // life 감소
                    missileNum[i] = 0;  // 미사일 삭제
                }
            }

            //화면 너비 지정 ( 벽에 닿았을 시 사라짐 )
            if(mx[i] > scrw - scrw / 16 || mx[i] < 0 || my[i] > scrh - scrh / 16 || my[i] < 0){
                missileNum[i] = 0;
            }
        }

    //-------------------------character 그림 ----------------------------
        for(int i=0; i<12; i++){
            //그림 파일 경로
            main[i] = BitmapFactory.decodeResource(getResources(), R.drawable.character01 + i);
            //그림 파일 크기 설정
            Bitmap.createScaledBitmap(main[i], (scrw-scrw%64)/8, (scrh-scrh%32)/4, true);
            if(i == n) {
                canvas.drawBitmap(main[i], (float)scrw / 2 + xd, (float)scrh - scrh / 2 + yd, null);
            }
        }
        //적 그리기
        for(int i = 0; i <3; i++) {
            Paint rect3 = new Paint();
            rect3.setColor(Color.RED);
            //life가 있을경우 적 그리기
            if(life[i] > 0) canvas.drawRect(scrw/2+rxd[i], scrh/2+ryd[i], scrw/2 + (scrw-scrw%64)/8 + rxd[i], scrw/2 + (scrh-scrh%32) / 4 + ryd[i], rect3);
        }

        Bitmap IS [][] = new Bitmap[1][4];
        Bitmap I = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.dir);
        I = Bitmap.createScaledBitmap(I, scrw/8, scrh, true);
        //파일 경로 저장 및 분할, 화면에 나올 크기 지정

        for(int i=0; i<1; i++){
            for(int j=0; j<4; j++){
                IS[i][j] = Bitmap.createBitmap(I, i*scrw/8, j*scrh/4, scrw/8, scrh/4);
            }
        }
        //UP
        canvas.drawBitmap(IS[0][0], scrw / 8, scrh-scrh/2, null);
        //Left
        canvas.drawBitmap(IS[0][1], 0, scrh-scrh/4, null);
        //Right
        canvas.drawBitmap(IS[0][2], scrw / 4, scrh-scrh/4, null);
        //Down
        canvas.drawBitmap(IS[0][3], scrw / 8, scrh-scrh/4, null);

        //적 생명력 표시
        canvas.drawText("적 생명력 : " + life[0] + "적 생명력 : " + life[1] + "적 생명력 : " + life[2] + "발사 가능한 미사일 수 : " + missileCount, 0, scrh/16, p);
    }

    @Override
    //스크린 터치 이벤트 처리
    public boolean onTouchEvent(MotionEvent event) {
        //화면 터치시
        if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            //Right Button
            if((int)event.getX() > scrw/4 && (int)event.getX() < scrw*3 / 8 && (int)event.getY() < scrh && (int)event.getY() > scrh-scrh / 4){
                if(start == false && count == 0) {
                    start = true;
                    DirButton = "Right";
                }
                DirButton2 = "Right";
            }
            //Left Button
            else if((int) event.getX() > 0 && (int)event.getX() < scrw / 8 && (int) event.getY() < scrh && (int)event.getY() > scrh - scrh / 4){
                if(start == false && count == 0){
                    start = true;
                    DirButton = "Left";
                }
                DirButton2 = "Left";
            }
            //Up Button
            else if ((int)event.getX() > scrw / 8 && (int)event.getX() < scrw / 4 && (int)event.getY() < scrh - scrh/4 && (int) event.getY() > scrh - scrh / 2){
                if(start == false && count == 0) {
                    start = true;
                    DirButton = "Up";
                }
                DirButton2 = "Up";
            }
            //Down Button
            else if((int)event.getX() > scrw / 8 && (int)event.getX() < scrw / 4 && (int)event.getY() < scrh && (int)event.getY() > scrh - scrh / 4){
                if(start == false && count == 0){
                    start = true;
                    DirButton = "Down";
                }
                DirButton2 = "Down";
            }

            //미사일 관련 위치 정보
            else if((int)event.getX() > scrw / 2){
                for (int i = 0; i < 10; i++){
                    //i번째 미사일 비활성화 상태
                    if(missileNum[i] == 0){
                        mx[i] = scrw / 2 + (scrw / 8 - scrw / 16)/2 + xd;
                        my[i] = scrh / 2 + (scrh / 4 - scrw / 16)/2 + yd;
                        md[i] = MD;
                        missileNum[i] = 1;

                        if(missileCount != 0) missileCount -= 1;

                        break;
                    }
                }
            }
            //버튼 클릭을 하지 않을 때
            else {
                start  = false;
            }
        }
        //터치하다가 손을 땠을 때
        if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_POINTER_UP){
            //Right Button
            if((int)event.getX() > scrw/4 && (int)event.getX() < scrw*3 / 8 && (int)event.getY() < scrh && (int)event.getY() > scrh-scrh / 4){
                start = false;
            }
            //Left Button
            else if((int) event.getX() > 0 && (int)event.getX() < scrw / 8 && (int) event.getY() < scrh && (int)event.getY() > scrh - scrh / 4){
                start = false;
            }
            //Up Button
            else if((int)event.getX() > scrw / 8 && (int)event.getX() < scrw / 4 && (int)event.getY() < scrh - scrh/4 && (int) event.getY() > scrh - scrh / 2){
                start= false;
            }
            //Down Button
            else if((int)event.getX() > scrw / 8 && (int)event.getX() < scrw / 4 && (int)event.getY() < scrh && (int)event.getY() > scrh - scrh / 4){
                start = false;
            }
        }
        return true;
    }

    class GameThread extends Thread {
        public boolean run = true;

        @Override
        public void run(){  //무한루프
            while(run){
                try{
                    postInvalidate();   //뷰에서 이미지 분리

                    if(count == 8){
                        count = 0;
                        DirButton = DirButton2;
                    }
                    //*****************적 정보 뷰*******************
                    for(int i = 0; i <3; i++){
                        if(count2[i] == 8){
                            count2[i] = 0;
                        }

                        if(count2[i] == 0){
                            //1~4 랜던값
                            int r = random.nextInt(4 - 1 + 1) + 1;

                            if(r == 1){
                                RectDirButton[i] = "Left";
                            }

                            if(r == 2){
                                RectDirButton[i] = "Right";
                            }

                            if(r == 3){
                                RectDirButton[i] = "Up";
                            }

                            if(r == 4){
                                RectDirButton[i] = "Down";
                            }
                        }
                        //i번째 생명이 남았고, 방향 : Down
                        if(life[i] > 0 &&  RectDirButton[i] == "Down"){
                            if(scrh / 2 + ryd[i] < scrh - scrh / 4 - (scrh%32)/2){
                                ryd[i] += scrh / 32;
                            }
                        }

                        //i번째 생명이 남았고, 방향 : UP
                        if(life[i] > 0 && RectDirButton[i] == "Up"){
                            if(scrh/2 + ryd[i] > (scrh%32)/2){
                                ryd[i] -= scrh / 32;
                            }
                        }

                        //i번째 생명이 남았고, 방향 : Left
                        if(life[i] > 0 && RectDirButton[i] == "Left"){
                            if(scrw / 2 + rxd[i] > (scrw%64)/2){
                                rxd[i] -= scrw / 64;
                            }
                        }

                        //i번째 생명이 남았고, 방향 : Right
                        if(life[i] > 0 && RectDirButton[i] == "Right"){
                            if(scrw / 2 + rxd[i] < scrw - scrw/8 - (scrw%64)/2){
                                rxd[i] += scrw / 64;
                            }
                        }
                    }

                    //******************캐릭터*************************
                    //버튼 누른 상태에 따른 이미지 바꾸기
                    //1. Down Button
                    if(start == true && DirButton == "Down" && count != 20 || start == false && count > 0 && count < 20 && DirButton=="Down"){
                        if(count % 4 == 0){
                            yd += scrh / 32;
                            n = 0;
                            MD = 4;
                        }
                        else if(count % 4 == 1 || count % 4 == 3){
                            yd += scrh / 32;
                            n = 1;
                        }
                        else if(count % 4 == 2){
                            yd += scrh / 32;
                            n = 2;
                        }
                    }

                    //2. Up Button
                    if(start == true && DirButton == "Up" && count != 20 || start == false && count > 0 && count < 20 && DirButton == "Up"){
                        if(count % 4 == 0){
                            yd -= scrh / 32;
                            n = 6;
                            MD = 3;
                        }
                        else if(count % 4 == 1 || count % 4 == 3){
                            yd -= scrh / 32;
                            n = 7;
                        }
                        else if(count % 4 == 2){
                            yd -=scrh / 32;
                            n = 8;
                        }
                    }

                    //3. Left Button
                    if(start == true && DirButton == "Left" && count != 20 || start == false && count > 0 && count < 20 && DirButton == "Left"){
                        if(count % 4 == 0){
                            xd -= scrw / 64;
                            n = 3;
                            MD = 1;
                        }
                        else if(count % 4 == 1 || count % 4 == 3){
                            xd -= scrw / 64;
                            n = 4;
                        }
                        else if(count % 4 == 2){
                            xd -= scrw / 64;
                            n = 5;
                        }
                    }

                    //4. Right Button
                    if(start == true && DirButton == "Right" && count != 20 || start == false && count > 0 && count < 20 && DirButton == "Right"){
                        if(count % 4 == 0){
                            xd += scrw / 64;
                            n = 9;
                            MD = 2;
                        }
                        else if(count % 4 == 1 || count % 4 == 3){
                            xd += scrw / 64;
                            n = 10;
                        }
                        else if(count % 4 == 2){
                            xd += scrw / 64;
                            n = 11;
                        }
                    }

                    if(start == true && count == 0){
                        count  += 1;
                    }
                    else{
                        if(count > 0 && count < 8) count += 1;
                    }

                    for(int i = 0; i < 3; i++){
                        //적 생명 > 0
                        if(life[i] > 0){
                            count2[i] += 1;
                        }
                    }

                    sleep(100);  // 속도 조절
                    // 예외사항
                } catch (Exception e){


                }
            }
        }
    }
}
