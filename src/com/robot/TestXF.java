package com.robot;

import java.util.ArrayList;
//import com.iflytek.speech.RecognizerListener;
//import com.iflytek.speech.RecognizerResult;
//import com.iflytek.speech.SpeechError;
//import com.iflytek.speech.SpeechRecognizer;
 

public class TestXF {
	         /***
				 *      * 这里需要改成你自己的实际appid      
				 */
	//    private String version = 改成你自己实际的appid;
	//    private SpeechRecognizer recognizer;
	//    private StringBuffer rStr = new StringBuffer();
	//    /***
	//     * 初始化声音组件
	//     */
	//    {
	//        if (SpeechRecognizer.getRecognizer() == null)
	//            SpeechRecognizer.createRecognizer("appid=" + version);
	//        recognizer = SpeechRecognizer.getRecognizer();
	//    }
	// 
	//    public String listen() {
	// 
	//        if (rStr.length() != 0)
	//            rStr.setLength(0);
	//        // 开始监听语音输入，sms表示文本,vad_eos表示用户停止说话后多长时间表示结束
	//        recognizer.startListening(resultListener, "sms",
	//                "vad_bos =10000,vad_eos=2000", null);
	//        while (true) {
	//            if (recognizer.isAvaible())
	//                break;
	//        }
	//        return rStr.toString();
	//    }
	// 
	//    private RecognizerListener resultListener = new RecognizerListener() {
	// 
	//        @Override
	//        public void onCancel() {
	//        }
	// 
	//        @Override
	//        public void onEnd(SpeechError mLastError) {
	// 
	//        }
	// 
	//        @Override
	//        public void onBeginOfSpeech() {
	//        }
	// 
	//        @Override
	//        public void onEndOfSpeech() {
	// 
	//        }
	// 
	//        /**
	//         * 获取识别结果. 获取ArrayList类型的识别结果，并对结果进行累加，显示到Area里
	//         */
	//        @Override
	//        public void onResults(ArrayList results, boolean islast) {
	//            String text = "";
	//            for (int i = 0; i < results.size(); i++) {
	//                RecognizerResult result = (RecognizerResult) results.get(i);
	//                text += result.text;
	//            }
	//            rStr.append(text);
	//        }
	// 
	//        @Override
	//        public void onVolumeChanged(int volume) {
	//            if (volume == 0)
	//                volume = 1;
	//            else if (volume >= 6)
	//                volume = 6;
	// 
	//        }
	//    };
	// 
	    
	public static void main(String[] args) {
 
        TestXF t = new TestXF();
//        System.out.println("我刚才说的:" + t.listen());
//        System.out.println("第二句---------------");
//        System.out.println("我刚才说的:" + t.listen());
    }

}
