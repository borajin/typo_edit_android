package com.example.typo_edit_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    final String[] SUBWAY_LINE_4_KEYWORDS = {"미남", "동래", "수안", "낙민", "충렬사", "명장", "서동", "금사", "반여농산물시장", "석대", "영산대", "동부산대학", "고촌", "안평"};
    final String[] NAVIGATION_KEYWORDS = {"안내", "경유"};
    final String[] FACILITIES_KEYWORDS = {"화장실", "역무원"};

    TypoEditSystem typoEditSystem = new TypoEditSystem();

    Context cThis;//context 설정
    String LogTT="[STT]";//LOG타이틀
    //음성 인식용
    Intent SttIntent;
    SpeechRecognizer mRecognizer;
    //음성 출력용
    TextToSpeech tts;
    boolean callcheak = false;

    // 화면 처리용
    ImageView btnSttStart;
    TextView txtInMsg;
    TextView txtSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //음성인식
        SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");//한국어 사용
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        //음성출력 생성, 리스너 초기화
        tts = new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=android.speech.tts.TextToSpeech.ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        //버튼설정
        btnSttStart=(ImageView)findViewById(R.id.btn_stt_start);
        btnSttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("음성인식 시작!");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        mRecognizer.startListening(SttIntent);

                    }catch (SecurityException e){e.printStackTrace();}
                }
            }
        });

        txtInMsg=(TextView) findViewById(R.id.txtInMsg);
        txtSystem=(TextView) findViewById(R.id.txtSystem);


        /*
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //txtSystem.setText("어플 실행됨--자동 실행-----------"+"\r\n");
                if (callcheak == false){
                    btnSttStart.performClick();
                    //mRecognizer.startListening(SttIntent);
            }}
        },1000);
        */
    }



    private RecognitionListener listener=new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            //txtSystem.setText("onReadyForSpeech..........."+"\r\n");
        }

        @Override
        public void onBeginningOfSpeech() {
            //txtSystem.setText("네 말씀하세요.."+"\r\n");
            //FuncVoiceOut("네 말씀하세요");
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            //txtSystem.setText("onBufferReceived..........."+"\r\n"); 머지?
        }

        @Override
        public void onEndOfSpeech() {
            //txtSystem.setText("인식이 완료되었습니다."+"\r\n");
            //FuncVoiceOut("네 말씀하세요");
        }

        @Override
        public void onError(int i) {
            //txtSystem.setText("천천히 다시 말해 주세요..........."+"\r\n");
        }

        @Override
        public void onResults(Bundle results) {
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            txtInMsg.setText(rs[0]+"\r\n"+txtInMsg.getText());

            FuncVoiceOrderCheck(rs[0]);
            //mRecognizer.startListening(SttIntent);
        }


        @Override
        public void onPartialResults(Bundle bundle) {
            //txtSystem.setText("onPartialResults..........."+"\r\n"); 부분적
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            //txtSystem.setText("onEvent..........."+"\r\n"); 이벤트일때?
        }
    };

    private void SystemMessage(String system_message) {
        FuncVoiceOut(system_message);
        txtSystem.setText(system_message);
    }


    //입력된 음성 메세지 확인 후 동작 처리
    private void FuncVoiceOrderCheck(String VoiceMsg){
        //if(VoiceMsg.length()<1)return;

        String navigation_keyword = typoEditSystem.typo_edit(VoiceMsg, NAVIGATION_KEYWORDS);
        String subway_line_4_keyword = typoEditSystem.typo_edit(VoiceMsg, SUBWAY_LINE_4_KEYWORDS);
        String facilities_keyword = typoEditSystem.typo_edit(VoiceMsg, FACILITIES_KEYWORDS);

        System.out.println(navigation_keyword + ", " + subway_line_4_keyword + ", " + facilities_keyword);

        if(navigation_keyword.equals("안내")) {
            if(subway_line_4_keyword.equals("ERROR")) {
                SystemMessage("알아듣지 못했어요. 다시 말씀해주세요.");
            } else {
                SystemMessage(subway_line_4_keyword + "역으로 안내하겠습니다.");
            }
        } else if(navigation_keyword.equals("경유")) {
            if(facilities_keyword.equals("ERROR")) {
                SystemMessage("알아듣지 못했어요. 다시 말씀해주세요.");
            } else {
                SystemMessage(facilities_keyword + "을 경유하여 경로를 재설정하겠습니다.");
            }
        } else {
            SystemMessage("알아듣지 못했어요. 다시 말씀해주세요.");
        }

/*
        if(callcheak == true){
            if(VoiceMsg.indexOf("안내")>-1){
                if(VoiceMsg.indexOf("장전")>-1){
                    FuncVoiceOut("장전역으로 안내하겠습니다.");
                    txtSystem.setText("장전역으로 안내하겠습니다.");}
                if(VoiceMsg.indexOf("구서")>-1){
                    FuncVoiceOut("구서역으로 안내하겠습니다.");
                    txtSystem.setText("구서역으로 안내하겠습니다.");
                }
                if(VoiceMsg.indexOf("부산대")>-1){
                    FuncVoiceOut("부산대역으로 안내하겠습니다.");
                    txtSystem.setText("부산대역으로 안내하겠습니다.");
                }
                callcheak = false;
            }
            else if(VoiceMsg.indexOf("경유")>-1 || VoiceMsg.indexOf("들렸")>-1 ){
                if(VoiceMsg.indexOf("화장실")>-1){
                    FuncVoiceOut("화장실을 경유하여 경로를 재설정하겠습니다.");
                    txtSystem.setText("화장실을 경유하여 경로를 재설정하겠습니다.");
                }
                if(VoiceMsg.indexOf("역무원")>-1){
                    FuncVoiceOut("역무원을 경유하여 경로를 재설정하겠습니다.");
                    txtSystem.setText("역무원을 경유하여 경로를 재설정하겠습니다.");
                }
                callcheak = false;
            }
            else if(VoiceMsg.indexOf("아니야")>-1)
                callcheak = false;

            else{
                FuncVoiceOut("다시 말씀해주세요.");
                txtSystem.setText("다시 말씀해주세요.");
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnSttStart.performClick();
                    }
                },2000);

            }
        }

        /*
        if(callcheak == false){
            if(VoiceMsg.indexOf("시리")>-1){
                FuncVoiceOut("네 말씀하세요");
                txtSystem.setText("네 말씀하세요.");
                callcheak = true;
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnSttStart.performClick();
                    }
                },2000);
            }
    }
    */
}



    //음성 메세지 출력용
    private void FuncVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH,null);

        //어플이 종료할때는 완전히 제거

    }
    //이동을 했는데 음성인식 어플이 종료되지 않아 계속 실행되는 경우를 막기위해 어플 종료 함수
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }
}
