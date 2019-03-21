package com.koflance.lt.others;

import org.apache.commons.cli.*;

/**
 * Created by liujun on 2017/11/11.
 *
 * note: write down 'test --block-size=12 abc tce -O filename' in the program arguments
 */
public class TestCli {

    private static String cliFormatStr = "gmkdir [-p][-v/--verbose][--block-size][-h/--help] DirectoryName";
    private static String header = "Do something useful with an input file\n\n";
    private static String footer = "\nPlease report issues at http://example.com/issues";
    private static HelpFormatter helpFormatter = new HelpFormatter();
    private static Options options = new Options();

    static {
        options.addOption(Option.builder("bz").longOpt("block-size").hasArg().valueSeparator('=').desc("use SIZE-byte blocks.").build());
        options.addOption(Option.builder("O").desc("search for buildfile towards the root of the filesystem and use it.").argName("file").hasArg().build());
        options.addOption("p", "no error if existing, make parent directories as needed.");
        options.addOption("v", "verbose", false,"explain what is being done.");
        options.addOption("h", "help", false,"print help for the command.");
        options.addOption(Option.builder("D").hasArgs().valueSeparator('=').desc("use value for given property").build());
    }

    public static void main(String[] args) {
        args = "test -Djava=new --block-size=12 abc tce -O filename".split(" ");
        DefaultParser defaultParser = new DefaultParser();
        CommandLine parse = null;
        try {
            parse = defaultParser.parse(options, args);
        } catch (ParseException e) {
            printHelp(true);
            return;
        }

        if(parse.hasOption("h")){
            printHelp(false);
            return;
        }

        if(parse.hasOption("block-size")){
            System.out.println("block-size=" + parse.getOptionValue("block-size"));
        }

        String[] args1 = parse.getArgs();
        if(args1 != null && args1.length > 0){
            for (String s : args1) {
                System.out.println(s);
            }
        }

        if(parse.hasOption("O")){
            System.out.println("file=" + parse.getOptionValue("O"));
        }

        if(parse.hasOption("D")){
            System.out.println("key=" + parse.getOptionValues("D")[0]);
            System.out.println("value=" + parse.getOptionValues("D")[1]);
        }
    }

    private static void printHelp(boolean isError){
        if(isError){
            helpFormatter.printHelp(cliFormatStr, options, true);
        }else{
            helpFormatter.printHelp(cliFormatStr, header, options, footer);
        }
    }
}
