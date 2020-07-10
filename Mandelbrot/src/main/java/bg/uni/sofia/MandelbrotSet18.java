package bg.uni.sofia;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class MandelbrotSet18 {
    int p = 6;
    int granularity = 1;
    boolean isQuiet = false;
    int width = 900;
    int height = 700;
    String file = "mandelbrot.png";
    float[] coordinates = new float[4];
    static int max = 255;

    public MandelbrotSet18(int width, int height, int p, int g, float[] coordinates, boolean isQuiet, String file) {
        this.width = width;
        this.height = height;
        this.p = p;
        this.granularity = g;
        this.coordinates = coordinates;
        this.isQuiet = isQuiet;
        this.file = file;
    }

    void metaCompute(Pair<Integer, Integer> startIdx) {
//        int black = 0x000000, white = 0xFFFFFF;
        int[] colors = new int[max];
        for (int i = 0; i<max; i++) {
            colors[i] = Color.HSBtoRGB(i/509f, 1, i/(i+9f));
        }

        int startX = startIdx.getKey();
        int startY = startIdx.getValue();

        List<Integer> rows = rowBuckets.get(startX);
        List<Integer> cols = colBuckets.get(startY);
//        System.out.println(Thread.currentThread().getName() + " rows: [" + rows.get(0) + " - " + rows.get(rows.size() - 1) + "] cols:[" + cols.get(0) + " - " + cols.get(cols.size() - 1) + "]");

        rows.forEach(row -> {
            cols.forEach(col -> {
                double c_re = (col - width/2f)*4.0/width;
                double c_im = (row - height/2f)*4.0/width;
                Complex number = new Complex(0, 0);
                int iteration = 0;
                while((FastMath.pow(number.getReal(), 2) + FastMath.pow(number.getImaginary(), 2) < 4) &&
                        iteration < max) {
                    Complex newNumber = new Complex(c_re, c_im).multiply(new Complex(number.getReal(), number.getImaginary()).cos());
                    iteration++;
                    number = newNumber;
                }
                if (iteration < max) image.setRGB(col, row, colors[iteration]);
                else image.setRGB(col, row, 0x150031);
            });
        });
        try {
            ImageIO.write(image, "png", new File("mandelbrot.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void rowCompute(double chunkSize, int[] startIndexesX, boolean quiet) {
        long start = System.currentTimeMillis();
        int[] colors = new int[max];
        for (int i = 0; i<max; i++) {
            colors[i] = Color.HSBtoRGB(i/509f, 1, i/(i+9f));
        }

        for(int col = 0; col < width; ++col) {
            for(int i = 0; i < startIndexesX.length; i++) {
                double c_re = (col - width/2f)*4.0/width;
                for(int row = (int) (startIndexesX[i] * chunkSize - chunkSize); row < startIndexesX[i] * chunkSize;  ++row) {
                    double c_im = (row - height / 2f) * 4.0 / width;
                    Complex number = new Complex(0, 0);
                    int iteration = 0;
                    while ((FastMath.pow(number.getReal(), 2) + FastMath.pow(number.getImaginary(), 2) < 4) &&
                            iteration < max) {
                        Complex newNumber = new Complex(c_re, c_im).multiply(new Complex(number.getReal(), number.getImaginary()).cos());
                        iteration++;
                        number = newNumber;
                    }
                    if (iteration < max) image.setRGB(col, row, colors[iteration]);
                    else image.setRGB(col, row, 0x150031);
                }
            }
        }
        try {
            ImageIO.write(image, "png", new File("mandelbrot.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(!quiet) {
                System.out.println(Thread.currentThread().getName() + " took: " + (System.currentTimeMillis() - start));
            }
        }
    }

    void rowSplit() throws InterruptedException {
        int chunks = p * granularity;
        double chunkSizeX = height / chunks;

        Thread[] threads = new Thread[p];

        for(int i = 0; i < p; ++i) {
            int[] startIndexesX = new int[granularity];
            int count = 0;
            for(int j = 1; j <= chunks; j++) {
                if (j % p == i) {
                    startIndexesX[count] = j;
                    count++;
                }
            }
            Thread current = new Thread(() -> rowCompute(chunkSizeX, startIndexesX, isQuiet));
            threads[i] = current;
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        if(!isQuiet) {
            System.out.println("Took: " + (System.currentTimeMillis() - start));
        }
    }

    List<List<Integer>> colBuckets = new ArrayList<>(p);
    List<List<Integer>> rowBuckets = new ArrayList<>(p);
    Vector<Pair<Integer, Integer>> regionVector = new Vector<>();
    List<Pair<Integer, Integer>> regionList2 = new ArrayList<>();

    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    void metaSplit() throws InterruptedException {
        int N = 0, R = 0;
        int factor = p * granularity;
        int nRows = height / factor;
        int nCols = width / factor;
        IntStream.range(0, factor).forEach(i -> {
            rowBuckets.add(new ArrayList<>());
            colBuckets.add(new ArrayList<>());
        });
        for(int i = 0; i < height; i++) {
            rowBuckets.get(N).add(i);
            if(rowBuckets.get(N).size() == nRows && (N < factor - 1)) {
                N++;
            }
        }
        for(int j = 0; j < width; j++) {
            colBuckets.get(R).add(j);
            if(colBuckets.get(R).size() == nCols && (R < factor - 1)) {
                R++;
            }
        }

        int xIdx = 0;
        for(int i = 0; i < height; i += height / factor) {
            int yIdx = 0;
            for(int j = 0; j < width; j += width / factor) {
                if(yIdx < factor && xIdx < factor) {
                    regionList2.add(new Pair<>(xIdx, yIdx));
                }
                yIdx++;
            }
            xIdx++;
        }

        List<Thread> threads = new LinkedList<>();

        for(int i = 0; i < p; i++) {
            List<Pair<Integer, Integer>> regionForCurrentThread = new LinkedList<>();
            for(int j = 0; j < regionList2.size(); j++) {
                if(j % p == i) {
                    //then thread i should work on region j
                    regionForCurrentThread.add(regionList2.get(j));
                }
            }
            threads.add(new Thread(() -> regionForCurrentThread
                    .forEach(this::metaCompute)));
        }

        long start = System.currentTimeMillis();

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Completed in " + (System.currentTimeMillis() - start));
    }

    private class MandelbrotRunnable2 implements Runnable {
        private BufferedImage image;

        public MandelbrotRunnable2(BufferedImage image) {
            this.image = image;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            ThreadLocalRandom random = ThreadLocalRandom.current();
//            while (!regionQueue.isEmpty()) {
            while(!regionVector.isEmpty()) {
                try {
                    int idx = random.nextInt(regionVector.size());
                    Pair<Integer, Integer> nextIndexes = regionVector.get(idx);
                    regionVector.remove(idx);
                    metaCompute(nextIndexes);
                }
                catch (Exception e) {
                    System.out.println("oops");
                    e.printStackTrace();
                }
            }
            System.out.println(Thread.currentThread().getName() + " completed in " + (System.currentTimeMillis() - start));
        }
    }
}

