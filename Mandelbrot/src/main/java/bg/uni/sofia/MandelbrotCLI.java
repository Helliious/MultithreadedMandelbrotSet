package bg.uni.sofia;
import org.apache.commons.cli.*;

public class MandelbrotCLI {
    private static Options createOptions() {
        Option size = new Option("s", "size", true, "size of the image");
        Option rect = new Option("r", "rect", true, "coordinates of the rectangle");
        Option task = new Option("t", "tasks", true, "number of threads");
        Option output = new Option("o", "output", true, "output file name");
        Option quiet = new Option("q", "quiet", false, "minimize log messages");

        Options options = new Options();
        options.addOption(size);
        options.addOption(rect);
        options.addOption(task);
        options.addOption(output);
        options.addOption(quiet);

        return options;
    }

    private static CommandLine createCmd(Options options, String[] args) {
        CommandLineParser parser = new DefaultParser();

        try {
            return parser.parse(options, args);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Parsing error!");
        }
    }

    private static MandelbrotSet18 createMandelbrot(CommandLine cmd) {
        int width = 800, height = 600;
        if(cmd.hasOption("s")) {
            String arg = cmd.getOptionValue("s");
            String[] res = arg.split("x");
            width = Integer.parseInt(res[0]);
            height = Integer.parseInt(res[1]);

            if(width < 0 || height < 0) {
                throw new IllegalArgumentException("width and height must be positive integers");
            }
        }

        float[] coordinates = new float[4];
        coordinates[0] = -2f;
        coordinates[1] = 2f;
        coordinates[2] = -2f;
        coordinates[3] = 2f;
        if(cmd.hasOption("r")) {
            String arg = cmd.getOptionValue("r");
            String[] res = arg.split(":");
            coordinates[0] = Float.parseFloat(res[0]);
            coordinates[1] = Float.parseFloat(res[1]);
            coordinates[2] = Float.parseFloat(res[2]);
            coordinates[3] = Float.parseFloat(res[3]);
        }

        int countOfThreads = 1;
        if(cmd.hasOption("t")) {
            countOfThreads = Integer.parseInt(cmd.getOptionValue("t"));

            if(countOfThreads < 1) {
                throw new IllegalArgumentException("Exception threads number > 0");
            }
        }

        String nameOfFile = "zad18.png";
        if(cmd.hasOption("o")) {
            nameOfFile = cmd.getOptionValue("o");
        }

        boolean isQuiet = false;
        if(cmd.hasOption("q")) {
            isQuiet = true;
        }

        int granularity = 1;
        if(cmd.hasOption("g")) {
            granularity = Integer.parseInt(cmd.getOptionValue("g"));
        }

        return new MandelbrotSet18(width, height, countOfThreads, granularity, coordinates, isQuiet, nameOfFile);
    }

    public static void startGeneration(String[] args) throws InterruptedException {
        Options options = createOptions();
        CommandLine cmd = createCmd(options, args);
        MandelbrotSet18 mandelbrot = createMandelbrot(cmd);

        mandelbrot.rowSplit();
//        mandelbrot.metaSplit();
    }

    public static void main(String[] args) {
        try {
            startGeneration(args);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
