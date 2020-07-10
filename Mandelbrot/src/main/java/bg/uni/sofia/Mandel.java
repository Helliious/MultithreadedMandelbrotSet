package bg.uni.sofia;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Mandel {
    static int p = 2;
    static int width = 500;
    static int height = 500;
    static int max = 200;

    static Map<Integer, Pair<Integer, Integer>> startCoords = new HashMap<>();

    static void loadCoords() {
        int idx = 0;
        for(int i = 0; i < height; i++) {
            for(int c = 0; c < width; c++) {
                startCoords.put(idx, new Pair<>(i, c));
            }
        }
    }



    static void drawMandel(Pair<Integer, Integer> idxPair) throws IOException {
        int max = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int black = 0x000000, white = 0xFFFFFF;

        for (int row = idxPair.getKey(); row < height; row++) {
            double c_im = (row - height/2)*4.0/width;
            for (int col = idxPair.getValue(); col < width; col++) {
                System.out.println(row + ":" + col);
                double c_re = (col - width/2)*4.0/width;
                Complex number = new Complex(0, 0);
                int iteration = 0;
                while((FastMath.pow(number.getReal(), 2) + FastMath.pow(number.getImaginary(), 2) < 4) &&
                        iteration < max) {
                    Complex newNumber = new Complex(c_re, c_im).multiply(new Complex(number.getReal(), number.getImaginary()).cos());
                    iteration++;
                    number = newNumber;
                }
                if (iteration < max) image.setRGB(col, row, white);
                else image.setRGB(col, row, black);
            }
        }

        ImageIO.write(image, "png", new File("mandelbrot.png"));
    }

    private static class MandelRunnable implements Runnable {
        List<Integer> rows = new ArrayList<>();

        public MandelRunnable(List<Integer> rows) {
            this.rows = rows;
        }

        @Override
        public void run() {
            try {
                Mandel.compute(rows);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void compute(List<Integer> rows) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int black = 0x000000, white = 0xFFFFFF;
        rows.forEach(row -> {
            for(int col = 0; col < width; col++) {
                double c_re = (col - width/2)*4.0/width;
                double c_im = (row - height/2)*4.0/width;
                Complex number = new Complex(0, 0);
                int iteration = 0;
                while((FastMath.pow(number.getReal(), 2) + FastMath.pow(number.getImaginary(), 2) < 4) &&
                        iteration < max) {
                    Complex newNumber = new Complex(c_re, c_im).multiply(new Complex(number.getReal(), number.getImaginary()).cos());
                    iteration++;
                    number = newNumber;
                }
                if (iteration < max) image.setRGB(col, row, white);
                else image.setRGB(col, row, black);
            }
        });
        try {
            ImageIO.write(image, "png", new File("mandelbrot.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void splitIntoBuckets() {
        List<Thread> threads = new LinkedList<>();
        int rc, N = 0, n_rows = height / p;
        List<List<Integer>> buckets = new ArrayList<>(p);
        for(int i = 0; i < height; i++) {
            if(Objects.isNull(buckets.get(N))) {
                buckets.set(N, new ArrayList<>());
            }
            buckets.get(N).add(i);
            if(buckets.get(N).size() == n_rows && (N < p - 1)) {
                N++;
            }
        }
        for(int i = 0; i < p; i ++) {
//            threads.add(new Thread(Mandel::compute(buckets.get(i))));
            int finalI = i;
            new Thread(() -> compute(buckets.get(finalI))).start();
        }
//        thread t[::NUM_THREADS];
//        // Split window by height
//        int rc, N = 0, n_rows = ::height / ::NUM_THREADS;
//        vector< vector<int>> buckets( ::NUM_THREADS );
//        for (int y = 0; y < ::height; ++y) {
//            buckets[N].push_back(y);
//            if ( (buckets[N].size() == n_rows) && (N < (::NUM_THREADS - 1)) ) {
//                N++;
//            }
//        }
//        // compute mandelbrot set on each chunk
//        for( int i = 0; i < ::NUM_THREADS; ++i ) {
//            t[i] = thread(compute_mandelbrot, buckets[i]);
//        }
    }

    public static void main(String[] args) {
        splitIntoBuckets();
    }
}
