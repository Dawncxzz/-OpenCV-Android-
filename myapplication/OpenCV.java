package com.example.myapplication;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class OpenCV {
    private static int minEdge;
    static {
        //在使用OpenCV前必须加载Core.NATIVE_LIBRARY_NAME类,否则会报错
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCVLoader.initDebug();
    }

    public static Results houghLines(Bitmap bitmap) {


        minEdge  = bitmap.getHeight() / 10;
//        Log.e("宽高","宽" +bitmap.getWidth() + "高"+bitmap.getHeight() + " " + minEdge);
        Mat src = new Mat();
        Mat gary=new Mat();
        Mat lines=new Mat();
        Mat gray = new Mat();
        Mat BW = new Mat();
        Utils.bitmapToMat(bitmap,gray);
        Utils.bitmapToMat(bitmap,src);
        //1.得到灰度图
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY,0);
        //2.边缘处理
        Imgproc.Canny(gray, gary, 100, 380);
        //3.霍夫变换-直线检测
        Imgproc.HoughLinesP(gary,lines, 1,Imgproc.HOUGH_GRADIENT/180.0, bitmap.getHeight() / 7, bitmap.getHeight() / 10, bitmap.getHeight()  / 100);
//        Imgproc.HoughLines(gary,lines,1,Imgproc.HOUGH_GRADIENT/180.0, 6);

        double[] date;
        double sum = 0,tan,atan,angle,count = 0,distance;

        //旋转处理
//        Log.e("123123","lines:" + lines.rows());
        for(int i=0,len=lines.rows();i<len;i++) {
            date=lines.get(i, 0).clone();

            tan = (date[3] - date[1]) / (date[2] - date[0]);
            distance = Math.sqrt(Math.pow(date[3] - date[1],2) + Math.pow(date[2] - date[0],2));
            atan = Math.atan(tan);
            angle = Math.toDegrees(atan);
            if(angle > -45 && angle < 45){
                sum +=angle;
                count++;
            }
//            //直线显示
//            Imgproc.line(src,new Point((int)date[0],(int)date[1]), new Point((int)date[2],(int)date[3]) ,new Scalar(255, 0, 0) , 1, Imgproc.LINE_AA);
        }
        angle = count == 0 ? 0 : sum/ count;

        /* 图像旋转矫正开始 */
        gray = rotate(gray, angle);
        src = rotate(src, angle);
        /* 图像旋转矫正结束 */

        Imgproc.medianBlur(gray,gray,5);

        //图像二值化
        int blockSize = 0;
        if(((bitmap.getWidth() + bitmap.getHeight()) / 10) % 2 == 0){
            blockSize = (bitmap.getWidth() + bitmap.getHeight()) / 10 + 1;
        }else{
            blockSize = (bitmap.getWidth() + bitmap.getHeight()) / 10;
        }
        Imgproc.threshold(gray, BW, 32, 255, Imgproc.THRESH_BINARY);
//        Imgproc.adaptiveThreshold(gray, BW, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, blockSize,0);
        Bitmap newbitMap = Bitmap.createBitmap(bitmap);

        //获取行列矩阵
        Mat rowMat = sum1(BW);
        Mat colMat = sum2(BW);

        //获取行列块数
        int recCount1 = checkNoise(rowMat);
        int recCount2 = checkNoise(colMat);

        Log.e("recCount1", ":" + recCount1 + " rowMat:" + rowMat.dump());
        Log.e("recCount2", ":" + recCount2 + " colMat:" + colMat.dump());

        //获取两个矩阵的起始位置和结束位置
        int[] st1 = startPos(rowMat, recCount1);
        int[] en1 = endPos(rowMat,recCount1);
        int[] st2 = startPos(colMat, recCount2);
        int[] en2 = endPos(colMat,recCount2);

        //根据参数计算占比
        double rest = Calculate(BW, recCount1, recCount2, st1, en1, st2, en2);
        double result = rest/ (recCount1 * recCount2);

        //参数返回
        Utils.matToBitmap(src, newbitMap);
//        Log.e("result",":" + result);
        return new Results(result, newbitMap);
//        return newbitMap;
    }

    //旋转函数
    public static Mat rotate(Mat src, double angle) {
        Mat dst = src.clone();
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, angle, 1.0);
        Imgproc.warpAffine(src, dst, affineTrans, dst.size(), Imgproc.INTER_NEAREST);
        return dst;
    }

    //非转置列相加函数
    public static Mat sum1(Mat src){
        Mat mat = new Mat(1,src.cols(), CvType.CV_8UC1,new Scalar(0));
        int channels = src.channels();
        byte[] data = new byte[channels];
        byte[] data3 = new byte[channels];
        int check,k,count;
        for(int col = 0; col < src.cols();col++){
            check = 0;
            count = 0;
//            Log.e("矩阵检测到不是方块（宽度不足）","col:" + col + " " + src.col(col).dump());
            for(int row = 0; row < src.rows();row++){
                //读取像素点
                src.get(row,col,data);
//                k = 1;
                if((data[0] & 0xff) == 255){
//                    check = 1;
                    count ++;
                    //方块检测

//                    while(row + k < src.rows() && k<minEdge){
//                        src.get(row + k,col ,data3);
//                        if((data3[0] & 0xff) == 0){
////                            Log.e("矩阵检测到不是方块（宽度不足）","row:" + row + " col:" + col);
//                            break;
//                        }
//                        k++;
//                    }
                }

//                if(k == minEdge)
//                    break;
//                else {
//                    check = 0;
//                    row += k;
//                }
//                Log.e("矩阵检测到不是方块（宽度不足）","check:" + check + " row:" + row);
            }
            //保存修改
            Log.e("456","count:" + count + "src.rows()" + src.rows() * 0.95);
            if( count >= src.rows() * 0.95 || count < src.rows() * 0.05 || col == src.cols() - 1) {
                data[0] = (byte)0;
                mat.put(0, col, data);
            }
            else {
                data[0] = (byte)255;
                mat.put(0, col, data);
            }
        }
        return mat;
    }

    //转置列相加函数
    public static Mat sum2(Mat src){
        Mat mat = new Mat(1,src.rows(), CvType.CV_8UC1,new Scalar(0));
        int channels = src.channels();
        byte[] data = new byte[channels];
        byte[] data3 = new byte[channels];
        int check,k,count;
//        Log.e("矩阵检测到不是方块（宽度不足）","row:" + src.rows());
        for(int row = 0; row < src.rows(); row++){
            check = 0;
            count = 0;
//            Log.e("矩阵检测到不是方块（宽度不足）","row:" + row + " " + src.row(row).dump());
            for(int col = 0; col < src.cols(); col++){
                //读取像素点
                src.get(row,col,data);

//                k = 1;
                if((data[0] & 0xff) == 255){
//                    check = 1;
                    count++;
                    //方块检测

//                    while(col + k < src.cols() && k<minEdge){
//                        src.get(row,col + k ,data3);
//                        if((data3[0] & 0xff) == 0){
//                            check = 0;
////                            Log.e("矩阵检测到不是方块（宽度不足）","row:" + row + " col:" + col);
//                            break;
//                        }
//                        k++;
//                    }
                }
//                if(k == minEdge)
//                    break;
//                else {
//                    check = 0;
//                    row += k;
//                }
            }
//            Log.e("矩阵检测到不是方块（宽度不足）","row:" + row + " check:" + check);
            //保存修改
            Log.e("123","count:" + count + "src.cols()" + src.cols() * 0.95);
            if( count >= src.cols() * 0.95 ||  count < src.cols() * 0.05 || row == 0 || row == src.rows() - 1) {
                data[0] = (byte)0;
                mat.put(0, row, data);
            }
            else {
                data[0] = (byte)255;
                mat.put(0, row, data);
            }
        }
        return mat;
    }

    //噪声检查函数
    public static int checkNoise(Mat src){
        int channels = src.channels();
        byte[] data = new byte[channels];
        byte[] data2 = new byte[channels];
        byte[] data3 = new byte[channels];
//        Log.e("123123",":" + src.dump());
        int check,recCount=0;
        for(int row=0; row < src.rows(); row++){
            for(int col=1; col < src.cols(); col++){
                src.get(row,col,data);
                src.get(row,col - 1,data2);
                if((data[0] & 0xff) == 255 && (data2[0] & 0xff) == 0){
                    check = 0;
                    for(int k=1;k<minEdge;k++){
                        if(col + k < src.cols()){
                            src.get(row,col + k ,data3);
                            if((data3[0] & 0xff) == 0){
                                check = 1;
//                                Log.e("进来",":" + check);
                            }
                        }
                    }
                    if(check == 0){
                        recCount++;
                    }
                }
            }
        }
        return recCount;
    }

    //起始位置数组函数
    public static int[] startPos(Mat src, int recCount){
        int channels = src.channels();
        byte[] data = new byte[channels];
        byte[] data2 = new byte[channels];
        byte[] data3 = new byte[channels];
        int count=0,k;
//        Log.e("123123",":" + recCount);
        int[] sP = new int[recCount];
        for(int row=0; row < src.rows(); row++){
            for(int col=1; col < src.cols(); col++){
                src.get(row,col,data);
                src.get(row,col - 1,data2);
                if((data[0] & 0xff) == 255 && (data2[0] & 0xff) == 0){
                    k = 1;
                    while(col + k < src.cols()){
                        src.get(row,col + k ,data3);
                        if((data3[0] & 0xff) == 255){
                            k++;
                        }
                        else {
                            break;
                        }
                    }
                    if(k > minEdge){
                        sP[count] = col;
                        count++;
                    }
                    col += k;
                }
            }
        }
        return sP;
    }

    //结束位置数组函数
    public static int[] endPos(Mat src, int recCount){
        int channels = src.channels();
        byte[] data = new byte[channels];
        byte[] data2 = new byte[channels];
        byte[] data3 = new byte[channels];
        int count=0,k;
        int[] eP = new int[recCount];
//        Log.e("123123","recCount:" + recCount);
        for(int row = 0; row < src.rows(); row++){
            for(int col=src.cols(); col > 0; col--){
                src.get(row,col,data);
                src.get(row,col - 1,data2);
                if((data[0] & 0xff) == 0 && (data2[0] & 0xff) == 255){
                    k = 1;
                    while(col - k > 0){
                        src.get(row,col - k ,data3);
                        if((data3[0] & 0xff) == 255){
                            k++;
                        }
                        else {
                            break;
                        }
                    }
                    if(k > minEdge){
                        eP[recCount - count - 1] = col - 1;
                        count++;
                    }
                    col -= k;
                }
            }
        }
        return eP;
    }

    //计算百分比函数
    public static double Calculate(Mat src,int recCount1,int recCount2,int st1[],int en1[],int st2[],int en2[]){
        int channels = src.channels();
        byte[] data = new byte[channels];
        double count;
        double rest = 0;
        for(int i=0; i<recCount1; i++){
            for(int j=0; j<recCount2; j++){
                count = 0;
                for(int xx = st1[i];xx<en1[i];xx++){
                    for(int yy = st2[j];yy<en2[j];yy++){
                        src.get(xx,yy,data);
                        if((data[0] & 0xff) == 255)
                            count ++;
                    }
                }
//                Log.e("浓度检测：","x范围:" + "(" + st1[i] + "," + en1[i] + ")" + " y范围:(" +  st2[j] + "," + en2[j] + ") " + " 浓度:" + (double)count/((en1[i] - st1[i]) * (en2[j] - st2[j])));
                rest += count/((en1[i] - st1[i]) * (en2[j] - st2[j]));
            }
        }
        return rest;
    }
}


